"""
This module contains a set of classes and functions to handle object detection and classification.
"""

import cv2
import datetime
import math
import numpy as np
import os
import pathlib
import tensorflow as tf
import tensorflow.keras as K
import threading
import time

from keras_preprocessing.image import ImageDataGenerator

# vision configuration settings

root_dir = pathlib.Path(os.path.dirname(os.path.realpath(__file__)))
data_dir = root_dir.joinpath('data')

class LabelDataSet:
    """
    This class encapsulates all supported labels.
    """

    def __init__(self,
                 max_labels: int = 1000):
        self.max_labels = max_labels
        self.labels = [None] * max_labels
        self.labels_dict_key_to_idx = {}

    def add_label(self, label_key: str) -> int:
        if label_key in self.labels_dict_key_to_idx:
            return self.labels_dict_key_to_idx[label_key]

        free_idx = None

        for idx, value in enumerate(self.labels):
            if value is None:
                free_idx = idx
                break

        if free_idx is None:
            raise Exception('Max label count reached.')

        self.labels[free_idx] = label_key
        self.labels_dict_key_to_idx[label_key] = free_idx

    def encode(self, label_key: str) -> int:
        return self.labels_dict_key_to_idx[label_key]

    def decode(self, label_idx: int) -> str:
        return self.labels[label_idx]

    def __contains__(self, item):
        return item in self.labels_dict_key_to_idx

    @staticmethod
    def save(labelDataSet, path: str):
        """
        Saves a LabelDataSet object to a file.

        :param labelDataSet: A LabelDataSet object.
        :param path: A file path.
        """

        with open(path, 'w') as file:
            for idx, label in enumerate(labelDataSet.labels):
                if label is None:
                    file.write(f'{idx}=\n')
                else:
                    file.write(f'{idx}={label}\n')

    @staticmethod
    def load(path: str):
        """
        Loads a LabelDataSet object from a file.

        :param path: A file path.
        :return: Returns a LabelDataSet object.
        """

        try:
            with open(path, 'r') as file:
                lines = file.readlines()

                labelDataSet = LabelDataSet(len(lines))

                for idx, label in enumerate(lines):
                    v = label.split('=')
                    if v[1] != '\n':
                        labelDataSet.add_label(v[1].replace('\n',''))

                return labelDataSet
        except:
            return None

    @staticmethod
    def load_from_directory(labels_path: str, label_data_set = None):
        """
        Either creates a new LabelDataSet object or populates the label_data_set based on a directory where every
        subdirectory is considered as an label.

        :param labels_path: A director path.
        :param label_data_set: (optional) A LabelDataSet object.
        :return: Returns a LabelDataSet object.
        """

        labels = []

        for child in pathlib.Path(labels_path).iterdir():
            dir_name = child.parts[-1]

            labels.append(dir_name)

        labels.sort()

        if label_data_set is None:
            label_data_set = LabelDataSet()

        for label in labels:
            label_data_set.add_label(label)

        return label_data_set


class ExampleData:
    """
    This class encapsulates a single example which can be used for training, validation or evaluation.
    """

    def __init__(self,
                 label: str,
                 data_path: str):
        self.label = label
        self.data_path = data_path


class ExampleDataSet:
    """
    This class encapsulates an entire set of examples which can be used for training, validation or evaluation.
    """

    def __init__(self,
                 example_data: [ExampleData],
                 verbose: int = 0):
        self.example_data = example_data
        self.example_data_dict = {}
        self.verbose = verbose

        for e in example_data:
            if e.label not in self.example_data_dict:
                self.example_data_dict[e.label] = []

            self.example_data_dict[e.label].append(e.data_path)

        if self.verbose == 1:
            print(self.__str__())

            keys = list(self.example_data_dict.keys())

            for key in keys:
                print(f" label: {key}, examples: {len(self.example_data_dict[key])}")

    def __len__(self):
        return len(self.example_data)

    def __getitem__(self, key):
        return self.example_data[key]

    def __str__(self):
        return f'ExampleDataSet(examples={len(self.example_data)}, labels={len(self.example_data_dict.keys())})'


class ExampleDataSetBuilder:
    """
    This class is a helper class which can build an ExampleDataSet from a directory.
    """

    @staticmethod
    def build_from_directory(example_data_path: str,
                             file_suffixes: set = {'.jpg', '.png'},
                             label_data_set: LabelDataSet = None,
                             add_new_labels: bool = False,
                             verbose:int = 0) -> ExampleDataSet:
        """
        This builds an ExampleDataSet based on the scanned example data path.

        :param example_data_path: A path to a directory with labels and example data.
        :param file_suffixes: A list of supported file suffixes (e.g. .jpg).
        :param label_data_set: A LabelDataSet object with allowed labels.
        :param add_new_labels: Controls if new labels should be added to the LabelDataSet object.
        :param verbose: A parameter to control the detail level of generated messages.
        :return: Returns an ExampleDataSet object.
        """
        example_data: ExampleData = []

        for child in pathlib.Path(example_data_path).iterdir():
            dir_name = child.parts[-1]

            for example_data_file_path in child.iterdir():
                filename = example_data_file_path.parts[-1]

                for file_suffix in file_suffixes:
                    if filename.endswith(file_suffix):
                        label = dir_name

                        if label_data_set is None:
                            example_data.append(ExampleData(label, example_data_file_path))
                        elif label_data_set is not None and label in label_data_set:
                            example_data.append(ExampleData(label, example_data_file_path))
                        elif label_data_set is not None and add_new_labels:
                            label_data_set.add_label(label)
                            example_data.append(ExampleData(label, example_data_file_path))

        return ExampleDataSet(example_data, verbose=verbose)


class PredictedItem:
    """
    This class encapsulates an predicted item.
    """

    def __init__(self,
                 label: str,
                 index: int,
                 probability: float):
        self.label = label
        self.index = index
        self.probability = probability

    def __str__(self):
        return f'PredictedItem(label={self.label}, probability={self.probability})'

    def __repr__(self):
        return str(self)


class VisionModel:
    """
    This class encapsulates a tensorflow/keras model and the respective labal data set.
    """

    def __init__(self,
                 model_path: str,
                 verbose: int = 0):
        self.model_path = model_path
        self.model_prefix = 'keras'
        self.keras_model: tf.keras.Model = None
        self.labels_data_set: LabelDataSet = None
        self.verbose = verbose

        self.get_labels()
        self.get_keras_model()

    def get_labels(self, cached: bool = True) -> LabelDataSet:
        if cached is True and self.labels_data_set is not None:
            return self.labels_data_set

        self.labels_data_set = LabelDataSet.load(os.path.join(self.model_path, 'labels.txt'))

        if self.labels_data_set is None:
            self.labels_data_set = LabelDataSet()

        LabelDataSet.save(self.labels_data_set, os.path.join(self.model_path, 'labels.txt'))

        return self.labels_data_set

    def persist_labels(self):
        LabelDataSet.save(self.labels_data_set, os.path.join(self.model_path, 'labels.txt'))

    def get_keras_model(self, cached: bool = True) -> tf.keras.Model:
        if cached is True and self.keras_model is not None:
            return self.keras_model

        latest_tf_models = self.__scan_tf_h5_models(self.model_path, self.model_prefix)

        if latest_tf_models is None or len(latest_tf_models) == 0:
            return None

        self.model_file_path = latest_tf_models[0][0]

        if self.verbose == 1:
            print(f"")
            print(f"Use model: {self.model_file_path}")
            print(f"")

        self.keras_model = tf.keras.models.load_model(self.model_file_path, compile=True)

        return self.keras_model

    def set_keras_model(self, keras_model: tf.keras.Model):
        self.keras_model = keras_model

    def predict(self, input_layer_data) -> [PredictedItem]:
        output_layer_data = self.keras_model.predict(input_layer_data)

        return self.__decode_prediction(output_layer_data)

    def predict_from_image_file(self,
                                image_file_path: str) -> [PredictedItem]:
        image = cv2.imread(image_file_path, flags=cv2.IMREAD_COLOR)
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        image = cv2.resize(image, (128, 128))
        image_array = (image[None] / 127.5) - 1.0

        return self.predict(image_array)

    def __decode_prediction(self, output_layer_data) -> [PredictedItem]:
        labels_data_set: LabelDataSet = self.get_labels()

        predictions = []

        for index, value in np.ndenumerate(output_layer_data):
            neuron_index = index[1]
            label = labels_data_set.decode(neuron_index)
            predictions.append(PredictedItem(label, neuron_index, value))

        # order by prediction value descending
        predictions_sorted = sorted(predictions, key=lambda item: item.probability, reverse=True)

        #print(list(filter(lambda e: e.label is not None, predictions_sorted)))

        return predictions_sorted

    def save_keras_model(self, logs, keep_only_latest_models: int = 4):
        models = self.__scan_tf_h5_models(self.model_path, self.model_prefix)
        version = 1

        if len(models) >= 1:
            version = models[0][1] + 1

        file_name = f'{self.model_prefix}-version-{version}-accuracy-{logs["accuracy"]:.4f}.h5'
        file_path = os.path.join(self.model_path, file_name)

        self.keras_model.save(file_path)

        if self.verbose == 1:
            print(f'\n')
            print(f'Model saved: {file_path}')

        if len(models) >= keep_only_latest_models:
            for model_to_delete in models[keep_only_latest_models - 1:]:
                if self.verbose == 1:
                    print(f'Delete: {str(model_to_delete[0])}')
                os.remove(str(model_to_delete[0]))

    @staticmethod
    def __scan_tf_h5_models(base_path: str,
                            file_prefix: str) -> []:
        """
        Scans a given base_path and for files which match the file_name_regex and orders them descending by a number.

        :param base_path: The base path to scan.
        :param file_name_regex: Regex to apply while scanning for model files.
        :return: Returns a list with tuples of (model_file_path, version).
        """

        import re
        from pathlib import Path

        # Parse directory and identify all model related files.

        pattern = re.compile(file_prefix + '-version-([0-9]*)([\.\-a-z0-9]*)?.h5')

        model_file_paths = []

        for file_path in pathlib.Path(base_path).iterdir():
            file_name = file_path.parts[-1]

            m = pattern.search(file_name)

            if m:
                version = int(m.group(1))
                model_file_paths.append((file_path, version))

        # Order models descending by version.

        model_file_paths.sort(key=lambda e: e[1], reverse=True)

        return model_file_paths

    @staticmethod
    def find_latest_model_path(base_path: str):
        return VisionModel.__scan_tf_h5_models(base_path, 'keras')[0][0]


def create_model_from_MobileNetV2_type1(num_labels) -> K.Model:
    base_model = K.applications.MobileNetV2(
        input_shape=(128, 128, 3),
        weights='imagenet',
        include_top=False)

    for layer in base_model.layers:
        layer.trainable = False

    #for layer in base_model.layers[-4:]:
    #    layer.trainable = True

    x = K.layers.GlobalAveragePooling2D()(base_model.output)

    output_layer = K.layers.Dense(num_labels, activation="softmax")(x)

    return K.Model(inputs=base_model.input, outputs=output_layer)

def create_model_from_MobileNetV2_type2(num_labels) -> K.Model:
    in1 = tf.keras.layers.Input(name="InputLayer", shape=(128, 128, 3))

    base_model = K.applications.MobileNetV2(
        input_shape=(128, 128, 3),
        input_tensor=in1,
        weights='imagenet',
        include_top=False)

    for layer in base_model.layers:
        layer.trainable = False

    x1 = K.layers.GlobalAveragePooling2D()(base_model.output)
    #x1 = K.layers.Dense(num_labels, activation='relu')(x1)

    x2 = tf.keras.models.Sequential([
        # This is the first convolution
        tf.keras.layers.Conv2D(64, (3, 3), activation='relu', input_shape=(128, 128, 3)),
        tf.keras.layers.GlobalAveragePooling2D(),
        # Flatten the results to feed into a DNN
        tf.keras.layers.Dropout(0.2),
        # 512 neuron hidden layer
        tf.keras.layers.Dense(256, activation='relu'),
        #tf.keras.layers.Dense(name="Dense", units=num_labels, activation="softmax")
    ])(inputs = in1)

    # combine the output of the two branches
    combined = tf.keras.layers.concatenate(inputs=[x1, x2])

    output_layer = K.layers.Dense(num_labels, activation="softmax")(combined)

    return K.Model(inputs=in1, outputs=output_layer)

def create_model_from_MobileNetV2_type3(num_labels) -> K.Model:
    return tf.keras.models.Sequential([
         # Note the input shape is the desired size of the image 128x128 with 3 bytes color
         # This is the first convolution
         tf.keras.layers.Conv2D(64, (3,3), activation='relu', input_shape=(128, 128, 3)),
         tf.keras.layers.MaxPooling2D(2, 2),
         # The second convolution
         tf.keras.layers.Conv2D(64, (3,3), activation='relu'),
         tf.keras.layers.MaxPooling2D(2,2),
         # The third convolution
         tf.keras.layers.Conv2D(128, (3,3), activation='relu'),
         tf.keras.layers.MaxPooling2D(2,2),
         # The fourth convolution
         tf.keras.layers.Conv2D(128, (3,3), activation='relu'),
         tf.keras.layers.MaxPooling2D(2,2),
         # Flatten the results to feed into a DNN
         tf.keras.layers.Flatten(),
         tf.keras.layers.Dropout(0.5),
         # 512 neuron hidden layer
         tf.keras.layers.Dense(512, activation='relu'),
         tf.keras.layers.Dense(name="Dense", units=num_labels, activation="softmax")
    ])


class TrainDataGenerator(tf.keras.utils.Sequence):
    """
    This class encapsulates a training data generator which is used by tensorflow/keras during training and validation.
    """

    def __init__(self,
                 dataset: [ExampleData],
                 label_data_set: LabelDataSet,
                 batch_size: 32,
                 image_data_generator: ImageDataGenerator,
                 additional_images: int = 1,
                 shuffle: bool = True,
                 input_size=(128, 128, 3),
                 verbose: int = 0,
                 name: str = ''):
        self.dataset = dataset
        self.label_data_set = label_data_set
        self.batch_size = batch_size
        self.image_data_generator = image_data_generator
        self.additional_images = additional_images
        self.shuffle = shuffle
        self.input_size = input_size
        self.verbose = verbose
        self.name = name

        self.batch_count = int(len(self.dataset) / self.batch_size)

        if self.verbose == 1:
            print()
            print(f"TrainDataGenerator {name}")
            print(f"- batch count: {self.batch_count}")
            print(f"- batch size: {self.batch_size}")
            print(f"- additional generated images per batch: {self.additional_images - 1}")
            print(f"- total batch size: {self.batch_size + self.additional_images - 1}")
            print(f"- total data points:  batch_count * batch_size * additional_images = {self.batch_count * self.batch_size * self.additional_images}")
            print()

    def on_epoch_end(self):
        if self.verbose == 1:
            print()
            print(f"TrainDataGenerator {self.name} - Epoche ended. Shuffle = {self.shuffle}")
            print()

        if self.shuffle:
            import random
            random.shuffle(self.dataset)

    def __get_input(self, idx, image_generator_itr, data_original, target_size):
        if idx != 0:
            batch = image_generator_itr.next()

            # convert to unsigned integers for viewing
            image_arr = batch[0]

            image_arr = tf.image.resize(image_arr, (target_size[0], target_size[1])).numpy()

            return (image_arr / 127.5) - 1.0
        else:
            image_arr = tf.image.resize(data_original, (target_size[0], target_size[1])).numpy()

            return (image_arr / 127.5) - 1.0

    def __get_output(self, label, labels_count):
        return tf.keras.utils.to_categorical(self.label_data_set.encode(label), num_classes=labels_count)

    def __get_data(self, item: ExampleData):
        # Generates data containing batch_size samples

        image_label = item.label
        image_path = item.data_path

        from numpy import expand_dims
        from tensorflow.keras.preprocessing.image import load_img
        from tensorflow.keras.preprocessing.image import img_to_array

        # load the image
        img = load_img(image_path)

        # convert to numpy array
        data_original = img_to_array(img)
        data = data_original

        # expand dimension to one sample
        samples = expand_dims(data, 0)

        # prepare image generator iterator
        it = self.image_data_generator.flow(samples, batch_size=1)

        xlist = []
        ylist = []

        for idx in range(self.additional_images):
            xlist.append(self.__get_input(idx, it, data_original, self.input_size))
            ylist.append(self.__get_output(image_label, self.label_data_set.max_labels))

        return xlist, ylist

    def __getitem__(self, index):
        xlist = []
        ylist = []

        for batch_item_idx in range(self.batch_size):
            example_data = self.dataset[index * self.batch_size + batch_item_idx]

            xx, yy = self.__get_data(example_data)

            xlist = xlist + xx
            ylist = ylist + yy

        X = np.asarray(xlist)
        y = np.asarray(ylist)

        return X, y

    def __len__(self):
        return self.batch_count


class TrainDataGeneratorBuilder:
    """
    A builder class to support dataset scanning and splitting into training and validation data.
    """

    def __init__(self,
                 example_data_set: ExampleDataSet,
                 label_data_set: LabelDataSet = LabelDataSet(),
                 validation_split: float = 0.0,
                 shuffle: bool = True,
                 max_data_per_label: int = None,
                 max_data_per_label_exclude: [] = [],
                 verbose: int = 0):
        """
        Constructor.

        :param labels_path: The directory to be scanned with classes and images.
        :param validation_split: The split factor to split the dataset to a training and validation dataset.
        :param shuffle: A flag if the dataset should be shuffled before splitting it into training and validation.
        """

        self.example_data_set = example_data_set
        self.label_data_set = label_data_set
        self.shuffle = shuffle
        self.validation_split = validation_split
        self.verbose = verbose

        # prepare dataset and take only labels which are also in the label data set

        self.dataset: ExampleData = []

        for e in self.example_data_set.example_data:
            if e.label in self.label_data_set:
                self.dataset.append(e)

        # shuffle dataset
        if self.shuffle:
            import random
            random.shuffle(self.dataset)

        # if max_images_per_label is set then reduce data per label

        dataset_dic = {}

        for e in self.dataset:
            if e.label in dataset_dic:
                dataset_dic[e.label].append(e.data_path)
            else:
                dataset_dic[e.label] = [e.data_path]

        if max_data_per_label is not None:
            for label, images in dataset_dic.items():
                if label not in max_data_per_label_exclude and len(images) > max_data_per_label:
                    dataset_dic[label] = images[:max_data_per_label]

            self.dataset: ExampleData = []

            for label, images in dataset_dic.items():
                for image in images:
                    self.dataset.append(ExampleData(label, image))

            if self.shuffle:
                import random
                random.shuffle(self.dataset)

        # split the dataset by training and validation data

        dataset_validation_split = int(len(self.dataset) * self.validation_split)

        self.dataset_training: ExampleData = self.dataset[dataset_validation_split:]
        self.dataset_validation: ExampleData = self.dataset[:dataset_validation_split]

        if verbose == 1:
            print(f"TrainDataGeneratorBuilder:")

            for label in self.label_data_set.labels:
                if label is not None and label in dataset_dic:
                    print(f" '{label}' - image count: {len(dataset_dic[label])}")

            print(f"")
            print(f"Training data count: {len(self.dataset_training)}")
            print(f"Validation data count: {len(self.dataset_validation)}")
            print(f"")

    def training_generator(self,
                           image_data_generator: ImageDataGenerator,
                           additional_images: int = 1,
                           batch_size: int = 32,
                           shuffle: bool = True,
                           input_size=(128, 128, 3)):
        return TrainDataGenerator(
            dataset=self.dataset_training,
            label_data_set=self.label_data_set,
            image_data_generator=image_data_generator,
            batch_size=batch_size,
            additional_images=additional_images,
            shuffle=shuffle,
            input_size=input_size,
            verbose=self.verbose,
            name='training')


    def validation_generator(self,
                           image_data_generator: ImageDataGenerator,
                           additional_images: int = 1,
                           batch_size: int = 32,
                           shuffle: bool = True,
                           input_size=(128, 128, 3)):
        return TrainDataGenerator(
            dataset=self.dataset_validation,
            label_data_set=self.label_data_set,
            image_data_generator=image_data_generator,
            batch_size=batch_size,
            additional_images=additional_images,
            shuffle=shuffle,
            input_size=input_size,
            verbose=self.verbose,
            name='validation')

class SaveModelCallback(tf.keras.callbacks.Callback):
    def __init__(self,
                 vision_model: VisionModel,
                 base_path: str,
                 safe_after_epoch: bool = True,
                 save_after_batches: int = None,
                 keep_only_latest_models: int = 3,
                 verbose: int = 0):
        self.vision_model = vision_model
        self.base_path = base_path
        self.safe_after_epoch = safe_after_epoch
        self.save_after_batches = save_after_batches
        self.keep_only_latest_models = keep_only_latest_models
        self.verbose = verbose
        self.epoch = 1

        if self.verbose == 1:
            print(f"SaveModelCallback:")
            print(f" safe_after_epoch = {safe_after_epoch}")
            print(f" save_after_batches = {save_after_batches}")
            print(f" keep_only_latest_models = {keep_only_latest_models}")

    def on_epoch_end(self, epoch, logs=None):
        self.epoch = self.epoch + 1

        if self.safe_after_epoch:
            self.vision_model.save_keras_model(logs, self.keep_only_latest_models)

    def on_train_batch_end(self, batch, logs=None):
        if self.save_after_batches and batch >= 1 and batch % self.save_after_batches == 0:
            self.vision_model.save_keras_model(logs, self.keep_only_latest_models)


class TrainingStatus:
    """
    This class encapsulates the training status.
    """

    def __init__(self):
        self.epochs = None
        self.batches = None
        self.epoch_current = None
        self.epoch_eta_seconds = None
        self.epoch_eta = None
        self.batch_current = None
        self.batch_processing_times = []
        self.metric_loss = None
        self.metric_accuracy = None

class TrainingStatusCallback(tf.keras.callbacks.Callback):
    """
    This class handles keras callback during model training and updates the referenced training status.
    """

    def __init__(self,
                 model: K.Model,
                 batches: int = None,
                 verbose: int = 0):
        self.model = model
        self.batches = batches
        self.verbose = verbose
        self.epoch_current = 0
        self.batch_current = 0
        self.batch_processing_time = []

        self.stats_epoch_eta_seconds = 0
        self.stats_epoch_eta = 'NA'
        self.stats_epoch = 0
        self.stats_batch = 0
        self.stats_batches = 0
        self.stats_loss = 0.0
        self.stats_accuracy = 0.0

        if self.verbose == 1:
            print(f"ProgressReportCallback:")

    def on_epoch_begin(self, epoch, logs=None):
        self.epoch_current += 1
        self.batch_current = 0
        self.batch_processing_time = []

    def on_train_batch_begin(self, epoch, logs=None):
        import time

        self.batch_current += 1
        self.batch_processing_time.append((time.time(), None, None))

    def on_train_batch_end(self, batch, logs=None):
        import time

        end_time = time.time()
        start_time, ee, dt = self.batch_processing_time[-1]

        self.batch_processing_time[-1] = (start_time, end_time, end_time - start_time)

        delta_time = 0

        for e in self.batch_processing_time[-10:]:
            delta_time += e[2]

        delta_time /= min(len(self.batch_processing_time), 10)

        seconds = int(delta_time * (self.batches - self.batch_current))
        time_str = time.strftime('%H:%M:%S', time.gmtime(seconds))

        self.stats_epoch_eta_seconds = seconds
        self.stats_epoch_eta = time_str
        self.stats_epoch = self.epoch_current
        self.stats_batch = self.batch_current
        self.stats_batches = self.batches
        self.stats_loss = logs["loss"]
        self.stats_accuracy = logs["accuracy"]


class VisionModelTrainer:
    """
    This class encapsulates the main training thread.
    """

    def __init__(self,
                 model_base_path: str,
                 verbose: int = 0):
        self.__model_base_path = model_base_path
        self.__verbose = verbose
        self.__train_thread = None

    def start_training(self,
                       example_data_set: ExampleDataSet = None):
        if self.__train_thread is not None:
            return

        self.__example_data_set = example_data_set

        self.__train_thread = threading.Thread(name='VisionModelTrainer', target=self.___train)
        self.__train_thread.setDaemon(True)
        self.__train_thread.start()

    def stop_training(self):
        self.__vision_model.keras_model.stop_training = True

    def is_train(self):
        return self.__train_thread is not None

    def ___train(self):
        # Prepare the label data set.

        self.__vision_model = VisionModel(self.__model_base_path,
                                          verbose=self.__verbose)

        # Prepare a helper object to scan the dataset.

        for example_data in self.__example_data_set.example_data:
            if example_data.label not in self.__vision_model.labels_data_set:
                self.__vision_model.labels_data_set.add_label(example_data.label)

        self.__vision_model.persist_labels()

        # Prepare train data generators based on the example data set.

        generator_builder = TrainDataGeneratorBuilder(self.__example_data_set,
                                                      label_data_set=self.__vision_model.get_labels(),
                                                      validation_split=0.20,
                                                      shuffle=True,
                                                      # max_data_per_label=20,
                                                      # max_data_per_label_exclude=['default'],
                                                      verbose=1)

        # Prepare image generators.

        image_data_generator_for_training = ImageDataGenerator(
            rotation_range=90,
            width_shift_range=[-0.2, 0.2],
            height_shift_range=[-0.2, 0.2],
            brightness_range=[0.8, 1.2],
            zoom_range=[0.5, 2],
            fill_mode='constant',
            cval=255,
            dtype='float32')

        image_data_generator_for_validation = ImageDataGenerator(
            rotation_range=90,
            width_shift_range=[-0.2, 0.2],
            height_shift_range=[-0.2, 0.2],
            brightness_range=[0.8, 1.2],
            zoom_range=[0.5, 2],
            fill_mode='constant',
            cval=255,
            dtype='float32')

        # Define a generator for the training data.

        generator_train_data = generator_builder.training_generator(image_data_generator=image_data_generator_for_training,
                                                                    additional_images=40,
                                                                    batch_size=32,
                                                                    shuffle=True)

        # Define a generator for the validation data.

        generator_validation_data = generator_builder.validation_generator(
            image_data_generator=image_data_generator_for_validation,
            additional_images=16,
            batch_size=32,
            shuffle=True)

        #model = create_model_from_MobileNetV2_type1(self.__vision_model.get_labels().max_labels)

        #model.compile(
        #    loss="categorical_crossentropy",
        #    optimizer="adam",
        #    metrics=["accuracy", "categorical_accuracy", "top_k_categorical_accuracy"]
        #)

        model = self.__vision_model.get_keras_model()

        self.__vision_model.set_keras_model(model)

        callback_tensor_board = K.callbacks.TensorBoard(
            log_dir=os.path.join("logs", 'keras') + datetime.datetime.now().strftime("%Y%m%d-%H%M%S"),
            histogram_freq=1)

        self.__training_progress_report = TrainingStatusCallback(
            model=model,
            batches=len(generator_train_data))

        # model.summary()

        model.fit(generator_train_data,
                  epochs=4,
                  # validation_data=generator_validation_data,
                  callbacks=[callback_tensor_board,
                             SaveModelCallback(
                                 vision_model=self.__vision_model,
                                 base_path=self.__model_base_path,
                                 save_after_batches=int(len(generator_train_data) / 20),  # save every 1/20 of epoch
                                 keep_only_latest_models=5,
                                 verbose=1),
                             self.__training_progress_report
                             ])

        self.__train_thread = None

        if self.__verbose == 1:
            print(f"Training ended.")


class BoundingBox:
    """
    This class encapsulates a bounding box.
    """

    def __init__(self,
                 x1: int,
                 y1: int,
                 x2: int,
                 y2: int):
        self.x1 = x1
        self.y1 = y1
        self.x2 = x2
        self.y2 = y2
        self.width = x2 - x1
        self.height = y2 - y1

    def __str__(self):
        return f'BoundingBox(x1={self.x1}, y1={self.y1}, x2={self.x2}, y2={self.y2})'

    def __repr__(self):
        return str(self)


class PredictedItemWithBoundingBox:
    """
    This class encapsulates an predicted item.
    """

    def __init__(self,
                 label: str,
                 index: int,
                 probability: float,
                 bounding_box: BoundingBox):
        self.label = label
        self.index = index
        self.probability = probability
        self.bounding_box = bounding_box

    def __str__(self):
        return f'PredictedItemWithBoundingBox(label={self.label}, probability={self.probability}, bounding_box={self.bounding_box})'

    def __repr__(self):
        return str(self)




def vision_model_predict_with_bounding_box(vision_model: VisionModel,
                                           image_file_path: str,
                                           verbose: int = 0) -> [PredictedItemWithBoundingBox]:
    """
    This implements a naive object detection approach by splitting an image into smaller images and
    classifying each small image.

    :param vision_model A VisionModel object which should be used for classification.
    :param image_file_path The image file path.
    :param verbose The verbose level.
    """

    image = cv2.imread(image_file_path, flags=cv2.IMREAD_COLOR)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    height, width = image.shape[:2]

    result: [PredictedItemWithBoundingBox] = []

    k = 128

    width_new = (math.floor(width / k) + 1) * k
    height_new = (math.floor(height / k) + 1) * k

    sx = width_new / width
    sy = height_new / height

    steps_x = int(width_new / k)
    steps_y = int(height_new / k)

    if verbose == 1:
        print(f"Size original: ({width}, {height})")
        print(f"Size new: ({width_new}, {height_new})")
        print(f"Scale factor: ({sx}, {sy})")
        print(f"Steps: ({steps_x}, {steps_y})")

    image = cv2.resize(image, (width_new, height_new))

    for xs in range(0, steps_x):
        for ys in range(0, steps_y):
            x1 = xs * k
            x2 = x1 + k
            y1 = ys * k
            y2 = y1 + k

            try:
                subimage = image[y1:y2, x1:x2]
                subimage = cv2.resize(subimage, (128, 128))
                subimage_array = (subimage[None] / 127.5) - 1.0

                p = vision_model.predict(subimage_array)

                p_first = p[0]

                if p_first.probability > 0.5 and p_first.label != 'default':
                    bb = BoundingBox(x1, y1, x2, y2)

                    if verbose == 1:
                        print(f"{p[0]} [{x1},{y1},{x2},{y2}]")
                        print(f"{p[:3]}")

                    pitem = PredictedItemWithBoundingBox(p_first.label, p_first.index, p_first.probability, bb)

                    result.append(pitem)
            except Exception as e:
                print(f"Exception: {e}")

    return result


def tf_list_devices():
    """
    This functions lists visible devices which tensorflow can use for training or inference.
    """

    visible_devices = tf.config.get_visible_devices()
    print(f'''
    Devices:
    ''')
    for device in visible_devices:
        print(f"Device: {device}")
        print(f"Device type: {device.device_type}")

    print(f'''
    GPUs available: {len(tf.config.experimental.list_physical_devices('GPU'))}
    ''')


def log_execution_time(func):
    """
    This wraps a function and logs its execution time.
    """

    def wrapper(*args, **kwargs):
        start_time = time.time()
        result = func(*args, **kwargs)
        print("--- %s seconds ---" % (time.time() - start_time))
        return result

    wrapper.__name__ = func.__name__

    return wrapper
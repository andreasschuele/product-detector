"""
This tests image classification using a vision model.
"""

import numpy as np
import cv2
import pathlib
import vision
import statistics

def read_image(file: pathlib.Path):
    image = cv2.imread(str(file), flags=cv2.IMREAD_COLOR)
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    image = cv2.resize(image, (128, 128))

    return image, (image[None] / 127.5) - 1.0

labels = []  # labels
dataset = [] # (label_name, image)
dataset_dict = {} # {label: [image]}

validation_dir = pathlib.Path('../product-detector-demo-data/fruits-360-original-archive/Test')

for child in validation_dir.iterdir():
    dir_name = child.parts[-1]
    images = []

    # print(child)

    for image in child.iterdir():
        # print(images)
        filename = image.parts[-1]

        if filename.endswith('.jpg'):
            images.append(image)
            dataset.append((dir_name, image))

            if dir_name not in dataset_dict:
                dataset_dict[dir_name] = []

            dataset_dict[dir_name].append(image)

    if dir_name != None and len(images) > 0:
        labels.append(dir_name)



print(f"Labels: {labels}")
print(f"Dataset: {dataset}")
print(f"Dataset Dictionary: {dataset_dict}")


def test_evaluation():
    vision_model: vision.VisionModel = vision.VisionModel('models\\fruits360\\model', 1)

    def to_str(v):
        return f"{v:.3f}"

    def test_prediction(directory: str, image: str) -> int:
        image, image_array = read_image(validation_dir.test_images_dir.joinpath(directory, image))
        matches: [vision.PredictedItem] = vision_model.predict(image_array)
        best_match = matches[0]
        print(f"Best match: {best_match}")
        return 1 if best_match.name == directory.lower() else 0

    results = []

    stat_all_precision = []

    for label, images in dataset_dict.items():
        count_true_positive = 0
        count_false_positive = 0
        probability = []

        for image_path in images:
            image, image_array = read_image(image_path)
            matches: [vision.PredictedItem] = vision_model.predict(image_array)
            best_match = matches[0]
            #print(f"Best match: {best_match}")

            if best_match.label == label:
                count_true_positive += 1
            else:
                count_false_positive += 1

            # find label match

            for match in matches:
                if match.label == label:
                    probability.append(match.probability)

        probability_sorted = sorted(probability)
        quantiles = statistics.quantiles(probability, n=4)

        stat_precision = count_true_positive / (count_true_positive + count_false_positive)
        stat_mean = statistics.mean(probability)
        stat_stdev = statistics.stdev(probability)
        stat_min = probability_sorted[0]
        stat_q1 = quantiles[0]
        stat_median = statistics.median(probability)
        stat_q3 = quantiles[2]
        stat_max = probability_sorted[-1]

        stat_all_precision.append(stat_precision)

        results.append((label,
                        '',
                        len(images),
                        to_str(stat_precision),
                        to_str(stat_mean),
                        to_str(stat_stdev),
                        to_str(stat_min),
                        to_str(stat_q1),
                        to_str(stat_median),
                        to_str(stat_q3),
                        to_str(stat_max)))

        #print(f"Label: {label:<15} Precision: {precision:.3f} Recall: {recall:.3f}")

        print(f"============================")
        print(f"Label: {label}")
        print(f"Precision: {stat_precision:.3f}")
        print(f"")
        print(f"Mean: {stat_mean:.3f}")
        print(f"Stdev: {stat_stdev:.3f}")
        print(f"Min: {stat_min:.3f}")
        print(f"Q1: {stat_q1:.3f}")
        print(f"Median: {stat_median:.3f}")
        print(f"Q3: {stat_q3:.3f}")
        print(f"Max: {stat_max:.3f}")

    stat_all_precision_sorted = sorted(stat_all_precision)
    quantiles = statistics.quantiles(stat_all_precision, n=4)

    stat_mean = statistics.mean(stat_all_precision)
    stat_stdev = statistics.stdev(stat_all_precision)
    stat_min = stat_all_precision_sorted[0]
    stat_q1 = quantiles[0]
    stat_median = statistics.median(stat_all_precision)
    stat_q3 = quantiles[2]
    stat_max = stat_all_precision_sorted[-1]

    results.append(('Precision',
                    '',
                    '',
                    '',
                    to_str(stat_mean),
                    to_str(stat_stdev),
                    to_str(stat_min),
                    to_str(stat_q1),
                    to_str(stat_median),
                    to_str(stat_q3),
                    to_str(stat_max)))

    import csv
    with open('results.csv', 'w', newline='') as csvfile:
        writer = csv.writer(csvfile, delimiter=',',  quotechar='"', quoting=csv.QUOTE_MINIMAL)
        writer.writerow(['Label', 'Images Training', 'Images Validation', 'Precision', 'Mean', 'STDEV', 'Min', 'Q1', 'Median', 'Q3', 'Max'])

        for result in results:
            writer.writerow(result)

test_evaluation()
"""
This tests the training of a vision model.
"""

import time
import vision

if __name__ == '__main__':
    example_data_set = vision.ExampleDataSetBuilder.build_from_directory(
        example_data_path='..//product-detector-demo-data\\fruits-360-original-archive\\Training',
        verbose=1)

    vision_model_trainer = vision.VisionModelTrainer(model_base_path='models\\fruits360\\model',
                                                     verbose=1)

    vision_model_trainer.start_training(example_data_set=example_data_set)

    stop_after = 30

    while vision_model_trainer.is_train():
        time.sleep(1)

        stop_after -= 1

        #if stop_after <= 0:
        #    detector_model_trainer.stop_training()
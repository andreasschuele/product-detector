"""
This tool can generate a vision model with demo data.
"""

import cv2
import os
import pathlib
import vision

from tinydb import TinyDB, Query
from uuid import uuid4

def create_vision_model_with_demo_data(model_name: str, demo_data_path: pathlib.Path):
  db = TinyDB('./vision_database.json', sort_keys=True, indent=2, separators=(',', ': '))
  db_tmodel = db.table('model')
  db_query = Query()

  print(demo_data_path)

  if db_tmodel.contains(db_query.name == model_name):
    return

  model = {
    'id': uuid4().hex,
    'base': '',
    'name': model_name,
    'settings': {},
    'data': {},
    'labels': [],
    'trainings': {}
  }

  path_models = vision.root_dir.joinpath('models')
  path_model = path_models.joinpath(model_name)
  path_model.mkdir(parents=True, exist_ok=True)
  path_model_data = path_models.joinpath(model_name, 'data')
  path_model_data.mkdir(parents=True, exist_ok=True)
  path_model_model = path_models.joinpath(model_name, 'model')
  path_model_model.mkdir(parents=True, exist_ok=True)

  label_dir_names = list(os.listdir(demo_data_path))

  # for all labels in the demo data path
  for idx1, label_dir_name in enumerate(label_dir_names):
    label_path = demo_data_path.joinpath(label_dir_name)

    label_image_names = list(os.listdir(label_path))
    label_image_names = list(filter(lambda e: str(e).endswith('jpg'), label_image_names))

    # for all label images
    for idx2, label_image_name in enumerate(label_image_names):
      label_image_file_path = label_path.joinpath(label_image_name)
      print(f"[{idx1} / {len(label_dir_names)}] [{idx2} / {len(label_image_names)}] : {label_image_file_path}")

      dataId = uuid4().hex

      file_name = dataId + '.jpg'
      path_file = path_model_data.joinpath(file_name)

      with open(label_image_file_path, mode='rb') as in_file, \
              open(path_file, mode='wb') as out_file:
        out_file.write(in_file.read())
        out_file.flush()

        im = cv2.imread(str(path_file))
        h, w, c = im.shape

        model['data'][dataId] = {
          'id': dataId,
          'fileName': file_name,
          'objects': [
            {
              'boundingBox': {
                'height': h,
                'width': w,
                'x': 0,
                'y': 0
              },
              'label': label_dir_name
            }
          ]
        }

  db_tmodel.insert(model)

if __name__ == "__main__":
  create_vision_model_with_demo_data('fruit360-subset', pathlib.Path('../product-detector-demo-data/fruits-360-original-training-subset1'))
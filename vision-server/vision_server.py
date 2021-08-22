"""
This module contains the implementation of the Vision API based on Flask.
"""

import atexit
import datetime
import json
import os
import pathlib
import shutil
import threading
import uuid
import vision

from flask import Flask, request, jsonify, render_template, Response, send_file, abort
from flask_httpauth import HTTPBasicAuth
from flask_cors import CORS
from flask_swagger_ui import get_swaggerui_blueprint

from tinydb import TinyDB, Query
from tinydb.storages import JSONStorage
from tinydb.middlewares import CachingMiddleware

from werkzeug.security import generate_password_hash, check_password_hash


app = Flask(__name__, static_folder='static', template_folder='templates')

auth = HTTPBasicAuth()

cors = CORS(app, resources={r"/api/*": {"origins": "*"}})

users = {
  "admin": generate_password_hash("admin"),
  "user": generate_password_hash("user")
}

db = TinyDB('./vision_database.json', sort_keys=True, indent=2, separators=(',', ': '), storage=CachingMiddleware(JSONStorage))
db_tmodel = db.table('model')
db_session_lock = threading.RLock()
db_query = Query()

g_vision_model: vision.VisionModel = None
g_vision_model_trainer: vision.VisionModelTrainer = None


def db_session(func):
  """
  This wraps a function and uses a lock to ensure safe execution.
  """

  def wrapper(*args, **kwargs):
    with db_session_lock:
      return func(*args, **kwargs)

  wrapper.__name__ = func.__name__

  return wrapper


def endpoint_log(func):
  """
  This wraps a function (Flask route endpoint function) and adds request and response logging.
  """

  def wrapper(*args, **kwargs):
    try:
      try:
        print(f'Request: {request}')
        print(f'Request JSON: {request.json}')
      except Exception:
        pass

      response = func(*args, **kwargs)

      try:
        print(f'Response: {response}')
        print(f'Response JSON: {response.get_json()}')
      except Exception:
        pass

      return response
    except Exception as e:
      print("Service exception: %s", e)
      return Response(json.dumps({}), status=500, mimetype='application/json')

  wrapper.__name__ = func.__name__

  return wrapper


def fetch_model(modelId):
  """
  This loads a model from tinydb by a modelId.
  """

  if not db_tmodel.contains(db_query.id == modelId):
    return Response(json.dumps({}), status=404, mimetype='application/json'), None

  model = db_tmodel.get(db_query.id == modelId)

  return None, model


def fetch_model_data(modelId, dataId):
  """
  This loads a model and model data from tinydb by modelId and dataId.
  """

  r, model = fetch_model(modelId)

  if r is not None: return r

  allDataIds = list(map(lambda e: e['id'], model['data'].values()))

  if not dataId in allDataIds:
    return Response(json.dumps({}), status=404, mimetype='application/json'), None, None

  return None, model, model['data'][dataId]


def parse_settings(settings_array):
  result = {}

  for e in settings_array:
    result[e['key']] = e['value']

  return result


@app.route("/")
def index():
  return render_template('index.html', title = "API")


@app.route("/status")
@auth.login_required
def status():
  user = "Hello, {}!".format(auth.username())
  return render_template('index.html', title = "API" + " " + user)


@app.route('/api/v1/vision/model', methods=['POST'])
@auth.login_required
@endpoint_log
@db_session
def modelCreate():
  modelCreateRequest = request.json

  modelName = modelCreateRequest.get('modelName', None)
  modelBase = modelCreateRequest.get('baseModel', None)
  settings = modelCreateRequest.get('settings', [])

  if db_tmodel.contains(db_query.name == modelName):
    return Response(json.dumps({}), status=409, mimetype='application/json')

  modelId = uuid.uuid4().hex

  path_models = vision.root_dir.joinpath('models')
  path_model = path_models.joinpath(modelName)
  path_model.mkdir(parents=True, exist_ok=True)
  path_model_data = path_models.joinpath(modelName, 'data')
  path_model_data.mkdir(parents=True, exist_ok=True)
  path_model_model = path_models.joinpath(modelName, 'model')
  path_model_model.mkdir(parents=True, exist_ok=True)

  model = {
    'id': modelId,
    'base': modelBase,
    'name': modelName,
    'settings': parse_settings(settings),
    'data': {},
    'labels': [],
    'trainings': {}
  }

  if modelBase is None:
    print(f'Create a new TF model ...')

  if modelBase is not None and db_tmodel.contains(db_query.name == modelBase):
    path_base_model = path_models.joinpath(modelBase)
    path_base_model_data = path_base_model.joinpath('data')

    base_model = db_tmodel.get(db_query.name == modelBase)

    model['data'] = base_model['data']

    path_model_data.rmdir()

    shutil.copytree(str(path_base_model_data), str(path_model_data))

  db_tmodel.insert(model)

  return jsonify({
    "modelId": modelId,
    "modelName": modelName,
    "baseModel": modelBase
  })


@app.route('/api/v1/vision/model', methods=['GET'])
@auth.login_required
@db_session
def modelGetAll():
  models = map(lambda e: {
    "modelId": e['id'],
    "modelName": e['name']
  }, db_tmodel.all())

  return jsonify({"models": list(models)})


@app.route('/api/v1/vision/model/<modelId>', methods=['GET'])
@auth.login_required
@endpoint_log
@db_session
def modelGet(modelId):
  r, model = fetch_model(modelId)

  if r is not None: return r

  return jsonify({
    "modelId": model['id'],
    "modelName": model['name']
  })


@app.route('/api/v1/vision/model/<modelId>', methods=['DELETE'])
@auth.login_required
@endpoint_log
@db_session
def modelDelete(modelId):
  r, model = fetch_model(modelId)

  if r is not None: return r

  path_models = vision.root_dir.joinpath('models')
  path_model = path_models.joinpath(model['name'])

  import shutil

  shutil.rmtree(path_model)

  db_tmodel.remove(doc_ids=[model.doc_id])

  return jsonify({
    "status": "200",
    "message": "Deleted"
  })


@app.route('/api/v1/vision/model/<modelId>/detect', methods=['POST'])
@auth.login_required
@endpoint_log
@db_session
def modelDetect(modelId):
  r, model = fetch_model(modelId)

  if r is not None: return r

  modelDetectRequest = request.json

  import base64

  model_path = pathlib.Path('models', model['name'], 'model')

  tmp_path = vision.root_dir.joinpath('tmp')
  tmp_path.mkdir(parents=True, exist_ok=True)
  tmp_file_prefix = f"{datetime.datetime.now().strftime('%Y%m%d-%H%M%S')}-{model['name']}-{uuid.uuid4().hex}"
  tmp_image_file_path = tmp_path.joinpath(f'{tmp_file_prefix}.jpeg')
  tmp_prediction_file_path = tmp_path.joinpath(f'{tmp_file_prefix}.txt')

  with open(str(tmp_image_file_path), "wb") as file:
    file.write(base64.b64decode(modelDetectRequest['image']['data']))

  global g_vision_model

  if g_vision_model is None \
          or g_vision_model is not None and g_vision_model.model_file_path != vision.VisionModel.find_latest_model_path(base_path=str(model_path)):
    g_vision_model = vision.VisionModel(model_path=str(model_path),
                                        verbose=1)

  predictions: [vision.PredictedItem] = g_vision_model.predict_from_image_file(str(tmp_image_file_path))

  predictions = filter(lambda e: e.label is not None, predictions)

  predictions = sorted(predictions, key=lambda e: e.probability, reverse=True)

  with open(str(tmp_prediction_file_path), "w") as file:
    for prediction in predictions:
      file.write(f'{prediction}\n')

  #os.remove(str(tmp_image_file_path))
  #os.remove(str(tmp_prediction_file_path))

  detected_objects = []

  for prediction in predictions:
    detected_object = {
      "labelId": prediction.index,
      "label": prediction.label,
      "probability": float(prediction.probability),
      "boundingBox": {"x": 0, "y": 0, "width": 0, "height": 0}
    }

    detected_objects.append(detected_object)

  return jsonify({
    "detectedObjects": detected_objects
  })


@app.route('/api/v1/vision/model/<modelId>/train', methods=['POST'])
@auth.login_required
@endpoint_log
@db_session
def modelTrain(modelId):
  r, model = fetch_model(modelId)

  if r is not None: return r

  db.storage.flush()

  trainId = uuid.uuid4().hex

  model_path = pathlib.Path('models', model['name'], 'model')

  model['trainings'][trainId] = {
    'id': trainId,
    'progress': 0,
    'settings': parse_settings(request.json.get('settings'))
  }

  vision_labels = set(request.json.get('labels'))

  example_data_list: vision.ExampleData = []

  for image_data in model['data'].values():
    for object in image_data['objects']:
      object_label = object["label"]
      object_filename = image_data['fileName']

      if object_label not in vision_labels:
        continue

      example_data = vision.ExampleData(object_label, f"models/{model['name']}/data/{object_filename}")

      example_data_list.append(example_data)

  example_data_set = vision.ExampleDataSet(example_data_list, verbose=1)

  global g_vision_model_trainer

  if g_vision_model_trainer is not None:
    return jsonify({
      'trainId': 'NA'
    })

  g_vision_model_trainer = vision.VisionModelTrainer(model_base_path=str(model_path),
                                                     verbose=1)

  g_vision_model_trainer.start_training(example_data_set)

  db_tmodel.update(model, doc_ids=[model.doc_id])

  return jsonify({
    'trainId': trainId
  })


@app.route('/api/v1/vision/model/<modelId>/train/<trainId>', methods=['GET'])
@auth.login_required
@endpoint_log
@db_session
def modelTrainGetStatus(modelId, trainId):
  r, model = fetch_model(modelId)

  if r is not None: return r

  training = model['trainings'][trainId]

  return jsonify({
    "progress": training['progress']
  })


@app.route('/api/v1/vision/model/<modelId>/data', methods=['POST'])
@auth.login_required
@endpoint_log
@db_session
def modelDataCreate(modelId):
  r, model = fetch_model(modelId)

  if r is not None: return r

  modelDataCreateRequest = request.json

  data_image = modelDataCreateRequest['image']['data']
  data_objects = modelDataCreateRequest['objects']

  dataId = uuid.uuid4().hex

  path_models = vision.root_dir.joinpath('models')
  path_model_data = path_models.joinpath(model['name'], 'data')
  file_name = dataId + '.jpg'
  path_file = path_model_data.joinpath(file_name)

  import base64

  with open(pathlib.Path(path_file), "wb") as image_file:
    image_file.write(base64.b64decode(data_image))

  model['data'][dataId] = {
    'id': dataId,
    'fileName': file_name,
    'objects': data_objects
  }

  db_tmodel.update(model, doc_ids=[model.doc_id])

  return jsonify({
    'dataId': dataId
  })


@app.route('/api/v1/vision/model/<modelId>/data', methods=['GET'])
@auth.login_required
@endpoint_log
@db_session
def modelDataGetAll(modelId):
  r, model = fetch_model(modelId)

  if r is not None: return r

  data = list(map(lambda e: {
    'dataId': e['id'],
    'fileName': e['fileName'],
    'objects': e['objects'],
  }, model['data'].values()))

  return jsonify({
    'data': data
  })


@app.route('/api/v1/vision/model/<modelId>/data/<dataId>', methods=['GET'])
@auth.login_required
@endpoint_log
@db_session
def modelDataGet(modelId, dataId):
  r, model, modelData = fetch_model_data(modelId, dataId)

  if r is not None: return r

  downloadFile = request.args.get('downloadFile')

  path_models = vision.root_dir.joinpath('models')
  path_model_data = path_models.joinpath(model['name'], 'data')
  file_name = modelData['fileName']
  path_file = path_model_data.joinpath(file_name)

  if downloadFile == "true":
    return send_file(str(path_file),
                     attachment_filename=file_name)
  else:
    import base64

    with open(str(path_file), "rb") as image_file:
      image_file_bytes = image_file.read()
      image_file_base64 = base64.b64encode(image_file_bytes).decode("utf-8")

      return jsonify({
        'dataId': modelData['id'],
        'objects': modelData['objects'],
        'image': {
          'fileName': modelData['fileName'],
          'format': 'jpg',
          'data': image_file_base64,
          'encoding': 'base64'
        }
      })


@app.route('/api/v1/vision/model/<modelId>/data/<dataId>', methods=['DELETE'])
@auth.login_required
@endpoint_log
@db_session
def modelDataDelete(modelId, dataId):
  r, model, modelData = fetch_model_data(modelId, dataId)

  if r is not None: return r

  path_models = vision.root_dir.joinpath('models')
  path_model_data = path_models.joinpath(model['name'], 'data')
  file_name = modelData['fileName']
  path_file = path_model_data.joinpath(file_name)

  os.remove(path_file)

  model['data'].pop(dataId)

  db_tmodel.update(model, doc_ids=[model.doc_id])

  return jsonify({
    "status": "200",
    "message": "Deleted"
  })


@app.route('/api/v1/docs/v3', methods=['GET'])
@auth.login_required
@endpoint_log
def getOpenApiDocs():
  content = open('vision_server_api.yaml').read()

  return Response(content, mimetype="application/json")


@auth.verify_password
def verify_password(username, password):
  if username in users and check_password_hash(users.get(username), password):
    return username


def configure_swagger(app):
  SWAGGER_URL = '/api/v1/docs/v3/swagger-ui'
  API_URL = '/api/v1/docs/v3'

  swaggerui_blueprint = get_swaggerui_blueprint(
    SWAGGER_URL,
    API_URL,
    config={
      'app_name': "Vision API"
    }
  )

  @swaggerui_blueprint.before_request
  @auth.login_required
  def before_request():
    if 'Authorization' not in request.headers:
      abort(401)

  app.register_blueprint(swaggerui_blueprint)


if __name__ == "__main__":
  def exit_handler():
    print('EXIT handler ...')
    db.storage.flush()
    db.close()

  atexit.register(exit_handler)

  configure_swagger(app)

  app.run(port=5080)
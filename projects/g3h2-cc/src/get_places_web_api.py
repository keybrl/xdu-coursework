from flask import Flask, request
from hbase_manager import HBaseRecord
import json
from flask_cors import CORS

app = Flask(__name__)
CORS(app)


@app.route('/')
@app.route('/test')
@app.route('/hello')
def hello():
    return 'Hello World!'


@app.route('/get_places', methods=['GET', 'POST'])
def get_places():
    if request.method == 'GET':
        data = request.args
    else:
        if request.headers.get('Content-Type') == 'application/json':
            data = json.loads(request.data.decode())
        else:
            data = request.form

    eid = data.get('eid')
    start_time = data.get('start_time')
    end_time = data.get('end_time')

    if not eid or not start_time or not end_time:
        return '参数格式错误', 400

    records = app.config.get('hbase').get_places(eid, start_time, end_time)
    if not records:
        return '[]', 200, {'Content-Type': 'application/json'}
    result = []
    for record in records:
        eid, timestamp, place_id = record[0].split('##')
        result.append({
            'eid': eid,
            'timestamp': timestamp,
            'place_id': place_id,
            'place_name': record[1],
            'longitude': record[2],
            'latitude': record[3]
        })
    return json.dumps(result), 200, {'Content-Type': 'application/json'}


if __name__ == '__main__':
    app.config['hbase'] = HBaseRecord('http://cc-keybrl-node0:2048')
    app.run(host='0.0.0.0', port=8080, debug=False)

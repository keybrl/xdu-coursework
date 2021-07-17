import base64
import json

import requests


class HBaseRecord(object):
    def __init__(self, host):
        self.host = host

    def test(self):
        res = requests.get(self.host + '/')
        return res.status_code == 200

    # 建表（重建、覆盖）
    def init(self):
        url = '/record2/schema'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        data = {'ColumnSchema': [{'name': 'info'}]}

        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        if res.status_code != 201:
            return False

        url = '/VehicleCount/schema'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        data = {'ColumnSchema': [{'name': 'info'}]}

        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        if res.status_code != 201:
            return False

        url = '/MeetCount/schema'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        data = {'ColumnSchema': [{'name': 'info'}]}

        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        if res.status_code != 201:
            return False

        url = '/record/schema'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        data = {'ColumnSchema': [{'name': 'info'}]}

        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        return res.status_code == 201

    # 插入一行
    def insert_record(self, record_rows):
        url = '/record/fakerow'
        url2 = '/record2/fakerow'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        data = {'Row': []}
        data2 = {'Row': []}
        for row in record_rows:
            row_key = '{place_id}##{time}##{eid}'.format(
                place_id=row['place_id'],
                time=row['time'],
                eid=row['eid']
            )
            row_key2 = '{eid}##{time}##{place_id}'.format(
                place_id=row['place_id'],
                time=row['time'],
                eid=row['eid']
            )
            data['Row'].append({
                'key': base64.b64encode(row_key.encode()).decode(),
                'Cell': [
                    # info:address
                    {'column': 'aW5mbzphZGRyZXNz', '$': base64.b64encode(row['address'].encode()).decode()},
                    # info:longitude
                    {'column': 'aW5mbzpsb25naXR1ZGU=', '$': base64.b64encode(row['longitude'].encode()).decode()},
                    # info:latitude
                    {'column': 'aW5mbzpsYXRpdHVkZQ==', '$': base64.b64encode(row['latitude'].encode()).decode()},
                ]
            })
            data2['Row'].append({
                'key': base64.b64encode(row_key2.encode()).decode(),
                'Cell': [
                    # info:address
                    {'column': 'aW5mbzphZGRyZXNz', '$': base64.b64encode(row['address'].encode()).decode()},
                    # info:longitude
                    {'column': 'aW5mbzpsb25naXR1ZGU=', '$': base64.b64encode(row['longitude'].encode()).decode()},
                    # info:latitude
                    {'column': 'aW5mbzpsYXRpdHVkZQ==', '$': base64.b64encode(row['latitude'].encode()).decode()},
                ]
            })

        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        res2 = requests.put(self.host + url2, headers=headers, data=json.dumps(data2))
        return res.status_code == 200 and res2.status_code == 200

    def insert_meet_count(self, record_rows):
        url = '/MeetCount/fakerow'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }

        i = 0
        data = {'Row': []}
        for row in record_rows:
            data['Row'].append({
                'key': base64.b64encode(row[0].encode()).decode(),
                'Cell': [
                    {'column': base64.b64encode(('info:' + row[1]).encode()).decode(),
                     '$': base64.b64encode(str(row[2]).encode()).decode()},
                ]
            })
            i += 1
            if i >= 1000:
                res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
                if res.status_code != 200:
                    return False
                i = 0
                data = {'Row': []}
        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        return res.status_code == 200

    def insert_vehicle_count(self, record_rows):
        url = '/VehicleCount/fakerow'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        data = {'Row': []}
        for row in record_rows:
            data['Row'].append({
                'key': base64.b64encode(str(row[0]).encode()).decode(),
                'Cell': [
                    # info:address
                    {'column': 'aW5mbzphZGRyZXNz', '$': base64.b64encode(row[1].encode()).decode()},
                    # info:longitude
                    {'column': 'aW5mbzpsb25naXR1ZGU=', '$': base64.b64encode(row[2].encode()).decode()},
                    # info:latitude
                    {'column': 'aW5mbzpsYXRpdHVkZQ==', '$': base64.b64encode(row[3].encode()).decode()},
                    # info:count
                    {'column': 'aW5mbzpjb3VudA==', '$': base64.b64encode(str(row[4]).encode()).decode()},
                ]
            })

        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        return res.status_code == 200

    def get_places(self, eid: str, start_time: str, end_time: str):
        url = '/record2/scanner'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
        data = {
            'startRow': base64.b64encode(
                '{eid}##{start_time}##1'.format(eid=eid, start_time=start_time).encode()
            ).decode(),
            'endRow': base64.b64encode(
                '{eid}##{end_time}##330'.format(eid=eid, end_time=end_time).encode()
            ).decode()
        }

        res = requests.put(self.host + url, headers=headers, data=json.dumps(data))
        if res.status_code != 201:
            return None
        url = res.headers.get('Location')

        res = requests.get(url, headers=headers)
        if res.status_code != 200:
            return None
        result = []
        for row in json.loads(res.text).get('Row'):
            row_key = base64.b64decode(row['key'].encode()).decode()
            cell = {}
            for col in row['Cell']:
                cell[col["column"]] = base64.b64decode(col["$"].encode()).decode()
            result.append((
                row_key,
                cell['aW5mbzphZGRyZXNz'],
                cell['aW5mbzpsb25naXR1ZGU='],
                cell['aW5mbzpsYXRpdHVkZQ==']
            ))

        requests.delete(url)

        return result

    def get_vehicle_count(self):
        url = '/VehicleCount/scanner'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }

        res = requests.put(self.host + url, headers=headers, data="{}")
        if res.status_code != 201:
            return None
        url = res.headers.get('Location')

        res = requests.get(url, headers=headers)
        if res.status_code != 200:
            return None
        result = []
        for row in json.loads(res.text).get('Row'):
            row_key = base64.b64decode(row['key'].encode()).decode()
            cell = {}
            for col in row['Cell']:
                cell[col["column"]] = base64.b64decode(col["$"].encode())
            result.append((
                int(row_key),
                cell['aW5mbzphZGRyZXNz'].decode(),
                cell['aW5mbzpsb25naXR1ZGU='].decode(),
                cell['aW5mbzpsYXRpdHVkZQ=='].decode(),
                int(cell['aW5mbzpjb3VudA=='].decode()),
            ))

        requests.delete(url)
        return result

    def get_meet_count(self):
        url = '/MeetCount/scanner'
        headers = {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }

        res = requests.put(self.host + url, headers=headers, data="{}")
        if res.status_code != 201:
            return None
        url = res.headers.get('Location')

        res = requests.get(url, headers=headers)
        if res.status_code != 200:
            return None
        result = []
        for row in json.loads(res.text).get('Row'):
            row_key = base64.b64decode(row['key'].encode()).decode()
            for col in row['Cell']:
                eid = base64.b64decode(col["column"].encode()).decode()[5:]
                count = base64.b64decode(col["$"].encode()).decode()
                result.append((
                    row_key,
                    eid,
                    int(count),
                ))

        requests.delete(url)
        return result

    # 清空所有数据
    def clear(self):
        self.init()

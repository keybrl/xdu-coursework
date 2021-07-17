import json
import os
import sqlite3


def run():
    # 读取配置文件
    with open('config.json', 'r', encoding='utf-8') as f:
        config = json.load(f).get('json_to_sqlite')

    if config is None:
        print('不能读取应用 `json_to_sqlite` 的配置。')
        exit()

    # 获取配置项
    JSON_PATH = config.get('json_path')
    SQLITE_PATH = config.get('sqlite_path')

    if not JSON_PATH or not SQLITE_PATH:
        print('不能读取应用 `json_to_sqlite` 的配置。')
        exit()

    if not os.path.isfile(JSON_PATH):
        print('JSON 文件 \'{json_path}\' 不存在。'.format(json_path=JSON_PATH))
        exit()

    conn = sqlite3.connect(SQLITE_PATH)
    cur = conn.cursor()

    # 建表
    cur.execute('DROP TABLE IF EXISTS record')
    conn.commit()
    cur.execute(
        'CREATE TABLE record ('
        '    id INTEGER PRIMARY KEY AUTOINCREMENT,'
        '    eid CHAR(14),'
        '    time INTEGER,'
        '    placeId INTEGER,'
        '    address VARCHAR(128),'
        '    longitude CHAR(10),'
        '    latitude CHAR(10)'
        ')'
    )
    conn.commit()

    with open(JSON_PATH, 'r', encoding='utf-8') as f:

        row = f.readline()
        i = 0

        while row:
            data = json.loads(row)
            if float(data['longitude']) <= 130 and float(data['latitude']) <= 40:
                cur.execute('INSERT INTO record VALUES (null, ?, ?, ?, ?, ?, ?)',
                            (data['eid'], data['time'], data['placeId'],
                             data['address'], data['longitude'], data['latitude']))
            i += 1

            # 每插入1000条数据，commit一次
            if i == 5000:
                i = 0
                print('=', end='')
                conn.commit()

            row = f.readline()
        else:
            print('=')
            conn.commit()

    conn.close()


if __name__ == '__main__':
    run()

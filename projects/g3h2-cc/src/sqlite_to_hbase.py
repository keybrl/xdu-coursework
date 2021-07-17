import json
import os
import sqlite3

from hbase_manager import HBaseRecord


def run():
    # 读取配置文件
    with open('config.json', 'r', encoding='utf-8') as f:
        config = json.load(f).get('sqlite_to_hbase')

    if config is None:
        print('不能读取应用 `sqlite_to_hbase` 的配置。')
        exit()

    # 获取配置项
    SQLITE_PATH = config.get('sqlite_path')
    HBASE_HOST = config.get('hbase_host')

    if not SQLITE_PATH or not HBASE_HOST:
        print('不能读取应用 `sqlite_to_hbase` 的配置。')
        exit()

    if not os.path.isfile(SQLITE_PATH):
        print('SQLite 文件 \'{sqlite_path}\' 不存在。'.format(sqlite_path=SQLITE_PATH))
        exit()

    print('正在连接数据库...')
    hbase = HBaseRecord(HBASE_HOST)

    if not hbase.test():
        print('无法连接 HBase 的 REST API 。')
        exit()

    print('正在初始化表...')
    if not hbase.init():
        print('建表失败。')
        exit()

    print('正在插入数据......')

    conn = sqlite3.connect(SQLITE_PATH)
    cur = conn.cursor()

    rows = cur.execute(
        'SELECT place_id, address, longitude, latitude, count FROM vehicle_count'
    ).fetchall()
    if not hbase.insert_vehicle_count(rows):
        print('\'VehicleCount\' 插入失败')
    rows = cur.execute(
        'SELECT eid1, eid2, count FROM meet_count'
    ).fetchall()
    if not hbase.insert_meet_count(rows):
        print('\'MeetCount\' 插入失败')

    i = 0
    rows = cur.execute(
        'SELECT eid, time, placeId, address, longitude, latitude FROM record WHERE id > 0 LIMIT 500'
    ).fetchall()

    while rows:
        record_rows = [{
            'eid': row[0],
            'time': row[1],
            'place_id': row[2],
            'address': row[3],
            'longitude': row[4],
            'latitude': row[5]
        } for row in rows]

        if not hbase.insert_record(record_rows):
            print('数据插入失败。')
            exit()

        if i % 10 == 0:
            print(i)

        i += 1
        rows = cur.execute(
            'SELECT eid, time, placeId, address, longitude, latitude FROM record WHERE id > ? LIMIT 500',
            (i * 500, )
        ).fetchall()

    conn.close()

    print('\n数据插入完成！\n已完成初始化。')


if __name__ == '__main__':
    run()

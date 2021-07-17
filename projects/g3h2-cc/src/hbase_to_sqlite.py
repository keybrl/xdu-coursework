import json
import sqlite3


from hbase_manager import HBaseRecord


def run():
    # 读取配置文件
    with open('config.json', 'r', encoding='utf-8') as f:
        config = json.load(f).get('hbase_to_sqlite')

    if config is None:
        print('不能读取应用 `hbase_to_sqlite` 的配置。')
        exit()

    # 获取配置项
    HBASE_HOST = config.get('hbase_host')
    SQLITE_PATH = config.get('sqlite_path')

    if not HBASE_HOST or not SQLITE_PATH:
        print('不能读取应用 `hbase_to_sqlite` 的配置。')
        exit()

    conn = sqlite3.connect(SQLITE_PATH)
    cur = conn.cursor()

    # 建表
    cur.execute('DROP TABLE IF EXISTS vehicle_count')
    cur.execute('DROP TABLE IF EXISTS meet_count')
    conn.commit()
    cur.execute(
        'CREATE TABLE vehicle_count ('
        '    id INTEGER PRIMARY KEY AUTOINCREMENT,'
        '    place_id INTEGER,'
        '    address VARCHAR(128),'
        '    longitude CHAR(10),'
        '    latitude CHAR(10),'
        '    count INTEGER'
        ')'
    )
    cur.execute(
        'CREATE TABLE meet_count ('
        '    id INTEGER PRIMARY KEY AUTOINCREMENT,'
        '    eid1 CHAR(14),'
        '    eid2 CHAR(14),'
        '    count INTEGER'
        ')'
    )
    conn.commit()

    print('正在连接HBase...')
    hbase = HBaseRecord(HBASE_HOST)

    if not hbase.test():
        print('无法连接 HBase 的 REST API 。')
        exit()

    print('正在备份数据')
    result = hbase.get_vehicle_count()
    for i in result:
        cur.execute('INSERT INTO vehicle_count VALUES (null, ?, ?, ?, ?, ?)', i)
    conn.commit()
    result = hbase.get_meet_count()
    for i in result:
        cur.execute('INSERT INTO meet_count VALUES (null, ?, ?, ?)', i)
    conn.commit()


if __name__ == '__main__':
    run()

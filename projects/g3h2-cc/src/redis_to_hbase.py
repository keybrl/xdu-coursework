import json


from hbase_manager import HBaseRecord

import redis


def subscribe(p) -> str:
    print('正在监听频道...')
    s = p.parse_response()
    return s[2].decode() if type(s[2]) == bytes else ''


def transfer_from_redis_to_hbase(hbase: HBaseRecord, r: redis.Redis, key: str):
    print('正在从Redis往HBase转移数据...')

    i = 0
    while True:
        records = r.spop(key, 500)
        if not records:
            break

        rows = []
        for record in records:
            record = json.loads(record.decode())
            rows.append({
                'eid': record['eid'],
                'time': str(record['time']),
                'place_id': str(record['placeId']),
                'address': record['address'],
                'longitude': str(record['longitude']),
                'latitude': str(record['latitude'])
            })

        hbase.insert(rows)

        i += 1
        if i % 10 == 0:
            print('=', end='')


def run():
    # 读取配置文件
    with open('config.json', 'r', encoding='utf-8') as f:
        config = json.load(f).get('redis_to_hbase')

    if config is None:
        print('不能读取应用 `redis_to_hbase` 的配置。')
        exit()

    # 获取配置项
    redis_host = config.get('redis_host')
    redis_port = config.get('redis_port')
    redis_passwd = config.get('redis_passwd')
    redis_db = config.get('redis_db')
    redis_key = config.get('redis_key')
    redis_channel = config.get('redis_channel')
    hbase_host = config.get('hbase_host')

    if None in (redis_host, redis_port, redis_passwd, redis_db, redis_key, redis_channel, hbase_host):
        print('不能读取应用 `redis_to_hbase` 的配置。')
        exit()

    print('连接Redis...')
    r = redis.Redis(host=redis_host, port=redis_port, db=redis_db, password=redis_passwd)
    try:
        r.exists(redis_key)
    except redis.exceptions.ConnectionError as e:
        print('无法连接 Redis 服务：{err}'.format(err=e))
        exit()

    print('正在连接HBase...')
    hbase = HBaseRecord(hbase_host)

    if not hbase.test():
        print('无法连接 HBase 的 REST API 。')
        exit()

    choice = input('是否需要初始化（重置）HBase？ (\'Y\' 或 \'n\'): ')
    if choice == 'Y':
        if not hbase.init():
            print('建表失败。')
            exit()
        print('已初始化Hbase')

    p = r.pubsub()
    p.subscribe(redis_channel)
    print('已订阅频道，\'{channel}\' 。'.format(channel=redis_channel))
    try:
        while True:
            mes = subscribe(p)
            if mes == 'hello':
                transfer_from_redis_to_hbase(hbase, r, redis_key)
            else:
                print('某人在频道里说 \'{mes}\' ，这是什么意思？'.format(mes=mes))
    except KeyboardInterrupt as e:
        p.unsubscribe(redis_channel)
        print('已停止..')


if __name__ == '__main__':
    run()

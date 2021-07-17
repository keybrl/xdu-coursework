import json
import os

import redis


def run():
    # 读取配置文件
    with open('config.json', 'r', encoding='utf-8') as f:
        config = json.load(f).get('json_to_redis')

    if config is None:
        print('不能读取应用 `json_to_redis` 的配置。')
        exit()

    # 获取配置项
    JSON_PATH = config.get('json_path')
    REDIS_HOST = config.get('redis_host')
    REDIS_PORT = config.get('redis_port')
    REDIS_PASSWD = config.get('redis_passwd')
    REDIS_DB = config.get('redis_db')
    REDIS_KEY = config.get('redis_key')

    if None in (JSON_PATH, REDIS_HOST, REDIS_PORT, REDIS_PASSWD, REDIS_DB):
        print('不能读取应用 `json_to_redis` 的配置。')
        exit()

    if not os.path.isfile(JSON_PATH):
        print('JSON 文件 \'{json_path}\' 不存在。'.format(json_path=JSON_PATH))
        exit()

    r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, db=REDIS_DB, password=REDIS_PASSWD)
    try:
        res = r.exists(REDIS_KEY)
        if res != 0:
            choice = input('Redis 中 key \'{key}\' 已存在，是否覆盖它？(\'Y\' 或 \'n\')：'.format(key=REDIS_KEY))
            if choice == 'Y':
                r.delete(REDIS_KEY)
                print('已删除 key \'{key}\' ...'.format(key=REDIS_KEY))
            else:
                print('操作中止...')
                exit()
    except redis.exceptions.ConnectionError as e:
        print('无法连接 Redis 服务：{err}'.format(err=e))
        exit()

    with open(JSON_PATH, 'r', encoding='utf-8') as f:

        row = f.readline()
        i = 0

        print('正在往 Redis 插入数据...')
        while row:
            row = row[:-1] if row[-1] == '\n' else row
            r.sadd(REDIS_KEY, row)

            i += 1

            # 每插入1000条数据，commit一次
            if i == 5000:
                i = 0
                print('=', end='')
                r.publish('SE-5035', 'hello')

            row = f.readline()

    print('\n插入完成.')


if __name__ == "__main__":
    run()

import json
import time

from hbase_manager import HBaseRecord


def run():
    # 读取配置文件
    with open('config.json', 'r', encoding='utf-8') as f:
        config = json.load(f).get('get_places')

    if config is None:
        print('不能读取应用 `get_places` 的配置。')
        exit()

    # 获取配置项
    HBASE_HOST = config.get('hbase_host')

    if not HBASE_HOST:
        print('不能读取应用 `get_places` 的配置。')
        exit()

    print('正在连接数据库...')
    hbase = HBaseRecord(HBASE_HOST)

    if not hbase.test():
        print('无法连接 HBase 的 REST API 。')
        exit()

    while True:
        eid = input('eid: ')
        if eid == 'quit':
            break
        start_time = input('Start Time: ')
        end_time = input('End Time: ')

        print('Result:')
        for i in hbase.get_places(eid, start_time, end_time):
            eid, time_stamp, place_id = i[0].split('##')
            print(eid, time.strftime('%Y-%m-%d_%H:%M:%S', time.localtime(int(time_stamp))), time_stamp, place_id, i[1], i[2], i[3])
        print('----------------\n')

    print('应用已停止...')


if __name__ == '__main__':
    run()

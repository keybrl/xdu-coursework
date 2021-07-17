import base64
import json
import logging
import threading
import time
from hashlib import md5
from typing import Any, Dict, List

import requests

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger()
logger.setLevel(logging.INFO)


def single_thread_test(url: str, test_data: str, test_times: int, tcf: Dict[str, bool], res_pool: List[Any]):
    t_name = threading.current_thread().name
    logger.info(f'{t_name} 已启动')

    for i in range(test_times):
        if tcf['force_stop']:
            logger.info(f'{t_name} 已强行终止')
            return

        start_time = time.time()
        try:
            ret = requests.post(url, json=test_data)
        except Exception as e:
            res_pool.append({'t_name': t_name, 'id': i,  'success': False, 'msg': f'上传图片异常：{e}'})
            logger.info(f'{t_name} test-{i} 上传图片失败')
            continue

        upload_delay = time.time() - start_time

        if ret.status_code != 200:
            res_pool.append({'t_name': t_name, 'id': i,  'success': False, 'msg': f'上传图片失败：{ret.text}'})
            logger.info(f'{t_name} test-{i} 上传图片失败')
            continue

        res_data = json.loads(ret.text)
        res_url = res_data['result']
        # res_url = 'https://ocr.keyboardluo.com/test/dev_check_result?img_id=312ee6f7-9f62-46a7-9567-d81a9a1aff81'

        try:
            ret = requests.get(res_url, timeout=30)
        except Exception as e:
            res_pool.append({'t_name': t_name, 'id': i,  'success': False, 'msg': f'获取结果异常：{e}'})
            logger.info(f'{t_name} test-{i} 获取结果失败')
            continue

        delay = time.time() - start_time

        if ret.status_code != 200:
            res_pool.append({'t_name': t_name, 'id': i,  'success': False, 'msg': f'获取结果失败：{ret.text}'})
            logger.info(f'{t_name} test-{i} 获取结果失败')
            continue

        if md5(ret.text.encode()).hexdigest() != 'da6f8ec5ecf2664f46babf3f210edd12':
            res_pool.append({'t_name': t_name, 'id': i,  'success': False, 'msg': f'获取结果错误：{ret.text}'})
            logger.info(f'{t_name} test-{i} 获取结果错误')
            continue

        res_pool.append({
            't_name': t_name,
            'id': i,
            'success': True,
            'msg': '成功',
            'upload_delay': upload_delay,
            'get_res_delay': delay - upload_delay,
            'total_delay': delay
        })
        logger.info(f'{t_name} test-{i} 已完成')

    logger.info(f'{t_name} 已结束')


def multi_thread_test(url: str, t_num: int, test_times: int, interval: float = 0) -> List[Any]:
    threads = []
    threads_ctrl_flags = {
        'force_stop': False
    }
    res_pool = []

    with open('test_img.png', 'rb') as fp:
        test_data = {
            'type': 'image/png',
            'image': base64.b64encode(fp.read()).decode()
        }

    # 创建子线程
    for i in range(t_num):
        threads.append(
            threading.Thread(
                target=single_thread_test,
                args=(url, test_data, test_times, threads_ctrl_flags, res_pool),
                name=f'node-{i}'
            )
        )

    # 启动子线程
    logger.info(f'正在启动 {t_num} 个子线程...')
    for t in threads:
        t.start()

        if interval:
            time.sleep(interval)

    # 等待所有子线程退出
    try:
        for t in threads:
            t.join()
    # 强制退出
    except KeyboardInterrupt:
        threads_ctrl_flags['force_stop'] = True
        for t in threads:
            t.join()

    return res_pool


def main():
    start_time = time.time()
    res = multi_thread_test('https://ocr.keyboardluo.com/test/dev_upload_img_to_cos', 1, 200)
    end_time = time.time()
    success_count = 0
    for i in res:
        if i['success']:
            success_count += 1
            print(i['total_delay'])
        else:
            print(i)
    print(success_count)
    print(res)
    print(end_time - start_time)
    with open('res/single_thread_200.json', 'wt', encoding='utf-8') as fp:
        json.dump({
            'total_delay': end_time - start_time,
            'success_count': success_count,
            'total_count': 1000,
            'res': res
        }, fp)


if __name__ == '__main__':
    main()

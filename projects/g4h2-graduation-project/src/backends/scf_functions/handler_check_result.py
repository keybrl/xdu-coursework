import json
import logging
import sys
import time
from typing import Dict, List, Optional, Union

from conf import settings
from utils.exceptions import exception_handler, HttpException, BadRequest, NotFound, InternalServerError
from utils.response import generate_response

sys.path.insert(0, './libs')
from qcloud_cos import CosConfig, CosS3Client
from qcloud_cos.cos_exception import CosServiceError, CosClientError


logger = logging.getLogger('check_result')


def main_handler(event, _):
    logger.debug(f'入参： event = {str(event)}')

    try:
        logger.info('提取图像 id ...')
        img_id = get_img_id(event)
        logger.info(f'图像 id ： {img_id}')

        logger.info('检查图像是否存在...')
        has_img = check_has_img(img_id)
        if not has_img:
            err_msg = f'查询图像 id \'{img_id}\' 不存在'
            logger.debug(err_msg)
            raise NotFound(err_msg)
        logger.info('确认图像存在')

        logger.info('查询图像 OCR 结果...')
        result = get_result(img_id)
        logger.info(f'结果：{result}')
        return generate_response(result)

    except HttpException as e:
        return exception_handler(e)


def get_img_id(event: Dict[str, Union[str, int, list, Dict]]) -> str:
    """获取图像 id

    :param event: SCF 函数的入参
    :return: 图像 id
    """
    img_id = event['queryString'].get('img_id')

    if not img_id:
        err_msg = '未指定图像 id ，需通过请求参数 `img_id` 指定'
        logger.debug(err_msg)
        raise BadRequest(err_msg)

    if img_id.find('/') != -1:
        err_msg = f'img_id: \'{img_id}\' 取值非法，不能含有 \'/\''
        logger.debug(err_msg)
        raise BadRequest(err_msg)

    return img_id


def check_has_img(img_id: str) -> bool:
    """检查图像是否存在

    在 COS 中检查 id 对应图像是否存在

    :param img_id: 图像 id
    :return: 是否存在
    """
    config = CosConfig(
        SecretId=settings.QCLOUD_SECRET_ID,
        SecretKey=settings.QCLOUD_SECRET_KEY,
        Region=settings.QCLOUD_REGION,
        Scheme='https'
    )
    client = CosS3Client(config)

    try:
        client.head_object(Bucket=settings.COS_BUCKET, Key=f'{settings.IMAGES_ROOT}/{img_id}')
        return True

    except CosServiceError as e:

        if e.get_error_code() == 'NoSuchResource':
            return False

        err_msg = f'请求图像 HEAD 信息时， COS 服务端返回异常，异常信息：[{e.get_error_code()}] {e.get_error_msg()}'
        logger.error(err_msg)
        raise InternalServerError(err_msg)

    except CosClientError as e:
        err_msg = f'请求图像 HEAD 信息时， COS 客户端返回异常，异常信息：{e}'
        logger.error(err_msg)
        raise InternalServerError(err_msg)


def get_result(img_id: str) -> Optional[List[Dict[str, Union[str, int, list, Dict]]]]:
    """获取图像 OCR 结果

    在超时时间内，反复查询 COS ，直到得到所需的结果或者超时
    超时时间由 conf.settings.CHECK_RESULT_TIMEOUT 确定
    轮询间隔由 conf.settings.CHECK_RESULT_INTERVAL_TIME 确定
    超时后还会试最后一次

    :param img_id: 图像 id
    :return: 查询结果（可 JSON 序列化的对象），如果直到超时也没有查询到结果，则返回 None
    """

    config = CosConfig(
        SecretId=settings.QCLOUD_SECRET_ID,
        SecretKey=settings.QCLOUD_SECRET_KEY,
        Region=settings.QCLOUD_REGION,
        Scheme='https'
    )
    client = CosS3Client(config)

    timeout = 0 if settings.CHECK_RESULT_TIMEOUT < 0 else settings.CHECK_RESULT_TIMEOUT
    start_time = time.time()

    # 最后一次机会
    last_chance = True

    # 请求次数计数器
    req_count = 0

    while time.time() - start_time <= timeout or last_chance:

        # 超时后消费掉最后一次机会
        if time.time() - start_time > timeout:
            logger.info('轮询 OCR 结果，已超时，进行最后一次查询')
            last_chance = False

        req_count += 1

        try:
            logger.info(f'轮询 OCR 结果，第 {req_count} 次查询')
            ret = client.get_object(Bucket=settings.COS_BUCKET, Key=f'{settings.RESULTS_ROOT}/{img_id}.json')
            data = json.load(ret['Body'].get_raw_stream())
            return data

        except CosServiceError as e:

            # 如果查询不到，等待一个间隔时间后继续
            if e.get_error_code() == 'NoSuchKey':
                time.sleep(settings.CHECK_RESULT_INTERVAL_TIME)
                continue

            err_msg = f'请求 OCR 结果时， COS 服务端返回异常，异常信息：[{e.get_error_code()}] {e.get_error_msg()}'
            logger.error(err_msg)
            raise InternalServerError(err_msg)

        except CosClientError as e:
            err_msg = f'请求 OCR 结果时， COS 客户端返回异常，异常信息：{e}'
            logger.error(err_msg)
            raise InternalServerError(err_msg)

        except json.JSONDecodeError as e:
            err_msg = f'请求 OCR 结果时，结果不可 JSON 反序列化，错误信息：{e}'
            logger.error(err_msg)
            raise InternalServerError(err_msg)

    return None

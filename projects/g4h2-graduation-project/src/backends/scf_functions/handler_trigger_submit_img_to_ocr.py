import json
import logging
import re
import sys
from typing import Any, Dict, List

from conf import settings

sys.path.insert(0, './libs')
from tencentcloud.common import credential
from tencentcloud.common.exception.tencent_cloud_sdk_exception import TencentCloudSDKException
from tencentcloud.scf.v20180416 import scf_client


logger = logging.getLogger('trigger_submit_img_to_ocr')


def main_handler(event, _):
    logger.debug(f'入参： event = {str(event)}')

    logger.info('提取新增图像在 COS 中的键名...')
    images_list = get_images_list(event)
    logger.info(f'新增图像：{str([img["key"] for img in images_list])}')

    for image in images_list:
        func_name = settings.SUBMIT_IMG_TO_OCR_FUNC_NAME_FORMATTER.format(f_id=image["filename"][0])
        cred = credential.Credential(settings.QCLOUD_SECRET_ID, settings.QCLOUD_SECRET_KEY)
        client = scf_client.ScfClient(cred, settings.QCLOUD_REGION)

        try:
            ret = client.call(
                'Invoke',
                {
                    'Namespace': settings.SUBMIT_IMG_TO_OCR_FUNC_NAMESPACE,
                    'FunctionName': func_name,
                    'InvocationType': 'Event',
                    'ClientContext': json.dumps(image)
                }
            )
            logger.debug(f'已调用函数，返回：{ret}')
        except TencentCloudSDKException as e:
            err_msg = f'调用 SCF 函数时接口返回异常，异常信息：{e}'
            logger.error(err_msg)
            continue

    logger.info('完成。')

    return images_list


def get_images_list(event: Dict[str, Any]) -> List[Dict[str, Any]]:
    """提取图片列表

    从一次 COS 触发的事件中提取出涉及的新增图片的信息

    :param event: COS 触发器产生的事件， SCF 函数的入参 `event`
    :return: 图片在 COS 中路径的列表
    """
    records = event.get('Records')
    if records is None:
        logger.warning('event 结构不符合预期，event 中不含有 Records 属性，已忽略该事件')
        return []

    images_list = []

    for i, record in enumerate(records):
        cos = record.get('cos')
        if cos is None:
            logger.warning(f'event 结构不符合预期， event.Records[{i}] 中不含有 cos 属性，已忽略该记录')
            continue

        bucket = cos['cosBucket']

        source_bucket_name = f'{bucket.get("name")}-{bucket.get("appid")}'
        if source_bucket_name != settings.COS_BUCKET:
            logger.warning(
                f'事件源 bucket \'{source_bucket_name}\' 并不是 conf.settings '
                f'中 COS_BUCKET 配置的 \'{settings.COS_BUCKET}\' ，'
                f'已忽略该记录'
            )
            continue

        obj = cos['cosObject']

        content_type = obj['meta'].get('Content-Type')
        if content_type not in ('image/png', 'image/jpeg'):
            logger.warning(f'暂不支持的文件类型 {content_type}')
            continue

        key = re.match(r'^/([0-9]+?)/([^/]+?)/(.+?)$', obj['key'])
        if key is None:
            logger.warning(f'event.Records[{i}].cos.cosObject.key 结构不符合预期，已忽略该记录')
            continue

        key = key.group(3)
        filename = key.split('/')[-1]
        images_list.append({
            'key': key,
            'filename': filename
        })

    return images_list

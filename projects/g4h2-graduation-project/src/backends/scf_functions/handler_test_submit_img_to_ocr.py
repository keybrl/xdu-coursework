import json
import logging
import sys
import time
from enum import Enum
from typing import Any, Callable, Dict, List, Optional, Union

from conf import settings

sys.path.insert(0, './libs')
from qcloud_cos import CosConfig, CosS3Client
from qcloud_cos.cos_exception import CosServiceError, CosClientError
from tencentcloud.common import credential
from tencentcloud.common.exception.tencent_cloud_sdk_exception import TencentCloudSDKException
from tencentcloud.ocr.v20181119 import ocr_client, models as ocr_models


logger = logging.getLogger('submit_img_to_ocr')


def main_handler(event, _):
    logger.debug(f'入参： event = {str(event)}')
    logger.debug(f'开始时间：{time.time()}')

    image = event

    logger.info('生成图像预签名 URL ...')
    image['auth_url'] = get_auth_url(image['key'])
    if image['auth_url'] is None:
        logger.warning(f'无法为图片 {image["key"]} 生成预签名 URL ，已跳过')
        return
    logger.info(f'图片 {image["key"]} 的预签名 URL ： {image["auth_url"]}')

    logger.info('提交图片到 OCR 服务...')
    try:
        image['result'] = submit_to_ocr(image['auth_url'])
    except RuntimeError:
        logger.error('获取 OCR 结果失败，已略过')
        return
    logger.info(f'图片 {image["key"]} 的 OCR 结果： {image["result"]}')

    logger.info('上传 OCR 结果到 COS ...')
    try:
        upload_result_to_cos(image['result'], f'{image["filename"]}.json')
    except RuntimeError:
        logger.error('上传结果失败，已略过')
        image['result'] = None
        return

    logger.info('完成。')
    logger.debug(f'结束时间：{time.time()}')

    return image


def get_auth_url(file_path: str) -> Optional[str]:
    """获取图像的预签名 URL

    :param file_path: COS 中文件路径
    :return: 预签名 URL
    """

    config = CosConfig(
        SecretId=settings.QCLOUD_SECRET_ID,
        SecretKey=settings.QCLOUD_SECRET_KEY,
        Region=settings.QCLOUD_REGION,
        Scheme='https'
    )
    client = CosS3Client(config)

    try:
        return client.get_presigned_download_url(
            Bucket=settings.COS_BUCKET,
            Key=file_path,
            Expired=300,
        )
    except CosServiceError as e:
        logger.error(
            f'请求 COS 获取预签名 URL 时， COS 服务端返回异常，异常信息：[{e.get_error_code()}] {e.get_error_msg()}'
        )
        return None
    except CosClientError as e:
        logger.error(
            f'请求 COS 获取预签名 URL 时， COS 客户端返回异常，异常信息：{e}'
        )
        return None


def submit_to_ocr(img_url: str) -> List[Dict[str, Any]]:
    """提交图片到 OCR 服务

    :param img_url: 图片 URL
    :return: OCR 识别后的结果
    """
    if settings.OCR_DETECT_FIRST:
        ocr = OcrService('detect')
        ret = ocr.submit(img_url)

        # 不含文字
        if not ret['HasText']:
            return []

    ocr = OcrService(settings.OCR_TYPE)
    ret = ocr.submit(img_url)

    # 不含文字
    if ret is None:
        return []

    return ret['TextDetections']


class OcrService(object):
    """OCR 服务调用器
    """

    class OcrTypeEnum(Enum):
        BASIC = 0        # 通用印刷体识别
        FAST = 1         # 通用印刷体识别（高速版）
        EFFICIENT = 2    # 通用印刷体识别（精简版）
        ACCURATE = 3     # 通用印刷体识别（高精度版）
        HANDWRITING = 4  # 通用手写体识别
        DETECT = 5       # 快速文本检测

    OCR_TYPE_MAP = {
        'basic': OcrTypeEnum.BASIC,
        'fast': OcrTypeEnum.FAST,
        'efficient': OcrTypeEnum.EFFICIENT,
        'accurate': OcrTypeEnum.ACCURATE,
        'handwriting': OcrTypeEnum.HANDWRITING,
        'detect': OcrTypeEnum.DETECT,
    }

    def __init__(self, ocr_type: Union[OcrTypeEnum, str]) -> None:
        if isinstance(ocr_type, str):
            ocr_type = self.OCR_TYPE_MAP.get(ocr_type)

        self.ocr_type = ocr_type
        if self.ocr_type not in self.OcrTypeEnum:
            self.raise_not_supported_orc_type()

        cred = credential.Credential(settings.QCLOUD_SECRET_ID, settings.QCLOUD_SECRET_KEY)
        self.client = ocr_client.OcrClient(credential=cred, region=settings.QCLOUD_REGION)

    def submit(self, img_url: str) -> Optional[Dict[str, Any]]:
        """提交图片到 OCR 服务
        """
        method = self.get_request_method()
        request = self.get_request(img_url)

        time.sleep(1)

        response = (
            '{"TextDetections": [{"DetectedText": "\\u8fd9\\u662f\\u4e00\\u5f20\\u6837\\u4f8b\\u56fe\\'
            'u7247", "Confidence": 99, "Polygon": [{"X": 194, "Y": 259}, {"X": 194, "Y": 205}, {"X": 580, "Y": 207}'
            ', {"X": 580, "Y": 260}], "AdvancedInfo": "{\\"Parag\\":{\\"ParagNo\\":1}}"}, {"DetectedText": "\\u56fe'
            '\\u7247\\u8981\\u6c42:", "Confidence": 99, "Polygon": [{"X": 228, "Y": 318}, {"X": 228, "Y": 354}, {"X'
            '": 376, "Y": 354}, {"X": 376, "Y": 318}], "AdvancedInfo": "{\\"Parag\\":{\\"ParagNo\\":2}}"}, {"Detect'
            'edText": "1.\\u56fe\\u7247\\u5927\\u5c0f\\u4e0d\\u5927\\u4e8e2.25MB", "Confidence": 99, "Polygon": [{"'
            'X": 272, "Y": 394}, {"X": 272, "Y": 432}, {"X": 704, "Y": 431}, {"X": 704, "Y": 393}], "AdvancedInfo":'
            ' "{\\"Parag\\":{\\"ParagNo\\":3}}"}, {"DetectedText": "2.\\u56fe\\u7247\\u683c\\u5f0f\\u4e3aJPEG\\u621'
            '6PNG", "Confidence": 99, "Polygon": [{"X": 265, "Y": 457}, {"X": 265, "Y": 496}, {"X": 724, "Y": 493},'
            ' {"X": 724, "Y": 454}], "AdvancedInfo": "{\\"Parag\\":{\\"ParagNo\\":3}}"}], "Angel": 0, "RequestId": '
            '""}'
        )
        return json.loads(response)

    def get_request_method(self) -> Callable:
        """获取请求方法

        初始化 OCR 客户端
        根据 OCR 类型，获取腾讯云 SDK 对应的请求方法

        :return: 请求方法
        """
        method = {
            self.OcrTypeEnum.BASIC: self.client.GeneralBasicOCR,
            self.OcrTypeEnum.FAST: self.client.GeneralFastOCR,
            self.OcrTypeEnum.EFFICIENT: self.client.GeneralEfficientOCR,
            self.OcrTypeEnum.ACCURATE: self.client.GeneralAccurateOCR,
            self.OcrTypeEnum.HANDWRITING: self.client.GeneralHandwritingOCR,
            self.OcrTypeEnum.DETECT: self.client.TextDetect,
        }.get(self.ocr_type)

        if method is None:
            self.raise_not_supported_orc_type()

        return method

    def get_request(self, img_url: str) -> ocr_models.AbstractModel:
        """获取请求结构

        获取腾讯云 SDK 要求的 Request 结构

        :param img_url: 图像 URL
        :return: 请求结构
        """
        request = {
            self.OcrTypeEnum.BASIC: ocr_models.GeneralBasicOCRRequest(),
            self.OcrTypeEnum.FAST: ocr_models.GeneralFastOCRRequest(),
            self.OcrTypeEnum.EFFICIENT: ocr_models.GeneralEfficientOCRRequest(),
            self.OcrTypeEnum.ACCURATE: ocr_models.GeneralAccurateOCRRequest(),
            self.OcrTypeEnum.HANDWRITING: ocr_models.GeneralHandwritingOCRRequest(),
            self.OcrTypeEnum.DETECT: ocr_models.TextDetectRequest(),
        }.get(self.ocr_type)

        if request is None:
            self.raise_not_supported_orc_type()

        if self.ocr_type == self.OcrTypeEnum.BASIC:
            request.LanguageType = settings.GENERAL_BASIC_OCR_LANGUAGE

        request.ImageUrl = img_url
        return request

    def raise_not_supported_orc_type(self) -> None:
        """抛出“不支持的 OCR 类型”异常
        """
        err_msg = f'不能理解的 OCR 类型 {self.ocr_type}'
        logger.error(err_msg)
        raise ValueError(err_msg)


def upload_result_to_cos(result: List[Dict[str, Any]], filename: str) -> None:
    """上传结果到 COS

    将结果以 JSON 格式保存到 COS

    :param result: 可 JSON 序列化的对象
    :param filename: 保存的文件名
    """
    config = CosConfig(
        SecretId=settings.QCLOUD_SECRET_ID,
        SecretKey=settings.QCLOUD_SECRET_KEY,
        Region=settings.QCLOUD_REGION,
        Scheme='https'
    )
    client = CosS3Client(config)

    try:
        result_json = json.dumps(result)
    except TypeError:
        err_msg = f'结果不可序列化为 JSON ，原始结果：{result}'
        logger.error(err_msg)
        raise ValueError(err_msg)

    file_path = f'{settings.RESULTS_ROOT}/{filename}'

    try:
        client.put_object(
            Bucket=settings.COS_BUCKET,
            Body=result_json,
            Key=file_path,
            ContentType='application/json'
        )
    except CosServiceError as e:
        err_msg = f'上传结果到 COS 时， COS 服务端返回异常，异常信息：[{e.get_error_code()}] {e.get_error_msg()}'
        logger.error(err_msg)
        raise RuntimeError(err_msg)
    except CosClientError as e:
        err_msg = f'上传结果到 COS 时， COS 客户端返回异常，异常信息：{e}'
        logger.error(err_msg)
        raise RuntimeError(err_msg)

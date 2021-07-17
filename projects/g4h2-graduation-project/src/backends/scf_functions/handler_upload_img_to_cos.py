import base64
import binascii
import logging
import sys
import uuid
from typing import Any, AnyStr, Dict, Optional

from conf import settings
from utils.exceptions import exception_handler, HttpException, BadRequest, RequestEntityTooLarge, InternalServerError
from utils.request import parse_body
from utils.response import generate_response

sys.path.insert(0, './libs')
from qcloud_cos import CosConfig, CosS3Client
from qcloud_cos.cos_exception import CosServiceError, CosClientError

logger = logging.getLogger('upload_img_to_cos')


IMAGE_TYPES = {
    'jpeg':       ('.jpg', 'image/jpeg'),
    'jpg':        ('.jpg', 'image/jpeg'),
    'image/jpeg': ('.jpg', 'image/jpeg'),

    'png':        ('.png', 'image/png'),
    'image/png':  ('.png', 'image/png'),
}


def main_handler(event, _):
    try:
        # 提取图像
        image = get_image(event)

        # 上传至 COS
        img_id = upload_to_cos(image)

        return generate_response({
            'id': img_id,
            'result': settings.RESULT_URL_FORMATTER.format(f_id=img_id[0], id=img_id),
            'img_direct_url': settings.IMG_DIRECT_URL_FORMATTER.format(id=img_id),
            'result_direct_url': settings.RESULT_DIRECT_URL_FORMATTER.format(id=img_id)
        }, 200)
    except HttpException as e:
        return exception_handler(e)


def get_image(event: Dict[str, Any]) -> Optional[Dict[str, AnyStr]]:
    """提取图像

    从触发事件数据结构中提取出图像内容和元信息

    :param event: SCF 的入参 `event`
    :return: {'type': 'IMAGE_TYPES 中的一个键', 'image': b'图像字节码'}
    """

    data = parse_body(event, allowed_content_types=['application/json', ], allow_none=False)

    img_type = data.get('type')
    img = data.get('image')

    if not img or not img_type:
        err_msg = '不合法的请求，请求 body 中必须带有 `type` 和 `image` 字段'
        logger.debug(err_msg)
        raise BadRequest(err_msg)

    if not isinstance(img, (str, bytes)) or not isinstance(img_type, (str, bytes)):
        err_msg = '不合法的请求，字段 `type` 和 `image` 的值都必须是字符串类型'
        logger.debug(err_msg)
        raise BadRequest(err_msg)

    # 判断图像类型
    img_type = img_type.decode('utf-8') if isinstance(img_type, bytes) else img_type
    if img_type.lower() not in IMAGE_TYPES:
        err_msg = f'不支持图像类型 {img_type} （目前可选值：{", ".join(IMAGE_TYPES.keys())} ）'
        logger.debug(err_msg)
        raise BadRequest(err_msg)
    img_type = img_type.lower()

    img = img.decode('utf-8') if isinstance(img, bytes) else img

    # 判断图像是否过大， 0.75 是 Base64 编码前的大小系数
    if len(img) * 0.75 > settings.IMAGE_MAX_SIZE * 1024 * 1024:
        err_msg = f'图像尺寸过大，图像大小必须小于 {settings.IMAGE_MAX_SIZE}MB'
        logger.debug(err_msg)
        raise RequestEntityTooLarge(err_msg)

    # 对图像进行 Base64 解码
    try:
        img = base64.b64decode(img)
    except binascii.Error:
        err_msg = f'不合法的请求，字段 `image` 的值必须是 Base64 编码的字符串'
        logger.debug(err_msg)
        raise BadRequest(err_msg)

    return {
        'type': img_type,
        'image': img
    }


def upload_to_cos(image: Dict[str, AnyStr]) -> str:
    """将图像上传到 COS

    :param image: 方法 get_image 的返回值
    :return: 图像唯一标识，一串 uuid
    """
    config = CosConfig(
        SecretId=settings.QCLOUD_SECRET_ID,
        SecretKey=settings.QCLOUD_SECRET_KEY,
        Region=settings.QCLOUD_REGION,
        Scheme='https'
    )
    client = CosS3Client(config)

    img_id = str(uuid.uuid4())
    _, img_content_type = IMAGE_TYPES[image['type']]

    try:
        client.put_object(
            Bucket=settings.COS_BUCKET,
            Body=image['image'],
            Key=f'{settings.IMAGES_ROOT}/{img_id}',
            ContentType=img_content_type,
        )
    except CosServiceError as e:
        err_msg = f'上传图片时 COS 服务返回错误，错误信息：[{e.get_error_code()}] {e.get_error_msg()}'
        logger.error(err_msg)
        raise InternalServerError(err_msg)
    except CosClientError as e:
        err_msg = f'上传图片时 COS 客户端返回错误，错误信息：{e}'
        logger.error(err_msg)
        raise InternalServerError(err_msg)

    return img_id

"""集成响应

封装了用于生成 SCF 的 API 网关触发器“集成响应”数据结构的方法
详见 https://cloud.tencent.com/document/product/583/12513#apiStructure
"""

import json
import logging
from typing import Any, Dict, Optional


logger = logging.getLogger('utils.response')


STANDARD_STATUS_CODES = [
    100, 101,
    200, 201, 202, 203, 204, 205, 206,
    300, 301, 302, 303, 304, 305, 306, 307,
    400, 401, 402, 403, 404, 405, 406, 407, 408, 409,
    410, 411, 412, 413, 414, 415, 416, 417,
    500, 501, 502, 503, 504, 505,
]


def generate_response(data: Any = None, status: int = 200, headers: Optional[Dict[str, str]] = None):
    """生成集成响应数据结构

    生成一个 application/json 格式的集成响应数据结构，并返回
    包含一些默认行为和简单的参数检查

    :param data:
    :param status:
    :param headers:
    :return: 集成响应数据结构
    """

    if status == 204:
        if data is not None:
            logger.warning('由于响应带有 204 状态码，忽略响应的 body 部分')
            data = None
    else:
        try:
            data = json.dumps(data)
        except TypeError as e:
            err_msg = f'构造响应时，数据转换为 JSON 格式时出错，错误信息：{e}'
            logger.error(err_msg)
            raise ValueError(err_msg)

    if not 100 <= status < 600:
        err_msg = f'不合法的 HTTP 状态码 {status}'
        logger.error(err_msg)
        raise ValueError(err_msg)

    if status not in STANDARD_STATUS_CODES:
        logger.warning(f'状态码 {status} 是非标准的，不一定能被客户端所理解')

    headers = headers or {}
    headers['Content-Type'] = 'application/json'

    return get_response(data, status, headers, False)


def get_response(body: str, status_code: int, headers: Dict[str, str], is_base64_encoded: bool):
    """获取集成响应数据结构

    将入参组织成集成响应要求的数据结构，然后返回。

    :param body: HTTP 返回的 body 内容，取值为字符串
    :param status_code: HTTP 返回的状态码，取值为整数
    :param headers: HTTP 返回的头部内容，取值取值为多个键值对的字典
    :param is_base64_encoded: 指明 body 内的内容是否为 Base64 编码后的二进制内容，取值为 `True` 或者 `False`
    :return: 集成响应数据结构
    """

    return {
        'body': body,
        'statusCode': status_code,
        'headers': headers,
        'isBase64Encoded': is_base64_encoded
    }

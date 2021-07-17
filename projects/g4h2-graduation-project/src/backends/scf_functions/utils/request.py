"""集成请求

包装了一些从 SCF 的入参 `event` 中解析出 API 网关“集成请求”内容的方法
详见 https://cloud.tencent.com/document/product/583/12513
"""

import cgi
import json
import logging
import urllib.parse
from io import BytesIO
from typing import Any, Dict, List, Optional

from .exceptions import BadRequest


logger = logging.getLogger('utils.request')


def parse_body(
        event: Dict[str, Any],
        allowed_content_types: Optional[List[str]] = None,
        allow_none: bool = True
) -> Optional[Dict[str, Any]]:
    """解析 body 中的字段信息

    支持 JSON 、 form-data 和 x-www-form-urlencoded 格式

    :param event: SCF 的入参 `event`
    :param allowed_content_types: 允许的 Content-Type ，
            默认 ['application/json', 'multipart/form-data', 'application/x-www-form-urlencoded']
    :param allow_none: 是否与允许空的 body ，或者说出现空 body 时是返回 None 还是抛出异常
    :return: body 中字段信息，字典格式
    """

    data = event.get('body')

    if data is None:
        if allow_none:
            return None
        else:
            err_msg = '请求 body 为空'
            logger.debug(err_msg)
            raise BadRequest(err_msg)

    content_type = event['headers'].get('content-type', '')
    allowed_content_types = (
        ['application/json', 'multipart/form-data', 'application/x-www-form-urlencoded']
        if allowed_content_types is None
        else allowed_content_types
    )

    # JSON
    if content_type[:16] == 'application/json' and 'application/json' in allowed_content_types:
        try:
            return json.loads(data)
        except json.JSONDecodeError as e:
            err_msg = f'以 JSON 解析 body 时发生错误，错误信息：{e}'
            logger.debug(err_msg)
            raise BadRequest(err_msg)

    # form-data
    elif content_type[:19] == 'multipart/form-data' and 'multipart/form-data' in allowed_content_types:
        _, pdict = cgi.parse_header(content_type)
        if not pdict or not pdict.get('boundary'):
            err_msg = f'以 form-data 解析 body 时，发现无法理解的请求头 \'Content-Type: {content_type}\''
            logger.debug(err_msg)
            raise BadRequest(err_msg)
        pdict['boundary'] = pdict['boundary'].encode('utf-8')

        try:
            data = cgi.parse_multipart(BytesIO(data.encode('utf-8')), pdict)
        except Exception as e:
            err_msg = f'以 form-data 解析 body 时发生错误，错误信息：{e}'
            logger.debug(err_msg)
            raise BadRequest(err_msg)

        return {
            k: v[0] if len(v) == 1 else v
            for k, v in data.items()
        }

    # x-www-form-urlencoded
    elif (
        content_type[:33] == 'application/x-www-form-urlencoded'
        and 'application/x-www-form-urlencoded' in allowed_content_types
    ):
        data = urllib.parse.parse_qs(data, keep_blank_values=True)
        return {
            k: v[0] if len(v) == 1 else v
            for k, v in data.items()
        }

    # 不支持的格式
    else:
        err_msg = f'不支持解析 {content_type} 格式的 body （仅支持 {", ".join(allowed_content_types)}）'
        logger.debug(err_msg)
        raise BadRequest(err_msg)

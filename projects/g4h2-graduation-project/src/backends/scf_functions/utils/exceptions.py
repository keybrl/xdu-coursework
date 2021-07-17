"""HTTP 异常

定义了一些与 HTTP 状态码 4xx 和 5xx 相关的异常，和从异常得到对应 HTTP 响应的方法
"""

import logging
from typing import Optional
from .response import generate_response


logger = logging.getLogger('utils.exceptions')


class HttpException(Exception):
    default_status_code = 500
    default_detail = 'Error'

    def __init__(self, detail: Optional[str] = None, status_code: Optional[int] = None):
        self.detail = self.get_detail(detail)
        self.status_code = self.get_status_code(status_code)
        super(HttpException, self).__init__(self.detail)

    @classmethod
    def get_status_code(cls, custom_status_code: Optional[int] = None):
        if hasattr(cls, 'status_code'):
            return cls.status_code
        else:
            if custom_status_code is not None:
                return custom_status_code
            elif hasattr(cls, 'default_status_code'):
                return cls.default_status_code
            else:
                err_msg = '无法确认 HTTP 异常的状态码'
                logger.error(err_msg)
                ValueError(err_msg)

    @classmethod
    def get_detail(cls, custom_detail: Optional[str] = None):
        if hasattr(cls, 'detail'):
            return cls.detail
        else:
            if custom_detail is not None:
                return custom_detail
            elif hasattr(cls, 'default_detail'):
                return cls.default_detail
            else:
                err_msg = '无法确认 HTTP 异常的异常信息'
                logger.error(err_msg)
                ValueError(err_msg)


def exception_handler(e: HttpException):
    return generate_response(e.detail, e.status_code)


class BadRequest(HttpException):
    default_detail = 'Bad Request'
    status_code = 400


class Forbidden(HttpException):
    default_detail = 'Forbidden'
    status_code = 403


class NotFound(HttpException):
    default_detail = 'Not Found'
    status_code = 404


class RequestEntityTooLarge(HttpException):
    default_detail = 'Request Entity Too Large'
    status_code = 413


class InternalServerError(HttpException):
    default_detail = 'Internal Server Error'
    status_code = 500

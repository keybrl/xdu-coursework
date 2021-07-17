import json
import unittest
from typing import List, Tuple

from .exceptions import (
    exception_handler, HttpException,
    BadRequest, Forbidden, NotFound, RequestEntityTooLarge, InternalServerError,
)
from .request import parse_body


class TestRequest(unittest.TestCase):
    def test_parse_body_with_json(self):
        self.assertEqual(
            {'a': 'a', 'b': 1, 'c': [1, 2, '3']},
            parse_body({
                'body': '{"a": "a", "b": 1, "c": [1, 2, "3"]}',
                'headers': {'content-type': 'application/json'}
            }),
            'parse_body 以 JSON 解析时，返回结果与预期不符'
        )
        self.assertRaisesRegex(
            BadRequest,
            r'以 JSON 解析 body 时发生错误，错误信息：(.*?)',
            parse_body,
            event={'body': 'whatever', 'headers': {'content-type': 'application/json'}}
        )

    def test_parse_body_with_form_data(self):
        self.assertEqual(
            {'a': b'a', 'b': b'1', 'c': [b'1', b'2', b'3']},
            parse_body({
                'body': (
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"a\"\r\n\r\na\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"b\"\r\n\r\n1\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"c\"\r\n\r\n1\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"c\"\r\n\r\n2\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"c\"\r\n\r\n3\r\n'
                    '----------------------------941656935662493127909519--\r\n'
                ),
                'headers': {
                    'content-type': 'multipart/form-data; boundary=--------------------------941656935662493127909519'
                }
            }),
            'parse_body 以 form-data 解析时，返回结果与预期不符'
        )
        self.assertRaisesRegex(
            BadRequest,
            r'以 form-data 解析 body 时，发现无法理解的请求头 \'Content-Type: multipart/form-data\'',
            parse_body,
            event={
                'body': '',
                'headers': {
                    'content-type': 'multipart/form-data'
                }
            }
        )
        self.assertRaisesRegex(
            BadRequest,
            r'以 form-data 解析 body 时发生错误，错误信息：(.*?)',
            parse_body,
            event={
                'body': (
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"a\"\r\n\r\na\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"b\"\r\n\r\n1\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"c\"\r\n\r\n1\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"c\"\r\n\r\n2\r\n'
                    '----------------------------941656935662493127909519\r\n'
                    'Content-Disposition: form-data; name=\"c\"\r\n\r\n3\r\n'
                    '----------------------------941656935662493127909519--\r\n'
                ),
                'headers': {
                    'content-type': 'multipart/form-data; boundary=\b'
                }
            }
        )

    def test_parse_body_with_x_www_form_urlencoded(self):
        self.assertEqual(
            {'a': 'a', 'b': '1', 'c': ['1', '2', '3']},
            parse_body({
                'body': 'a=a&b=1&c=1&c=2&c=3',
                'headers': {'content-type': 'application/x-www-form-urlencoded'}
            }),
            'parse_body 以 x-www-form-urlencoded 解析时，返回结果与预期不符'
        )

    def test_parse_body_when_no_body(self):
        self.assertIsNone(parse_body({}), '遇到空 body 且若允许空 body 时， parse_body 方法应该返回 None')
        self.assertIsNone(parse_body({}, allow_none=True), '遇到空 body 且允许空 body 时， parse_body 方法应该返回 None')
        self.assertRaisesRegex(
            BadRequest,
            r'请求 body 为空',
            parse_body,
            event={},
            allow_none=False
        )

    def test_parse_body_with_unsupported_content_type(self):
        self.assertRaisesRegex(
            BadRequest,
            r'不支持解析 whatever 格式的 body （仅支持 application/json, multipart/form-data, application/x-www-form-urlencoded）',
            parse_body,
            event={'headers': {'content-type': 'whatever'}, 'body': ''},
        )
        self.assertRaisesRegex(
            BadRequest,
            r'不支持解析 application/json 格式的 body （仅支持 multipart/form-data, application/x-www-form-urlencoded）',
            parse_body,
            event={'headers': {'content-type': 'application/json'}, 'body': ''},
            allowed_content_types=['multipart/form-data', 'application/x-www-form-urlencoded']
        )


class TestResponse(unittest.TestCase):
    pass


class TestExceptions(unittest.TestCase):
    def check_http_exceptions(self, test_cases: List[Tuple[str, HttpException, str, int]]):
        for case in test_cases:
            e = case[1]
            msg = f'测试用例 {case[0]} ，{{msg}}'
            self.assertTrue(hasattr(e, 'detail'), msg.format(msg='异常类的 `detail` 属性不存在'))
            self.assertTrue(hasattr(e, 'status_code'), msg.format(msg='异常类的 `status_code` 属性不存在'))
            self.assertEqual(case[2], e.detail, msg.format(msg='异常类的 `detail` 属性的值与预期不符'))
            self.assertEqual(case[3], e.status_code, msg.format(msg='异常类的 `status_code` 属性的值与预期不符'))

            r = exception_handler(e)
            self.assertIsInstance(r, dict, msg.format(msg='异常处理器返回格式错误'))
            self.assertEqual(
                json.dumps(case[2]),
                r.get('body'),
                msg.format(msg='异常处理器返回响应中 `body` 字段与预期不符')
            )
            self.assertEqual(
                case[3],
                r.get('statusCode'),
                msg.format(msg='异常处理器返回响应中 `statusCode` 字段与预期不符')
            )
            self.assertEqual(
                {'Content-Type': 'application/json'},
                r.get('headers'),
                msg.format(msg='异常处理器返回响应中 `headers` 字段与预期不符')
            )
            self.assertFalse(
                r.get('isBase64Encoded'),
                msg.format(msg='异常处理器返回响应中 `isBase64Encoded` 字段与预期不符')
            )

    def test_http_exception(self):
        test_cases = [
            ('1.1.1', HttpException(), 'Error', 500),
            ('1.1.2', HttpException(None, None), 'Error', 500),
            ('1.1.3', HttpException(None, 400), 'Error', 400),
            ('1.1.4', HttpException('error'), 'error', 500),
            ('1.1.5', HttpException('error', None), 'error', 500),
            ('1.1.6', HttpException('error', 400), 'error', 400),
            ('1.1.7', HttpException(status_code=400, detail='error'), 'error', 400),
        ]
        self.check_http_exceptions(test_cases)

    def test_http_exception_subclass(self):
        test_cases = [
            ('2.1.1', BadRequest(), 'Bad Request', 400),
            ('2.1.2', BadRequest('error'), 'error', 400),
            ('2.1.3', BadRequest(None, 444), 'Bad Request', 400),
            ('2.1.4', BadRequest('error', 444), 'error', 400),

            ('2.2.1', Forbidden(), 'Forbidden', 403),
            ('2.2.2', Forbidden('error'), 'error', 403),
            ('2.2.3', Forbidden(None, 444), 'Forbidden', 403),
            ('2.2.4', Forbidden('error', 444), 'error', 403),

            ('2.3.1', RequestEntityTooLarge(), 'Request Entity Too Large', 413),
            ('2.3.2', RequestEntityTooLarge('error'), 'error', 413),
            ('2.3.3', RequestEntityTooLarge(None, 444), 'Request Entity Too Large', 413),
            ('2.3.4', RequestEntityTooLarge('error', 444), 'error', 413),

            ('2.4.1', InternalServerError(), 'Internal Server Error', 500),
            ('2.4.2', InternalServerError('error'), 'error', 500),
            ('2.4.3', InternalServerError(None, 444), 'Internal Server Error', 500),
            ('2.4.4', InternalServerError('error', 444), 'error', 500),

            ('2.2.1', NotFound(), 'Not Found', 404),
            ('2.2.2', NotFound('error'), 'error', 404),
            ('2.2.3', NotFound(None, 444), 'Not Found', 404),
            ('2.2.4', NotFound('error', 444), 'error', 404),
        ]
        self.check_http_exceptions(test_cases)

import base64
import json
import logging
import unittest
from hashlib import md5
from io import BytesIO
from typing import Any, AnyStr, Dict, Tuple
from unittest import mock

from conf import settings
import handler_upload_img_to_cos
import handler_trigger_submit_img_to_ocr
import handler_submit_img_to_ocr
import handler_check_result


logger = logging.getLogger('unittest')


def check_cos_config(
        test_case: unittest.TestCase,
        mock_cos_config: mock.MagicMock,
        call_count: int = -1,
        case_id: str = None
):
    """检查 COS 配置是否正确
    """
    case_id_in_msg = f'[case {case_id}] ' if case_id else ''

    if call_count >= 0:
        test_case.assertEqual(call_count, mock_cos_config.call_count, f'{case_id_in_msg}CosConfig 初始化次数与预期不符')

    for call_args in mock_cos_config.call_args_list:

        test_case.assertEqual(0, len(call_args[0]), f'{case_id_in_msg}CosConfig 初始化时有预期外的位置参数')

        test_case.assertEqual(
            settings.QCLOUD_SECRET_ID,
            call_args[1].get('SecretId'),
            f'{case_id_in_msg}CosConfig 初始化时参数 SecretId 不符合预期'
        )

        test_case.assertEqual(
            settings.QCLOUD_SECRET_KEY,
            call_args[1].get('SecretKey'),
            f'{case_id_in_msg}CosConfig 初始化时参数 SecretKey 不符合预期'
        )

        test_case.assertEqual(
            settings.QCLOUD_REGION,
            call_args[1].get('Region'),
            f'{case_id_in_msg}CosConfig 初始化时参数 Region 不符合预期'
        )

        test_case.assertEqual(
            'https',
            call_args[1].get('Scheme'),
            f'{case_id_in_msg}CosConfig 初始化时参数 Scheme 不符合预期'
        )

        test_case.assertEqual(4, len(call_args[1]), '{case_id_in_msg}CosConfig 初始化时有不符合预期数量的关键字参数')


def check_cos_client(
        test_case: unittest.TestCase,
        mock_cos_client: mock.MagicMock,
        call_count: int = -1,
        case_id: str = None
):
    """检查 COS 客户端是否配置正确
    """
    case_id_in_msg = f'[case {case_id}] ' if case_id else ''

    if call_count >= 0:
        test_case.assertEqual(call_count, mock_cos_client.call_count, f'{case_id_in_msg}CosS3Client 初始化次数与预期不符')

    for call_args in mock_cos_client.call_args_list:
        test_case.assertEqual(1, len(call_args[0]), f'{case_id_in_msg}CosS3Client 初始化时有不符合预期数量的位置参数')
        test_case.assertEqual(0, len(call_args[1]), f'{case_id_in_msg}CosS3Client 初始化时有预期之外的关键字参数')
        test_case.assertEqual('config', call_args[0][0], f'{case_id_in_msg}CosS3Client 初始化时参数与预期不符')


class TestUploadImgToCos(unittest.TestCase):
    @staticmethod
    def open_img(path: str) -> Tuple[Dict[str, AnyStr], str]:
        """打开图像
        """
        img_suffix = path.split('.')[-1] if len(path.split('.')) != 0 else None
        if img_suffix in ('jpg', 'jpeg', 'JPG', 'JPEG'):
            img_type = 'image/jpeg'
        elif img_suffix in ('png', 'PNG'):
            img_type = 'image/png'
        else:
            img_type = 'application/octet-stream'

        with open(path, 'rb') as fp:
            img = fp.read()

        img_md5 = md5(img).hexdigest()

        return {
            'type': img_type,
            'image': base64.b64encode(img).decode()
        }, img_md5

    def generate_test_event(self, img_path: str) -> Tuple[Dict[str, Any], str]:
        """生成测试用的 API 网关触发事件
        """
        opened_img, img_md5 = self.open_img(img_path)
        return {
            'body': json.dumps(opened_img),
            'headers': {'content-type': 'application/json'}
        }, img_md5

    def check_ret(self, ret, status_code: int = 200, case_id: str = None):
        """检查函数响应
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''
        self.assertIsInstance(ret, dict, f'{case_id_in_msg}主函数响应格式与预期不符')
        self.assertEqual(status_code, ret.get('statusCode'), f'{case_id_in_msg}主函数响应状态码与预期不符')
        self.assertFalse(ret.get('isBase64Encoded'), f'{case_id_in_msg}主函数响应 isBase64Encoded 字段与预期不符')

        if status_code == 200:
            self.assertEqual(
                'application/json',
                ret.get('headers', {}).get('Content-Type'),
                f'{case_id_in_msg}主函数响应 Content-Type 与预期不符'
            )

            ret_body = json.loads(ret.get('body'))

            img_id = ret_body.get('id')
            self.assertRegex(
                img_id,
                r'[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}',
                f'{case_id_in_msg}主函数响应图片 id 与预期不符'
            )

            result_url = ret_body.get('result')
            self.assertEqual(
                settings.RESULT_URL_FORMATTER.format(f_id=img_id[0], id=img_id),
                result_url,
                f'{case_id_in_msg}主函数响应图片 result 与预期不符'
            )

            img_direct_url = ret_body.get('img_direct_url')
            self.assertEqual(
                settings.IMG_DIRECT_URL_FORMATTER.format(f_id=img_id[0], id=img_id),
                img_direct_url,
                f'{case_id_in_msg}主函数响应图片 img_direct_url 与预期不符'
            )

            result_direct_url = ret_body.get('result_direct_url')
            self.assertEqual(
                settings.RESULT_DIRECT_URL_FORMATTER.format(f_id=img_id[0], id=img_id),
                result_direct_url,
                f'{case_id_in_msg}主函数响应图片 result_direct_url 与预期不符'
            )

    def check_cos_put_object(
            self, client: mock.MagicMock,
            img_id: str,
            img_content_type: str,
            img_md5: str,
            case_id: str = None
    ):
        """检查 COS 对象是否按正确方式上传
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''

        self.assertEqual(
            settings.COS_BUCKET,
            client.put_object.call_args[1].get('Bucket'),
            f'{case_id_in_msg}上传 COS 时 Bucket 参数与预期不符'
        )
        self.assertEqual(
            f'{settings.IMAGES_ROOT}/{img_id}',
            client.put_object.call_args[1].get('Key'),
            f'{case_id_in_msg}上传 COS 时 Key 参数与预期不符'
        )
        self.assertEqual(
            img_content_type,
            client.put_object.call_args[1].get('ContentType'),
            f'{case_id_in_msg}上传 COS 时 ContentType 参数与预期不符'
        )
        self.assertEqual(
            img_md5,
            md5(client.put_object.call_args[1].get('Body')).hexdigest(),
            f'{case_id_in_msg}上传 COS 时 Body 参数与预期不符'
        )

    @mock.patch('handler_upload_img_to_cos.CosConfig')
    @mock.patch('handler_upload_img_to_cos.CosS3Client')
    def test_main_process(self, mock_cos_client, mock_cos_config):
        """测试主流程

        测试用例：
        1.1 test_data/test-img-normal-1.jpg 普通 JPEG 图片
        1.2 test_data/test-img-normal-2.png 普通 PNG 图片
        """

        mock_cos_config.return_value = 'config'
        client = mock_cos_client.return_value

        # 测试 JPEG 图片
        event, img_md5 = self.generate_test_event('test_data/test-img-normal-1.jpg')
        ret = handler_upload_img_to_cos.main_handler(event, None)

        self.check_ret(ret, 200, '1.1')
        img_id = json.loads(ret.get('body')).get('id')

        self.check_cos_put_object(client, img_id, 'image/jpeg', img_md5, '1.1')

        # 测试 PNG 图片
        event, img_md5 = self.generate_test_event('test_data/test-img-normal-2.png')
        ret = handler_upload_img_to_cos.main_handler(event, None)

        self.check_ret(ret, 200, '1.2')
        img_id = json.loads(ret.get('body')).get('id')

        self.check_cos_put_object(client, img_id, 'image/png', img_md5, '1.2')

        check_cos_config(self, mock_cos_config, 2, '1.x')
        check_cos_client(self, mock_cos_client, 2, '1.x')

    @mock.patch('handler_upload_img_to_cos.CosConfig')
    @mock.patch('handler_upload_img_to_cos.CosS3Client')
    def test_img_too_big(self, mock_cos_client, mock_cos_config):
        """测试图片过大的情况

        测试用例：
        1.3 test_data/test-img-too-big-1.png 过大图片
        """

        mock_cos_config.return_value = 'config'
        client = mock_cos_client.return_value

        event, img_md5 = self.generate_test_event('test_data/test-img-too-big-1.png')
        ret = handler_upload_img_to_cos.main_handler(event, None)

        self.check_ret(ret, 413, '1.3')
        self.assertFalse(client.put_object.called, '[case 1.3] 向 COS 发送了过大的图片')

    @mock.patch('handler_upload_img_to_cos.CosConfig')
    @mock.patch('handler_upload_img_to_cos.CosS3Client')
    def test_bad_request(self, mock_cos_client, mock_cos_config):
        """测试格式不合法的请求

        测试用例： 1.4 - 1.10
        """

        mock_cos_config.return_value = 'config'
        client = mock_cos_client.return_value

        test_cases = [
            ('1.4', {}),
            ('1.5', {'body': '{}', 'headers': {}}),
            ('1.6', {'body': '{}', 'headers': {'content-type': 'application/xml'}}),
            (
                '1.7',
                {'body': '{}', 'headers': {'content-type': 'application/json'}}
            ),
            (
                '1.8',
                {'body': '{"type": "image/jpeg", "image": [1, 2]}', 'headers': {'content-type': 'application/json'}}
            ),
            (
                '1.9',
                {'body': '{"type": "image/gif", "image": "test"}', 'headers': {'content-type': 'application/json'}}
            ),
            (
                '1.10',
                {'body': '{"type": "image/jpeg", "image": "testa=="}', 'headers': {'content-type': 'application/json'}}
            ),
        ]

        for i, case in test_cases:
            ret = handler_upload_img_to_cos.main_handler(case, None)
            self.check_ret(ret, 400, i)
            self.assertFalse(client.put_object.called, f'[case {i}] 非法请求触发了向 COS 的发送')

    @mock.patch('handler_upload_img_to_cos.CosConfig')
    @mock.patch('handler_upload_img_to_cos.CosS3Client')
    def test_cos_exception(self, mock_cos_client, mock_cos_config):
        """测试 COS 异常情况

        测试用例：
        1.11 上传时抛出 CosServiceError
        1.12 上传时抛出 CosClientError
        """

        mock_cos_config.return_value = 'config'
        client = mock_cos_client.return_value

        event, _ = self.generate_test_event('test_data/test-img-normal-1.jpg')

        client.put_object.side_effect = handler_upload_img_to_cos.CosServiceError('PUT', 'message', 'status_code')
        ret = handler_upload_img_to_cos.main_handler(event, None)
        self.check_ret(ret, 500, '1.11')

        client.put_object.side_effect = handler_upload_img_to_cos.CosClientError('message')
        ret = handler_upload_img_to_cos.main_handler(event, None)
        self.check_ret(ret, 500, '1.12')


class TestTriggerSubmitImgToOcr(unittest.TestCase):
    @staticmethod
    def generate_test_event(img_path: str, content_type: str):
        """生成用于触发函数的事件
        """
        bucket_cli_name = settings.COS_BUCKET[:-len(settings.QCLOUD_APP_ID) - 1]

        return {
            'Records': [{
                'cos': {
                    'cosBucket': {
                        'name': bucket_cli_name,
                        'appid': settings.QCLOUD_APP_ID,
                    },
                    'cosObject': {
                        'key': f'/{settings.QCLOUD_APP_ID}/{bucket_cli_name}/{img_path}',
                        'meta': {
                            'Content-Type': content_type
                        }
                    }
                }
            }]
        }

    def check_ret(self, ret: Any, key: str, case_id: str = None):
        """检查函数返回结果
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''

        self.assertIsInstance(ret, list, f'{case_id_in_msg}函数返回结果类型不符合预期')
        self.assertEqual(1, len(ret), f'{case_id_in_msg}函数返回结果条数不符合预期')
        self.assertIsInstance(ret[0], dict, f'{case_id_in_msg}函数返回结果子条目类型不符合预期')
        self.assertEqual(key, ret[0].get('key'), f'{case_id_in_msg}函数返回结果 key 字段不符合预期')
        self.assertEqual(key.split('/')[-1], ret[0].get('filename'), f'{case_id_in_msg}函数返回结果 filename 字段不符合预期')
        self.assertEqual(2, len(ret[0]), f'{case_id_in_msg}函数返回结果子条目键数目不符合预期')

    def check_scf_call(self, client: mock.MagicMock, key: str, case_id: str = None):
        """检查是否正确使用 SCF 的 call 方法
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''

        self.assertEqual(
            2,
            len(client.call.call_args[0]),
            f'{case_id_in_msg}调用 SCF 的 call 方法时位置参数的数目不符合预期'
        )

        self.assertEqual(
            'Invoke',
            client.call.call_args[0][0],
            f'{case_id_in_msg}调用 SCF 的 call 方法时第 1 个位置参数的值不符合预期'
        )

        image = {
            'key': key,
            'filename': key.split('/')[-1]
        }

        self.assertEqual(
            {
                'Namespace': settings.SUBMIT_IMG_TO_OCR_FUNC_NAMESPACE,
                'FunctionName': settings.SUBMIT_IMG_TO_OCR_FUNC_NAME_FORMATTER.format(f_id=key.split('/')[-1][0]),
                'InvocationType': 'Event',
                'ClientContext': json.dumps(image)
            },
            client.call.call_args[0][1],
            f'{case_id_in_msg}调用 SCF 的 call 方法时第 2 个位置参数的值不符合预期'
        )

        self.assertEqual(
            0,
            len(client.call.call_args[1]),
            f'{case_id_in_msg}调用 SCF 的 call 方法时有多余的关键字参数'
        )

    @mock.patch('handler_trigger_submit_img_to_ocr.credential')
    @mock.patch('handler_trigger_submit_img_to_ocr.scf_client')
    def test_main_process(self, mock_scf_client: mock.MagicMock, mock_credential: mock.MagicMock):
        """测试主过程

        测试用例： 4.1.1
        """
        mock_credential.Credential.return_value = 'cred'
        scf_client = mock_scf_client.ScfClient.return_value

        key = f'{settings.IMAGES_ROOT}/2a932c76-869c-49d0-8d16-04f480dbdcf7'
        event = self.generate_test_event(key, 'image/jpeg')

        ret = handler_trigger_submit_img_to_ocr.main_handler(event, None)

        self.check_ret(ret, key, '4.1.1')
        self.check_scf_call(scf_client, key, '4.1.1')

    @mock.patch('handler_trigger_submit_img_to_ocr.credential')
    @mock.patch('handler_trigger_submit_img_to_ocr.scf_client')
    def test_bad_request(self, mock_scf_client: mock.MagicMock, mock_credential: mock.MagicMock):
        """测试非法入参

        测试用例： 4.2.1 - 4.2.5
        """
        mock_credential.Credential.return_value = 'cred'
        scf_client = mock_scf_client.ScfClient.return_value

        test_cases = [
            ('4.2.1', {}),
            ('4.2.2', {'Records': [{}, ]}),
            ('4.2.3', {
                'Records': [{
                    'cos': {
                        'cosBucket': {
                            'name': 'bucket',
                            'appid': settings.QCLOUD_APP_ID,
                        },
                        'cosObject': {
                            'key': f'/{settings.QCLOUD_APP_ID}/bucket/img_path',
                            'meta': {
                                'Content-Type': 'image/jpeg'
                            }
                        }
                    }
                }, ]
            }),
            ('4.2.4', {
                'Records': [{
                    'cos': {
                        'cosBucket': {
                            'name': settings.COS_BUCKET.split('-')[0],
                            'appid': settings.QCLOUD_APP_ID,
                        },
                        'cosObject': {
                            'key': f'/{settings.QCLOUD_APP_ID}/{settings.COS_BUCKET.split("-")[0]}/img_path',
                            'meta': {
                                'Content-Type': 'image/gif'
                            }
                        }
                    }
                }, ]
            }),
            ('4.2.5', {
                'Records': [{
                    'cos': {
                        'cosBucket': {
                            'name': settings.COS_BUCKET.split('-')[0],
                            'appid': settings.QCLOUD_APP_ID,
                        },
                        'cosObject': {
                            'key': f'img_path',
                            'meta': {
                                'Content-Type': 'image/jpeg'
                            }
                        }
                    }
                }, ]
            }),
        ]

        for i, test_case in test_cases:
            ret = handler_trigger_submit_img_to_ocr.main_handler(test_case, None)
            self.assertEqual([], ret, f'[case {i}] 函数返回内容不符合预期')
            self.assertEqual(0, scf_client.call.call_count, f'[case [{i}] SCF 的 call 方法调用次数不符合预期]')

    @mock.patch('handler_trigger_submit_img_to_ocr.credential')
    @mock.patch('handler_trigger_submit_img_to_ocr.scf_client')
    def test_scf_exception(self,  mock_scf_client: mock.MagicMock, mock_credential: mock.MagicMock):
        """测试调用 SCF 发生异常的情况

        测试用例： 4.3
        """
        mock_credential.Credential.return_value = 'cred'
        scf_client = mock_scf_client.ScfClient.return_value
        scf_client.call.side_effect = handler_trigger_submit_img_to_ocr.TencentCloudSDKException()

        key = f'{settings.IMAGES_ROOT}/2a932c76-869c-49d0-8d16-04f480dbdcf7'
        event = self.generate_test_event(key, 'image/jpeg')
        ret = handler_trigger_submit_img_to_ocr.main_handler(event, None)

        self.check_ret(ret, key, '4.3')


class TestSubmitImgToOcr(unittest.TestCase):
    @staticmethod
    def generate_test_event(key: str):
        """生成用于触发函数的事件
        """
        return {
            'key': key,
            'filename': key.split('/')[-1]
        }

    @staticmethod
    def prepare_mock(
            mock_ocr_client: mock.MagicMock,
            mock_credential: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock,
            mock_settings: mock.MagicMock = None
    ) -> Tuple[mock.MagicMock, mock.MagicMock]:
        """准备测试所需 mock 的对象
        """
        mock_cos_config.return_value = 'config'
        cos_client = mock_cos_client.return_value
        cos_client.get_presigned_download_url.return_value = 'auth_url'
        mock_credential.Credential.return_value = 'cred'
        ocr_client = mock_ocr_client.OcrClient.return_value
        ocr_client.GeneralBasicOCR.return_value.to_json_string.return_value = '{"TextDetections": "ocr_result"}'
        ocr_client.GeneralFastOCR.return_value.to_json_string.return_value = '{"TextDetections": "ocr_result"}'
        ocr_client.GeneralEfficientOCR.return_value.to_json_string.return_value = '{"TextDetections": "ocr_result"}'
        ocr_client.GeneralAccurateOCR.return_value.to_json_string.return_value = '{"TextDetections": "ocr_result"}'
        ocr_client.GeneralHandwritingOCR.return_value.to_json_string.return_value = '{"TextDetections": "ocr_result"}'
        ocr_client.TextDetect.return_value.to_json_string.return_value = '{"HasText": true}'

        if mock_settings:
            for attr in dir(settings):
                setattr(mock_settings, attr, getattr(settings, attr))

        return cos_client, ocr_client

    def check_ret(self, ret: Any, key: str, case_id: str = None):
        """检查函数返回结果
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''

        self.assertIsInstance(ret, dict, f'{case_id_in_msg}函数返回结果类型不符合预期')
        self.assertIsInstance(ret, dict, f'{case_id_in_msg}函数返回结果子条目类型不符合预期')
        self.assertEqual(key, ret.get('key'), f'{case_id_in_msg}函数返回结果 key 字段不符合预期')
        self.assertEqual(key.split('/')[-1], ret.get('filename'), f'{case_id_in_msg}函数返回结果 filename 字段不符合预期')
        self.assertEqual('auth_url', ret.get('auth_url'), f'{case_id_in_msg}函数返回结果 auth_url 字段不符合预期')
        self.assertEqual('ocr_result', ret.get('result'), f'{case_id_in_msg}函数返回结果 result 字段不符合预期')
        self.assertEqual(4, len(ret), f'{case_id_in_msg}函数返回结果子条目键数目不符合预期')

    def check_cos_get_presigned_download_url(self, client: mock.MagicMock, key: str, case_id: str = None):
        """检查是否正确使用 COS 的 get_presigned_download_url 方法
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''

        self.assertEqual(
            0,
            len(client.get_presigned_download_url.call_args[0]),
            f'{case_id_in_msg}调用 COS 的 get_presigned_download_url 方法时含预期外的位置参数'
        )
        self.assertEqual(
            settings.COS_BUCKET,
            client.get_presigned_download_url.call_args[1].get('Bucket'),
            f'{case_id_in_msg}调用 COS 的 get_presigned_download_url 方法时 Bucket 参数不符合预期'
        )
        self.assertEqual(
            key,
            client.get_presigned_download_url.call_args[1].get('Key'),
            f'{case_id_in_msg}调用 COS 的 get_presigned_download_url 方法时 Key 参数不符合预期'
        )
        self.assertEqual(
            300,
            client.get_presigned_download_url.call_args[1].get('Expired'),
            f'{case_id_in_msg}调用 COS 的 get_presigned_download_url 方法时 Expired 参数不符合预期'
        )
        self.assertEqual(
            3,
            len(client.get_presigned_download_url.call_args[1]),
            f'{case_id_in_msg}调用 COS 的 get_presigned_download_url 方法时关键字参数的数目不符合预期'
        )

    def check_cos_put_object(self, client: mock.MagicMock, key: str, case_id: str = None):
        """检查是否正确使用 COS 的 put_object 方法
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''

        self.assertEqual(
            0,
            len(client.put_object.call_args[0]),
            f'{case_id_in_msg}上传 OCR 结果时含预期外的位置参数'
        )
        self.assertEqual(
            settings.COS_BUCKET,
            client.put_object.call_args[1].get('Bucket'),
            f'{case_id_in_msg}上传 OCR 结果时 Bucket 参数与预期不符'
        )
        self.assertEqual(
            f'{settings.RESULTS_ROOT}/{key.split("/")[-1]}.json',
            client.put_object.call_args[1].get('Key'),
            f'{case_id_in_msg}上传 OCR 结果时 Key 参数与预期不符'
        )
        self.assertEqual(
            'application/json',
            client.put_object.call_args[1].get('ContentType'),
            f'{case_id_in_msg}上传 OCR 结果时 ContentType 参数与预期不符'
        )
        self.assertEqual(
            '"ocr_result"',
            client.put_object.call_args[1].get('Body'),
            f'{case_id_in_msg}上传 OCR 结果时 Body 参数与预期不符'
        )
        self.assertEqual(
            4,
            len(client.put_object.call_args[1]),
            f'{case_id_in_msg}上传 OCR 结果时关键字参数的数目不符合预期'
        )

    def check_ocr_submit(self, client: mock.MagicMock, mock_settings: mock.MagicMock, case_id: str = None):
        """检查 OCR 服务是否正确调用
        """
        case_id_in_msg = f'[case {case_id}] ' if case_id else ''

        if mock_settings.OCR_DETECT_FIRST:
            self.assertIsInstance(
                client.TextDetect.call_args[0][0],
                handler_submit_img_to_ocr.ocr_models.TextDetectRequest,
                f'{case_id_in_msg}调用 OCR TextDetect 方法时参数类型不符合预期'
            )
            self.assertEqual(
                'auth_url',
                client.TextDetect.call_args[0][0].ImageUrl,
                f'{case_id_in_msg}调用 OCR TextDetect 方法时参数的 ImageUrl 字段不符合预期'
            )

        if mock_settings.OCR_TYPE == 'basic':
            self.assertIsInstance(
                client.GeneralBasicOCR.call_args[0][0],
                handler_submit_img_to_ocr.ocr_models.GeneralBasicOCRRequest,
                f'{case_id_in_msg}调用 OCR GeneralBasicOCR 方法时参数类型不符合预期'
            )
            self.assertEqual(
                'auth_url',
                client.GeneralBasicOCR.call_args[0][0].ImageUrl,
                f'{case_id_in_msg}调用 OCR GeneralBasicOCR 方法时参数的 ImageUrl 字段不符合预期'
            )
            self.assertEqual(
                settings.GENERAL_BASIC_OCR_LANGUAGE,
                client.GeneralBasicOCR.call_args[0][0].LanguageType,
                f'{case_id_in_msg}调用 OCR GeneralBasicOCR 方法时参数的 LanguageType 字段不符合预期'
            )
        elif mock_settings.OCR_TYPE == 'fast':
            self.assertIsInstance(
                client.GeneralFastOCR.call_args[0][0],
                handler_submit_img_to_ocr.ocr_models.GeneralFastOCRRequest,
                f'{case_id_in_msg}调用 OCR GeneralFastOCR 方法时参数类型不符合预期'
            )
            self.assertEqual(
                'auth_url',
                client.GeneralFastOCR.call_args[0][0].ImageUrl,
                f'{case_id_in_msg}调用 OCR GeneralFastOCR 方法时参数的 ImageUrl 字段不符合预期'
            )
        elif mock_settings.OCR_TYPE == 'efficient':
            self.assertIsInstance(
                client.GeneralEfficientOCR.call_args[0][0],
                handler_submit_img_to_ocr.ocr_models.GeneralEfficientOCRRequest,
                f'{case_id_in_msg}调用 OCR GeneralEfficientOCR 方法时参数类型不符合预期'
            )
            self.assertEqual(
                'auth_url',
                client.GeneralEfficientOCR.call_args[0][0].ImageUrl,
                f'{case_id_in_msg}调用 OCR GeneralEfficientOCR 方法时参数的 ImageUrl 字段不符合预期'
            )
        elif mock_settings.OCR_TYPE == 'accurate':
            self.assertIsInstance(
                client.GeneralAccurateOCR.call_args[0][0],
                handler_submit_img_to_ocr.ocr_models.GeneralAccurateOCRRequest,
                f'{case_id_in_msg}调用 OCR GeneralAccurateOCR 方法时参数类型不符合预期'
            )
            self.assertEqual(
                'auth_url',
                client.GeneralAccurateOCR.call_args[0][0].ImageUrl,
                f'{case_id_in_msg}调用 OCR GeneralAccurateOCR 方法时参数的 ImageUrl 字段不符合预期'
            )
        elif mock_settings.OCR_TYPE == 'handwriting':
            self.assertIsInstance(
                client.GeneralHandwritingOCR.call_args[0][0],
                handler_submit_img_to_ocr.ocr_models.GeneralHandwritingOCRRequest,
                f'{case_id_in_msg}调用 OCR GeneralHandwritingOCR 方法时参数类型不符合预期'
            )
            self.assertEqual(
                'auth_url',
                client.GeneralHandwritingOCR.call_args[0][0].ImageUrl,
                f'{case_id_in_msg}调用 OCR GeneralHandwritingOCR 方法时参数的 ImageUrl 字段不符合预期'
            )

    @mock.patch('handler_submit_img_to_ocr.settings')
    @mock.patch('handler_submit_img_to_ocr.CosConfig')
    @mock.patch('handler_submit_img_to_ocr.CosS3Client')
    @mock.patch('handler_submit_img_to_ocr.credential')
    @mock.patch('handler_submit_img_to_ocr.ocr_client')
    def test_main_process(
            self,
            mock_ocr_client: mock.MagicMock,
            mock_credential: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock,
            mock_settings: mock.MagicMock
    ):
        """测试主流程

        测试用例： 2.1.1 - 2.1.10
        """
        cos_client, ocr_client = self.prepare_mock(
            mock_ocr_client,
            mock_credential,
            mock_cos_client,
            mock_cos_config,
            mock_settings
        )

        key = f'{settings.IMAGES_ROOT}/2a932c76-869c-49d0-8d16-04f480dbdcf7'

        event = self.generate_test_event(key)

        test_cases = [
            ('2.1.1', 'basic', False),
            ('2.1.2', 'fast', False),
            ('2.1.3', 'efficient', False),
            ('2.1.4', 'accurate', False),
            ('2.1.5', 'handwriting', False),
            ('2.1.6', 'basic', True),
            ('2.1.7', 'fast', True),
            ('2.1.8', 'efficient', True),
            ('2.1.9', 'accurate', True),
            ('2.1.10', 'handwriting', True),
        ]

        for i, test_case in enumerate(test_cases):
            mock_settings.OCR_TYPE = test_case[1]
            mock_settings.OCR_DETECT_FIRST = test_case[2]

            ret = handler_submit_img_to_ocr.main_handler(event, None)
            self.check_ret(ret, key, test_case[0])
            check_cos_config(self, mock_cos_config, 2 * i + 2, test_case[0])
            check_cos_client(self, mock_cos_client, 2 * i + 2, test_case[0])
            self.check_cos_get_presigned_download_url(cos_client, key, test_case[0])
            self.check_cos_put_object(cos_client, key, test_case[0])
            self.check_ocr_submit(ocr_client, mock_settings, test_case[0])

    @mock.patch('handler_submit_img_to_ocr.CosConfig')
    @mock.patch('handler_submit_img_to_ocr.CosS3Client')
    @mock.patch('handler_submit_img_to_ocr.credential')
    @mock.patch('handler_submit_img_to_ocr.ocr_client')
    def test_cos_exception(
            self,
            mock_ocr_client: mock.MagicMock,
            mock_credential: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock
    ):
        """测试函数运行过程中 COS 抛出异常的情况

        测试用例： 2.2.1 - 2.2.4
        """
        cos_client, ocr_client = self.prepare_mock(mock_ocr_client, mock_credential, mock_cos_client, mock_cos_config)
        cos_client.get_presigned_download_url.side_effect = [
            handler_submit_img_to_ocr.CosServiceError('GET', 'message', 'status_code'),
            handler_submit_img_to_ocr.CosClientError('message'),
            'auth_url',
            'auth_url'
        ]
        cos_client.put_object.side_effect = [
            handler_submit_img_to_ocr.CosServiceError('GET', 'message', 'status_code'),
            handler_submit_img_to_ocr.CosClientError('message'),
        ]

        key = f'{settings.IMAGES_ROOT}/2a932c76-869c-49d0-8d16-04f480dbdcf7'
        event = self.generate_test_event(key)

        test_cases = [
            ('2.2.1', 1, 0),
            ('2.2.2', 2, 0),
            ('2.2.3', 4, 1),
            ('2.2.4', 6, 2)
        ]

        for test_case in test_cases:
            ret = handler_submit_img_to_ocr.main_handler(event, None)
            self.assertEqual(None, ret, f'[case {test_case[0]}] 函数返回内容不符合预期')
            check_cos_config(self, mock_cos_config, test_case[1], test_case[0])
            check_cos_client(self, mock_cos_client, test_case[1], test_case[0])
            self.assertEqual(
                test_case[2],
                mock_ocr_client.OcrClient.call_count,
                f'[case {test_case[0]}] 初始化 OCR 客户端的次数不符合预期'
            )

    @mock.patch('handler_submit_img_to_ocr.CosConfig')
    @mock.patch('handler_submit_img_to_ocr.CosS3Client')
    def test_upload_bad_result(self, mock_cos_client, mock_cos_config):
        """测试上传格式错误的结果

        测试用例： 2.3
        """
        self.assertRaises(ValueError, handler_submit_img_to_ocr.upload_result_to_cos, {1, 2, 3}, 'filename.json')

    @mock.patch('handler_submit_img_to_ocr.settings')
    @mock.patch('handler_submit_img_to_ocr.CosConfig')
    @mock.patch('handler_submit_img_to_ocr.CosS3Client')
    @mock.patch('handler_submit_img_to_ocr.credential')
    @mock.patch('handler_submit_img_to_ocr.ocr_client')
    def test_no_text(
            self,
            mock_ocr_client: mock.MagicMock,
            mock_credential: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock,
            mock_settings: mock.MagicMock
    ):
        """测试没有识别到文字的情况

        测试用例： 2.4.1 - 2.4.10
        """
        cos_client, ocr_client = self.prepare_mock(
            mock_ocr_client,
            mock_credential,
            mock_cos_client,
            mock_cos_config,
            mock_settings
        )

        no_text_error = handler_submit_img_to_ocr.TencentCloudSDKException(code='FailedOperation.ImageNoText')
        ocr_client.GeneralBasicOCR.side_effect = no_text_error
        ocr_client.GeneralFastOCR.side_effect = no_text_error
        ocr_client.GeneralEfficientOCR.side_effect = no_text_error
        ocr_client.GeneralAccurateOCR.side_effect = no_text_error
        ocr_client.GeneralHandwritingOCR.side_effect = no_text_error
        ocr_client.TextDetect.return_value.to_json_string.return_value = '{"HasText": false}'

        test_cases = [
            ('2.4.1', 'basic', False),
            ('2.4.2', 'fast', False),
            ('2.4.3', 'efficient', False),
            ('2.4.4', 'accurate', False),
            ('2.4.5', 'handwriting', False),
            ('2.4.6', 'basic', True),
            ('2.4.7', 'fast', True),
            ('2.4.8', 'efficient', True),
            ('2.4.9', 'accurate', True),
            ('2.4.10', 'handwriting', True),
        ]

        key = f'{settings.IMAGES_ROOT}/2a932c76-869c-49d0-8d16-04f480dbdcf7'
        event = self.generate_test_event(key)

        for test_case in test_cases:
            mock_settings.OCR_TYPE = test_case[1]
            mock_settings.OCR_DETECT_FIRST = test_case[2]

            ret = handler_submit_img_to_ocr.main_handler(event, None)
            self.assertEqual([], ret.get('result'), f'[case {test_case[0]}] 函数返回内容不符合预期')

    @mock.patch('handler_submit_img_to_ocr.CosConfig')
    @mock.patch('handler_submit_img_to_ocr.CosS3Client')
    @mock.patch('handler_submit_img_to_ocr.credential')
    @mock.patch('handler_submit_img_to_ocr.ocr_client')
    def test_init_ocr_service_error(
            self,
            mock_ocr_client: mock.MagicMock,
            mock_credential: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock,
    ):
        """测试初始化 handler_submit_img_to_ocr.OcrService 错误的情况

        测试用例： 2.5.1 - 2.5.3
        """
        _, ocr_client = self.prepare_mock(mock_ocr_client, mock_credential, mock_cos_client, mock_cos_config)

        # 2.6.1
        self.assertRaises(ValueError, handler_submit_img_to_ocr.OcrService, 'ocr_type')

        client = handler_submit_img_to_ocr.OcrService('basic')
        client.ocr_type = None

        # 2.6.2
        self.assertRaises(ValueError, client.submit, 'auth_url')

        # 2.6.3
        self.assertRaises(ValueError, client.get_request, 'auth_url')

    @mock.patch('handler_submit_img_to_ocr.CosConfig')
    @mock.patch('handler_submit_img_to_ocr.CosS3Client')
    @mock.patch('handler_submit_img_to_ocr.credential')
    @mock.patch('handler_submit_img_to_ocr.ocr_client')
    def test_ocr_exception(
            self,
            mock_ocr_client: mock.MagicMock,
            mock_credential: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock
    ):
        """测试 OCR 服务发生异常的情况

        测试用例： 2.6
        """
        cos_client, ocr_client = self.prepare_mock(mock_ocr_client, mock_credential, mock_cos_client, mock_cos_config)

        ocr_error = handler_submit_img_to_ocr.TencentCloudSDKException()
        ocr_client.GeneralBasicOCR.side_effect = ocr_error
        ocr_client.GeneralFastOCR.side_effect = ocr_error
        ocr_client.GeneralEfficientOCR.side_effect = ocr_error
        ocr_client.GeneralAccurateOCR.side_effect = ocr_error
        ocr_client.GeneralHandwritingOCR.side_effect = ocr_error
        ocr_client.TextDetect.side_effect = ocr_error

        key = f'{settings.IMAGES_ROOT}/2a932c76-869c-49d0-8d16-04f480dbdcf7'
        event = self.generate_test_event(key)
        ret = handler_submit_img_to_ocr.main_handler(event, None)

        self.assertEqual(None, ret, f'[case 2.6] 函数返回内容不符合预期')
        self.assertEqual(0, cos_client.put_object.call_count, f'[case 2.6] 发生了预期外的 COS 上传')


class TestCheckResult(unittest.TestCase):
    @staticmethod
    def get_cos_service_exception(code: str = 'error'):
        """生成 CosServiceError
        """
        return handler_check_result.CosServiceError(
            'GET',
            (
                '<Error>'
                f'    <Code>{code}</Code>'
                '    <Message>string</Message>'
                '    <Resource>string</Resource>'
                '    <RequestId>string</RequestId>'
                '    <TraceId>string</TraceId>'
                '</Error>'
            ),
            404
        )

    def check_ret(self, ret, status_code: int = 200, body: Any = None, case_id: str = None):
        """检查函数响应
        """
        case_id_in_msg = f'测试用例: {case_id} ，' if case_id else ''
        self.assertIsInstance(ret, dict, f'{case_id_in_msg}主函数响应格式与预期不符')
        self.assertEqual(status_code, ret.get('statusCode'), f'{case_id_in_msg}主函数响应状态码与预期不符')
        self.assertFalse(ret.get('isBase64Encoded'), f'{case_id_in_msg}主函数响应 isBase64Encoded 字段与预期不符')

        if status_code == 200:
            self.assertEqual(
                'application/json',
                ret.get('headers', {}).get('Content-Type'),
                f'{case_id_in_msg}主函数响应 Content-Type 与预期不符'
            )

            ret_body = json.loads(ret.get('body'))
            self.assertEqual(body, ret_body, f'{case_id_in_msg}主函数响应内容与预期不符')

    def check_head_object(self, client: mock.MagicMock, img_id: str, case_id: str = None):
        """检查是否正确使用 COS 的 head_object 方法
        """
        case_id_in_msg = f'测试用例: {case_id} ，' if case_id else ''
        self.assertEqual(1, client.head_object.call_count, f'{case_id_in_msg}对 COS 作 head_object 请求的次数不符合预期')
        self.assertEqual(
            0,
            len(client.head_object.call_args[0]),
            f'{case_id_in_msg}对 COS 作 head_object 请求时有预期外的位置参数'
        )
        self.assertEqual(
            settings.COS_BUCKET,
            client.head_object.call_args[1].get('Bucket'),
            f'{case_id_in_msg}对 COS 作 head_object 请求时 Bucket 参数不符合预期'
        )
        self.assertEqual(
            f'{settings.IMAGES_ROOT}/{img_id}',
            client.head_object.call_args[1].get('Key'),
            f'{case_id_in_msg}对 COS 作 head_object 请求时 Key 参数不符合预期'
        )
        self.assertEqual(
            2,
            len(client.head_object.call_args[1]),
            f'{case_id_in_msg}对 COS 作 head_object 请求时位置参数的数目不符合预期'
        )

    def check_get_object(self, mock_client: mock.MagicMock, img_id: str, call_count: int = -1, case_id: str = None):
        """检查是否正确使用 COS 的 get_object 方法
        """
        case_id_in_msg = f'测试用例: {case_id} ，' if case_id else ''
        if call_count >= 0:
            self.assertEqual(
                call_count,
                mock_client.get_object.call_count,
                f'{case_id_in_msg}对 COS 作 get_object 请求的次数不符合预期'
            )

        for call_args in mock_client.get_object.call_args_list:
            self.assertEqual(0, len(call_args[0]), f'{case_id_in_msg}对 COS 作 get_object 请求时有预期外的位置参数')
            self.assertEqual(
                settings.COS_BUCKET,
                call_args[1].get('Bucket'),
                f'{case_id_in_msg}对 COS 作 get_object 请求时 Bucket 参数不符合预期'
            )
            self.assertEqual(
                f'{settings.RESULTS_ROOT}/{img_id}.json',
                call_args[1].get('Key'),
                f'{case_id_in_msg}对 COS 作 get_object 请求时 Key 参数不符合预期'
            )
            self.assertEqual(2, len(call_args[1]), f'{case_id_in_msg}对 COS 作 get_object 请求时位置参数的数目不符合预期')

    def check_sleep(self, mock_time: mock.MagicMock, call_count: int = -1, case_id: str = None):
        """检查是否正确调用 time.sleep 方法
        """
        case_id_in_msg = f'测试用例: {case_id} ，' if case_id else ''
        if call_count >= 0:
            self.assertEqual(
                call_count,
                mock_time.sleep.call_count,
                f'{case_id_in_msg}调用 time.sleep 的次数不符合预期'
            )

        for call_args in mock_time.sleep.call_args_list:
            self.assertEqual(1, len(call_args[0]), f'{case_id_in_msg}调用 time.sleep 时位置参数数目不符合预期')
            self.assertEqual(
                settings.CHECK_RESULT_INTERVAL_TIME,
                call_args[0][0],
                f'{case_id_in_msg}调用 time.sleep 时，睡眠时长不符合预期'
            )
            self.assertEqual(0, len(call_args[1]), f'{case_id_in_msg}调用 time.sleep 时有预期外的关键字参数')

    @mock.patch('handler_check_result.CosConfig')
    @mock.patch('handler_check_result.CosS3Client')
    @mock.patch('handler_check_result.time')
    def test_main_process(
            self,
            mock_time: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock
    ):
        """测试主流程

        测试用例： 3.1
        """
        mock_cos_config.return_value = 'config'
        mock_time.time.side_effect = range(30)
        ocr_result = mock.MagicMock()
        ocr_result['Body'].get_raw_stream.return_value = BytesIO(b'["ocr_result1", "ocr_result2"]')
        client = mock_cos_client.return_value
        client.get_object.side_effect = [
            self.get_cos_service_exception('NoSuchKey'),
            self.get_cos_service_exception('NoSuchKey'),
            ocr_result
        ]

        img_id = '12345678-1234-1234-1234-1234567890ab'
        ret = handler_check_result.main_handler({'queryString': {'img_id': img_id}}, None)

        self.check_ret(ret, 200, ["ocr_result1", "ocr_result2"], '3.1')
        check_cos_config(self, mock_cos_config, 2, '3.1')
        check_cos_client(self, mock_cos_client, 2, '3.1')
        self.check_head_object(client, img_id, '3.1')
        self.check_get_object(client, img_id, 3, '3.1')
        self.check_sleep(mock_time, 2, '3.1')

    @mock.patch('handler_check_result.CosConfig')
    @mock.patch('handler_check_result.CosS3Client')
    def test_bad_request(self, mock_cos_client: mock.MagicMock, mock_cos_config: mock.MagicMock):
        """测试非法请求

        测试用例：
        3.2 无 img_id
        3.3 img_id 带有 '/'
        """

        ret = handler_check_result.main_handler({'queryString': {}}, None)
        self.check_ret(ret, 400, None, '3.2')

        ret = handler_check_result.main_handler(
            {'queryString': {'img_id': 'hhh/12345678-1234-1234-1234-1234567890ab'}},
            None
        )
        self.check_ret(ret, 400, None, '3.3')

        check_cos_config(self, mock_cos_config, 0, '3.x')
        check_cos_client(self, mock_cos_client, 0, '3.x')

    @mock.patch('handler_check_result.CosConfig')
    @mock.patch('handler_check_result.CosS3Client')
    def test_img_not_found(self, mock_cos_client: mock.MagicMock, mock_cos_config: mock.MagicMock):
        """测试图片不存在

        测试用例： 3.4
        """
        mock_cos_config.return_value = 'config'
        client = mock_cos_client.return_value
        client.head_object.side_effect = self.get_cos_service_exception('NoSuchResource')

        img_id = '12345678-1234-1234-1234-1234567890ab'
        ret = handler_check_result.main_handler(
            {'queryString': {'img_id': img_id}},
            None
        )
        self.check_ret(ret, 404, None, '3.4')
        check_cos_config(self, mock_cos_config, 1, '3.4')
        check_cos_client(self, mock_cos_client, 1, '3.4')
        self.check_head_object(client, img_id, '3.4')
        self.check_get_object(client, img_id, 0, '3.4')

    @mock.patch('handler_check_result.CosConfig')
    @mock.patch('handler_check_result.CosS3Client')
    @mock.patch('handler_check_result.time')
    def test_timeout(
            self,
            mock_time: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock
    ):
        """测试轮询 COS 超时

        测试用例： 3.5
        """
        mock_cos_config.return_value = 'config'
        mock_time.time.side_effect = range(30)
        client = mock_cos_client.return_value
        client.get_object.side_effect = [
            self.get_cos_service_exception('NoSuchKey')
            for _ in range(30)
        ]

        img_id = '12345678-1234-1234-1234-1234567890ab'
        ret = handler_check_result.main_handler({'queryString': {'img_id': img_id}}, None)

        self.check_ret(ret, 200, None, '3.5')
        check_cos_config(self, mock_cos_config, 2, '3.5')
        check_cos_client(self, mock_cos_client, 2, '3.5')
        self.check_head_object(client, img_id, '3.5')
        self.check_get_object(client, img_id, 11, '3.5')
        self.check_sleep(mock_time, 11, '3.5')

    @mock.patch('handler_check_result.CosConfig')
    @mock.patch('handler_check_result.CosS3Client')
    @mock.patch('handler_check_result.time')
    def test_cos_exception(
            self,
            mock_time: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock
    ):
        """测试 COS 返回异常的情况

        测试用例： 3.6 - 3.9
        """
        mock_cos_config.return_value = 'config'
        mock_time.time.side_effect = range(30)
        ocr_result = mock.MagicMock()
        ocr_result['Body'].get_raw_stream.return_value = BytesIO(b'["ocr_result1", "ocr_result2"]')
        client = mock_cos_client.return_value
        client.head_object.side_effect = [
            handler_check_result.CosServiceError('GET', 'message', 'status_code'),
            handler_check_result.CosClientError('message'),
            'head',
            'head'
        ]
        client.get_object.side_effect = [
            handler_check_result.CosServiceError('GET', 'message', 'status_code'),
            handler_check_result.CosClientError('message'),
        ]

        img_id = '12345678-1234-1234-1234-1234567890ab'

        ret = handler_check_result.main_handler({'queryString': {'img_id': img_id}}, None)
        self.check_ret(ret, 500, None, '3.6')

        ret = handler_check_result.main_handler({'queryString': {'img_id': img_id}}, None)
        self.check_ret(ret, 500, None, '3.7')

        self.check_get_object(client, img_id, 0, '3.6 - 3.7')

        ret = handler_check_result.main_handler({'queryString': {'img_id': img_id}}, None)
        self.check_ret(ret, 500, None, '3.8')

        ret = handler_check_result.main_handler({'queryString': {'img_id': img_id}}, None)
        self.check_ret(ret, 500, None, '3.9')

        check_cos_config(self, mock_cos_config, 6, '3.x')
        check_cos_client(self, mock_cos_client, 6, '3.x')
        self.check_sleep(mock_time, 0, '3.x')

    @mock.patch('handler_check_result.CosConfig')
    @mock.patch('handler_check_result.CosS3Client')
    @mock.patch('handler_check_result.time')
    def test_bad_ocr_result(
            self,
            mock_time: mock.MagicMock,
            mock_cos_client: mock.MagicMock,
            mock_cos_config: mock.MagicMock
    ):
        """测试 OCR 返回非法数据

        测试用例： 3.10
        """
        mock_cos_config.return_value = 'config'
        mock_time.time.side_effect = range(30)
        ocr_result = mock.MagicMock()
        ocr_result['Body'].get_raw_stream.return_value = BytesIO(b'test')
        client = mock_cos_client.return_value
        client.get_object.return_value = ocr_result

        img_id = '12345678-1234-1234-1234-1234567890ab'
        ret = handler_check_result.main_handler({'queryString': {'img_id': img_id}}, None)

        self.check_ret(ret, 500, None, '3.10')
        check_cos_config(self, mock_cos_config, 2, '3.10')
        check_cos_client(self, mock_cos_client, 2, '3.10')
        self.check_head_object(client, img_id, '3.10')
        self.check_get_object(client, img_id, 1, '3.10')
        self.check_sleep(mock_time, 0, '3.0')

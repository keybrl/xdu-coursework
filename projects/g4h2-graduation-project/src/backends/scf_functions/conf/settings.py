from .secrets import QCLOUD_APP_ID, QCLOUD_SECRET_ID, QCLOUD_SECRET_KEY

QCLOUD_REGION = 'ap-guangzhou'

COS_BUCKET = f'keybrlocr-{QCLOUD_APP_ID}'
IMAGES_ROOT = 'release/images'
IMAGE_MAX_SIZE = 2.25  # 2.25 / 0.75 = 3 ，Base64 编码后小于 3MB
RESULT_URL_FORMATTER = 'https://ocr.keyboardluo.com/release/check_result_{f_id}?img_id={id}'

IMG_DIRECT_URL_FORMATTER = 'https://cdn.ocr.keyboardluo.com/release/images/{id}'
RESULT_DIRECT_URL_FORMATTER = 'https://cdn.ocr.keyboardluo.com/release/results/{id}.json'

SUBMIT_IMG_TO_OCR_FUNC_NAME_FORMATTER = 'submit_img_to_ocr_{f_id}'
SUBMIT_IMG_TO_OCR_FUNC_NAMESPACE = 'keybrlocr'

# OCR 结果存放在 COS 的根路径
RESULTS_ROOT = 'release/results'


# OCR 类型，可选值：
# 'basic':       通用印刷体识别
# 'fast':        通用印刷体识别（高速版）
# 'efficient':   通用印刷体识别（精简版）
# 'accurate':    通用印刷体识别（高精度版）
# 'handwriting': 通用手写体识别
OCR_TYPE = 'accurate'

# 调用文本识别接口前是否先进行快速文本检测
OCR_DETECT_FIRST = False

# 通用文字识别时选择的语言
# 可选值：
# zh\auto\jap\kor\
# spa\fre\ger\por\
# vie\may\rus\ita\
# hol\swe\fin\dan\
# nor\hun\tha\lat
# 可选值分别表示：
# 中英文混合、自动识别、日语、韩语、
# 西班牙语、法语、德语、葡萄牙语、
# 越南语、马来语、俄语、意大利语、
# 荷兰语、瑞典语、芬兰语、丹麦语、
# 挪威语、匈牙利语、泰语、拉丁语系。
GENERAL_BASIC_OCR_LANGUAGE = 'auto'


# 查询 OCR 结果的超时时间（s）
# 该函数总共超时间 17s ，给前面操作时间留了 2s
# <= 0 的值都表示超时时间为无穷小（趋于 0 ），不是无穷大，在这种情况下只进行一次请求
CHECK_RESULT_TIMEOUT = 20

# 查询 OCR 结果时轮询间隔时间（s）
CHECK_RESULT_INTERVAL_TIME = 0.5

try:
    from .dev import *
except ImportError:
    pass

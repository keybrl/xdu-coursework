# 获取车辆轨迹的API

**提示：压缩包中的 `cc-get_places.postman_collection.json` 是Postman导出的示例，可以参考理解**

URL：`http://hostname/get_places`

参数

- `eid` ，电子车牌，字符串
- `start_time` ，开始时间戳，精确到秒
- `end_time` ，结束时间戳，精确到秒

参数传递方式

1. GET方法的请求，参数作为请求参数
2. POST方法的请求，参数以form-data格式带在body里
3. POST方法的请求，参数以JSON格式带在body里，header指定 `Content-Type: application/json`

示例：（部分请求头被省略）

```http request
GET /get_places?eid=33041100005310&amp; start_time=1496289399&amp; end_time=1496290344 HTTP/1.1
Host: cc.keybrl.com


```

```http request
POST /get_places HTTP/1.1
Host: cc.keybrl.com
Content-Type: application/json

{
	"eid": "33041100005310",
	"start_time": "1496289399",
	"end_time": "1496290344"
}
```

```http request
POST /get_places HTTP/1.1
Host: cc.keybrl.com
content-length: 408
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW


Content-Disposition: form-data; name="eid"

33041100005310
------WebKitFormBoundary7MA4YWxkTrZu0gW--,
Content-Disposition: form-data; name="eid"

33041100005310
------WebKitFormBoundary7MA4YWxkTrZu0gW--
Content-Disposition: form-data; name="start_time"

1496289399
------WebKitFormBoundary7MA4YWxkTrZu0gW--,
Content-Disposition: form-data; name="eid"

33041100005310
------WebKitFormBoundary7MA4YWxkTrZu0gW--
Content-Disposition: form-data; name="start_time"

1496289399
------WebKitFormBoundary7MA4YWxkTrZu0gW--
Content-Disposition: form-data; name="end_time"

1496290344
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

响应

成功响应

Status: 200 OK

Headers:

```text
Content-Type: application/json
```

Body:

JSON，UTF-8编码

原始数据示例如下

```text
[{"eid": "33041100005310", "place_name": "\u4f73\u6e90\u5e7f\u573a1", "longitude": "120.624102", "latitude": "30.801957", "place_id": "216", "timestamp": "1496289399"}, {"eid": "33041100005310", "place_name": "\u4f73\u6e90\u5e7f\u573a1", "longitude": "120.624102", "latitude": "30.801957", "place_id": "216", "timestamp": "1496289797"}, {"eid": "33041100005310", "place_name": "\u8679\u6865\u4e1c\u5317\u5927\u8857\u8def\u53e3", "longitude": "120.624039", "latitude": "30.802865", "place_id": "218", "timestamp": "1496289840"}, {"eid": "33041100005310", "place_name": "\u4f73\u6e90\u5e7f\u573a1", "longitude": "120.624102", "latitude": "30.801957", "place_id": "216", "timestamp": "1496289940"}, {"eid": "33041100005310", "place_name": "\u4f73\u6e90\u5e7f\u573a1", "longitude": "120.624102", "latitude": "30.801957", "place_id": "216", "timestamp": "1496290344"}]
```

按JSON格式化后数据示例如下

- `eid` ，电子车牌
- `place_name` ，地点名，含有中文，utf-8编码
- `place_id` ，地点id，地点的唯一标识
- `longitude` ，经度
- `latitude` ，纬度

```json
[
    {
        "eid": "33041100005310",
        "place_name": "佳源广场1",
        "longitude": "120.624102",
        "latitude": "30.801957",
        "place_id": "216",
        "timestamp": "1496289399"
    },
    {
        "eid": "33041100005310",
        "place_name": "佳源广场1",
        "longitude": "120.624102",
        "latitude": "30.801957",
        "place_id": "216",
        "timestamp": "1496289797"
    },
    {
        "eid": "33041100005310",
        "place_name": "虹桥东北大街路口",
        "longitude": "120.624039",
        "latitude": "30.802865",
        "place_id": "218",
        "timestamp": "1496289840"
    },
    {
        "eid": "33041100005310",
        "place_name": "佳源广场1",
        "longitude": "120.624102",
        "latitude": "30.801957",
        "place_id": "216",
        "timestamp": "1496289940"
    },
    {
        "eid": "33041100005310",
        "place_name": "佳源广场1",
        "longitude": "120.624102",
        "latitude": "30.801957",
        "place_id": "216",
        "timestamp": "1496290344"
    }
]
```

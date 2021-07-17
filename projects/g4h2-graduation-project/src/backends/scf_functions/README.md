# SCF 函数

## helloworld

一个测试函数

默认情况下返回 `'Hello World!'` ，如果请求参数 `target` 为 `event` 或 `context` ，则返回对应入参 `event` 或 `context`

## upload_img_to_cos

该函数用于将图片上传至 COS

其 API 网关触发器接收 `Content-Type: application/json` 的 POST 请求，请求 body 内容示例如下

```json
{
  "type": "jpeg | png",
  "image": "Base64 编码的图片"
}
```

响应格式为

```json
{
  "id": "图片唯一标识，一串 uuid ，用于后续请求",
  "result": "查询结果的 URL"
}
```

# HBase部分代码说明

该目录下文件及其说明如下：

- `config.json` ，配置文件，以下各应用的配置
- `hbase_manager.py` ，HBase操作类，封装了该应用所需的HBase操作
- `get_places.py` ，获取车辆轨迹
- `get_places_web_api.py` ，获取车辆轨迹（Web API）
- `redis_to_hbase.py` ，使用消息发布订阅模式从Redis往HBase转移数据
- `hbase_to_sqlite.py` ，将HBase中部分数据备份到本地SQLite数据库
- `sqlite_to_hbase.py` ，从本地SQLite数据库部分恢复HBase的数据

该项目都是通过HBase的REST接口操作HBase，需要Python安装有第三方库requests，HBase开启REST服务。

`get_places_web_api.py` 使用了第三方库flask和flask-cors用于提供Web服务和Web接口跨域访问功能。
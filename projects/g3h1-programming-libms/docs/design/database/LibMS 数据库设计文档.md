# LibMS 数据库设计文档

> 20181030 KeybrL

## 数据库模式设计

**E-R图** 如下

![E-R 图](LibMS-DB-ER.png)

建立各表的 **SQL**

```sql
CREATE TABLE book_info (
    isbn CHAR(20) PRIMARY KEY NOT NULL,
    name VARCHAR(256) NOT NULL,
    author VARCHAR(256) NOT NULL,
    price NUMERIC(16, 2) NOT NULL,
    category CHAR(16) NOT NULL,
);

CREATE TABLE user (
    id BIGINT PRIMARY KEY NOT NULL,
    password CHAR(32) NOT NULL,
    type INTEGER NOT NULL,
    name VARCHAR(256) NOT NULL,
    phone_num CHAR(32) NOT NULL,
    email VARCHAR(256) NOT NULL,
    unit VARCHAR(256) NOT NULL,
    address VARCHAR(256) NOT NULL
);

CREATE TABLE book (
    id CHAR(32) PRIMARY KEY NOT NULL,
    isbn CHAR(20) NOT NULL,
    holder BIGINT,
    FOREIGN KEY (isbn) REFERENCES book_info(isbn),
    FOREIGN KEY (holder) REFERENCES user(id)
);

CREATE TABLE borrow_log (
    seq BIGINT PRIMARY KEY AUTO INCREMENT NOT NULL,
    date_time DATETIME NOT NULL,
    type INTEGER NOT NULL,
    book_id CHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);
```

## 详细说明

该数据库共有4个表，以下是每个表及其属性的描述和格式说明

### book_info

书本详细信息。由于书本信息仅依赖于ISBN，与具体是哪一本书无关，这部分信息如果加入到 `book` 表中则不符合BCNF，所以单独拆分出来

- `isbn` ：主键。书本的ISBN，纯数字字符串，原则上只能是ISBN-13或ISBN-10，以 `-` 分隔，也就是13位或者17位，预留到20位以便日后扩充
- `name` ：书名，不超过256字符
- `author` ：作者，不超过256字符
- `price` ：单价，精确到分，小于10万亿
- `category` ：分类号，依据“中国图书馆分类法（第五版）”设置，不超过16字符。

### user

用户表

- `id` ：主键。用户ID，不小于5位，不大于16位的纯数字字符串，余下16位保留日后扩展使用。

- `password` ：密码的32位MD5散列。

- `type` ：用户类型，整数，其值及含义如下

  - `0` - root用户，超级管理员
  - `15` - 管理员
  - `16` - 普通用户
  - `255` - VIP用户

  在目前的实际实现中， `type < 16` 的就是管理员，取值 `15` ； `type >= 16` 的就是用户，取值 `16` 。以上设计仅为预留日后扩展需要，使用代号原则是管理员代号越接近 `0` 则权限越高，用户代号越接近 `255` 则用户权益越高，取值不超过 [0, 256) 。

- `name` ：用户名，不超过256字符。

- `phone_num` ：电话号码，留了足够的位数可以带城市区号（座机）和国际代号，不超过32字符。

- `email` ：电子邮箱地址，不超过256字符。

- `unit` ：单位，不超过256字符。

- `address` ：地址，不超过256字符。

### book

书本表，馆中每一本客观实在的书都在该表中有且仅有一条记录。

- `id` ：主键。书本唯一识别码，格式为 `ISBN-13 + 3位16进制序号` 或 `233 + ISBN-10 + 3位16进制序号` （ISBN不需要分隔符），共16位，余下16位留作日后扩展使用。

  其中，序号用于区分同ISBN的每本书，若不足3位则以前导0填充，取值范围为 [0x001, 0xffe] ，`0x000` 和 `0xfff` 留作日后扩展使用。一般按书本入库顺序由小到大录入，删除某书本后，如非必要，该书序号不再使用（此番是为减轻数据库查询负担，但如果没有新序号可以使用，也可以检查是否有旧的被弃用的序号可以使用）。

- `isbn` ： 外键，引用 book_info(isbn) 。该书的ISBN。

- `holder` ：外键，引用 user(id) 。该书的当前持有者。若已借出则为借书人ID，若在库中则为 `null`。

### borrow_log

- `seq` ：主键。序号，从1开始的自增整数。
- `date_time` ：产生该记录时的日期时间。
- `type` ：记录类型，`0` 为借书， `1` 为还书。其他值可留作日后扩展使用。
- `book_id` ：外键，引用 book(id) 。所借或还的书ID。
- `user_id` ：外键，引用 user(id) 。借或还书人的用户ID。
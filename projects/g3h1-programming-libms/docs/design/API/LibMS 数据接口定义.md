# LibMS 数据接口定义

> KeybrL 2018.11.06

[TOC]

## 0 概述

文件结构

```
libms/
 |- model/
     |- orm/
     |   |- Book.java
     |   |- BookInfo.java
     |   |- BorrowLog.java
     |   |- User.java
     |   |- Model.java
     |
     |- DataAPI.java
     |- DataMap.java
     |- Response.java
```

其中

- `libms.model.orm.*` ：定义了数据对象模式，一个类对应数据库中的一个表，模式参照数据库模式设计而设计，但针对数据接口做了一点优化（具体定义见 节 *1 数据模式定义*）
- `libms.model.DataAPI` ：定义数据接口方法，通过传入的数据库连接参数进行初始化，提供若干封装到“事务”的数据接口（具体定义见 节 *2 数据操作定义*），该类还提供了开发环境的数据接口，根据代码中固化的数据库连接参数进行数据库连接和实例化类 `DataAPI` 的对象，便于开发时使用（具体定义见 节 *4 开发环境中数据接口的使用*）
- `libms.model.DataMap` ：定义了部分数据的映射与转换接口（具体定义见 节 *3 数据映射定义*）。
- `libms.model.Response` ：定义了数据操作接口的返回格式 （具体见 节 *2.0 数据操作定义 - 概述*）

## 1 数据模式定义

### 1.0 概述

参考 ORM 的设计模式，定义如下四个类。

每个类对应一个数据库模式。类的每个对象对应数据库中相应表的一条行记录。每个类属性对应行记录某一列的值。外键表示为对另一数据对象的引用而非数据库中存储的值。

它们的每个对象都是只读的。也就是说，你只能从数据接口中获得这些对象，而不应该自己产生这些对象，更不应该将这些对象输入到数据接口中。对所获得对象的任何操作都不会影响数据库中的值。（所以你并不能把他当作真正的 ORM 进行操作）

为了简化数据接口对参数合法性的定义，以下每个数据模式类对部分属性（比如 User.name ）定义了一些静态方法（比如 User.checkName("hhh")），用于检查某个值是否可以是该属性的合法值。所以数据接口对参数合法性的定义就被简化为：所有能让对应检查方法返回 `true` 的值，对于数据接口来说，就是合法的。所以你可以在调用 节 *2 数据操作定义* 所描述的方法之前，先用这些静态方法检查一下你的参数。

每个数据模式类都是类 `libms.model.orm.Model` 的子类

以下各小节将以源代码的方式给出各数据模式类的部分定义。首先有以下几点说明：

1. 所列属性，如未在其后以注释的方式作另外说明，则其格式和含义同 *数据库设计文档* 中所作的说明；
2. 以 `check` 开头的静态方法是用于合法性检查的，如果一个值可以是对应属性的合法值，则该方法返回 `true` ，否则返回 `false` ；
3. 有些属性的值是由数据接口自动生成的，它们永远不会作为数据接口的输入，比如 `User.id` 。因此，对它们没有任何合法性检查的必要，他们不会有对应的检查方法；
4. 由于每个对象都是只读的，因此他们不需要成员方法，他们都只有属性和静态方法。

### 1.1 User

属性

```java
public String id;
public String password;
public String type;  // 其值只能是 “admin” 或 “user”
public String name;
public String phoneNum;
public String email;
public String unit;
public String address;
```

静态方法

```java
public static boolean checkPassword(String password) {}
public static boolean checkType(String type) {}
public static boolean checkName(String name) {}
public static boolean checkPhoneNum(String phoneNum) {}
public static boolean checkEmail(String email) {}
public static boolean checkUnit(String unit) {}
public static boolean checkAddress(String address) {}
```

### 1.2 BookInfo

属性

```java
public String isbn;
public String name;
public String author;
public BigDecimal price;  // 有效数字最多16位，其中小数点后有2位的 `BigDecimal` 类型（定点数）的对象
public String category;
public int total;  // 总存书量，该值为临时计算，数据库中并未存储
public int available;  // 可借书量，该值为临时计算，数据库中并未存储
```

静态方法

```java
public static boolean checkISBN(String isbn) {}
public static boolean checkName(String name) {}
public static boolean checkAuthor(String author) {}
public static boolean checkPrice(BigDecimal price) {}
public static boolean checkCategory(String category) {}
```

### 1.3 Book

属性

```java
public String id;
public BookInfo info;  // 对应数据库中的 `isbn` 属性，在此是对对应 `BookInfo` 对象的引用
public User holder;  // 对应数据库中的 `holder` 属性，在此是对对应 `User` 对象的引用
```

### 1.4 BorrowLog

属性

```java
public String seq;  // 用字符表示的整数
public Date dateTime;  // 精确到秒
public String type;  // 其值只能是 "borrow" 或 "return"
public Book book;  // 对应数据库中的 `book_id` 属性，在此是对应 `Book` 对象的引用
public User user;  // 对应数据库中的 `user_id` 属性，在此是对应 `User` 对象的引用
```

## 2 数据操作定义

### 2.0 概述

数据接口模块为其他模块以事务为单位封装了所有需要的数据操作，他们都可以通过类 `libms.model.DataAPI` 的对象的成员方法完成。

使用数据接口进行数据操作的最简单例子可以看下面这段示例代码

```java
import libms.model.DataAPI;
import libms.model.Response;

public class JustForTest {
    public static void main(String[] args) {
		
		// whatever...
		
		// initialization
		DataAPI dataAPI = new DataAPI("host:port", "database", "user", "password");
		
		// do something
        Response res = dataAPI.addUser("KeybrL", "admin", "12345qwert");
        if (res.statusCode == 200) {
        	// success
        }
        else {
        	// what's wrong with it?!
        }
        
        // whatever...
    }
}
```

正如上面这段示例程序那样，每一个数据操作都会返回一个类 `Response` 的对象，`Response` 部分定义如下

```java
import libms.model.orm.Model;

public Response {
    public int statusCode;  // 状态码，参考 HTTP/1.1: Status Code Definitions (RFC2616 Sec. 10)
    public String statusMessage;  // 状态描述
    public Model[] data;  // 数据列表
}
```

以下各小节将会详细叙述每个接口方法的使用。**以下示例代码中不会写出初始化语句，并且都将用 `dataAPI` 作为 `DataAPI` 的对象的变量名。**

### 2.1 对用户的操作

#### 2.1.0 验证

```java
public Response verifyUser(String userId, String password) {}
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `403 "forbidden"` 密码错误
- `404 "not found"` 用户不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 就只有一个元素，`data[0]` 为所验证的用户对应的类 `User` 的对象
- 其他情况下 `data` 都是 `null`

#### 2.1.1 添加用户

```java
public Response addUser(String name, String type, String password) {}
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数

数据

- 若 `statusCode` 的值位 `200` ， `data` 只有一个元素， `data[0]` 为新添加用户对应的类 `User` 的对象，用户id自动生成，其他信息置为空字符串 `""`
- 其他情况下 `data` 都是 `null`

#### 2.1.2 删除用户

```java
public Response deleteUser(String userId) {}
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `404 "not found"` 用户不存在

数据

- 若 `statusCode` 的值位 `200` ， `data` 只有一个元素， `data[0]` 为所删除用户删除前对应的类 `User` 的对象
- 其他情况下 `data` 都是 `null`

#### 2.1.3 获取用户信息

```java
public Response getUserById(String userId) {}  // 根据用户id获取用户
public Response getUsersByName(String name) {}  // 按姓名查找用户
public Response getUsersByType(String type) {}  // 获取所有 "admin" 用户或者 “user” 用户
public Response getUsers() {}  // 获取所有用户
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `404 "not found"` 用户不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 为所获得的类 `User` 的对象的列表
- 其他情况下 `data` 都是 `null`

#### 2.1.4 更新用户信息

```java
public Response updateUser(String userId, String name, String type, String password, String phoneNum, String email, String unit, String address) {}
```

其中，参数 `id` 为需要更新信息的用户id，其他参数为需要更新为的值，若某属性不需要更新，则对应参数输入 `null` 即可

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `404 "not found"` 用户不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 只有一个元素， `data[0]` 为所更新用户更新后对应的类 `User` 的对象
- 其他情况下 `data` 都是 `null`

### 2.2 对书本详细信息的操作

#### 2.2.1 添加书本信息

```java
public Response addBookInfo(String isbn) {}
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `403 "forbidden"` 书本信息已存在

数据

- 若 `statusCode` 的值为 `200` 或 `403` ， `data` 只有一个元素， `data[0]` 为新添加或已存在书本信息对应的类 `BookInfo` 的对象
- 其他情况下 `data` 都是 `null`

#### 2.2.2 删除书本信息

```java
public Response deleteBookInfo(String isbn) {}  // 删除指定书本信息
public Response deleteBooksInfo() {}  // 删除所有总存书量为0的书本信息
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `403 "forbidden"` 该书存书量不为0，无法删除
- `404 "not found"` 书本信息不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 为所有删除的书本信息对应的类 `BookInfo` 的对象的列表。执行 `deleteBookInfo()` 时， `data` 的长度将可能是 `0` （没有删除任何书本信息）
- 其他情况下 `data` 都是 `null`

#### 2.2.3 获取书本信息

```java
public Response getBookInfoByISBN(String isbn) {}  // 根据ISBN获取书本信息
public Response getBooksInfoByName(String name, boolean strict) {}  // 根据书名获取书本信息
public Response getBooksInfoByAuthor(String author, boolean strict) {}  // 根据作者获取书本信息
public Respinse getBooksInfoByKey(String key) {}  // 根据关键字获取书本信息
public Response getBooksInfo() {}  // 获取所有书本信息
```

add其中， `getBooksInfoByName` 和 `getBooksInfoByAuthor` 的第二个参数指定是否严格匹配，输入 `true` 则为严格匹配，否则为近似匹配

方法 `getBooksInfoByKey` 会对所有书本信息的 ISBN、书名、作者、标签 进行近似匹配 

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `404 "not found"` 书本不存在

数据

- 若 `statusCode` 的值是 `200` ， `data` 为所获得所有书本信息对应的类 `BookInfo` 的对象的列表。
- 其他情况下 `data` 都是 `null`

#### 2.2.4 更新书本信息

```java
public Response updateBookInfo(String isbn, String name, String author, BigDecimal price, String category)
```

其中，参数 `isbn` 为需要更新信息的书本信息ISBN，其他参数为需要更新为的值，若某属性不需要更新，则对应参数输入 `null` 即可

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `404 "not found"` 该书本信息不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 只有一个元素， `data[0]` 为更新的书本信息对应的类 `BookInfo` 的对象
- 其他情况下 `data` 都是 `null`

### 2.3 对书本的操作

#### 2.3.1 添加书本

```java
public Response addBooks(String isbn, int number) {}
```

其中，`number` 为要添加的书本的数目，`isbn` 为 所添加书本的 ISBN，若相应书本信息已存在则自动绑定到书本信息，若相应书本信息不存在，则自动新建书本信息。

状态

- `200 "ok"` 成功
- `200 "ok but not perfect"` 成功但是书本信息不存在，已自动新建书本信息
- `400 "bad request"` 非法参数

数据

- 若 `statusCode` 的值为 `200` ， `data` 为所添加书本对应的类 `Book` 对象的列表
- 其他情况下 `data` 都是 `null`

#### 2.3.2 删除书本

```java
public Response deleteBook(String bookId) {}
```

其中 `id` 是书本id

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `403 "forbidden"` 书本已被借出，不能删除
- `404 "not found"` 该书不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 只有一个元素， `data[0]` 为删除的书本对应的类 `Book` 的对象
- 其他情况下 `data` 都是 `null`

#### 2.3.3 获取书本

```java
public Response getBookById(String bookId) {}  // 根据书本id获取书本
public Response getBooksByISBN(String isbn) {}  // 根据书本ISBN获取书本
public Response getBooksByHolder(String userId) {}  // 根据书本持有人获取书本
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `404 "not found"` 根据方法的不同，可能是书本不存在、书本信息不存在或书本持有人不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 为所获得书本对应的类 `Book` 对象的列表
- 其他情况下 `data` 都是 `null`

### 2.4 借还书操作

#### 2.4.1 借书

```java
public Response borrowBook(String userId, String bookId) {}
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `403 "borrowed too much"` 借书数目过多，不能再借
- `403 "loaned out"` 书本已被借出
- `404 "book not found"` 书本不存在
- `404 "user not found"` 用户不存在

数据

- 只要不是 `404 "user not found"` 的情况， `data` 为该用户当前所借所有书对应的类 `Book` 对象的列表
- 其他情况下 `data` 都是 `null`

#### 2.4.2 还书

```java
public Response returnBook(String bookId) {}
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `403 "not loaned out"` 书本未被借出，不能还书
- `404 "not found"` 书本不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 只有一个元素， `data[0]` 为该书之前借书人对应的类 `User` 对象
- 其他情况下 `data` 都是 `null`

#### 2.4.3 借书时长查询

```java
public Response borrowHowLong(String bookId) {}
```

状态

- `200 "12345"` 成功（"12345" 是一个表示整数的字符串，表示以秒计的借书的时长，规定一次借书不能超过60天，即5184000秒）
- `400 "bad request"` 非法参数
- `403 "forbidden"` 书本未借出
- `404 "not found"` 书本不存在

数据

- 若 `statusCode` 的值为 `200` ， `data` 只有一个元素， `data[0]` 为该书借书人对应的类 `User` 对象
- 其他情况下 `data` 都是 `null`

### 2.5 借还记录查询

```java
public Response getBorrowLogsByUser(String userId) {}  // 获取某用户借还记录
public Response getBorrowLogsByBook(String bookId) {}  // 获取某书借还记录
public Response getBorrowLogsByISBN(String isbn) {}  // 获取某种书借还记录
public Response getBorrowLogsByDateTime(Date beginDateTime, Date endDateTime) {}  // 获取某段时间内的借还记录
public Response getBorrowLogs() {}  // 获取所有借还记录
```

状态

- `200 "ok"` 成功
- `400 "bad request"` 非法参数
- `404 "not found"` 根据所使用方法不同，可以是用户不存在、书本不存在、书本信息不存在、日期时间不存在（早于第一条记录产生的时间或晚于当前时间）

数据

- 若 `statusCode` 的值为 `200` ， `data` 为所获取的借还记录对应的类 `BorrowLog` 对象的列表
- 其他情况下 `data` 都是 `null`

## 3 数据映射定义

### 3.0 概述

基于对数据库性能的考虑，某些数据会以不很直观的方式呈现（比如书本的分类可能以类似“TP311.5”这样的分类号，而不是以类似“工业 -> 自动化技术、计算机技术 -> 计算机软件 -> 软件工程”这样的分类名来存储）。但对于应用的其他部分来说，他们没有必要完全了解数据的存储形式。因此需要定义一些方法，用以将数据从数据库存储的形式转化为用户友好的形式，或者反向的转化。这些方法都被定义在类 `libms.model.DataMap` 中，他们的存在形式是多种多样的，比如内部类、成员方法、静态方法、静态属性、...一种映射被定义为何种形式将取决于怎样更用户友好。

以下各小节将会详细叙述每个映射的定义与使用

### 3.1 暂无需求

......欢迎应用其他部分开发者提出需求。

## 4 开发环境中数据接口的使用

在开发环境，你并不需要手动实例化类 `DataAPI` 的对象。

使用数据接口，你只需要

```java
import libms.model.DataAPI
import libms.model.Response

public class JustForTest {
    public static void main(String[] args) {
    
		// whatever...
		
		DataAPI dataAPI = DataAPI.testAPI();
		Response res = addUser("KeybrL", "admin", "password");
        if (res.statusCode == 200) {
            // 成功添加用户
        }
        else {
            // What's wrong with it!?
        }
		
		// whatever...
		
    }
}
```

正如上面这段代码这样，你需要做的只是用 `DataAPI` 的一个静态方法实例化一个类 `DataAPI` 的对象，比如示例中的 `dataAPI` ，然后就像使用类 `DataAPI` 的对象那样使用它就好。

**但是，基于对整个应用结构的考虑，你不应该在每个需要的方法中实例化一个类 `DataAPI` 的对象。你应该尽可能少地实例化它，对这个对象的引用应该随着方法的调用逐级传递。因为这个对象在最终发布版本中，是全局唯一的**

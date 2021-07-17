package libms.model;

// package libms.model
import libms.model.orm.Book;
import libms.model.orm.BookInfo;
import libms.model.orm.User;
import libms.model.orm.BorrowLog;

// package java.sql
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// others
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.math.BigInteger;
import java.math.BigDecimal;


/**
 * 数据接口类
 * 对数据库访问操作，以事务为单位进行封装
 *
 * @author keybrl
 */
public class DataAPI {

    private DBManager dbManger;
    public DataAPI(String host, String database, String user, String password) {
        this.dbManger = new DBManager(host, database, user, password);
    }
    public static DataAPI testAPI() {
        return new DataAPI("39.108.75.56:3306", "libms", "libms", "12345qwert");
    }


    private Response initDB() {
        Response res = new Response();


        String[] sqls = {
                "DROP TABLE IF EXISTS borrow_log",
                "DROP TABLE IF EXISTS book",
                "DROP TABLE IF EXISTS user",
                "DROP TABLE IF EXISTS book_info",
                "CREATE TABLE book_info (" +
                        "isbn CHAR(20) PRIMARY KEY NOT NULL," +
                        "name VARCHAR(256) NOT NULL," +
                        "author VARCHAR(256) NOT NULL," +
                        "price NUMERIC(16, 2) NOT NULL," +
                        "category CHAR(16) NOT NULL" +
                        ")",
                "CREATE TABLE user (" +
                        "id BIGINT PRIMARY KEY NOT NULL," +
                        "password CHAR(32) NOT NULL," +
                        "type INTEGER NOT NULL," +
                        "name VARCHAR(256) NOT NULL," +
                        "phone_num CHAR(32) NOT NULL," +
                        "email VARCHAR(256) NOT NULL," +
                        "unit VARCHAR(256) NOT NULL," +
                        "address VARCHAR(256) NOT NULL" +
                        ")",
                "CREATE TABLE book (" +
                        "id CHAR(32) PRIMARY KEY NOT NULL," +
                        "isbn CHAR(20) NOT NULL," +
                        "holder BIGINT," +
                        "FOREIGN KEY (isbn) REFERENCES book_info(isbn)," +
                        "FOREIGN KEY (holder) REFERENCES user(id)" +
                        ")",
                "CREATE TABLE borrow_log (" +
                        "seq BIGINT PRIMARY KEY AUTO_INCREMENT NOT NULL," +
                        "date_time DATETIME NOT NULL," +
                        "type INTEGER NOT NULL," +
                        "book_id CHAR(32) NOT NULL," +
                        "user_id BIGINT NOT NULL," +
                        "FOREIGN KEY (book_id) REFERENCES book(id)," +
                        "FOREIGN KEY (user_id) REFERENCES user(id)" +
                        ")"
        };
        Connection conn = this.dbManger.connect();
        Statement stmt = DBManager.statement(conn);
        try {
            for (String sql: sqls) {
                stmt.execute(sql);
            }
        }
        catch (SQLException ex){
            DBManager.printEx(ex);
            res.statusCode = 500;
            res.statusMessage = "unknown error";
        }
        finally {
            DBManager.closeStatement(stmt);
            DBManager.closeConnection(conn);
        }
        return res;
    }


    // 数据接口方法
    // 对用户的操作
    public Response verifyUser(String userId, String password) {

        if (userId == null || userId.length() == 0 || !User.checkPassword(password)) {
            return new Response(400);
        }

        Response res = getUserById(userId);
        if (res.statusCode == 200) {
            if (!((User)res.data[0]).password.equals(User.md5(password))) {
                res = new Response(403);
            }
        }
        return res;
    }
    public Response addUser(String name, String type, String password) {

        // 参数初步检查
        if (!User.checkName(name) || !User.checkType(type) || !User.checkPassword(password)) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();

        // 获取当前最大用户id
        Statement stmt = DBManager.statement(conn);
        BigInteger lastId = new BigInteger("0");
        try {
            ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM user");
            rs.first();
            if (rs.getString(1) == null) {
                lastId = new BigInteger("10000");
            }
            else {
                rs.first();
                lastId = new BigInteger(rs.getString(1));
            }
            DBManager.closeStatement(stmt);
            stmt = null;
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        if (lastId.equals(BigInteger.ZERO)) {
            throw new RuntimeException("无法从数据库获取数据，无法获知当前最大 user(id) 。");
        }
        else {
            lastId = lastId.add(BigInteger.ONE);
        }


        PreparedStatement preparedStmt = DBManager.statement(conn, "INSERT INTO user VALUE (?, ?, ?, ?, '', '', '', '')");
        try {
            preparedStmt.setString(1, lastId.toString());
            preparedStmt.setString(2, User.md5(password));
            preparedStmt.setInt(3, type.equals("admin") ? 15 : 16);
            preparedStmt.setString(4, name);
            preparedStmt.execute();
            res.data = new User[1];
            res.data[0] = new User(lastId.toString(), User.md5(password), type, name, "", "", "", "");
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStmt);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response deleteUser(String userId) {

        if (userId == null || userId.length() == 0) {
            return new Response(400);
        }

        Response res = getUserById(userId);
        if (res.statusCode != 200) {
            return new Response(404);
        }

        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "DELETE FROM user WHERE id = ?");
        try {
            preparedStatement.setString(1, userId);
            preparedStatement.execute();
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getUserById(String userId) {

        if (userId == null || userId.length() == 0) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();

        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = DBManager.statement(conn, "SELECT * FROM user WHERE id = ?");
            preparedStatement.setString(1, userId);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                res.data = new User[1];
                res.data[0] = DBManager.createUser(rs);
            }
            else {
                // 可恶的失败
                res = new Response(404);
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getUsersByName(String name) {

        if (!User.checkName(name)) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();

        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM user WHERE name = ?");
        try {
            preparedStatement.setString(1, name);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.first()) {
                res.data = DBManager.createUsers(rs);
            }
            else {
                res = new Response(404);
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getUsersByType(String type) {

        if (!User.checkType(type)) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();

        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM user WHERE type = ?");
        try {
            preparedStatement.setInt(1, type.equals("admin") ? 15 : 16);
            ResultSet rs = preparedStatement.executeQuery();

            res.data = DBManager.createUsers(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getUsers() {

        Response res = new Response(200);

        Connection conn = this.dbManger.connect();
        Statement stmt = DBManager.statement(conn);
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM user");
            res.data = DBManager.createUsers(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response updateUser(String userId, String name, String type, String password, String phoneNum, String email, String unit, String address) {
        if (
                userId == null ||
                        (name != null && !User.checkName(name)) ||
                        (type != null && !User.checkType(type)) ||
                        (password != null && !User.checkPassword(password)) ||
                        (phoneNum != null && !User.checkPhoneNum(phoneNum)) ||
                        (email != null && !User.checkEmail(email)) ||
                        (unit != null && !User.checkUnit(unit)) ||
                        (address != null && !User.checkAddress(address))
        ) {
            return new Response(400);
        }

        Response oldUser = getUserById(userId);
        if (oldUser.statusCode != 200) {
            return new Response(404);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "UPDATE user SET password = ?, type = ?, name = ?, phone_num = ?, email = ?, unit = ?, address = ? WHERE id = ?");
        try {
            preparedStatement.setString(1, password != null ? User.md5(password) : ((User) oldUser.data[0]).password);
            preparedStatement.setInt(2, type != null ? (type.equals("admin") ? 15 : 16) : (((User) oldUser.data[0]).type.equals("admin") ? 15 : 16));
            preparedStatement.setString(3, name != null ? name : ((User) oldUser.data[0]).name);
            preparedStatement.setString(4, phoneNum != null ? phoneNum : ((User) oldUser.data[0]).phoneNum);
            preparedStatement.setString(5, email != null ? email : ((User) oldUser.data[0]).email);
            preparedStatement.setString(6, unit != null ? unit : ((User) oldUser.data[0]).unit);
            preparedStatement.setString(7, address != null ? address : ((User) oldUser.data[0]).address);
            preparedStatement.setString(8, userId);
            preparedStatement.execute();

            res.data = getUserById(userId).data;
        } catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        } finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }

    // 对书本详细信息的操作
    public Response addBookInfo(String isbn) {

        if (!BookInfo.checkISBN(isbn)) {
            return new Response(400);
        }

        if (getBookInfoByISBN(isbn).statusCode == 200) {
            return new Response(403);
        }

        Response res = new Response(500);
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "INSERT INTO book_info VALUE(?, '', '', 0.00, '')");
        try {
            preparedStatement.setString(1, isbn);
            preparedStatement.execute();

            res = getBookInfoByISBN(isbn);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response deleteBookInfo(String isbn) {

        if (!BookInfo.checkISBN(isbn)) {
            return new Response(400);
        }

        Response res = getBookInfoByISBN(isbn);
        if (res.statusCode != 200) {
            return new Response(404);
        }
        if (((BookInfo)res.data[0]).total != 0) {
            return new Response(403);
        }

        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "DELETE FROM book_info WHERE isbn = ?");
        try {
            preparedStatement.setString(1, isbn);
            preparedStatement.execute();
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response deleteBooksInfo() {

        Response allBooks = getBooksInfo();
        if (allBooks.statusCode != 200) {
            return new Response(500);
        }
        if (allBooks.data.length == 0) {
            return new Response(200, "ok", new BookInfo[0]);
        }


        Response res = new Response(200);
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "DELETE FROM book_info WHERE isbn = ?");
        try {
            List<BookInfo> deletedBooks = new ArrayList<BookInfo>();

            for (BookInfo oneBook: (BookInfo[])allBooks.data) {
                if (getBooksNum(oneBook.isbn)[0] == 0) {
                    preparedStatement.setString(1, oneBook.isbn);
                    preparedStatement.execute();

                    deletedBooks.add(oneBook);
                }
            }

            res.data = deletedBooks.toArray(new BookInfo[0]);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }


        return res;
    }
    public Response getBookInfoByISBN(String isbn) {

        if (!BookInfo.checkISBN(isbn)) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM book_info WHERE isbn = ?");
        try {
            preparedStatement.setString(1, isbn);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.first()) {
                res.data = new BookInfo[1];
                res.data[0] = createBookInfo(rs);
            }
            else {
                res = new Response(404);
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBooksInfoByName(String name, boolean strict) {

        if (!BookInfo.checkName(name)) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = null;
        try {
            if (strict) {
                preparedStatement = DBManager.statement(conn, "SELECT * FROM book_info WHERE name = ?");
                preparedStatement.setString(1, name);
            }
            else {
                preparedStatement = DBManager.statement(conn, "SELECT * FROM book_info WHERE name like ?");
                preparedStatement.setString(1, "%" + name + "%");
            }
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBooksInfo(rs);

        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBooksInfoByAuthor(String author, boolean strict) {
        if (!BookInfo.checkAuthor(author)) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = null;
        try {
            if (strict) {
                preparedStatement = DBManager.statement(conn, "SELECT * FROM book_info WHERE author = ?");
                preparedStatement.setString(1, author);
            }
            else {
                preparedStatement = DBManager.statement(conn, "SELECT * FROM book_info WHERE author like ?");
                preparedStatement.setString(1, "%" + author + "%");
            }
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBooksInfo(rs);

        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBooksInfoByKey(String key) {

        if (key == null || key.length() == 0) {
            return new Response(400);
        }

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM book_info WHERE isbn LIKE ? OR name LIKE ? OR author LIKE ? OR category LIKE ? ORDER BY isbn");
        try {
            preparedStatement.setString(1, "%" + key + "%");
            preparedStatement.setString(2, "%" + key + "%");
            preparedStatement.setString(3, "%" + key + "%");
            preparedStatement.setString(4, "%" + key + "%");
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBooksInfo(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBooksInfo() {

        Response res = new Response(200);
        Connection conn = this.dbManger.connect();
        Statement stmt = DBManager.statement(conn);
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM book_info");
            res.data = createBooksInfo(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response updateBookInfo(String isbn, String name, String author, BigDecimal price, String category) {

        if (
                !BookInfo.checkISBN(isbn) ||
                (name != null && !BookInfo.checkName(name)) ||
                (author != null && !BookInfo.checkAuthor(author)) ||
                (price != null && !BookInfo.checkPrice(price)) ||
                (category != null && !BookInfo.checkCategory(category))
        ) {
            return new Response(400);
        }

        Response oldBookInfo = getBookInfoByISBN(isbn);
        if (oldBookInfo.statusCode != 200) {
            return new Response(404);
        }

        Response res = null;
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "UPDATE book_info SET name = ?, author = ?, price = ?, category = ? WHERE isbn = ?");
        try {
            preparedStatement.setString(1, name != null ? name : ((BookInfo)oldBookInfo.data[0]).name);
            preparedStatement.setString(2, author != null ? author : ((BookInfo)oldBookInfo.data[0]).author);
            preparedStatement.setBigDecimal(3, price != null ? price : ((BookInfo)oldBookInfo.data[0]).price);
            preparedStatement.setString(4, category != null ? category : ((BookInfo)oldBookInfo.data[0]).category);
            preparedStatement.setString(5, isbn);
            preparedStatement.execute();

            res = getBookInfoByISBN(isbn);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    private int[] getBooksNum(String isbn) {
        int total = 0;
        int available = 0;

        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT id, holder FROM book WHERE isbn = ?");
        try {
            preparedStatement.setString(1, isbn);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                total += 1;
                if (rs.getString(2) == null) {
                    available += 1;
                }
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return new int[]{total, available};
    }
    private BookInfo createBookInfo(ResultSet rs) {
        try {
            int[] booksNum = getBooksNum(rs.getString("isbn"));
            return new BookInfo(
                    rs.getString("isbn"),
                    rs.getString("name"),
                    rs.getString("author"),
                    rs.getBigDecimal("price"),
                    rs.getString("category"),
                    booksNum[0],
                    booksNum[1]
            );
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }
        return null;
    }
    private BookInfo[] createBooksInfo(ResultSet rs) {
        List<BookInfo> bookInfoList = new ArrayList<BookInfo>();

        try {
            if (rs.first()) {
                bookInfoList.add(createBookInfo(rs));
                while (rs.next()) {
                    bookInfoList.add(createBookInfo(rs));
                }
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }

        return bookInfoList.toArray(new BookInfo[0]);
    }

    // 对书本的操作
    public Response addBooks(String isbn, int number) {

        if (!BookInfo.checkISBN(isbn) || number < 1) {
            return new Response(400);
        }

        //
        int bookIdLast = 1;
        Response res = new Response(200);
        if (getBookInfoByISBN(isbn).statusCode != 200) {
            addBookInfo(isbn);
            res.statusMessage = "ok but not perfect";
        }
        else {
            Response existsBooks = getBooksByISBN(isbn);
            if (existsBooks.data != null && existsBooks.data.length != 0) {
                for (Book existsBook: (Book[])existsBooks.data) {
                    int existsIdLast = Integer.parseInt(existsBook.id.substring(13, 16), 16);
                    bookIdLast = existsIdLast > bookIdLast ? existsIdLast : bookIdLast;
                }
                bookIdLast += 1;
            }
        }
        if (bookIdLast + number - 1 > 4094) {
            return new Response(500, "books too much");
        }


        // 开始插入
        Connection conn = this.dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "INSERT INTO book VALUES(?, ?, null)");
        res.data = new Book[number];
        try {
            preparedStatement.setString(2, isbn);
            for (int i = 0; i < number; i++) {
                preparedStatement.setString(1, buildBookId(isbn, bookIdLast + i));
                preparedStatement.execute();

                res.data[i] = getBookById(buildBookId(isbn, bookIdLast + i)).data[0];
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response deleteBook(String bookId) {

        if (bookId == null || bookId.length() == 0) {
            return new Response(400);
        }

        Response existsBook = getBookById(bookId);
        if (existsBook.statusCode != 200) {
            return new Response(404);
        }

        if (((Book)existsBook.data[0]).holder != null) {
            return new Response(403);
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "DELETE FROM book WHERE id = ?");
        try {
            preparedStatement.setString(1, bookId);
            preparedStatement.execute();

            res.data = new Book[1];
            res.data[0] = existsBook.data[0];
            ((Book)res.data[0]).info.total -= 1;
            ((Book)res.data[0]).info.available -= 1;
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBookById(String bookId) {

        if (bookId == null || bookId.length() == 0) {
            return new Response(400);
        }


        Response res = null;
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM book WHERE id = ?");
        try {
            preparedStatement.setString(1, bookId);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.first()) {
                res = new Response(200, "ok", new Book[1]);
                res.data[0] = createBook(rs);
            }
            else {
                res = new Response(404);
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBooksByISBN(String isbn) {

        if (!BookInfo.checkISBN(isbn)) {
            return new Response(400);
        }

        if (getBookInfoByISBN(isbn).statusCode != 200) {
            return new Response(404);
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM book WHERE isbn = ?");
        try {
            preparedStatement.setString(1, isbn);
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBooks(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBooksByHolder(String userId) {

        if (userId == null || userId.length() == 0) {
            return new Response(400);
        }

        if (getUserById(userId).statusCode != 200) {
            return new Response(404);
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM book WHERE holder = ?");
        try {
            preparedStatement.setString(1, userId);
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBooks(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    private Book createBook(ResultSet rs) {
        try {
            String holderId = rs.getString("holder");
            return new Book(
                    rs.getString("id"),
                    (BookInfo)getBookInfoByISBN(rs.getString("isbn")).data[0],
                    holderId == null ? null : (User)getUserById(holderId).data[0]
            );
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }
        return null;
    }
    private Book[] createBooks(ResultSet rs) {
        List<Book> bookList = new ArrayList<Book>();

        try {
            if (rs.first()) {
                bookList.add(createBook(rs));
                while (rs.next()) {
                    bookList.add(createBook(rs));
                }
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }

        return bookList.toArray(new Book[0]);
    }
    private static String buildBookId(String isbn, int bookIdLast) {
        StringBuilder bookId = new StringBuilder();
        for (char ch: isbn.toCharArray()) {
            if (ch != '-') {
                bookId.append(ch);
            }
        }
        if (bookId.length() == 10) {
            bookId.insert(0, "233");
        }
        bookId.append(Integer.toHexString(bookIdLast));
        while (bookId.length() < 16) {
            bookId.insert(13, "0");
        }
        return bookId.toString();
    }

    // 借还书操作
    public Response borrowBook(String userId, String bookId) {

        if (userId == null || bookId == null) {
            return new Response(400);
        }

        Response targetBook = getBookById(bookId);
        if (targetBook.statusCode != 200) {
            return new Response(404, "book not found");
        }
        if (((Book)targetBook.data[0]).holder != null) {
            return new Response(403, "loaned out");
        }

        Response targetUser = getUserById(userId);
        if (targetUser.statusCode != 200) {
            return new Response(404, "user not found");
        }

        Response targetUserBook = getBooksByHolder(userId);
        if (targetUserBook.data != null && targetUserBook.data.length >= 5) {  // 每位读者最多同时借书5本
            return new Response(403, "borrowed too much");
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = DBManager.statement(conn, "UPDATE book SET holder = ? WHERE id = ?");
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, bookId);
            preparedStatement.execute();

            preparedStatement = DBManager.statement(conn, "INSERT INTO borrow_log VALUE(null, ?, 0, ?, ?)");
            preparedStatement.setString(1, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
            preparedStatement.setString(2, bookId);
            preparedStatement.setString(3, userId);
            preparedStatement.execute();

            res.data = getBooksByHolder(userId).data;
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response returnBook(String bookId) {

        if (bookId == null || bookId.length() == 0) {
            return new Response(400);
        }

        Response targetBook = getBookById(bookId);
        if (targetBook.statusCode != 200) {
            return new Response(404);
        }

        if (((Book)targetBook.data[0]).holder == null) {
            return new Response(403);
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = DBManager.statement(conn, "UPDATE book SET holder = null WHERE id = ?");
            preparedStatement.setString(1, bookId);
            preparedStatement.execute();

            preparedStatement = DBManager.statement(conn, "INSERT INTO borrow_log VALUE(null, ?, 1, ?, ?)");
            preparedStatement.setString(1, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
            preparedStatement.setString(2, bookId);
            preparedStatement.setString(3, ((Book)targetBook.data[0]).holder.id);
            preparedStatement.execute();

            res.data = new User[1];
            res.data[0] = ((Book)targetBook.data[0]).holder;
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response borrowHowLong(String bookId) {

        if (bookId == null || bookId.length() == 0) {
            return new Response(400);
        }

        Response log = getBorrowLogsByBook(bookId);
        if (log.statusCode != 200) {
            return new Response(404);
        }

        Response res = new Response(200, "-1", new User[1]);
        if (log.data.length == 0 || ((BorrowLog)log.data[0]).book.holder == null) {
            return new Response(403);
        }
        else {
            res.data[0] = ((BorrowLog)log.data[0]).book.holder;
            res.statusMessage = BigInteger.valueOf(((new Date()).getTime() - ((BorrowLog)log.data[0]).dateTime.getTime()) / 1000).toString();
        }

        return res;
    }

    // 借还记录查询
    public Response getBorrowLogsByUser(String userId) {

        if (userId == null || userId.length() == 0) {
            return new Response(400);
        }

        if (getUserById(userId).statusCode != 200) {
            return new Response(404);
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM borrow_log WHERE user_id = ? ORDER BY date_time DESC");
        try {
            preparedStatement.setString(1, userId);
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBorrowLogs(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBorrowLogsByBook(String bookId) {
        if (bookId == null || bookId.length() == 0) {
            return new Response(400);
        }

        if (getBookById(bookId).statusCode != 200) {
            return new Response(404);
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM borrow_log WHERE book_id = ? ORDER BY date_time DESC");
        try {
            preparedStatement.setString(1, bookId);
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBorrowLogs(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBorrowLogsByISBN(String isbn) {

        if (!BookInfo.checkISBN(isbn)) {
            return new Response(404);
        }

        Response allBooks = getBooksByISBN(isbn);
        if (allBooks.statusCode != 200) {
            return new Response(404);
        }

        if (allBooks.data.length == 0) {
            return new Response(200, "ok", new BorrowLog[0]);
        }

        Response res = new Response(200);
        List<BorrowLog> logList = new ArrayList<BorrowLog>();
        for (Book book: (Book[])allBooks.data) {
            Response logs = getBorrowLogsByBook(book.id);
            if (logs.statusCode != 200) {
                continue;
            }
            logList.addAll(Arrays.asList((BorrowLog[])logs.data));
        }
        res.data = logList.toArray(new BorrowLog[0]);

        return res;
    }
    public Response getBorrowLogsByDateTime(Date beginDateTime, Date endDateTime) {

        if (beginDateTime == null || endDateTime == null) {
            return new Response(400);
        }

        Date nowDateTime = new Date();
        if (beginDateTime.compareTo(nowDateTime) >= 0) {
            return new Response(404);
        }

        Response res = new Response(200);
        Connection conn = dbManger.connect();
        PreparedStatement preparedStatement = DBManager.statement(conn, "SELECT * FROM borrow_log WHERE date_time >= ? AND date_time <= ? ORDER BY date_time DESC");
        try {
            preparedStatement.setString(1, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(beginDateTime));
            preparedStatement.setString(2, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(endDateTime));
            ResultSet rs = preparedStatement.executeQuery();

            res.data = createBorrowLogs(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(preparedStatement);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    public Response getBorrowLogs() {
        Response res = new Response(200);
        Connection conn = dbManger.connect();
        Statement stmt = DBManager.statement(conn);
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM borrow_log ORDER BY seq DESC");

            res.data = createBorrowLogs(rs);
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            DBManager.closeConnection(conn);
        }

        return res;
    }
    private BorrowLog createBorrowLog(ResultSet rs) {
        try {
            return new BorrowLog(
                    rs.getString("seq"),
                    new Date(rs.getTimestamp("date_time").getTime()),
                    rs.getInt("type") == 0 ? "borrow" : "return",
                    (Book)getBookById(rs.getString("book_id")).data[0],
                    (User)getUserById(rs.getString("user_id")).data[0]
            );
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }
        return null;
    }
    private BorrowLog[] createBorrowLogs(ResultSet rs) {
        List<BorrowLog> logList = new ArrayList<BorrowLog>();

        try {
            if (rs.first()) {
                logList.add(createBorrowLog(rs));
                while (rs.next()) {
                    logList.add(createBorrowLog(rs));
                }
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }

        return logList.toArray(new BorrowLog[0]);
    }
}

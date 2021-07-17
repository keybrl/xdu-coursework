package libms.model;

import libms.model.orm.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


/**
 * 数据库管理员类
 * 维护数据库连接参数。提供安全 简洁的数据库连接接口
 *
 * @author keybrl
 */
class DBManager {
    private String host;
    private String database;
    private String user;
    private String password;

    DBManager(String host, String database, String user, String password) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    Connection connect() {
        if (null == host) {
            host = "localhost:3306";
        }
        String connURL = "jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + password;

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connURL);
        }
        catch (SQLException ex) {
            printEx(ex);
        }
        return conn;
    }


    static void printEx(SQLException ex) {
        System.out.println("SQLException: " + ex.getMessage());
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("VendorError: " + ex.getErrorCode());
    }
    static Statement statement(Connection conn) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
        }
        catch (SQLException ex){
            printEx(ex);
        }
        return stmt;
    }
    static PreparedStatement statement(Connection conn, String sql) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
        }
        catch (SQLException ex){
            DBManager.printEx(ex);
        }
        return stmt;
    }
    static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            }
            catch (SQLException sqlEx) {
                DBManager.printEx(sqlEx);
            }
        }
    }
    static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException sqlEx) {
                DBManager.printEx(sqlEx);
            }
        }
    }
    static void closeConnection(Connection conn) {
        try {
            conn.close();
        }
        catch (SQLException ex) {
            printEx(ex);
        }
    }

    static User createUser(ResultSet rs) {
        try {
            return new User(
                    rs.getString("id"),
                    rs.getString("password"),
                    rs.getInt("type") == 15 ? "admin" : "user",
                    rs.getString("name"),
                    rs.getString("phone_num"),
                    rs.getString("email"),
                    rs.getString("unit"),
                    rs.getString("address")
            );
        }
        catch (SQLException ex) {
            printEx(ex);
        }
        return null;
    }
    static User[] createUsers(ResultSet rs) {
        List<User> userList = new ArrayList<User>();

        try {
            if (rs.first()) {
                userList.add(DBManager.createUser(rs));
                while (rs.next()) {
                    userList.add(createUser(rs));
                }
            }
        }
        catch (SQLException ex) {
            printEx(ex);
        }

        return userList.toArray(new User[0]);
    }
}

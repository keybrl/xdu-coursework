package pim.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;


/**
 * 数据库管理员类
 * 维护数据库连接参数，维护连接池。提供安全 简洁的数据库连接接口
 *
 * @author keybrl
 */
class DBManager {
    private BasicDataSource dataSource;

    DBManager(String host, String database, String user, String password) {
        // 数据库配置
        this.dataSource = new BasicDataSource();
        this.dataSource.setUrl("jdbc:mysql://" + host + "/" + database + "?user=" + user + "&password=" + password);
        // 连接池配置
        this.dataSource.setInitialSize(8);
        this.dataSource.setMinIdle(4);
        this.dataSource.setMaxIdle(16);
        this.dataSource.setMaxTotal(32);
        this.dataSource.setMaxWaitMillis(5000);
        closeConnection(this.getConnection());
    }


    Connection getConnection() {
        try {
            return this.dataSource.getConnection();
        }
        catch (SQLException ex) {
            printEx(ex);
        }
        return null;
    }
    void returnConnection(Connection conn) {
        closeConnection(conn);
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
}

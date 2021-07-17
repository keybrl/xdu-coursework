package pim.model;

// package java.sql
import pim.model.orm.PIMAppointment;
import pim.model.orm.PIMContact;
import pim.model.orm.PIMEntity;
import pim.model.orm.PIMNote;
import pim.model.orm.PIMTodo;
import pim.generic.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



public class DataAPI {

    public static void main(String[] args) {
        DataAPI dataAPI = new DataAPI("39.108.75.56:3306", "pim", "pim", "12345qwert");
        System.out.println((dataAPI.initDB()).statusCode);
    }


    private DBManager dbManager;

    public DataAPI(String host, String database, String user, String password) {
        this.dbManager = new DBManager(host, database, user, password);
    }


    // 初始化数据库，删表 重建表
    private Response initDB() {

        Response res = new Response();


        String[] sqls = {
                "DROP TABLE IF EXISTS contact",
                "DROP TABLE IF EXISTS appointment",
                "DROP TABLE IF EXISTS note",
                "DROP TABLE IF EXISTS todo",
                "DROP TABLE IF EXISTS entity",
                "DROP TABLE IF EXISTS user",
                "CREATE TABLE user (" +
                        "email VARCHAR(256) PRIMARY KEY NOT NULL," +
                        "name VARCHAR(256) NOT NULL," +
                        "passwd VARCHAR(256) NOT NULL" +
                ")",
                "CREATE TABLE entity (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL," +
                        "create_time DATETIME NOT NULL," +
                        "type INTEGER NOT NULL," +
                        "priority VARCHAR(128) NOT NULL," +
                        "user_id VARCHAR(256) NOT NULL," +
                        "sharable BOOLEAN NOT NULL," +
                        "FOREIGN KEY (user_id) REFERENCES user(email)" +
                ")",
                "CREATE TABLE todo (" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "date DATETIME NOT NULL," +
                        "text VARCHAR(512) NOT NULL," +
                        "FOREIGN KEY (id) REFERENCES entity(id)" +
                ")",
                "CREATE TABLE note (" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "text VARCHAR(512) NOT NULL," +
                        "FOREIGN KEY (id) REFERENCES entity(id)" +
                ")",
                "CREATE TABLE appointment (" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "date DATETIME NOT NULL," +
                        "description VARCHAR(512) NOT NULL," +
                        "FOREIGN KEY (id) REFERENCES entity(id)" +
                ")",
                "CREATE TABLE contact (" +
                        "id INTEGER PRIMARY KEY NOT NULL," +
                        "first_name VARCHAR(256) NOT NULL," +
                        "last_name VARCHAR(256) NOT NULL," +
                        "email VARCHAR(256) NOT NULL," +
                        "FOREIGN KEY (id) REFERENCES entity(id)" +
                ")"
        };
        Connection conn = this.dbManager.getConnection();
        Statement stmt = DBManager.statement(conn);
        try {
            for (String sql: sqls) {
                stmt.execute(sql);
            }
        }
        catch (SQLException ex){
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }
        return res;
    }


    // 往数据库中添加 PIMEntity
    public Response addEntity(PIMEntity entity) {
        if (entity == null) {
            return new Response(400);
        }

        Connection conn = this.dbManager.getConnection();

        int lastId = -1;
        try {
            lastId = this.getLastId(conn);
        }
        catch (RuntimeException ex) {
            return new Response(500);
        }

        int type = 0;
        if (entity instanceof PIMTodo) {
            type = 1;
        }
        else if (entity instanceof PIMNote) {
            type = 2;
        }
        else if (entity instanceof PIMAppointment) {
            type = 3;
        }
        else if (entity instanceof PIMContact) {
            type = 4;
        }
        else {
            return new Response(400, "unknown entity type.");
        }


        PreparedStatement insertEntityStmt = null;
        try {
            insertEntityStmt = DBManager.statement(conn, "INSERT INTO entity VALUE (?, ?, ?, ?, ?, ?)");
            insertEntityStmt.setInt(1, lastId + 1);
            insertEntityStmt.setString(2, DataAPI.defaultDateFormat.format(new Date()));
            insertEntityStmt.setInt(3, type);
            insertEntityStmt.setString(4, entity.getPriority() == null ? "" : entity.getPriority());
            insertEntityStmt.setString(5, entity.getUser().getEmail());
            insertEntityStmt.setBoolean(6, entity.isShareable());
            insertEntityStmt.execute();
            DBManager.closeStatement(insertEntityStmt);

            switch (type) {
                case 1:
                    insertEntityStmt = DBManager.statement(conn, "INSERT INTO todo VALUE (?, ?, ?)");
                    insertEntityStmt.setString(2, DataAPI.defaultDateFormat.format(((PIMTodo) entity).getDate()));
                    insertEntityStmt.setString(3, ((PIMTodo) entity).getText());
                    break;
                case 2:
                    insertEntityStmt = DBManager.statement(conn, "INSERT INTO note VALUE (?, ?)");
                    insertEntityStmt.setString(2, ((PIMNote) entity).getText());
                    break;
                case 3:
                    insertEntityStmt = DBManager.statement(conn, "INSERT INTO appointment VALUE (?, ?, ?)");
                    insertEntityStmt.setString(2, DataAPI.defaultDateFormat.format(((PIMAppointment) entity).getDate()));
                    insertEntityStmt.setString(3, ((PIMAppointment) entity).getDesc());
                    break;
                case 4:
                    insertEntityStmt = DBManager.statement(conn, "INSERT INTO contact VALUE (?, ?, ?, ?)");
                    insertEntityStmt.setString(2, ((PIMContact) entity).getFirstName());
                    insertEntityStmt.setString(3, ((PIMContact) entity).getLastName());
                    insertEntityStmt.setString(4, ((PIMContact) entity).getEmail());
                    break;
                default:
                    return new Response(500);
            }
            insertEntityStmt.setInt(1, lastId + 1);
            insertEntityStmt.execute();
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
        }
        finally {
            DBManager.closeStatement(insertEntityStmt);
            this.dbManager.returnConnection(conn);
        }

        return new Response(200);
    }


    // 获取 PIMEntity
    // 获取全部 PIMEntity
    public Response getEntities(User user) {
        Connection conn = this.dbManager.getConnection();

        int lastId = -1;
        try {
            lastId = this.getLastId(conn);
        }
        catch (RuntimeException ex) {
            return new Response(500);
        }
        if (lastId < 0) {
            return new Response(500);
        }
        else if (lastId == 0) {
            return new Response(200, "ok", new PIMEntity[0]);
        }

        ArrayList<PIMEntity> resEntities = new ArrayList<PIMEntity>();

        PreparedStatement getEntitiesStmt = DBManager.statement(conn,
                "SELECT *" +
                "FROM " +
                "  entity " +
                "  LEFT JOIN user ON entity.user_id = user.email" +
                "  LEFT JOIN todo ON entity.id = todo.id " +
                "  LEFT JOIN note ON entity.id = note.id " +
                "  LEFT JOIN appointment ON entity.id = appointment.id " +
                "  LEFT JOIN contact ON entity.id = contact.id " +
                "WHERE entity.user_id = ? OR entity.sharable = true "+
                "ORDER BY entity.id ASC"
        );
        try {
            getEntitiesStmt.setString(1, user.getEmail());
            ResultSet rs = getEntitiesStmt.executeQuery();
            boolean flag = rs.first();
            while (flag) {
                int type = rs.getInt("entity.type");
                try {
                    switch (type) {
                        case 1:
                            resEntities.add(new PIMTodo(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("todo.date")),
                                    rs.getString("todo.text")
                            ));
                            break;
                        case 2:
                            resEntities.add(new PIMNote(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    rs.getString("note.text")
                            ));
                            break;
                        case 3:
                            resEntities.add(new PIMAppointment(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("appointment.date")),
                                    rs.getString("appointment.description")
                            ));
                            break;
                        case 4:
                            resEntities.add(new PIMContact(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    rs.getString("contact.first_name"),
                                    rs.getString("contact.last_name"),
                                    rs.getString("contact.email")
                            ));
                            break;
                        default:
                            return new Response(500, "unknown entity type");
                    }
                }
                catch (ParseException ex) {
                    ex.printStackTrace();
                    flag = rs.next();
                    continue;
                }

                flag = rs.next();
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(getEntitiesStmt);
            this.dbManager.returnConnection(conn);
        }

        return new Response(200, "ok", resEntities.toArray(new PIMEntity[0]));
    }
    // 获取某个类型的 PIMEntity
    public Response getEntities(String entityType, User user) {
        switch (entityType) {
            case "todo":
                return getTodos(user);
            case "note":
                return getNotes(user);
            case "appointment":
                return getAppointments(user);
            case "contact":
                return getContacts(user);
            default:
                return new Response(400, "unknown entity type");
        }
    }
    private Response getTodos(User user) {
        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = DBManager.statement(conn,
                "SELECT * " +
                "FROM todo " +
                "LEFT JOIN entity ON todo.id = entity.id " +
                "LEFT JOIN user ON entity.user_id = user.email " +
                "WHERE entity.user_id = ? OR entity.sharable = true " +
                "ORDER BY todo.id ASC"
        );

        ArrayList<PIMTodo> resEntities = new ArrayList<PIMTodo>();
        try {
            stmt.setString(1, user.getEmail());
            ResultSet rs = stmt.executeQuery();

            boolean flag = rs.first();
            while (flag) {
                try {
                    resEntities.add(new PIMTodo(
                            rs.getInt("entity.id"),
                            new User(rs.getString("user.email"), rs.getString("user.name"), null),
                            rs.getBoolean("entity.sharable"),
                            rs.getString("entity.priority"),
                            DataAPI.defaultDateFormat.parse(rs.getString("todo.date")),
                            rs.getString("todo.text")
                    ));
                }
                catch (ParseException ex) {
                    ex.printStackTrace();
                    flag = rs.next();
                    continue;
                }
                flag = rs.next();
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }

        return new Response(200, "ok", resEntities.toArray(new PIMTodo[0]));
    }
    private Response getNotes(User user) {
        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = DBManager.statement(conn,
                "SELECT * " +
                "FROM note " +
                "LEFT JOIN entity ON note.id = entity.id " +
                "LEFT JOIN user ON entity.user_id = user.email " +
                "WHERE entity.user_id = ? OR entity.sharable = true " +
                "ORDER BY note.id ASC"
        );

        ArrayList<PIMNote> resEntities = new ArrayList<PIMNote>();
        try {
            stmt.setString(1, user.getEmail());
            ResultSet rs = stmt.executeQuery();

            boolean flag = rs.first();
            while (flag) {
                resEntities.add(new PIMNote(
                        rs.getInt("entity.id"),
                        new User(rs.getString("user.email"), rs.getString("user.name"), null),
                        rs.getBoolean("entity.sharable"),
                        rs.getString("entity.priority"),
                        rs.getString("note.text")
                ));
                flag = rs.next();
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }

        return new Response(200, "ok", resEntities.toArray(new PIMNote[0]));
    }
    private Response getAppointments(User user) {
        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = DBManager.statement(conn,
                "SELECT * " +
                "FROM appointment " +
                "LEFT JOIN entity ON appointment.id = entity.id " +
                "LEFT JOIN user ON entity.user_id = user.email " +
                "WHERE entity.user_id = ? OR entity.sharable = true " +
                "ORDER BY appointment.id ASC"
        );

        ArrayList<PIMAppointment> resEntities = new ArrayList<PIMAppointment>();
        try {
            stmt.setString(1, user.getEmail());
            ResultSet rs = stmt.executeQuery();

            boolean flag = rs.first();
            while (flag) {
                try {
                    resEntities.add(new PIMAppointment(
                            rs.getInt("entity.id"),
                            new User(rs.getString("user.email"), rs.getString("user.name"), null),
                            rs.getBoolean("entity.sharable"),
                            rs.getString("entity.priority"),
                            DataAPI.defaultDateFormat.parse(rs.getString("appointment.date")),
                            rs.getString("appointment.description")
                    ));
                }
                catch (ParseException ex) {
                    ex.printStackTrace();
                    flag = rs.next();
                    continue;
                }
                flag = rs.next();
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }

        return new Response(200, "ok", resEntities.toArray(new PIMAppointment[0]));
    }
    private Response getContacts(User user) {
        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = DBManager.statement(conn,
                "SELECT * " +
                "FROM contact " +
                "LEFT JOIN entity ON contact.id = entity.id " +
                "LEFT JOIN user ON entity.user_id = user.email " +
                "WHERE entity.user_id = ? OR entity.sharable = true " +
                "ORDER BY contact.id ASC"
        );

        ArrayList<PIMContact> resEntities = new ArrayList<PIMContact>();
        try {
            stmt.setString(1, user.getEmail());
            ResultSet rs = stmt.executeQuery();

            boolean flag = rs.first();
            while (flag) {
                resEntities.add(new PIMContact(
                        rs.getInt("entity.id"),
                        new User(rs.getString("user.email"), rs.getString("user.name"), null),
                        rs.getBoolean("entity.sharable"),
                        rs.getString("entity.priority"),
                        rs.getString("contact.first_name"),
                        rs.getString("contact.last_name"),
                        rs.getString("contact.email")
                ));
                flag = rs.next();
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }

        return new Response(200, "ok", resEntities.toArray(new PIMContact[0]));
    }
    // 获取某天的 PIMEntity
    public Response getEntities(Date sDate, Date eDate, User user) {
        if (sDate == null) {
            return new Response(400);
        }
        if (eDate == null) {
            eDate = sDate;
        }

        Connection conn = this.dbManager.getConnection();

        ArrayList<PIMEntity> resEntities = new ArrayList<PIMEntity>();
        PreparedStatement getEntitiesStmt = DBManager.statement(conn,
                "SELECT *" +
                "FROM " +
                "  entity " +
                "  LEFT JOIN user ON entity.user_id = user.email " +
                "  LEFT JOIN todo ON entity.id = todo.id " +
                "  LEFT JOIN note ON entity.id = note.id " +
                "  LEFT JOIN appointment ON entity.id = appointment.id " +
                "  LEFT JOIN contact ON entity.id = contact.id " +
                "WHERE" +
                "  ((entity.type = 1 AND todo.date >= ? AND todo.date <= ?) OR" +
                "  (entity.type = 3 AND appointment.date >= ? AND appointment.date <= ?) OR" +
                "  (entity.type IN (2, 4) AND entity.create_time >= ? AND entity.create_time <= ?)) AND" +
                "  (entity.user_id = ? OR entity.sharable = true)" +
                "ORDER BY entity.id ASC");
        try {
            String startDate = (new SimpleDateFormat("yyyy-MM-dd")).format(sDate) + " 00:00:00";
            String endDate = (new SimpleDateFormat("yyyy-MM-dd")).format(eDate) + " 23:59:59";
            getEntitiesStmt.setString(1, startDate);
            getEntitiesStmt.setString(3, startDate);
            getEntitiesStmt.setString(5, startDate);
            getEntitiesStmt.setString(2, endDate);
            getEntitiesStmt.setString(4, endDate);
            getEntitiesStmt.setString(6, endDate);
            getEntitiesStmt.setString(7, user.getEmail());
            ResultSet rs = getEntitiesStmt.executeQuery();

            boolean flag = rs.first();
            while (flag) {
                int type = rs.getInt("entity.type");
                try {
                    switch (type) {
                        case 1:
                            resEntities.add(new PIMTodo(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("todo.date")),
                                    rs.getString("todo.text")
                            ));
                            break;
                        case 2:
                            resEntities.add(new PIMNote(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    rs.getString("note.text")
                            ));
                            break;
                        case 3:
                            resEntities.add(new PIMAppointment(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("appointment.date")),
                                    rs.getString("appointment.description")
                            ));
                            break;
                        case 4:
                            resEntities.add(new PIMContact(
                                    rs.getInt("entity.id"),
                                    new User(rs.getString("user.email"), rs.getString("user.name"), null),
                                    rs.getBoolean("entity.sharable"),
                                    rs.getString("entity.priority"),
                                    rs.getString("contact.first_name"),
                                    rs.getString("contact.last_name"),
                                    rs.getString("contact.email")
                            ));
                            break;
                        default:
                            return new Response(500, "unknown entity type");
                    }
                }
                catch (ParseException ex) {
                    ex.printStackTrace();
                    flag = rs.next();
                    continue;
                }
                flag = rs.next();
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(getEntitiesStmt);
            this.dbManager.returnConnection(conn);
        }

        return new Response(200, "ok", resEntities.toArray(new PIMEntity[0]));
    }

    // 删除
    public Response delEntity(int id) {
        if (id < 1) {
            return new Response(400);
        }

        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = null;
        Response res = new Response();
        try {
            stmt = DBManager.statement(conn, "DELETE FROM todo WHERE id = ?");
            stmt.setInt(1, id);
            stmt.execute();
            DBManager.closeStatement(stmt);

            stmt = DBManager.statement(conn, "DELETE FROM note WHERE id = ?");
            stmt.setInt(1, id);
            stmt.execute();
            DBManager.closeStatement(stmt);

            stmt = DBManager.statement(conn, "DELETE FROM appointment WHERE id = ?");
            stmt.setInt(1, id);
            stmt.execute();
            DBManager.closeStatement(stmt);

            stmt = DBManager.statement(conn, "DELETE FROM contact WHERE id = ?");
            stmt.setInt(1, id);
            stmt.execute();
            DBManager.closeStatement(stmt);

            stmt = DBManager.statement(conn, "DELETE FROM entity WHERE id = ?");
            stmt.setInt(1, id);
            stmt.execute();
            DBManager.closeStatement(stmt);

        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }

        return res;
    }

    // 修改
    public Response editEntity(PIMEntity entity) {
        if (entity == null || entity.getId() < 1) {
            return new Response(400);
        }


        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = null;
        Response res = new Response();
        try {
            stmt = DBManager.statement(conn, "UPDATE entity SET priority = ?, sharable = ? WHERE id = ?");
            stmt.setString(1, entity.getPriority());
            stmt.setBoolean(2, entity.isShareable());
            stmt.setInt(3, entity.getId());
            stmt.execute();
            DBManager.closeStatement(stmt);

            if (entity instanceof PIMTodo) {
                stmt = DBManager.statement(conn, "UPDATE todo SET date = ?, text = ? WHERE id = ?");
                stmt.setString(1, (new SimpleDateFormat("yyyy-MM-dd")).format(((PIMTodo) entity).getDate()));
                stmt.setString(2, ((PIMTodo) entity).getText());
                stmt.setInt(3, entity.getId());
                stmt.execute();
                DBManager.closeStatement(stmt);
            }
            else if (entity instanceof PIMNote) {
                stmt = DBManager.statement(conn, "UPDATE note SET text = ? WHERE id = ?");
                stmt.setString(1, ((PIMNote) entity).getText());
                stmt.setInt(2, entity.getId());
                stmt.execute();
                DBManager.closeStatement(stmt);
            }
            else if (entity instanceof PIMAppointment) {
                stmt = DBManager.statement(conn, "UPDATE appointment SET date = ?, description = ? WHERE id = ?");
                stmt.setString(1, (new SimpleDateFormat("yyyy-MM-dd")).format(((PIMAppointment) entity).getDate()));
                stmt.setString(2, ((PIMAppointment) entity).getDesc());
                stmt.setInt(3, entity.getId());
                stmt.execute();
                DBManager.closeStatement(stmt);
            }
            else if (entity instanceof PIMContact) {
                stmt = DBManager.statement(conn, "UPDATE contact SET first_name = ?, last_name = ?, email = ? WHERE id = ?");
                stmt.setString(1, ((PIMContact) entity).getFirstName());
                stmt.setString(2, ((PIMContact) entity).getLastName());
                stmt.setString(3, ((PIMContact) entity).getEmail());
                stmt.setInt(4, entity.getId());
                stmt.execute();
                DBManager.closeStatement(stmt);
            }
            else {
                res = new Response(500);
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            res = new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }

        return res;
    }

    // 用户操作
    public Response verifyUser(User user) {
        if (user.getEmail() == null || user.getPasswd() == null) {
            return new Response(400);
        }

        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = DBManager.statement(conn, "SELECT * FROM user WHERE email = ?");
        Response res = new Response(200);
        try {
            stmt.setString(1, user.getEmail());
            ResultSet rs = stmt.executeQuery();

            if (rs.first()) {
                String realPasswd = rs.getString("passwd");
                if (realPasswd.equals(user.getPasswd())) {
                    user.setName(rs.getString("name"));
                    user.setStatus("ok");
                }
                else {
                    res = new Response(403);
                    user.setStatus("incorrect passwd");
                }
            }
            else {
                res = new Response(404);
                user.setStatus("non-existent");
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }

        return res;
    }
    public Response addUser(User user) {
        if (user.getEmail() == null || user.getName() == null || user.getPasswd() == null) {
            return new Response(400);
        }

        Connection conn = this.dbManager.getConnection();
        PreparedStatement stmt = DBManager.statement(conn, "SELECT * FROM user WHERE email = ?");
        Response res = new Response(200);
        try {
            stmt.setString(1, user.getEmail());
            ResultSet rs = stmt.executeQuery();

            if (rs.first()) {
                res = new Response(403);
            }
            else {
                DBManager.closeStatement(stmt);

                stmt = DBManager.statement(conn, "INSERT INTO user VALUE (?, ?, ?)");
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getName());
                stmt.setString(3, user.getPasswd());
                stmt.execute();
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            return new Response(500);
        }
        finally {
            DBManager.closeStatement(stmt);
            this.dbManager.returnConnection(conn);
        }
        return res;
    }


    private int getLastId(Connection conn) {
        Statement stmt = DBManager.statement(conn);
        int lastId = -1;
        try {
            ResultSet rs = stmt.executeQuery("SELECT max(id) FROM entity");
            if (rs.first()) {
                lastId = rs.getInt(1);
            }
            else {
                throw new RuntimeException("数据库访问异常");
            }
        }
        catch (SQLException ex) {
            DBManager.printEx(ex);
            throw new RuntimeException("数据库访问异常");
        }
        finally {
            DBManager.closeStatement(stmt);
        }

        return lastId;
    }


    // 常用静态属性
    private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}

package pim.model;

// package java.sql
import pim.model.entities.*;

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
                "CREATE TABLE entity (" +
                        "id INTEGER PRIMARY KEY AUTO_INCREMENT NOT NULL," +
                        "create_time DATETIME NOT NULL," +
                        "type INTEGER NOT NULL," +
                        "priority VARCHAR(128) NULL" +
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
                ")",
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
            insertEntityStmt = DBManager.statement(conn, "INSERT INTO entity VALUE (?, ?, ?, ?)");
            insertEntityStmt.setInt(1, lastId + 1);
            insertEntityStmt.setString(2, DataAPI.defaultDateFormat.format(new Date()));
            insertEntityStmt.setInt(3, type);
            insertEntityStmt.setString(4, entity.getPriority() == null ? "" : entity.getPriority());
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
    public Response getEntities() {
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

        PIMEntity[] resEntities = new PIMEntity[lastId];

        Statement getEntitiesStmt = DBManager.statement(conn);
        try {
            ResultSet rs = getEntitiesStmt.executeQuery(
                    "SELECT *" +
                    "FROM " +
                    "  entity " +
                    "  LEFT JOIN todo on entity.id = todo.id " +
                    "  LEFT JOIN note on entity.id = note.id " +
                    "  LEFT JOIN appointment on entity.id = appointment.id " +
                    "  LEFT JOIN contact on entity.id = contact.id " +
                    "ORDER BY entity.id ASC");
            boolean flag = rs.first();
            while (flag) {
                int type = rs.getInt("entity.type");
                try {
                    switch (type) {
                        case 1:
                            resEntities[rs.getInt("entity.id") - 1] = new PIMTodo(
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("todo.date")),
                                    rs.getString("todo.text")
                            );
                            break;
                        case 2:
                            resEntities[rs.getInt("entity.id") - 1] = new PIMNote(
                                    rs.getString("entity.priority"),
                                    rs.getString("note.text")
                            );
                            break;
                        case 3:
                            resEntities[rs.getInt("entity.id") - 1] = new PIMAppointment(
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("appointment.date")),
                                    rs.getString("appointment.description")
                            );
                            break;
                        case 4:
                            resEntities[rs.getInt("entity.id") - 1] = new PIMContact(
                                    rs.getString("entity.priority"),
                                    rs.getString("contact.first_name"),
                                    rs.getString("contact.last_name"),
                                    rs.getString("contact.email")
                            );
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

        return new Response(200, "ok", resEntities);
    }
    // 获取某个类型的 PIMEntity
    public Response getEntities(String entityType) {
        switch (entityType) {
            case "todo":
                return getTodos();
            case "note":
                return getNotes();
            case "appointment":
                return getAppointments();
            case "contact":
                return getContacts();
            default:
                return new Response(400, "unknown entity type");
        }
    }
    private Response getTodos() {
        Connection conn = this.dbManager.getConnection();
        Statement stmt = DBManager.statement(conn);

        ArrayList<PIMTodo> resEntities = new ArrayList<PIMTodo>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM todo JOIN entity ON todo.id = entity.id ORDER BY todo.id ASC;");

            boolean flag = rs.first();
            while (flag) {
                try {
                    resEntities.add(new PIMTodo(
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
    private Response getNotes() {
        Connection conn = this.dbManager.getConnection();
        Statement stmt = DBManager.statement(conn);

        ArrayList<PIMNote> resEntities = new ArrayList<PIMNote>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM note JOIN entity ON note.id = entity.id ORDER BY note.id ASC;");

            boolean flag = rs.first();
            while (flag) {
                resEntities.add(new PIMNote(
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
    private Response getAppointments() {
        Connection conn = this.dbManager.getConnection();
        Statement stmt = DBManager.statement(conn);

        ArrayList<PIMAppointment> resEntities = new ArrayList<PIMAppointment>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM appointment JOIN entity ON appointment.id = entity.id ORDER BY appointment.id ASC;");

            boolean flag = rs.first();
            while (flag) {
                try {
                    resEntities.add(new PIMAppointment(
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
    private Response getContacts() {
        Connection conn = this.dbManager.getConnection();
        Statement stmt = DBManager.statement(conn);

        ArrayList<PIMContact> resEntities = new ArrayList<PIMContact>();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM contact JOIN entity ON contact.id = entity.id ORDER BY contact.id ASC;");

            boolean flag = rs.first();
            while (flag) {
                resEntities.add(new PIMContact(
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
    public Response getEntities(Date date) {
        if (date == null) {
            return new Response(400);
        }

        Connection conn = this.dbManager.getConnection();

        ArrayList<PIMEntity> resEntities = new ArrayList<PIMEntity>();
        PreparedStatement getEntitiesStmt = DBManager.statement(conn,
                "SELECT *" +
                "FROM " +
                "  entity " +
                "  LEFT JOIN todo on entity.id = todo.id " +
                "  LEFT JOIN note on entity.id = note.id " +
                "  LEFT JOIN appointment on entity.id = appointment.id " +
                "  LEFT JOIN contact on entity.id = contact.id " +
                "WHERE" +
                "  (entity.type = 1 AND todo.date >= ? AND todo.date <= ?) OR" +
                "  (entity.type = 3 AND appointment.date >= ? AND appointment.date <= ?) OR" +
                "  (entity.type IN (2, 4) AND entity.create_time >= ? AND entity.create_time <= ?) " +
                "ORDER BY entity.id ASC");
        try {
            String startDate = (new SimpleDateFormat("yyyy-MM-dd")).format(date) + " 00:00:00";
            String endDate = (new SimpleDateFormat("yyyy-MM-dd")).format(date) + " 23:59:59";
            getEntitiesStmt.setString(1, startDate);
            getEntitiesStmt.setString(3, startDate);
            getEntitiesStmt.setString(5, startDate);
            getEntitiesStmt.setString(2, endDate);
            getEntitiesStmt.setString(4, endDate);
            getEntitiesStmt.setString(6, endDate);
            ResultSet rs = getEntitiesStmt.executeQuery();

            boolean flag = rs.first();
            while (flag) {
                int type = rs.getInt("entity.type");
                try {
                    switch (type) {
                        case 1:
                            resEntities.add(new PIMTodo(
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("todo.date")),
                                    rs.getString("todo.text")
                            ));
                            break;
                        case 2:
                            resEntities.add(new PIMNote(
                                    rs.getString("entity.priority"),
                                    rs.getString("note.text")
                            ));
                            break;
                        case 3:
                            resEntities.add(new PIMAppointment(
                                    rs.getString("entity.priority"),
                                    DataAPI.defaultDateFormat.parse(rs.getString("appointment.date")),
                                    rs.getString("appointment.description")
                            ));
                            break;
                        case 4:
                            resEntities.add(new PIMContact(
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

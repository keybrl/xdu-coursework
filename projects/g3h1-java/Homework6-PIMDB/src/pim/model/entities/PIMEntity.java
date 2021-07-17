package pim.model.entities;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;


/**
 * PIM信息
 * PIMTodo PIMNote PIMContact PIMAppointment 类的父类
 *
 * @author 罗阳豪 16130120191
 */
public abstract class PIMEntity implements Serializable {
    private String priority;  // every kind of item has a priority

    // default constructor sets priority to "normal"
    PIMEntity() {
        this.priority = "normal";
    }

    // priority can be established via this constructor.
    PIMEntity(String priority) {
        if (priority == null) {
            throw new NullPointerException("参数 `priority` 不能是 `null` ！");
        }
        this.priority = priority;
    }

    // accessor method for getting the priority string
    public String getPriority() {
        return this.priority;
    }

    // method that changes the priority string
    public void setPriority(String priority) {
        if (priority == null) {
            throw new NullPointerException("参数 `priority` 不能是 `null` ！");
        }
        this.priority = priority;
    }

    // Each pim.model.entities.PIMEntity needs to be able to set all state information
    // (fields) from a single text string.
    abstract public void setFromString(String fmtStr);

    // This is actually already defined by the super class
    // Object, but redefined here as abstract to make sure
    // that derived classes actually implement it
    abstract public String toString();

    public static Date parseDate(String fmtDateStr) {

        if (fmtDateStr == null) {
            throw new NullPointerException("参数 `fmtDateStr` 不能是 `null` ！");
        }

        SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd/yyyy");
        try {
            return dateFmt.parse(fmtDateStr);
        }
        catch (ParseException ex) {
            throw new IllegalArgumentException("参数 `fmtDateStr` 格式非法，系统只能接受 'MM/dd/yyyy' 格式的日期。");
        }
    }

    public static String formatDate(Date date) {
        if (date == null) {
            throw new NullPointerException("参数 `date` 不能是 `null` ！");
        }

        SimpleDateFormat dateFmt = new SimpleDateFormat("MM/dd/yyyy");
        return dateFmt.format(date);
    }
}

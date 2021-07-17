import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * 会面预约类
 * 记录一个会面预约信息
 *
 * @author 罗阳豪 16130120191
 */
public class PIMAppointment extends PIMEntity {

    private Date date;
    private String desc;


    PIMAppointment() {
        super();
        this.date = new Date();  // 当前时间
        this.desc = "Appointment with someone...";
    }
    PIMAppointment(String priority, Date date, String desc) {

        super(priority);

        if (date == null) {
            throw new NullPointerException("参数 `date` 不能是 `null` ！");
        }
        this.date = date;

        if (desc == null) {
            throw new NullPointerException("参数 `desc` 不能是 `null` ！");
        }
        this.desc = desc;
    }
    PIMAppointment(String fmtStr) {
        super();
        setFromString(fmtStr);
    }

    Date getDate() {
        return this.date;
    }
    String getDesc() {
        return this.desc;
    }

    public void setFromString(String fmtStr) {

        if (fmtStr == null) {
            throw new NullPointerException("参数 `fmtStr` 不能是 `null` ！");
        }

        Pattern pattern = Pattern.compile("^APPOINTMENT ([^ ]+?) ([0-9]{2}/[0-9]{2}/[0-9]{4}) (.*?)$");
        Matcher matcher = pattern.matcher(fmtStr);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("参数 `fmtStr` 格式非法！（e.g. \"APPOINTMENT urgent 04/20/2018 Submit Java homework.\"）");
        }

        this.setPriority(matcher.group(1));
        this.date = parseDate(matcher.group(2));
        this.desc = matcher.group(3);
    }
    public String toString() {
        String res = "APPOINTMENT ";
        res += this.getPriority() + " ";
        res += formatDate(this.date) + " ";
        res += this.desc;
        return res;
    }

}
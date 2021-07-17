package pim.model.orm;

import pim.generic.User;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 * 清单类
 * 记录单条的清单信息
 *
 * @author 罗阳豪 16130120191
 */
public class PIMTodo extends PIMEntity {

    private Date date;
    private String text;


    public PIMTodo(int id, User user, boolean shareable) {
        super(id, user, shareable);
        this.date = new Date();  // 当前时间
        this.text = "Todo something...";
    }
    public PIMTodo(int id, User user, boolean shareable, String priority, Date date, String text) {

        super(id, user, shareable, priority);

        if (date == null) {
            throw new NullPointerException("参数 `date` 不能是 `null` ！");
        }
        this.date = date;

        if (text == null) {
            throw new NullPointerException("参数 `text` 不能是 `null` ！");
        }
        this.text = text;
    }
    public PIMTodo(int id, User user, boolean shareable, String fmtStr) {
        super(id, user, shareable);
        setFromString(fmtStr);
    }

    public Date getDate() {
        return this.date;
    }
    public String getText() {
        return this.text;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public void setText(String text) {
        this.text = text;
    }

    public void setFromString(String fmtStr) {

        if (fmtStr == null) {
            throw new NullPointerException("参数 `fmtStr` 不能是 `null` ！");
        }

        Pattern pattern = Pattern.compile("^TODO ([^ ]+?) ([0-9]{2}/[0-9]{2}/[0-9]{4}) (.+?)$");
        Matcher matcher = pattern.matcher(fmtStr);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("参数 `fmtStr` 格式非法！（e.g. \"TODO urgent 04/20/2018 Submit Java homework.\"）");
        }

        this.setPriority(matcher.group(1));
        this.date = parseDate(matcher.group(2));
        this.text = matcher.group(3);
    }
    public String toString() {
        String res = "TODO ";
        res += this.getPriority() + " ";
        res += formatDate(this.date) + " ";
        res += this.text;
        return res;
    }

}

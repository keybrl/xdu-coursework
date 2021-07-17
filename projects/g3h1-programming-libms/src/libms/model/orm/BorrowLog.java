package libms.model.orm;

import java.util.Date;


/**
 * 借还记录类
 * 对应数据库的 `borrow_log` 表
 *
 * @author keybrl
 */
public class BorrowLog extends Model {
    public String seq;
    public Date dateTime;
    public String type;
    public Book book;
    public User user;

    public BorrowLog(String seq, Date dateTime, String type, Book book, User user) {
        if (null == seq) {
            throw new Model.NullPrimaryKey("参数 `seq` 不能是 `null` ！");
        }
        this.seq = seq;

        if (null == dateTime) {
            throw new Model.UnexpectedtNull("参数 `dateTime` 不能是 `null` ！");
        }
        this.dateTime = dateTime;

        if (null == type) {
            throw new Model.UnexpectedtNull("参数 `type` 不能是 `null` ！");
        }
        if (type.equals("borrow") || type.equals("return")) {
            this.type = type;
        }
        else {
            throw new Model.UnexpectedValue("参数 `type` 的值只能是 `\"borrow\"` 或 `\"return\"` ！");
        }

        if (null == book) {
            throw new Model.UnexpectedtNull("参数 `book` 不能是 `null` ！");
        }
        this.book = book;

        if (null == user) {
            throw new Model.UnexpectedtNull("参数 `user` 不能是 `null` ！");
        }
        this.user = user;
    }
}

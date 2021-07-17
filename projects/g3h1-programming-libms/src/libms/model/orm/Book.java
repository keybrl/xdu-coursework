package libms.model.orm;


/**
 * 书本类
 * 对应数据库的 `book` 表。
 *
 * @author keybrl
 */
public class Book extends Model {
    public String id;
    public BookInfo info;
    public User holder;


    public Book(String id, BookInfo info, User holder) {
        if (null == id) {
            throw new NullPrimaryKey("参数 `id` 不能是 `null` ！");
        }
        this.id = id;

        if (null == info) {
            throw new UnexpectedtNull("参数 `info` 不能是 `null` ！");
        }
        this.info = info;

        this.holder = holder;
    }
}

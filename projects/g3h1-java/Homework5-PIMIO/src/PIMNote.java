import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;


/**
 * 便签类
 * 记录单条便签内容
 *
 * @author 罗阳豪 16130120191
 */
public class PIMNote extends PIMEntity implements PIMDate {

    private Date date;
    private String text;


    PIMNote() {
        super();
        this.text = "Note something...";
        this.date = new Date();
    }
    PIMNote(String priority, String text) {

        super(priority);

        if (text == null) {
            throw new NullPointerException("参数 `text` 不能是 `null` ！");
        }
        this.text = text;
        this.date = new Date();
    }
    PIMNote(String fmtStr) {
        super();
        setFromString(fmtStr);
        this.date = new Date();
    }

    String getText() {
        return this.text;
    }

    public void setFromString(String fmtStr) {
        if (fmtStr == null) {
            throw new NullPointerException("参数 `fmtStr` 不能是 `null` ！");
        }

        Pattern pattern = Pattern.compile("^NOTE ([^ ]+?) ([0-9]{2}/[0-9]{2}/[0-9]{4}) (.+?)$");
        Matcher matcher = pattern.matcher(fmtStr);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "参数 `fmtStr` 格式非法！（e.g. \"NOTE normal 11/30/2018 “真程序员” 通常具备工程学和物理学背景，并常常\\n" +
                    "是业余无线电爱好者。他们穿着白色袜子，涤纶衬衫，打\\n着领带，戴着厚厚的眼睛，使用机器语言，汇编语言，\\n" +
                    "FORTRAN或者其他一些已经被人们遗忘了的古老的编程语言。"
            );
        }

        this.setPriority(matcher.group(1));
        this.date = parseDate(matcher.group(2));
        this.text = matcher.group(3);
    }
    public String toString() {
        String res = "NOTE ";
        res += this.getPriority() + " ";
        res += formatDate(this.date) + " ";
        res += this.text;
        return res;
    }

    public Date getDate() {
        return this.date;
    }

}

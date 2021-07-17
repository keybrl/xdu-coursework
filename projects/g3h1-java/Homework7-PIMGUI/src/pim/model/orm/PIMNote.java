package pim.model.orm;

import pim.generic.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 便签类
 * 记录单条便签内容
 *
 * @author 罗阳豪 16130120191
 */
public class PIMNote extends PIMEntity {

    private String text;


    PIMNote(int id, User user, boolean shareable) {
        super(id, user, shareable);
        this.text = "Note something...";
    }
    public PIMNote(int id, User user, boolean shareable, String priority, String text) {

        super(id, user, shareable, priority);

        if (text == null) {
            throw new NullPointerException("参数 `text` 不能是 `null` ！");
        }
        this.text = text;
    }
    PIMNote(int id, User user, boolean shareable, String fmtStr) {
        super(id, user, shareable);
        setFromString(fmtStr);
    }

    public String getText() {
        return this.text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public void setFromString(String fmtStr) {
        if (fmtStr == null) {
            throw new NullPointerException("参数 `fmtStr` 不能是 `null` ！");
        }

        Pattern pattern = Pattern.compile("^NOTE ([^ ]+?) (.+?)$");
        Matcher matcher = pattern.matcher(fmtStr);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "参数 `fmtStr` 格式非法！（e.g. \"NOTE normal “真程序员” 通常具备工程学和物理学背景，并常常\\n" +
                    "是业余无线电爱好者。他们穿着白色袜子，涤纶衬衫，打\\n着领带，戴着厚厚的眼睛，使用机器语言，汇编语言，\\n" +
                    "FORTRAN或者其他一些已经被人们遗忘了的古老的编程语言。"
            );
        }

        this.setPriority(matcher.group(1));
        this.text = matcher.group(2);
    }
    public String toString() {
        String res = "NOTE ";
        res += this.getPriority() + " ";
        res += this.text;
        return res;
    }

}

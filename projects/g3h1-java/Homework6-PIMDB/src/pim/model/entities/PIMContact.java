package pim.model.entities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;


/**
 * 通讯录类
 * 记录单条通讯录信息
 *
 * @author 罗阳豪 16130120191
 */
public class PIMContact extends PIMEntity {

    private String firstName;
    private String lastName;
    private String email;


    PIMContact() {
        super();
        this.firstName = "WHO";
        this.lastName = "AreYou";
        this.email = "someone@example.com";
    }
    public PIMContact(String priority, String firstName, String lastName, String email) {
        super(priority);
        this.firstName = firstName == null ? "" : firstName;
        this.lastName = lastName == null ? "" : lastName;
        this.email = email == null ? "" : email;
    }
    PIMContact(String fmtStr) {
        super();
        setFromString(fmtStr);
    }

    public String getFirstName() {
        return this.firstName;
    }
    public String getLastName() {
        return this.lastName;
    }
    public String getEmail() {
        return this.email;
    }

    public void setFromString(String fmtStr) {
        if (fmtStr == null) {
            throw new NullPointerException("参数 `fmtStr` 不能是 `null` ！");
        }

        Pattern pattern = Pattern.compile("^CONTACT ([^ ]+?) first-name:(.*?) last-name:(.*?) emaIl:(.*?)$");
        Matcher matcher = pattern.matcher(fmtStr);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("参数 `fmtStr` 格式非法！（e.g. \"CONTACT normal first-name:Yang-hao last-name:LUO emaIl:keyboard-l@outlook.com\"");
        }

        this.setPriority(matcher.group(1));
        this.firstName = matcher.group(2);
        this.lastName = matcher.group(3);
        this.email = matcher.group(4);
    }
    public String toString() {
        String res = "CONTACT ";
        res += this.getPriority() + " first-name:";
        res += this.firstName + " last-name:";
        res += this.lastName + " emaIl:";
        res += this.email;
        return res;
    }
}

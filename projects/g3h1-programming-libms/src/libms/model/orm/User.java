package libms.model.orm;

import java.math.BigInteger;
import java.security.MessageDigest;


/**
 * 用户类
 * 对应数据库的 `user` 表
 *
 * @author keybrl
 */
public class User extends Model {
    public String id;
    public String password;
    public String type;
    public String name;
    public String phoneNum;
    public String email;
    public String unit;
    public String address;


    public User(String id, String password, String type, String name, String phoneNum, String email, String unit, String address) {
        if (null == id) {
            throw new NullPrimaryKey("参数 `id` 不允许为 `null` ！");
        }
        this.id = id;

        if (null == password) {
            throw new UnexpectedtNull("参数 `password` 不允许为 `null` ！");
        }
        this.password = password;

        if (null == type) {
            throw new UnexpectedtNull("参数 `type` 不允许为 `null` ！");
        }
        if (User.checkType(type)) {
            this.type = type;
        }
        else {
            throw new UnexpectedValue("参数 `type` 的值只能是 `\"admin\"` 或 `\"user\"` ！");
        }

        if (null == name) {
            throw new UnexpectedtNull("参数 `name` 不允许为 `null` ！");
        }
        this.name = name;

        this.phoneNum = null != phoneNum ? phoneNum : "";
        this.email = null != email ? email : "";
        this.unit = null != unit ? unit : "";
        this.address = null != address ? address : "";
    }

    public static boolean checkPassword(String password) {
        return password != null && !password.equals("");
    }
    public static boolean checkType(String type) {
        return type != null && (type.equals("admin") || type.equals("user"));
    }
    public static boolean checkName(String name) {
        return name != null && name.length() < 256 && name.length() != 0;
    }
    public static boolean checkPhoneNum(String phoneNum) {
        return phoneNum != null && phoneNum.length() < 32;
    }
    public static boolean checkEmail(String email) {
        return email != null && email.length() < 256;
    }
    public static boolean checkUnit(String unit) {
        return unit != null && unit.length() < 256;
    }
    public static boolean checkAddress(String address) {
        return address != null && address.length() < 256;
    }
    public static String md5(String source) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(source.getBytes());
            return new BigInteger(1, messageDigest.digest()).toString(16);
        }
        catch (Exception exc) {
            return null;
        }
    }
}

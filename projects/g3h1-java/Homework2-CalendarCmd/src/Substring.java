import java.util.regex.Pattern;


/**
 * 切词
 *
 * @author 罗阳豪 16130120191
 */
public class Substring {
    public static void main(String[] args) {
        // 检查参数数目是否为 3
        if (3 != args.length) {
            System.out.println("参数数目错误，只允许输入 3 个命令行参数！");
            return;
        }

        // 第 2 3 个参数必须是非负整数
        if (!Pattern.matches("^[1-9]\\d*|0$", args[1])) {
            System.out.println("非法参数，第 2 个命令行参数必须是整数！");
            return;
        }
        if (!Pattern.matches("^[1-9]\\d*|0$", args[2])) {
            System.out.println("非法参数，第 3 个命令行参数必须是整数！");
            return;
        }

        try{
            Substring subString = new Substring(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            System.out.println(subString.toString());
        }
        catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    String target;
    int start;
    int length;
    Substring(String target, int start, int length) {
        if (null != target) {
            this.target = target;
        }
        else {
            throw new NullPointerException("参数 `target` 不能是null");
        }

        if (start >= target.length()) {
            throw new IllegalArgumentException("参数 `start` 的值不能超过 `target` 的长度");
        }
        this.start = start;

        if (start + length > target.length()) {
            throw new IllegalArgumentException("截取的部分不能超过 `target` 的长度");
        }
        this.length = length;
    }
    public String toString() {
        return this.target.substring(start, start + length);
    }
}

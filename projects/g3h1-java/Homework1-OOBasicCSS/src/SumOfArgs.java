/**
 * 读取命令行参数中所有的整数，打印其和
 */
public class SumOfArgs {
    public static void main(String[] args) {
        int sum = 0;
        String digits = "1234567890";
        for (String arg: args) {
            // 判断是否为整数
            boolean is_num = true;
            char[] arg_char_arr = arg.toCharArray();
            for (char arg_ch: arg_char_arr) {
                if (digits.indexOf(arg_ch) != -1) {
                    continue;
                }
                is_num = false;
                break;
            }
            if (!is_num) {
                continue;
            }

            // 加
            sum += Integer.valueOf(arg);
        }
        // 输出
        System.out.println(sum);
    }
}

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Date;


/**
 * 日历类
 * 打印某年某月的日历
 *
 * @author 罗阳豪 16130120191
 */
public class Cal {
    public static void main(String[] args) {
        if (0 == args.length) {
            Calendar now = Calendar.getInstance();
            Cal cal = new Cal(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1);
            cal.print();
            return;
        }

        if (2 != args.length) {
            System.out.println("非法参数，命令行参数必须是 2 个或 0 个");
            return;
        }

        if (!Pattern.matches("^[1-9]\\d*$", args[0])) {
            System.out.println("非法参数，第 1 个命令行参数必须是正整数");
            return;
        }

        if (Integer.parseInt(args[0]) < 1 || Integer.parseInt(args[0]) > 12) {
            System.out.println("非法参数，第 1 个命令行参数必须是合法的月份（1-12）");
            return;
        }

        if (!Pattern.matches("^-?[1-9]\\d*$", args[1])) {
            System.out.println("非法参数，第 2 个命令行参数必须是整数");
            return;
        }

        Cal cal = new Cal(Integer.parseInt(args[1]), Integer.parseInt(args[0]));
        cal.print();
    }

    private int year;
    private int month;
    private Cal(int year, int month) {
        this.year = year;
        this.month = month;
    }

    private void print() {
        Calendar cal = Calendar.getInstance();
        cal.set(this.year, this.month - 1, 1);

        System.out.printf("%s %d\n", cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH), cal.get(Calendar.YEAR));
        System.out.println("Su Mo Tu We Th Fr Sa");


        // 打印第一周的前导空格
        for (int i = 0; i < cal.get(Calendar.DAY_OF_WEEK) - 1; i++) {
            System.out.print("   ");
        }
        while (cal.get(Calendar.MONTH) + 1 == this.month) {
            System.out.printf("%2d ", cal.get(Calendar.DAY_OF_MONTH));

            // 如果打印完周六，就换行
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                System.out.println();
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        System.out.println();
    }
}

package pim.controller;

import java.util.Calendar;


/**
 * 日历类
 * 获取某年某月的日历
 *
 * @author 罗阳豪 16130120191
 */
public class Cal {
    private Calendar calendar;

    public Cal() {
        this.calendar = Calendar.getInstance();
    }

    public void toLastMonth() {
        this.calendar.add(Calendar.MONTH, -1);
    }
    public void toNextMonth() {
        this.calendar.add(Calendar.MONTH, 1);
    }

    public void setDate(int year, int month) {
        this.calendar.set(year, month - 1, 1);
    }
    public Calendar getDate() {
        return this.calendar;
    }


    public CalItem[][] getItems() {

        CalItem[][] res = new CalItem[6][7];
        Calendar cal = (Calendar) this.calendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        // 回退到1号所在周的周日
        cal.add(Calendar.DAY_OF_YEAR, 1 - cal.get(Calendar.DAY_OF_WEEK));

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                res[row][col] = new CalItem(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) == this.calendar.get(Calendar.MONTH));
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        return res;
    }

    public static class CalItem {
        public int day;
        public boolean able;

        private CalItem(int day, boolean able)  {
            this.day = day;
            this.able = able;
        }
    }
}

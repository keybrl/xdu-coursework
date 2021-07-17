package pim.views.gui.main;

import pim.controller.Cal;
import pim.views.gui.generic.Colors;
import pim.views.gui.generic.Fonts;

import javax.swing.*;
import java.util.Calendar;

class CalTitlePanel extends JPanel {

    private JLabel yearLabel;
    private JLabel monthLabel;
    private JLabel[] weekDayLabel;

    CalTitlePanel() {
        this.setLayout(null);
        this.setBackground(Colors.DEFAULT_BG);

        this.yearLabel = new JLabel("", JLabel.RIGHT);
        this.monthLabel = new JLabel("", JLabel.LEFT);

        this.weekDayLabel = new JLabel[]{
                new JLabel("日", JLabel.LEFT),
                new JLabel("一", JLabel.LEFT),
                new JLabel("二", JLabel.LEFT),
                new JLabel("三", JLabel.LEFT),
                new JLabel("四", JLabel.LEFT),
                new JLabel("五", JLabel.LEFT),
                new JLabel("六", JLabel.LEFT),
        };

        this.yearLabel.setFont(Fonts.CAL_TITLE_YEAR);
        this.monthLabel.setFont(Fonts.CAL_TITLE_MONTH);
        this.weekDayLabel[0].setFont(Fonts.CAL_TITLE_WEEKDAY);
        this.weekDayLabel[1].setFont(Fonts.CAL_TITLE_WEEKDAY);
        this.weekDayLabel[2].setFont(Fonts.CAL_TITLE_WEEKDAY);
        this.weekDayLabel[3].setFont(Fonts.CAL_TITLE_WEEKDAY);
        this.weekDayLabel[4].setFont(Fonts.CAL_TITLE_WEEKDAY);
        this.weekDayLabel[5].setFont(Fonts.CAL_TITLE_WEEKDAY);
        this.weekDayLabel[6].setFont(Fonts.CAL_TITLE_WEEKDAY);


        this.yearLabel.setForeground(Colors.CAL_TITLE_YEAR_F);
        this.monthLabel.setForeground(Colors.CAL_TITLE_MONTH_F);
        this.weekDayLabel[0].setForeground(Colors.CAL_TITLE_WEEKDAY_F);
        this.weekDayLabel[1].setForeground(Colors.CAL_TITLE_WEEKDAY_F);
        this.weekDayLabel[2].setForeground(Colors.CAL_TITLE_WEEKDAY_F);
        this.weekDayLabel[3].setForeground(Colors.CAL_TITLE_WEEKDAY_F);
        this.weekDayLabel[4].setForeground(Colors.CAL_TITLE_WEEKDAY_F);
        this.weekDayLabel[5].setForeground(Colors.CAL_TITLE_WEEKDAY_F);
        this.weekDayLabel[6].setForeground(Colors.CAL_TITLE_WEEKDAY_F);

        this.autoResize();

        this.add(this.yearLabel);
        this.add(this.monthLabel);
        this.add(this.weekDayLabel[0]);
        this.add(this.weekDayLabel[1]);
        this.add(this.weekDayLabel[2]);
        this.add(this.weekDayLabel[3]);
        this.add(this.weekDayLabel[4]);
        this.add(this.weekDayLabel[5]);
        this.add(this.weekDayLabel[6]);

    }

    void setDate(Cal cal) {
        int year = cal.getDate().get(Calendar.YEAR);
        int month = cal.getDate().get(Calendar.MONTH) + 1;

        this.yearLabel.setText(Integer.toString(year));
        switch (month) {
            case 1:
                this.monthLabel.setText("一月");
                break;
            case 2:
                this.monthLabel.setText("二月");
                break;
            case 3:
                this.monthLabel.setText("三月");
                break;
            case 4:
                this.monthLabel.setText("四月");
                break;
            case 5:
                this.monthLabel.setText("五月");
                break;
            case 6:
                this.monthLabel.setText("六月");
                break;
            case 7:
                this.monthLabel.setText("七月");
                break;
            case 8:
                this.monthLabel.setText("八月");
                break;
            case 9:
                this.monthLabel.setText("九月");
                break;
            case 10:
                this.monthLabel.setText("十月");
                break;
            case 11:
                this.monthLabel.setText("十一月");
                break;
            case 12:
                this.monthLabel.setText("十二月");
                break;
        }
    }


    void autoResize() {
        int width = this.getWidth();
        this.monthLabel.setBounds(12, 12, 100, 32);
        this.yearLabel.setBounds(width - 112, 12, 100, 32);

        int weekDayLabelWidth = width / 7;
        for (int i = 0; i < 7; i++) {
            this.weekDayLabel[i].setBounds(weekDayLabelWidth * i + 7, 51, 64, 32);
        }
    }
}

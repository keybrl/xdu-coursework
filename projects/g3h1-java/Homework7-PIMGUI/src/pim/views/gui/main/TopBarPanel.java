package pim.views.gui.main;

import pim.controller.Cal;
import pim.views.gui.generic.Colors;
import pim.views.gui.generic.ElemStatus;
import pim.views.gui.generic.TopBarBtn;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class TopBarPanel extends JPanel {

    // 父元素
    private MainFrame mainFrame;

    // 左侧按钮
    private TopBarBtn toTodayBtn;
    private TopBarBtn toLastMonthBtn;
    private TopBarBtn toNextMonthBtn;
    private TopBarBtn toAnyMonthBtn;

    // 右侧按钮
    private TopBarBtn pimBtn;


    TopBarPanel(MainFrame frame) {
        this.mainFrame = frame;
        this.setBackground(Colors.TOP_BAR_BG);
        this.setLayout(null);

        this.toTodayBtn = new TopBarBtn("今天");
        this.toLastMonthBtn = new TopBarBtn("<");
        this.toNextMonthBtn = new TopBarBtn(">");
        this.toAnyMonthBtn = new TopBarBtn("前往");
        this.pimBtn = new TopBarBtn("个人中心");

        this.toTodayBtn.setBounds(8, TopBarBtn.top, 58, TopBarBtn.height);
        this.toLastMonthBtn.setBounds(74, TopBarBtn.top, 33, TopBarBtn.height);
        this.toNextMonthBtn.setBounds(106, TopBarBtn.top, 33, TopBarBtn.height);
        this.toAnyMonthBtn.setBounds(147, TopBarBtn.top, 58, TopBarBtn.height);

        autoResize();

        TopBarListener actionListener = new TopBarListener(this);
        this.toTodayBtn.addActionListener(actionListener);
        this.toLastMonthBtn.addActionListener(actionListener);
        this.toNextMonthBtn.addActionListener(actionListener);
        this.toAnyMonthBtn.addActionListener(actionListener);
        this.pimBtn.addActionListener(actionListener);

        this.add(this.toTodayBtn);
        this.add(this.toLastMonthBtn);
        this.add(this.toNextMonthBtn);
        this.add(this.toAnyMonthBtn);
        this.add(this.pimBtn);
    }

    void autoResize() {
        this.pimBtn.setBounds(this.getWidth() - 104, TopBarBtn.top, 96, TopBarBtn.height);
    }

    private static class TopBarListener implements ActionListener {

        private TopBarPanel panel;
        TopBarListener(TopBarPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object eventSource = e.getSource();
            if (eventSource == this.panel.toTodayBtn) {
                this.panel.mainFrame.setDate(new Cal());
            }
            else if (eventSource == this.panel.toLastMonthBtn) {
                Cal cal = this.panel.mainFrame.getDate();
                cal.toLastMonth();
                this.panel.mainFrame.setDate(cal);
            }
            else if (eventSource == this.panel.toNextMonthBtn) {
                Cal cal = this.panel.mainFrame.getDate();
                cal.toNextMonth();
                this.panel.mainFrame.setDate(cal);
            }
            else if (eventSource == this.panel.toAnyMonthBtn) {
                Pattern pattern = Pattern.compile("^([0-9]+?)-([0-9]{2})$");

                while (true) {
                    String inputText = JOptionPane.showInputDialog(
                            this.panel.mainFrame, "请输入需要跳转到的年月 (格式： yyyy-MM) ：",
                            "输入目的日期", JOptionPane.QUESTION_MESSAGE
                    );
                    if (inputText == null) {
                        return;
                    }

                    Matcher matcher = pattern.matcher(inputText);
                    if (!matcher.matches()) {
                        JOptionPane.showMessageDialog(
                                this.panel.mainFrame, "输入格式不正确！",
                                "格式错误", JOptionPane.ERROR_MESSAGE
                        );
                        continue;
                    }

                    int year = Integer.parseInt(matcher.group(1));
                    int month = Integer.parseInt(matcher.group(2));

                    if (month < 1 || month > 12) {
                        JOptionPane.showMessageDialog(
                                this.panel.mainFrame, "月份只能取值 1-12 。",
                                "格式错误", JOptionPane.ERROR_MESSAGE
                        );
                        continue;
                    }

                    Cal cal = this.panel.mainFrame.getDate();
                    cal.setDate(year, month);
                    this.panel.mainFrame.setDate(cal);
                    break;

                }

            }
            else if (eventSource == this.panel.pimBtn) {
                this.panel.mainFrame.sideBarSwitch();
            }

            Calendar calendar = Calendar.getInstance();
            if (
                    this.panel.mainFrame.getDate().getDate().get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                    this.panel.mainFrame.getDate().getDate().get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
            ) {
                this.panel.toTodayBtn.setStatus(ElemStatus.UNABLE);
            }
            else {
                this.panel.toTodayBtn.setStatus(ElemStatus.DEFAULT);
            }
        }
    }
}

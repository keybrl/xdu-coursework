package pim.views.gui.sidebar;

import pim.generic.User;
import pim.model.Response;
import pim.views.gui.generic.DefaultBtn;
import pim.views.gui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Locale;


/**
 * 用户信息页
 *
 * @author 罗阳豪 16130120191
 */
class UserInfoPanel extends SideBarContainPanel {

    private DefaultBtn allPIMBtn;
    private DefaultBtn todoBtn;
    private DefaultBtn noteBtn;
    private DefaultBtn appoBtn;
    private DefaultBtn contBtn;
    private DefaultBtn logoutBtn;
    private DefaultBtn newBtn;

    UserInfoPanel(SideBarPanel panel) {
        super(panel);
        this.setLayout(null);

        User user = this.panel.mainFrame.getIface().getUser();
        JLabel nameLabel = new JLabel(user.getName());
        JLabel emailLabel = new JLabel(user.getEmail());
        Calendar calendar = Calendar.getInstance();
        JLabel dateLabel = new JLabel(calendar.get(Calendar.YEAR) + "年" + (calendar.get(Calendar.MONTH) + 1) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日");
        JLabel weekLabel = new JLabel(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.CHINA));
        this.allPIMBtn = new DefaultBtn("全部事项");
        this.todoBtn = new DefaultBtn("待办");
        this.noteBtn = new DefaultBtn("笔记");
        this.appoBtn = new DefaultBtn("预约");
        this.contBtn = new DefaultBtn("通讯录");
        this.newBtn = new DefaultBtn("新建");
        this.logoutBtn = new DefaultBtn("注销");

        int width = MainFrame.sideBarWidth;
        int top = 20;
        dateLabel.setBounds(20, top, width, 32);
        top += 28;
        weekLabel.setBounds(20, top, width, 32);
        top += 42;
        nameLabel.setBounds(20, top, width, 32);
        top += 28;
        emailLabel.setBounds(20, top, width, 32);
        top += 42;
        this.allPIMBtn.setBounds(20, top, (width - 40) / 2, 36);
        this.newBtn.setBounds((width - 40) / 2 + 20, top, width - (width - 40) / 2 - 40, 36);
        top += 41;
        this.todoBtn.setBounds(20, top, (width - 40) / 2, 36);
        this.noteBtn.setBounds((width - 40) / 2 + 20, top, width - (width - 40) / 2 - 40, 36);
        top += 41;
        this.appoBtn.setBounds(20, top, (width - 40) / 2, 36);
        this.contBtn.setBounds((width - 40) / 2 + 20, top, width - (width - 40) / 2 - 40, 36);
        top += 51;
        this.logoutBtn.setBounds(20, top, 80, 36);

        dateLabel.setFont(new Font("Noto Sans CJK SC Bold", Font.PLAIN, 20));
        weekLabel.setFont(new Font("Noto Sans CJK SC Medium", Font.PLAIN, 14));
        nameLabel.setFont(new Font("Noto Sans CJK SC Bold", Font.PLAIN, 18));
        emailLabel.setFont(new Font("Noto Sans CJK SC Medium", Font.PLAIN, 14));

        MineActionListener actionListener = new MineActionListener(this);
        this.allPIMBtn.addActionListener(actionListener);
        this.todoBtn.addActionListener(actionListener);
        this.noteBtn.addActionListener(actionListener);
        this.appoBtn.addActionListener(actionListener);
        this.contBtn.addActionListener(actionListener);
        this.newBtn.addActionListener(actionListener);
        this.logoutBtn.addActionListener(actionListener);

        this.add(dateLabel);
        this.add(weekLabel);
        this.add(nameLabel);
        this.add(emailLabel);
        this.add(this.allPIMBtn);
        this.add(this.todoBtn);
        this.add(this.noteBtn);
        this.add(this.appoBtn);
        this.add(this.contBtn);
        this.add(this.newBtn);
        this.add(this.logoutBtn);

        this.panel.mainFrame.calPanel.setDate(this.panel.mainFrame.cal);
    }

    private static class MineActionListener implements ActionListener {
        private UserInfoPanel panel;

        MineActionListener(UserInfoPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object eventSource = e.getSource();
            if (eventSource == this.panel.allPIMBtn) {
                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().getEntities(this.panel.panel.mainFrame.getIface().getUser());

                if (res.statusCode != 200) {
                    throw new RuntimeException("DataAPI 响应非200状态");
                }

                this.panel.panel.panelSwitch(new EntitiesListPanel(this.panel.panel, this.panel, res.data));
            }
            else if (eventSource == this.panel.todoBtn) {
                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().getEntities("todo", this.panel.panel.mainFrame.getIface().getUser());

                if (res.statusCode != 200) {
                    throw new RuntimeException("DataAPI 响应非200状态");
                }

                this.panel.panel.panelSwitch(new EntitiesListPanel(this.panel.panel, this.panel, res.data));
            }
            else if (eventSource == this.panel.noteBtn) {
                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().getEntities("note", this.panel.panel.mainFrame.getIface().getUser());

                if (res.statusCode != 200) {
                    throw new RuntimeException("DataAPI 响应非200状态");
                }

                this.panel.panel.panelSwitch(new EntitiesListPanel(this.panel.panel, this.panel, res.data));
            }
            else if (eventSource == this.panel.appoBtn) {
                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().getEntities("appointment", this.panel.panel.mainFrame.getIface().getUser());

                if (res.statusCode != 200) {
                    throw new RuntimeException("DataAPI 响应非200状态");
                }

                this.panel.panel.panelSwitch(new EntitiesListPanel(this.panel.panel, this.panel, res.data));
            }
            else if (eventSource == this.panel.contBtn) {
                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().getEntities("contact", this.panel.panel.mainFrame.getIface().getUser());

                if (res.statusCode != 200) {
                    throw new RuntimeException("DataAPI 响应非200状态");
                }

                this.panel.panel.panelSwitch(new EntitiesListPanel(this.panel.panel, this.panel, res.data));
            }
            else if (eventSource == this.panel.newBtn) {
                this.panel.panel.panelSwitch(new NewEntityPanel(this.panel.panel, this.panel, null));
            }
            else if (eventSource == this.panel.logoutBtn) {
                this.panel.panel.panelSwitch(new LoginPanel(this.panel.panel));
                this.panel.panel.mainFrame.getIface().setUser(null);
                this.panel.panel.mainFrame.calPanel.setDate(this.panel.panel.mainFrame.cal);
            }
        }
    }
}

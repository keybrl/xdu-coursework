package pim.views.gui.sidebar;

import pim.generic.User;
import pim.model.Response;
import pim.views.gui.generic.*;
import pim.views.gui.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class LoginPanel extends SideBarContainPanel {

    private JTextField emailInput;
    private JPasswordField passwdInput;
    private DefaultBtn loginBtn;
    private DefaultBtn registBtn;

    LoginPanel(SideBarPanel panel) {
        super(panel);
        this.setLayout(null);

        JLabel titleLabel = new JLabel("用户登录", JLabel.CENTER);
        JLabel emailLabel = new DefaultLabel("邮箱");
        JLabel passwdLabel = new DefaultLabel("密码");

        this.emailInput = new DefaultTextField();
        this.passwdInput = new JPasswordField();

        this.loginBtn = new DefaultBtn("登录");
        this.registBtn = new DefaultBtn("注册");


        int width = MainFrame.sideBarWidth;
        int top = 20;
        titleLabel.setBounds(0, top, width, 32);
        top += 47;
        emailLabel.setBounds(20, top, 30, 32);
        this.emailInput.setBounds(60, top, width - 80, 32);
        top += 42;
        passwdLabel.setBounds(20, top, 30,32);
        this.passwdInput.setBounds(60, top, width - 80, 32);
        top += 42;
        this.loginBtn.setBounds(20, top, (width - 50) / 2, 32);
        this.registBtn.setBounds(30 + (width - 50) / 2, top, (width - 50) / 2, 32);

        titleLabel.setFont(Fonts.SIDEBAR_TITLE);
        titleLabel.setForeground(Colors.SIDEBAR_TITLE_F);

        ActionListener actionListener = new MineActionListener(this);
        this.loginBtn.addActionListener(actionListener);
        this.registBtn.addActionListener(actionListener);

        this.add(titleLabel);
        this.add(emailLabel);
        this.add(passwdLabel);
        this.add(this.emailInput);
        this.add(this.passwdInput);
        this.add(this.loginBtn);
        this.add(this.registBtn);
    }

    private static class MineActionListener implements ActionListener {

        private LoginPanel panel;

        MineActionListener(LoginPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object eventSource = e.getSource();
            if (eventSource == this.panel.loginBtn) {
                String email = this.panel.emailInput.getText();
                String passwd = new String(this.panel.passwdInput.getPassword());

                if (email.length() == 0) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "邮箱不能为空", "非法字段", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (passwd.length() == 0) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "密码不能为空", "非法字段", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                User user = new User(email, null, passwd);
                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().verifyUser(user);
                if (res.statusCode == 404) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "用户不存在", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
                else if (res.statusCode == 403) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "用户或密码错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
                else if (res.statusCode == 200) {
                    this.panel.panel.mainFrame.getIface().setUser(user);

                    this.panel.panel.panelSwitch(new UserInfoPanel(this.panel.panel));
                    System.out.println("登录成功");
                }
                else {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "未知错误", "登录失败", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException("DataAPI 返回非预期状态");
                }

            }
            else if (eventSource == this.panel.registBtn) {
                this.panel.panel.panelSwitch(new RegisterPanel(this.panel.panel));
            }
        }
    }

}

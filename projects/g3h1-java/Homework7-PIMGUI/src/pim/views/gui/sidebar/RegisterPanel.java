package pim.views.gui.sidebar;

import pim.generic.User;
import pim.model.Response;
import pim.views.gui.generic.*;
import pim.views.gui.main.MainFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class RegisterPanel extends SideBarContainPanel {

    private JTextField emailInput;
    private JTextField nameInput;
    private JPasswordField passwdInput;
    private JPasswordField rePasswdInput;
    private DefaultBtn submitBtn;
    private DefaultBtn returnBtn;

    RegisterPanel(SideBarPanel panel) {
        super(panel);
        this.setLayout(null);

        JLabel titleLabel = new JLabel("用户注册", JLabel.CENTER);
        JLabel emailLabel = new DefaultLabel("邮箱");
        JLabel nameLabel = new DefaultLabel("用户名");
        JLabel passwdLabel = new DefaultLabel("密码");
        JLabel rePasswdLabel = new DefaultLabel("确认密码");

        this.emailInput = new DefaultTextField();
        this.nameInput = new DefaultTextField();
        this.passwdInput = new JPasswordField();
        this.rePasswdInput = new JPasswordField();

        this.submitBtn = new DefaultBtn("提交");
        this.returnBtn = new DefaultBtn("返回");


        int width = MainFrame.sideBarWidth;
        int top = 20;
        titleLabel.setBounds(0, top, width, 32);
        top += 47;
        emailLabel.setBounds(20, top, 60, 32);
        this.emailInput.setBounds(90, top, width - 110, 32);
        top += 42;
        nameLabel.setBounds(20, top, 60, 32);
        this.nameInput.setBounds(90, top, width - 110, 32);
        top += 42;
        passwdLabel.setBounds(20, top, 60,32);
        this.passwdInput.setBounds(90, top, width - 110, 32);
        top += 42;
        rePasswdLabel.setBounds(20, top, 60, 32);
        this.rePasswdInput.setBounds(90, top, width - 110, 32);
        top += 42;
        this.submitBtn.setBounds(20, top, (width - 50) / 2, 32);
        this.returnBtn.setBounds(30 + (width - 50) / 2, top, (width - 50) / 2, 32);

        titleLabel.setFont(Fonts.SIDEBAR_TITLE);
        titleLabel.setForeground(Colors.SIDEBAR_TITLE_F);

        ActionListener actionListener = new MineActionListener(this);
        this.submitBtn.addActionListener(actionListener);
        this.returnBtn.addActionListener(actionListener);

        this.add(titleLabel);
        this.add(emailLabel);
        this.add(nameLabel);
        this.add(passwdLabel);
        this.add(rePasswdLabel);
        this.add(this.emailInput);
        this.add(this.nameInput);
        this.add(this.passwdInput);
        this.add(this.rePasswdInput);
        this.add(this.submitBtn);
        this.add(this.returnBtn);
    }

    private static class MineActionListener implements ActionListener {

        private RegisterPanel panel;

        MineActionListener(RegisterPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object eventSource = e.getSource();
            if (eventSource == this.panel.submitBtn) {
                String email = this.panel.emailInput.getText();
                String name = this.panel.nameInput.getText();
                String passwd = new String(this.panel.passwdInput.getPassword());
                String rePasswd = new String(this.panel.rePasswdInput.getPassword());

                if (email.length() == 0) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "邮箱不能为空", "非法字段", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (name.length() == 0) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "用户名不能为空", "非法字段", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (passwd.length() == 0) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "密码不能为空", "非法字段", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!passwd.equals(rePasswd)) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "两次输入密码不同", "验证提醒", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }


                User user = new User(email, name, passwd);
                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().addUser(user);
                if (res.statusCode == 403) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "用户已存在", "注册失败", JOptionPane.ERROR_MESSAGE);
                }
                else if (res.statusCode == 200) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "注册成功，可前往登录。", "注册成功", JOptionPane.INFORMATION_MESSAGE);
                    this.panel.panel.panelSwitch(new LoginPanel(this.panel.panel));
                }
                else {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "未知错误", "注册失败", JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException("DataAPI 返回非预期状态");
                }
            }
            else if (eventSource == this.panel.returnBtn) {
                this.panel.panel.panelSwitch(new LoginPanel(this.panel.panel));
            }
        }
    }

}

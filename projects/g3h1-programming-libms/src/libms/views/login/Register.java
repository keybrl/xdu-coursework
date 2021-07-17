package libms.views.login;

import libms.Iface;
import libms.model.Response;
import libms.model.orm.User;
import libms.views.DefaultBtn;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;


/**
 * 注册页面
 *
 * @author 16130120118 陈敏
 * @author 1040370550@qq.com
 */
class Register extends JFrame {
    // 全局接口
    private Iface iface;

    private Container container;

    private JTextField nameInput;
    private JTextField phoneNumInput;
    private JTextField emailInput;
    private JTextField addrInput;
    private JTextField unitInput;
    private JPasswordField passwdInput;
    private JPasswordField rePasswdInput;

    private JButton submitBtn;
    private JButton cancelBtn;


    Register(Iface iface) {
        super("图书管理系统 - 用户注册");
        this.iface = iface;

        this.setSize(Register.frameWidth,Register.frameHeight);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        this.container = getContentPane();
        this.container.setLayout(null);

        this.initGUI();
    }


    private void initGUI() {
        // 设置背景颜色
        this.container.setBackground(Color.WHITE);

        // 创建页面元素
        JLabel nameLabel = new JLabel("姓名");
        JLabel passwdLabel = new JLabel("设置密码");
        JLabel rePasswdLabel = new JLabel("再次输入密码");
        JLabel phoneNumLabel = new JLabel("电话");
        JLabel emailLabel = new JLabel("邮箱");
        JLabel addrLabel = new JLabel("地址");
        JLabel unitLabel = new JLabel("单位");

        this.nameInput = new JTextField();
        this.passwdInput = new JPasswordField();
        this.rePasswdInput = new JPasswordField();
        this.phoneNumInput = new JTextField();
        this.emailInput = new JTextField();
        this.addrInput = new JTextField();
        this.unitInput = new JTextField();

        this.submitBtn = new DefaultBtn("提交");
        this.cancelBtn = new DefaultBtn("返回");

        // 设置字体
        Font font = new Font(null, Font.PLAIN, 14);
        nameLabel.setFont(font);
        passwdLabel.setFont(font);
        rePasswdLabel.setFont(font);
        phoneNumLabel.setFont(font);
        emailLabel.setFont(font);
        addrLabel.setFont(font);
        unitLabel.setFont(font);
        this.submitBtn.setFont(font);
        this.cancelBtn.setFont(font);

        font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        this.nameInput.setFont(font);
        this.passwdInput.setFont(font);
        this.rePasswdInput.setFont(font);
        this.phoneNumInput.setFont(font);
        this.emailInput.setFont(font);
        this.addrInput.setFont(font);
        this.unitInput.setFont(font);

        // 设置元素 位置 大小
        int inputLeft = (Register.frameWidth - Register.inputWidth) / 2;
        int firstElemTop = 50;
        int distanceBetweenLabelAndInput = Register.labelHeight;
        int distanceBetweenGroups = Register.labelHeight + Register.inputHeight + 8;
        nameLabel.setBounds(inputLeft, firstElemTop, Register.labelWidth, Register.labelHeight);
        passwdLabel.setBounds(inputLeft, firstElemTop + distanceBetweenGroups, Register.labelWidth, Register.labelHeight);
        rePasswdLabel.setBounds(inputLeft, firstElemTop + distanceBetweenGroups * 2, Register.labelWidth, Register.labelHeight);
        phoneNumLabel.setBounds(inputLeft,firstElemTop + distanceBetweenGroups * 3, Register.labelWidth, Register.labelHeight);
        emailLabel.setBounds(inputLeft,firstElemTop + distanceBetweenGroups * 4, Register.labelWidth, Register.labelHeight);
        addrLabel.setBounds(inputLeft,firstElemTop + distanceBetweenGroups * 5, Register.labelWidth, Register.labelHeight);
        unitLabel.setBounds(inputLeft,firstElemTop + distanceBetweenGroups * 6, Register.labelWidth, Register.labelHeight);

        this.nameInput.setBounds(inputLeft, firstElemTop + distanceBetweenLabelAndInput, Register.inputWidth, Register.inputHeight);
        this.passwdInput.setBounds(inputLeft, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups, Register.inputWidth, Register.inputHeight);
        this.rePasswdInput.setBounds(inputLeft, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups * 2, Register.inputWidth, Register.inputHeight);
        this.phoneNumInput.setBounds(inputLeft, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups * 3, Register.inputWidth, Register.inputHeight);
        this.emailInput.setBounds(inputLeft, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups * 4, Register.inputWidth, Register.inputHeight);
        this.addrInput.setBounds(inputLeft, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups * 5, Register.inputWidth, Register.inputHeight);
        this.unitInput.setBounds(inputLeft, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups * 6, Register.inputWidth, Register.inputHeight);

        int leftBtnLeft = Register.frameWidth - inputLeft - 2 * Register.buttonWidth - Register.buttonDistance;
        this.submitBtn.setBounds(leftBtnLeft, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups * 7, Register.buttonWidth, Register.buttonHeight);
        this.cancelBtn.setBounds(leftBtnLeft + Register.buttonWidth + Register.buttonDistance, firstElemTop + distanceBetweenLabelAndInput + distanceBetweenGroups * 7, Register.buttonWidth, Register.buttonHeight);


        // 为按钮绑定事件监听器
        ActionHandle actionHandle = new ActionHandle(this);
        this.submitBtn.addActionListener(actionHandle);
        this.cancelBtn.addActionListener(actionHandle);


        // 将元素加入容器
        this.container.add(nameLabel);
        this.container.add(passwdLabel);
        this.container.add(rePasswdLabel);
        this.container.add(phoneNumLabel);
        this.container.add(emailLabel);
        this.container.add(addrLabel);
        this.container.add(unitLabel);

        this.container.add(nameInput);
        this.container.add(passwdInput);
        this.container.add(rePasswdInput);
        this.container.add(phoneNumInput);
        this.container.add(emailInput);
        this.container.add(addrInput);
        this.container.add(unitInput);

        this.container.add(submitBtn);
        this.container.add(cancelBtn);

    }


    private static int frameWidth = 360;
    private static int frameHeight = 610;
    private static int buttonWidth = 80;
    private static int buttonHeight = 36;
    private static int buttonDistance = 10;
    private static int inputWidth = 300;
    private static int inputHeight = 30;
    private static int labelWidth = 100;
    private static int labelHeight = 20;

    private static class ActionHandle implements ActionListener {
        Register registerFrame;

        ActionHandle(Register registerFrame) {
            this.registerFrame = registerFrame;
        }

        public void actionPerformed(ActionEvent event) {
            Object eventSource = event.getSource();
            if (eventSource == this.registerFrame.submitBtn) {
                // 单击提交按钮

                String name = this.registerFrame.nameInput.getText();
                String type = "user";
                String passwd = new String(this.registerFrame.passwdInput.getPassword());
                String rePasswd = new String(this.registerFrame.rePasswdInput.getPassword());
                String phoneNum = this.registerFrame.phoneNumInput.getText();
                String email = this.registerFrame.emailInput.getText();
                String addr = this.registerFrame.addrInput.getText();
                String unit = this.registerFrame.unitInput.getText();

                // 初步检查
                if (name.length() == 0) {
                    JOptionPane.showMessageDialog(null, "请输入姓名！");
                    return;
                }
                else if (passwd.length() == 0) {
                    JOptionPane.showMessageDialog(null, "请输入密码！");
                    return;
                }
                else if (!passwd.equals(rePasswd)) {
                    JOptionPane.showMessageDialog(null, "两次输入的密码不匹配！");
                    return;
                }

                // 添加账户
                Response addUserRes = this.registerFrame.iface.getDataAPI().addUser(name, "user", passwd);
                if (addUserRes.statusCode != 200) {
                    JOptionPane.showMessageDialog(null, "输入格式错误，请确认后重试！");
                    return;
                }
                Response updateUserRes = this.registerFrame.iface.getDataAPI().updateUser(((User)addUserRes.data[0]).id, null, null, null, phoneNum, email, unit, addr);
                if (updateUserRes.statusCode != 200) {
                    JOptionPane.showMessageDialog(null, "用户已成功创建。但部分信息输入格式错误，未能录入！\n您仍能正常登录，您的借书号为：\n" + ((User)addUserRes.data[0]).id);
                    this.registerFrame.dispose();
                    return;
                }
                JOptionPane.showMessageDialog(null, "注册成功！\n请牢记您的借书号：\n" + ((User)addUserRes.data[0]).id);
                this.registerFrame.dispose();

            }
            else if (eventSource == this.registerFrame.cancelBtn) {
                // 单击返回按钮
                this.registerFrame.dispose();
            }
            else {
                return;
            }
            this.registerFrame.dispose();
        }
    }

}

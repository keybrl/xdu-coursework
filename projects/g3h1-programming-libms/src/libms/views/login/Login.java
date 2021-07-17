package libms.views.login;

import libms.Iface;
import libms.model.Response;
import libms.model.orm.User;
import libms.views.DefaultBtn;
import libms.views.admin.MainFrame;

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
 * 登录页面
 *
 * @author 16130120118 陈敏
 * @author 1040370550@qq.com
 */
class Login extends JFrame {
    // 全局接口
    private Iface iface;

    private Container container;
    private JTextField idInput;
    private JPasswordField passwdInput;
    private JButton loginBtn, registerBtn;

    Login(Iface iface) {
        super("图书管理系统 - 登录");
        this.iface = iface;

        this.setSize(Login.frameWidth, Login.frameHeight);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.container = this.getContentPane();
        this.container.setLayout(null);

        this.initGUI();
    }

    private void initGUI() {
        // 设置背景颜色
        this.container.setBackground(Color.WHITE);

        // 创建页面元素
        JLabel idLabel = new JLabel("借书号");
        JLabel passwdLabel = new JLabel("密码");

        this.idInput = new JTextField();
        this.passwdInput = new JPasswordField();

        this.loginBtn = new DefaultBtn("登录");
        this.registerBtn = new DefaultBtn("注册");


        // 设置字体
        Font font = new Font(null, Font.PLAIN, 14);
        idLabel.setFont(font);
        passwdLabel.setFont(font);
        this.loginBtn.setFont(font);
        this.registerBtn.setFont(font);

        font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        this.idInput.setFont(font);
        this.passwdInput.setFont(font);


        // 设置元素 位置 大小
        int inputLeft = (Login.frameWidth - Login.inputWidth) / 2;
        idLabel.setBounds(inputLeft, 50, 60, 20);
        passwdLabel.setBounds(inputLeft, 134, 60, 20);
        this.idInput.setBounds(inputLeft, 78, Login.inputWidth, Login.inputHeight);
        this.passwdInput.setBounds(inputLeft, 158, Login.inputWidth, Login.inputHeight);

        int leftBtnLeft = Login.frameWidth - inputLeft - 2 * Login.buttonWidth - Login.buttonDistance;
        loginBtn.setBounds(leftBtnLeft, 224, Login.buttonWidth, Login.buttonHeight);
        registerBtn.setBounds(leftBtnLeft + Login.buttonWidth + Login.buttonDistance, 224, Login.buttonWidth, Login.buttonHeight);


        // 为按钮添加事件处理器
        ActionHandler actionHandler = new ActionHandler(this);
        this.loginBtn.addActionListener(actionHandler);
        this.registerBtn.addActionListener(actionHandler);


        // 将元素加入容器
        this.container.add(idLabel);
        this.container.add(idInput);
        this.container.add(passwdLabel);
        this.container.add(passwdInput);
        this.container.add(loginBtn);
        this.container.add(registerBtn);
    }


    // 定义窗口基本参数
    private static int frameWidth = 360;
    private static int frameHeight = 350;
    private static int buttonWidth = 80;
    private static int buttonHeight = 36;
    private static int buttonDistance = 10;
    private static int inputWidth = 300;
    private static int inputHeight = 36;


    class ActionHandler implements ActionListener {
        Login loginFrame;

        ActionHandler(Login loginFrame) {
            this.loginFrame = loginFrame;
        }

        public void actionPerformed(ActionEvent event) {
            Object eventSource = event.getSource();

            if (eventSource == this.loginFrame.loginBtn) {
                // 点击了登录按钮
                String userId = this.loginFrame.idInput.getText();
                String password = new String(this.loginFrame.passwdInput.getPassword());


                // 初步检查
                if (userId.length() == 0) {
                    JOptionPane.showMessageDialog(null, "请输入借书号！");
                    return;
                }
                else if (password.length() == 0) {
                    JOptionPane.showMessageDialog(null, "请输入密码！");
                    return;
                }


                Response res = this.loginFrame.iface.getDataAPI().verifyUser(userId, password);
                switch (res.statusCode) {
                    case 200:
                        this.loginFrame.iface.setUser((User) res.data[0]);
                        switch (((User) res.data[0]).type) {
                            case "user":
                                try {
                                    // 启动普通用户界面
                                    libms.views.user.MainFrame userFrame = new libms.views.user.MainFrame(this.loginFrame.iface);
                                    userFrame.setVisible(true);
                                }
                                catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, "一股神秘力量阻止了你的登录！");
                                    System.out.println("Login: User拒绝启动");
                                    ex.printStackTrace();
                                    return;
                                }

                                System.out.printf("Login: %s，普通用户登录成功！\n", this.loginFrame.iface.getUser().name);
                                break;
                            case "admin":
                                try {
                                    // 启动管理员界面
                                    MainFrame adminFrame = new MainFrame(this.loginFrame.iface);
                                    adminFrame.setVisible(true);
                                }
                                catch (Exception ex) {
                                    JOptionPane.showMessageDialog(null, "一股神秘力量阻止了你的登录！");
                                    System.out.println("Login: Admin拒绝启动");
                                    return;
                                }

                                System.out.printf("Login: %s，管理员登录成功！\n", this.loginFrame.iface.getUser().name);
                                break;
                            default:
                                JOptionPane.showMessageDialog(null, "你是谁！？别过来！");
                                return;
                        }
                        break;
                    case 400:
                        JOptionPane.showMessageDialog(null, "用户名或密码格式错误，请确认后重试！");
                        return;
                    case 403:
                        JOptionPane.showMessageDialog(null, "密码错误！");
                        return;
                    case 404:
                        JOptionPane.showMessageDialog(null, "用户不存在！");
                        return;
                    default:
                        JOptionPane.showMessageDialog(null, "我也不知道发生了什么 (>_<)!!");
                        return;
                }

                this.loginFrame.dispose();
            } else if (eventSource == this.loginFrame.registerBtn) {
                // 点击了注册按钮
                Register registerFrame = new Register(this.loginFrame.iface);
                registerFrame.setVisible(true);
            }
        }
    }

}

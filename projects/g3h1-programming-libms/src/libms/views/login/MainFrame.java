package libms.views.login;

import libms.Iface;


/**
 * 应用主页
 *
 * @author 16130120118 陈敏
 * @author 1040370550@qq.com
 */
public class MainFrame {
    private Login loginFrame;

    public MainFrame(Iface iface) {
        this.loginFrame = new Login(iface);
    }

    public void setVisible(boolean b) {
        this.loginFrame.setVisible(b);
    }
}

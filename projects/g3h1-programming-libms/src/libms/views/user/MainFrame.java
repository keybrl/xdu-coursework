package libms.views.user;

import libms.Iface;
import libms.model.DataAPI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;


/**
 * 普通用户界面
 *
 * @author virtuoso
 */
public class MainFrame {
    private JFrame jFrame;
    public MainFrame(Iface iface) {
        if (iface.getUser() != null && iface.getUser().type.equals("user")) {
            DataAPI dataAPI = iface.getDataAPI();
            if (dataAPI != null) {
                this.jFrame = new JFrame("图书管理系统");
                this.jFrame.setSize(1440, 890);
                this.jFrame.setLocationRelativeTo(null);
                this.jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                this.jFrame.setResizable(false);

                UserPanel user = new UserPanel(jFrame.getWidth(), jFrame.getHeight(), iface);

                this.jFrame.add(user.userPanel);

                this.jFrame.setVisible(true);
            }
            else {
                System.out.println("No dataAPI!");

                // 拒绝执行
                throw new RuntimeException();
            }
        }
        else {
            JOptionPane.showMessageDialog(null,"Only User can use this user!");

            // 拒绝执行
            throw new RuntimeException();
        }
    }


    public void setVisible(boolean b) {
        this.jFrame.setVisible(b);
    }
}

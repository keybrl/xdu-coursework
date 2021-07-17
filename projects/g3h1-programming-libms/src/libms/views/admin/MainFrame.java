package libms.views.admin;

import libms.Iface;
import libms.views.Colors;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * 管理员页面
 *
 * @author keybrl
 */
public class MainFrame extends JFrame {
    private Iface iface;
    private Container container;

    private JButton toBooksBtn;
    private JButton toUsersBtn;
    private JButton toLogsBtn;

    private JPanel rightPanel;

    private JPanel booksPanel;
    private JPanel usersPanel;
    private JPanel logsPanel;


    public MainFrame(Iface iface) {

        super("图书管理系统 - 管理员");


        // 接受Iface
        if (iface == null) {
            System.out.println("Admin: 全局接口为null");
            JOptionPane.showMessageDialog(null,"全局接口为null，不能接受的失败！");
            throw new RuntimeException("全局接口为null");
        }
        else if (iface.getUser() == null || !iface.getUser().type.equals("admin")) {
            System.out.println("Admin: 非管理员用户禁止登录管理员界面");
            JOptionPane.showMessageDialog(null,"非管理员用户禁止登录管理员界面");
            throw new RuntimeException("非管理员用户禁止登录管理员界面");
        }
        else if (iface.getDataAPI() == null) {
            System.out.println("Admin: 数据接口为null");
            JOptionPane.showMessageDialog(null,"数据接口为null，不能接受的失败！");
            throw new RuntimeException("数据接口为null");
        }
        this.iface = iface;

        this.setSize(MainFrame.frameWidth, MainFrame.frameHeight);
        this.setBackground(Colors.DEFAULT_BG);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.container = this.getContentPane();
        this.container.setLayout(null);

        this.initGUI();
    }

    private void initGUI() {
        // 创建页面元素
        JPanel leftPanel = new BtnGroup();
        this.booksPanel = new BooksPanel(this.iface.getDataAPI());
        this.usersPanel = new UsersPanel(this.iface.getDataAPI());
        this.logsPanel = new LogsPanel(this.iface.getDataAPI());
        this.rightPanel = this.booksPanel;

        this.toBooksBtn = new JButton("书本信息");
        this.toUsersBtn = new JButton("用户信息");
        this.toLogsBtn = new JButton("借还日志");


        // 为左侧面板的按钮绑定事件监听器
        SwitchRightPanelListener actionListener = new SwitchRightPanelListener(this);
        this.toBooksBtn.addActionListener(actionListener);
        this.toUsersBtn.addActionListener(actionListener);
        this.toLogsBtn.addActionListener(actionListener);


        // 将元素加入页面容器
        leftPanel.add(this.toBooksBtn);
        leftPanel.add(this.toUsersBtn);
        leftPanel.add(this.toLogsBtn);

        this.container.add(leftPanel);
        this.container.add(this.rightPanel);

        ((UsersPanel) this.usersPanel).getUserList();
    }


    private void switchRightPanel(JPanel panel) {
        this.container.remove(this.rightPanel);
        this.container.add(panel);
        this.rightPanel = panel;
        panel.updateUI();
        panel.repaint();
        this.container.repaint();
        if (panel == this.logsPanel) {
            ((LogsPanel) this.logsPanel).refresh();
        }
    }


    static int frameWidth = 1440;
    static int frameHeight = 890;


    private static class SwitchRightPanelListener implements ActionListener {
        private MainFrame frame;

        SwitchRightPanelListener(MainFrame frame) {
            this.frame = frame;
        }

        public void actionPerformed(ActionEvent event) {

            Object eventSource = event.getSource();
            if (eventSource == this.frame.toBooksBtn) {
                // 切换到书本信息页
                this.frame.switchRightPanel(this.frame.booksPanel);
            }
            else if (eventSource == this.frame.toUsersBtn) {
                // 切换到用户信息页
                this.frame.switchRightPanel(this.frame.usersPanel);
            }
            else if (eventSource == this.frame.toLogsBtn) {
                // 切换到日志页
                this.frame.switchRightPanel(this.frame.logsPanel);
            }
        }
    }
}

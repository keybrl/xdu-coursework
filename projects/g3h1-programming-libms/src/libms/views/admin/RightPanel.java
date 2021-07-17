package libms.views.admin;

import libms.views.Colors;

import javax.swing.JPanel;


/**
 * 右侧面板类
 * 置于页面右侧，显示页面内容
 * BooksPanel UsersPanel LogsPanel 的父类
 *
 * @author keybrl
 */
class RightPanel extends JPanel {

    RightPanel() {
        this.setBounds(BtnGroup.panelWidth, 0, RightPanel.panelWidth, RightPanel.panelHeight);
        this.setBackground(Colors.DEFAULT_BG);
        this.setLayout(null);
    }

    static int panelWidth = MainFrame.frameWidth - BtnGroup.panelWidth;
    static int panelHeight = MainFrame.frameHeight;
}

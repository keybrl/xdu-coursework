package libms.views.admin;

import libms.views.Colors;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * 按钮组类
 * 放置于页面左侧的导航栏
 *
 * @author keybrl
 */
class BtnGroup extends JPanel {
    private int btnNum;

    BtnGroup() {
        this.setBounds(0, 0, BtnGroup.panelWidth, BtnGroup.panelHeight);
        this.setBackground(Colors.LEFT_PANEL_BG);
        this.setLayout(null);
        this.btnNum = 0;
    }

    public Component add(Component comp) {
        comp.setBounds(0, this.btnNum * BtnGroup.btnHeight, BtnGroup.panelWidth, BtnGroup.btnHeight);
        comp.setBackground(Colors.LEFT_PANEL_BG);
        comp.setForeground(Colors.LEFT_PANEL_FONT);
        comp.addMouseListener(new SwitchBtnStatus());
        comp.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        ((JButton) comp).setFocusPainted(false);

        ((JButton) comp).setBorder(null);
        this.btnNum += 1;
        return super.add(comp);
    }

    static int panelWidth = 240;
    private static int panelHeight = MainFrame.frameHeight;
    private static int btnHeight = 40;

    private static class SwitchBtnStatus implements MouseListener {
        public void mouseClicked(MouseEvent event) {
        }

        public void mousePressed(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.LEFT_PANEL_BG);
            ((JButton) event.getSource()).setForeground(Colors.LEFT_PANEL_FONT);
        }

        public void mouseReleased(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.LEFT_PANEL_HOVER_BG);
            ((JButton) event.getSource()).setForeground(Color.WHITE);
        }

        public void mouseEntered(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.LEFT_PANEL_HOVER_BG);
            ((JButton) event.getSource()).setForeground(Color.WHITE);
        }

        public void mouseExited(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.LEFT_PANEL_BG);
            ((JButton) event.getSource()).setForeground(Colors.LEFT_PANEL_FONT);
        }
    }
}

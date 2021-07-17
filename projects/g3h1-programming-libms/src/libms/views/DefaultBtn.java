package libms.views;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * 该项目的默认Button
 *
 * @author keybrl
 */
public class DefaultBtn extends JButton {
    public DefaultBtn(String text) {
        super(text);
        this.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        this.setBackground(Colors.DEFAULT_BTN_BG);
        this.setForeground(Colors.DEFAULT_BTN_FONT);
        this.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
        this.setFocusPainted(false);

        this.addMouseListener(new SwitchBtnStatusListener());
    }

    private static class SwitchBtnStatusListener implements MouseListener {

        public void mouseClicked(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.DEFAULT_BTN_BG);
            ((JButton) event.getSource()).setForeground(Colors.DEFAULT_BTN_FONT);
        }

        public void mousePressed(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.DEFAULT_BTN_BG);
            ((JButton) event.getSource()).setForeground(Colors.DEFAULT_BTN_FONT);
        }

        public void mouseReleased(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.DEFAULT_BTN_HOVER_BG);
            ((JButton) event.getSource()).setForeground(Color.BLACK);
        }

        public void mouseEntered(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.DEFAULT_BTN_HOVER_BG);
            ((JButton) event.getSource()).setForeground(Color.BLACK);
        }

        public void mouseExited(MouseEvent event) {
            ((JButton) event.getSource()).setBackground(Colors.DEFAULT_BTN_BG);
            ((JButton) event.getSource()).setForeground(Colors.DEFAULT_BTN_FONT);
        }
    }
}

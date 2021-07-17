package libms.views.user;

import libms.views.Colors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * 菜单面板
 *
 * @author virtuoso
 */
class MenuPanel {

    private JButton userInfo;
    private JButton search;
    private JButton borrowBooks;
    private JButton returnBooks;

    JPanel panel;
    private int menuWidth;
    private int menuHeight;


    MenuPanel(JButton UserInfo, JButton Search, JButton BorrowBooks, JButton ReturnBooks, int width, int height){
        this.panel = new JPanel();

        this.menuWidth = width;
        this.menuHeight = height;

        this.userInfo = UserInfo;
        this.search = Search;
        this.borrowBooks = BorrowBooks;
        this.returnBooks = ReturnBooks;

        this.PlaceComment();
    }

    private void PlaceComment(){
        this.panel.setLayout(null);
        this.panel.setBounds(0, 0, menuWidth, menuHeight *6);
        this.panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.panel.setBackground(Colors.LEFT_PANEL_BG);

        this.userInfo.setBounds(0, 0, menuWidth, menuHeight /3);
        this.search.setBounds(0, menuHeight / 3, menuWidth, menuHeight /3);
        this.borrowBooks.setBounds(0, (menuHeight / 3) * 2, menuWidth, menuHeight /3);
        this.returnBooks.setBounds(0, menuHeight - 1, menuWidth, menuHeight /3);

        Font font = new Font(Font.DIALOG, Font.PLAIN, 16);
        this.userInfo.setFont(font);
        this.search.setFont(font);
        this.borrowBooks.setFont(font);
        this.returnBooks.setFont(font);

        this.userInfo.setBackground(Colors.LEFT_PANEL_BG);
        this.search.setBackground(Colors.LEFT_PANEL_BG);
        this.borrowBooks.setBackground(Colors.LEFT_PANEL_BG);
        this.returnBooks.setBackground(Colors.LEFT_PANEL_BG);
        this.userInfo.setForeground(Colors.LEFT_PANEL_FONT);
        this.search.setForeground(Colors.LEFT_PANEL_FONT);
        this.borrowBooks.setForeground(Colors.LEFT_PANEL_FONT);
        this.returnBooks.setForeground(Colors.LEFT_PANEL_FONT);

        this.userInfo.setBorder(null);
        this.search.setBorder(null);
        this.borrowBooks.setBorder(null);
        this.returnBooks.setBorder(null);

        this.userInfo.setFocusPainted(false);
        this.search.setFocusPainted(false);
        this.borrowBooks.setFocusPainted(false);
        this.returnBooks.setFocusPainted(false);

        this.userInfo.addMouseListener(new SwitchBtnStatus());
        this.search.addMouseListener(new SwitchBtnStatus());
        this.borrowBooks.addMouseListener(new SwitchBtnStatus());
        this.returnBooks.addMouseListener(new SwitchBtnStatus());

        this.panel.add(this.userInfo);
        this.panel.add(this.search);
        this.panel.add(this.borrowBooks);
        this.panel.add(this.returnBooks);
    }

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

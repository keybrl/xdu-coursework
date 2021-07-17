package pim.views.gui.generic;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * 顶栏按钮组件
 *
 * @author 罗阳豪 16130120191
 */
public class TopBarBtn extends JButton {
    ElemStatus status;

    public TopBarBtn(String text) {
        super(text);

        this.setFocusPainted(false);
        this.setFont(Fonts.DEFAULT);

        setStatus(ElemStatus.DEFAULT);

        this.addMouseListener(new MineMouseListener());
    }
    public void setStatus(ElemStatus status) {
        if (status == this.status) {
            return;
        }

        switch (status) {
            case HOVER:
                this.setBackground(Colors.TOP_BAR_BTN_HOVER_BG);
                this.setForeground(Colors.TOP_BAR_BTN_DEFAULT_F);
                this.setBorder(BorderFactory.createLineBorder(Colors.TOP_BAR_BTN_HOVER_BORDER, 1));
                this.status = ElemStatus.HOVER;
                this.setEnabled(true);
                break;
            case UNABLE:
                this.setBackground(Colors.TOP_BAR_BTN_DEFAULT_BG);
                this.setForeground(Colors.TOP_BAR_BTN_UNABLE_F);
                this.setBorder(BorderFactory.createLineBorder(Colors.TOP_BAR_BTN_UNABEL_BORDER, 1));
                this.status = ElemStatus.UNABLE;
                this.setEnabled(false);
                break;
            case DEFAULT:
            default:
                this.setBackground(Colors.TOP_BAR_BTN_DEFAULT_BG);
                this.setForeground(Colors.TOP_BAR_BTN_DEFAULT_F);
                this.setBorder(BorderFactory.createLineBorder(Colors.TOP_BAR_BTN_DEFAULT_BORDER, 1));
                this.status = ElemStatus.DEFAULT;
                this.setEnabled(true);
                break;
        }
    }


    private static class MineMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (((TopBarBtn) e.getSource()).status == ElemStatus.DEFAULT) {
                ((TopBarBtn) e.getSource()).setStatus(ElemStatus.HOVER);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (((TopBarBtn) e.getSource()).status == ElemStatus.HOVER) {
                ((TopBarBtn) e.getSource()).setStatus(ElemStatus.DEFAULT);
            }
        }
    }

    public static final int height = 35;
    public static final int top = 4;
}

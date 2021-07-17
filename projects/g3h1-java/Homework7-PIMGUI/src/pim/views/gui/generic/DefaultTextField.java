package pim.views.gui.generic;

import javax.swing.*;


/**
 * 默认文本输入框
 *
 * @author 罗阳豪 16130120191
 */
public class DefaultTextField extends JTextField {
    public DefaultTextField() {
        this.intiGUI();
    }

    private void intiGUI() {
        this.setFont(Fonts.INPUT);
    }
}

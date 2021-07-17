package pim.views.gui.generic;

import javax.swing.*;

public class DefaultLabel extends JLabel {
    public DefaultLabel(String text) {
        super(text);

        this.initGUI();
    }

    private void initGUI() {
        this.setFont(Fonts.DEFAULT);
        this.setForeground(Colors.DEFAULT_F);
    }
}

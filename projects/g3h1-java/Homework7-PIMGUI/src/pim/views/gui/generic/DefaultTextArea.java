package pim.views.gui.generic;

import javax.swing.*;
import java.awt.*;

public class DefaultTextArea extends JScrollPane {
    private JTextArea textArea;

    public DefaultTextArea() {
        super();
        this.textArea = new JTextArea();
        this.textArea.setFont(Fonts.INPUT);
        this.add(textArea);
        this.setViewportView(textArea);
    }
    public String getText() {
        return this.textArea.getText();
    }
    public void setText(String text) {
        this.textArea.setText(text);
    }
    public void lockEdit() {
        this.textArea.setEditable(false);
    }
}

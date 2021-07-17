package pim.views.gui.sidebar;

import pim.views.gui.generic.Colors;
import pim.views.gui.main.MainFrame;

import javax.swing.*;
import java.awt.*;

public class SideBarPanel extends JPanel {

    MainFrame mainFrame;
    public SideBarContainPanel mainPanel;

    public SideBarPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        this.setLayout(null);
        this.setBackground(Colors.SIDEBAR_BG);

        this.mainPanel = new LoginPanel(this);

        this.add(this.mainPanel);
    }

    public void autoResize() {
        this.mainPanel.setBounds(1, 0, this.getWidth() - 1, this.getHeight());
        if (this.mainPanel instanceof EntitiesListPanel) {
            ((EntitiesListPanel) this.mainPanel).autoResize();
        }
    }

    public void panelSwitch(SideBarContainPanel newPanel) {
        if (newPanel == this.mainPanel) {
            return;
        }
        else if (newPanel == null) {
            throw new NullPointerException("参数 newPanel 不能为 null");
        }

        this.remove(this.mainPanel);

        this.add(newPanel);
        this.mainPanel = newPanel;

        this.autoResize();
        this.updateUI();
        this.repaint();

        if (!this.mainFrame.sideBarPanel.isVisible()) {
            this.mainFrame.sideBarSwitch();
        }
    }

}

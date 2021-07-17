package pim.views.gui.sidebar;

import pim.views.gui.generic.Colors;
import pim.views.gui.main.MainFrame;

import javax.swing.*;

class SideBarContainPanel extends JPanel {

    SideBarPanel panel;

    SideBarContainPanel(SideBarPanel panel) {
        this.panel = panel;
        this.setBackground(Colors.DEFAULT_BG);
    }
}

package pim.views.gui.main;

import pim.controller.Cal;
import pim.generic.Iface;
import pim.views.gui.sidebar.SideBarPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


public class MainFrame extends JFrame {

    private Iface iface;

    private TopBarPanel topBarPanel;
    private CalTitlePanel calTitlePanel;
    public CalPanel calPanel;

    public SideBarPanel sideBarPanel;

    private Container container;

    public Cal cal;


    public MainFrame(Iface iface) {

        super("Calendar");


        if (iface.getDataAPI() == null) {
            throw new RuntimeException("MainFrame 启动参数错误！Iface 缺少 DataAPI");
        }
        this.iface = iface;

        this.container = this.getContentPane();
        this.initGUI();

        this.addComponentListener(new ResizeListener(this));

        this.setDate(new Cal());

        this.setVisible(true);
    }

    private void initGUI() {
        this.setSize(MainFrame.frameDefaultWidth, MainFrame.frameDefaultHeight + 30);
        this.setMinimumSize(new Dimension(MainFrame.frameMinWidth, MainFrame.frameMinHeight + 30));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.container.setLayout(null);

        this.topBarPanel = new TopBarPanel(this);
        this.calTitlePanel = new CalTitlePanel();
        this.calPanel = new CalPanel(this.iface, this);
        this.sideBarPanel = new SideBarPanel(this);

        this.sideBarPanel.setVisible(false);

        this.autoResize();

        this.container.add(this.topBarPanel);
        this.container.add(this.calTitlePanel);
        this.container.add(this.calPanel);
        this.container.add(this.sideBarPanel);

    }
    private void autoResize() {
        int width = this.getWidth();
        int height = this.getHeight() - 30;

        if (this.sideBarPanel.isVisible()) {
            width -= MainFrame.sideBarWidth;
            this.sideBarPanel.setBounds(width, 0, MainFrame.sideBarWidth, height);
            this.sideBarPanel.autoResize();
            this.setMinimumSize(new Dimension(MainFrame.frameMinWidth + MainFrame.sideBarWidth, MainFrame.frameMinHeight + 30));
        }
        else {
            this.setMinimumSize(new Dimension(MainFrame.frameMinWidth, MainFrame.frameMinHeight + 30));
        }

        this.topBarPanel.setBounds(0, 0, width, MainFrame.topBarHeight);
        this.topBarPanel.autoResize();

        this.calTitlePanel.setBounds(0, MainFrame.topBarHeight, width, MainFrame.calTitleHeight);
        this.calTitlePanel.autoResize();

        this.calPanel.setBounds(
                0,
                MainFrame.topBarHeight + MainFrame.calTitleHeight,
                width,
                height - MainFrame.topBarHeight - MainFrame.calTitleHeight
        );
        this.calPanel.autoResize();

        this.sideBarPanel.autoResize();
    }

    void setDate(Cal cal) {
        this.cal = cal;
        this.calTitlePanel.setDate(cal);
        this.calPanel.setDate(cal);
    }
    Cal getDate() {
        return this.cal;
    }

    public Iface getIface() {
        return this.iface;
    }
    public void sideBarSwitch() {
        if (this.sideBarPanel.isVisible()) {
            this.sideBarPanel.setVisible(false);
            this.setSize(this.getWidth() - MainFrame.sideBarWidth, this.getHeight());
        }
        else {
            this.sideBarPanel.setVisible(true);
            this.setSize(this.getWidth() + MainFrame.sideBarWidth, this.getHeight());
        }
        this.autoResize();
    }

    private static final int frameDefaultWidth = 1165;
    private static final int frameDefaultHeight = 720;
    private static final int frameMinWidth = 584;
    private static final int frameMinHeight = 526;

    private static final int topBarHeight = 43;
    private static final int calTitleHeight = 83;

    public static final int sideBarWidth = 324;


    private static class ResizeListener implements ComponentListener {

        private MainFrame frame;
        ResizeListener(MainFrame frame) {
            this.frame = frame;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            this.frame.autoResize();
        }

        @Override
        public void componentMoved(ComponentEvent e) {

        }

        @Override
        public void componentShown(ComponentEvent e) {

        }

        @Override
        public void componentHidden(ComponentEvent e) {

        }
    }
}

package pim.views.gui.sidebar;

import pim.model.Response;
import pim.model.orm.*;
import pim.views.gui.generic.*;
import pim.views.gui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;


/**
 * 显示 PIMEntity 列表的侧栏面板
 *
 * @author 罗阳豪 16130120191
 */
public class EntitiesListPanel extends SideBarContainPanel {
    private SideBarContainPanel lastPanel;

    private DefaultBtn returnBtn;
    private DefaultBtn newBtn;
    private JScrollPane entitiesContainer;

    public EntitiesListPanel(SideBarPanel panel, SideBarContainPanel lastPanel, PIMEntity[] entitiesList) {
        super(panel);
        this.lastPanel = lastPanel;

        this.setLayout(null);
        int width = MainFrame.sideBarWidth;

        // 设置顶部按钮
        this.returnBtn = new DefaultBtn("返回");
        this.newBtn = new DefaultBtn("新建");
        this.returnBtn.setBounds(20, 20, 58, 35);
        this.newBtn.setBounds(width - 78, 20, 58, 35);
        MineActionListener actionListener = new MineActionListener(this);
        this.returnBtn.addActionListener(actionListener);
        this.newBtn.addActionListener(actionListener);
        this.add(returnBtn);
        this.add(newBtn);


        // 设置滚动面板
        JPanel entitiesContain = new JPanel();
        entitiesContain.setLayout(null);

        // 设置每一条 PIMEntity 的显示
        entitiesContain.setPreferredSize(new Dimension(0, EntityItemPanel.height * entitiesList.length));
        entitiesContain.setBackground(Colors.DEFAULT_BG2);
        SideBarContainPanel this_panel = this;
        MouseListener mouseListener = new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (((EntityItemPanel) e.getSource()).entity.getUser().getEmail().equals(panel.mainFrame.getIface().getUser().getEmail())) {
                    panel.panelSwitch(new EntityDetailPanel(panel, this_panel, ((EntityItemPanel) e.getSource()).entity));
                }
                else {
                    JOptionPane.showMessageDialog(panel.mainFrame, "该信息创建人不是你，无法修改", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {
                ((EntityItemPanel) e.getSource()).setBackground(Colors.CAL_ITEM_HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ((EntityItemPanel) e.getSource()).setBackground(Colors.DEFAULT_BG);
            }
        };
        for (int i = 0; i < entitiesList.length; i++) {
            EntityItemPanel itemPanel = new EntityItemPanel(entitiesList[i]);
            itemPanel.setBounds(-1, (EntityItemPanel.height - 1) * i, width, EntityItemPanel.height);
            itemPanel.addMouseListener(mouseListener);
            entitiesContain.add(itemPanel);
        }

        this.entitiesContainer = new JScrollPane();
        this.entitiesContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.entitiesContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.entitiesContainer.setBounds(0, 65, width, this.panel.getHeight() - 65);
        this.entitiesContainer.setBorder(null);
        this.entitiesContainer.add(entitiesContain);
        this.entitiesContainer.setViewportView(entitiesContain);
        this.add(this.entitiesContainer);

    }

    void autoResize() {
        this.entitiesContainer.setBounds(0, 65, this.getWidth(), this.getHeight() - 65);
    }

    private static class EntityItemPanel extends JPanel {
        private PIMEntity entity;

        EntityItemPanel(PIMEntity entity) {
            this.setLayout(null);
            this.setBackground(Colors.DEFAULT_BG);
            this.setBorder(BorderFactory.createLineBorder(Colors.CAL_ITEM_BORDER, 1));
            this.entity = entity;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            int width = MainFrame.sideBarWidth - 16;

            // 设置类型字段
            JLabel typeLabel = new JLabel("未知", JLabel.LEFT);
            if (entity instanceof PIMTodo) {
                typeLabel.setText("待办");

                JLabel dateLabel = new JLabel(dateFormat.format(((PIMTodo) entity).getDate()));
                dateLabel.setFont(Fonts.DEFAULT);
                dateLabel.setBounds(5, 30, 200, 20);
                dateLabel.setForeground(Colors.CAL_ITEM_UNABLE_F);
                this.add(dateLabel);

                DefaultTextArea descTextLabel = new DefaultTextArea();
                descTextLabel.setText(((PIMTodo) entity).getText());
                descTextLabel.lockEdit();
                descTextLabel.setBounds(5, 55, width - 10, 55);
                descTextLabel.setBorder(null);
                this.add(descTextLabel);
            }
            else if (entity instanceof PIMNote) {
                typeLabel.setText("笔记");

                DefaultTextArea noteTextLabel = new DefaultTextArea();
                noteTextLabel.setText(((PIMNote) entity).getText());
                noteTextLabel.lockEdit();
                noteTextLabel.setBounds(5, 30, width - 10, 80);
                noteTextLabel.setBorder(null);
                this.add(noteTextLabel);
            }
            else if (entity instanceof PIMAppointment) {
                typeLabel.setText("预约");

                JLabel dateLabel = new JLabel(dateFormat.format(((PIMAppointment) entity).getDate()));
                dateLabel.setFont(Fonts.DEFAULT);
                dateLabel.setBounds(5, 30, 200, 20);
                dateLabel.setForeground(Colors.CAL_ITEM_UNABLE_F);
                this.add(dateLabel);

                DefaultTextArea descTextLabel = new DefaultTextArea();
                descTextLabel.setText(((PIMAppointment) entity).getDesc());
                descTextLabel.lockEdit();
                descTextLabel.setBounds(5, 55, width - 10, 55);
                descTextLabel.setBorder(null);
                this.add(descTextLabel);
            }
            else if (entity instanceof PIMContact) {
                typeLabel.setText("通讯录");

                JLabel nameLabel = new JLabel(((PIMContact) entity).getFirstName() + " " + ((PIMContact) entity).getLastName());
                JLabel emailLabel = new JLabel(((PIMContact) entity).getEmail());
                nameLabel.setBounds(5, 30, width - 10, 20);
                emailLabel.setBounds(5, 55, width - 10, 20);
                nameLabel.setFont(Fonts.DEFAULT);
                emailLabel.setFont(Fonts.DEFAULT);
                this.add(nameLabel);
                this.add(emailLabel);
            }
            typeLabel.setFont(Fonts.SIDEBAR_ENTITY_TYPE);
            typeLabel.setForeground(Colors.SIDEBAR_ENTITY_TYPE_F);
            typeLabel.setBounds(5, 5, 60, 20);
            this.add(typeLabel);

            // 设置优先级字段
            JLabel priorityLabel = new JLabel(entity.getPriority(), JLabel.LEFT);
            priorityLabel.setFont(Fonts.SIDEBAR_ENTITY_PRIORITY);
            priorityLabel.setForeground(Colors.SIDEBAR_ENTITY_PRIORITY_F);
            priorityLabel.setBounds(60, 5, width - 60, 20);
            this.add(priorityLabel);

        }

        static final int height = 120;
    }

    private static class MineActionListener implements ActionListener {
        private EntitiesListPanel panel;

        MineActionListener(EntitiesListPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object eventSource = e.getSource();
            if (eventSource == this.panel.returnBtn) {

                this.panel.panel.panelSwitch(this.panel.lastPanel);
            }
            else if (eventSource == this.panel.newBtn) {
                this.panel.panel.panelSwitch(new NewEntityPanel(this.panel.panel, this.panel, null));
            }
        }
    }
}

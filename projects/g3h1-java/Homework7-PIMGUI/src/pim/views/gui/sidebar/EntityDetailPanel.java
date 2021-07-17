package pim.views.gui.sidebar;

import pim.generic.User;
import pim.model.Response;
import pim.model.orm.*;
import pim.views.gui.generic.*;
import pim.views.gui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Pattern;


public class EntityDetailPanel extends SideBarContainPanel {

    private SideBarPanel panel;
    private SideBarContainPanel lastPanel;
    private PIMEntity entity;

    private DefaultBtn returnBtn;
    private DefaultBtn submitBtn;
    private DefaultBtn deleteBtn;
    private DefaultLabel typeLabel;
    private DefaultTextField priorityInput;


    private DefaultLabel dateLabel;
    private DefaultLabel descLabel;
    private DefaultTextField dateInput;
    private DefaultTextField descInput;
    private DefaultLabel textLabel;
    private DefaultTextArea textInput;
    private DefaultLabel nameLabel;
    private DefaultLabel emailLabel;
    private DefaultTextField firstNameInput;
    private DefaultTextField lastNameInput;
    private DefaultTextField emailInput;
    private DefaultLabel sharableLabel;
    private ButtonGroup sharableInput;
    private JRadioButton trueBtn;
    private JRadioButton falseBtn;

    EntityDetailPanel(SideBarPanel panel, SideBarContainPanel lastPanel, PIMEntity entity) {
        super(panel);
        this.panel = panel;
        this.lastPanel = lastPanel;
        this.entity = entity;

        this.setLayout(null);
        int width = MainFrame.sideBarWidth;

        // 设置顶部按钮
        this.returnBtn = new DefaultBtn("返回");
        this.submitBtn = new DefaultBtn("提交修改");
        this.deleteBtn = new DefaultBtn("删除");
        this.returnBtn.setBounds(20, 20, 58, 35);
        this.deleteBtn.setBounds(width - 184, 20, 58, 35);
        this.submitBtn.setBounds(width - 116, 20, 96, 35);
        MineActionListener actionListener = new MineActionListener(this);
        this.returnBtn.addActionListener(actionListener);
        this.submitBtn.addActionListener(actionListener);
        this.deleteBtn.addActionListener(actionListener);
        this.add(returnBtn);
        this.add(submitBtn);
        this.add(deleteBtn);

        // 优先级输入框
        DefaultLabel priorityLabel = new DefaultLabel("优先级");
        this.priorityInput = new DefaultTextField();
        this.priorityInput.setText(entity.getPriority());
        priorityLabel.setBounds(20, 108, 80, 32);
        this.priorityInput.setBounds(80, 108, width - 100, 32);
        this.add(priorityLabel);
        this.add(this.priorityInput);

        // 共享选项单选框
        this.sharableLabel = new DefaultLabel("共享");
        this.sharableInput = new ButtonGroup();
        this.trueBtn = new JRadioButton("是", entity.isShareable());
        this.falseBtn = new JRadioButton("否", !entity.isShareable());
        this.trueBtn.setFont(Fonts.INPUT);
        this.falseBtn.setFont(Fonts.INPUT);
        this.trueBtn.setBackground(Colors.DEFAULT_BG);
        this.falseBtn.setBackground(Colors.DEFAULT_BG);
        this.sharableInput.add(this.trueBtn);
        this.sharableInput.add(this.falseBtn);
        this.sharableLabel.setBounds(20, 249, 60, 32);
        this.trueBtn.setBounds(80, 249, 60, 32);
        this.falseBtn.setBounds(140, 249, 60, 32);
        this.add(sharableLabel);
        this.add(trueBtn);
        this.add(falseBtn);


        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.typeLabel = new DefaultLabel("");
        this.typeLabel.setBounds(20, 65, 200, 28);
        // 针对Todo和Appointment
        if (entity instanceof PIMTodo || entity instanceof PIMAppointment) {
            String date = null;
            String text = null;
            if (entity instanceof PIMTodo) {
                this.typeLabel.setText("类型：待办");

                date = dateFormat.format(((PIMTodo) entity).getDate());
                text = ((PIMTodo) entity).getText();
            }
            else {
                this.typeLabel.setText("类型：预约");

                date = dateFormat.format(((PIMAppointment) entity).getDate());
                text = ((PIMAppointment) entity).getDesc();
            }

            // 日期输入框
            this.dateLabel = new DefaultLabel("日期");
            this.dateInput = new DefaultTextField() {
                @Override
                protected void paintComponent(final Graphics pG) {
                    super.paintComponent(pG);

                    if (this.getText().length() > 0) {
                        return;
                    }

                    final Graphics2D g = (Graphics2D) pG;
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Colors.TOP_BAR_BTN_UNABLE_F);
                    g.drawString("e.g. 2018-01-01", getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            };
            this.dateInput.setText(date);
            this.dateLabel.setBounds(20, 155, 80, 32);
            this.dateInput.setBounds(80, 155, width - 100, 32);
            this.add(this.dateLabel);
            this.add(this.dateInput);

            // 描述文本输入框
            this.descLabel = new DefaultLabel("描述");
            this.descInput = new DefaultTextField();
            this.descInput.setText(text);
            this.descLabel.setBounds(20, 202, 80, 32);
            this.descInput.setBounds(80, 202, width - 100, 32);
            this.add(this.descLabel);
            this.add(this.descInput);
        }
        // 针对 PIMNote
        else if (entity instanceof PIMNote) {
            this.typeLabel.setText("类型：笔记");

            // 内容文本输入框
            this.textLabel = new DefaultLabel("笔记");
            this.textInput = new DefaultTextArea();
            this.textInput.setText(((PIMNote) entity).getText());
            this.textLabel.setBounds(20, 155, 80, 32);
            this.textInput.setBounds(80, 155, width - 100, 160);
            this.add(this.textLabel);
            this.add(this.textInput);

            this.sharableLabel.setBounds(20, 330, 60, 32);
            this.trueBtn.setBounds(80, 330, 60, 32);
            this.falseBtn.setBounds(140, 330, 60, 32);
        }
        else if (entity instanceof PIMContact) {
            this.typeLabel.setText("类型：通讯录");

            this.nameLabel = new DefaultLabel("姓名");
            this.emailLabel = new DefaultLabel("邮箱");
            this.firstNameInput = new DefaultTextField() {
                @Override
                protected void paintComponent(final Graphics pG) {
                    super.paintComponent(pG);

                    if (this.getText().length() > 0) {
                        return;
                    }

                    final Graphics2D g = (Graphics2D) pG;
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Colors.TOP_BAR_BTN_UNABLE_F);
                    g.drawString("名", getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            };
            this.lastNameInput = new DefaultTextField() {
                @Override
                protected void paintComponent(final Graphics pG) {
                    super.paintComponent(pG);

                    if (this.getText().length() > 0) {
                        return;
                    }

                    final Graphics2D g = (Graphics2D) pG;
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(Colors.TOP_BAR_BTN_UNABLE_F);
                    g.drawString("姓", getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
                }
            };
            this.emailInput = new DefaultTextField();
            this.firstNameInput.setText(((PIMContact) entity).getFirstName());
            this.lastNameInput.setText(((PIMContact) entity).getLastName());
            this.emailInput.setText(((PIMContact) entity).getEmail());
            this.nameLabel.setBounds(20, 155, 80, 32);
            this.lastNameInput.setBounds(80, 155, 80, 32);
            this.firstNameInput.setBounds(160, 155, width - 180, 32);
            this.emailLabel.setBounds(20, 202, 80, 32);
            this.emailInput.setBounds(80, 202, width - 100, 32);
            this.add(this.nameLabel);
            this.add(this.emailLabel);
            this.add(this.firstNameInput);
            this.add(this.lastNameInput);
            this.add(this.emailInput);
        }
        this.add(typeLabel);

    }

    private static class MineActionListener implements ActionListener {
        private EntityDetailPanel panel;

        MineActionListener(EntityDetailPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object eventSource = e.getSource();
            if (eventSource == this.panel.returnBtn) {
                this.panel.panel.panelSwitch(this.panel.lastPanel);
            }
            else if (eventSource == this.panel.deleteBtn) {
                if (this.panel.panel.mainFrame.getIface().getDataAPI().delEntity(this.panel.entity.getId()).statusCode == 200) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "未知错误，删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
                this.panel.panel.panelSwitch(new UserInfoPanel(this.panel.panel));

            }
            else if (eventSource == this.panel.submitBtn) {
                this.panel.entity.setPriority(this.panel.priorityInput.getText());
                this.panel.entity.setShareable(this.panel.trueBtn.isSelected());

                try {
                    if (this.panel.entity instanceof PIMTodo) {
                        ((PIMTodo) this.panel.entity).setDate((new SimpleDateFormat("yyyy-MM-dd")).parse(this.panel.dateInput.getText()));
                        ((PIMTodo) this.panel.entity).setText(this.panel.descInput.getText());
                    }
                    else if (this.panel.entity instanceof PIMAppointment) {
                        ((PIMAppointment) this.panel.entity).setDate((new SimpleDateFormat("yyyy-MM-dd")).parse(this.panel.dateInput.getText()));
                        ((PIMAppointment) this.panel.entity).setDesc(this.panel.descInput.getText());
                    }
                    else if (this.panel.entity instanceof PIMNote) {
                        ((PIMNote) this.panel.entity).setText(this.panel.textInput.getText());
                    }
                    else if (this.panel.entity instanceof PIMContact) {
                        ((PIMContact) this.panel.entity).setFirstName(this.panel.firstNameInput.getText());
                        ((PIMContact) this.panel.entity).setLastName(this.panel.lastNameInput.getText());
                        ((PIMContact) this.panel.entity).setEmail(this.panel.emailInput.getText());
                    }
                    else {
                        return;
                    }
                }
                catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "输入日期格式错误！", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (this.panel.panel.mainFrame.getIface().getDataAPI().editEntity(this.panel.entity).statusCode == 200) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "修改成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "未知错误，删除失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
                this.panel.panel.panelSwitch(new UserInfoPanel(this.panel.panel));
            }
        }
    }
}

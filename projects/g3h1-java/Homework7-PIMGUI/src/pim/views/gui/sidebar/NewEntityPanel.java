package pim.views.gui.sidebar;

import pim.generic.User;
import pim.model.Response;
import pim.model.orm.*;
import pim.views.gui.generic.*;
import pim.views.gui.main.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * 新建 PIMEntity 的侧栏面板
 *
 * @author 罗阳豪 16130120191
 */
class NewEntityPanel extends SideBarContainPanel {
    private SideBarContainPanel lastPanel;

    private DefaultBtn returnBtn;
    private DefaultBtn submitBtn;
    private DefaultComboBox<String> typeInput;
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


    NewEntityPanel(SideBarPanel panel, SideBarContainPanel lastPanel, PIMEntity entity) {
        super(panel);
        this.lastPanel = lastPanel;

        this.setLayout(null);
        int width = MainFrame.sideBarWidth;

        // 设置顶部按钮
        this.returnBtn = new DefaultBtn("返回");
        this.submitBtn = new DefaultBtn("提交");
        this.returnBtn.setBounds(20, 20, 58, 35);
        this.submitBtn.setBounds(width - 78, 20, 58, 35);
        MineActionListener actionListener = new MineActionListener(this);
        this.returnBtn.addActionListener(actionListener);
        this.submitBtn.addActionListener(actionListener);
        this.add(returnBtn);
        this.add(submitBtn);

        // 类型选择框
        DefaultLabel typeLabel = new DefaultLabel("类型");
        this.typeInput = new DefaultComboBox<String>();
        this.typeInput.addItem("代办");
        this.typeInput.addItem("笔记");
        this.typeInput.addItem("预约");
        this.typeInput.addItem("通讯录");
        typeLabel.setBounds(20, 65, 80, 28);
        this.typeInput.setBounds(80, 65, 120, 28);
        ComboListener comboListener = new ComboListener(this);
        this.typeInput.addItemListener(comboListener);
        this.add(typeLabel);
        this.add(this.typeInput);

        // 优先级输入框
        DefaultLabel priorityLabel = new DefaultLabel("优先级");
        this.priorityInput = new DefaultTextField();
        this.priorityInput.setText("Normal");
        priorityLabel.setBounds(20, 108, 80, 32);
        this.priorityInput.setBounds(80, 108, width - 100, 32);
        this.add(priorityLabel);
        this.add(this.priorityInput);

        // 日期输入框（针对Todo和Appointment）
        this.dateLabel = new DefaultLabel("日期") {
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
        this.dateInput = new DefaultTextField();
        this.dateLabel.setBounds(20, 155, 80, 32);
        this.dateInput.setBounds(80, 155, width - 100, 32);
        this.dateLabel.setVisible(false);
        this.dateInput.setVisible(false);
        this.add(this.dateLabel);
        this.add(this.dateInput);

        // 描述文本输入框（针对Todo和Appointment）
        this.descLabel = new DefaultLabel("描述");
        this.descInput = new DefaultTextField();
        this.descLabel.setBounds(20, 202, 80, 32);
        this.descInput.setBounds(80, 202, width - 100, 32);
        this.descLabel.setVisible(false);
        this.descInput.setVisible(false);
        this.add(this.descLabel);
        this.add(this.descInput);

        // 内容文本输入框（针对 Note）
        this.textLabel = new DefaultLabel("笔记");
        this.textInput = new DefaultTextArea();
        this.textLabel.setBounds(20, 155, 80, 32);
        this.textInput.setBounds(80, 155, width - 100, 160);
        this.textLabel.setVisible(false);
        this.textInput.setVisible(false);
        this.add(this.textLabel);
        this.add(this.textInput);

        // 通讯录三件套字段（针对 Contact）
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
        this.nameLabel.setBounds(20, 155, 80, 32);
        this.lastNameInput.setBounds(80, 155, 80, 32);
        this.firstNameInput.setBounds(160, 155, width - 180, 32);
        this.emailLabel.setBounds(20, 202, 80, 32);
        this.emailInput.setBounds(80, 202, width - 100, 32);
        this.nameLabel.setVisible(false);
        this.emailLabel.setVisible(false);
        this.firstNameInput.setVisible(false);
        this.lastNameInput.setVisible(false);
        this.emailInput.setVisible(false);
        this.add(this.nameLabel);
        this.add(this.emailLabel);
        this.add(this.firstNameInput);
        this.add(this.lastNameInput);
        this.add(this.emailInput);

        // 共享选项单选框
        this.sharableLabel = new DefaultLabel("共享");
        this.sharableInput = new ButtonGroup();
        this.trueBtn = new JRadioButton("是", false);
        this.falseBtn = new JRadioButton("否", true);
        this.trueBtn.setFont(Fonts.INPUT);
        this.falseBtn.setFont(Fonts.INPUT);
        this.trueBtn.setBackground(Colors.DEFAULT_BG);
        this.falseBtn.setBackground(Colors.DEFAULT_BG);
        this.sharableInput.add(this.trueBtn);
        this.sharableInput.add(this.falseBtn);
        this.add(sharableLabel);
        this.add(trueBtn);
        this.add(falseBtn);

        this.switchType("代办");
    }

    private void switchType(String type) {
        switch (type) {
            case "代办":
                this.dateLabel.setVisible(true);
                this.dateInput.setVisible(true);

                this.descLabel.setVisible(true);
                this.descInput.setVisible(true);

                this.textLabel.setVisible(false);
                this.textInput.setVisible(false);

                this.nameLabel.setVisible(false);
                this.emailLabel.setVisible(false);
                this.firstNameInput.setVisible(false);
                this.lastNameInput.setVisible(false);
                this.emailInput.setVisible(false);

                this.sharableLabel.setBounds(20, 249, 60, 32);
                this.trueBtn.setBounds(80, 249, 60, 32);
                this.falseBtn.setBounds(140, 249, 60, 32);

                break;
            case "笔记":
                this.dateLabel.setVisible(false);
                this.dateInput.setVisible(false);

                this.descLabel.setVisible(false);
                this.descInput.setVisible(false);

                this.textLabel.setVisible(true);
                this.textInput.setVisible(true);

                this.nameLabel.setVisible(false);
                this.emailLabel.setVisible(false);
                this.firstNameInput.setVisible(false);
                this.lastNameInput.setVisible(false);
                this.emailInput.setVisible(false);

                this.sharableLabel.setBounds(20, 330, 60, 32);
                this.trueBtn.setBounds(80, 330, 60, 32);
                this.falseBtn.setBounds(140, 330, 60, 32);

                break;
            case "预约":
                this.dateLabel.setVisible(true);
                this.dateInput.setVisible(true);

                this.descLabel.setVisible(true);
                this.descInput.setVisible(true);

                this.textLabel.setVisible(false);
                this.textInput.setVisible(false);

                this.nameLabel.setVisible(false);
                this.emailLabel.setVisible(false);
                this.firstNameInput.setVisible(false);
                this.lastNameInput.setVisible(false);
                this.emailInput.setVisible(false);

                this.sharableLabel.setBounds(20, 249, 60, 32);
                this.trueBtn.setBounds(80, 249, 60, 32);
                this.falseBtn.setBounds(140, 249, 60, 32);

                break;
            case "通讯录":
                this.dateLabel.setVisible(false);
                this.dateInput.setVisible(false);

                this.descLabel.setVisible(false);
                this.descInput.setVisible(false);

                this.textLabel.setVisible(false);
                this.textInput.setVisible(false);

                this.nameLabel.setVisible(true);
                this.emailLabel.setVisible(true);
                this.firstNameInput.setVisible(true);
                this.lastNameInput.setVisible(true);
                this.emailInput.setVisible(true);

                this.sharableLabel.setBounds(20, 249, 60, 32);
                this.trueBtn.setBounds(80, 249, 60, 32);
                this.falseBtn.setBounds(140, 249, 60, 32);

                break;
        }
    }

    private static class MineActionListener implements ActionListener {
        private NewEntityPanel panel;

        MineActionListener(NewEntityPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Object eventSource = e.getSource();
            if (eventSource == this.panel.returnBtn) {

                this.panel.panel.panelSwitch(this.panel.lastPanel);
            }
            else if (eventSource == this.panel.submitBtn) {

                User mine = this.panel.panel.mainFrame.getIface().getUser();
                String priority = this.panel.priorityInput.getText();
                if (priority.length() == 0) {
                    priority = "Normal";
                }
                PIMEntity entity = null;
                Pattern datePattern = Pattern.compile("^([0-9]+?)-[0-9]{2}-[0-9]{2}$");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                boolean sharable = this.panel.sharableInput.isSelected(this.panel.trueBtn.getModel());
                try {
                    switch ((String) Objects.requireNonNull(this.panel.typeInput.getSelectedItem())) {
                        case "代办":
                            entity = new PIMTodo(
                                    0, mine, sharable, priority,
                                    dateFormat.parse(this.panel.dateInput.getText()),
                                    this.panel.descInput.getText()
                            );
                            break;
                        case "笔记":
                            entity = new PIMNote(
                                    0, mine, sharable, priority,
                                    this.panel.textInput.getText()
                            );
                            break;
                        case "预约":
                            entity = new PIMAppointment(
                                    0, mine, sharable, priority,
                                    dateFormat.parse(this.panel.dateInput.getText()),
                                    this.panel.descInput.getText()
                            );
                            break;
                        case "通讯录":
                            entity = new PIMContact(
                                    0, mine, sharable, priority,
                                    this.panel.firstNameInput.getText(),
                                    this.panel.lastNameInput.getText(),
                                    this.panel.emailInput.getText()
                            );
                            break;
                    }
                }
                catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "输入日期格式错误！", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Response res = this.panel.panel.mainFrame.getIface().getDataAPI().addEntity(entity);
                if (res.statusCode != 200) {
                    throw new RuntimeException("数据库返回非预期状态码");
                }

                JOptionPane.showMessageDialog(this.panel.panel.mainFrame, "添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                this.panel.panel.panelSwitch(new UserInfoPanel(this.panel.panel));
            }
        }
    }


    private static class ComboListener implements ItemListener {
        private NewEntityPanel panel;

        ComboListener(NewEntityPanel panel) {
            this.panel = panel;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this.panel.switchType((String) Objects.requireNonNull(this.panel.typeInput.getSelectedItem()));
            }
        }
    }
}

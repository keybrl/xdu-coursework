package libms.views.admin;

import libms.model.DataAPI;
import libms.model.Response;
import libms.model.orm.User;
import libms.views.Colors;
import libms.views.DefaultBtn;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * 用户信息面板
 *
 * @author keybrl
 */
class UsersPanel extends RightPanel {
    private DataAPI dataAPI;

    private JScrollPane userListPanel;
    private JTextField searchInput;

    UsersPanel(DataAPI dataAPI) {
        super();
        this.dataAPI = dataAPI;

        this.initGUI();
    }

    private void initGUI() {
        JPanel searchBar = new JPanel();
        JPanel mainPanel = new JPanel();
        this.userListPanel = new UserListPanel();
        this.searchInput = new JTextField();
        JButton searchBtn = new DefaultBtn("查询姓名");

        searchBar.setBounds(0, 0, RightPanel.panelWidth, UsersPanel.searchBarHeight);
        mainPanel.setBounds(0, UsersPanel.searchBarHeight, RightPanel.panelWidth, RightPanel.panelHeight - UsersPanel.searchBarHeight);
        searchBar.setLayout(null);
        this.searchInput.setBounds(
                UsersPanel.searchInputOffset,
                (UsersPanel.searchBarHeight - UsersPanel.searchInputHeight) / 2,
                UsersPanel.searchInputWidth,
                UsersPanel.searchInputHeight
        );
        searchBtn.setBounds(
                UsersPanel.searchInputOffset + UsersPanel.searchInputWidth + UsersPanel.searchBtnOffset,
                (UsersPanel.searchBarHeight - UsersPanel.searchInputHeight) / 2,
                UsersPanel.searchBtnWidth,
                UsersPanel.searchInputHeight
        );


        searchBar.setBackground(Color.WHITE);
        this.searchInput.setBackground(Color.WHITE);
        this.searchInput.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
        this.searchInput.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.userListPanel.setBackground(Colors.DEFAULT_BG);

        ActionListener actionListener = new SearchActionListener(this);
        searchBtn.addActionListener(actionListener);

        searchBar.add(this.searchInput);
        searchBar.add(searchBtn);

        mainPanel.setLayout(null);
        mainPanel.add(this.userListPanel);

        this.add(searchBar);
        this.add(mainPanel);

    }

    void getUserList() {
        String searchText = this.searchInput.getText();

        Response res = null;
        if (searchText.length() == 0) {
            res = this.dataAPI.getUsers();
        }
        else {
            res = this.dataAPI.getUsersByName(searchText);
        }

        if (res == null) {
            JOptionPane.showMessageDialog(null, "数据库已无言，难以理解的异常！");
            System.out.println("UsersPanel: 数据接口返回null");
        }
        else if (res.statusCode == 200 && res.data != null) {
            // 查询成功
            ((UserListPanel) this.userListPanel).reloadList((User[]) res.data);
        }
        else {
            JOptionPane.showMessageDialog(null, "你做了什么？为什么会这样？这是难以理解的异常！");
            System.out.println("UsersPanel: 数据接口返回非200状态或数据集为null");
        }
    }


    static int searchBarHeight = 120;
    private static int searchInputHeight = 40;
    private static int searchInputWidth = 600;
    private static int searchBtnWidth = 100;
    private static int searchBtnOffset = 5;
    private static int searchInputOffset = (RightPanel.panelWidth - UsersPanel.searchInputWidth - UsersPanel.searchBtnWidth - UsersPanel.searchBtnOffset) / 2;

    private static class SearchActionListener implements ActionListener {
        private UsersPanel panel;

        SearchActionListener(UsersPanel panel) {
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent event) {
            this.panel.getUserList();
        }
    }
}


class UserListPanel extends JScrollPane {
    private JPanel panel;

    UserListPanel() {

        this.setBounds(0, 0, RightPanel.panelWidth, RightPanel.panelHeight - UsersPanel.searchBarHeight - 30);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(null);

        this.panel = new JPanel();
        this.panel.setLayout(null);
        this.panel.setBackground(Colors.DEFAULT_BG);

        this.add(this.panel);
        this.setViewportView(panel);
    }

    void reloadList(User[] users) {
        this.panel.removeAll();
        this.panel.setPreferredSize(new Dimension(RightPanel.panelWidth, (itemPanelHeight - 1) * (users.length + 1) + 100));

        for (int i = -1; i < users.length; i++) {

            JPanel item = new JPanel();
            item.setLayout(null);

            JLabel userIdLabel = new JLabel(i == -1 ? "借书号" : users[i].id);
            JLabel nameLabel = new JLabel(i == -1 ? "姓名" : users[i].name);
            JLabel typeLabel = new JLabel(i == -1 ? "类型" : users[i].type);
            JLabel emailLabel = new JLabel(i == -1 ? "E-Mail" : users[i].email);
            JLabel phoneLabel = new JLabel(i == -1 ? "电话" : users[i].phoneNum);
            JLabel addrLabel = new JLabel(i == -1 ? "住址" : users[i].address);
            JLabel unitLabel = new JLabel(i == -1 ? "单位" : users[i].unit);

            Component detailBtn = null;
            if (i == -1) {
                detailBtn = new JLabel("查看详情", JLabel.CENTER);
            }
            else {
                detailBtn = new DefaultBtn("详情");
                ((JButton) detailBtn).addActionListener(new DetailBtnListener(users[i]));
            }

            // 设置元素摆放位置
            int top = (i + 1) * (itemPanelHeight - 1) + 30;
            int left = 10;
            item.setBounds(20, top, RightPanel.panelWidth - 56, itemPanelHeight);
            userIdLabel.setBounds(left, 0, userIdLabelWidth, itemPanelHeight);
            left += userIdLabelWidth + 10;
            nameLabel.setBounds(left, 0, nameLabelWidth, itemPanelHeight);
            left += nameLabelWidth + 10;
            typeLabel.setBounds(left, 0, typeLabelWidth, itemPanelHeight);
            left += typeLabelWidth + 10;
            phoneLabel.setBounds(left, 0, phoneLabelWidth, itemPanelHeight);
            left += phoneLabelWidth + 10;
            emailLabel.setBounds(left, 0, emailLabelWidth, itemPanelHeight);
            left += emailLabelWidth + 10;
            addrLabel.setBounds(left, 0, addrLabelWidth, itemPanelHeight);
            left += addrLabelWidth + 10;
            unitLabel.setBounds(left, 0, unitLabelWidth, itemPanelHeight);
            left += unitLabelWidth + 10;
            detailBtn.setBounds(left, 0, RightPanel.panelWidth - 56 - left, itemPanelHeight);


            // 设置边框
            item.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
            item.setBackground(Color.WHITE);


            // 设置字体
            Font font = new Font(Font.DIALOG, Font.PLAIN, 14);
            userIdLabel.setFont(font);
            nameLabel.setFont(font);
            typeLabel.setFont(font);
            emailLabel.setFont(font);
            phoneLabel.setFont(font);
            addrLabel.setFont(font);
            unitLabel.setFont(font);


            if (i == -1) {
                ((JLabel) detailBtn).setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
                detailBtn.setFont(font);
            }


            // 将元素插入列表
            item.add(userIdLabel);
            item.add(nameLabel);
            item.add(typeLabel);
            item.add(phoneLabel);
            item.add(emailLabel);
            item.add(addrLabel);
            item.add(unitLabel);
            item.add(detailBtn);

            this.panel.add(item);
        }

        // 重刷页面
        this.panel.updateUI();
        this.panel.repaint();
    }

    private static final int itemPanelHeight = 48;
    private static final int userIdLabelWidth = 60;
    private static final int nameLabelWidth = 100;
    private static final int typeLabelWidth = 60;
    private static final int phoneLabelWidth = 100;
    private static final int emailLabelWidth = 200;
    private static final int addrLabelWidth = 200;
    private static final int unitLabelWidth = 200;

    private static class DetailBtnListener implements ActionListener {
        User user;

        DetailBtnListener(User user) {
            this.user = user;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            JOptionPane.showMessageDialog(null,
            "用户详细信息：\n" +
                    "借书号：" + this.user.id + "\n" +
                    "姓名：" + this.user.name + "\n" +
                    "类型：" + this.user.type + "\n" +
                    "E-Mail：" + this.user.email + "\n" +
                    "电话：" + this.user.phoneNum + "\n" +
                    "地址：" + this.user.address + "\n" +
                    "单位：" + this.user.unit
            );
        }
    }
}

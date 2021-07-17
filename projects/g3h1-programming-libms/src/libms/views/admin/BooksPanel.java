package libms.views.admin;

import libms.model.DataAPI;
import libms.model.Response;
import libms.model.orm.BookInfo;
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
 * 书本信息面板
 *
 * @author keybrl
 */
class BooksPanel extends RightPanel {
    private DataAPI dataAPI;

    private JScrollPane booksListPanel;

    private JButton searchBtn;
    private JButton addBookInfoBtn;
    private JTextField searchInput;


    BooksPanel(DataAPI dataAPI) {
        super();
        this.dataAPI = dataAPI;

        this.initGUI();
    }


    private void initGUI() {
        JPanel searchBar = new JPanel();
        JPanel mainPanel = new JPanel();
        this.booksListPanel = new BooksInfoListPanel(this.dataAPI);
        this.searchInput = new JTextField();
        this.searchBtn = new DefaultBtn("搜索");
        this.addBookInfoBtn = new DefaultBtn("新建");

        searchBar.setBounds(0, 0, RightPanel.panelWidth, BooksPanel.searchBarHeight);
        mainPanel.setBounds(0, BooksPanel.searchBarHeight, RightPanel.panelWidth, RightPanel.panelHeight - BooksPanel.searchBarHeight);
        searchBar.setLayout(null);
        this.searchInput.setBounds(
                BooksPanel.searchInputOffset,
                (BooksPanel.searchBarHeight - BooksPanel.searchInputHeight) / 2,
                BooksPanel.searchInputWidth,
                BooksPanel.searchInputHeight
        );
        this.searchBtn.setBounds(
                BooksPanel.searchInputOffset + BooksPanel.searchInputWidth + BooksPanel.searchBtnOffset,
                (BooksPanel.searchBarHeight - BooksPanel.searchInputHeight) / 2,
                BooksPanel.searchBtnWidth,
                BooksPanel.searchInputHeight
        );
        this.addBookInfoBtn.setBounds(
                BooksPanel.searchInputOffset + BooksPanel.searchInputWidth + BooksPanel.searchBtnOffset + BooksPanel.searchBtnWidth + 20,
                (BooksPanel.searchBarHeight - BooksPanel.searchInputHeight) / 2,
                BooksPanel.searchBtnWidth,
                BooksPanel.searchInputHeight
        );

        searchBar.setBackground(Color.WHITE);
        this.searchInput.setBackground(Color.WHITE);
        this.searchInput.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
        this.searchInput.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));

        ActionListener actionListener = new SearchActionListener(this);
        this.searchBtn.addActionListener(actionListener);
        this.addBookInfoBtn.addActionListener(actionListener);

        searchBar.add(this.searchInput);
        searchBar.add(this.searchBtn);
        searchBar.add(this.addBookInfoBtn);

        mainPanel.setLayout(null);
        mainPanel.add(this.booksListPanel);

        this.add(searchBar);
        this.add(mainPanel);
    }


    static int searchBarHeight = 120;
    private static int searchInputHeight = 40;
    private static int searchInputWidth = 600;
    private static int searchBtnWidth = 100;
    private static int searchBtnOffset = 5;
    private static int searchInputOffset = (RightPanel.panelWidth - BooksPanel.searchInputWidth - BooksPanel.searchBtnWidth - BooksPanel.searchBtnOffset) / 2;

    private static class SearchActionListener implements ActionListener {
        private BooksPanel panel;

        SearchActionListener(BooksPanel panel) {
            this.panel = panel;
        }

        public void actionPerformed(ActionEvent event) {
            Object eventSource = event.getSource();
            if (eventSource == this.panel.searchBtn) {
                String searchText = this.panel.searchInput.getText();

                Response res = null;

                if (searchText.length() == 0) {
                    res = this.panel.dataAPI.getBooksInfo();
                }
                else {
                    res = this.panel.dataAPI.getBooksInfoByKey(searchText);
                }

                if (res == null) {
                    JOptionPane.showMessageDialog(null, "数据库已无言，难以理解的异常！");
                    System.out.println("BooksPanel: 数据接口返回null");
                }
                else if (res.statusCode == 200 && res.data != null) {
                    // 查询成功
                    ((BooksInfoListPanel)this.panel.booksListPanel).reloadList((BookInfo[])res.data);
                }
                else {
                    JOptionPane.showMessageDialog(null, "你做了什么？为什么会这样？这是难以理解的异常！");
                    System.out.println("BooksPanel: 数据接口返回非200状态或数据集为null");
                }
            }
            else if (eventSource == this.panel.addBookInfoBtn) {
                String isbn = JOptionPane.showInputDialog("请输入需要新建的书本的ISBN：");
                Response res = this.panel.dataAPI.addBookInfo(isbn);

                if (res.statusCode == 200) {
                    JOptionPane.showMessageDialog(null, "书本信息创建成功！请前往修改具体信息。");
                }
                else if (res.statusCode == 403) {
                    JOptionPane.showMessageDialog(null, "书本信息已存在！");
                }
                else if (res.statusCode == 400) {
                    JOptionPane.showMessageDialog(null, "ISBN格式错误，书本信息未创建！");
                }
                else {
                    JOptionPane.showMessageDialog(null, "未知错误，书本信息未创建");
                }
            }


        }
    }
}


class BooksInfoListPanel extends JScrollPane {
    private DataAPI dataAPI;
    private JPanel panel;

    BooksInfoListPanel(DataAPI dataAPI) {
        this.dataAPI = dataAPI;

        this.setBounds(0, 0, RightPanel.panelWidth, RightPanel.panelHeight - BooksPanel.searchBarHeight - 30);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(null);

        this.panel = new JPanel();
        this.panel.setLayout(null);
        this.panel.setBackground(Colors.DEFAULT_BG);

        this.add(this.panel);
        this.setViewportView(panel);
    }

    void reloadList(BookInfo[] booksInfo) {
        this.panel.removeAll();
        this.panel.setPreferredSize(new Dimension(RightPanel.panelWidth, (itemPanelHeight - 1) * (booksInfo.length + 1) + 100));

        for (int i = -1; i < booksInfo.length; i++) {

            JPanel item = new JPanel();
            item.setLayout(null);

            JLabel isbnLabel = new JLabel(i == -1 ? "ISBN" : booksInfo[i].isbn);
            JLabel nameLabel = new JLabel(i == -1 ? "书名" : booksInfo[i].name);
            JLabel authorLabel = new JLabel(i == -1 ? "作者" : booksInfo[i].author);
            JLabel priceLabel = new JLabel(i == -1 ? "价格" : booksInfo[i].price.toString());
            JLabel categoryLabel = new JLabel(i == -1 ? "分类" : booksInfo[i].category);
            JLabel availableLabel = new JLabel(i == -1 ? "可借/总数" : booksInfo[i].available + " / " + booksInfo[i].total);

            Component detailBtn = null;
            if (i == -1) {
                detailBtn = new JLabel("查看详情", JLabel.CENTER);
            }
            else {
                detailBtn = new DefaultBtn("详情");
                ((JButton) detailBtn).addActionListener(new DetailBtnListener(booksInfo[i], this.dataAPI));
            }

            // 设置元素摆放位置
            int top = (i + 1) * (itemPanelHeight - 1) + 30;
            int left = 20;
            item.setBounds(20, top, RightPanel.panelWidth - 56, itemPanelHeight);
            isbnLabel.setBounds(left, 0, isbnLabelWidth, itemPanelHeight);
            left += isbnLabelWidth + 20;
            nameLabel.setBounds(left, 0, nameLabelWidth, itemPanelHeight);
            left += nameLabelWidth + 20;
            authorLabel.setBounds(left, 0, authorLabelWidth, itemPanelHeight);
            left += authorLabelWidth + 20;
            categoryLabel.setBounds(left, 0, categoryLabelWidth, itemPanelHeight);
            left += categoryLabelWidth + 20;
            priceLabel.setBounds(left, 0, priceLabelWidth, itemPanelHeight);
            left += priceLabelWidth + 20;
            availableLabel.setBounds(left, 0, availableLabelWidth, itemPanelHeight);
            left += availableLabelWidth + 20;
            detailBtn.setBounds(left, 0, RightPanel.panelWidth - 56 - left, itemPanelHeight);


            // 设置边框
            item.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
            item.setBackground(Color.WHITE);


            // 设置字体
            Font font = new Font(Font.DIALOG, Font.PLAIN, 14);
            isbnLabel.setFont(font);
            nameLabel.setFont(font);
            authorLabel.setFont(font);
            priceLabel.setFont(font);
            categoryLabel.setFont(font);
            availableLabel.setFont(font);


            if (i == -1) {
                ((JLabel) detailBtn).setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
                detailBtn.setFont(font);
            }


            // 将元素插入列表
            item.add(isbnLabel);
            item.add(nameLabel);
            item.add(authorLabel);
            item.add(categoryLabel);
            item.add(priceLabel);
            item.add(availableLabel);
            item.add(detailBtn);

            this.panel.add(item);
        }

        // 重刷页面
        this.panel.updateUI();
        this.panel.repaint();
    }

    private static final int itemPanelHeight = 48;
    private static final int isbnLabelWidth = 150;
    private static final int nameLabelWidth = 300;
    private static final int authorLabelWidth = 200;
    private static final int categoryLabelWidth = 100;
    private static final int priceLabelWidth = 50;
    private static final int availableLabelWidth = 80;

    private static class DetailBtnListener implements ActionListener {
        BookInfo bookinfo;
        DataAPI dataAPI;

        DetailBtnListener(BookInfo bookInfo, DataAPI dataAPI) {
            this.bookinfo = bookInfo;
            this.dataAPI = dataAPI;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            new BookDetail(this.dataAPI, this.bookinfo);
        }
    }
}

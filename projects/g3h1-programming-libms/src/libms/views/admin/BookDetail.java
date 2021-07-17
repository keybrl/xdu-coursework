package libms.views.admin;

import libms.model.DataAPI;
import libms.model.Response;
import libms.model.orm.Book;
import libms.model.orm.BookInfo;

import libms.views.Colors;
import libms.views.DefaultBtn;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 书本详情页面
 *
 * @author keybrl
 */
class BookDetail extends JFrame {

    //  数据接口
    private DataAPI dataAPI;
    private BookInfo bookInfo;

    private Container container;

    private JPanel mainPaneContainer;

    private JPanel bookListPanel;

    private JLabel noBookLabel;

    private JTextField nameInput;
    private JTextField authorInput;
    private JTextField priceInput;
    private JTextField categoryInput;

    private JButton refreshBtn;
    private JButton submitBtn;
    private JButton addBookBtn;
    private JButton deleteBookInfoBtn;


    BookDetail(DataAPI dataAPI, BookInfo bookInfo) {
        super("图书管理系统 - 书本详情");
        this.dataAPI = dataAPI;
        this.bookInfo = bookInfo;

        this.setSize(BookDetail.frameWidth, BookDetail.frameHeight);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        this.container = this.getContentPane();
        this.container.setLayout(null);

        this.initGUI();
    }


    private void initGUI() {
        // 创建页面元素
        JScrollPane mainPane = new JScrollPane();
        this.mainPaneContainer = new JPanel();
        this.mainPaneContainer.setLayout(null);
        JPanel bookInfoPanel = new JPanel();
        bookInfoPanel.setLayout(null);
        this.bookListPanel = new JPanel();
        this.bookListPanel.setLayout(null);

        JLabel isbnLabel = new JLabel("ISBN");
        JLabel nameLabel = new JLabel("书名");
        JLabel authorLabel = new JLabel("作者");
        JLabel priceLabel = new JLabel("价格");
        JLabel categoryLabel = new JLabel("分类");
        JLabel availableLabel = new JLabel("可借/总数");

        JLabel isbnValue = new JLabel(this.bookInfo.isbn);
        this.nameInput = new JTextField(this.bookInfo.name);
        this.authorInput = new JTextField(this.bookInfo.author);
        this.priceInput = new JTextField(this.bookInfo.price.toString());
        this.categoryInput = new JTextField(this.bookInfo.category);
        JLabel availableValue = new JLabel(this.bookInfo.available + " / " + this.bookInfo.total);

        this.refreshBtn = new DefaultBtn("刷新");
        this.submitBtn = new DefaultBtn("提交修改");
        this.addBookBtn = new DefaultBtn("添加书本");
        this.deleteBookInfoBtn = new DefaultBtn("删除信息");

        JLabel bookIdLabel = new JLabel("书本ID", JLabel.CENTER);
        JLabel holderLabel = new JLabel("持书人", JLabel.CENTER);
        JLabel deleteBookLabel = new JLabel("下架该书", JLabel.CENTER);
        this.noBookLabel = new JLabel("正在获取书本列表...");


        // 设置大小
        int bookListPanelHeight = 76;

        mainPane.setBounds(0, 0, BookDetail.frameWidth, BookDetail.frameHeight - 30);
        this.mainPaneContainer.setPreferredSize(new Dimension(BookDetail.mainPaneContainerWidth, BookDetail.bookInfoPanelHeight + bookListPanelHeight + 50));

        bookInfoPanel.setBounds(0, 0, BookDetail.mainPaneContainerWidth, BookDetail.bookInfoPanelHeight);
        this.bookListPanel.setBounds(0, BookDetail.bookInfoPanelHeight + 20, BookDetail.mainPaneContainerWidth, bookListPanelHeight);

        int topOffset = 20;
        int leftOffset = 20;
        int itemHeight = 35;
        int labelWidth = 80;
        int inputLeftOffset = leftOffset + labelWidth;
        int inputHeight = 30;
        int inputWidth = 300;

        isbnLabel.setBounds(leftOffset, topOffset, labelWidth, inputHeight);
        nameLabel.setBounds(leftOffset, itemHeight + topOffset, labelWidth, inputHeight);
        authorLabel.setBounds(leftOffset, itemHeight * 2 + topOffset, labelWidth, inputHeight);
        priceLabel.setBounds(leftOffset, itemHeight * 3 + topOffset, labelWidth, inputHeight);
        categoryLabel.setBounds(leftOffset, itemHeight * 4 + topOffset, labelWidth, inputHeight);
        availableLabel.setBounds(leftOffset, itemHeight * 5 + topOffset, labelWidth, inputHeight);

        isbnValue.setBounds(inputLeftOffset, topOffset, inputWidth, inputHeight);
        this.nameInput.setBounds(inputLeftOffset, itemHeight + topOffset, inputWidth, inputHeight);
        this.authorInput.setBounds(inputLeftOffset, itemHeight * 2 + topOffset, inputWidth, inputHeight);
        this.priceInput.setBounds(inputLeftOffset, itemHeight * 3 + topOffset, inputWidth, inputHeight);
        this.categoryInput.setBounds(inputLeftOffset, itemHeight * 4 + topOffset, inputWidth, inputHeight);
        availableValue.setBounds(inputLeftOffset, itemHeight * 5 + topOffset, inputWidth, inputHeight);

        this.refreshBtn.setBounds(20, itemHeight * 6 + topOffset + 20, 96, 38);
        this.submitBtn.setBounds(115, itemHeight * 6 + topOffset + 20, 96, 38);
        this.addBookBtn.setBounds(210, itemHeight * 6 + topOffset + 20, 96, 38);
        this.deleteBookInfoBtn.setBounds(305, itemHeight * 6 + topOffset + 20, 96, 38);

        bookIdLabel.setBounds(-1, BookDetail.bookInfoPanelHeight + 20, 170, 38);
        holderLabel.setBounds(168, BookDetail.bookInfoPanelHeight + 20, 170, 38);
        deleteBookLabel.setBounds(337, BookDetail.bookInfoPanelHeight + 20, 86, 38);
        this.noBookLabel.setBounds(10, BookDetail.bookInfoPanelHeight + 59, 200, 30);


        // 设置字体 颜色 其他
        this.container.setBackground(Colors.DEFAULT_BG);
        mainPane.setBackground(Colors.DEFAULT_BG);
        this.mainPaneContainer.setBackground(Colors.DEFAULT_BG);
        this.bookListPanel.setBackground(Color.WHITE);
        bookInfoPanel.setBackground(Color.WHITE);
        mainPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        bookIdLabel.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
        holderLabel.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
        deleteBookLabel.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));

        Font font = new Font(Font.DIALOG, Font.PLAIN, 14);
        isbnLabel.setFont(font);
        nameLabel.setFont(font);
        authorLabel.setFont(font);
        priceLabel.setFont(font);
        categoryLabel.setFont(font);
        availableLabel.setFont(font);
        bookIdLabel.setFont(font);
        holderLabel.setFont(font);
        deleteBookLabel.setFont(font);
        this.noBookLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));


        // 绑定事件监听器
        ChangeInfoListener actionListener = new ChangeInfoListener(this);
        this.refreshBtn.addActionListener(actionListener);
        this.submitBtn.addActionListener(actionListener);
        this.addBookBtn.addActionListener(actionListener);
        this.deleteBookInfoBtn.addActionListener(actionListener);


        // 将元素加入页面
        bookInfoPanel.add(isbnLabel);
        bookInfoPanel.add(nameLabel);
        bookInfoPanel.add(authorLabel);
        bookInfoPanel.add(priceLabel);
        bookInfoPanel.add(categoryLabel);
        bookInfoPanel.add(availableLabel);
        bookInfoPanel.add(isbnValue);
        bookInfoPanel.add(this.nameInput);
        bookInfoPanel.add(this.authorInput);
        bookInfoPanel.add(this.priceInput);
        bookInfoPanel.add(this.categoryInput);
        bookInfoPanel.add(availableValue);
        bookInfoPanel.add(this.refreshBtn);
        bookInfoPanel.add(this.submitBtn);
        bookInfoPanel.add(this.addBookBtn);
        bookInfoPanel.add(this.deleteBookInfoBtn);


        this.mainPaneContainer.add(bookIdLabel);
        this.mainPaneContainer.add(holderLabel);
        this.mainPaneContainer.add(deleteBookLabel);
        this.mainPaneContainer.add(this.noBookLabel);

        this.mainPaneContainer.add(bookInfoPanel);
        this.mainPaneContainer.add(this.bookListPanel);



        mainPane.add(this.mainPaneContainer);
        mainPane.setViewportView(this.mainPaneContainer);

        this.container.add(mainPane);

        this.setVisible(true);

        this.initBookList();
    }
    private void initBookList() {
        // 获取书本列表
        this.bookListPanel.removeAll();

        Response res = dataAPI.getBooksByISBN(bookInfo.isbn);
        if (res.statusCode != 200) {
            System.out.println("BookDetail: 数据库返回非200状态");
            JOptionPane.showMessageDialog(null, "数据库拒绝了我，难以理解的错误！");
            throw new RuntimeException("数据库返回非200状态");
        }
        if (res.data.length == 0) {
            this.noBookLabel.setText("该书库存 0 本.");
        }
        else {
            int bookItemHeight = 30;
            for (int i = 0; i < res.data.length; i++) {
                JLabel bookId = new JLabel(((Book) res.data[i]).id, JLabel.CENTER);
                JButton holder = new DefaultBtn(((Book) res.data[i]).holder == null ? "" : ((Book) res.data[i]).holder.id);
                JButton delete = new DefaultBtn("下架");

                bookId.setBounds(-1, i * (bookItemHeight - 1) + 38, 170, bookItemHeight);
                holder.setBounds(168, i * (bookItemHeight - 1) + 38, 170, bookItemHeight);
                delete.setBounds(337, i * (bookItemHeight - 1) + 38, 86, bookItemHeight);

                bookId.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));

                BookListListener actionListener = new BookListListener(this, (Book) res.data[i]);
                holder.addActionListener(actionListener);
                delete.addActionListener(actionListener);

                this.bookListPanel.add(bookId);
                this.bookListPanel.add(holder);
                this.bookListPanel.add(delete);
            }
            int bookListPanelHeight = res.data.length * (bookItemHeight - 1) + 38;
            this.noBookLabel.setText("");
            this.bookListPanel.setBounds(0, BookDetail.bookInfoPanelHeight + 20, BookDetail.mainPaneContainerWidth, bookListPanelHeight);
            this.mainPaneContainer.setPreferredSize(new Dimension(BookDetail.mainPaneContainerWidth, BookDetail.bookInfoPanelHeight + bookListPanelHeight + 50));
            this.mainPaneContainer.updateUI();
            this.mainPaneContainer.repaint();
        }
    }


    // 定义窗口基本参数
    private static int frameWidth = 440;
    private static int frameHeight = 890;
    private static int bookInfoPanelHeight = 310;
    private static int mainPaneContainerWidth = frameWidth - 18;

    private static class ChangeInfoListener implements ActionListener {
        private BookDetail bookDetail;

        ChangeInfoListener(BookDetail bookDetail) {
            this.bookDetail = bookDetail;
        }

        private void reload(BookInfo bookInfo) {
            if (bookInfo == null) {
                Response res = this.bookDetail.dataAPI.getBookInfoByISBN(this.bookDetail.bookInfo.isbn);
                if (res.statusCode != 200) {
                    System.out.println("BookDetail: 数据库返回非200状态");
                    JOptionPane.showMessageDialog(null, "数据库拒绝了我，难以理解的错误！");
                    throw new RuntimeException("数据库返回非200状态");
                }
                new BookDetail(this.bookDetail.dataAPI, (BookInfo) res.data[0]);
            }
            else {
                new BookDetail(this.bookDetail.dataAPI, bookInfo);
            }
            this.bookDetail.dispose();
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            Object eventSource = event.getSource();
            if (eventSource == this.bookDetail.refreshBtn) {
                reload(null);
            }
            else if (eventSource == this.bookDetail.submitBtn) {
                // 获取书本信息
                String name = this.bookDetail.nameInput.getText();
                String author = this.bookDetail.authorInput.getText();
                String priceStr = this.bookDetail.priceInput.getText();
                String category = this.bookDetail.categoryInput.getText();

                BigDecimal price = null;
                try {
                    price = new BigDecimal(priceStr);
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "价格字段的值非法，请确认后重试！");
                    return;
                }
                price = price.setScale(2, RoundingMode.FLOOR);
                Response res = this.bookDetail.dataAPI.updateBookInfo(this.bookDetail.bookInfo.isbn, name, author, price, category);

                if (res.statusCode != 200) {
                    System.out.println("BookDetail: 数据库返回非200状态");
                    JOptionPane.showMessageDialog(null, "数据库拒绝了我，难以理解的错误！");
                    throw new RuntimeException("数据库返回非200状态");
                }
                reload((BookInfo) res.data[0]);
            }
            else if (eventSource == this.bookDetail.addBookBtn) {
                String booksNumStr = JOptionPane.showInputDialog("请输入需要添加的书本数量：");
                try {
                    int booksNum = Integer.parseInt(booksNumStr);
                    Response res = this.bookDetail.dataAPI.addBooks(this.bookDetail.bookInfo.isbn, booksNum);
                    if (res.statusCode != 200) {
                        JOptionPane.showMessageDialog(null, "未添加书本！");
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "添加书本成功！\n该窗口将自动刷新。");
                        reload(null);
                    }
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "未添加书本！");
                }
            }
            else if (eventSource == this.bookDetail.deleteBookInfoBtn) {
                if (this.bookDetail.bookInfo.total != 0) {
                    JOptionPane.showMessageDialog(null, "该书存书量不为 0 ，不能删除！");
                }
                else {
                    Response res = this.bookDetail.dataAPI.deleteBookInfo(this.bookDetail.bookInfo.isbn);
                    if (res.statusCode != 200) {
                        JOptionPane.showMessageDialog(null, "未能删除，请刷新该页面后重试！");
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "删除书本信息成功！");
                        this.bookDetail.dispose();
                    }
                }
            }
        }
    }
    private static class BookListListener implements ActionListener {
        private BookDetail bookDetail;
        private Book book;

        BookListListener(BookDetail bookDetail, Book book) {
            this.bookDetail = bookDetail;
            this.book = book;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            Object eventSource = event.getSource();
            if (((JButton) eventSource).getText().equals("下架")) {
                if (this.book.holder != null) {
                    JOptionPane.showMessageDialog(null, "该书已被借出，不能下架！");
                    return;
                }
                Response res = this.bookDetail.dataAPI.deleteBook(this.book.id);
                if (res.statusCode != 200) {
                    JOptionPane.showMessageDialog(null, "未能删除，请刷新该页面后重试！");
                }
                else {
                    JOptionPane.showMessageDialog(null, "该书已被删除！");
                    this.bookDetail.initBookList();
                }
            }
            else if (this.book.holder != null && ((JButton) eventSource).getText().equals(this.book.holder.id)) {
                JOptionPane.showMessageDialog(null,
                        "用户详细信息：\n" +
                                "借书号：" + this.book.holder.id + "\n" +
                                "姓名：" + this.book.holder.name + "\n" +
                                "类型：" + this.book.holder.type + "\n" +
                                "E-Mail：" + this.book.holder.email + "\n" +
                                "电话：" + this.book.holder.phoneNum + "\n" +
                                "地址：" + this.book.holder.address + "\n" +
                                "单位：" + this.book.holder.unit
                );
            }
            else if (this.book.holder == null && ((JButton) eventSource).getText().equals("")) {
                JOptionPane.showMessageDialog(null, "该书未被借出。");
            }
        }
    }
}

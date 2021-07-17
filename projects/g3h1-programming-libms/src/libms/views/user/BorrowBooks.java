package libms.views.user;

import libms.Iface;
import libms.model.DataAPI;
import libms.model.Response;
import libms.model.orm.Book;
import libms.views.Colors;
import libms.views.DefaultBtn;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * 借书面板
 *
 * @author virtuoso
 */
class BorrowBooks {
    JPanel borrowBooks;
    private JLabel title;

    private JLabel bookIdText;

    private JTextField bookIdInput;

    private DefaultBtn submitBtn;
    private int panelWidth;
    private int panelHeight;

    private JPanel borrowMessage;

    private Iface iface;
    private DataAPI dataAPI;

    BorrowBooks(int width, int height, Iface iface) {
        this.borrowBooks = new JPanel();
        this.title = new JLabel("书 籍 借 阅");
        this.bookIdText = new JLabel("输入想要借阅的图书 ID :");
        this.bookIdInput = new JTextField();
        this.submitBtn = new DefaultBtn("确认");
        this.iface = iface;
        this.dataAPI = iface.getDataAPI();
        this.panelWidth = width;
        this.panelHeight = height;
        this.PlaceComment();
        this.submitBtn.addActionListener(this::actionPerFormed);
        this.borrowMessage = null;
    }

    void PlaceComment() {
        this.borrowBooks.setLayout(null);
        this.borrowBooks.setBounds(0, 0, this.panelWidth, this.panelHeight);
        this.borrowBooks.setBorder(BorderFactory.createLineBorder(new Color(0xC8C8C8)));
        this.borrowBooks.setBackground(Colors.DEFAULT_BG);
        this.title.setBounds(this.panelWidth / 2 - 100, 20, 300, 50);
        this.title.setFont(new Font(Font.DIALOG, Font.PLAIN, 26));
        this.borrowBooks.add(this.title);

        this.bookIdText.setBounds(150, 100, 400, 50);
        this.bookIdText.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.bookIdInput.setBounds(150, 150, 700, 50);
        this.bookIdInput.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        this.borrowBooks.add(this.bookIdText);
        this.borrowBooks.add(this.bookIdInput);

        this.submitBtn.setBounds(this.panelWidth - 380, 150, 150, 50);
        this.submitBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.borrowBooks.add(this.submitBtn);
    }

    void actionPerFormed(ActionEvent e) {
        if (e.getSource() == submitBtn) {

            if (this.borrowMessage != null) {
                this.borrowBooks.remove(this.borrowMessage);
            }

            String BookID = this.bookIdInput.getText();
            Response res = this.dataAPI.getBookById(BookID);
            if (res == null || res.statusCode != 200) {
                new PopFrame("错误", "请输入正确的书本Id！", "返回");
            }
            else {
                Book[] book = (libms.model.orm.Book[]) res.data;
                this.borrowMessage = new JPanel();
                this.borrowMessage.setVisible(true);
                this.borrowMessage.setLayout(null);
                this.borrowMessage.setBounds(150, 250, 700, 500);
                this.borrowMessage.setBorder(null);
                this.borrowMessage.setBackground(Color.WHITE);

                DefaultBtn submitAgain = new DefaultBtn("确认借阅");
                submitAgain.setBounds(550, 420, 100, 50);
                submitAgain.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));

                JLabel bookIdText = new JLabel("书本 ID ：");
                JLabel bookNameText = new JLabel("书本名称：");
                JLabel bookISBNText = new JLabel("书本 ISBN：");
                JLabel bookAuthorText = new JLabel("作者：");
                JLabel bookTimeText = new JLabel("可借阅时长：");
                JLabel bookIdLabel = new JLabel(book[0].id);
                JLabel bookNameLabel = new JLabel(book[0].info.name);
                JLabel bookISBNLabel = new JLabel(book[0].info.isbn);
                JLabel bookAuthorLabel = new JLabel(book[0].info.author);
                JLabel bookTimeLabel = new JLabel("60 天");

                bookIdText.setBounds(30, 30, 300, 40);
                bookIdLabel.setBounds(260, 30, 300, 40);
                bookNameText.setBounds(30, 110, 300, 40);
                bookNameLabel.setBounds(260, 110, 300, 40);
                bookISBNText.setBounds(30, 190, 300, 40);
                bookISBNLabel.setBounds(260, 190, 300, 40);
                bookAuthorText.setBounds(30, 270, 300, 40);
                bookAuthorLabel.setBounds(260, 270, 300, 40);
                bookTimeText.setBounds(30, 350, 300, 40);
                bookTimeLabel.setBounds(260, 350, 300, 40);
                Font font = new Font(Font.DIALOG, Font.PLAIN, 18);
                Font font1 = new Font(Font.DIALOG, Font.PLAIN, 18);
                bookIdText.setFont(font);
                bookNameText.setFont(font);
                bookISBNText.setFont(font);
                bookAuthorText.setFont(font);
                bookTimeText.setFont(font);
                bookIdLabel.setFont(font1);
                bookNameLabel.setFont(font1);
                bookISBNLabel.setFont(font1);
                bookAuthorLabel.setFont(font1);
                bookTimeLabel.setFont(font1);

                this.borrowMessage.add(bookIdText);
                this.borrowMessage.add(bookIdLabel);
                this.borrowMessage.add(bookNameText);
                this.borrowMessage.add(bookNameLabel);
                this.borrowMessage.add(bookISBNText);
                this.borrowMessage.add(bookISBNLabel);
                this.borrowMessage.add(bookAuthorText);
                this.borrowMessage.add(bookAuthorLabel);
                this.borrowMessage.add(bookTimeText);
                this.borrowMessage.add(bookTimeLabel);

                this.borrowMessage.add(submitAgain);
                submitAgain.addActionListener(
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Response response = dataAPI.borrowBook(iface.getUser().id, BookID);
                                if (response.statusCode == 200) {
                                    new PopFrame("成功", "借书成功请前往个人信息查看！", "确认");

                                    bookIdInput.setText("");
                                    borrowBooks.remove(borrowMessage);
                                    borrowBooks.repaint();

                                }
                                else if (response.statusCode == 400) {
                                    new PopFrame("错误", "请输入正确的书本Id！", "确认");
                                }
                                else if (response.statusCode == 403) {
                                    if (response.statusMessage.equals("loaned out")) {
                                        new PopFrame("错误", "该书本已被借出！", "确认");
                                    }
                                    else if (response.statusMessage.equals("borrowed too much")) {
                                        new PopFrame("错误", "所借的书本已经超过可借书的总量！", "确认");
                                    }
                                }
                                else if (response.statusCode == 404) {
                                    new PopFrame("错误", "您所搜索的书本不存在！", "确认");
                                }
                            }
                        }
                );
                borrowBooks.add(this.borrowMessage);
            }
        }
        borrowBooks.repaint();
    }
}

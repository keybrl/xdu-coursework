package libms.views.user;

import libms.Iface;
import libms.model.DataAPI;
import libms.model.Response;
import libms.model.orm.Book;
import libms.model.orm.User;
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
import java.math.BigDecimal;


/**
 * 还书面板
 *
 * @author virtuoso
 */
class ReturnBooks {
    JPanel returnBooks;
    private JLabel title;
    private JLabel BookIdText;
    private JTextField BookIdInput;
    private JPanel returnMessage;
    private DefaultBtn Submit;
    private int PanelWidth;
    private int PanelHeight;
    private DataAPI dataAPI;
    private BigDecimal price;

    ReturnBooks(int width, int height, Iface iface){
        returnBooks = new JPanel();
        title = new JLabel("书 籍 归 还");
        BookIdText = new JLabel("输入需要归还的图书 ID :");
        BookIdInput = new JTextField();
        Submit = new DefaultBtn("确认");
        dataAPI = iface.getDataAPI();
        PanelWidth = width;
        PanelHeight = height;
        price = new BigDecimal("0.1");
        PlaceComment();
        Submit.addActionListener(this::actionPerFormed);
    }

    private void PlaceComment(){
        returnBooks.setLayout(null);
        returnBooks.setBounds(0 , 0, PanelWidth, PanelHeight);
        returnBooks.setBorder(BorderFactory.createLineBorder(new Color(0xC8C8C8)));
        returnBooks.setBackground(Colors.DEFAULT_BG);
        title.setBounds(PanelWidth / 2 - 100, 20, 400, 50);
        title.setFont(new Font(Font.DIALOG, Font.PLAIN, 26));
        returnBooks.add(title);

        BookIdText.setBounds(150, 100, 400, 50);
        BookIdText.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        BookIdInput.setBounds(150, 150, 700, 50);
        BookIdInput.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        returnBooks.add(BookIdText);
        returnBooks.add(BookIdInput);

        Submit.setBounds(PanelWidth - 380, 150, 150, 50);
        Submit.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        returnBooks.add(Submit);
    }

    private void actionPerFormed(ActionEvent e){
        if (e.getSource() == Submit) {
            if (this.returnMessage != null) {
                this.returnMessage.remove(this.returnMessage);
            }
            String BookID = BookIdInput.getText();
            Response response = dataAPI.borrowHowLong(BookID);
            Response response1 = dataAPI.getBookById(BookID);
            if (response == null || response.statusCode != 200) {
                new PopFrame("错误", "请输入正确的书本Id！", "确认");
                throw new RuntimeException();
            }
            if (response1 == null || response1.statusCode != 200) {
                throw new RuntimeException();
            }

            User[] user = (libms.model.orm.User[])response.data;
            Book[] book = (libms.model.orm.Book[])response1.data;
            int[] borrowTimeLong = new int[response.data.length];
            borrowTimeLong[0] = Integer.parseInt(response.statusMessage);
            this.returnMessage = new JPanel();
            this.returnMessage.setVisible(true);
            this.returnMessage.setLayout(null);
            this.returnMessage.setBounds(150, 250, 700, 500);
            this.returnMessage.setBorder(null);
            this.returnMessage.setBackground(Color.WHITE);

            DefaultBtn submitAgain = new DefaultBtn("确认归还");
            submitAgain.setBounds(550, 420, 100, 50);
            submitAgain.setFont(new Font(Font.DIALOG, Font.PLAIN, 15));

            JLabel userIdText = new JLabel("借书号：");
            JLabel userNameText = new JLabel("借阅人名称：");
            JLabel bookNameText = new JLabel("书籍名称：");
            JLabel bookIdText = new JLabel("书籍 ID ：");
            JLabel borrowTimeText = new JLabel("借阅时长：");
            JLabel userIdLabel = new JLabel(user[0].id);
            JLabel userNameLabel = new JLabel(user[0].name);
            JLabel bookNameLabel = new JLabel(book[0].info.name);
            JLabel bookIdLabel = new JLabel(book[0].id);
            JLabel borrowTimeLabel = new JLabel("您已经借阅 " + String.format("%02d", borrowTimeLong[0] / 86400) + "天" + String.format("%02d", (borrowTimeLong[0] % 86400) / 3600) + "小时");
            JLabel overdueReminders = new JLabel();

            if (borrowTimeLong[0] > 5184000) {
                overdueReminders.setText("您所借阅的书本已逾期，逾期时长 " + String.format("%02d", (borrowTimeLong[0] - 5184000) / 86400) + "天"
                        + String.format("%02d", ((borrowTimeLong[0] - 5184000) % 86400) / 3600) + "小时， 产生费用" + price.multiply(new BigDecimal((borrowTimeLong[0] - 5184000) / 86400)) + "CNY.");
            }

            userIdText.setBounds(30, 30, 300, 32);
            userIdLabel.setBounds(260, 30, 300, 32);
            userNameText.setBounds(30, 100, 300, 32);
            userNameLabel.setBounds(260, 100, 300, 32);
            bookNameText.setBounds(30, 170, 300, 32);
            bookNameLabel.setBounds(260, 170, 300, 32);
            bookIdText.setBounds(30, 240, 300, 32);
            bookIdLabel.setBounds(260, 240, 300, 32);
            borrowTimeText.setBounds(30, 310, 300, 32);
            borrowTimeLabel.setBounds(260, 310, 300, 32);
            overdueReminders.setBounds(30, 350, 600, 32);
            Font font = new Font(Font.DIALOG, Font.PLAIN, 18);
            Font font1 = new Font(Font.DIALOG, Font.PLAIN, 18);
            bookIdText.setFont(font);
            bookNameText.setFont(font);
            userIdText.setFont(font);
            userNameText.setFont(font);
            borrowTimeText.setFont(font);
            bookIdLabel.setFont(font1);
            bookNameLabel.setFont(font1);
            userIdLabel.setFont(font1);
            userNameLabel.setFont(font1);
            borrowTimeLabel.setFont(font1);
            overdueReminders.setFont(font);
            overdueReminders.setForeground(Color.RED);

            this.returnMessage.add(userIdText);
            this.returnMessage.add(userIdLabel);
            this.returnMessage.add(userNameText);
            this.returnMessage.add(userNameLabel);
            this.returnMessage.add(bookNameText);
            this.returnMessage.add(bookNameLabel);
            this.returnMessage.add(bookIdText);
            this.returnMessage.add(bookIdLabel);
            this.returnMessage.add(borrowTimeText);
            this.returnMessage.add(borrowTimeLabel);
            this.returnMessage.add(overdueReminders);
            this.returnMessage.add(submitAgain);

            submitAgain.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Response res = dataAPI.returnBook(BookID);
                            if(res.statusCode == 200){
                                new PopFrame("成功", "还书成功请前往个人信息查看！", "确认");

                                BookIdInput.setText("");
                                returnBooks.remove(returnMessage);
                                returnBooks.repaint();
                            }
                            else if (res.statusCode == 400) {
                                new PopFrame("错误", "请输入正确的书本Id！", "确认");
                            }
                            else if (res.statusCode == 403) {
                                new PopFrame("拒绝操作", "书本尚未借出无法归还！", "确认");
                            }
                            else if (res.statusCode == 404) {
                                new PopFrame("错误", "该书本Id不存在！", "确认");
                            }
                        }
                    }
            );
            returnBooks.add(this.returnMessage);
        }
        returnBooks.repaint();
    }
}

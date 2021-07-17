package libms.views.user;

import libms.Iface;
import libms.model.DataAPI;
import libms.model.Response;
import libms.model.orm.Book;
import libms.views.Colors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;


/**
 * 用户信息面板
 *
 * @author virtuoso
 */
class UserInfo {
    private DataAPI dataAPI;
    private Iface iface;

    JPanel userInfo;
    private JLabel title;
    private JLabel userId;
    private JLabel userName;
    private JLabel userUnit;
    private JLabel userEmail;
    private JLabel userPhone;
    private JLabel userAddress;
    private JLabel userId1;
    private JLabel userName1;
    private JLabel userUnit1;
    private JLabel userEmail1;
    private JLabel userPhone1;
    private JLabel userAddress1;
    private JLabel borrowList;
    private JLabel leftTime;
    private JLabel leftTime1;

    private int panelWidth;
    private int panelHeight;

    UserInfo(int width, int height, Iface iface){
        this.dataAPI = iface.getDataAPI();
        this.iface = iface;

        userInfo = new JPanel();

        title = new JLabel("个 人 信 息");
        userId = new JLabel("借书号:");
        userName = new JLabel("用户姓名:");
        userUnit = new JLabel("单位:");
        userEmail = new JLabel("邮箱地址:");
        userPhone = new JLabel("联系电话:");
        userAddress = new JLabel("用户住址:");

        userId1 = new JLabel(iface.getUser().id);
        userName1 = new JLabel(iface.getUser().name);
        userUnit1 = new JLabel(iface.getUser().unit);
        userEmail1 = new JLabel(iface.getUser().email);
        userPhone1 = new JLabel(iface.getUser().phoneNum);
        userAddress1 = new JLabel(iface.getUser().address);

        borrowList = new JLabel("已借阅书籍信息:");
        leftTime = new JLabel("剩余借阅数量:");
        int leftBorrowTime = 5;
        leftTime1 = new JLabel(String.valueOf(leftBorrowTime));

        Font font = new Font(Font.DIALOG, Font.PLAIN, 16);
        Font font1 = new Font(Font.DIALOG, Font.PLAIN, 16);
        this.userId.setFont(font);
        this.userName.setFont(font);
        this.userUnit.setFont(font);
        this.userEmail.setFont(font);
        this.userPhone.setFont(font);
        this.userAddress.setFont(font);
        this.userId1.setFont(font1);
        this.userName1.setFont(font1);
        this.userUnit1.setFont(font1);
        this.userEmail1.setFont(font1);
        this.userPhone1.setFont(font1);
        this.userAddress1.setFont(font1);
        this.title.setFont(new Font(Font.DIALOG, Font.PLAIN, 26));
        this.borrowList.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.leftTime.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.leftTime1.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));

        panelWidth = width;
        panelHeight = height;
        PlaceComment();
        addBookList();


    }

    private void PlaceComment(){
        userInfo.setLayout(null);
        userInfo.setBounds(0, 0, panelWidth, panelHeight);
        userInfo.setBorder(BorderFactory.createLineBorder(new Color(0xC8C8C8)));
        userInfo.setBackground(Colors.DEFAULT_BG);
        title.setBounds(panelWidth / 2 - 100, 10, 400, 50);
        userInfo.add(title);

        userId.setBounds(200, 50, 300, 50);
        userName.setBounds(200, 85, 300, 50);
        userUnit.setBounds(200, 120, 300, 50);
        userEmail.setBounds(200, 155, 300, 50);
        userPhone.setBounds(200, 190, 300, 50);
        userAddress.setBounds(200, 225, 300, 50);
        userId1.setBounds(400, 50, 300, 50);
        userName1.setBounds(400, 85, 300, 50);
        userUnit1.setBounds(400, 120, 300, 50);
        userEmail1.setBounds(400, 155, 300, 50);
        userPhone1.setBounds(400, 190, 300, 50);
        userAddress1.setBounds(400, 225, 300, 50);
        borrowList.setBounds(200, 260, 300, 50);
        leftTime.setBounds(200, 650, 300, 50);
        leftTime1.setBounds(400, 650, 300, 50);
        userInfo.add(userId);
        userInfo.add(userName);
        userInfo.add(userUnit);
        userInfo.add(userEmail);
        userInfo.add(userPhone);
        userInfo.add(userAddress);
        userInfo.add(userId1);
        userInfo.add(userName1);
        userInfo.add(userUnit1);
        userInfo.add(userEmail1);
        userInfo.add(userPhone1);
        userInfo.add(userAddress1);
        userInfo.add(borrowList);
        userInfo.add(leftTime);
        userInfo.add(leftTime1);
    }

    private void addBookList(){
        JPanel bookList = new JPanel();
        bookList.setVisible(true);
        bookList.setLayout(null);
        bookList.setBounds(200, 305, 900, 330);
        bookList.setBackground(Color.WHITE);

        System.out.println(dataAPI);
        Response res = dataAPI.getBooksByHolder(iface.getUser().id);

        if (res == null || res.statusCode != 200) {
            throw new RuntimeException();
        }
        this.leftTime1.setText(5 - res.data.length + "");
        String[] borrowListBookId = new String[res.data.length];
        String[] borrowListBookName = new String[res.data.length];
        String[] borrowListBookCategory = new String[res.data.length];
        String[] borrowListBookISBN = new String[res.data.length];
        int[] borrowTimeLong = new int[res.data.length];
        Book[] books = (libms.model.orm.Book[])res.data;
        for(int i = 0; i < res.data.length; i++){
            borrowListBookId[i] = books[i].id;
            borrowListBookName[i] = books[i].info.name;
            borrowListBookCategory[i] = books[i].info.category;
            borrowListBookISBN[i] = books[i].info.isbn;
            Response response = dataAPI.borrowHowLong(borrowListBookId[i]);
            if (response == null || response.statusCode != 200) {
                throw new RuntimeException();
            }
            borrowTimeLong[i] = Integer.parseInt(response.statusMessage);

            System.out.println(borrowListBookCategory[i]);

            JLabel listId = new JLabel("图书 ID ");
            JLabel listName = new JLabel("图书名称");
            JLabel listCat = new JLabel("图书分类");
            JLabel listISBN = new JLabel("图书 ISBN ");
            JLabel listTime = new JLabel("已借阅时长");
            JLabel borrowInfoBookId = new JLabel(borrowListBookId[i]);
            JLabel borrowInfoBookName = new JLabel(borrowListBookName[i]);
            JLabel borrowInfoBookCategory = new JLabel(borrowListBookCategory[i]);
            JLabel borrowInfoBookISBN = new JLabel(borrowListBookISBN[i]);
            JLabel borrowInfoTimeLong = new JLabel(String.format("%02d", borrowTimeLong[i] / 86400) + "天" + String.format("%02d", (borrowTimeLong[i] % 86400) / 3600) + "小时");

            listId.setBounds(20, 20, 150, 40);
            listName.setBounds(190, 20, 230, 40);
            listCat.setBounds(440, 20, 130, 40);
            listISBN.setBounds(590, 20, 150, 40);
            listTime.setBounds(770, 20, 150, 40);
            borrowInfoBookId.setBounds(20, i * 50 + 60, 150, 40);
            borrowInfoBookName.setBounds(190, i * 50 + 60, 230, 40);
            borrowInfoBookCategory.setBounds(440, i * 50 + 60, 130, 40);
            borrowInfoBookISBN.setBounds(590, i * 50 + 60, 150, 40);
            borrowInfoTimeLong.setBounds(770, i * 50 + 60, 150, 40);


            Font font =  new Font(Font.DIALOG, Font.PLAIN, 16);
            Font font1 = new Font(Font.DIALOG, Font.PLAIN, 17);
            listId.setFont(font1);
            listName.setFont(font1);
            listCat.setFont(font1);
            listISBN.setFont(font1);
            listTime.setFont(font1);
            borrowInfoBookId.setFont(font);
            borrowInfoBookName.setFont(font);
            borrowInfoBookCategory.setFont(font);
            borrowInfoBookISBN.setFont(font);
            borrowInfoTimeLong.setFont(font);

            bookList.add(listId);
            bookList.add(listName);
            bookList.add(listCat);
            bookList.add(listISBN);
            bookList.add(listTime);
            bookList.add(borrowInfoBookId);
            bookList.add(borrowInfoBookName);
            bookList.add(borrowInfoBookCategory);
            bookList.add(borrowInfoBookISBN);
            bookList.add(borrowInfoTimeLong);
        }
        this.userInfo.add(bookList);
    }
}

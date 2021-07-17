package libms.views.user;

import libms.Iface;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;


/**
 * 用户页面
 *
 * @author virtuoso
 */
class UserPanel {
    private Iface iface;
    JPanel userPanel;
    private JPanel contentPanel;
    private JButton userInfoBtn;
    private JButton searchBtn;
    private JButton borrowBooksBtn;
    private JButton returnBooksBtn;

    private int width;
    private int height;

    private int leftWidth;
    private int rightWidth;

    private Search search;
    private BorrowBooks borrowBooks;
    private ReturnBooks returnBooks;

    private JPanel nowPanel;

    UserPanel(int WindowsWidth, int WindowsHeight, Iface iface){
        userPanel = new JPanel();
        contentPanel = new JPanel();
        userInfoBtn = new JButton("个人信息");
        searchBtn = new JButton("图书搜索");
        borrowBooksBtn = new JButton("图书借阅");
        returnBooksBtn = new JButton("图书归还");

        this.iface = iface;

        width = WindowsWidth;
        height = WindowsHeight;

        leftWidth = width / 8;
        rightWidth = width - leftWidth;

        search = new Search(rightWidth, height, iface);
        borrowBooks = new BorrowBooks(rightWidth, height, iface);
        returnBooks = new ReturnBooks(rightWidth, height, iface);
        //bookInfo = new BookInfo(rightWidth, height);

        PlacePanel();

        userInfoBtn.addActionListener(this::actionPerFormed);
        searchBtn.addActionListener(this::actionPerFormed);
        borrowBooksBtn.addActionListener(this::actionPerFormed);
        returnBooksBtn.addActionListener(this::actionPerFormed);

    }

    private void PlacePanel(){
        contentPanel.setBounds(leftWidth, 0, rightWidth, height);
        contentPanel.setLayout(null);

        nowPanel = search.search;
        contentPanel.add(search.search);

        MenuPanel menu = new MenuPanel(userInfoBtn, searchBtn, borrowBooksBtn, returnBooksBtn, leftWidth, height / 6);
        userPanel.setLayout(null);
        userPanel.setBounds(0, 0, width, height);

        userPanel.add(contentPanel);
        userPanel.add(menu.panel);
    }

    private void ChangePanel(JPanel panel){
        contentPanel.remove(nowPanel);
        contentPanel.add(panel);
        nowPanel = panel;
    }

    private void actionPerFormed(ActionEvent e){
        if(e.getSource() == userInfoBtn) {
            UserInfo userInfo = new UserInfo(this.rightWidth, this.height, this.iface);
            ChangePanel(userInfo.userInfo);
        }

        else if(e.getSource() == searchBtn)
            ChangePanel(search.search);
        else if(e.getSource() == borrowBooksBtn) {
            this.borrowBooks = new BorrowBooks(this.rightWidth, this.height, this.iface);
            ChangePanel(borrowBooks.borrowBooks);
        }
        else {
            this.returnBooks = new ReturnBooks(this.rightWidth, this.height, this.iface);
            ChangePanel(returnBooks.returnBooks);
        }
        userPanel.repaint();
    }
}

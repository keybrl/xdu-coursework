package libms.views.user;

import libms.Iface;
import libms.model.DataAPI;
import libms.model.Response;
import libms.views.Colors;
import libms.views.DefaultBtn;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;


/**
 * 搜索面板
 *
 * @author virtuoso
 */
class Search {
    JPanel search;
    private DefaultBtn searchBooks;
    private JTextField searchInput;

    private JRadioButton fuzzySearch;
    private JRadioButton searchAsISBN;
    private JRadioButton searchAsAuthor;
    private JRadioButton searchAsBookName;

    private int PanelWidth;
    private int PanelHeight;

    private DataAPI dataAPI;


    Search(int width, int height, Iface iface) {
        search = new JPanel();
        searchBooks = new DefaultBtn("Search");
        searchInput = new JTextField();

        fuzzySearch = new JRadioButton("模糊匹配");
        searchAsISBN = new JRadioButton("按 ISBN 搜索");
        searchAsAuthor = new JRadioButton("按作者搜索");
        searchAsBookName = new JRadioButton("按书名搜索");

        dataAPI = iface.getDataAPI();

        Font font = new Font(Font.DIALOG, Font.PLAIN, 20);
        Font font1 = new Font(Font.DIALOG, Font.PLAIN, 15);
        this.searchBooks.setFont(font);
        this.fuzzySearch.setFont(font1);
        this.searchAsISBN.setFont(font1);
        this.searchAsAuthor.setFont(font1);
        this.searchAsBookName.setFont(font1);
        PanelWidth = width;
        PanelHeight = height;
        PanelComment();

        searchBooks.addActionListener(this::actionPerFormed);
    }

    void PanelComment() {
        search.setLayout(null);
        search.setBounds(0, 0, PanelWidth, PanelHeight);
        search.setBorder(BorderFactory.createLineBorder(new Color(0xC8C8C8)));
        search.setBackground(Colors.DEFAULT_BG);
        searchInput.setBounds(PanelHeight / 4, 125, 700, 50);
        searchInput.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        search.add(searchInput);

        searchBooks.setBounds(PanelWidth - 310, 125, 100, 50);
        search.add(searchBooks);

        fuzzySearch.setBounds(PanelHeight / 4, PanelHeight - 710, 150, 35);
        search.add(fuzzySearch);

        searchAsISBN.setBounds(PanelHeight / 4 + 160, PanelHeight - 710, 150, 35);
        search.add(searchAsISBN);

        searchAsAuthor.setBounds(PanelHeight / 4 + 340, PanelHeight - 710, 150, 35);
        search.add(searchAsAuthor);

        searchAsBookName.setBounds(PanelHeight / 4 + 510, PanelHeight - 710, 170, 35);
        search.add(searchAsBookName);

        ButtonGroup group = new ButtonGroup();
        group.add(fuzzySearch);
        group.add(searchAsISBN);
        group.add(searchAsAuthor);
        group.add(searchAsBookName);
    }


    private void actionPerFormed(ActionEvent e) {
        if (e.getSource() == searchBooks) {
            //BookInfo bookInfo = new BookInfo();
            String key;
            String author;
            String ISBN;
            String bookName;
            if (searchInput.getText().length() == 0) {
                Response res = dataAPI.getBooksInfo();
                for (libms.model.orm.BookInfo bookInfo : (libms.model.orm.BookInfo[]) res.data) {
                    System.out.println(bookInfo.name);
                }
                new BookInfo((libms.model.orm.BookInfo[]) res.data);
            }
            else if (fuzzySearch.isSelected()) {
                key = searchInput.getText();
                Response res = dataAPI.getBooksInfoByKey(key);
                System.out.println(res.statusCode);
                if (res.statusCode == 200) {
                    //wait to write!
                    for (libms.model.orm.BookInfo bookInfo : (libms.model.orm.BookInfo[]) res.data) {
                        System.out.println(bookInfo.name);
                    }
                    new BookInfo((libms.model.orm.BookInfo[]) res.data);
                }
                else if (res.statusCode == 400) {
                    new PopFrame("错误", "请输入正确的关键字！", "返回");
                }
                else if (res.statusCode == 404) {
                    new PopFrame("错误", "找不到您所搜索的书籍！", "返回");
                }
            }
            else if (searchAsISBN.isSelected()) {
                ISBN = searchInput.getText();
                Response res = dataAPI.getBookInfoByISBN(ISBN);
                System.out.println(res.statusCode);
                if (res.statusCode == 200) {
                    //wait to write!
                    for (libms.model.orm.BookInfo bookInfo : (libms.model.orm.BookInfo[]) res.data) {
                        System.out.println(bookInfo.name);
                    }
                    new BookInfo((libms.model.orm.BookInfo[]) res.data);
                }
                else if (res.statusCode == 400) {
                    new PopFrame("错误", "请输入正确的 ISBN！", "返回");
                }
                else if (res.statusCode == 404) {
                    new PopFrame("错误", "找不到您所搜索的书籍！", "返回");
                }
            }
            else if (searchAsAuthor.isSelected()) {
                author = searchInput.getText();
                Response res = dataAPI.getBooksInfoByAuthor(author, false);
                System.out.println(res.statusCode);
                if (res.statusCode == 200) {
                    //wait to write!
                    for (libms.model.orm.BookInfo bookInfo : (libms.model.orm.BookInfo[]) res.data) {
                        System.out.println(bookInfo.name);
                    }
                    new BookInfo((libms.model.orm.BookInfo[]) res.data);
                }
                else if (res.statusCode == 400) {
                    new PopFrame("错误", "请输入正确的作者名！", "返回");
                }
                else if (res.statusCode == 404) {
                    new PopFrame("错误", "找不到您所搜索的书籍！", "返回");
                }
            }
            else if (searchAsBookName.isSelected()) {
                bookName = searchInput.getText();
                Response res = dataAPI.getBooksInfoByName(bookName, false);
                System.out.println(res.statusCode);
                if (res.statusCode == 200) {
                    //wait to write!
                    for (libms.model.orm.BookInfo bookInfo : (libms.model.orm.BookInfo[]) res.data) {
                        System.out.println(bookInfo.name);
                    }
                    new BookInfo((libms.model.orm.BookInfo[]) res.data);
                }
                else if (res.statusCode == 400) {
                    new PopFrame("错误", "请输入正确的图书名！", "返回");
                }
                else if (res.statusCode == 404) {
                    new PopFrame("错误", "找不到您所搜索的书籍！", "返回");
                }
            }
            else {
                new PopFrame("提示", "请选择搜索类型！", "返回");
            }
        }
        search.repaint();
    }
}
package libms.views.user;

import libms.views.DefaultBtn;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;


/**
 * 书本信息面板
 *
 * @author virtuoso
 */
public class BookInfo {
    private JFrame jFrame;
    private DefaultBtn backBtn;


    BookInfo(libms.model.orm.BookInfo[] booksInfo){

        this.jFrame = new JFrame("书籍信息");
        this.backBtn = new DefaultBtn("返回菜单");
        JLabel infoLabel = new JLabel("搜索结果均显示在上面表格中，您可以查看可借数量以确认能否借阅图书。");

        this.jFrame.setSize(1440, 890);
        this.jFrame.setLocationRelativeTo(null);
        this.jFrame.setResizable(false);

        infoLabel.setBounds(100, 690, 1000, 80);
        infoLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        this.jFrame.add(infoLabel);

        this.backBtn.setBounds(1200, 750, 150, 50);
        this.backBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));
        this.jFrame.add(this.backBtn);

        BookTable book = new BookTable(booksInfo);
        book.bookPanel.setBounds(100, 100, 1220, 610);
        this.jFrame.add(book.bookPanel);
        this.jFrame.setVisible(true);

        this.backBtn.addActionListener(this::actionPerFormed);
    }


    private void actionPerFormed(ActionEvent e){
        if(e.getSource() == this.backBtn){
            this.jFrame.dispose();
        }
    }
}

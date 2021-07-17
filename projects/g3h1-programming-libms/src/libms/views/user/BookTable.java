package libms.views.user;

import libms.model.orm.BookInfo;
import libms.views.Colors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * 书本表
 *
 * @author virtuoso
 */
class BookTable{
    JPanel bookPanel;

    BookTable(BookInfo[] booksInfo) {
        bookPanel = new JPanel();

        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);

        //创建一个表格
        Object[][] playerInfo = new String[booksInfo.length][];
        for (int i = 0; i < playerInfo.length; i++) {
            playerInfo[i] = new String[]{booksInfo[i].isbn, booksInfo[i].name, booksInfo[i].author, booksInfo[i].category, booksInfo[i].available + "/" + booksInfo[i].total};
        }


        String[] Names = {"ISBN", "书名", "作者", "分类", "可借数量 / 馆藏数量"};

        JTable table = new JTable(playerInfo, Names);


        table.setForeground(Color.BLACK);
        table.setFont(new Font(null, Font.PLAIN, 14));
        table.setSelectionForeground(Color.DARK_GRAY);
        table.setSelectionBackground(Color.LIGHT_GRAY);
        table.setGridColor(Color.GRAY);

        table.setRowHeight(50);

        table.getColumnModel().getColumn(4).setPreferredWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(0);
        table.getColumn("ISBN").setCellRenderer(tcr);
        table.getColumn("书名").setCellRenderer(tcr);
        table.getColumn("作者").setCellRenderer(tcr);
        table.getColumn("分类").setCellRenderer(tcr);
        table.getColumn("可借数量 / 馆藏数量").setCellRenderer(tcr);

        table.setPreferredScrollableViewportSize(new Dimension(1220, 680));

        JScrollPane scrollPane = new JScrollPane(table);

        bookPanel.setBackground(Colors.DEFAULT_BG);
        bookPanel.add(scrollPane);
    }
}

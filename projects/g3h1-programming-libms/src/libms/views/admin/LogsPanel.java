package libms.views.admin;

import libms.model.DataAPI;
import libms.model.Response;
import libms.model.orm.BorrowLog;
import libms.views.Colors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.SimpleDateFormat;


/**
 * 借还日志面板
 *
 * @author keybrl
 */
class LogsPanel extends RightPanel {
    private DataAPI dataAPI;

    private JScrollPane logsListPanel;


    LogsPanel(DataAPI dataAPI) {
        super();
        this.dataAPI = dataAPI;

        this.initGUI();
    }

    private void initGUI() {
        this.logsListPanel = new LogsListPanel(this.dataAPI);
        this.logsListPanel.setBackground(Colors.DEFAULT_BG);
        this.add(this.logsListPanel);
    }

    void refresh() {
        ((LogsListPanel) this.logsListPanel).reloadList();
        this.updateUI();
        this.repaint();
    }
}


class LogsListPanel extends JScrollPane {
    private DataAPI dataAPI;
    private JPanel panel;

    LogsListPanel(DataAPI dataAPI) {
        this.dataAPI = dataAPI;

        this.setBounds(0, 0, RightPanel.panelWidth, RightPanel.panelHeight - 30);
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.setBorder(null);

        this.panel = new JPanel();
        this.panel.setLayout(null);
        this.panel.setBackground(Colors.DEFAULT_BG);

        this.add(this.panel);
        this.setViewportView(panel);
    }

    void reloadList() {
        this.panel.removeAll();

        Response res = this.dataAPI.getBorrowLogs();
        if (res.statusCode != 200) {
            JOptionPane.showMessageDialog(null, "数据库失去联系，难以理解的错误！");
            System.out.println("LogsPanel: 数据接口返回非200状态");
            throw new RuntimeException("数据接口返回非200状态");
        }
        BorrowLog[] logs = (BorrowLog[]) res.data;

        this.panel.setPreferredSize(new Dimension(RightPanel.panelWidth, (itemPanelHeight - 1) * (logs.length + 1) + 100));

        SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (int i = -1; i < logs.length; i++) {

            JPanel item = new JPanel();
            item.setLayout(null);

            JLabel seqLabel = new JLabel(i == -1 ? "序号" : logs[i].seq);
            JLabel dataTimeLabel = new JLabel(i == -1 ? "时间" : formater.format(logs[i].dateTime));
            JLabel typeLabel = new JLabel(i == -1 ? "类型" : logs[i].type);
            JLabel bookIdLabel = new JLabel(i == -1 ? "书本Id" : logs[i].book.id);
            JLabel userIdLabel = new JLabel(i == -1 ? "用户Id" : logs[i].user.id);


            // 设置元素摆放位置
            int top = (i + 1) * (itemPanelHeight - 1) + 30;
            int left = 10;
            item.setBounds(20, top, RightPanel.panelWidth - 56, itemPanelHeight);
            seqLabel.setBounds(left, 0, seqLabelWidth, itemPanelHeight);
            left += seqLabelWidth + 10;
            dataTimeLabel.setBounds(left, 0, dateTimeLabelWidth, itemPanelHeight);
            left += dateTimeLabelWidth + 10;
            typeLabel.setBounds(left, 0, typeLabelWidth, itemPanelHeight);
            left += typeLabelWidth + 10;
            userIdLabel.setBounds(left, 0, userIdLabelWidth, itemPanelHeight);
            left += userIdLabelWidth + 10;
            bookIdLabel.setBounds(left, 0, emailLabelWidth, itemPanelHeight);


            // 设置边框
            item.setBorder(BorderFactory.createLineBorder(Colors.DEFAULT_BORDER, 1));
            item.setBackground(Color.WHITE);


            // 设置字体
            Font font = new Font(Font.DIALOG, Font.PLAIN, 14);
            seqLabel.setFont(font);
            dataTimeLabel.setFont(font);
            typeLabel.setFont(font);
            bookIdLabel.setFont(font);
            userIdLabel.setFont(font);


            // 将元素插入列表
            item.add(seqLabel);
            item.add(dataTimeLabel);
            item.add(typeLabel);
            item.add(userIdLabel);
            item.add(bookIdLabel);

            this.panel.add(item);
        }

        // 重刷页面
        this.panel.updateUI();
        this.panel.repaint();
    }

    private static final int itemPanelHeight = 48;
    private static final int seqLabelWidth = 60;
    private static final int dateTimeLabelWidth = 200;
    private static final int typeLabelWidth = 100;
    private static final int userIdLabelWidth = 100;
    private static final int emailLabelWidth = 200;
}

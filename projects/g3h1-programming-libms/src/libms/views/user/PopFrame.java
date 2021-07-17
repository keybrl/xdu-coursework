package libms.views.user;

import libms.views.DefaultBtn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 *  用户界面的弹窗类
 *
 * @author keybrl
 */
class PopFrame extends JFrame {
    PopFrame(String title, String message, String btnTitle) {
        super(title);

        this.setSize(PopFrame.frameWidth, PopFrame.frameHeight);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(null);

        JButton backBtn = new DefaultBtn(btnTitle);
        backBtn.setBounds(280, 110, PopFrame.btnWidth, PopFrame.btnHeight);

        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setBounds(10, 20, PopFrame.frameWidth - 20, 38);
        messageLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));

        backBtn.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                }
        );
        this.add(messageLabel);
        this.add(backBtn);

        this.setVisible(true);
    }

    private static final int frameWidth = 400;
    private static final int frameHeight = 200;
    private static final int btnWidth = 80;
    private static final int btnHeight = 40;
}

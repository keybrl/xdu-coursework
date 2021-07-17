package libms;

import libms.model.DataAPI;
import libms.views.login.MainFrame;


/**
 * 程序入口
 * 初始化数据接口，初始化第一个登录页面
 *
 * @author keybrl
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        // 初始化DataAPI
        DataAPI dataAPI = new DataAPI("localhost", "libms", "libms", "passwd");

        // 初始化face
        Iface iface = new Iface(dataAPI, null);

        MainFrame mainFrame = new MainFrame(iface);
        mainFrame.setVisible(true);
    }
}

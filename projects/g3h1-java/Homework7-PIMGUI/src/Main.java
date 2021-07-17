import pim.model.DataAPI;
import pim.views.gui.main.MainFrame;
import pim.generic.Iface;


/**
 * 应用入口
 *
 * @author 罗阳豪 16130120191
 */
public class Main {
    public static void main(String[] args) {
        DataAPI dataAPI = new DataAPI("39.108.75.56:3306", "pim", "pim", "12345qwert");
        Iface iface = new Iface(dataAPI, null);
        new MainFrame(iface);
    }
}

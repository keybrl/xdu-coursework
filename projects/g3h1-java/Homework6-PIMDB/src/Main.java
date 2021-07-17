import pim.model.DataAPI;
import pim.views.cmd.PIMManager;


/**
 * 应用入口
 *
 * @author 罗阳豪 16130120191
 */
public class Main {
    public static void main(String[] args) {
        DataAPI dataAPI = new DataAPI("39.108.75.56:3306", "pim", "pim", "12345qwert");
        PIMManager pimManager = new PIMManager(dataAPI);
        pimManager.startSession();
    }
}

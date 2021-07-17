package libms;

import libms.model.DataAPI;
import libms.model.orm.User;


/**
 * 全局接口类
 * 由Main创建，在各views类间传递
 *
 * @author keybrl
 */
public class Iface {
    private DataAPI dataAPI;
    private User user;
    Iface(DataAPI dataAPI, User user) {
        this.dataAPI = dataAPI;
        this.user = user;
    }

    public DataAPI getDataAPI() {
        return this.dataAPI;
    }

    public User getUser() {
        return this.user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}

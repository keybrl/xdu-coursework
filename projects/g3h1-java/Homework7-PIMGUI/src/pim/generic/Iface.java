package pim.generic;

import pim.model.DataAPI;


/**
 * 应用通用接口
 * 包括应用所需的DataAPI，和登录的User
 *
 * @author 罗阳豪 16130120191
 */
public class Iface {
    private DataAPI dataAPI;
    private User user;


    public Iface() {
        this.dataAPI = null;
        this.user = null;
    }
    public Iface(DataAPI dataAPI, User user) {
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
    public void setDataAPI(DataAPI dataAPI) {
        if (dataAPI == null) {
            throw new NullPointerException("参数 dataAPI 不能是 null");
        }

        this.dataAPI = dataAPI;
    }

}

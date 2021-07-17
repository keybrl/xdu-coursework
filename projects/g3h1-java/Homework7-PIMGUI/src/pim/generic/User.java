package pim.generic;

import pim.model.DataAPI;

public class User {
    // 用户信息
    private String email;
    private String name;
    private String passwd;

    // 用户状态
    // "unknown" -> 未验证
    // "non-existent" -> 用户不存在
    // "incorrect passwd" -> 密码不正确
    // "ok" -> 一切正常
    private String status;


    public User(String email, String name, String passwd) {
        this.email = email;
        this.name = name;
        this.passwd = passwd;

        this.status = "unknown";
    }


    public String getEmail() {
        return this.email;
    }
    public String getName() {
        return this.name;
    }
    public String getPasswd() {
        return this.passwd;
    }
    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setName(String name) {
        this.name = name;
    }

}

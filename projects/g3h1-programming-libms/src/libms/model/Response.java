package libms.model;

import libms.model.orm.Model;


/**
 * 数据接口响应类
 *
 * @author keybrl
 */
public class Response {
    public int statusCode;
    public String statusMessage;
    public Model[] data;

    Response() {
        this.statusCode = 200;
        this.statusMessage = "ok";
        this.data = null;
    }
    Response(int statusCode) {
        this.statusCode = statusCode;
        switch (this.statusCode) {
            case 200:
                this.statusMessage = "ok";
                break;
            case 400:
                this.statusMessage = "bad request";
                break;
            case 403:
                this.statusMessage = "forbidden";
                break;
            case 404:
                this.statusMessage = "not found";
                break;
            case 500:
                this.statusMessage = "unknown error";
                break;
            default:
                this.statusMessage = "what can i say?";
        }
        this.data = null;
    }
    Response(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = null;
    }
    Response(Model[] data) {
        this.statusCode = 200;
        this.statusMessage = "ok";
        this.data = data;
    }
    Response(int statusCode, String statusMessage, Model[] data) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
    }
}

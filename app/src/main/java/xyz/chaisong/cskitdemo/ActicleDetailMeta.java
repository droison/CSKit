package xyz.chaisong.cskitdemo;

import java.util.List;

/**
 * Created by song on 16/4/11.
 */
public class ActicleDetailMeta {
    private int id = 0;
    private String body;
    private List<String> css;
    private List<String> js;
    private List<String> image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getCss() {
        return css;
    }

    public void setCss(List<String> css) {
        this.css = css;
    }

    public List<String> getJs() {
        return js;
    }

    public void setJs(List<String> js) {
        this.js = js;
    }

    public List<String> getImage() {
        return image;
    }

    public void setImage(List<String> image) {
        this.image = image;
    }
}
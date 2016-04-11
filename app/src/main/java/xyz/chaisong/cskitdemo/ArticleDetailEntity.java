package xyz.chaisong.cskitdemo;

import xyz.chaisong.cskitdemo.network.response.RespBaseMeta;

/**
 * Created by song on 16/4/11.
 */
public class ArticleDetailEntity extends RespBaseMeta {
    private ArticleDetail response;

    public ArticleDetail getResponse() {
        return response;
    }

    public void setResponse(ArticleDetail response) {
        this.response = response;
    }
}


package com.joyplus.adkey.AdkeyUtil;

import java.util.List;

/**
 * Created by UPC on 2016/5/17.
 */
public class URLModel {

    private List<UrlInfoEntity> url_info;

    public List<UrlInfoEntity> getUrl_info() {
        return url_info;
    }

    public void setUrl_info(List<UrlInfoEntity> url_info) {
        this.url_info = url_info;
    }

    public static class UrlInfoEntity {
        private String id;
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

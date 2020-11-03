package com.husker.launcher.managers.net.http;

import org.apache.http.impl.cookie.BasicClientCookie;

import java.net.HttpCookie;
import java.util.Date;

public class Cookie {
    private String name;
    private String value;

    private String path, domain;
    private boolean secure, httpOnly;
    private long time;

    public Cookie(String name, String value){
        this.name = name;
        this.value = value;
    }

    public Cookie(HttpCookie cookie){
        this(cookie.getName(), cookie.getValue());
        setHttpOnly(cookie.isHttpOnly());
        setDomain(cookie.getDomain());
        setPath(cookie.getPath());
        setSecure(cookie.getSecure());
        setTime(cookie.getMaxAge());
    }

    public Cookie(org.apache.http.cookie.Cookie cookie){
        this(cookie.getName(), cookie.getValue());
        setHttpOnly(true);
        setDomain(cookie.getDomain());
        setPath(cookie.getPath());
        setSecure(cookie.isSecure());
        setTime(cookie.getExpiryDate() != null ? cookie.getExpiryDate().getTime() : 0);
    }

    public HttpCookie getHttpCookie(){
        HttpCookie cookie = new HttpCookie(getName(), getValue());
        cookie.setHttpOnly(isHttpOnly());
        cookie.setDomain(getDomain());
        cookie.setPath(getPath());
        cookie.setSecure(isSecure());
        cookie.setMaxAge(getTime());
        return cookie;
    }

    public org.apache.http.cookie.Cookie getApacheCookie(){
        BasicClientCookie cookie = new BasicClientCookie(getName(), getValue());
        cookie.setDomain(getDomain());
        cookie.setPath(getPath());
        cookie.setSecure(isSecure());
        cookie.setExpiryDate(new Date(getTime()));
        return cookie;
    }

    public String toString(){
        return getApacheCookie().toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}

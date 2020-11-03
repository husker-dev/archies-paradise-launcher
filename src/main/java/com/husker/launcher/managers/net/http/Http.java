package com.husker.launcher.managers.net.http;

import org.apache.http.impl.client.BasicCookieStore;

import java.io.IOException;
import java.util.ArrayList;

public class Http {

    public static boolean debug = false;

    private BasicCookieStore cookieStore = new BasicCookieStore();
    private HttpRequest request;
    private String lastContent = "";

    public Http get(CharSequence url) throws IOException {
        request = new Get(url);
        request.setCookieStore(cookieStore);
        request.execute();
        cookieStore = request.getCookieStore();

        if(debug)
            printInfo();
        return this;
    }

    public Http post(CharSequence url) throws IOException {
        request = new Post(url);
        request.setCookieStore(cookieStore);
        request.execute();
        cookieStore = request.getCookieStore();

        if(debug)
            printInfo();
        return this;
    }

    public String getLastHeader(String name){
        return request == null ? null : request.getLastHeader(name);
    }

    public String getFirstHeader(String name){
        return request == null ? null : request.getFirstHeader(name);
    }

    public String getUrl(){
        return request.getUrl();
    }

    public Cookie[] getCookies(){
        ArrayList<Cookie> cookies = new ArrayList<>();
        for(org.apache.http.cookie.Cookie cookie : cookieStore.getCookies())
            cookies.add(new Cookie(cookie));
        return cookies.toArray(new Cookie[0]);
    }

    public void addCookie(Cookie... cookies){
        for(Cookie cookie : cookies)
            cookieStore.addCookie(cookie.getApacheCookie());
    }

    public String getHtmlContent(){
        try{
            lastContent = request == null ? null : request.getHtmlContent();
        }catch (Exception ex){
        }
        return lastContent;
    }

    public BasicCookieStore getCookieStore(){
        return cookieStore;
    }

    public void setCookieStore(BasicCookieStore cookieStore){
        this.cookieStore = cookieStore;
    }

    public void clearCookies(){
        cookieStore.clear();
    }

    public void printInfo(){
        System.out.println("-------------------------------");
        System.out.println((request instanceof Get ? "GET " : "POST ") + request.getUrl());
        for(Cookie cookie : getCookies())
            System.out.println("Cookie: " + cookie.getName() + " = " + cookie.getValue());
        System.out.println("Location: " + request.getLastHeader("location"));
        System.out.println("Content: " + getHtmlContent());
    }
}

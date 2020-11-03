package com.husker.launcher.managers.net.http;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpRequest {

    private final String url;
    private final HashMap<String, String> parameters = new HashMap<>();
    private BasicCookieStore cookieStore = new BasicCookieStore();

    private CloseableHttpResponse response;

    public HttpRequest(CharSequence url){
        this.url = url.toString();
    }

    public abstract void execute() throws IOException;

    public void setParameter(String name, String value){
        parameters.put(name, value);
    }

    public void addCookie(Cookie... cookies){
        for(Cookie cookie : cookies)
            cookieStore.addCookie(cookie.getApacheCookie());
    }

    public Cookie[] getCookies(){
        ArrayList<Cookie> cookies = new ArrayList<>();
        for(org.apache.http.cookie.Cookie cookie : cookieStore.getCookies())
            cookies.add(new Cookie(cookie));
        return cookies.toArray(new Cookie[0]);
    }

    public String getLastHeader(String name){
        return response != null && (response.getLastHeader(name) != null) ? response.getLastHeader(name).getValue() : null;
    }

    public String getFirstHeader(String name){
        return response != null && (response.getFirstHeader(name) != null) ? response.getFirstHeader(name).getValue() : null;
    }

    public String getHtmlContent() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));

        StringBuilder out = new StringBuilder();

        String inputLine;
        while ((inputLine = br.readLine()) != null)
            out.append(inputLine);
        br.close();
        return out.toString();
    }

    public InputStream getInputStream() throws IOException {
        return response.getEntity().getContent();
    }

    public Map<String, String> getParameters(){
        return parameters;
    }

    public String getUrl(){
        return url;
    }

    protected void setResponse(CloseableHttpResponse response){
        this.response = response;
    }

    protected void setCookieStore(BasicCookieStore cookieStore){
        this.cookieStore = cookieStore;
    }

    public BasicCookieStore getCookieStore(){
        return cookieStore;
    }

    protected CloseableHttpClient createClient(){
        return HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                .setDefaultCookieStore(getCookieStore())
                .build();
    }

    protected UrlEncodedFormEntity createParameterList(){
        try {
            return new UrlEncodedFormEntity(new ArrayList<NameValuePair>() {{
                for (Map.Entry<String, String> entry : getParameters().entrySet())
                    add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }}, StandardCharsets.UTF_8);

        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}

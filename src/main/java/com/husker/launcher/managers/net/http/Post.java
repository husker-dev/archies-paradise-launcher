package com.husker.launcher.managers.net.http;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.IOException;
import java.util.Map;

public class Post extends HttpRequest {

    public Post(CharSequence url){
        super(url);
    }

    public void execute() throws IOException {
        // Post request itself
        HttpPost post = new HttpPost(getUrl()){{
            for(Map.Entry<String, String> entry : getParameters().entrySet())
                setParameter(entry.getKey(), entry.getValue());
        }};
        post.setEntity(createParameterList());

        // For out cookies
        HttpClientContext context = HttpClientContext.create();

        setResponse(createClient().execute(post, context));
        setCookieStore((BasicCookieStore) context.getCookieStore());
    }

}

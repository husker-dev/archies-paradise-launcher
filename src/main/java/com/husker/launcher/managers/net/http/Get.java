package com.husker.launcher.managers.net.http;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;

import java.io.IOException;
import java.util.Map;

public class Get extends HttpRequest {

    public Get(CharSequence url) {
        super(url);
    }

    public void execute() throws IOException {
        // Post request itself
        HttpGet get = new HttpGet(getUrl()){{
            for(Map.Entry<String, String> entry : getParameters().entrySet())
                setParameter(entry.getKey(), entry.getValue());
        }};

        // For out cookies
        HttpClientContext context = HttpClientContext.create();

        setResponse(createClient().execute(get, context));
        setCookieStore((BasicCookieStore) context.getCookieStore());
    }
}

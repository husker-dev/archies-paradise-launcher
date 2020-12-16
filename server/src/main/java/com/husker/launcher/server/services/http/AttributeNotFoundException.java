package com.husker.launcher.server.services.http;

import com.husker.launcher.server.services.HttpService;

public class AttributeNotFoundException extends ApiException {

    public AttributeNotFoundException(String attribute){
        super("Attribute '" + attribute + "' not found", HttpService.ErrorCodes.ATTRIBUTE_NOT_FOUND);
    }
}

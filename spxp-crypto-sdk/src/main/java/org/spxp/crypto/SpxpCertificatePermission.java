package org.spxp.crypto;

import java.util.HashMap;

public enum SpxpCertificatePermission {
    
    POST("post"),
    FRIENDS("friends"),
    GRANT("grant"),
    CA("ca"),
    IMPERSONATE("impersonate");

    private String key;
    
    private static HashMap<String, SpxpCertificatePermission> keyToEnum = buildKeyToEnumMap();
    
    private static HashMap<String, SpxpCertificatePermission> buildKeyToEnumMap() {
        HashMap<String, SpxpCertificatePermission> result = new HashMap<>(values().length);
        for(SpxpCertificatePermission value : values()) {
            result.put(value.getKey(), value);
        }
        return result;
    }
    
    private SpxpCertificatePermission(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
    
    public static SpxpCertificatePermission fromKey(String key) {
        SpxpCertificatePermission result = keyToEnum.get(key);
        if(result == null) {
            throw new IllegalArgumentException("Unknoen SpxpCertificatePermission key: "+key);
        }
        return result;
    }
    
}

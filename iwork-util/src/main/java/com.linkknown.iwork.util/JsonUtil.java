package com.linkknown.iwork.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtil {

    public static String writeToString (Object object) {
        String result;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            result = "";
        }
        return result;
    }

    public static Object parseToObject(String object) {
        return parseToObject(object, Object.class);
    }

    public static <T> T parseToObject(String object, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(object, clazz);
    }
}

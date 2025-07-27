package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String formatSuccess(Object data){
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("data", data);
        return gson.toJson(responseMap);
    }

    public static String formatFail(Object data){
        Map<String,Object> responseMap = new HashMap<>();
        responseMap.put("status", "fail");
        responseMap.put("data", data);
        return gson.toJson(responseMap);
    }
}

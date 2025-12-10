package com.example.mybighomework.database.converter;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class StringArrayConverter {
    private static final Gson gson = new Gson();
    
    @TypeConverter
    public static String fromStringArray(String[] value) {
        if (value == null) {
            return null;
        }
        return gson.toJson(value);
    }
    
    @TypeConverter
    public static String[] toStringArray(String value) {
        if (value == null) {
            return null;
        }
        Type arrayType = new TypeToken<String[]>(){}.getType();
        return gson.fromJson(value, arrayType);
    }
}
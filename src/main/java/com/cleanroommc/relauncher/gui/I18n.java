package com.cleanroommc.relauncher.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class I18n {
    private static Map<String, String> locales = new HashMap<>();

    public static void load(String lang) {
        try (InputStream stream = LangUtil.class.getResourceAsStream("/assets/cleanroomrelauncher/lang/en_us.json")){
            for(Map.Entry<String, JsonElement> entry : JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(stream))).getAsJsonObject().entrySet()) {
                locales.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (IOException | NullPointerException e) {
            
        }
        if (!"en_us".equals(lang)) {
            try (InputStream stream = LangUtil.class.getResourceAsStream("/assets/cleanroomrelauncher/lang/" + lang + ".json")){
                for(Map.Entry<String, JsonElement> entry : JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(stream))).getAsJsonObject().entrySet()) {
                    locales.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
                }
            } catch (IOException | NullPointerException e) {
                
            }
        }
    }

    public static String format(String key, Object... args) {
        if (locales.getValue().containsKey(key)) {
            return String.format(locales.getValue().get(key), args);
        } else return key;
    }

    // lang key - lang name
    public static Map.Entry<String, String> getLocales() {
        try (InputStream stream = LangUtil.class.getResourceAsStream("/assets/cleanroomrelauncher/lang/locales.json")) {
            HashMap<String, String> map = new HashMap<>();
            for(Map.Entry<String, JsonElement> entry : JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(stream))).getAsJsonObject().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getAsString());
            }
            return map;
        } catch (IOException | NullPointerException e) {
            
        }
        return Collections.emptyList();
    }

    static {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage().toLowerCase() + "_" + locale.getCountry().toLowerCase();
        load(lang);
    }
}
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

    public static List<String> getLocales() {
        try (InputStream stream = LangUtil.class.getResourceAsStream("/assets/cleanroomrelauncher/lang/locales.json") {
            JsonArray array = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(stream)));
            ArrayList<String> list = new ArrayList<>(array.size());
            for (JsonElement e : array) {
                list.add(e.getAsString());
            }
            return array;
        } catch (IOException | NullPointerException e) {
            
        }
        return Collections.emptyList();
    }

    static {
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage().toLowerCase() + "_" + locale.getCountry().toLowerCase();
        load(CleanroomRelauncher.CON);
    }
}
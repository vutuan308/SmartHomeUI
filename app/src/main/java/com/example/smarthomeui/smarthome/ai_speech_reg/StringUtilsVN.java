package com.example.smarthomeui.smarthome.ai_speech_reg;

import java.text.Normalizer;

public class StringUtilsVN {
    public static String fold(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        t = t.toLowerCase().trim().replaceAll("\\s+", " ");
        return t;
    }
}


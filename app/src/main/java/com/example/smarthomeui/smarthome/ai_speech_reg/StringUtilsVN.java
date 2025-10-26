package com.example.smarthomeui.smarthome.ai_speech_reg;

import java.text.Normalizer;
import java.util.Locale;

public class StringUtilsVN {
    public static String fold(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", ""); // ← dòng này làm mất %
        return n.trim();
    }
}


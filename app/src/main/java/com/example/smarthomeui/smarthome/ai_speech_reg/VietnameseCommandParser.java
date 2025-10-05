package com.example.smarthomeui.smarthome.ai_speech_reg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VietnameseCommandParser {

    private static final String[] ON_WORDS = {"bật","mở","turn on","bật lên"};
    private static final String[] OFF_WORDS = {"tắt","đóng","turn off","tắt đi"};
    private static final String[] TOGGLE_WORDS = {"đổi","chuyển","toggle"};


    private static final String[] LIGHT_WORDS = {"đèn","đèn led","đèn rgb","đèn ngủ"};
    private static final String[] FAN_WORDS = {"quạt","quạt trần","quạt cây"};
    private static final String[] AC_WORDS = {"điều hòa","máy lạnh","máy điều hòa"};


    private static final String[] LOC_LIVING = {"phòng khách","sala","phòng lớn"};
    private static final String[] LOC_BED = {"phòng ngủ","bedroom"};
    private static final String[] LOC_KITCH = {"bếp","nhà bếp"};

    public Command parse(String raw) {
        if (raw == null) return null;
        String text = raw.toLowerCase().trim();


        Command c = new Command();


        if (contains(text, ON_WORDS)) c.intent = Command.Intent.ON;
        else if (contains(text, OFF_WORDS)) c.intent = Command.Intent.OFF;
        else if (contains(text, TOGGLE_WORDS)) c.intent = Command.Intent.TOGGLE;


        if (contains(text, LIGHT_WORDS)) c.device = "light";
        else if (contains(text, FAN_WORDS)) c.device = "fan";
        else if (contains(text, AC_WORDS)) c.device = "ac";


        if (contains(text, LOC_LIVING)) c.location = "living_room";
        else if (contains(text, LOC_BED)) c.location = "bedroom";
        else if (contains(text, LOC_KITCH)) c.location = "kitchen";


        Matcher m = Pattern.compile("(\\d{1,3})").matcher(text);
        if (m.find()) {
            int v = Integer.parseInt(m.group(1));
            if (text.contains("độ") || text.contains("°")) {
                c.intent = Command.Intent.SET_TEMP;
                c.percentOrValue = v;
            } else if (text.contains("%") || text.contains("phần trăm") || text.contains("độ sáng") || text.contains("tốc độ")) {
                c.intent = text.contains("độ sáng") ? Command.Intent.SET_BRIGHTNESS
                        : text.contains("tốc độ") ? Command.Intent.SET_SPEED
                        : Command.Intent.SET_BRIGHTNESS;
                c.percentOrValue = v;
            }
        }
        if (text.contains("đỏ")) c.color = "red";
        else if (text.contains("xanh dương") || text.contains("xanh nước biển")) c.color = "blue";
        else if (text.contains("xanh lá")) c.color = "green";
        else if (text.contains("vàng")) c.color = "yellow";
        else if (text.contains("trắng")) c.color = "white";
        else if (text.contains("tím")) c.color = "purple";


        if (c.intent == null && c.color != null) c.intent = Command.Intent.SET_COLOR;


        if (c.intent == null || c.device == null)
            return null;
        return c;
    }

    private boolean contains(String text, String[] arr) {
        for (String w : arr) if (text.contains(w)) return true;
        return false;
    }
}
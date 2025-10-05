package com.example.smarthomeui.smarthome.ai_speech_reg;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;


import java.util.ArrayList;


public class SpeechHelper {
    private final Context ctx;
    private SpeechRecognizer recognizer;
    private Intent intent;


    public interface Listener {
        void onPartial(String text);
        void onFinal(String text);
        void onError(String message);
    }


    public SpeechHelper(Context ctx) {
        this.ctx = ctx;
        recognizer = SpeechRecognizer.createSpeechRecognizer(ctx);
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
    }


    public void start(Listener listener) {
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onResults(Bundle results) {
                ArrayList<String> texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (texts != null && !texts.isEmpty()) listener.onFinal(texts.get(0));
            }
            @Override public void onPartialResults(Bundle partialResults) {
                ArrayList<String> texts = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (texts != null && !texts.isEmpty()) listener.onPartial(texts.get(0));
            }
            @Override public void onError(int error) { listener.onError("STT error: " + error); }
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
        recognizer.startListening(intent);
    }


    public void stop() { if (recognizer != null) recognizer.stopListening(); }
    public void destroy() { if (recognizer != null) { recognizer.destroy(); recognizer = null; } }
}
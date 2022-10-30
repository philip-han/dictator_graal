package net.cequals.greatdictator.service;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import net.cequals.greatdictator.speechcore.service.Speech;

public class DummySpeechRecognizer implements Speech {

    private Function1<? super Boolean, Unit> recognizerStatecallback;
    private Function2<? super String, ? super Float, Unit> callback;

    @Override
    public void start() {
        callback.invoke("i was caught in a landslide", 1F);
    }

    @Override
    public void end() {
        System.out.println("end");
    }

    @Override
    public void registerCallback(Function2<? super String, ? super Float, Unit> callback) {
        this.callback = callback;
    }

    @Override
    public void registerStateCallback(Function1<? super Boolean,Unit> recognizerStateCallback) {
        this.recognizerStatecallback = recognizerStateCallback;
    }

    @Override
    public void close() {
        System.out.println("close");
    }

}
package net.cequals.greatdictator.speechcore.service;

import org.graalvm.word.WordFactory;
import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CEntryPointLiteral;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import net.cequals.greatdictator.graal.NativeInterface.Headers;

import static net.cequals.greatdictator.graal.NativeInterface.CSpeechRecognizer;

@CContext(Headers.class)
public class FPSpeechRecognizer implements net.cequals.greatdictator.speechcore.service.Speech {

    private static CSpeechRecognizer cSpeechRecognizer;
    private static final CEntryPointLiteral callbackEntryPointLiteral = CEntryPointLiteral.create(FPSpeechRecognizer.class, "callbackFunction", IsolateThread.class, CCharPointer.class, float.class);
    private static final CEntryPointLiteral stateCallbackEntryPointLiteral = CEntryPointLiteral.create(FPSpeechRecognizer.class, "stateCallbackFunction", IsolateThread.class, boolean.class);
    private static Function2<? super String, ? super Float, Unit> callback;
    private static Function1<? super Boolean, Unit> stateCallback;
    private static FPSpeechRecognizer INSTANCE;

    private FPSpeechRecognizer() {}

    public static FPSpeechRecognizer getInstance() {
        if (FPSpeechRecognizer.cSpeechRecognizer == WordFactory.nullPointer()) throw new IllegalStateException("speech recognizer is not set");
        if (INSTANCE == null) INSTANCE = new FPSpeechRecognizer();
        return INSTANCE;
    }

    public static void setCSpeechRecognizer(CSpeechRecognizer cSpeechRecognizer) {
        FPSpeechRecognizer.cSpeechRecognizer = cSpeechRecognizer;
    }

    @Override
    public void start() {
        cSpeechRecognizer.getStart().invoke();
    }

    @Override
    public void end() {
        cSpeechRecognizer.getEnd().invoke();
    }

    @Override
    public void registerCallback(Function2<? super String, ? super Float, Unit> callback) {
        FPSpeechRecognizer.callback = callback;
        cSpeechRecognizer.getRegisterCallback().invoke(callbackEntryPointLiteral.getFunctionPointer());
    }

    @Override
    public void registerStateCallback(Function1<? super Boolean,Unit> stateCallback) {
        FPSpeechRecognizer.stateCallback = stateCallback;
        cSpeechRecognizer.getRegisterStateCallback().invoke(stateCallbackEntryPointLiteral.getFunctionPointer());
    }

    @CEntryPoint(name="callbackFunction")
    public static void callbackFunction(IsolateThread isolateThread,  CCharPointer speech, float confidence) {
        if (callback != null) {
            callback.invoke(CTypeConversion.toJavaString(speech), confidence);
        } else System.out.println("speech recognizer callback is null: " + CTypeConversion.toJavaString(speech));
    }

    @CEntryPoint(name="stateCallbackFunction")
    public static void stateCallbackFunction(IsolateThread isolateThread,  boolean state) {
        if (callback != null) stateCallback.invoke(state);
        else System.out.println("recognizer state callback is null: state: " + state);
    }

    @Override
    public void close() {
        FPSpeechRecognizer.callback = null;
        FPSpeechRecognizer.stateCallback = null;
        cSpeechRecognizer.getClose().invoke();
    }

}
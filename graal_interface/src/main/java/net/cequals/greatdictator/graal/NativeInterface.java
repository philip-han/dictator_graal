package net.cequals.greatdictator.graal;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.graalvm.nativeimage.IsolateThread;
import static org.graalvm.nativeimage.UnmanagedMemory.calloc;
import static org.graalvm.nativeimage.UnmanagedMemory.free;
import static org.graalvm.nativeimage.UnmanagedMemory.malloc;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.constant.CEnum;
import org.graalvm.nativeimage.c.constant.CEnumLookup;
import org.graalvm.nativeimage.c.constant.CEnumValue;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CFunctionPointer;
import org.graalvm.nativeimage.c.function.InvokeCFunctionPointer;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.struct.SizeOf;
import org.graalvm.nativeimage.c.type.CCharPointer;
import static org.graalvm.nativeimage.c.type.CTypeConversion.toCString;
import org.graalvm.nativeimage.c.type.WordPointer;
import org.graalvm.word.PointerBase;
import org.graalvm.word.WordFactory;

import net.cequals.greatdictator.graal.NativeInterface.Headers;
import net.cequals.greatdictator.speechcore.SpeechCore;
import net.cequals.greatdictator.speechcore.model.SpeechBusinessLogic.SpeechBusinessData;
import net.cequals.greatdictator.speechcore.service.http.CSpeechDataRetriever;
import net.cequals.greatdictator.speechcore.model.ModulesForJava;
import net.cequals.greatdictator.speechcore.model.ModulesForJavaKt;
import net.cequals.greatdictator.speechcore.service.FPSpeechRecognizer;

@CContext(Headers.class)
public class NativeInterface {
    public int test = 42;
    private static SpeechCore speechCore;
    private static SpeechCore.FlowAdapter adapter;

    public static class Headers implements CContext.Directives {
        @Override
        public List<String> getHeaderFiles() {
            var projDir = new File(System.getProperty("user.dir")).getParentFile().getParentFile().getParent();
            return Collections.unmodifiableList(Arrays.asList(
                    String.format("\"%s/src/main/resources/headers/dictator.h\"", projDir)
            ));
        }
    }

    @CEntryPoint(name="speechcore_init")
    public static void initSpeechCore(IsolateThread isolateThread, PointerBase ctx, SpeechDispatcher speechDispatcher, RecognizerStateDispatcher recognizerStateDispatcher,
                                      ErrorDispatcher errorDispatcher, CSpeechRecognizer cSpeechRecognizer, CSpeechDataFunctionPointer cSpeechDataFunctionPointer) {
        FPSpeechRecognizer.setCSpeechRecognizer(cSpeechRecognizer);
        ModulesForJavaKt.start();
        speechCore = new ModulesForJava(new CSpeechDataRetriever(cSpeechDataFunctionPointer), FPSpeechRecognizer.getInstance()).getViewModelCore();
        adapter = speechCore.new FlowAdapter();
        adapter.observe((List<SpeechBusinessData> list) -> {
                // provision for null pointer termination
                WordPointer cList = allocArray(list.size() + 1);
                IntStream.range(0, list.size()).forEachOrdered(i -> {
                    cList.write(i, convertToCSpeechBusinessData(list.get(i)));
                });
                // write a null pointer to mark end of array
                cList.write(list.size(), WordFactory.nullPointer());
                if (speechDispatcher.isNonNull()) {
                    speechDispatcher.invoke(ctx, cList);
                }
                free(cList);
            },
            (Boolean state) -> {
                if (recognizerStateDispatcher.isNonNull()) recognizerStateDispatcher.invoke(ctx, state);
            },
            (String errorMessage) -> {
                var cErrorMessage = allocString(errorMessage);
                if (errorDispatcher.isNonNull()) errorDispatcher.invoke(ctx, cErrorMessage);
                free(cErrorMessage);
            }
        );
    }

    private static CSpeechBusinessData convertToCSpeechBusinessData(SpeechBusinessData sbd) {
        CSpeechBusinessData csbd = malloc(SizeOf.get(CSpeechBusinessData.class));
        csbd.setSpeech(allocString(sbd.getSpeech()));
        csbd.setConfidenceRatingColor(allocString(sbd.getConfidenceRatingColor()));
        csbd.setSentiment(allocString(sbd.getSentiment()));
        csbd.setInterrogative(sbd.getInterrogative());
        return csbd;
    }

    @CEntryPoint(name="speechcore_toggle")
    public static void toggleSpeechCore(IsolateThread isolateThread) {
        speechCore.toggle();
    }

    @CEntryPoint(name="speechcore_start")
    public static void startSpeechCore(IsolateThread isolateThread) {
        speechCore.startDictation();
    }

    @CEntryPoint(name="speechcore_end")
    public static void endSpeechCore(IsolateThread isolateThread) {
        speechCore.stopDictation();
    }

    @CEntryPoint(name="speechcore_close")
    public static void closeSpeechCore(IsolateThread isolateThread) {
        speechCore.close();
    }

    @CFunction
    protected static native CCharPointer strdup(CCharPointer s);

    public static WordPointer allocArray(int size) {
        return calloc(size * SizeOf.get(WordPointer.class));
    }

    public static CCharPointer allocString(String s) {
        return strdup(toCString(s).get());
    }

    public interface CSpeechDataFunctionPointer extends CFunctionPointer {
        @InvokeCFunctionPointer
        CEither invoke(CCharPointer uri, CCharPointer speech);
    }

    public interface SpeechDispatcher extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(PointerBase ctx, WordPointer speechArr);
    }

    public interface RecognizerStateDispatcher extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(PointerBase ctx, boolean state);
    }

    public interface ErrorDispatcher extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(PointerBase ctx, CCharPointer errorMessage);
    }

    public interface StartFP extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke();
    }

    public interface EndFP extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke();
    }

    public interface CloseFP extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke();
    }

    public interface RegisterCallbackFP extends CFunctionPointer {
        @InvokeCFunctionPointer
        void invoke(CFunctionPointer callbackFP);
    }

    @CEnum("EitherType")
    public enum CEitherType {

        intValue, stringValue;

        @CEnumValue
        public native int getCType();

        @CEnumLookup
        public static native CEitherType fromCValue(int value);

    }

    @CStruct("CError")
    public interface CError extends PointerBase {
        @CField("code") void setCode(int code);
        @CField("code") int getCode();
        @CField("message") void setMessage(CCharPointer message);
        @CField("message") CCharPointer getMessage();
    }

    @CStruct("Either")
    public interface CEither extends PointerBase {
        @CField("error") void setError(CError error);
        @CField("error") CError getError();
        @CField("value") void setValue(PointerBase value);
        @CField("value") PointerBase getValue();
        @CField("type") void setType(int type);
        @CField("type") int getType();
    }

    @CStruct("SpeechRecognizer")
    public interface CSpeechRecognizer extends PointerBase {
        @CField("start") void setStart(StartFP start);
        @CField("start") StartFP getStart();
        @CField("end") void setEnd(EndFP end);
        @CField("end") EndFP getEnd();
        @CField("close") void setClose(CloseFP start);
        @CField("close") CloseFP getClose();
        @CField("registerCallback") void setRegisterCallback(RegisterCallbackFP speechCallbackFP);
        @CField("registerCallback") RegisterCallbackFP getRegisterCallback();
        @CField("registerStateCallback") void setRegisterStateCallback(RegisterCallbackFP stateCallbackFP);
        @CField("registerStateCallback") RegisterCallbackFP getRegisterStateCallback();
    }

    @CStruct("SpeechBusinessData")
    public interface CSpeechBusinessData extends PointerBase {
        @CField("speech") void setSpeech(CCharPointer speech);
        @CField("speech") CCharPointer getSpeech();
        @CField("confidenceRatingColor") void setConfidenceRatingColor(CCharPointer confidenceRatingColor);
        @CField("confidenceRatingColor") CCharPointer getConfidenceRatingColor();
        @CField("sentiment") void setSentiment(CCharPointer sentiment);
        @CField("sentiment") CCharPointer getSentiment();
        @CField("interrogative") void setInterrogative(boolean interrogative);
        @CField("interrogative") boolean isInterrogative();
    }

}
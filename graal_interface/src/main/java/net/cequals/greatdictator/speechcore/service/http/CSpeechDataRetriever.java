package net.cequals.greatdictator.speechcore.service.http;

import javax.annotation.Nonnull;

import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.type.CCharPointer;
import static org.graalvm.nativeimage.c.type.CTypeConversion.toCString;
import static org.graalvm.nativeimage.c.type.CTypeConversion.toJavaString;

import net.cequals.greatdictator.graal.NativeInterface.CError;
import net.cequals.lib.nullsafe.Op;
import net.cequals.lib.CodedException;
import net.cequals.greatdictator.graal.NativeInterface.CSpeechDataFunctionPointer;
import net.cequals.greatdictator.graal.NativeInterface.CEither;
import net.cequals.greatdictator.graal.NativeInterface.Headers;
import net.cequals.greatdictator.speechcore.service.http.CSpeechClient.SpeechDataRetriever;

@CContext(Headers.class)
public class CSpeechDataRetriever implements SpeechDataRetriever {

    private final CSpeechDataFunctionPointer fp;

    public CSpeechDataRetriever(CSpeechDataFunctionPointer fp) {
        this.fp = fp;
    }

    @Override
    public Op<String> retrieve(@Nonnull String uri, @Nonnull String speech) {
        CEither cstr = fp.invoke(toCString(uri).get(), toCString(speech).get());
        if (cstr.getError().isNonNull()) {
            CError cerror = cstr.getError();
            return Op.errorOp(new CodedException(cerror.getCode(), toJavaString(cerror.getMessage())));
        } else {
            var swiftString = toJavaString(((CCharPointer) cstr.getValue()));
            return Op.valueOp(swiftString);
        }
    }

}
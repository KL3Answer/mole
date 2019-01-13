package org.mole.tracer;

import org.mole.tracer.aspect.MethodWatcherTransformer;
import org.mole.tracer.context.TracerContext;
import org.mole.tracer.context.ContextConfig;
import org.mole.tracer.utils.SimpleLoggerManager;
import org.mole.tracer.watcher.WatcherMediator;

import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;

/**
 * Created by k3a
 * on 19-1-1  下午6:04
 */
public class MoleAgent {

    /**
     * @param tracerArgument the path of config file
     */
    public static void premain(String tracerArgument, Instrumentation instrumentation) {
        try {
            System.out.println(
                    "\n\n+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n" +
                            "                                   >>>starting MoleAgent.premain<<<                                  \n" +
                            "config file:" + tracerArgument +
                            "\n+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-++-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+\n");


            final TracerContext context = TracerContext.ContextBuilder.newBuilder()
                    .config(new ContextConfig(Paths.get(tracerArgument)))
                    .build();

            WatcherMediator.init(context);
            instrumentation.addTransformer(new MethodWatcherTransformer(context), true);
            SimpleLoggerManager.info("create and add Transformer finished");
        } catch (Throwable e) {
            SimpleLoggerManager.logFullStackTrace(e);
        }
    }

}

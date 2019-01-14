package org.mole.tracer.aspect;

import org.mole.tracer.context.TracerContext;
import org.mole.tracer.dto.SimpleMethod;
import org.mole.tracer.utils.SimpleLoggerManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Objects;

/**
 * Created by k3a
 * on 19-1-2  下午9:48
 */
public class MethodWatcherTransformer implements ClassFileTransformer {

    private final TracerContext context;

    private final Map<String, Map<String, Map<String, SimpleMethod>>> watchedClass;

    @SuppressWarnings("WeakerAccess")
    public MethodWatcherTransformer(TracerContext context) {
        Objects.requireNonNull(context, "context can not be null");
        this.context = context;

        this.watchedClass = context.getWatchedMethods();
        Objects.requireNonNull(this.watchedClass, "watchedClass can not be null");
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return insertInstIfNeeded(className.replace("/", "."), classfileBuffer);
    }

    /**
     * insert ins into watched methods
     */
    private byte[] insertInstIfNeeded(final String className, byte[] classfileBuffer) {
        final Map<String, Map<String, SimpleMethod>> simpleMethods = watchedClass.get(className);
        if (simpleMethods != null && !simpleMethods.isEmpty()) {
            try {
                final ClassReader cr = new ClassReader(classfileBuffer);
                final ClassWriter cw = new ClassWriter(null, ClassWriter.COMPUTE_FRAMES);
                cr.accept(new WatcherClassVisitor(cw, className, simpleMethods, this.context), ClassReader.SKIP_FRAMES);

                SimpleLoggerManager.info("insert tracer into class :" + className);

                final byte[] bytes = cw.toByteArray();
                final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/home/k3a/Downloads/test.class"));
                bos.write(bytes);
                bos.flush();

                //todo optimize
//                PackManager.v().getPack("wjtp").add();
//                soot.Main.main(new String[]{
//                        "-w",
//                        "-f", "J",
//                        "-p", "cg.spark", "enabled:true",
//                        "-p", "wjtp.myapp", "enabled:true",
//                        "-soot-class-path", String.join(File.pathSeparator, classpath),
//                        args[1]
//                });


                return bytes;

            } catch (Throwable e) {
                SimpleLoggerManager.logFullStackTrace(e);
            }
        }
        return classfileBuffer;
    }

}
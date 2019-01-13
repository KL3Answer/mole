package org.mole.tracer.context;

import org.mole.tracer.dto.SimpleMethod;

import java.util.*;

/**
 * Created by k3a
 * on 19-1-3  下午9:28
 */
public class TracerContext {

    public final ContextConfig config;

    private final Set<SimpleMethod> methods;

    public TracerContext(Set<SimpleMethod> methods, ContextConfig config) {
        Objects.requireNonNull(methods, "methods can not be null");
        Objects.requireNonNull(config, "config can not be null");
        this.methods = methods;
        this.config = config;
    }

    /**
     * key className ,value methods
     */
    public Map<String, Map<String, Map<String, SimpleMethod>>> getWatchedMethods() {
        Map<String, Map<String, Map<String, SimpleMethod>>> map3 = new HashMap<>();
        for (SimpleMethod method : methods) {
            Map<String, Map<String, SimpleMethod>> map2 = map3.computeIfAbsent(method.className, k -> new HashMap<>());
            Map<String, SimpleMethod> map1 = map2.computeIfAbsent(method.methodName, k -> new HashMap<>());
            map1.put(method.argDesc, method);
        }
        return map3;
    }

    public static final class ContextBuilder {
        private Set<SimpleMethod> methods;
        private ContextConfig config;

        private ContextBuilder() {
        }

        public static ContextBuilder newBuilder() {
            return new ContextBuilder();
        }

        public ContextBuilder method(Set<SimpleMethod> method) {
            this.methods = method;
            return this;
        }

        public ContextBuilder config(ContextConfig config) {
            this.config = config;
            return this;
        }

        public TracerContext build() {
            if ((methods == null || methods.isEmpty()) && config != null) {
                HashSet<SimpleMethod> set = new HashSet<>();
                for (String s : config.getWatchedMethods()) {
                    set.add(SimpleMethod.of(s));
                }
                this.methods = set;
            }
            return new TracerContext(methods, config);
        }
    }
}

package org.mole.tracer.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by k3a
 * on 19-1-9  下午10:28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Watched {

    // ordinal of trace object in method args
    int trace() default -1;

    // ordinal of span object in method args
    int span() default -1;
}

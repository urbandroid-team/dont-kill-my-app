package android.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface FloatRange {
    double from() default Double.NEGATIVE_INFINITY;

    boolean fromInclusive() default true;

    double to() default Double.POSITIVE_INFINITY;

    boolean toInclusive() default true;
}

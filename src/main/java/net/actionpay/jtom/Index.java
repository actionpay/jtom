package net.actionpay.jtom;

import java.lang.annotation.*;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
@Target(value = ElementType.ANNOTATION_TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Index {
    IndexType indexType();

    boolean unique() default true;

    String name();
}

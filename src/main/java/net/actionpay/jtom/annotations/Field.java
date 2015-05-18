package net.actionpay.jtom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
@Target(value = {ElementType.FIELD, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Field {
    /**
     * Position of field in data set
     *
     * @return field position in result data set.
     */
    int position();
}

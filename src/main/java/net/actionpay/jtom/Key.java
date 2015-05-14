package net.actionpay.jtom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Key {
    /**
     * Index for this key
     * @return index
     */
    int index();

    /**
     * Position in index for this key
     * @return position
     */
    int position();
}

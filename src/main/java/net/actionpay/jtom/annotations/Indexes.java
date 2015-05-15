package net.actionpay.jtom.annotations;

import java.lang.annotation.*;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Indexes {
    Index[] value();
}

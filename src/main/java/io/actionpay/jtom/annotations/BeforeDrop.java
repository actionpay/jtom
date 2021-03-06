package io.actionpay.jtom.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation to mark methods should be called before entity will be dropped
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface BeforeDrop {
}

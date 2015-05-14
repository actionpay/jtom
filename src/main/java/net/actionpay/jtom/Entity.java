package net.actionpay.jtom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Entity {
    /**
     * Name of data set (space) for entity
     *
     * @return data space name
     */
    String space() default "default";

    /**
     * Connection name of storage where entities stores
     *
     * @return connection name
     */
    String connection() default "default";
}

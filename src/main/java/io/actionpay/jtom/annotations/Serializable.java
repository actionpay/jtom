package io.actionpay.jtom.annotations;

import io.actionpay.jtom.Serializer;

/**
 * Created by Temp on 21.05.2015.
 */
public @interface Serializable {
	Class<? extends Serializer> value();
}

package io.actionpay.jtom;

/**
 * Created by Temp on 21.05.2015.
 */
public interface Serializer {
	Object marshal(Object o);

	Object unmarshal(Object o);

}

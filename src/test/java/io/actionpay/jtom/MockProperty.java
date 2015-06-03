package io.actionpay.jtom;

import io.actionpay.jtom.annotations.Properties;
import io.actionpay.jtom.annotations.Property;

import java.util.Arrays;
import java.util.List;

/**
 * @author Artur Khakimov<djion@ya.ru>
 */
public class MockProperty {
	static {
		SerializeManager.register(MockProperty.class, new Serializer() {

			@Override
			public Object marshal(Object o) {
				if (o instanceof MockProperty) {
					return Arrays.asList(((MockProperty) o).field1, ((MockProperty) o).field2, ((MockProperty) o).field3);
				}
				return null;
			}

			@Override
			public Object unmarshal(Object o) {
				if (o instanceof List) {
					List list = (List) o;
					MockProperty mockProperty = new MockProperty();
					mockProperty.field1 = (String) (list.get(0));
					mockProperty.field2 = (String) (list.get(1));
					mockProperty.field3 = (Integer) (list.get(2));
					return mockProperty;
				}
				return null;
			}
		});
	}

	Integer field3;

	String field1;

	String field2;

	public String getField1() {
		return field1;
	}

	public MockProperty setField1(String field1) {
		this.field1 = field1;
		return this;
	}

	public String getField2() {
		return field2;
	}

	public MockProperty setField2(String field2) {
		this.field2 = field2;
		return this;
	}

	@Override
	public String toString() {
		return field1 + " " + field2 + " " + field3;
	}

	public MockProperty setField3(int field3) {
		this.field3 = field3;
		return this;
	}
}

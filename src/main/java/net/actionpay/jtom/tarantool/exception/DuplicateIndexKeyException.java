package net.actionpay.jtom.tarantool.exception;

/**
 * @author Artur Khakimov <djion@ya.ru>
 */
public class DuplicateIndexKeyException extends Exception {
	public DuplicateIndexKeyException(String s) {
		super(s);
	}
}

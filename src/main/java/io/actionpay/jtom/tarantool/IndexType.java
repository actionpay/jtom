package io.actionpay.jtom.tarantool;

import io.actionpay.jtom.tarantool.exception.WrongTarantoolIndexTypeException;

/**
 * Index type enum for tarantool
 *
 * @author Artur Khakimov <djion@ya.ru>
 */
public enum IndexType {
	INDEX_TYPE_HASH,
	INDEX_TYPE_TREE,
	INDEX_TYPE_BITSET,
	INDEX_TYPE_RTREE
	;

	public String toString(){
		switch (this) {
			case INDEX_TYPE_HASH:
				return "HASH";
			case INDEX_TYPE_BITSET:
				return "BITSET";
			case INDEX_TYPE_RTREE:
				return "RTREE";
			case INDEX_TYPE_TREE:
				return "TREE";
		}
		return "";
	}
}

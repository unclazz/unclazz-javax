package usertools.jp1ajs2.unitdef.core;

class TupleEntryImpl implements TupleEntry {
	private final String k;
	private final String v;
	TupleEntryImpl(String key, String value){
		k = key;
		v = value;
	}
	TupleEntryImpl(String value){
		k = "";
		v = value;
	}
	@Override
	public String getKey() {
		return k;
	}
	@Override
	public String getValue() {
		return v;
	}
	@Override
	public String toString() {
		if (getKey().length() == 0) {
			return getValue();
		} else {
			return getKey() + "=" + getValue();
		}
	}
}

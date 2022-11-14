package org.osate.iso26262.fmea.export;

public class Pair<E extends Object, F extends Object> {
	private E first;
	private F second;

	public Pair(E key, F value) {
		this.first = key;
		this.second = value;
    }


	public E getKey() {
		return first;
	}

	public F getValue() {
		return second;
	}
}
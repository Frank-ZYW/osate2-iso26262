package org.osate.iso26262.fmea;

public enum AP {

	High("H"), Middle("M"), Low("L"), Emp("NULL");

	private final String printname;

	private AP(String name) {
		this.printname = name;
	}

	public String getPrintname() {
		return printname;
	}
}

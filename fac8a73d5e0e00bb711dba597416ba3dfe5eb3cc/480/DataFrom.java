package org.onetwo.ext.permission.api;

import java.util.stream.Stream;

public enum DataFrom {
	SYNC("同步"),
	MANUAL("手动");
//	DATAFIELD
	
	private final String label;

	private DataFrom(String name) {
		this.label = name;
	}

	public String getLabel() {
		return label;
	}
	
	public String getValue() {
		return name();
	}
	
	public static DataFrom of(String code){
		return Stream.of(values()).filter(pt->pt.name().equals(code))
									.findFirst()
									.orElse(MANUAL);
	}
		
}

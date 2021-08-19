package com.example.touchpad.communication;

public enum InputType {
	MOVEMENT(0),
	PRESS(1),
	RELEASE(2),
	OTHER(3);
	
	private int intType;
	
	InputType(int intType) {
		this.intType = intType;
	}
	
	public int getIntType() {
		return this.intType;
	}
	
	public static InputType valueOf(int intType) throws NumberFormatException{
		InputType[] types = InputType.values();
		for(InputType type : types)
			if(type.intType == intType)
				return type;
		
		return null;
	}
}

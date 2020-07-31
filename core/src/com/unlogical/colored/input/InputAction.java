package com.unlogical.colored.input;

public enum InputAction
{
	MOVE_RIGHT,
	MOVE_LEFT,
	MOVE_UP,
	MOVE_DOWN,
	
	JUMP,
	TOGGLE_DIMENSIONS,
	PAUSE,
	INTERACT,
	CONFIRM,
	CANCEL,
	DELETE,
	BACK;
	
	public static InputAction getAction(String action)
	{
		return getAction(Integer.parseInt(action));
	}
	
	public static InputAction getAction(int action)
	{
		return values()[action];
	}
	
	public static boolean contains(int index)
	{
		return index >= 0 && index < values().length;
	}
}

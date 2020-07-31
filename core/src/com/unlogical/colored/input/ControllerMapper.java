package com.unlogical.colored.input;

import java.util.HashMap;

import com.badlogic.gdx.controllers.mappings.Xbox;

@SuppressWarnings("serial")
public class ControllerMapper
{
	private static final HashMap<InputAction, Integer> defaultButtonActionMapping = new HashMap<InputAction, Integer>()
	{
		{
			put(InputAction.JUMP, Xbox.A);
			put(InputAction.INTERACT, Xbox.X);
			put(InputAction.TOGGLE_DIMENSIONS, Xbox.Y);
			put(InputAction.PAUSE, Xbox.START);
			put(InputAction.BACK, Xbox.B);
		}
	};

	private static final HashMap<InputAction, Integer> defaultAxisActionMapping = new HashMap<InputAction, Integer>()
	{
		{
			put(InputAction.MOVE_UP, 0);
			put(InputAction.MOVE_RIGHT, 1);
			put(InputAction.MOVE_DOWN, 2);
			put(InputAction.MOVE_LEFT, 3);
		}
	};
	
	private static final HashMap<ControllerAxis, Integer> defaultAxisMapping = new HashMap<ControllerAxis, Integer>()
	{
		{
			put(ControllerAxis.L_HORIZONTAL, Xbox.L_STICK_HORIZONTAL_AXIS);
			put(ControllerAxis.L_VERTICAL, Xbox.L_STICK_VERTICAL_AXIS);
			put(ControllerAxis.R_HORIZONTAL, Xbox.R_STICK_HORIZONTAL_AXIS);
			put(ControllerAxis.R_VERTICAL, Xbox.R_STICK_VERTICAL_AXIS);
		}
	};
	public int getIndex(InputAction action)
	{
		return defaultButtonActionMapping.get(action);
	}

	public static int mapButton(InputAction action)
	{
		return defaultButtonActionMapping.get(action);
	}

	public static boolean isButtonMapped(InputAction action)
	{
		return defaultButtonActionMapping.containsKey(action);
	}

	public static int mapAxis(ControllerAxis axis)
	{
		return defaultAxisMapping.get(axis);
	}
	
	public static int mapAxis(InputAction action)
	{
		return defaultAxisActionMapping.get(action);
	}

	public static int getButtonCount()
	{
		return defaultButtonActionMapping.size();
	}

	public static HashMap<InputAction, Integer> getButtonMapping()
	{
		return defaultButtonActionMapping;
	}
}

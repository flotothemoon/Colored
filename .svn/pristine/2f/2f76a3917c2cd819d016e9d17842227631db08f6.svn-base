package com.unlogical.colored.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class KeyBindings
{
	private static Map<InputAction, KeyGroup> keyBindings = new HashMap<InputAction, KeyGroup>();

	public static void init()
	{
		keyBindings.clear();

		keyBindings.put(InputAction.MOVE_LEFT, new KeyGroup(Input.Keys.LEFT, Input.Keys.A));
		keyBindings.put(InputAction.MOVE_RIGHT, new KeyGroup(Input.Keys.RIGHT, Input.Keys.D));
		keyBindings.put(InputAction.MOVE_UP, new KeyGroup(Input.Keys.UP, Input.Keys.W));
		keyBindings.put(InputAction.MOVE_DOWN, new KeyGroup(Input.Keys.DOWN, Input.Keys.S));

		keyBindings.put(InputAction.INTERACT, new KeyGroup(Input.Keys.UP, Input.Keys.W));
		keyBindings.put(InputAction.JUMP, new KeyGroup(Input.Keys.SPACE));
		keyBindings.put(InputAction.TOGGLE_DIMENSIONS, new KeyGroup(Input.Keys.SHIFT_LEFT, Input.Keys.SHIFT_RIGHT));
		keyBindings.put(InputAction.CONFIRM, new KeyGroup(Input.Keys.ENTER));
		keyBindings.put(InputAction.CANCEL, new KeyGroup(Input.Keys.FORWARD_DEL));
		keyBindings.put(InputAction.DELETE, new KeyGroup(Input.Keys.FORWARD_DEL));
		keyBindings.put(InputAction.PAUSE, new KeyGroup(Input.Keys.ESCAPE));
		keyBindings.put(InputAction.BACK, new KeyGroup(Input.Keys.DEL));
	}

	public static boolean isKeyBound(int keyCode)
	{
		return isKeyBound(keyCode, null);
	}

	public static boolean isKeyBound(int keyCode, InputAction ignoredAction)
	{
		for (InputAction action : keyBindings.keySet())
		{
			if (ignoredAction != action)
			{
				if (keyBindings.get(action).contains(keyCode))
				{
					return true;
				}
			}
		}

		return false;
	}

	static boolean isKeyPressed(InputAction action)
	{
		KeyGroup group = keyBindings.get(action);

		for (Integer key : group.getKeyCodes())
		{
			if (Gdx.input.isKeyJustPressed(key))
			{
				return true;
			}
		}

		return false;
	}

	static boolean isKeyDown(InputAction action)
	{
		KeyGroup group = keyBindings.get(action);

		for (Integer key : group.getKeyCodes())
		{
			if (Gdx.input.isKeyPressed(key))
			{
				return true;
			}
		}

		return false;
	}

	public static KeyGroup getKeyGroup(InputAction action)
	{
		return keyBindings.get(action);
	}

	public static void containsKey(InputAction action, Integer keyCode)
	{
		keyBindings.get(action).contains(keyCode);
	}

	public static void addKey(InputAction action, Integer keyCode)
	{
		keyBindings.get(action).addKey(keyCode);
	}

	public static void removeKey(InputAction action, Integer keyCode)
	{
		keyBindings.get(action).removeKey(keyCode);
	}

	public static void swapKeys(InputAction action, Integer oldKey, Integer newKey)
	{
		keyBindings.get(action).removeKey(oldKey);
		keyBindings.get(action).addKey(newKey);
	}

	public static void swapMainKey(InputAction action, Integer newKey)
	{
		HashSet<Integer> toReAdd = new HashSet<Integer>();

		for (Integer keyCode : keyBindings.get(action).getKeyCodes())
		{
			keyBindings.get(action).removeKey(keyCode);

			toReAdd.addAll(keyBindings.get(action).getKeyCodes());

			keyBindings.get(action).clear();

			keyBindings.get(action).addKey(newKey);

			keyBindings.get(action).getKeyCodes().addAll(toReAdd);

			break;
		}
	}

	public static int getMainKey(InputAction action)
	{
		for (Integer keyCode : keyBindings.get(action).getKeyCodes())
		{
			return keyCode;
		}

		throw new IllegalStateException("KeyBinding for " + action + " doesn't seem to have any key bound to it. What would that be? A KeyBinding without the key or without the binding?");
	}
}

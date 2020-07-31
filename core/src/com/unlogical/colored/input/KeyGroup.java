package com.unlogical.colored.input;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class KeyGroup
{
	private LinkedHashSet<Integer> keyCodes;
	
	public KeyGroup(Integer... keyCodes)
	{
		this.keyCodes = new LinkedHashSet<Integer>(Arrays.asList(keyCodes));
	}
	
	public boolean contains(Integer keyCode)
	{
		return this.keyCodes.contains(keyCode);
	}
	
	public boolean addKey(Integer keyCode)
	{
		return this.keyCodes.add(keyCode);
	}
	
	public boolean removeKey(Integer keyCode)
	{
		return this.keyCodes.remove(keyCode);
	}
	
	public LinkedHashSet<Integer> getKeyCodes()
	{
		return keyCodes;
	}
	
	void clear()
	{
		this.keyCodes.clear();
	}
	
	boolean isEmpty()
	{
		return this.keyCodes.isEmpty();
	}
}

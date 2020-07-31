package com.unlogical.colored.util;

public class Event
{
	public final Object type;
	public final Object object;
	
	public Event(final Object eventType)
	{
		this.type = eventType;
		this.object = null;
	}
	
	public Event(final Object eventType, final Object eventObject)
	{
		this.type = eventType;
		this.object = eventObject;
	}
}

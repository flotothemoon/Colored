package com.unlogical.colored.gui;

public class HintProperties
{
	public static final HintProperties DEFAULT_PROPERTIES = new HintProperties(false, 250, 100, 128, 0.0f, 0, 0, true);
	public static final HintProperties WARNING_PROPERTIES = new HintProperties(true, 150, 100, 1000, 0.0f, 0, 0, true);
	
	private boolean fadingIn;
	private boolean fadingOut;
	
	private boolean shouldForceDisplay;
	private boolean centeredHeadDisplay = true;
		
	private int fadeInTime;
	private int fadeOutTime;
	
	private int displayTime;
	
	private float startAlpha;
			
	private float xOffset;
	private float yOffset;
	
	public HintProperties(boolean shouldForceDisplay, int fadeInTime, int fadeOutTime, int displayTime, float startAlpha, float xOffset, float yOffset, boolean centeredHeadDisplay)
	{
		this.fadingIn = fadeInTime > 0;
		this.fadingOut = fadeOutTime > 0;
		this.shouldForceDisplay = shouldForceDisplay;
		this.fadeInTime = fadeInTime;
		this.fadeOutTime = fadeOutTime;
		this.displayTime = displayTime;
		this.startAlpha = startAlpha;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.centeredHeadDisplay = centeredHeadDisplay;
	}

	public boolean isFadingIn()
	{
		return fadingIn;
	}

	public boolean isFadingOut()
	{
		return fadingOut;
	}
	
	public boolean shouldForceDisplay()
	{
		return shouldForceDisplay;
	}

	public int getFadeInTime()
	{
		return fadeInTime;
	}

	public int getFadeOutTime()
	{
		return fadeOutTime;
	}

	public int getDisplayTime()
	{
		return displayTime;
	}

	public float getStartAlpha()
	{
		return startAlpha;
	}

	public float getXOffset()
	{
		return xOffset;
	}

	public float getYOffset()
	{
		return yOffset;
	}
	
	public boolean shouldCenterOnHead()
	{
		return centeredHeadDisplay;
	}

	@Override
	public String toString()
	{
		return "HintProperties [fadingIn=" + fadingIn + ", fadingOut=" + fadingOut + ", shouldForceDisplay=" + shouldForceDisplay + ", centeredHeadDisplay=" + centeredHeadDisplay + ", fadeInTime=" + fadeInTime + ", fadeOutTime=" + fadeOutTime + ", displayTime=" + displayTime + ", startAlpha=" + startAlpha + ", xOffset=" + xOffset + ", yOffset=" + yOffset + "]";
	}
}

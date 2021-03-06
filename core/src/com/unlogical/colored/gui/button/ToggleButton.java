package com.unlogical.colored.gui.button;

public abstract class ToggleButton extends Button
{
	private boolean inInitalState;
	private String toggledLabel;
	private String initialLabel;

	public ToggleButton(String initialLabel, String toggledLabel, int xOffset, int yOffset, int width, int height)
	{
		super(initialLabel, xOffset, yOffset, width, height, false);
		
		this.inInitalState = true;
		this.initialLabel = initialLabel;
		this.toggledLabel = toggledLabel;
	}

	@Override
	public void onClick()
	{
		toggle();
	}
	
	public void toggle()
	{
		inInitalState = !inInitalState;

		this.setText(inInitalState ? initialLabel : toggledLabel);

		if (inInitalState)
		{
			toggledToInitialState();
		}
		else
		{
			toggledToToggledState();
		}
		
		this.updateInterface();
	}
	
	public void toggle(boolean toggled)
	{
		if (toggled)
		{
			toToggledState();
		}
		else
		{
			toInitialState();
		}
	}
	
	public void toToggledState()
	{
		if (inInitalState)
		{
			this.inInitalState = false;
			this.setText(toggledLabel);
			this.updateInterface();
			this.toggledToToggledState();
		}
	}
	
	public void toInitialState()
	{
		if (!inInitalState)
		{
			this.inInitalState = true;
			this.setText(initialLabel);
			this.updateInterface();
			this.toggledToInitialState();
		}
	}

	protected abstract void toggledToToggledState();

	protected abstract void toggledToInitialState();
}

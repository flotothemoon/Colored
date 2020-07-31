package com.unlogical.colored.input;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.controllers.Controller;

public class ControllerHandler
{
	public final static int USED_AXIS_COUNT = 4;

	public final float MAX_AXIS_FACTOR = 0.8f;
	public final float MIN_AXIS_FACTOR = 0.4f;

	Controller target;

	private Map<InputAction, Boolean> lastButtons;
	private Map<InputAction, Boolean> pressedButtons;
	private boolean[] lastAxis;
	private boolean[] currentAxis;
	private boolean[] pressedAxis;
	private boolean[] initialAxis;
	private boolean initialising;
	float[] currentAxisFactors;

	public ControllerHandler(Controller controller)
	{
		this.target = controller;

		this.lastButtons = new HashMap<>();
		this.pressedButtons = new HashMap<>();
		this.lastAxis = new boolean[USED_AXIS_COUNT];
		this.currentAxis = new boolean[USED_AXIS_COUNT];
		this.pressedAxis = new boolean[USED_AXIS_COUNT];
		this.currentAxisFactors = new float[USED_AXIS_COUNT];
		this.initialAxis = new boolean[USED_AXIS_COUNT];

		this.initialising = true;
	}

	public void update(int delta)
	{
		for (InputAction action : ControllerMapper.getButtonMapping().keySet())
		{
			pressedButtons.put(action, target.getButton(ControllerMapper.mapButton(action)) && !lastButtons.get(action));

			lastButtons.put(action, pressedButtons.get(action));
		}

		float xAxis = target.getAxis(ControllerMapper.mapAxis(ControllerAxis.L_HORIZONTAL));
		float yAxis = target.getAxis(ControllerMapper.mapAxis(ControllerAxis.L_VERTICAL));

		for (int i = 0; i < USED_AXIS_COUNT; i++)
		{
			currentAxisFactors[i] = 0.0f;
			currentAxis[i] = false;
		}

		if (xAxis < 0)
		{
			if (-xAxis > MIN_AXIS_FACTOR)
			{
				currentAxisFactors[3] = (-xAxis - MIN_AXIS_FACTOR) * (1 / (MAX_AXIS_FACTOR - MIN_AXIS_FACTOR));

				if (currentAxisFactors[3] > 1.0f)
				{
					currentAxisFactors[3] = 1.0f;
				}
			}
		}
		else
		{
			if (xAxis > MIN_AXIS_FACTOR)
			{
				currentAxisFactors[1] = (xAxis - MIN_AXIS_FACTOR) * (1 / (MAX_AXIS_FACTOR - MIN_AXIS_FACTOR));

				if (currentAxisFactors[1] > 1.0f)
				{
					currentAxisFactors[1] = 1.0f;
				}
			}
		}

		if (yAxis < 0)
		{
			if (-yAxis > MIN_AXIS_FACTOR)
			{
				currentAxisFactors[0] = (-yAxis - MIN_AXIS_FACTOR) * (1 / (MAX_AXIS_FACTOR - MIN_AXIS_FACTOR));

				if (currentAxisFactors[0] > 1.0f)
				{
					currentAxisFactors[0] = 1.0f;
				}
			}
		}
		else
		{
			if (yAxis > MIN_AXIS_FACTOR)
			{
				currentAxisFactors[2] = (yAxis - MIN_AXIS_FACTOR) * (1 / (MAX_AXIS_FACTOR - MIN_AXIS_FACTOR));

				if (currentAxisFactors[2] > 1.0f)
				{
					currentAxisFactors[2] = 1.0f;
				}
			}
		}

		for (int i = 0; i < USED_AXIS_COUNT; i++)
		{
			currentAxis[i] = currentAxisFactors[i] > 0.0f;

			if (initialAxis[i])
			{
				if (currentAxis[i])
				{
					currentAxis[i] = false;
				}
				else
				{
					initialAxis[i] = false;
				}
			}

			pressedAxis[i] = currentAxis[i] && !lastAxis[i];

			lastAxis[i] = currentAxis[i];
		}

		if (initialising)
		{
			for (int i = 0; i < initialAxis.length; i++)
			{
				initialAxis[i] = currentAxis[i];
			}

			initialising = false;
		}
	}

	public boolean isButtonDown(InputAction action)
	{
		if (ControllerMapper.isButtonMapped(action))
		{
			return target.getButton(ControllerMapper.mapButton(action));
		}
		else
		{
			return currentAxis[ControllerMapper.mapAxis(action)];
		}
	}

	public boolean isButtonPressed(InputAction action)
	{
		if (ControllerMapper.isButtonMapped(action))
		{
			return pressedButtons.get(action);
		}
		else
		{
			return pressedAxis[ControllerMapper.mapAxis(action)];
		}
	}
}

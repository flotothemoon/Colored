package com.unlogical.colored.util;

import static java.lang.Math.E;
import static java.lang.Math.pow;

import com.unlogical.colored.debug.Debug;

public class Transitions
{
	public static final int TRANSITION_FACTORS = 1000;

	private static float offset;
	private static float factor = 1.0f;
	private static boolean finished;
	private static float[] values;

	static
	{
		Debug.log("Calculating transition data...");

		Transitions.calculateValues();
	}

	public static void calculateValues()
	{
		if (finished)
		{
			throw new RuntimeException("Values already have been calculated. Bam.");
		}

		values = new float[TRANSITION_FACTORS + 1];

		offset = -calculateValue(0);

		factor = 1.0f / calculateValue(TRANSITION_FACTORS);

		for (int i = 0; i < values.length; i++)
		{
			values[i] = calculateValue(i);
		}

		Debug.log("Calculated " + TRANSITION_FACTORS + " factors with offset " + offset + " and factor " + factor + ".");

		finished = true;
	}

	public static float getFactor(float time)
	{
		return getFactor((int) (time * 1000));
	}

	public static float getFactor(int time)
	{
		if (time <= 0)
		{
			return 0.0f;
		}
		else if (time >= TRANSITION_FACTORS)
		{
			return 1.0f;
		}
		else
		{
			return values[time];
		}
	}

	private static float calculateValue(int x)
	{
		return calculateValue((float) x / (float) TRANSITION_FACTORS);
	}

	private static float calculateValue(float x)
	{
		return ((float) (1.0 / (1.0 + 110.0 * pow(E, -9 * x))) + offset) * factor;
	}

	public static boolean isFinished()
	{
		return finished;
	}
}
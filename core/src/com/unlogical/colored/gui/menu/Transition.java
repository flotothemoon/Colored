package com.unlogical.colored.gui.menu;

import com.unlogical.colored.util.Transitions;

public class Transition
{
	private int transitionLength;
	private float transitionTime;
	private float fadingInFactor;
	private float fadingOutFactor;
	private boolean reversed;

	public Transition(int transitionLength)
	{
		this(1.0f, 1.0f, transitionLength);
	}

	public Transition(float fadingInFactor, float fadingOutFactor, int transitionLength)
	{
		this.fadingInFactor = fadingInFactor;
		this.fadingOutFactor = fadingOutFactor;
		this.transitionLength = transitionLength;
		this.transitionTime = 0;
	}

	public void update(float delta)
	{
		if (reversed)
		{
			this.transitionTime -= delta * fadingOutFactor;

			if (this.transitionTime < 0.0f)
			{
				this.transitionTime = 0.0f;
			}
		}
		else
		{
			this.transitionTime += delta * fadingInFactor;

			if (this.transitionTime > this.transitionLength)
			{
				this.transitionTime = this.transitionLength;
			}
		}
	}

	public void setReversed(boolean reversed)
	{
		this.reversed = reversed;
	}

	public void reverse()
	{
		this.reversed = !this.reversed;
	}
	
	public void reset()
	{
		if (reversed)
		{
			this.transitionTime = this.transitionLength;
		}
		else
		{
			this.transitionTime = 0;
		}
	}

	public boolean isReversed()
	{
		return reversed;
	}

	public boolean isComplete()
	{
		return reversed ? transitionTime <= 0 : transitionTime >= transitionLength;
	}
	
	public boolean isActive()
	{
		return !isComplete();
	}

	public float getTransitionFactor()
	{
		return calculateFactor(transitionTime / transitionLength);
	}

	protected float calculateFactor(float factor)
	{
		return Transitions.getFactor(factor);
	}

	public float getRisingFactor()
	{
		return fadingInFactor;
	}

	public float getFallingFactor()
	{
		return fadingOutFactor;
	}

	public static AlphaTransition alphaTransition(float alpha1, float alpha2, float fadeInFactor, float fadeOutFactor, int transitionLength)
	{
		return new AlphaTransition(alpha1, alpha2, fadeInFactor, fadeOutFactor, transitionLength);
	}

	public static AlphaTransition alphaTransition(float alpha1, float alpha2, int transitionLength)
	{
		return new AlphaTransition(alpha1, alpha2, transitionLength);
	}

	public static class AlphaTransition extends Transition
	{
		private float alpha1;
		private float alpha2;

		public AlphaTransition(float alpha1, float alpha2, int transitionLength)
		{
			this(alpha1, alpha2, 1.0f, 1.0f, transitionLength);
		}

		public AlphaTransition(float alpha1, float alpha2, float fadeInFactor, float fadeOutFactor, int transitionLength)
		{
			super(fadeInFactor, fadeOutFactor, transitionLength);

			this.alpha1 = Math.min(alpha1, alpha2);
			this.alpha2 = Math.max(alpha1, alpha2);
		}

		public float getAlphaFactor()
		{
			return getTransitionFactor() * (alpha2 - alpha1) + alpha1;
		}

		public float getAlpha1()
		{
			return alpha1;
		}

		public float getAlpha2()
		{
			return alpha2;
		}
	}
}

package com.unlogical.colored.particle;

import com.badlogic.gdx.graphics.Color;
import com.unlogical.colored.util.ColorTransformer;

public enum ColorDistributionMode implements IColorDistributionMode
{
	RGB_LINEAR()
	{
		@Override
		public Color getColor(Color startColor, Color endColor, float factor1, float factor2, float factor3)
		{
			float rngFloat = factor1;

			colorBuffer.r = Math.min(startColor.r, endColor.r) + rngFloat * Math.abs(endColor.r - startColor.r);
			colorBuffer.g = Math.min(startColor.g, endColor.g) + rngFloat * Math.abs(endColor.g - startColor.g);
			colorBuffer.b = Math.min(startColor.b, endColor.b) + rngFloat * Math.abs(endColor.b - startColor.b);

			return colorBuffer;
		}
	},

	RGB_AREA()
	{
		@Override
		public Color getColor(Color startColor, Color endColor, float factor1, float factor2, float factor3)
		{
			colorBuffer.r = Math.min(startColor.r, endColor.r) + factor1 * Math.abs(endColor.r - startColor.r);
			colorBuffer.g = Math.min(startColor.g, endColor.g) + factor2 * Math.abs(endColor.g - startColor.g);
			colorBuffer.b = Math.min(startColor.b, endColor.b) + factor3 * Math.abs(endColor.b - startColor.b);

			return colorBuffer;
		}
	},

	HSV_LINEAR()
	{
		@Override
		public Color getColor(Color startColor, Color endColor, float factor1, float factor2, float factor3)
		{
			float[] startHSV = ColorTransformer.toHSVColor(startColor, ColorTransformer.floatBuffer1);
			float[] endHSV = ColorTransformer.toHSVColor(endColor, ColorTransformer.floatBuffer2);
			float[] result = ColorTransformer.floatBuffer3;
			float rngFloat = factor1;

			result[0] = Math.min(startHSV[0], endHSV[0]) + rngFloat * Math.abs(endHSV[0] - startHSV[0]);
			result[1] = Math.min(startHSV[1], endHSV[1]) + rngFloat * Math.abs(endHSV[1] - startHSV[1]);
			result[2] = Math.min(startHSV[2], endHSV[2]) + rngFloat * Math.abs(endHSV[2] - startHSV[2]);

			return ColorTransformer.toRGBColor(result, ColorTransformer.colorBuffer1);
		}
	},

	HSV_AREA()
	{
		@Override
		public Color getColor(Color startColor, Color endColor, float factor1, float factor2, float factor3)
		{
			float[] startHSV = ColorTransformer.toHSVColor(startColor, ColorTransformer.floatBuffer1);
			float[] endHSV = ColorTransformer.toHSVColor(endColor, ColorTransformer.floatBuffer2);
			float[] result = ColorTransformer.floatBuffer3;

			result[0] = Math.min(startHSV[0], endHSV[0]) + factor1 * Math.abs(endHSV[0] - startHSV[0]);
			result[1] = Math.min(startHSV[1], endHSV[1]) + factor2 * Math.abs(endHSV[1] - startHSV[1]);
			result[2] = Math.min(startHSV[2], endHSV[2]) + factor3 * Math.abs(endHSV[2] - startHSV[2]);

			return ColorTransformer.toRGBColor(result, ColorTransformer.colorBuffer1);
		}
	},

	;

	private static Color colorBuffer = new Color(1.0f, 1.0f, 1.0f, 1.0f);

	public ColorDistributionMode next()
	{
		return values()[this.ordinal() + 1 >= values().length ? 0 : this.ordinal() + 1];
	}
}

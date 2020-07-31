package com.unlogical.colored.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;

public abstract class GUICheckbox
{
	private float xOffset;
	private float yOffset;

	private float width;
	private float height;

	private boolean checked;

	private boolean active = true;
	private boolean allowChange = true;

	private Rectangle checkbox;

	private Color fillColor = new Color(0.0f, 0.0f, 0.0f, 0.5f);
	private Color crossColor = new Color(Color.WHITE);
	private Color borderColor =  new Color(Color.WHITE);

	public GUICheckbox(boolean checked, float xOffset, float yOffset, float width, float height)
	{
		this.checked = checked;

		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.width = width;
		this.height = height;

		this.checkbox = new Rectangle(xOffset, yOffset, width, height);
	}

	public void toggleState()
	{
		this.checked = !checked;
	}

	public abstract void onChecked();

	public abstract void onUnchecked();

	public void update(int delta)
	{
		crossColor = Color.WHITE;

		this.checkbox.set(xOffset, yOffset, width, height);

		if (allowChange && checkbox.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
		{
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched())
			{
				toggleState();

				if (checked)
				{
					onChecked();
				}
				else
				{
					onUnchecked();
				}
			}
		}
	}

	public void render(float alphaFactor, Batch batch)
	{
		ShapeRenderer sr = Renderer.useShapeRenderer();

		sr.set(ShapeType.Filled);
		ColorHelper.applyAlphaAndCopy(fillColor, alphaFactor, sr);
		sr.rect(checkbox.x, checkbox.y, checkbox.width, checkbox.height);

		sr.set(ShapeType.Line);
		ColorHelper.applyAlphaAndCopy(borderColor, alphaFactor, sr);
		sr.rect(checkbox.x, checkbox.y, checkbox.width, checkbox.height);

		if (checked)
		{
			ColorHelper.applyAlphaAndCopy(crossColor, alphaFactor, sr);

			sr.line(checkbox.getX(), checkbox.getY(), checkbox.getX() + checkbox.getWidth(), checkbox.getY() + checkbox.getHeight());
			sr.line(checkbox.getX(), checkbox.getY() + checkbox.getHeight(), checkbox.getX() + checkbox.getWidth(), checkbox.getY());
		}
	}

	public float getXOffset()
	{
		return xOffset;
	}

	public void setXOffset(float xOffset)
	{
		this.xOffset = xOffset;
	}

	public void adjust(int xChange, int yChange)
	{
		adjustX(xChange);
		adjustY(yChange);
	}

	public void adjustX(float xChange)
	{
		this.xOffset += xChange;
	}

	public float getYOffset()
	{
		return yOffset;
	}

	public void setYOffset(float yOffset)
	{
		this.yOffset = yOffset;
	}

	public void adjustY(float yChange)
	{
		this.yOffset += yChange;
	}

	public float getWidth()
	{
		return width;
	}

	public void setWidth(float width)
	{
		this.width = width;
	}

	public float getHeight()
	{
		return height;
	}

	public void setHeight(float height)
	{
		this.height = height;
	}

	public boolean isChecked()
	{
		return checked;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public boolean isAllowChange()
	{
		return allowChange;
	}

	public void setAllowChange(boolean allowChange)
	{
		this.allowChange = allowChange;
	}

	public Color getFillColor()
	{
		return fillColor;
	}

	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	public Color getCrossColor()
	{
		return crossColor;
	}

	public void setCrossColor(Color crossColor)
	{
		this.crossColor = crossColor;
	}

	public Color getBorderColor()
	{
		return borderColor;
	}

	public void setBorderColor(Color borderColor)
	{
		this.borderColor = borderColor;
	}

	public void setChecked(boolean checked)
	{
		this.checked = checked;
	}

	public int getCenterY()
	{
		return (int) (this.checkbox.getY() + this.checkbox.height / 2);
	}

}

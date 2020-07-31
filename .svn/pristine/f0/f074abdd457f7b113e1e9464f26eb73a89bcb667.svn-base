package com.unlogical.colored.gui;

import static com.unlogical.colored.util.ColorHelper.copyAndAdd;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.unlogical.colored.gui.menu.SelectionMenu;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;

public abstract class Slider<T>
{
	private boolean active;
	private boolean movingMode;

	private float xPos;
	private float yPos;
	private float width;
	private float height;

	private float selectedRange;

	private Color additionalColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);

	private Color selectionColor = new Color(1.0f, 1.0f, 1.0f, SelectionMenu.SELECTION_ALPHA);
	private Color fillColor = new Color(0.2f, 0.2f, 0.2f, SelectionMenu.FILL_ALPHA);

	private Rectangle selectedSlider;
	private Rectangle fullSlider;

	private T reference;

	public Slider(float xPos, float yPos, float width, float height, float initialSelection, T reference)
	{
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		this.selectedRange = initialSelection;
		this.reference = reference;

		createSlider();
	}

	private void createSlider()
	{
		this.fullSlider = new Rectangle(xPos, yPos, width, height);
		this.selectedSlider = new Rectangle(xPos, yPos, width * selectedRange, height);
	}

	public void update(int delta)
	{
		if (active)
		{
			if (fullSlider.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
			{
				if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
				{
					movingMode = true;
				}
			}
			else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT))
			{
				movingMode = false;
			}

			if (movingMode)
			{
				if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
				{
					selectedRange = (InputHandler.getCurrentInputX() - fullSlider.getX()) / width;

					if (selectedRange < 0.0f)
					{
						selectedRange = 0.0f;
					}
					else if (selectedRange > 1.0f)
					{
						selectedRange = 1.0f;
					}

					selectedSlider.setWidth(selectedRange * width);

					onFactorChanged(selectedRange, reference);

					selectionColor.r = selectionColor.r;
					selectionColor.g = selectionColor.g;
					selectionColor.b = selectionColor.b;
				}
			}
		}
	}

	protected abstract void onFactorChanged(float selectedRange, T reference);

	public void render(Batch batch)
	{
		if (active)
		{
			ShapeRenderer sr = Renderer.useShapeRenderer();
			
			sr.set(ShapeType.Filled);
			
			sr.setColor(fillColor);
			
			sr.rect(fullSlider.x, fullSlider.y, fullSlider.width, fullSlider.height);;

			copyAndAdd(selectionColor, ColorHelper.buffer, additionalColor);

			sr.setColor(ColorHelper.buffer);
			sr.rect(selectedSlider.x, selectedSlider.y, selectedSlider.width, selectedSlider.height);;
		}
	}

	public Color getSelectedColor()
	{
		return this.selectionColor;
	}

	public Color getAdditionalColor()
	{
		return this.additionalColor;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public float getSelectedRange()
	{
		return selectedRange;
	}

	public void setSelectedRange(float selectedRange)
	{
		this.selectedRange = selectedRange;
		this.selectedSlider.setWidth(width * selectedRange);
	}

	public void adjust(int xChange, int yChange)
	{
		xPos += xChange;
		yPos += yChange;
		
		this.selectedSlider.setPosition(selectedSlider.getX() + xChange, selectedSlider.getY() + yChange);
		this.fullSlider.setPosition(fullSlider.getX() + xChange, fullSlider.getY() + yChange);
	}

	public int getHeight()
	{
		return (int) this.height;
	}

	public int getCenterY()
	{
		return (int) (this.fullSlider.getY() + this.fullSlider.getHeight() / 2);
	}
	
	public float getX()
	{
		return this.xPos;
	}
	
	public float getY()
	{
		return this.yPos;
	}
}
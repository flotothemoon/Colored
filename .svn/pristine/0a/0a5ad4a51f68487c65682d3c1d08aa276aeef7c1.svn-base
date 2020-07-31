package com.unlogical.colored.gui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.configuration.DisplayConstants;
import com.unlogical.colored.gui.menu.Menu.MenuItem;
import com.unlogical.colored.gui.menu.MenuHandler.MenuEmitterHandler;
import com.unlogical.colored.input.InputAction;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.util.Renderer;

public class MenuItemSlider extends MenuItem
{
	private boolean movingMode;

	private float padding;
	private float selectedRange;

	private MenuEmitterHandler selectedSlider;
	private MenuEmitterHandler fullSlider;

	private IMenuItemSliderListener listener;

	private float sliderWidth;
	private float sliderHeight;

	public MenuItemSlider(String item)
	{
		this(item, DisplayConstants.SLIDER_WIDTH, DisplayConstants.SLIDER_HEIGHT, DisplayConstants.SLIDER_PADDING);
	}

	public MenuItemSlider(String item, float sliderWidth, float sliderHeight, float padding)
	{
		super(item);

		this.padding = padding;
		this.sliderWidth = sliderWidth;
		this.sliderHeight = sliderHeight;

		this.createSlider();
	}

	void createSlider()
	{
		if (this.fullSlider != null)
		{
			this.fullSlider.remove();
		}

		if (this.selectedSlider != null)
		{
			this.selectedSlider.remove();
		}

		this.fullSlider = new MenuEmitterHandler(this.centerX - this.sliderWidth / 2, this.centerY - this.sliderHeight / 2, this.sliderWidth, this.sliderHeight, 0, EmitterType.SLIDER_FULL);
		this.selectedSlider = new MenuEmitterHandler(this.fullSlider.getX(), this.fullSlider.getY(), this.fullSlider.getWidth() * this.selectedRange, this.fullSlider.getHeight(), 0, EmitterType.SLIDER_SELECTED);
	}

	private void updateSliders()
	{
		this.fullSlider.setPosition(this.centerX - this.sliderWidth / 2, this.centerY - this.sliderHeight / 2);
		this.fullSlider.setWidth(this.sliderWidth);
		this.fullSlider.setHeight(this.sliderHeight);

		this.selectedSlider.setPosition(this.centerX - this.sliderWidth / 2, this.centerY - this.sliderHeight / 2);
		this.selectedSlider.setWidth(this.selectedRange * this.sliderWidth);
		this.selectedSlider.setHeight(this.sliderHeight);
	}

	@Override
	public void update(boolean selected, float delta)
	{
		if (this.fullSlider.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
		{
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
			{
				this.movingMode = true;
			}
		}
		else if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT))
		{
			this.movingMode = false;
		}

		if (!selected)
		{
			this.movingMode = false;
		}

		float beforeSelectedRange = this.selectedRange;
		boolean jumped = false;

		if (this.movingMode)
		{
			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
			{
				this.selectedRange = (InputHandler.getCurrentInputX() - this.fullSlider.getX()) / this.sliderWidth;

				jumped = true;
			}
		}

		if (selected)
		{
			if (InputHandler.isKeyPressed(InputAction.MOVE_LEFT))
			{
				this.selectedRange -= 0.00075f * delta;
			}
			else if (InputHandler.isKeyPressed(InputAction.MOVE_RIGHT))
			{
				this.selectedRange += 0.00075f * delta;
			}
		}

		if (this.selectedRange < 0.0f)
		{
			this.selectedRange = 0.0f;
		}
		else if (this.selectedRange > 1.0f)
		{
			this.selectedRange = 1.0f;
		}

		if (beforeSelectedRange != this.selectedRange)
		{
			this.onFactorChanged(this.selectedRange);

			if (jumped)
			{
				this.selectedSlider.getEmitter().getSystem().simulateEmitter(this.selectedSlider.getEmitter(), 5000, 100);
			}
		}

		this.updateSliders();
	}

	@Override
	public void render(float alphaFactor, Color color, Batch batch)
	{
		Renderer.useSpriteBatch();

		Images.draw(this.item, this.centerX, this.centerY - this.padding - Images.getHandler(this.item).getImage().getRegionWidth(), this.scale, 0.0f, color, batch);

		float factor = color.a * 0.35f;

		this.fullSlider.getWrapper().getAddTint().set(factor, factor, factor, 1.0f);
		this.fullSlider.getEmitter().setAlphaFactor(alphaFactor);
		this.fullSlider.getWrapper().draw(batch);

		this.selectedSlider.getWrapper().getAddTint().set(factor, factor, factor, 1.0f);
		this.selectedSlider.getEmitter().setAlphaFactor(alphaFactor);
		this.selectedSlider.getWrapper().draw(batch);
	}

	public void onFactorChanged(float selectedRange)
	{
		if (this.listener != null)
		{
			this.listener.onFactorChanged(selectedRange);
		}
	}

	@Override
	public float getWidth()
	{
		return Math.max(this.sliderWidth, super.getWidth());
	}

	@Override
	public float getHeight()
	{
		return super.getHeight() + this.padding * 2 + this.sliderHeight;
	}

	public void setSelectedRange(float selectedRange)
	{
		this.selectedRange = selectedRange;

		this.onFactorChanged(selectedRange);
	}

	public float getSelectedRange()
	{
		return this.selectedRange;
	}

	public interface IMenuItemSliderListener
	{
		public void onFactorChanged(float selectedRange);
	}

	public void setListener(IMenuItemSliderListener listener)
	{
		this.listener = listener;
	}
}

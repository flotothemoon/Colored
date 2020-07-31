package com.unlogical.colored.gui.menu;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.input.InputAction;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.CursorType;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public abstract class Menu implements IMenu
{
	public static final float UNSELECTED_ALPHA = 0.8f;
	public static final float SELECTED_ALPHA = 1.0f;

	public static final Color UNSELECTED_COLOR = new Color(0.8f, 0.8f, 0.8f, UNSELECTED_ALPHA);
	public static final Color SELECTION_COLOR = new Color(1.0f, 1.0f, 1.0f, SELECTED_ALPHA);

	protected final MenuItem[] options;

	private boolean active;
	private boolean selecting = true;
	protected boolean ignoreButtons;

	private final int CLICK_COOLDOWN = 500;

	protected int selectedOption;
	private int clickCooldown;
	private int height;

	protected float width;

	private float xOffset;
	private float yOffset;

	protected Menu(final MenuItem[] options, float xOffset, float yOffset, float width, float padding, boolean centered)
	{
		if (options.length == 0)
		{
			throw new IllegalArgumentException("Menu options cannot be empty.");
		}

		this.xOffset = xOffset;
		this.yOffset = yOffset;

		this.width = width;
		this.options = new MenuItem[options.length];

		for (int i = 0; i < options.length; i++)
		{
			try
			{
				Images.registerAndLoad(Images.toIdentifier(options[i].getItem()));
			}
			catch (IOException e)
			{
				throw new RuntimeException("Exception while loading icon: " + this.options[i], e);
			}

			this.options[i] = options[i];
			this.options[i].centerX = centered ? xOffset + width / 2 : xOffset;

			if (i == 0)
			{
				this.options[i].centerY = yOffset;
			}
			else
			{
				this.options[i].centerY = this.options[i - 1].centerY + this.options[i - 1].getHeight() / 2 + padding + this.options[i].getHeight() / 2;
			}
			this.options[i].menu = this;
		}

		this.height = (int) (this.options[this.options.length - 1].centerY + this.options[this.options.length - 1].getHeight() / 2 - this.options[0].centerY - this.options[0].getHeight() / 2);
	}

	public Menu(final String[] options, float xOffset, float yOffset, float width, float padding, boolean centered)
	{
		this.xOffset = xOffset;
		this.yOffset = yOffset;

		this.width = width;
		this.options = new MenuItem[options.length];

		for (int i = 0; i < options.length; i++)
		{
			try
			{
				Images.registerAndLoad(Images.toIdentifier(options[i]));
			}
			catch (IOException e)
			{
				throw new RuntimeException("Exception while loading icon: " + this.options[i], e);
			}

			this.options[i] = new MenuItem(Images.toIdentifier(options[i]), centered ? xOffset + width / 2 : xOffset, 0.0f);
			this.options[i].centerY = i == 0 ? yOffset : this.options[i - 1].centerY + this.options[i - 1].getHeight() / 2 + padding + this.options[i].getHeight() / 2;
			this.options[i].menu = this;
		}

		this.height = (int) (this.options[this.options.length - 1].centerY + this.options[this.options.length - 1].getHeight() / 2 - this.options[0].centerY - this.options[0].getHeight() / 2);
	}

	@Override
	public void update(int delta)
	{
		this.clickCooldown -= delta;

		for (int i = 0; i < this.options.length; i++)
		{
			this.options[i].update(i == this.selectedOption, delta);

			if (this.options[i].contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && !this.ignoreButtons)
			{
				this.selectedOption = i;
				this.selecting = true;

				CursorType.useCursor(CursorType.GRAB);
			}
		}

		if (!this.ignoreButtons)
		{
			if (InputHandler.isKeyJustPressed(InputAction.MOVE_DOWN))
			{
				this.selectedOption++;

				if (this.selectedOption == this.options.length)
				{
					this.onSelectAfterBottom();
				}

				this.selecting = true;
			}

			if (InputHandler.isKeyJustPressed(InputAction.MOVE_UP))
			{
				this.selectedOption--;

				if (this.selectedOption < 0)
				{
					this.onSelectAboveTop();
				}

				this.selecting = true;
			}
		}

		if (this.selectedOption >= 0 && this.isAvailable(this.options[this.selectedOption]))
		{
			if (InputHandler.isKeyJustPressed(InputAction.BACK))
			{
				this.onBack();

				MenuHandler.getSoundManager().play(SoundType.MENU_CLICK, false);
			}

			if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || InputHandler.isKeyJustPressed(InputAction.JUMP) || this.options[this.selectedOption].contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched() && this.clickCooldown <= 0) && this.selecting)
			{
				this.onSelect(this.options[this.selectedOption]);

				MenuHandler.getSoundManager().play(SoundType.MENU_CLICK, false);
				this.clickCooldown = this.CLICK_COOLDOWN;
			}
		}

		for (int i = 0; i < this.options.length; i++)
		{
			if (i == this.selectedOption)
			{
				this.options[i].scale += (this.isAvailable(this.options[i]) ? Button.WOBBLE_AVAILABLE_SPEED : Button.WOBBLE_UNAVAILABLE_SPEED) * delta * (this.options[i].addingScale ? 1.0f : -1.0f);

				if (this.options[i].scale > (this.isAvailable(this.options[i]) ? Button.WOBBLE_AVAILABLE_MAX_SCALE_FACTOR : Button.WOBBLE_UNAVAILABLE_MAX_SCALE_FACTOR))
				{
					this.options[i].addingScale = false;
				}
				else if (this.options[i].scale < (this.isAvailable(this.options[i]) ? Button.WOBBLE_AVAILABLE_MIN_SCALE_FACTOR : Button.WOBBLE_UNAVAILABLE_MIN_SCALE_FACTOR))
				{
					this.options[i].addingScale = true;
				}
			}
			else if (this.options[i].scale != 1.0f)
			{
				this.options[i].scale = (this.options[i].scale - 1.0f) * (this.isAvailable(this.options[i]) ? Button.WOBBLE_AVAILABLE_SLOW_DOWN_FACTOR : Button.WOBBLE_UNAVAILABLE_SLOW_DOWN_FACTOR) + 1.0f;
			}
		}

		this.customUpdate(delta);
	}

	protected void onSelectAboveTop()
	{
		this.selectedOption = this.options.length - 1;
	}

	protected void onSelectAfterBottom()
	{
		this.selectedOption = 0;
	}

	@Override
	public void render(float alphaFactor, Batch batch)
	{
		ShaderHandler.disableLighting();

		for (int i = 0; i < this.options.length; i++)
		{
			Renderer.setFont(Fonts.getDefaultFont());

			Color color = this.isAvailable(this.options[i]) ? Color.WHITE : ColorHelper.multiplyAlphaAndCopy(Color.GRAY, 0.3f);
			color = this.selectedOption == i ? color : ColorHelper.multiplyAlphaAndCopy(color, 0.75f);
			color = ColorHelper.multiplyAlphaAndCopy(color, alphaFactor);

			this.options[i].render(alphaFactor, color, batch);

			this.customOptionRender(batch, i, this.options[i]);
		}

		this.customRender(batch);

		ShaderHandler.enableLighting();
	}

	protected boolean isAvailable(MenuItem item)
	{
		return true;
	}

	protected void customOptionRender(Batch batch, int option, MenuItem box)
	{

	}

	public int getHeight()
	{
		return this.height;
	}

	public void onBack()
	{
		this.onSelect(this.options[this.options.length - 1]);
	}

	public abstract void onSelect(MenuItem item);

	@Override
	public boolean isActive()
	{
		return this.active;
	}

	@Override
	public final void setActive(boolean active)
	{
		boolean beforeActive = this.active;

		this.active = active;

		if (!beforeActive && active)
		{
			this.onShow();
		}
	}

	protected void onShow()
	{

	}

	public float getY()
	{
		return this.yOffset;
	}

	public float getY(int option)
	{
		return this.options[option].centerY;
	}

	public void setYOffset(float yOffset)
	{
		this.yOffset = yOffset;
	}

	public float getXOffset()
	{
		return this.xOffset;
	}

	public void customUpdate(int delta)
	{
	}

	public void customRender(Batch batch)
	{
	}

	public int getSelectedOption()
	{
		return this.selectedOption;
	}

	public void setSelectedOption(int selectedOption)
	{
		this.selectedOption = selectedOption;
	}

	public float getWidth()
	{
		return this.width;
	}

	protected static class MenuItem
	{
		private Menu menu;

		protected float centerX;
		protected float centerY;

		protected float scale = 1.0f;

		private boolean addingScale;

		protected String item;

		public MenuItem(String item)
		{
			this.item = item;
		}

		public MenuItem(String item, float centerX, float centerY)
		{
			this.item = item;
			this.centerX = centerX;
			this.centerY = centerY;
		}

		public void setItem(String item)
		{
			if (!this.item.equals(item))
			{
				if (!Images.isLoaded(Images.toIdentifier(item)))
				{
					try
					{
						Images.registerAndLoad(Images.toIdentifier(item));
					}
					catch (IOException e)
					{
						throw new RuntimeException("Exception while loading \"" + item + "\": " + e, e);
					}
				}

				this.item = item;
			}
		}

		public boolean contains(float x, float y)
		{
			float hWidth = this.getWidth() / 2;
			float hHeight = this.getHeight() / 2;

			return x > this.centerX - hWidth && x < this.centerX + hWidth && y > this.centerY - hHeight && y < this.centerY + hHeight;
		}

		public void update(boolean selected, float delta)
		{

		}

		public void render(float alphaFactor, Color color, Batch batch)
		{
			Renderer.useSpriteBatch();

			Images.draw(this.item, this.centerX, this.centerY, this.scale, 0.0f, color, batch);
		}

		public float getHeight()
		{
			return Images.getHandler(this.item).getImage().getRegionHeight() * this.scale;
		}

		public float getWidth()
		{
			return Images.getHandler(this.item).getImage().getRegionWidth() * this.scale;
		}

		public float getCenterX()
		{
			return this.centerX;
		}

		public float getCenterY()
		{
			return this.centerY;
		}

		public float getScale()
		{
			return this.scale;
		}

		public String getItem()
		{
			return this.item;
		}

		public final boolean isParentActive()
		{
			return this.menu.active;
		}
	}
}

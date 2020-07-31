package com.unlogical.colored.gui.button;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.gui.menu.Menu;
import com.unlogical.colored.gui.menu.MenuHandler;
import com.unlogical.colored.input.InputAction;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.resources.image.Images.ImageHandler;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.CursorType;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public abstract class Button
{
	public static final float WOBBLE_AVAILABLE_SPEED = 0.0007f;
	public static final float WOBBLE_AVAILABLE_MAX_SCALE_FACTOR = 1.22f;
	public static final float WOBBLE_AVAILABLE_MIN_SCALE_FACTOR = 0.9f;
	public static final float WOBBLE_AVAILABLE_SLOW_DOWN_FACTOR = 0.96f;
	public static final float WOBBLE_UNAVAILABLE_SPEED = 0.00015f;
	public static final float WOBBLE_UNAVAILABLE_MAX_SCALE_FACTOR = 1.15f;
	public static final float WOBBLE_UNAVAILABLE_MIN_SCALE_FACTOR = 0.95f;
	public static final float WOBBLE_UNAVAILABLE_SLOW_DOWN_FACTOR = 0.95f;

	private int width;
	private int height;
	private int xOffset;
	private int yOffset;
	private int labelOffsetX;

	private int labelOffsetY;

	private float scale = 1.0f;

	private boolean shown = true;
	private boolean active = true;
	private boolean hovered = false;
	private boolean autoHoverControl = true;
	private boolean preferUnicode = false;
	private boolean showBox = true;
	private boolean useEventColors;
	private boolean useSymbol;
	private boolean addingScale;

	private Rectangle buttonBox;

	private String buttonLabel;
	private InputAction linkedAction;

	private Color fillColor = new Color(0.3f, 0.3f, 0.4f, 0.5f);
	private Color textColor = new Color(Color.WHITE);;
	private Color notActiveColor = Color.GRAY;
	private Color hoverColor = new Color(0.4f, 0.4f, 0.5f, 0.55f);
	private Color borderColor = new Color(Color.WHITE);;

	public Button(String label, int xOffset, int yOffset, int width, int height, boolean useSymbol)
	{
		this.buttonLabel = label;

		this.width = width;
		this.height = height;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.useSymbol = useSymbol;

		if (this.useSymbol)
		{
			try
			{
				Images.registerAndLoad(Images.toIdentifier(buttonLabel));
			}
			catch (IOException e)
			{
				Debug.warn("Failed to load symbol for button \"" + label + "\":" + e, e);

				this.useSymbol = false;
			}
		}

		updateInterface();
	}

	public void setProperties(Button other)
	{
		this.borderColor = other.borderColor;
		this.hoverColor = other.hoverColor;
		this.notActiveColor = other.notActiveColor;
		this.textColor = other.textColor;
		this.fillColor = other.fillColor;
	}

	public void update(int delta)
	{
		if (active && shown)
		{
			if (autoHoverControl)
			{
				hovered = false;
			}

			if (contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
			{
				if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched())
				{
					Debug.log("Clicked \"" + buttonLabel + "\" and called its onClick().");
					MenuHandler.getSoundManager().play(SoundType.MENU_CLICK, false);

					onClick();
				}
				else
				{
					hovered = true;

					CursorType.useCursor(CursorType.GRAB);
				}
			}
		}
		
		if (hovered)
		{
			scale += WOBBLE_AVAILABLE_SPEED * delta * (addingScale ? 1.0f : -1.0f);

			if (scale > WOBBLE_UNAVAILABLE_MAX_SCALE_FACTOR)
			{
				addingScale = false;
			}
			else if (scale < WOBBLE_AVAILABLE_MIN_SCALE_FACTOR)
			{
				addingScale = true;
			}
		}
		else if (scale != 1.0f)
		{
			scale = (scale - 1.0f) * WOBBLE_AVAILABLE_SLOW_DOWN_FACTOR + 1.0f;
		}

		customUpdate(delta);
	}

	public void render(float alphaFactor, Batch batch)
	{
		ShaderHandler.disableLighting();

		if (shown)
		{
			ShapeRenderer sr = Renderer.useShapeRenderer();

			if (showBox)
			{
				ColorHelper.applyAlphaAndCopy(fillColor, alphaFactor, sr);

				if (hovered)
				{
					ColorHelper.applyAlphaAndCopy(hoverColor, alphaFactor, sr);
				}

				sr.set(ShapeType.Filled);
				sr.rect(buttonBox.getX(), buttonBox.getY(), buttonBox.getWidth(), buttonBox.getHeight());
			}

			if (useEventColors)
			{
				if (hovered)
				{
					ColorHelper.applyAlphaAndCopy(Menu.SELECTION_COLOR, alphaFactor, batch);
				}
				else
				{
					ColorHelper.applyAlphaAndCopy(Menu.UNSELECTED_COLOR, alphaFactor, batch);
				}
			}
			else
			{
				ColorHelper.applyAlphaAndCopy(textColor, alphaFactor, batch);

				if (!active)
				{
					ColorHelper.applyAlphaAndCopy(notActiveColor, alphaFactor, batch);
				}
			}

			Renderer.useSpriteBatch();

			if (useSymbol)
			{
				if (linkedAction != null)
				{
					ImageHandler actionHandler = Images.getHandler(InputHandler.getInputHintIdentifier(linkedAction));

					float scale = this.scale * 0.8f;
					
					Images.draw(Images.toIdentifier(buttonLabel), getCenterX(), getCenterY() - actionHandler.getHeight() * (0.6f), scale, 0.0f, ColorHelper.applyAlphaAndCopy(batch.getColor(), alphaFactor), batch);
					Images.draw(actionHandler, getCenterX(), getCenterY() + actionHandler.getHeight() * (scale - 0.5f), scale, 0.0f, ColorHelper.applyAlphaAndCopy(batch.getColor(), alphaFactor), batch);
				}
				else
				{
					Images.draw(Images.toIdentifier(buttonLabel), getCenterX(), getCenterY(), scale, 0.0f, ColorHelper.applyAlphaAndCopy(batch.getColor(), alphaFactor), batch);
				}
			}
			else
			{
				Renderer.setFont(Fonts.getDefaultFont());
				Renderer.drawString(buttonLabel, batch.getColor(), labelOffsetX, labelOffsetY, 1.0f, batch);
			}
		}

		ColorHelper.applyAlphaAndCopy(textColor, alphaFactor, batch);

		customRender(batch);

		ShaderHandler.enableLighting();
	}

	public void setPosition(int x, int y)
	{
		this.xOffset = x;
		this.yOffset = y;

		updateInterface();
	}

	public void updateInterface()
	{
		if (this.buttonBox == null)
		{
			this.buttonBox = new Rectangle(xOffset, yOffset, width, height);
		}
		else
		{
			this.buttonBox.setX(xOffset);
			this.buttonBox.setY(yOffset);
			this.buttonBox.setWidth(width);
			this.buttonBox.setHeight(height);
		}

		if (!useSymbol)
		{
			this.labelOffsetX = this.xOffset + (this.width / 2) - Renderer.getWidth(buttonLabel, Fonts.getDefaultFont()) / 2;
			this.labelOffsetY = this.yOffset + ((this.height - Renderer.getLineHeight(Fonts.getDefaultFont())));
		}
	}

	public void customUpdate(int delta)
	{

	}

	public void customRender(Batch batch)
	{

	}

	public abstract void onClick();

	public boolean contains(float x, float y)
	{
		if (useSymbol)
		{
			int centerX = getCenterX();
			int centerY = getCenterY();
			int width = Images.getImage(Images.toIdentifier(this.buttonLabel)).getRegionWidth();
			int height = Images.getImage(Images.toIdentifier(this.buttonLabel)).getRegionHeight();

			return x > centerX - (width * scale) / 2 && x < centerX + (width * scale) / 2 && y > centerY - (height * scale) / 2 && y < centerY + (height * scale) / 2;
		}
		else
		{
			return buttonBox.contains(x, y);
		}
	}

	public boolean isUseEventColors()
	{
		return useEventColors;
	}

	public void setUseEventColors(boolean useEventColors)
	{
		this.useEventColors = useEventColors;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public void adjust(int xChange, int yChange)
	{
		this.xOffset += xChange;
		this.yOffset += yChange;

		this.updateInterface();
	}

	public int getX()
	{
		return xOffset;
	}

	public void setX(int xOffset)
	{
		this.xOffset = xOffset;
		this.updateInterface();
	}

	public void adjustX(int xChange)
	{
		this.xOffset += xChange;
		this.updateInterface();
	}

	public int getCenterX()
	{
		return (int) (this.buttonBox.x + this.buttonBox.width / 2);
	}

	public int getCenterY()
	{
		return (int) (this.buttonBox.getY() + this.buttonBox.getHeight() / 2);
	}

	public int getY()
	{
		return yOffset;
	}

	public void setY(int yOffset)
	{
		this.yOffset = yOffset;
		this.updateInterface();
	}

	public void adjustY(int yChange)
	{
		this.yOffset += yChange;
		this.updateInterface();
	}

	public int getLabelOffsetX()
	{
		return labelOffsetX;
	}

	public void setLabelOffsetX(int labelOffsetX)
	{
		this.labelOffsetX = labelOffsetX;
	}

	public int getLabelOffsetY()
	{
		return labelOffsetY;
	}

	public void setLabelOffsetY(int labelOffsetY)
	{
		this.labelOffsetY = labelOffsetY;
	}

	public boolean isShown()
	{
		return shown;
	}

	public void setShown(boolean shown)
	{
		this.shown = shown;
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public boolean isHovered()
	{
		return hovered;
	}

	public void setHovered(boolean hovered)
	{
		this.hovered = hovered;
	}

	public String getButtonLabel()
	{
		return buttonLabel;
	}

	public void setText(String buttonLabel)
	{
		this.buttonLabel = buttonLabel;

		if (useSymbol && !Images.isLoaded(Images.toIdentifier(buttonLabel)))
		{
			try
			{
				Images.registerAndLoad(Images.toIdentifier(buttonLabel));
			}
			catch (IOException e)
			{
				throw new RuntimeException("Failed to load required button label: " + buttonLabel, e);
			}
		}

		updateInterface();
	}

	public void setLinkedAction(InputAction linkedAction)
	{
		this.linkedAction = linkedAction;
	}

	public Color getFillColor()
	{
		return fillColor;
	}

	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	public Color getTextColor()
	{
		return textColor;
	}

	public void setTextColor(Color textColor)
	{
		this.textColor = textColor;
	}

	public Color getNotActiveColor()
	{
		return notActiveColor;
	}

	public void setNotActiveColor(Color notActiveColor)
	{
		this.notActiveColor = notActiveColor;
	}

	public Color getHoverColor()
	{
		return hoverColor;
	}

	public void setHoverColor(Color hoverColor)
	{
		this.hoverColor = hoverColor;
	}

	public Color getBorderColor()
	{
		return borderColor;
	}

	public void setBorderColor(Color borderColor)
	{
		this.borderColor = borderColor;
	}

	public boolean isPreferUnicode()
	{
		return preferUnicode;
	}

	public void setPreferUnicode(boolean preferUnicode)
	{
		this.preferUnicode = preferUnicode;
	}

	public boolean isShowBox()
	{
		return showBox;
	}

	public void setShowBox(boolean showBox)
	{
		this.showBox = showBox;
	}

	public int getxOffset()
	{
		return xOffset;
	}

	public int getyOffset()
	{
		return yOffset;
	}

	public Rectangle getButtonBox()
	{
		return this.buttonBox;
	}

	public boolean isAutoHoverControl()
	{
		return autoHoverControl;
	}

	public void setAutoHoverControl(boolean autoHoverControl)
	{
		this.autoHoverControl = autoHoverControl;
	}

	public InputAction getLinkedAction()
	{
		return linkedAction;
	}
}

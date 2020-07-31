package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.configuration.DisplayConstants;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public abstract class GUIPanel extends InputAdapter
{
	private static final int TITLE_BAR_HEIGHT = 25;

	protected float xOffset;
	protected float yOffset;

	protected float height;
	protected float width;

	private Rectangle closeBox;
	private Rectangle titleBar;
	protected Rectangle contentBox;

	private boolean shown = true;
	private boolean active;
	private boolean hoveringClose;
	protected boolean movingMode;
	protected boolean showCancelButton;

	protected Color fillColor = new Color(1.0f, 1.0f, 1.0f, 0.7f);
	protected Color titleColor = Color.DARK_GRAY;
	protected Color borderColor = Color.BLACK;
	protected Color fillCloseColor = this.titleColor;
	protected Color borderCloseColor = Color.LIGHT_GRAY;

	protected String title;

	public GUIPanel(String title, float xOffset, float yOffset, float width, float height)
	{
		this.title = title;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.width = width;
		this.height = height;

		this.titleBar = new Rectangle(xOffset, yOffset - TITLE_BAR_HEIGHT, width, TITLE_BAR_HEIGHT);
		this.contentBox = new Rectangle(xOffset, yOffset, width, height);
		this.closeBox = new Rectangle(xOffset + width - DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH, yOffset - TITLE_BAR_HEIGHT, DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH - 4, TITLE_BAR_HEIGHT - 4);

		InputHandler.addInputProcessor(this);
	}

	public final void update(int delta)
	{
		if (this.active)
		{
			this.closeBox.setX(this.xOffset + this.width - DisplayConstants.SELECTION_MENU_SCROLLBAR_WIDTH + 2);
			this.closeBox.setY(this.yOffset - TITLE_BAR_HEIGHT + 2);

			this.contentBox.setX(this.xOffset);
			this.contentBox.setY(this.yOffset);

			this.titleBar.setX(this.xOffset);
			this.titleBar.setY(this.yOffset - TITLE_BAR_HEIGHT);

			this.hoveringClose = this.closeBox.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY());

			if (this.titleBar.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
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

			this.customUpdate(delta);

			if (this.hoveringClose)
			{
				this.fillCloseColor = Color.LIGHT_GRAY;
				this.borderCloseColor = this.titleColor;

				if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
				{
					this.onClose();
				}
			}
			else
			{
				this.fillCloseColor = this.titleColor;
				this.borderCloseColor = Color.LIGHT_GRAY;
			}
		}
	}

	protected void onClose()
	{
		this.setActive(false);
	}

	protected void customUpdate(int delta)
	{
	}

	protected void preDraw(Batch batch)
	{

	}

	public final void render(float alphaFactor, Batch batch)
	{
		if (this.active && this.shown)
		{
			ShaderHandler.disableLighting();

			this.preDraw(batch);

			ShapeRenderer sr = Renderer.useShapeRenderer();

			ColorHelper.applyAlphaAndCopy(this.titleColor, 1.0f, sr);
			sr.set(ShapeType.Filled);
			sr.rect(this.titleBar.x, this.titleBar.y, this.titleBar.width, this.titleBar.height);

			alphaFactor *= 0.8f;

			ColorHelper.applyAlphaAndCopy(this.fillColor, alphaFactor, sr);
			sr.rect(this.contentBox.x, this.contentBox.y, this.contentBox.width, this.contentBox.height);

			Renderer.setFont(Fonts.getSmallFont());
			Renderer.useSpriteBatch();
			Renderer.drawString(this.title, this.getCenterX() - Renderer.getWidth(this.title) / 2, this.titleBar.y + this.titleBar.height / 2 - Renderer.getLineHeight() / 2, 1.0f, batch);

			sr = Renderer.useShapeRenderer();

			ColorHelper.applyAlphaAndCopy(this.borderColor, alphaFactor, sr);
			sr.set(ShapeType.Line);
			sr.rect(this.titleBar.x, this.titleBar.y, this.titleBar.width, this.titleBar.height);
			sr.rect(this.contentBox.x, this.contentBox.y, this.contentBox.width, this.contentBox.height);

			ColorHelper.applyAlphaAndCopy(this.fillCloseColor, alphaFactor, sr);
			sr.set(ShapeType.Filled);
			sr.rect(this.closeBox.x, this.closeBox.y, this.closeBox.width, this.closeBox.height);

			ColorHelper.applyAlphaAndCopy(this.borderCloseColor, alphaFactor, sr);
			sr.set(ShapeType.Line);
			sr.rect(this.closeBox.x, this.closeBox.y, this.closeBox.width, this.closeBox.height);
			sr.line(this.closeBox.getX() + 1, this.closeBox.getY() + 1, this.closeBox.x + this.closeBox.width - 1, this.closeBox.y + this.closeBox.height - 1);
			sr.line(this.closeBox.getX() + 1, this.closeBox.y + this.closeBox.height - 1, this.closeBox.x + this.closeBox.width - 1, this.closeBox.y + 1);

			this.customRender(alphaFactor, batch);

			ShaderHandler.enableLighting();
		}
	}

	protected void customRender(float alphaFactor, Batch batch)
	{
	}

	@Override
	public boolean touchDragged(int newx, int newy, int pointer)
	{
		if (this.movingMode && this.active)
		{
			Vector2 pos = Renderer.unproject(newx, newy);

			newx = (int) pos.x;
			newy = (int) pos.y;

			int yChange = newy - InputHandler.getLastInputY();
			int xChange = newx - InputHandler.getLastInputX();

			if (this.xOffset + xChange > 0 && this.xOffset + this.width + xChange < GameLauncher.getWidth())
			{
				this.xOffset += newx - InputHandler.getLastInputX();
			}
			else
			{
				xChange = 0;
			}

			if (this.yOffset + yChange - TITLE_BAR_HEIGHT > 0 && this.yOffset + yChange + this.height - TITLE_BAR_HEIGHT < GameLauncher.getHeight())
			{
				this.yOffset += yChange;
			}
			else
			{
				yChange = 0;
			}

			this.onPositionUpdate(xChange, yChange);
		}

		return false;
	}

	public void setPosition(int x, int y)
	{
		int beforeX = (int) this.xOffset;
		int beforeY = (int) this.yOffset;

		this.xOffset = x;
		this.yOffset = y;

		this.onPositionUpdate(x - beforeX, y - beforeY);
	}

	public void adjust(int xChange, int yChange)
	{
		this.xOffset += xChange;
		this.yOffset += yChange;
	}

	public void onPositionUpdate(int xChange, int yChange)
	{

	}

	protected void onCancelButtonPressed()
	{

	}

	public void setHeight(float height)
	{
		this.height = height;

		this.titleBar = new Rectangle(this.xOffset, this.yOffset - TITLE_BAR_HEIGHT, this.width, TITLE_BAR_HEIGHT);
		this.contentBox = new Rectangle(this.xOffset, this.yOffset, this.width, height - TITLE_BAR_HEIGHT);
	}

	public void setWidth(float width)
	{
		this.width = width;

		this.titleBar = new Rectangle(this.xOffset, this.yOffset - TITLE_BAR_HEIGHT, width, TITLE_BAR_HEIGHT);
		this.contentBox = new Rectangle(this.xOffset, this.yOffset, width, this.height - TITLE_BAR_HEIGHT);
	}

	public void enable()
	{
		this.active = true;
	}

	public void disable()
	{
		this.active = false;
	}

	public void show()
	{
		this.shown = true;
	}

	public void hide()
	{
		this.shown = false;
	}

	public boolean contains(float mouseX, float mouseY)
	{
		return this.active && this.shown && (this.titleBar.contains(mouseX, mouseY - 3) || this.contentBox.contains(mouseX, mouseY) || this.subContains(mouseX, mouseY));
	}

	protected boolean subContains(float mouseX, float mouseY)
	{
		return false;
	}

	public float getXOffset()
	{
		return this.xOffset;
	}

	public void setXOffset(float xOffset)
	{
		this.xOffset = xOffset;
	}

	public float getCenterX()
	{
		return this.titleBar.getX() + this.titleBar.width / 2;
	}

	public float getYOffset()
	{
		return this.yOffset;
	}

	public void setYOffset(float yOffset)
	{
		this.yOffset = yOffset;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public float getHeight()
	{
		return this.height;
	}

	public float getWidth()
	{
		return this.width;
	}

	public boolean isActive()
	{
		return this.active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public Color getFillColor()
	{
		return this.fillColor;
	}

	public void setFillColor(Color fillColor)
	{
		this.fillColor = fillColor;
	}

	public Color getTitleColor()
	{
		return this.titleColor;
	}

	public void setTitleColor(Color titleColor)
	{
		this.titleColor = titleColor;
	}

	public Color getBorderColor()
	{
		return this.borderColor;
	}

	public void setBorderColor(Color borderColor)
	{
		this.borderColor = borderColor;
	}

	public boolean isShown()
	{
		return this.shown;
	}

	public void setShown(boolean shown)
	{
		this.shown = shown;
	}
}

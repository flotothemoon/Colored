package com.unlogical.colored.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.terrain.TerrainObject;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public class GUIItem
{
	private Color availableColor = new Color(0.5f, 0.5f, 0.5f, 0.5f);
	private Color hoverColor = new Color(1.0f, 1.0f, 1.0f, 0.5f);

	protected String name;
	protected String metadata;

	protected TextureRegion img;
	protected Rectangle itemBox;
	protected Rectangle textBox;

	private int numberOfItems;
	protected int xPos;
	protected int yPos;
	protected int width;
	protected int height;
	protected int borderWidth = 10;

	protected boolean enabled = true;
	protected boolean active = false;
	protected boolean hovered = false;
	protected boolean showBorder = true;
	protected boolean showNameOnHover = true;
	protected boolean sheared = false;
	private boolean selected = false;

	public GUIItem(String name, TextureRegion image, int numberOfItems, int xPos, int yPos, int width, int height)
	{
		this.name = name;
		this.img = image;
		this.numberOfItems = numberOfItems;
		this.xPos = xPos;
		this.yPos = yPos;
		this.width = width;
		this.height = height;
		
		updateBox();
	}

	public GUIItem(String name, String metadata, TextureRegion image, int numberOfItems, int xPos, int yPos, int width,
			int height)
	{
		this(name, image, numberOfItems, xPos, yPos, width, height);

		this.metadata = metadata;
	}

	public void update(int delta)
	{
		if (active && enabled)
		{
			hovered = false;

			if (itemBox.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
			{
				if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
				{
					selected = true;
					hovered = true;
					onClick();
				}
				else
				{
					hovered = true;
					actionOnHover();
				}

				if (showNameOnHover)
				{
					textBox = new Rectangle(InputHandler.getCurrentInputX() + 10, InputHandler.getCurrentInputY() - Renderer.getLineHeight(), Renderer.getWidth(name) + borderWidth, Renderer.getLineHeight() + borderWidth);
				}
			}

			customUpdate(delta);
		}
	}

	public void render(float alphaFactor, Batch batch)
	{
		if (active)
		{
			ShaderHandler.disableLighting();

			ShapeRenderer sr = Renderer.useShapeRenderer();

			if (hovered)
			{
				ColorHelper.applyAlphaAndCopy(hoverColor, alphaFactor, batch);
				sr.rect(itemBox.x, itemBox.y, itemBox.width, itemBox.height);
			}

			if (selected)
			{
				ColorHelper.applyAlphaAndCopy(Color.WHITE, alphaFactor, batch);
				sr.rect(itemBox.x, itemBox.y, itemBox.width, itemBox.height);
			}

			if (img != null)
			{
				Renderer.useSpriteBatch();

				ColorHelper.set(ColorHelper.buffer, 1.0f, 1.0f, 1.0f, alphaFactor);
				batch.setColor(ColorHelper.buffer);

				batch.draw(img, xPos, yPos, itemBox.width, itemBox.height);
			}
			else
			{
				sr.set(ShapeType.Filled);
				ColorHelper.applyAlphaAndCopy(TerrainObject.FILL_COLOR, alphaFactor, batch);
				sr.rect(xPos + borderWidth, yPos + borderWidth, 50, 50);

				sr.set(ShapeType.Line);
				ColorHelper.applyAlphaAndCopy(Color.BLACK, alphaFactor, batch);
				sr.rect(xPos + borderWidth, yPos + borderWidth, 50, 50);
			}

			if (showNameOnHover && hovered)
			{
				Renderer.useShapeRenderer();

				ColorHelper.applyAlphaAndCopy(MapEditor.panelColor, alphaFactor, batch);
				sr.set(ShapeType.Filled);
				sr.rect(textBox.x, textBox.y, textBox.width, textBox.height);

				Renderer.useSpriteBatch();

				ColorHelper.applyAlphaAndCopy(Color.WHITE, alphaFactor, batch);
				Renderer.drawString(name, (int) (textBox.getX() + borderWidth / 2), (int) (textBox.getY() + borderWidth / 2), 1.0f, batch);
			}

			customRender(batch);
			
			ShaderHandler.enableLighting();
		}
	}

	public void renderSheared(Batch batch, int topClipping, int bottomClipping)
	{
		if (active)
		{
			ShapeRenderer sr = Renderer.useShapeRenderer();

			sr.setColor(availableColor);

			if (hovered)
			{
				sr.setColor(hoverColor);
			}

			sr.set(ShapeType.Filled);
			sr.rect(itemBox.x, itemBox.y, itemBox.width, itemBox.height);

			Renderer.useSpriteBatch();

			batch.draw(img, xPos + borderWidth, yPos + borderWidth + topClipping, img.getRegionWidth(), img.getRegionHeight() - topClipping - bottomClipping);

			if (showNameOnHover && hovered)
			{
				sr = Renderer.useShapeRenderer();

				sr.setColor(MapEditor.panelColor);
				sr.rect(textBox.x, textBox.y, textBox.width, textBox.height);

				sr.setColor(Color.WHITE);
				Renderer.drawString(name, (int) (textBox.getX() + borderWidth / 2), (int) (textBox.getY() + borderWidth / 2), 1.0f, batch);

				sr.setColor(Color.WHITE);
			}

			customRender(batch);
		}
	}

	public void updateBox()
	{
		this.itemBox = new Rectangle(xPos, yPos, width + borderWidth * 2, height + borderWidth * 2);
	}

	public GUIItem copy()
	{
		return new GUIItem(name, metadata, img, numberOfItems, xPos, yPos, width, height);
	}

	public void customUpdate(int delta)
	{

	}

	public void customRender(Batch batch)
	{

	}

	public void onClick()
	{

	}

	public void actionOnHover()
	{

	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public TextureRegion getImg()
	{
		return img;
	}

	public void setImg(TextureRegion img)
	{
		this.img = img;
	}

	public int getNumberOfItems()
	{
		return numberOfItems;
	}

	public void setNumberOfItems(int numberOfItems)
	{
		this.numberOfItems = numberOfItems;
	}

	public Rectangle getItemBox()
	{
		return itemBox;
	}

	public void setItemBox(Rectangle itemBox)
	{
		this.itemBox = itemBox;
	}

	public int getxPos()
	{
		return xPos;
	}

	public void setXPos(int xPos)
	{
		this.xPos = xPos;
	}

	public int getYPos()
	{
		return yPos;
	}

	public void setYPos(int yPos)
	{
		this.yPos = yPos;
	}

	public int getBorderWidth()
	{
		return borderWidth;
	}

	public void setBorderWidth(int borderWidth)
	{
		this.borderWidth = borderWidth;
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

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public boolean isShowBorder()
	{
		return showBorder;
	}

	public void setShowBorder(boolean showBorder)
	{
		this.showBorder = showBorder;
	}

	public boolean isShowNameOnHover()
	{
		return showNameOnHover;
	}

	public void setShowNameOnHover(boolean showNameOnHover)
	{
		this.showNameOnHover = showNameOnHover;
	}

	public String getMetadata()
	{
		return metadata;
	}

	public void setMetadata(String metadata)
	{
		this.metadata = metadata;
	}

	public boolean isSheared()
	{
		return sheared;
	}

	public void setSheared(boolean sheared)
	{
		this.sheared = sheared;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isEnabled()
	{
		return enabled;
	}
}

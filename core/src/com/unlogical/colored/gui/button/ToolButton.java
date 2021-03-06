package com.unlogical.colored.gui.button;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.unlogical.colored.gui.GUIItem;
import com.unlogical.colored.gui.menu.Menu;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public abstract class ToolButton extends GUIItem
{
	private boolean selected;
	private boolean available = true;
	private Color notAvailable = new Color(0.2f, 0.2f, 0.3f, 0.5f);
	private Color availableColor = new Color(0.5f, 0.5f, 0.5f, 0.5f);

	public ToolButton(String name, TextureRegion image, int numberOfItems, int xPos, int yPos, int width, int height)
	{
		super(name, image, numberOfItems, xPos, yPos, width, height);
	}

	@Override
	public void updateBox()
	{
		this.itemBox = new Rectangle(xPos, yPos, this.img.getRegionWidth() + borderWidth * 2, this.img.getRegionHeight() + borderWidth * 2);
	}

	@Override
	public void render(float alphaFactor, Batch batch)
	{
		if (active)
		{
			Renderer.setFont(Fonts.getDefaultFont());

			ShapeRenderer sr = Renderer.useShapeRenderer();
			
			ShaderHandler.disableLighting();
			
			sr.setColor(availableColor);
			if (hovered)
			{
				sr.setColor(Color.LIGHT_GRAY);
			}
			else if (selected)
			{
				sr.setColor(Color.GRAY);
			}

			if (!available)
			{
				sr.setColor(notAvailable);
			}

			sr.set(ShapeType.Filled);
			sr.rect(itemBox.x, itemBox.y, itemBox.width, itemBox.height);

			batch.setColor(Color.WHITE);
			batch.draw(img, xPos + borderWidth, yPos + borderWidth);

			if (showNameOnHover && hovered)
			{
				sr.setColor(MapEditor.panelColor);
				sr.rect(textBox.x, textBox.y, textBox.width, textBox.height);
				sr.setColor(Menu.UNSELECTED_COLOR);

				if (selected)
				{
					sr.setColor(Menu.SELECTION_COLOR);
				}

				Renderer.drawString(name, (int) (textBox.getX() + borderWidth / 2), (int) (textBox.getY() + borderWidth / 2), 1.0f, batch);
			}

			ShaderHandler.enableLighting();
			
			customRender(batch);
		}
	}

	public boolean isAvailable()
	{
		return available;
	}

	public void setAvailable(boolean available)
	{
		this.available = available;
	}

	@Override
	public boolean isSelected()
	{
		return selected;
	}

	@Override
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	@Override
	public abstract void onClick();
}

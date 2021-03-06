package com.unlogical.colored.gui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.gui.menu.SelectionMenu.SelectionMenuItem;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;

public class SelectionState extends SelectionMenuItem
{
	private String ID;
	private String name;
	private String type;
	private String additional;
	private String path;

	public SelectionState(String name, String ID, String type, String path, String lastPlayed)
	{
		this.ID = ID;
		this.type = type;
		this.additional = lastPlayed;
		this.name = name;
		this.path = path;
	}

	@Override
	public void render(float alphaFactor, float x, float y, float width, float height, boolean selected, boolean hovered, boolean deleting, Batch batch)
	{
		super.render(alphaFactor, x, y, width, height, selected, hovered, deleting, batch);
		
		Renderer.useSpriteBatch();
		
		batch.setColor(ColorHelper.applyAlphaAndCopy(Color.WHITE, alphaFactor));

		Renderer.setFont(Fonts.getDefaultFont());
		Renderer.drawString(name + " (" + ID + ")", batch.getColor(), x, y + 10, 1.0f, batch);

		Renderer.setFont(Fonts.getSmallFont());
		Renderer.drawString(path, batch.getColor(), x, y - 5 + height - Renderer.getLineHeight() / 2 - 3, 1.0f, batch);
	}

	public String getAdditional()
	{
		return additional;
	}

	public void setAdditional(String additional)
	{
		this.additional = additional;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getID()
	{
		return ID;
	}

	public void setID(String iD)
	{
		ID = iD;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getLastPlayed()
	{
		return additional;
	}

	public void setLastPlayed(String lastPlayed)
	{
		this.additional = lastPlayed;
	}
}

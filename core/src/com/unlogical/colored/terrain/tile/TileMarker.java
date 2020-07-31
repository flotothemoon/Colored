package com.unlogical.colored.terrain.tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public class TileMarker extends Tile
{
	private static boolean showMarkers;
	private int markerID;

	public TileMarker(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}
	
	public static void toggleMarkers()
	{
		showMarkers = !showMarkers;
	}

	@Override
	public void onCreated()
	{
		for (Tile tile : level.getTiles())
		{
			if (tile != this && tile instanceof TileMarker && this.markerID <= ((TileMarker) tile).markerID)
			{
				this.markerID = ((TileMarker) tile).markerID + 1;
			}
		}
	}

	@Override
	public void readMetadata()
	{
		try
		{
			this.markerID = Integer.parseInt(this.metadata);
		}
		catch (Exception e)
		{
			this.markerID = 0;
		}
	}

	@Override
	protected void customDraw(Dimension dimension, Batch batch)
	{
		super.customDraw(dimension, batch);

		ShaderHandler.disableLighting();
		
		if (isHorizontalMarker())
		{
			Renderer.drawString(this.markerID + "", Color.WHITE, this.getCenterX(), this.position.y + 10, 1.0f, batch);
		}
		else
		{
			Renderer.drawString(this.markerID + "", position.x + 32, this.position.y + 25, 1.0f, batch);
		}
		
		ShaderHandler.enableLighting();
	}

	public boolean isHorizontalMarker()
	{
		return this.innerType == 0;
	}

	public boolean isVerticalMarker()
	{
		return this.innerType == 1;
	}

	@Override
	public void writeMetadata()
	{
		this.metadata = this.markerID + "";
	}

	@Override
	public boolean shouldUpdate()
	{
		return false;
	}

	@Override
	public boolean shouldRenderInGame()
	{
		return false;
	}

	@Override
	public boolean isActive()
	{
		return showMarkers;
	}
	
	public int getMarkerID()
	{
		return markerID;
	}
}

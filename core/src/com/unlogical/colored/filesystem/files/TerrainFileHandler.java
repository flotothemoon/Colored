package com.unlogical.colored.filesystem.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FileHandle;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.util.Dimension;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class TerrainFileHandler
{
	private FileHandle file;

	public TerrainFileHandler(FileHandle file)
	{
		this.file = file;
	}

	public TerrainFileHandler(String filePath) throws IOException
	{
		this(FileManager.getOrCreateFile(filePath));
	}

	public void write(List<Tile> tiles, Level level)
	{
		CSVWriter writer = null;
		String[] buffer = new String[11];

		try
		{
			writer = new CSVWriter(new OutputStreamWriter(file.createOutputStream()));
		}
		catch (IOException e)
		{
		}

		for (Tile tile : tiles)
		{
			if (tile.getDimension() == Dimension.COLORED)
			{
				tile.writeMetadata();

				buffer[0] = tile.getType().getTileName();

				buffer[1] = ((tile.getX() - level.getCameraOffset().x)) + "";
				buffer[2] = ((tile.getY() - level.getCameraOffset().y)) + "";

				buffer[3] = tile.getInnerType() + "";
				buffer[4] = tile.getRotation() + "";

				buffer[5] = tile.getMetadata();

				buffer[6] = tile.getHitbox().getRawWidth() + "";
				buffer[7] = tile.getHitbox().getRawHeight() + "";

				buffer[8] = tile.isShared() + "";
				buffer[9] = tile.blocksDimensionTravel() + "";
				buffer[10] = tile.isRawTimeEffected() + "";

				writer.writeNext(buffer);
			}
		}

		try
		{
			writer.close();
			file.closeOutputStream();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error while closing file writer " + e);
		}
	}

	public ArrayList<Tile> read(Level level) throws IOException
	{
		InputStream in;
		CSVReader reader = new CSVReader(new InputStreamReader(in = file.createInputStream()));
		String[] line = null;
		ArrayList<Tile> tiles = level.getTiles();
				
		while ((line = reader.readNext()) != null)
		{
			try
			{
				float x = Float.parseFloat(line[1]);
				float y = Float.parseFloat(line[2]);

				Tile tile = (Tile) Tile.createTile(TileType.getTileType(line[0]), line[5], Integer.parseInt(line[3]), 0.0f, new Vector2(x, y), level, Dimension.COLORED, TileType.getProperty(line[0]).copy(), true);

				tile.updateInnerType();
				tile.setWidth(Float.parseFloat(line[6]));
				tile.setHeight(Float.parseFloat(line[7]));
				tile.setRotation(Float.parseFloat(line[4]));
				tile.setShared(Boolean.parseBoolean(line[8]));
				tile.setBlocksDimensionTravel(Boolean.parseBoolean(line[9]));
				tile.setTimeEffected(Boolean.parseBoolean(line[10]));
				tile.setWasCreatedFromFile(true);
				
				if (tile.hasMirror())
				{
					tile.getMirroredTile().updateInnerType();
					tile.getMirroredTile().setWidth(tile.getHitbox().getRawWidth());
					tile.getMirroredTile().setHeight(tile.getHitbox().getRawHeight());
					tile.getMirroredTile().setRotation(tile.getRotation());
					tile.getMirroredTile().setBlocksDimensionTravel(Boolean.parseBoolean(line[9]));
					tile.getMirroredTile().setTimeEffected(Boolean.parseBoolean(line[10]));
					tile.getMirroredTile().setWasCreatedFromFile(true);
				}

				level.addTile(tile);
			}
			catch (Exception e)
			{
				Debug.warn("Error while reading map data: " + e + " " + e.getMessage(), e);
			}
		}

		level.transferBuffers();
		
		reader.close();
		in.close();
		
		return tiles;
	}
}

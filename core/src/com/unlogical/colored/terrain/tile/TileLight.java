package com.unlogical.colored.terrain.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.ILightSource;
import com.unlogical.colored.util.LightType;
import com.unlogical.colored.util.ShaderHandler;

public class TileLight extends Tile implements ILightSource
{
	private static final Comparator<ILightSource> chainLightComparator = new Comparator<ILightSource>()
	{
		@Override
		public int compare(ILightSource o1, ILightSource o2)
		{
			return (int) (((TileLight) o2).id - ((TileLight) o2).id);
		}
	};

	private List<ILightSource> connectedLights;

	private Color color;
	private String key;

	private float intensity;

	private long id;

	private int distance;
	private int angle;

	public TileLight(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level,
			Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);
	}

	public TileLight initDefault()
	{
		this.color.r = color.g = color.b = color.a = 1.0f;
		this.intensity = 1.0f;
		this.angle = 360;
		this.setInnerType(1);

		return this;
	}

	@Override
	public void onCreated()
	{
		assignID();
	}

	private void assignID()
	{
		for (Tile tile : level.getTiles())
		{
			if (tile.getType() == TileType.LIGHT && ((TileLight) tile).id >= this.id)
			{
				this.id = ((TileLight) tile).id + 1;
			}
		}
	}

	@Override
	public void onInnerTypeChanged()
	{
		ShaderHandler.fetchLights(this.level);
	}

	@Override
	public void writeMetadata()
	{
		this.metadata = color.r + ":" + color.g + ":" + color.b + ":";
		this.metadata += intensity + ":";
		this.metadata += angle + ":";
		this.metadata += distance + ":";
		this.metadata += key + ":";
		this.metadata += id;
	}

	@Override
	public void readMetadata()
	{
		try
		{
			String[] parts = this.metadata.split(":");

			this.color = new Color(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), 1.0f);
			this.intensity = Float.parseFloat(parts[3]);

			if (parts.length >= 10)
			{
				this.angle = Integer.parseInt(parts[6]);
				this.distance = 500;
			}
			else
			{
				this.angle = Integer.parseInt(parts[4]);
				this.distance = Integer.parseInt(parts[5]);

				if (parts.length >= 7)
				{
					this.key = parts[6];

					if (parts.length >= 8)
					{
						this.id = Long.parseLong(parts[7]);
					}
					else
					{
						assignID();
					}
				}
				else
				{
					this.key = "";
				}
			}
		}
		catch (Exception e)
		{
			this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
			this.intensity = 1.0f;
			this.angle = 360;
			this.distance = 300;
			this.key = "";
		}
	}

	@Override
	protected Color getDrawColor(Dimension dimension, Color colorBuffer)
	{
		return colorBuffer;
	}

	@Override
	public boolean isRotationLimited()
	{
		return false;
	}

	@Override
	public boolean shouldRenderInGame()
	{
		return false;
	}

	@Override
	public boolean isLightAffected()
	{
		return false;
	}

	@Override
	public boolean shouldUpdate()
	{
		return false;
	}

	@Override
	public boolean shouldCopy()
	{
		return false;
	}

	@Override
	public LightType getLightType()
	{
		if (this.innerType == 0)
		{
			return LightType.CONE;
		}
		else if (this.innerType == 1)
		{
			return LightType.AMBIENT;
		}
		else if (this.innerType == 2)
		{
			return LightType.CHAIN;
		}

		throw new IllegalStateException("Illegal innertype: " + this.innerType + " doesn't have a mapped light type!");
	}

	@Override
	public float getPositionX()
	{
		return this.hitbox.getCenterX();
	}

	@Override
	public float getPositionY()
	{
		return this.hitbox.getCenterY();
	}

	@Override
	public Color getColor()
	{
		return color;
	}

	@Override
	public Vector2 getPosition()
	{
		return position;
	}

	@Override
	public int getSpotDirection()
	{
		return (int) (90 - rotation);
	}

	@Override
	public int getAngle()
	{
		return angle / 2;
	}

	@Override
	public int getDistance()
	{
		return distance;
	}

	@Override
	public float getIntensity()
	{
		return intensity;
	}

	@Override
	public List<ILightSource> getConnectedLights()
	{
		if (this.connectedLights == null)
		{
			this.connectedLights = new ArrayList<ILightSource>();
		}

		this.connectedLights.clear();

		for (Tile tile : level.getTiles())
		{
			if (tile.getType() == TileType.LIGHT && ((TileLight) tile).getLightType() == LightType.CHAIN && ((TileLight) tile).key.equals(this.key))
			{
				this.connectedLights.add((ILightSource) tile);
			}
		}

		Collections.sort(this.connectedLights, chainLightComparator);

		return connectedLights;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public void setIntensity(float intensity)
	{
		this.intensity = intensity;
	}

	public void setAngle(int angle)
	{
		this.angle = angle;
	}

	public void setDistance(int distance)
	{
		this.distance = distance;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}
}

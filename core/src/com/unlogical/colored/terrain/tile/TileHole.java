package com.unlogical.colored.terrain.tile;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.particle.IEmitterWrapper;
import com.unlogical.colored.particle.IEmitterWrapper.ICustomEmitterInit;
import com.unlogical.colored.particle.ParticleEmitterImpl;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.property.TileProperty;
import com.unlogical.colored.terrain.tile.properties.ITileMirrorable;
import com.unlogical.colored.util.Dimension;

public class TileHole extends Tile implements ITileMirrorable
{
	// between 0.0 for no attraction and 1.0 max
	private float attraction = 1.0f;
	private TileHolePort holePort;

	public TileHole(TileType type, String metadata, int innerType, float rotation, Vector2 position, Level level, Dimension dimension, TileProperty properties, boolean allowMirrors)
	{
		super(type, metadata, innerType, rotation, position, level, dimension, properties, allowMirrors);

		this.addEmitter(EmitterType.DOORSPARKLE, dimension).setStencilled(true);
		this.addEmitter(EmitterType.DOORSPIRAL, dimension, new ICustomEmitterInit()
		{
			@Override
			public void applyCustomInit(IEmitterWrapper wrapper, ParticleEmitterImpl emitter)
			{
				emitter.setInitalSize(emitter.getInitialSize().copy());
				emitter.getInitialSize().set(wrapper.getEmitterWidth(), wrapper.getEmitterHeight());
			}
		}).setStencilled(true);

		this.updateAttractionEmitter();
	}

	@Override
	public void readMetadata()
	{
		try
		{
			String[] parts = this.metadata.split(":");

			this.attraction = Float.parseFloat(parts[0]);
			this.holePort = TileHolePort.valueOf(parts[1]);
		}
		catch (Exception e)
		{
			this.attraction = 0.5f;
			this.holePort = null;
		}
	}

	@Override
	public void writeMetadata()
	{

	}

	public ParticleEmitterImpl updateAttractionEmitter()
	{
		this.removeEmitter(EmitterType.HOLE_ATTRACT, this.dimension);

		return (ParticleEmitterImpl) this.addEmitter(EmitterType.HOLE_ATTRACT, this.dimension, new ICustomEmitterInit()
		{
			@Override
			public void applyCustomInit(IEmitterWrapper wrapper, ParticleEmitterImpl emitter)
			{
				float spawnCountMultiplier = (float) (TileHole.this.attraction * Math.pow(10, TileHole.this.attraction));
				float velocityMultiplier = TileHole.this.attraction * 2.0f;
				float lifetimeMultiplier = 1.2f / velocityMultiplier;

				emitter.setSpawnCount(emitter.getSpawnCount().copy());
				emitter.getSpawnCount().scale(spawnCountMultiplier);

				emitter.setVelocityX(emitter.getVelocityX().copy());
				emitter.getVelocityX().scale(velocityMultiplier);

				emitter.setInitalLife(emitter.getInitialLife().copy());
				emitter.getInitialLife().scale(lifetimeMultiplier);

				emitter.setFadeInTime(emitter.getFadeInTime().copy());
				emitter.getFadeInTime().scale(lifetimeMultiplier);

				emitter.setFadeOutTime(emitter.getFadeOutTime().copy());
				emitter.getFadeOutTime().scale(lifetimeMultiplier);

				emitter.setGrowthFactor(emitter.getGrowthFactor().copy());
				emitter.getGrowthFactor().scale(velocityMultiplier);
			}
		});
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		super.onUpdate(delta, rng);

		float range = 300;
		float force = -0.02f;

		Vector2 buffer = Pools.get(Vector2.class).obtain();

		for (Entity entity : this.level.getEntities())
		{
			if (entity.inSameDimension(this))
			{
				buffer.set(entity.getCenterX(), entity.getCenterY());
				buffer.sub(this.getCenterX(), this.getCenterY());

				float distance = buffer.len();

				if (distance <= range)
				{
					float attractionFactor = (float) (1.0f - Math.pow(distance / range, 1.5f));

					entity.getVelocity().add(Math.signum(buffer.x) * attractionFactor * force, Math.signum(buffer.y) * attractionFactor * force);
				}
			}
		}
	}

	@Override
	public boolean shouldUpdate()
	{
		return true;
	}

	@Override
	public boolean renderStatic()
	{
		return false;
	}

	@Override
	public boolean isReallyChangeable()
	{
		return false;
	}

	@Override
	protected boolean shouldUseStencilledEmitters()
	{
		return true;
	}

	public TileHolePort getHolePort()
	{
		return this.holePort;
	}

	public void setHolePort(TileHolePort holePort)
	{
		this.holePort = holePort;
	}

	public float getAttraction()
	{
		return this.attraction;
	}

	public void setAttraction(float attraction)
	{
		this.attraction = attraction;
	}

	public enum TileHolePort
	{
		WHITE(Color.WHITE), YELLOW(Color.YELLOW), RED(Color.RED),
		BLUE(Color.BLUE), GREEN(Color.GREEN), GOLD(Color.GOLD);

		public final Color color;

		private TileHolePort(Color color)
		{
			this.color = color;
		}
	}
}

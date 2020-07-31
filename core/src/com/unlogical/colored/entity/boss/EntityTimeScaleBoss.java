package com.unlogical.colored.entity.boss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.projectile.EntityIceSpike;
import com.unlogical.colored.entity.projectile.EntityMiniIceSpike;
import com.unlogical.colored.entity.property.EntityProperties;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.LevelObject;
import com.unlogical.colored.util.Renderer;

public class EntityTimeScaleBoss extends EntityBoss
{
	private static final int JUMP_WAIT_TIME = 7000;

	private List<EntityIceSpike> mainSpikes = new ArrayList<EntityIceSpike>();
	private List<EntityMiniIceSpike> miniSpikes = new ArrayList<EntityMiniIceSpike>();
	private EntityIceSpike nextSpike;

	private boolean lastJumping;
	private boolean active = true;

	private int timeUntilJump;

	public EntityTimeScaleBoss(EntityType type, Vector2 position, Level level, Dimension dimension, boolean allowMirrors)
	{
		super(type, position, level, dimension, allowMirrors);
	}

	@Override
	public LevelObject deepCopy(Map<Object, Object> copiedReferences)
	{
		EntityTimeScaleBoss copy = (EntityTimeScaleBoss) super.deepCopy(copiedReferences);

		copy.mainSpikes = Level.deepCopyCollection(this.mainSpikes, copy.mainSpikes, copiedReferences);
		copy.miniSpikes = Level.deepCopyCollection(this.miniSpikes, copy.miniSpikes, copiedReferences);
		copy.nextSpike = (EntityIceSpike) Level.getCopy(this.nextSpike, copiedReferences);
		copy.lastJumping = this.lastJumping;
		copy.timeUntilJump = this.timeUntilJump;

		return copy;
	}

	@Override
	public void init()
	{
		super.init();

		this.mainSpikes.clear();

		for (Entity entity : this.level.getEntities())
		{
			if (entity instanceof EntityIceSpike && entity.getDimension() == Dimension.COLORED)
			{
				if (entity instanceof EntityMiniIceSpike)
				{
					this.miniSpikes.add((EntityMiniIceSpike) entity);
				}
				else
				{
					this.mainSpikes.add((EntityIceSpike) entity);
				}
			}
		}

		Collections.sort(this.mainSpikes, new Comparator<Entity>()
		{
			@Override
			public int compare(Entity o1, Entity o2)
			{
				return (int) (o1.getCenterX() - o2.getCenterX());
			}
		});

		if (!this.mainSpikes.isEmpty())
		{
			this.nextSpike = this.mainSpikes.get(0);
		}
	}

	@Override
	public void onUpdate(float delta, Random rng)
	{
		super.onUpdate(delta, rng);

		if (Math.abs(this.distanceToPlayer.x) < 720.0f && Math.abs(this.distanceToPlayer.y) < 80)
		{
			if (!this.active)
			{
				this.active = true;

				this.timeUntilJump = -1;
			}
		}

		if (this.active)
		{
			this.movingRight = this.nextSpike.getCenterX() > this.getCenterX();

			float speedFactor = 0.5f;

			if (Math.abs(this.distanceToPlayer.x) < 400.0f)
			{
				this.timeUntilJump -= delta;

				speedFactor = 1.0f;
			}

			if (!this.jumping)
			{
				this.velocity.x += this.properties.getAcceleration(delta) * (this.movingRight ? 1 : -1) * speedFactor;

				if (Math.abs(this.velocity.x) > this.properties.getMaxSpeed() * speedFactor)
				{
					this.velocity.x = this.properties.getMaxSpeed() * speedFactor * Math.signum(this.velocity.x);
				}
			}

			if (this.timeUntilJump < 0)
			{
				this.resetVelocities();
				this.velocity.y = -0.9f;
				this.jumping = true;

				this.timeUntilJump = JUMP_WAIT_TIME;
			}

			if (this.lastJumping && !this.jumping && !this.mainSpikes.isEmpty())
			{
				for (EntityMiniIceSpike spike : this.miniSpikes)
				{
					spike.fall();
				}

				this.nextSpike.fall();

				int nextIndex = rng.nextInt(this.mainSpikes.size());

				if (this.mainSpikes.get(nextIndex) == this.nextSpike)
				{
					nextIndex++;

					if (nextIndex >= this.mainSpikes.size())
					{
						nextIndex = 0;
					}
				}

				this.nextSpike = this.mainSpikes.get(nextIndex);

				if (this.tileBelow != null)
				{

				}
				else
				{
					Debug.warn("EntityTimeScaleBoss stopped jumping but tilebelow is null. (" + this + ")");
				}
			}
		}

		this.lastJumping = this.jumping;
	}

	public void getAllLeft(Tile tile, Set<Tile> tiles)
	{
		tiles.add(tile);

		if (tile.getHitbox().isConnectedLeft() && tile.getLeftConnectedTile() != null)
		{
			this.getAllLeft(tile.getLeftConnectedTile(), tiles);
		}
	}

	public void getAllRight(Tile tile, Set<Tile> tiles)
	{
		tiles.add(tile);

		if (tile.getHitbox().isConnectedRight() && tile.getRightConnectedTile() != null)
		{
			this.getAllRight(tile.getRightConnectedTile(), tiles);
		}
	}

	@Override
	public void onDeathUpdate(float delta, Random rng)
	{
		super.onDeathUpdate(delta, rng);
	}

	@Override
	public void draw(Batch batch)
	{
		this.draw(batch, this.dimension);
	}

	@Override
	public void draw(Batch batch, Dimension dimension)
	{
		super.draw(batch, dimension);

		if (this.alive && this.active)
		{
			Renderer.setFont(Fonts.getSmallFont());

			String drawString = "";

			for (int i = 0; i < this.properties.getMaxHealth() - this.health; i++)
			{
				drawString += "X";
			}

			for (int i = 0; i < this.health; i++)
			{
				drawString += "O";
			}

			Renderer.drawString(drawString, this.customModifyDrawColor(this.getDrawColor(this.dimension, ColorHelper.buffer)), this.getCenterX() - Renderer.getWidth(drawString) / 2.0f, this.getCenterY() - 58, 1.0f, batch);
		}
	}

	@Override
	protected boolean canInteractWith(Entity entity)
	{
		return super.canInteractWith(entity) && entity.getType() != EntityType.GOOMBA && !(entity instanceof EntityMiniIceSpike);
	}

	@Override
	protected boolean shouldBounceOnGround()
	{
		return false;
	}

	@Override
	protected EntityProperties getEntityProperty()
	{
		return EntityProperties.TIME_SCALE_BOSS;
	}
}

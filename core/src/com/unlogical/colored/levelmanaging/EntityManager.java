package com.unlogical.colored.levelmanaging;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.profiling.Profiling;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.Renderer;

public class EntityManager
{
	public static void updateEntities(Level level, Dimension dimension, float delta, float dimensionDelta)
	{
		Profiling.startSection("entity");

		for (Entity entity : level.getEntities())
		{
			if (entity.getDimension() == dimension && (!level.isUpdatingOnlyCycleObjects() || entity.isCycleObject()))
			{
				if (entity.isTimeEffected() && !entity.isShared() && !(entity.getHoldingEntity() != null && (!entity.getHoldingEntity().isTimeEffected() || entity.isShared())))
				{
					entity.update(dimensionDelta);
				}
				else
				{
					entity.update(delta);
				}
			}
		}

		Profiling.endSection("entity");
	}

	public static void renderEntities(Level level, Dimension dimension, boolean renderShared, Batch batch)
	{
		for (Entity entity : level.getEntities())
		{
			if (entity.shouldRenderInBatch() && (entity.getDimension() == dimension && !entity.isShared() && !renderShared || renderShared && entity.isShared()))
			{
				renderEntity(level, entity, batch);
			}
		}
	}

	public static void renderEntity(Level level, Entity entity, Batch batch)
	{
		Profiling.startSection("entity");

		entity.draw(batch, entity.getDimension());

		if (level.shouldShowHitboxes())
		{
			ShapeRenderer sr = Renderer.useShapeRenderer();

			sr.setColor(entity.getDimension() == level.getForegroundDimension() ? Color.BLUE : ColorHelper.applyAlphaAndCopy(Color.BLUE, 0.7f));

			sr.polygon(entity.getHitbox().getPoints());

			if (LevelManager.isDevMode())
			{
				float[] points = entity.getHitbox().getPoints();

				sr.setColor(ColorHelper.applyAlphaAndCopy(Color.CYAN, entity.getDimension() == level.getForegroundDimension() ? 0.7f : 0.3f));

				float width = 4f;

				if (entity.isBlockedAbove())
				{
					sr.rectLine(points[0], points[1], points[2], points[3], width);
				}

				if (entity.isBlockedBelow())
				{
					sr.rectLine(points[4], points[5], points[6], points[7], width);
				}

				if (entity.isBlockedLeft())
				{
					sr.rectLine(points[0], points[1], points[6], points[7], width);
				}

				if (entity.isBlockedRight())
				{
					sr.rectLine(points[2], points[3], points[4], points[5], width);
				}

				sr.setColor(Color.RED);

				int cornerSize = 6;

				if (entity.getHitbox().isLeftTop())
				{
					sr.circle(points[0] - cornerSize / 2, points[1] - cornerSize / 2, cornerSize);
				}

				if (entity.getHitbox().isRightTop())
				{
					sr.circle(points[2] - cornerSize / 2, points[3] - cornerSize / 2, cornerSize);
				}

				if (entity.getHitbox().isRightBottom())
				{
					sr.circle(points[4] - cornerSize / 2, points[5] - cornerSize / 2, cornerSize);
				}

				if (entity.getHitbox().isLeftBottom())
				{
					sr.circle(points[6] - cornerSize / 2, points[7] - cornerSize / 2, cornerSize);
				}
			}
		}

		Profiling.endSection("entity");
	}
}
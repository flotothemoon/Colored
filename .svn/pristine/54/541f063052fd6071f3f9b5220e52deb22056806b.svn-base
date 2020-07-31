package com.unlogical.colored.levelmanaging;

import java.io.IOException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.resources.image.Images;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.Transitions;

public final class GUIManager
{
	private static final int ICON_DISPLAY_TIME = 2500;
	private static final int ICON_FADE_OUT_TIME = 500;
	private static final int ICON_FADE_IN_TIME = 800;

	private static final String RESUME_KEY_HINT = "resumeKeyHint";
	private static final String RESUME_HINT = "resumeHint";
	private static final String PREVIOUS_KEY_HINT = "previousKeyHint";
	private static final String PREVIOUS_HINT = "previousHint";
	private static final String NEXT_KEY_HINT = "nextKeyHint";
	private static final String NEXT_HINT = "nextHint";

	private static Level level;
	private static float iconDisplayTime;
	private static int selectionTime;

	public static void init() throws IOException
	{
		Images.registerAndLoad(RESUME_KEY_HINT, FilePaths.GUI + "/resumeKey.png");
		Images.registerAndLoad(RESUME_HINT, FilePaths.GUI + "/continue.png");
		Images.registerAndLoad(PREVIOUS_KEY_HINT, FilePaths.GUI + "/previousKey.png");
		Images.registerAndLoad(NEXT_KEY_HINT, FilePaths.GUI + "/nextKey.png");
		Images.registerAndLoad(NEXT_HINT, FilePaths.GUI + "/next.png");
		Images.registerAndLoad(PREVIOUS_HINT, FilePaths.GUI + "/previous.png");
	}

	public static void initLevel(Level level, boolean showIcon)
	{
		GUIManager.level = level;

		if (showIcon)
		{
			try
			{
				Images.registerAndLoadSilently("icons/" + level.getID(), FilePaths.ICONS + "/" + level.getID().replace('-', '_'));
			}
			catch (Exception e)
			{
			}

			if (Images.isLoaded("icons/" + level.getID()))
			{
				iconDisplayTime = ICON_DISPLAY_TIME;
			}
			else
			{
				iconDisplayTime = -1;

				Debug.warn("Couldn't load level icon for " + level.getID() + ".");
			}
		}
	}

	public static void update(int delta)
	{
		if (LevelManager.isInCheckpointSelectionMode())
		{
			selectionTime += delta;

			if (selectionTime > 5000)
			{
				selectionTime = 5000;
			}
		}
		else if (selectionTime > 0)
		{
			selectionTime -= delta * 8;
		}

		if (iconDisplayTime > 0)
		{
			iconDisplayTime -= delta;
		}
	}

	public static void render(Batch batch)
	{
		level.getControllingPlayer().drawOverlay(batch);

		if (level.getControllingPlayer().hasMirror())
		{
			((EntityPlayer) level.getControllingPlayer().getMirroredEntity()).drawOverlay(batch);
		}

		if (selectionTime > 0)
		{
			Renderer.useSpriteBatch();

			float scale = 1.5f;

			int curX = 50;
			int bottomYOffset = 60;
			int topY = (int) (GameLauncher.getHeight() - Images.getHandler(RESUME_HINT).getHeight() * scale - 50 * scale);
			int bottomY = GameLauncher.getHeight() - bottomYOffset;

			float alpha = (float) (Math.sin(System.currentTimeMillis() / 200.0) + 1.0f) / 8.0f + 0.75f;

			if (selectionTime <= 1000)
			{
				alpha *= Transitions.getFactor(selectionTime);
			}

			Color color = ColorHelper.multiplyAlphaAndCopy(Color.WHITE, alpha);

			curX += Images.getHandler(RESUME_HINT).getWidth() / 2.0f * scale;

			Images.draw(RESUME_HINT, curX, topY, scale, 0.0f, color, batch);
			Images.draw(RESUME_KEY_HINT, curX, bottomY, scale, 0.0f, color, batch);

			curX += Images.getHandler(PREVIOUS_HINT).getWidth() * scale;

			color = ColorHelper.applyAlphaAndCopy(color, alpha * (LevelManager.isPreviousCheckpointAvailable() ? 1.0f : 0.4f));

			Images.draw(PREVIOUS_HINT, curX, topY, scale, 0.0f, color, batch);
			Images.draw(PREVIOUS_KEY_HINT, curX, bottomY, scale, 0.0f, color, batch);

			curX += Images.getHandler(NEXT_HINT).getWidth() * scale;

			color = ColorHelper.applyAlphaAndCopy(color, alpha * (LevelManager.isNextCheckpointAvailable() ? 1.0f : 0.4f));

			Images.draw(NEXT_HINT, curX, topY, scale, 0.0f, color, batch);
			Images.draw(NEXT_KEY_HINT, curX, bottomY, scale, 0.0f, color, batch);
		}

		if (iconDisplayTime > 0 && Images.isLoaded("icons/" + level.getID()))
		{
			float factor = 1.0f;

			if (iconDisplayTime < ICON_FADE_OUT_TIME)
			{
				factor = iconDisplayTime / ICON_FADE_OUT_TIME;
			}
			else if (iconDisplayTime > ICON_DISPLAY_TIME - ICON_FADE_IN_TIME)
			{
				factor = 1.0f - iconDisplayTime / (ICON_DISPLAY_TIME - ICON_FADE_IN_TIME);

				if (factor < 0)
				{
					factor = 1.0f;
				}
			}

			factor = Transitions.getFactor(factor);

			Renderer.useSpriteBatch();
			Images.draw("icons/" + level.getID(), GameLauncher.getWidth() / 2.0f, GameLauncher.getHeight() / 2.0f, 0.75f + factor / 4.0f, 0.0f, ColorHelper.multiplyAlphaAndCopy(Color.WHITE, factor), batch);
		}
	}
}

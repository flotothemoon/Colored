package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.audio.MusicManager;
import com.unlogical.colored.entity.player.EntityPlayer;
import com.unlogical.colored.gui.menu.MenuHandler;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.levelmanaging.MapManager;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.particle.ParticleSystem;
import com.unlogical.colored.profiling.Profiling;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public class DebugPanel extends GUIPanel
{
	public DebugPanel(float xOffset, float yOffset, float width, float height)
	{
		super("Debug", xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);
		this.setHeight((Fonts.getSmallFont().getLineHeight() + 5) * 11);
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		int y = (int) (this.yOffset + 5);
		int x = (int) (this.xOffset + 5);

		Renderer.setFont(Fonts.getSmallFont());

		int offset = Renderer.getLineHeight() + 5;
		EntityPlayer player = LevelManager.getLevel().getControllingPlayer();

		Renderer.useSpriteBatch();

		Renderer.setFont(Fonts.getSmallBoldFont());
		Renderer.drawString("Level: " + GameLauncher.getLevelContext().getName() + " (" + GameLauncher.getLevelContext().getID() + ")" + " - " + Integer.toHexString(GameLauncher.getLevelContext().hashCode()), x, y, 1.0f, batch);
		y += offset;

		Renderer.setFont(Fonts.getSmallFont());

		Renderer.drawString("LevelType: " + player.getLevel().getType(), x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString("Checkpoint: " + (LevelManager.getCurrentCheckpoint() == null ? "Not set" : "Set") + " (total: " + LevelManager.getCheckpoints().size() + ")", x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString("FPS: " + GameLauncher.getFPS() + " (Lock: " + GameLauncher.getTargetFPS() + ", vsync:" + GameLauncher.isVSync() + ")", x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString("Memory Usage: " + GameLauncher.getUsedMemory() + " (" + GameLauncher.getUsedMemory() * 100 / GameLauncher.getAllocatedMemory() + "%) / " + GameLauncher.getAllocatedMemory() + "MB", x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString("Dimension: " + player.getDimension(), x, y, 1.0f, batch);
		y += offset;

		if (player.getLevel().getType() == LevelType.TIME_TRAVEL || player.getLevel().getType() == LevelType.FROZEN)
		{
			Renderer.drawString("CurrentFrame: " + player.getLevel().timeStepHandler.currentFrameStart + " (" + player.getLevel().timeStepHandler.currentFrameBeforeStart + ") - " + player.getLevel().timeStepHandler.currentFrameEnd, x, y, 1.0f, batch);
			y += offset;
		}

		Renderer.drawString((GameLauncher.isFullscreenMode() ? "F" : "W") + ": " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight() + " (" + Math.round((float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight() * 100) / 100.0f + ") - " + GameLauncher.getWidth() + "x" + GameLauncher.getHeight() + " (" + Math.round(GameLauncher.TARGET_RATIO * 100) / 100.0f + ")", x, y, 1.0f, batch);
		y += offset;

		if (player.isSuffocating() || player.hasMirror() && player.getMirroredEntity().isSuffocating())
		{
			if (player.isSuffocating())
			{
				Renderer.drawString("Suffocating!", x, y, 1.0f, batch);
			}
			else
			{
				Renderer.drawString("Can't toggle!", x, y, 1.0f, batch);
			}

			y += offset;
		}

		Renderer.drawString("CameraOffset: " + LevelManager.getLevel().getCameraOffset().x + " | " + LevelManager.getLevel().getCameraOffset().y, x, y, 1.0f, batch);
		y += offset;

		ParticleSystem ps = GameLauncher.getLevelContext().getParticleSystem();

		Renderer.drawString("E: " + LevelManager.getLevel().getEntities().size() + " | T: " + GameLauncher.getLevelContext().getTiles().size() + " | W: " + GameLauncher.getLevelContext().getWrappers().size() + " | E-P: " + ps.getEmitterCount() + " - " + ps.getActiveParticleCount() + " / " + ps.getPooledParticleCount(), x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString("Menu: E: " + MenuHandler.getActiveMenuParticleEmitters() + " / " + MenuHandler.getMenuParticleEmitters() + " | " + " P: " + MenuHandler.getMenuRealParticleCount() + " / " + MenuHandler.getMenuRealParticleCount(), x, y, batch);
		y += offset;

		Renderer.drawString("Lighting: " + (ShaderHandler.isEnabled() ? "enabled" : "disabled") + ", lights: " + ShaderHandler.getLights().size(), x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString("Profiling: " + (Profiling.enabled ? "enabled" : "disabled"), x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString(String.format("C: %d DC: %d SS: %d TB: %d V: %2d", GLProfiler.calls, GLProfiler.drawCalls, GLProfiler.shaderSwitches, GLProfiler.textureBindings, (int) GLProfiler.vertexCount.total), x, y, 1.0f, batch);
		y += offset;

		Renderer.drawString("M: " + MusicManager.getDebugString(), x, y, 1.0f, batch);
		y += offset;

		if (MapManager.isReSimulating())
		{
			Renderer.drawString("ReSimulating?", x, y, 1.0f, batch);
			y += offset;
		}

		if (y - this.yOffset + 20 != this.height)
		{
			this.setHeight(y - this.yOffset + 20);
		}

		GLProfiler.reset();

		if (GameLauncher.getSanityPanel().isActive() && GameLauncher.getSanityPanel().isShown())
		{
			ShapeRenderer sr = Renderer.useShapeRenderer();

			int centerRadius = Gdx.graphics.getHeight() / 2;

			sr.set(ShapeType.Filled);
			sr.setColor(ColorHelper.applyAlphaAndCopy(Color.RED, 0.5f));
			sr.circle(LevelManager.getLevel().getCenterX(), LevelManager.getLevel().getCenterY(), centerRadius);
		}
	}

	@Override
	public void setActive(boolean active)
	{
		if (active ^ GLProfiler.isEnabled())
		{
			if (active)
			{
				GLProfiler.enable();
				GLProfiler.listener = new GLErrorListener()
				{
					@Override
					public void onError(int error)
					{

					}
				};
			}
			else
			{
				GLProfiler.disable();
			}
		}

		super.setActive(active);
	}
}

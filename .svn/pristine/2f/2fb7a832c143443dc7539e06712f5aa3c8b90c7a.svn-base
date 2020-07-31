package com.unlogical.colored.gui.panel;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.levelmanaging.MapManager;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.Renderer;

public class SanityPanel extends GUIPanel
{
	private static boolean runningCheck;

	private int unitWidth = GameLauncher.TARGET_WIDTH;
	private int unitHeight = GameLauncher.TARGET_HEIGHT;

	private int perLevelCheckTime = 50;
	private int levelTime = 0;
	private int totalTime;
	private boolean finishedCheck;
	private BufferedImage entireLevelImage;
	private int currentLevelImageX;
	private int currentLevelImageY;
	private int currentLevelImageWidth;
	private int currentLevelImageHeight;

	private GameStatus beforeStatus;
	private String beforeLevelID;

	private Button startUpdateRenderTestButton;
	private String currentLevel;
	private Level level;
	private List<String> levelIDs = new ArrayList<String>();

	private List<LoggedError> loggedErrors = new ArrayList<LoggedError>();

	public SanityPanel(float xOffset, float yOffset, float width, float height)
	{
		super("Start Sanity Check", xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 30;

		this.startUpdateRenderTestButton = new Button("Snapshot all", (int) (xOffset + width / 3.0f), (int) (yOffset + height - lineHeight - 25), (int) (width / 3.0f), lineHeight, false)
		{
			@Override
			public void onClick()
			{
				if (SanityPanel.this.isShown() && SanityPanel.this.isActive())
				{
					SanityPanel.this.startTest(500);
				}
			}
		};
		this.startUpdateRenderTestButton.setActive(true);

		this.setHeight(height);
	}

	private void startTest(int perLevelTime)
	{
		if (!runningCheck)
		{
			this.perLevelCheckTime = perLevelTime;

			this.levelTime = 0;
			this.totalTime = 0;

			this.levelIDs.clear();
			this.levelIDs.addAll(Level.getAllLevelIDs());

			Collections.sort(this.levelIDs);

			if (GameLauncher.getStatus() == GameStatus.MAP_EDITOR)
			{
				MapEditor.saveItAll();
			}

			this.beforeStatus = GameLauncher.getStatus();
			this.beforeLevelID = GameLauncher.getLevelContext() != null ? GameLauncher.getLevelContext().getID() : null;

			GameLauncher.setStatus(GameStatus.IN_LEVEL);

			LevelManager.initLevel(Level.getMapPathByID(this.levelIDs.get(0)), 0, false, true);
			this.level = LevelManager.getLevel();

			this.initImageForLevel(this.level);

			this.currentLevel = LevelManager.getLevel().getID();

			this.startUpdateRenderTestButton.setActive(false);

			this.setTitle("Running Sanity Check...");

			this.loggedErrors.clear();

			runningCheck = true;
		}
	}

	public void onError(Exception e)
	{
		this.loggedErrors.add(new LoggedError(e, this.currentLevel));

		this.totalTime = (this.levelIDs.indexOf(this.currentLevel) + 1) * this.perLevelCheckTime;

		this.goToNextLevel();
	}

	@Override
	protected void customUpdate(int delta)
	{
		if (this.isShown() && this.isActive())
		{
			this.startUpdateRenderTestButton.update(delta);

			if (runningCheck)
			{
				this.levelTime += delta;
				this.totalTime += delta;

				if (this.levelTime >= this.perLevelCheckTime)
				{
					this.levelTime = 0;
				}

				MapManager.setFreeCamera();

				float absCenterX = this.level.getCenterX() - this.level.getCameraOffset().x;
				float absCenterY = this.level.getCenterY() - this.level.getCameraOffset().y;
				// float absCamMinX = this.level.getAbsLeftLimit().getCenterX()
				// - this.level.getCameraOffset().x;
				// float absCamMinY = this.level.getAbsTopLimit().getCenterY() -
				// this.level.getCameraOffset().y;
				// float widthDiff = this.currentLevelImageWidth -
				// this.level.getMapWidth();
				// float heightDiff = this.currentLevelImageHeight -
				// this.level.getMapHeight();

				System.out.println("center " + absCenterX + " " + absCenterY);
				// System.out.println("mapsize: " + this.level.getMapWidth() + "
				// " + this.level.getMapHeight());
				// System.out.println("imagesize: " +
				// this.currentLevelImageWidth + " " +
				// this.currentLevelImageHeight);
				// System.out.println("topleft: " + absCamMinX + " " +
				// absCamMinY);

				// float camXOffset = absCenterX;
				float camYOffset = absCenterY + this.currentLevelImageHeight / 2 - this.currentLevelImageY - this.unitHeight;

				// System.out.println(" -> " + camXOffset + " " + camYOffset);
				MapManager.setCameraOffset(this.level.getCenterX() - this.level.getCameraOffset().x, camYOffset);
			}
		}
	}

	private void initImageForLevel(Level level)
	{
		this.level = LevelManager.getLevel();

		this.unitWidth = Gdx.graphics.getWidth();
		this.unitHeight = Gdx.graphics.getHeight();

		int entireLevelImageWidth = (int) Math.ceil(this.level.getMapWidth() / (float) this.unitWidth) * this.unitWidth;
		int entireLevelImageHeight = (int) Math.ceil(this.level.getMapHeight() / (float) this.unitHeight) * this.unitHeight;

		this.entireLevelImage = new BufferedImage(entireLevelImageWidth, entireLevelImageHeight, BufferedImage.TYPE_INT_ARGB);

		this.currentLevelImageX = 0;
		this.currentLevelImageY = 0;
		this.currentLevelImageWidth = entireLevelImageWidth;
		this.currentLevelImageHeight = entireLevelImageHeight;

		Debug.log("Creating entire level image for level " + this.currentLevel + ", " + this.currentLevelImageWidth + "x" + this.currentLevelImageHeight);
	}

	private void goToNextLevel()
	{
		this.levelTime = 0;

		if (this.levelIDs.indexOf(this.currentLevel) != this.levelIDs.size() - 1)
		{
			try
			{
				this.currentLevel = this.levelIDs.get(this.levelIDs.indexOf(this.currentLevel) + 1);

				LevelManager.initLevel(Level.getMapPathByID(this.currentLevel), 0, false, false);

				this.initImageForLevel(this.level);
			}
			catch (Exception e)
			{
				this.onError(e);
			}
		}
		else
		{
			runningCheck = false;
			this.finishedCheck = true;

			this.setTitle("Sanity Check Finished");
			this.startUpdateRenderTestButton.setActive(true);

			MapManager.toggleCameraMode();
			GameLauncher.setStatus(this.beforeStatus);

			if (this.beforeLevelID != null)
			{
				if (this.beforeStatus == GameStatus.IN_LEVEL || this.beforeStatus == GameStatus.PAUSE_MENU)
				{
					GameLauncher.setStatus(GameStatus.IN_LEVEL);
					LevelManager.initLevel(Level.getMapPathByID(this.beforeLevelID), false);
				}
				else
				{
					GameLauncher.setStatus(GameStatus.MAP_EDITOR);
					MapEditor.initLevel(this.beforeLevelID, Level.getMapPathByID(this.beforeLevelID));
				}
			}
		}
	}

	public static Pixmap getScreenshot(int x, int y, int w, int h, boolean flipY)
	{
		Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

		final Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
		ByteBuffer pixels = pixmap.getPixels();
		Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);

		final int numBytes = w * h * 4;
		byte[] lines = new byte[numBytes];
		if (flipY)
		{
			final int numBytesPerLine = w * 4;
			for (int i = 0; i < h; i++)
			{
				pixels.position((h - i - 1) * numBytesPerLine);
				pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
			}
			pixels.clear();
			pixels.put(lines);
		}
		else
		{
			pixels.clear();
			pixels.get(lines);
		}

		return pixmap;
	}

	public static int[] pixmapToIntArray(Pixmap pixmap)
	{
		int w = pixmap.getWidth();
		int h = pixmap.getHeight();

		int dest = 0;
		int[] raw = new int[w * h];
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				int rgba = pixmap.getPixel(x, y);
				raw[dest++] = 0xFF000000 | rgba >> 8;
			}
		}
		return raw;
	}

	public static void takeAndStoreScreenshot()
	{
		takeAndStoreScreenshot(new SimpleDateFormat("dd_MM_yy_HH_mm_ss").format(Calendar.getInstance().getTime()));
	}

	public static void takeAndStoreScreenshot(String name)
	{
		try
		{
			Pixmap pixmap = getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

			BufferedImage image = new BufferedImage(pixmap.getWidth(), pixmap.getHeight(), BufferedImage.TYPE_INT_ARGB);

			image.setRGB(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), pixmapToIntArray(pixmap), 0, pixmap.getWidth());

			ImageIO.write(image, "PNG", Gdx.files.local("screenshots/" + name + ".png").write(false));
		}
		catch (IOException e)
		{
			Debug.warn("Error while taking screenshot: " + e, e);
		}
	}

	public void takeScreenshot()
	{
		Pixmap pixmap = getScreenshot(0, 0, this.unitWidth, this.unitHeight, true);

		System.out.println(this.level.getCameraOffset().x + " " + this.level.getCameraOffset().y + " at " + this.currentLevelImageX + " " + this.currentLevelImageY);

		this.entireLevelImage.setRGB(this.currentLevelImageX, this.currentLevelImageY, this.unitWidth, this.unitHeight, pixmapToIntArray(pixmap), 0, this.unitWidth);

		this.currentLevelImageX += this.unitWidth;

		if (this.currentLevelImageX >= this.currentLevelImageWidth)
		{
			this.currentLevelImageX = 0;
			this.currentLevelImageY += this.unitHeight;
		}

		if (this.currentLevelImageY >= this.currentLevelImageHeight)
		{
			try
			{
				ImageIO.write(this.entireLevelImage, "PNG", Gdx.files.local("screenshots/" + this.currentLevel + ".png").write(false));
			}
			catch (IOException e)
			{
				Debug.warn("Error while writing entire level image: " + e, e);
			}

			this.goToNextLevel();
		}
	}

	@Override
	protected void preDraw(Batch batch)
	{
		if (runningCheck)
		{
			if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_RIGHT))
			{
				this.takeScreenshot();
			}
		}
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		int y = (int) (this.yOffset + 5);
		int x = (int) (this.xOffset + 5);

		Renderer.setFont(Fonts.getSmallBoldFont());

		int offset = Renderer.getLineHeight() + 5;

		if (runningCheck)
		{
			Renderer.useSpriteBatch();

			Renderer.drawString("Level Info: " + GameLauncher.getLevelContext().getName() + " (" + this.currentLevel + ")", x, y, 1.0f, batch);
			y += offset;

			Renderer.setFont(Fonts.getSmallFont());

			Renderer.drawString("Check Progress: " + (this.levelIDs.indexOf(this.currentLevel) + 1) + " / " + this.levelIDs.size() + " (" + (int) (100 * (float) this.totalTime / ((float) this.levelIDs.size() * this.perLevelCheckTime)) + "%)", x, y, 1.0f, batch);
			y += offset;

			Renderer.drawString("Time Remaining: " + Math.round(((float) (this.levelIDs.size() * this.perLevelCheckTime) - this.totalTime) / 100.0f) / 10.0f + " seconds ", x, y, 1.0f, batch);
			y += offset;

			ShapeRenderer sr = Renderer.useShapeRenderer();

			sr.set(ShapeType.Filled);
			sr.setColor(ColorHelper.applyAlphaAndCopy(Color.BLUE, 0.7f));
			sr.rect(this.xOffset + 1, this.yOffset + this.height - 100, this.width * Math.min(1.0f, this.totalTime / ((float) this.levelIDs.size() * this.perLevelCheckTime)) - 2, 40);

			sr.set(ShapeType.Line);
			sr.setColor(ColorHelper.applyAlphaAndCopy(Color.WHITE, 0.7f));
			sr.rect(this.xOffset + 1, this.yOffset + this.height - 100, this.width - 2, 40);
		}
		else if (this.finishedCheck)
		{
			Color color = this.loggedErrors.isEmpty() ? Color.GREEN : Color.RED;

			Renderer.useSpriteBatch();

			Renderer.setFont(Fonts.getSmallBoldFont());
			Renderer.drawString("-- Summary --", color, x + this.width / 2 - Renderer.getWidth("-- Summary --") / 2, y, 1.0f, batch);
			y += offset;

			Renderer.setFont(Fonts.getSmallFont());

			Renderer.drawString("Time Elapsed: " + (float) this.totalTime / 1000 + " seconds", color, x, y, 1.0f, batch);
			y += offset;

			Renderer.drawString("Levels Checked: " + this.levelIDs.size(), color, x, y, 1.0f, batch);
			y += offset;

			Renderer.drawString("Levels Passed: " + (this.levelIDs.size() - this.loggedErrors.size()) + " (" + Math.round(100 - this.loggedErrors.size() / (float) this.levelIDs.size() * 100) + "%)", color, x, y, 1.0f, batch);
			y += offset;

			Renderer.drawString("Levels Failed: " + this.loggedErrors.size() + " (" + Math.round(this.loggedErrors.size() / (float) this.levelIDs.size() * 100) + "%)", color, x, y, 1.0f, batch);
			y += offset;

			// for (LoggedError error : this.loggedErrors)
			// {
			// Renderer.drawString(" " + error.level + ": " + error.exception +
			// ": " + error.exception.getMessage(), x, y, 1.0f, batch);
			// y += offset;
			//
			// Renderer.drawString(" at " +
			// error.exception.getStackTrace()[0].getFileName().substring(0,
			// error.exception.getStackTrace()[0].getFileName().lastIndexOf('.'))
			// + "." + error.exception.getStackTrace()[0].getMethodName() + ":"
			// + error.exception.getStackTrace()[0].getLineNumber(), x, y, 1.0f,
			// batch);
			// y += offset;
			//
			// }
		}

		this.startUpdateRenderTestButton.render(alphaFactor, batch);
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		this.startUpdateRenderTestButton.adjust(xChange, yChange);
	}

	public static boolean isRunningCheck()
	{
		return runningCheck;
	}

	@SuppressWarnings("unused")
	private static class LoggedError
	{
		private Exception exception;
		private String level;

		private LoggedError(Exception e, String level)
		{
			this.exception = e;
			this.level = level;
		}
	}
}

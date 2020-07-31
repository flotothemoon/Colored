package com.unlogical.colored.gui.panel;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unlogical.colored.collision.Line;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.projectile.EntityProjectile;
import com.unlogical.colored.gui.GUIItem;
import com.unlogical.colored.gui.InputChangeListener;
import com.unlogical.colored.gui.Slider;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.gui.selection.GUIItemSelection;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.cannon.ICannonBall;
import com.unlogical.colored.terrain.tile.cannon.TileCannon;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

public class CannonPanel extends GUIPanel
{
	private TileCannon cannon;

	private Button typeButton;
	private Button closeButton;

	private GUIItemSelection itemSelection;
	private UserInputLine interval;
	private UserInputLine minCount;
	private Slider<TileCannon> maxSpeed;
	
	private float lastRotation;
	private float lastCannonX;
	private float lastCannonY;

	private ArrayList<float[]> linePoints = new ArrayList<float[]>();
	private ArrayList<float[]> otherLinePoints = new ArrayList<float[]>();

	private Color pointsColor = new Color(1.0f, 0.1f, 0.0f, 0.5f);
	private Color otherPointsColor = new Color(0.0f, 0.1f, 1.0f, 0.5f);

	public CannonPanel(GUIItem[] items, String title, float xOffset, float yOffset, float width, float height)
	{
		super(title, xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 30;
		int inputWidth = (int) (width / 2) + 15;
		int currentOffset = (int) (yOffset + 10);
		int inputX = (int) (xOffset + width / 2) - 20;

		typeButton = new Button("", (int) xOffset, currentOffset, (int) width, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				cannon.setBehaviour(cannon.getBehaviour().next());
				typeButton.setText(cannon.getBehaviour().name());

				interval.setAllowUserInput(cannon.getBehaviour() == TileCannon.TileCannonBehaviour.INTERVAL);
				minCount.setAllowUserInput(cannon.getBehaviour() == TileCannon.TileCannonBehaviour.MIN_COUNT);

				updateLine();
			}
		};

		currentOffset += typeButton.getHeight() + 30;

		itemSelection = new GUIItemSelection("Cannon Ball", items, 5, 2, (int) xOffset + 5, currentOffset)
		{
			@Override
			public void itemSelected(int number, String name, String metadata)
			{
				cannon.setBallType(metadata);

				updateLine();
			}
		};
		itemSelection.setActive(true);
		itemSelection.setMoveable(false);

		currentOffset += itemSelection.getHeight() + 40;

		maxSpeed = new Slider<TileCannon>(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, cannon)
		{
			@Override
			protected void onFactorChanged(float selectedRange, TileCannon reference)
			{
				cannon.setShootSpeed(selectedRange);

				updateLine();
			}
		};
		maxSpeed.setActive(true);

		currentOffset += maxSpeed.getHeight() + 10;

		interval = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		interval.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				try
				{
					cannon.setInterval(Integer.parseInt(interval.getInput()));
				}
				catch (Exception e)
				{
				}
			}
		});
		interval.setOnlyNumeric(true);
		interval.setActive(true);

		currentOffset += interval.getHeight() + 10;

		minCount = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		minCount.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				try
				{
					cannon.setMinCount(Integer.parseInt(minCount.getInput()));
				}
				catch (Exception e)
				{
				}
			}
		});
		minCount.setOnlyNumeric(true);
		minCount.setActive(true);

		currentOffset += minCount.getHeight() + 10;

		closeButton = new Button("Close", (int) xOffset, currentOffset, (int) width, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				CannonPanel.this.setActive(false);
			}
		};
		closeButton.setActive(true);

		currentOffset += closeButton.getHeight();

		this.setHeight(currentOffset - yOffset + 30);
	}

	public void updateLine()
	{
		if (cannon.hasMirror())
		{
			TileCannon mirror = (TileCannon) cannon.getMirroredTile();

			mirror.setBallType(cannon.getBallType());
			mirror.setBehaviour(cannon.getBehaviour());
			mirror.setInterval(cannon.getInterval());
			mirror.setShootSpeed(cannon.getShootSpeed());
		}

		cannon.validateCannonBall();

		if (cannon.isValidCannonBall())
		{
			int lookAheadTime = 5000;
			int delta = 16;

			ICannonBall ball = cannon.shootCannonBall();

			linePoints.clear();
			otherLinePoints.clear();

			linePoints.add(new float[] { ball.getHitbox().getCenterX(), ball.getHitbox().getCenterY() });

			if (ball.hasMirror())
			{
				otherLinePoints.add(new float[] { ball.getMirroredBall().getHitbox().getCenterX(), ball.getMirroredBall().getHitbox().getCenterY() });
			}

			while (lookAheadTime >= 0 && !ball.isOutOfMap() && !ball.isDead())
			{
				ball.update(delta);
				lookAheadTime -= delta;

				linePoints.add(new float[] { ball.getHitbox().getCenterX(), ball.getHitbox().getCenterY() });
				
				if (ball.hasMirror() && !ball.getMirroredBall().isDead() && !ball.getMirroredBall().isOutOfMap())
				{
					ball.getMirroredBall().update(delta);
					
					if (ball.getMirroredBall() != null)
					{
						otherLinePoints.add(new float[] { ball.getMirroredBall().getHitbox().getCenterX(), ball.getMirroredBall().getHitbox().getCenterY() });
					}
				}
			}

			ball.removeMirror();

			if (ball instanceof Entity)
			{
				cannon.getLevel().removeEntity((Entity) ball);
			}
			else
			{
				cannon.getLevel().removeTile((Tile) ball);
			}

			cannon.getLevel().transferBuffers();
		}
	}

	public void init(TileCannon cannon)
	{
		this.cannon = cannon;

		this.lastRotation = cannon.getRotation();
		this.lastCannonX = cannon.getPosition().x;
		this.lastCannonY = cannon.getPosition().y;

		this.typeButton.setText(cannon.getBehaviour().name());
		this.interval.setInput(cannon.getInterval() + "");
		this.interval.setAllowUserInput(this.cannon.getBehaviour() == TileCannon.TileCannonBehaviour.INTERVAL);
		this.minCount.setInput(cannon.getMinCount() + "");
		this.minCount.setAllowUserInput(this.cannon.getBehaviour() == TileCannon.TileCannonBehaviour.MIN_COUNT);
		this.maxSpeed.setSelectedRange(cannon.getShootSpeed());
		this.itemSelection.select(this.itemSelection.getItemByMetadata(this.cannon.getBallType()));

		this.linePoints.clear();

		updateLine();
	}

	@Override
	protected void customUpdate(int delta)
	{
		if (cannon.isRemoved())
		{
			cannon = null;
			setActive(false);

			return;
		}

		if (lastRotation != cannon.getRotation() || lastCannonX != cannon.getPosition().x || lastCannonY != cannon.getPosition().y)
		{
			updateLine();

			lastRotation = cannon.getRotation();
			lastCannonX = cannon.getPosition().x;
			lastCannonY = cannon.getPosition().y;
		}

		typeButton.update(delta);
		itemSelection.update(delta);
		maxSpeed.update(delta);
		interval.update(delta);
		closeButton.update(delta);
		minCount.update(delta);
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		typeButton.render(alphaFactor, batch);
		itemSelection.render(alphaFactor, batch);
		maxSpeed.render(batch);
		interval.render(alphaFactor, batch);
		closeButton.render(alphaFactor, batch);
		minCount.render(alphaFactor, batch);

		drawPoints(pointsColor, 3, linePoints, batch);
		drawPoints(otherPointsColor, 3, otherLinePoints, batch);

		ShaderHandler.disableLighting();

		Renderer.useSpriteBatch();
		
		Renderer.drawString("Interval", xOffset + 5, interval.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("MinCount", xOffset + 5, minCount.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		
		ShapeRenderer sr = Renderer.useShapeRenderer();
		sr.setColor(Color.RED);

		for (Line line : EntityProjectile.lines)
		{
			sr.line(line.getStart(), line.getEnd());
		}
		
		ShaderHandler.enableLighting();
	}

	private void drawPoints(Color color, float radius, ArrayList<float[]> points, Batch batch)
	{
		float originalAlpha = color.a;
		int count = 0;

		float[] lastPoint = null;

		ShapeRenderer sr = Renderer.useShapeRenderer();
		
		for (float[] point : points)
		{
			if (!cannon.getHitbox().contains(point[0], point[1]))
			{
				color.a = originalAlpha * (1.0f - ((float) count) / ((float) points.size()));
				sr.setColor(color);
				sr.circle(point[0] - radius, point[1] - radius, radius * 2);
	
				if (lastPoint != null)
				{
					sr.line(lastPoint[0], lastPoint[1], point[0], point[1]);
				}
	
				lastPoint = point;
			}
			
			count++;
		}

		color.a = originalAlpha;
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		typeButton.adjust(xChange, yChange);
		itemSelection.adjust(xChange, yChange);
		maxSpeed.adjust(xChange, yChange);
		interval.adjust(xChange, yChange);
		closeButton.adjust(xChange, yChange);
		minCount.adjust(xChange, yChange);
	}

	public TileCannon getCannon()
	{
		return cannon;
	}
}

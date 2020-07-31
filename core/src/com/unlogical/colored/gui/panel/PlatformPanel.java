package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.collision.Point;
import com.unlogical.colored.gui.GUICheckbox;
import com.unlogical.colored.gui.InputChangeListener;
import com.unlogical.colored.gui.Slider;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.terrain.tile.platform.TilePlatform;
import com.unlogical.colored.terrain.tile.platform.TilePlatformType;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.Renderer;

public class PlatformPanel extends GUIPanel
{
	private static final float MIN_MAX_SPEED = 0.001f;
	private static final float MAX_MAX_SPEED = 0.4f;

	private static final float MIN_ROTATION_VELOCITY = 0.01f;
	private static final float MAX_ROTATION_VELOCITY = 2.0f;

	private static Point point = new Point(0.0f, 0.0f);
	private static Point other = new Point(0.0f, 0.0f);

	private TilePlatform tile;

	private Button typeButton;

	private GUICheckbox limited;

	private UserInputLine startLimit;
	private UserInputLine endLimit;

	private GUICheckbox startMoveRight;

	@SuppressWarnings({ "rawtypes" })
	private Slider maxSpeed;

	private Button rotationAnchor;
	private Button closeButton;

	private float lastWidth;
	private float lastHeight;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PlatformPanel(String title, float xOffset, float yOffset, float width, float height)
	{
		super(title, xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 30;
		int inputWidth = (int) (width / 2) + 15;
		int currentOffset = 40;
		int inputX = (int) (xOffset + width / 2) - 20;

		this.typeButton = new Button("Close", (int) xOffset, currentOffset, (int) width, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				PlatformPanel.this.tile.setPlatformType(TilePlatformType.values()[PlatformPanel.this.tile.getPlatformType().ordinal() + 1 >= TilePlatformType.values().length ? 0 : PlatformPanel.this.tile.getPlatformType().ordinal() + 1]);
				PlatformPanel.this.tile.setStartLimit(0);
				PlatformPanel.this.tile.setEndLimit(0);

				PlatformPanel.this.init(PlatformPanel.this.tile);

				PlatformPanel.this.updateTile();
			}
		};
		this.typeButton.setActive(true);

		currentOffset += this.typeButton.getHeight() + 20;

		this.limited = new GUICheckbox(true, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onChecked()
			{
				this.update(true);
			}

			@Override
			public void onUnchecked()
			{
				this.update(false);
			}

			private void update(boolean checked)
			{
				PlatformPanel.this.tile.setLimited(checked);

				PlatformPanel.this.startLimit.setAllowUserInput(checked);
				PlatformPanel.this.endLimit.setAllowUserInput(checked);

				PlatformPanel.this.updateTile();
			}
		};
		this.limited.setActive(true);

		currentOffset += this.limited.getHeight() + 10;

		this.startLimit = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.startLimit.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				try
				{
					PlatformPanel.this.tile.setStartLimit(Integer.parseInt(PlatformPanel.this.startLimit.getInput()));

					PlatformPanel.this.updateTile();
				}
				catch (Exception e)
				{
				}
			}
		});
		this.startLimit.setActive(true);
		this.startLimit.setOnlyNumeric(true);

		currentOffset += this.startLimit.getHeight() + 10;

		this.endLimit = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.endLimit.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				try
				{
					PlatformPanel.this.tile.setEndLimit(Integer.parseInt(PlatformPanel.this.endLimit.getInput()));

					PlatformPanel.this.updateTile();
				}
				catch (Exception e)
				{
				}
			}
		});
		this.endLimit.setActive(true);
		this.endLimit.setOnlyNumeric(true);

		currentOffset += this.endLimit.getHeight() + 20;

		this.startMoveRight = new GUICheckbox(true, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onChecked()
			{
				PlatformPanel.this.tile.setStartMoveRight(true);

				PlatformPanel.this.updateTile();
			}

			@Override
			public void onUnchecked()
			{
				PlatformPanel.this.tile.setStartMoveRight(false);

				PlatformPanel.this.updateTile();
			}
		};
		this.startMoveRight.setActive(true);

		currentOffset += this.startMoveRight.getHeight() + 10;

		this.rotationAnchor = new Button("", inputX, currentOffset, inputWidth, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				PlatformPanel.this.tile.setRotationAnchor(PlatformPanel.this.tile.getRotationAnchor() >= 2 ? 0 : PlatformPanel.this.tile.getRotationAnchor() + 1);
				PlatformPanel.this.tile.updateAnchor();

				if (PlatformPanel.this.tile.hasMirror())
				{
					((TilePlatform) PlatformPanel.this.tile.getMirroredTile()).setRotationAnchor(PlatformPanel.this.tile.getRotationAnchor());
					((TilePlatform) PlatformPanel.this.tile.getMirroredTile()).updateAnchor();
				}

				PlatformPanel.this.tile.updateBorders();

				PlatformPanel.this.updateRotationAnchorLabel();

				PlatformPanel.this.updateTile();
			}
		};

		currentOffset += this.startMoveRight.getHeight() + lineHeight + 25;

		this.maxSpeed = new Slider(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, null)
		{
			@Override
			protected void onFactorChanged(float selectedRange, Object reference)
			{
				if (PlatformPanel.this.tile.getPlatformType() == TilePlatformType.ROTATING)
				{
					PlatformPanel.this.tile.setRotationVelocity(MIN_ROTATION_VELOCITY + selectedRange * (MAX_ROTATION_VELOCITY - MIN_ROTATION_VELOCITY));
				}
				else
				{
					PlatformPanel.this.tile.setMaxSpeed(MIN_MAX_SPEED + selectedRange * (MAX_MAX_SPEED - MIN_MAX_SPEED));
				}

				PlatformPanel.this.updateTile();
			}
		};
		this.maxSpeed.setActive(true);

		currentOffset += this.maxSpeed.getHeight() + 20;

		this.closeButton = new Button("Close", (int) xOffset, currentOffset, (int) width, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				PlatformPanel.this.setActive(false);
			}
		};
		this.closeButton.setActive(true);

		currentOffset += this.closeButton.getHeight();

		this.setHeight(currentOffset);
	}

	private void updateTile()
	{
		this.tile.initPlatform();
		this.tile.updateBorders();

		if (this.tile.getDimension() == Dimension.COLORED && this.tile.hasMirror())
		{
			this.tile.getMirroredTile().setRotation(0.0f);
			this.tile.getMirroredTile().setRotation(this.tile.getRotation());
			this.tile.getMirroredTile().setPosition(this.tile.getPosition().x, this.tile.getPosition().y);
			this.tile.initMirror(this.tile.getMirroredTile(), this.tile.getLevel().getType());
		}
	}

	private void updateRotationAnchorLabel()
	{
		if (this.tile.getRotationAnchor() == TilePlatform.ROTATION_LEFT)
		{
			this.rotationAnchor.setText("Left");
		}
		else if (this.tile.getRotationAnchor() == TilePlatform.ROTATION_CENTER)
		{
			this.rotationAnchor.setText("Center");
		}
		else if (this.tile.getRotationAnchor() == TilePlatform.ROTATION_RIGHT)
		{
			this.rotationAnchor.setText("Right");
		}
	}

	public void init(TilePlatform tile)
	{
		this.tile = tile;

		this.typeButton.setText("Type: " + tile.getPlatformType().name());

		this.limited.setChecked(tile.isLimited());

		if (!tile.isLimited())
		{
			this.limited.onUnchecked();
		}

		this.startLimit.setInput(tile.getStartLimit() + "");
		this.endLimit.setInput(tile.getEndLimit() + "");

		this.startMoveRight.setChecked(tile.isStartMoveRight());

		if (tile.getPlatformType() == TilePlatformType.HORIZONTAL || tile.getPlatformType() == TilePlatformType.VERTICAL)
		{
			this.maxSpeed.setSelectedRange((tile.getMaxSpeed() - MIN_MAX_SPEED) / (MAX_MAX_SPEED - MIN_MAX_SPEED));
		}
		else if (tile.getPlatformType() == TilePlatformType.ROTATING)
		{
			this.maxSpeed.setSelectedRange((tile.getRotationVelocity() - MIN_ROTATION_VELOCITY) / (MAX_ROTATION_VELOCITY - MIN_ROTATION_VELOCITY));
		}

		this.updateRotationAnchorLabel();

		this.setActive(true);
	}

	@Override
	protected void customUpdate(int delta)
	{
		if (this.tile.isRemoved())
		{
			this.tile = null;
			this.setActive(false);

			return;
		}

		if (this.lastWidth != this.tile.getWidth() || this.lastHeight == this.tile.getHeight())
		{
			this.updateTile();
		}

		this.typeButton.update(delta);
		this.limited.update(delta);
		this.startLimit.update(delta);
		this.endLimit.update(delta);
		this.startMoveRight.update(delta);
		this.maxSpeed.update(delta);
		this.closeButton.update(delta);
		this.rotationAnchor.update(delta);

		this.lastWidth = this.tile.getWidth();
		this.lastHeight = this.tile.getHeight();
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		this.typeButton.render(alphaFactor, batch);
		this.limited.render(alphaFactor, batch);
		this.startLimit.render(alphaFactor, batch);
		this.endLimit.render(alphaFactor, batch);
		this.startMoveRight.render(alphaFactor, batch);
		this.maxSpeed.render(batch);
		this.closeButton.render(alphaFactor, batch);
		this.rotationAnchor.render(alphaFactor, batch);

		ColorHelper.applyAlphaAndCopy(Color.WHITE, alphaFactor, batch);

		Renderer.drawString("Limited", this.xOffset + 5, this.limited.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("StartLimit", this.xOffset + 5, this.startLimit.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("EndLimit", this.xOffset + 5, this.endLimit.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Inversed", this.xOffset + 5, this.startMoveRight.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Anchor", this.xOffset + 5, this.rotationAnchor.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);

		Renderer.drawString("MaxSpeed", Color.BLUE, this.xOffset + 5, this.maxSpeed.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);

		ShapeRenderer sr = Renderer.useShapeRenderer();

		if (this.tile.getPlatformType() == TilePlatformType.ROTATING)
		{
			sr.setColor(Color.CYAN);
			sr.circle(this.tile.getCenterOfRotationX() - 2, this.tile.getCenterOfRotationY() - 2, 4);
		}

		if (this.tile.isLimited())
		{
			ColorHelper.applyAlphaAndCopy(Color.RED, 0.5f, sr);

			if (this.tile.getPlatformType() == TilePlatformType.HORIZONTAL)
			{
				sr.line(this.tile.getHitbox().getMinX() - this.tile.getStartLimit(), this.tile.getHitbox().getMinY(), this.tile.getHitbox().getMinX() - this.tile.getStartLimit(), this.tile.getHitbox().getMaxY());
				sr.line(this.tile.getHitbox().getMaxX() + this.tile.getEndLimit(), this.tile.getHitbox().getMinY(), this.tile.getHitbox().getMaxX() + this.tile.getEndLimit(), this.tile.getHitbox().getMaxY());
			}
			else if (this.tile.getPlatformType() == TilePlatformType.VERTICAL)
			{
				sr.line(this.tile.getHitbox().getMinX(), this.tile.getHitbox().getMinY() - this.tile.getStartLimit(), this.tile.getHitbox().getMaxX(), this.tile.getHitbox().getMinY() - this.tile.getStartLimit());
				sr.line(this.tile.getHitbox().getMinX(), this.tile.getHitbox().getMaxY() + this.tile.getEndLimit(), this.tile.getHitbox().getMaxX(), this.tile.getHitbox().getMaxY() + this.tile.getEndLimit());
			}
			else if (this.tile.getPlatformType() == TilePlatformType.ROTATING)
			{
				this.drawRotatedLine(this.tile.getPosition().x, this.tile.getPosition().y, this.tile.getPosition().x + this.tile.getHitbox().getRawWidth(), this.tile.getPosition().y, this.tile.getRotation() + this.tile.getStartLimit(), this.tile.getCenterOfRotationX(), this.tile.getCenterOfRotationY(), sr);
				this.drawRotatedLine(this.tile.getPosition().x, this.tile.getPosition().y, this.tile.getPosition().x + this.tile.getHitbox().getRawWidth(), this.tile.getPosition().y, this.tile.getRotation() + this.tile.getEndLimit(), this.tile.getCenterOfRotationX(), this.tile.getCenterOfRotationY(), sr);
			}
		}
	}

	private void drawRotatedLine(float startX, float startY, float endX, float endY, float rotation, float centerOfRotationX, float centerOfRotationY, ShapeRenderer sr)
	{
		point.setLocation(startX, startY);
		other.setLocation(centerOfRotationX, centerOfRotationY);

		Hitbox.rotatePoint(point, other, rotation);

		startX = point.getX();
		startY = point.getY();

		point.setX(endX);
		point.setY(endY);

		Hitbox.rotatePoint(point, other, rotation);

		sr.line(startX, startY, point.getX(), point.getY());
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		this.typeButton.adjust(xChange, yChange);
		this.limited.adjust(xChange, yChange);
		this.startLimit.adjust(xChange, yChange);
		this.endLimit.adjust(xChange, yChange);
		this.startMoveRight.adjust(xChange, yChange);
		this.maxSpeed.adjust(xChange, yChange);
		this.closeButton.adjust(xChange, yChange);
		this.rotationAnchor.adjust(xChange, yChange);
	}
}

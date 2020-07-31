package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.audio.AudioManager.SoundType;
import com.unlogical.colored.gui.GUICheckbox;
import com.unlogical.colored.gui.InputChangeListener;
import com.unlogical.colored.gui.Slider;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.terrain.tile.TileSoundSource;
import com.unlogical.colored.terrain.tile.TileSoundSource.SoundPlayTimeType;
import com.unlogical.colored.terrain.tile.TileSoundSource.SoundRangeType;
import com.unlogical.colored.terrain.tile.TileSoundSource.SoundVolumeType;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;

public class SoundSourcePanel extends GUIPanel
{
	private TileSoundSource tile;

	private Button soundType;
	private Button playTimeType;
	private Button volumeType;
	private Button rangeType;

	private GUICheckbox loop;
	private UserInputLine intervalMin;
	private UserInputLine intervalMax;

	@SuppressWarnings("rawtypes")
	private Slider volumeSlider;
	@SuppressWarnings("rawtypes")
	private Slider rangeSlider;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SoundSourcePanel(String title, float xOffset, float yOffset, float width, float height)
	{
		super(title, xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 30;
		int currentOffset = 40;
		int inputX = (int) (xOffset + width / 2) - 20;

		this.soundType = new Button("SoundType", inputX, currentOffset, (int) width / 2, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				SoundType type = SoundType.allSounds.get(SoundSourcePanel.this.soundType.getButtonLabel());

				if (type == null)
				{
					SoundSourcePanel.this.tile.setSoundType(SoundType.allSounds.firstEntry().getValue().name);
				}
				else
				{
					SoundSourcePanel.this.tile.setSoundType(type.next().name);
				}

				this.setText(SoundSourcePanel.this.tile.getSoundType());
			}
		};
		this.soundType.setActive(true);

		currentOffset += this.soundType.getHeight() + 10;

		this.playTimeType = new Button("PlayTimeType", inputX, currentOffset, (int) width / 2, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				SoundPlayTimeType type = SoundPlayTimeType.valueOf(SoundSourcePanel.this.playTimeType.getButtonLabel());

				if (type == null)
				{
					SoundSourcePanel.this.tile.setPlayType(SoundPlayTimeType.RAND_INTERVAL_AMBIENT);
				}
				else
				{
					SoundSourcePanel.this.tile.setPlayType(type.next());
				}

				this.setText(SoundSourcePanel.this.tile.getPlayType().name());
			}
		};
		this.playTimeType.setActive(true);

		currentOffset += this.playTimeType.getHeight() + 10;

		this.volumeType = new Button("volumeType", inputX, currentOffset, (int) width / 2, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				SoundVolumeType type = SoundVolumeType.valueOf(SoundSourcePanel.this.volumeType.getButtonLabel());

				if (type == null)
				{
					SoundSourcePanel.this.tile.setVolumeType(SoundVolumeType.STATIC);
				}
				else
				{
					SoundSourcePanel.this.tile.setVolumeType(type.next());
				}

				this.setText(SoundSourcePanel.this.tile.getVolumeType().name());
			}
		};
		this.volumeType.setActive(true);

		currentOffset += this.volumeType.getHeight() + 10;

		this.rangeType = new Button("volumeType", inputX, currentOffset, (int) width / 2, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				SoundRangeType type = SoundRangeType.valueOf(SoundSourcePanel.this.rangeType.getButtonLabel());

				if (type == null)
				{
					SoundSourcePanel.this.tile.setRangeType(SoundRangeType.CIRCULAR);
				}
				else
				{
					SoundSourcePanel.this.tile.setRangeType(type.next());
				}

				this.setText(SoundSourcePanel.this.tile.getRangeType().name());
			}
		};
		this.rangeType.setActive(true);

		currentOffset += this.rangeType.getHeight() + 10;

		int halfWidth = (int) (this.width / 2);

		this.intervalMin = new UserInputLine(inputX, currentOffset, halfWidth / 2, lineHeight);
		this.intervalMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!SoundSourcePanel.this.intervalMin.getInput().isEmpty())
				{
					SoundSourcePanel.this.tile.setIntervalMin(Integer.parseInt(SoundSourcePanel.this.intervalMin.getInput()));
				}
			}
		});
		this.intervalMin.setOnlyNumeric(true);
		this.intervalMin.setActive(true);

		this.intervalMax = new UserInputLine(inputX + halfWidth / 2, currentOffset, halfWidth / 2, lineHeight);
		this.intervalMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!SoundSourcePanel.this.intervalMax.getInput().isEmpty())
				{
					SoundSourcePanel.this.tile.setIntervalMax(Integer.parseInt(SoundSourcePanel.this.intervalMax.getInput()));
				}
			}
		});
		this.intervalMax.setOnlyNumeric(true);
		this.intervalMax.setActive(true);

		currentOffset += this.intervalMax.getHeight() + 10;

		this.loop = new GUICheckbox(false, inputX + halfWidth / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				SoundSourcePanel.this.tile.setLoop(false);
				SoundSourcePanel.this.intervalMin.setAllowUserInput(true);
				SoundSourcePanel.this.intervalMax.setAllowUserInput(true);
			}

			@Override
			public void onChecked()
			{
				SoundSourcePanel.this.tile.setLoop(true);
				SoundSourcePanel.this.intervalMin.setAllowUserInput(false);
				SoundSourcePanel.this.intervalMax.setAllowUserInput(false);
			}
		};

		currentOffset += this.loop.getHeight() + 10;

		this.volumeSlider = new Slider(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, null)
		{
			@Override
			protected void onFactorChanged(float selectedRange, Object reference)
			{
				SoundSourcePanel.this.tile.setVolume(selectedRange);
			}
		};
		this.volumeSlider.setActive(true);

		currentOffset += this.volumeSlider.getHeight() + 10;

		this.rangeSlider = new Slider(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, null)
		{
			@Override
			protected void onFactorChanged(float selectedRange, Object reference)
			{
				SoundSourcePanel.this.tile.setRange(selectedRange * 1000.0f);
			}
		};
		this.rangeSlider.setActive(true);

		this.setHeight(currentOffset + 50);
	}

	@Override
	protected void customUpdate(int delta)
	{
		this.intervalMin.update(delta);
		this.intervalMax.update(delta);
		this.playTimeType.update(delta);
		this.soundType.update(delta);
		this.volumeType.update(delta);
		this.rangeType.update(delta);
		this.volumeSlider.update(delta);
		this.rangeSlider.update(delta);
		this.loop.update(delta);
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		this.intervalMin.render(alphaFactor, batch);
		this.intervalMax.render(alphaFactor, batch);
		this.playTimeType.render(alphaFactor, batch);
		this.soundType.render(alphaFactor, batch);
		this.volumeType.render(alphaFactor, batch);
		this.rangeType.render(alphaFactor, batch);
		this.volumeSlider.render(batch);
		this.rangeSlider.render(batch);
		this.loop.render(alphaFactor, batch);

		ColorHelper.applyAlphaAndCopy(Color.WHITE, alphaFactor, batch);

		Renderer.useSpriteBatch();

		Renderer.drawString("Interval", this.xOffset + 5, this.intervalMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("PlayTimeType", this.xOffset + 5, this.playTimeType.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("VolumeType", this.xOffset + 5, this.volumeType.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("SoundType", this.xOffset + 5, this.soundType.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("RangeType", this.xOffset + 5, this.rangeType.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Volume", this.xOffset + 5, this.volumeSlider.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Range", this.xOffset + 5, this.rangeSlider.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Loop", this.xOffset + 5, this.loop.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);

		this.tile.drawDebug(Renderer.useShapeRenderer());

		Renderer.useSpriteBatch();
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		super.onPositionUpdate(xChange, yChange);

		this.intervalMin.adjust(xChange, yChange);
		this.intervalMax.adjust(xChange, yChange);
		this.playTimeType.adjust(xChange, yChange);
		this.soundType.adjust(xChange, yChange);
		this.volumeType.adjust(xChange, yChange);
		this.rangeType.adjust(xChange, yChange);
		this.volumeSlider.adjust(xChange, yChange);
		this.rangeSlider.adjust(xChange, yChange);
		this.loop.adjust(xChange, yChange);
	}

	public void init(TileSoundSource tile)
	{
		if (tile != null)
		{
			this.tile = tile;

			this.soundType.setText(tile.getSoundType());
			this.playTimeType.setText(tile.getPlayType().name());
			this.volumeType.setText(tile.getVolumeType().name());
			this.rangeType.setText(tile.getRangeType().name());
			this.intervalMin.setInput(tile.getIntervalMin() + "");
			this.intervalMax.setInput(tile.getIntervalMax() + "");
			this.volumeSlider.setSelectedRange(tile.getVolume());
			this.rangeSlider.setSelectedRange(tile.getRange() / 1000.0f);
			this.loop.setChecked(tile.isLoop());

			if (tile.isLoop())
			{
				this.loop.onChecked();
			}
			else
			{
				this.loop.onUnchecked();
			}
		}
		else
		{
			this.setActive(false);
		}
	}

	public boolean shouldLockInput()
	{
		return this.intervalMin.isFocused() || this.intervalMax.isFocused();
	}
}

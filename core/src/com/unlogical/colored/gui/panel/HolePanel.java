package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.gui.Slider;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.particle.ParticleEmitterImpl;
import com.unlogical.colored.terrain.tile.TileHole;
import com.unlogical.colored.util.Renderer;

@SuppressWarnings("rawtypes")
public class HolePanel extends GUIPanel
{
	private TileHole tile;
	private Slider attractionSlider;

	@SuppressWarnings("unchecked")
	public HolePanel(String title, float xOffset, float yOffset, float width, float height)
	{
		super(title, xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 30;
		int currentOffset = 40;

		this.attractionSlider = new Slider(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, null)
		{
			@Override
			protected void onFactorChanged(float selectedRange, Object reference)
			{
				if (HolePanel.this.tile.getAttraction() != selectedRange)
				{
					HolePanel.this.tile.setAttraction(selectedRange);

					ParticleEmitterImpl emitter = HolePanel.this.tile.updateAttractionEmitter();

					emitter.getSystem().simulateEmitter(emitter, 120000, 150);
				}
			}
		};
		this.attractionSlider.setActive(true);

		currentOffset += this.attractionSlider.getHeight();

		this.setHeight(currentOffset);
	}

	public void init(TileHole tile)
	{
		this.tile = tile;

		this.attractionSlider.setSelectedRange(tile.getAttraction());
	}

	@Override
	protected void customUpdate(int delta)
	{
		this.attractionSlider.update(delta);
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		this.attractionSlider.render(batch);

		Renderer.useSpriteBatch();
		Renderer.drawString("Attraction", Color.BLUE, this.xOffset + 5, this.attractionSlider.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		this.attractionSlider.adjust(xChange, yChange);
	}

	public boolean shouldLockInput()
	{
		return false;
	}
}

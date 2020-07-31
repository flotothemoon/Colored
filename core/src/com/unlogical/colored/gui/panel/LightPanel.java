package com.unlogical.colored.gui.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.unlogical.colored.gui.InputChangeListener;
import com.unlogical.colored.gui.Slider;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.gui.colorpicker.ColorPicker;
import com.unlogical.colored.gui.colorpicker.ColorPickerListener;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.terrain.tile.TileLight;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.ILightSource;
import com.unlogical.colored.util.LightType;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class LightPanel extends GUIPanel
{
	private static final float LIGHT_SCALE_FACTOR = 1.0f;
	private static final float DISTANCE_SLIDER_SCALE = 2000.0f;

	private TileLight light;

	private Slider intensitySlider;
	private Slider angleSlider;
	private Slider distanceSlider;

	private UserInputLine distance;
	private UserInputLine key;
	
	private Button colorButton;
	private ColorPicker colorPicker;

	public LightPanel(float xOffset, float yOffset, float width, float height)
	{
		super("Edit Light", xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 30;
		int inputWidth = (int) (width / 2) + 15;
		int currentOffset = 40;
		int inputX = (int) (xOffset + width / 2) - 20;

		currentOffset += 25;

		intensitySlider = new Slider(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, null)
		{
			@Override
			protected void onFactorChanged(float selectedRange, Object reference)
			{
				light.setIntensity(selectedRange * LIGHT_SCALE_FACTOR);
				
				updateConnectedLights();
			}
		};
		intensitySlider.setActive(true);

		currentOffset += intensitySlider.getHeight() + 35;

		angleSlider = new Slider(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, null)
		{
			@Override
			protected void onFactorChanged(float selectedRange, Object reference)
			{
				light.setAngle((int) (360 * selectedRange));
				
				updateConnectedLights();
			}
		};
		angleSlider.setActive(true);

		currentOffset += angleSlider.getHeight() + 35;

		distanceSlider = new Slider(xOffset + 5, currentOffset, width - 5, lineHeight, 0.0f, null)
		{
			@Override
			protected void onFactorChanged(float selectedRange, Object reference)
			{
				light.setDistance(Math.round(selectedRange * DISTANCE_SLIDER_SCALE));
				distance.setInput(light.getDistance() + "");
				
				updateConnectedLights();
			}
		};
		distanceSlider.setActive(true);

		currentOffset += distanceSlider.getHeight() + 5;

		distance = new UserInputLine((int) xOffset + 5, currentOffset, (int) width - 5, lineHeight);
		distance.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				try
				{
					light.setDistance(Integer.parseInt(distance.getInput()));
					distanceSlider.setSelectedRange(light.getDistance() / ((float) DISTANCE_SLIDER_SCALE));
					
					updateConnectedLights();
				}
				catch (Exception e)
				{
				}
			}
		});
		distance.setOnlyNumeric(true);
		distance.setActive(true);

		currentOffset += distance.getHeight() + 10;

		key = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		key.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				try
				{
					light.setKey(key.getInput());
					
					ShaderHandler.fetchLights(light.getLevel());
				}
				catch (Exception e)
				{
				}
			}
		});
		key.setActive(true);

		currentOffset += key.getHeight() + 10;
		
		colorButton = new Button("", inputX, currentOffset, inputWidth, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				colorPicker.init(light.getColor());
				colorPicker.enable();
			}
		};

		currentOffset += colorButton.getHeight();

		colorPicker = new ColorPicker("Light Color", 1920 / 2 - 200, 1080 / 2 - 200, 0, 0);
		colorPicker.addColorPickerListener(new ColorPickerListener()
		{
			@Override
			public void colorChanged(Color newColor)
			{
				light.setColor(newColor);
				colorButton.setFillColor(newColor);
				colorButton.setHoverColor(newColor);
				
				updateConnectedLights();
			}
		});

		this.setHeight(currentOffset);
	}
	
	private void updateConnectedLights()
	{
		if (light.getLightType() == LightType.CHAIN)
		{
			for (ILightSource source : light.getConnectedLights())
			{
				if (source != light)
				{
					TileLight tile = (TileLight) source;
					
					tile.setAngle(light.getAngle());
					tile.setDistance(light.getDistance());
					tile.setColor(light.getColor().cpy());
					tile.setIntensity(light.getIntensity());
				}
			}
		}
	}

	public void init(TileLight light)
	{
		this.light = light;

		intensitySlider.setSelectedRange(light.getIntensity() / LIGHT_SCALE_FACTOR);
		angleSlider.setSelectedRange((light.getAngle()) / 360.0f);
		distanceSlider.setSelectedRange(light.getDistance() / ((float) DISTANCE_SLIDER_SCALE));
		distance.setInput(light.getDistance() + "");
		key.setInput(light.getKey());
		colorButton.setFillColor(light.getColor());
		colorButton.setHoverColor(light.getColor());
		colorPicker.setActive(false);

		setActive(true);
	}

	@Override
	protected void customUpdate(int delta)
	{
		intensitySlider.update(delta);
		angleSlider.update(delta);
		distanceSlider.update(delta);
		distance.update(delta);
		key.update(delta);
		colorButton.update(delta);
		colorPicker.update(delta);
	}

	@Override
	protected void customRender(float alphaFactor, Batch batch)
	{
		intensitySlider.render(batch);
		angleSlider.render(batch);
		distanceSlider.render(batch);
		distance.render(alphaFactor, batch);
		key.render(alphaFactor, batch);
		colorButton.render(alphaFactor, batch);
		colorPicker.render(alphaFactor, batch);

		Color color = ColorHelper.applyAlphaAndCopy(Color.WHITE, alphaFactor, batch);

		ShaderHandler.disableLighting();
		
		Renderer.setFont(Fonts.getSmallFont());
		Renderer.drawString("Intensity: " + Math.round(light.getIntensity() * 100) + "%", color, xOffset + 5, intensitySlider.getY() - Renderer.getLineHeight(), 1.0f, batch);
		Renderer.drawString("Angle: " + light.getAngle(), color, xOffset + 5, angleSlider.getY() - Renderer.getLineHeight(), 1.0f, batch);
		Renderer.drawString("Distance", color, xOffset + 5, distanceSlider.getY() - Renderer.getLineHeight(), 1.0f, batch);
		Renderer.drawString("Key", color, xOffset + 5, key.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Light Color", color, xOffset + 5, colorButton.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		
		ShaderHandler.enableLighting();
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		intensitySlider.adjust(xChange, yChange);
		angleSlider.adjust(xChange, yChange);
		distance.adjust(xChange, yChange);
		key.adjust(xChange, yChange);
		distanceSlider.adjust(xChange, yChange);
		colorButton.adjust(xChange, yChange);
	}

	@Override
	protected boolean subContains(float mouseX, float mouseY)
	{
		return colorPicker.contains(mouseX, mouseY);
	}

	public boolean shouldLockInput()
	{
		return distance.isFocused() || key.isFocused();
	}
}

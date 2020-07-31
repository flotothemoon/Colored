package com.unlogical.colored.gui.panel;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.FilePaths;
import com.unlogical.colored.filesystem.files.WrapperTextureHandler;
import com.unlogical.colored.gui.GUICheckbox;
import com.unlogical.colored.gui.InputChangeListener;
import com.unlogical.colored.gui.UserInputLine;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.gui.colorpicker.ColorPicker;
import com.unlogical.colored.gui.colorpicker.ColorPickerListener;
import com.unlogical.colored.gui.menu.MenuHandler;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.mapeditor.MapEditor;
import com.unlogical.colored.particle.EmitterType;
import com.unlogical.colored.particle.ParticleEmitter;
import com.unlogical.colored.particle.ParticleEmitterImpl;
import com.unlogical.colored.particle.ParticleEmitterImpl.IntegerRange;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;
import com.unlogical.colored.wrapper.WrapperTexture;

public class ParticleEmitterPanel extends GUIPanel
{
	public static final float FACTOR_SCALE = 500;

	private WrapperTexture wrapper;

	private GUICheckbox limitToWrapper;
	private GUICheckbox shouldBounceOutOfArea;
	private GUICheckbox tightBounce;
	private GUICheckbox additiveMode;
	private GUICheckbox circularEmitter;
	private GUICheckbox circularInversed;
	private GUICheckbox relativeAccelerations;
	private GUICheckbox limitedLifetime;
	private GUICheckbox wobbleMode;
	private GUICheckbox scaleWithWidth;
	private GUICheckbox scaleWithHeight;
	private GUICheckbox scissorUp;
	private GUICheckbox scissorDown;
	private GUICheckbox scissorLeft;
	private GUICheckbox scissorRight;
	private GUICheckbox maxOutParticles;

	private UserInputLine simpleName;
	private UserInputLine depth;
	private UserInputLine parallax;
	private UserInputLine rotation;
	private UserInputLine maxParticles;
	private UserInputLine emitterLifetime;

	private UserInputLine spawnIntervalMin;
	private UserInputLine spawnIntervalMax;

	private UserInputLine spawnCountMin;
	private UserInputLine spawnCountMax;

	private UserInputLine initialLifeMin;
	private UserInputLine initialLifeMax;

	private UserInputLine initialSizeMin;
	private UserInputLine initialSizeMax;

	private UserInputLine wobbleIntervalMin;
	private UserInputLine wobbleIntervalMax;

	private UserInputLine xOffsetMin;
	private UserInputLine xOffsetMax;

	private UserInputLine yOffsetMin;
	private UserInputLine yOffsetMax;

	private UserInputLine velocityXMin;
	private UserInputLine velocityXMax;

	private UserInputLine velocityYMin;
	private UserInputLine velocityYMax;

	private UserInputLine startRotationMin;
	private UserInputLine startRotationMax;

	private UserInputLine rotationVelocityMin;
	private UserInputLine rotationVelocityMax;

	private UserInputLine growthFactorMin;
	private UserInputLine growthFactorMax;

	private UserInputLine gravityFactorMin;
	private UserInputLine gravityFactorMax;

	private UserInputLine windFactorMin;
	private UserInputLine windFactorMax;

	private UserInputLine targetAlphaMin;
	private UserInputLine targetAlphaMax;

	private UserInputLine fadeInMin;
	private UserInputLine fadeInMax;

	private UserInputLine fadeOutMin;
	private UserInputLine fadeOutMax;

	private UserInputLine templateName;

	private ColorPicker startColor;
	private ColorPicker endColor;
	private ColorPicker addColor;

	private Button startColorButton;
	private Button endColorButton;
	private Button addColorButton;
	private Button colorDistrModeButton;

	private Hitbox spawnHitbox = new Hitbox(0.0f, 0.0f, 0.0f, 0.0f);

	public ParticleEmitterPanel(float xOffset, float yOffset, float width, float height)
	{
		super("", xOffset, yOffset, width, height);

		this.fillColor = new Color(MapEditor.panelColor);

		int lineHeight = 25;
		int inputWidth = (int) (width / 2) + 15;
		int halfWidth = inputWidth / 2;
		int currentOffset = 40;
		int inputX = (int) (xOffset + width / 2) - 20;
		int yGap = 3;

		this.simpleName = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.simpleName.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.simpleName.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.setSimpleName(ParticleEmitterPanel.this.simpleName.getInput());
				}
			}
		});

		currentOffset += this.simpleName.getHeight() + yGap;

		this.depth = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.depth.setOnlyNumeric(true);
		this.depth.setMaxChars(7);
		this.depth.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.depth.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.setDepth(Integer.parseInt(ParticleEmitterPanel.this.depth.getInput()));
					ParticleEmitterPanel.this.wrapper.getLevel().sortWrappers();
				}
			}
		});

		currentOffset += this.depth.getHeight() + yGap;

		this.parallax = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.parallax.setMaxChars(5);
		this.parallax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				try
				{
					ParticleEmitterPanel.this.wrapper.setParallax(Float.parseFloat(ParticleEmitterPanel.this.parallax.getInput()));
				}
				catch (Exception e)
				{
				}
			}
		});

		currentOffset += this.depth.getHeight() + yGap;

		this.rotation = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.rotation.setOnlyNumeric(true);
		this.rotation.setMaxChars(3);
		this.rotation.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.rotation.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.setRotation(Integer.parseInt(ParticleEmitterPanel.this.rotation.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
					ParticleEmitterPanel.this.wrapper.updateBorders();
				}
			}
		});
		this.rotation.setAllowUserInput(true);

		currentOffset += this.rotation.getHeight() + yGap;

		this.maxParticles = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.maxParticles.setOnlyNumeric(true);
		this.maxParticles.setMaxChars(5);
		this.maxParticles.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.maxParticles.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.setMaxParticles(Integer.parseInt(ParticleEmitterPanel.this.maxParticles.getInput()));
					ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
					ParticleEmitterPanel.this.wrapper.recreateEmitter();
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.maxParticles.setAllowUserInput(true);

		currentOffset += this.maxParticles.getHeight() + yGap;

		this.emitterLifetime = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.emitterLifetime.setOnlyNumeric(true);
		this.emitterLifetime.setMaxChars(9);
		this.emitterLifetime.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.emitterLifetime.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().setLifeTime(Integer.parseInt(ParticleEmitterPanel.this.emitterLifetime.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.emitterLifetime.setAllowUserInput(true);

		currentOffset += this.emitterLifetime.getHeight() + yGap;

		this.limitToWrapper = new GUICheckbox(false, inputX + inputWidth / 6 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setLimitToWrapper(false);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setLimitToWrapper(true);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		this.shouldBounceOutOfArea = new GUICheckbox(false, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldBounceOutOfArea(false);
				ParticleEmitterPanel.this.wrapper.getEmitter().initMirror();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldBounceOutOfArea(true);
				ParticleEmitterPanel.this.wrapper.getEmitter().initMirror();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		this.tightBounce = new GUICheckbox(false, inputX + 5 * inputWidth / 6 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setTightBounce(false);
				ParticleEmitterPanel.this.wrapper.getEmitter().initMirror();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setTightBounce(true);
				ParticleEmitterPanel.this.wrapper.getEmitter().initMirror();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		currentOffset += this.tightBounce.getHeight() + yGap;

		this.circularEmitter = new GUICheckbox(false, inputX + inputWidth / 4 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setCircularEmitter(false);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setCircularEmitter(true);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		this.circularInversed = new GUICheckbox(false, inputX + 3 * inputWidth / 4 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setCircularInversed(false);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setCircularInversed(true);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		currentOffset += this.circularEmitter.getHeight() + yGap;

		this.wobbleMode = new GUICheckbox(false, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setWobbleMode(false);

				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setWobbleMode(true);

				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		currentOffset += this.wobbleMode.getHeight() + yGap;

		this.wobbleIntervalMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.wobbleIntervalMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.wobbleIntervalMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getWobbleInterval().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.wobbleIntervalMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.wobbleIntervalMin.setOnlyNumeric(true);

		this.wobbleIntervalMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.wobbleIntervalMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.wobbleIntervalMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getWobbleInterval().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.wobbleIntervalMax.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.wobbleIntervalMax.setOnlyNumeric(true);

		currentOffset += this.wobbleIntervalMax.getHeight() + yGap;

		this.xOffsetMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.xOffsetMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.xOffsetMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getXOffset().setMinValue(Float.parseFloat(ParticleEmitterPanel.this.xOffsetMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.additiveMode = new GUICheckbox(false, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setAdditive(false);
				;
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setAdditive(true);
				;
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
			}
		};

		currentOffset += this.additiveMode.getHeight() + yGap;

		this.relativeAccelerations = new GUICheckbox(false, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setRotateAccelerations(false);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setRotateAccelerations(true);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		currentOffset += this.relativeAccelerations.getHeight() + yGap;

		this.limitedLifetime = new GUICheckbox(false, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setHasLimitedLifetime(false);
				ParticleEmitterPanel.this.wrapper.getEmitter().restart();

				if (ParticleEmitterPanel.this.wrapper.getEmitter().hasMirror())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().restart();
				}

				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setHasLimitedLifetime(true);
				ParticleEmitterPanel.this.wrapper.getEmitter().restart();

				if (ParticleEmitterPanel.this.wrapper.getEmitter().hasMirror())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().restart();
				}

				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		currentOffset += this.limitedLifetime.getHeight() + yGap;

		this.scaleWithWidth = new GUICheckbox(false, inputX + inputWidth / 4 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setScaleWithWidth(false);
				ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setScaleWithWidth(true);
				ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
			}
		};

		this.scaleWithHeight = new GUICheckbox(false, inputX + 3 * inputWidth / 4 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setScaleWithHeight(false);
				ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setScaleWithHeight(true);
				ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
			}
		};

		currentOffset += this.scaleWithHeight.getHeight() + yGap;

		this.scissorUp = new GUICheckbox(false, inputX + inputWidth / 8 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.UP, false);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.UP, true);
			}
		};

		this.scissorDown = new GUICheckbox(false, inputX + 3 * inputWidth / 8 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.DOWN, false);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.DOWN, true);
			}
		};

		this.scissorLeft = new GUICheckbox(false, inputX + 5 * inputWidth / 8 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.LEFT, false);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.LEFT, true);
			}
		};

		this.scissorRight = new GUICheckbox(false, inputX + 7 * inputWidth / 8 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.RIGHT, false);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setShouldScissor(ParticleEmitter.RIGHT, true);
			}
		};

		currentOffset += this.scissorUp.getHeight() + yGap;

		this.maxOutParticles = new GUICheckbox(false, inputX + inputWidth / 2 - lineHeight / 2, currentOffset, lineHeight, lineHeight)
		{
			@Override
			public void onUnchecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setMaxOutParticles(false);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}

			@Override
			public void onChecked()
			{
				ParticleEmitterPanel.this.wrapper.getEmitter().setMaxOutParticles(true);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
			}
		};

		currentOffset += this.maxOutParticles.getHeight() + yGap;

		this.spawnIntervalMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.spawnIntervalMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.spawnIntervalMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getSpawnInterval().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.spawnIntervalMin.getInput()));
					ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.spawnIntervalMin.setOnlyNumeric(true);

		this.spawnIntervalMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.spawnIntervalMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.spawnIntervalMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getSpawnInterval().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.spawnIntervalMax.getInput()));
					ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.spawnIntervalMax.setOnlyNumeric(true);

		currentOffset += this.spawnIntervalMin.getHeight() + yGap;

		this.spawnCountMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.spawnCountMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.spawnCountMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getSpawnCount().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.spawnCountMin.getInput()));
					ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.spawnCountMin.setOnlyNumeric(true);

		this.spawnCountMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.spawnCountMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.spawnCountMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getSpawnCount().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.spawnCountMax.getInput()));
					ParticleEmitterPanel.this.wrapper.updateEmitterOriginals();
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.spawnCountMax.setOnlyNumeric(true);

		currentOffset += this.spawnCountMin.getHeight() + yGap;

		this.initialLifeMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.initialLifeMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.initialLifeMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getInitialLife().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.initialLifeMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.initialLifeMin.setOnlyNumeric(true);

		this.initialLifeMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.initialLifeMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.initialLifeMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getInitialLife().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.initialLifeMax.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.initialLifeMax.setOnlyNumeric(true);

		currentOffset += this.initialLifeMin.getHeight() + yGap;

		this.initialSizeMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.initialSizeMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.initialSizeMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getInitialSize().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.initialSizeMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.initialSizeMin.setOnlyNumeric(true);

		this.initialSizeMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.initialSizeMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.initialSizeMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getInitialSize().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.initialSizeMax.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});
		this.initialSizeMax.setOnlyNumeric(true);

		currentOffset += this.initialSizeMin.getHeight() + yGap;

		this.xOffsetMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.xOffsetMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.xOffsetMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getXOffset().setMinValue(Float.parseFloat(ParticleEmitterPanel.this.xOffsetMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.xOffsetMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.xOffsetMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.xOffsetMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getXOffset().setMaxValue(Float.parseFloat(ParticleEmitterPanel.this.xOffsetMax.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.xOffsetMin.getHeight() + yGap;

		this.yOffsetMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.yOffsetMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.yOffsetMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getYOffset().setMinValue(Float.parseFloat(ParticleEmitterPanel.this.yOffsetMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.yOffsetMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.yOffsetMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.yOffsetMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getYOffset().setMaxValue(Float.parseFloat(ParticleEmitterPanel.this.yOffsetMax.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.yOffsetMin.getHeight() + yGap;

		this.velocityXMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.velocityXMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.velocityXMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getVelocityX().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.velocityXMin.getInput()) / FACTOR_SCALE);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.velocityXMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.velocityXMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.velocityXMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getVelocityX().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.velocityXMax.getInput()) / FACTOR_SCALE);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.xOffsetMin.getHeight() + yGap;

		this.velocityYMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.velocityYMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.velocityYMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getVelocityY().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.velocityYMin.getInput()) / FACTOR_SCALE);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.velocityYMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.velocityYMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.velocityYMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getVelocityY().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.velocityYMax.getInput()) / FACTOR_SCALE);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.velocityYMin.getHeight() + yGap;

		this.startRotationMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.startRotationMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.startRotationMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getStartRotation().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.startRotationMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.startRotationMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.startRotationMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.startRotationMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getStartRotation().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.startRotationMax.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.startRotationMin.getHeight() + yGap;

		this.rotationVelocityMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.rotationVelocityMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.rotationVelocityMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getRotationVelocity().setMinValue(Float.parseFloat(ParticleEmitterPanel.this.rotationVelocityMin.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.rotationVelocityMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.rotationVelocityMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.rotationVelocityMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getRotationVelocity().setMaxValue(Float.parseFloat(ParticleEmitterPanel.this.rotationVelocityMax.getInput()));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.rotationVelocityMax.getHeight() + yGap;

		this.growthFactorMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.growthFactorMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.growthFactorMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getGrowthFactor().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.growthFactorMin.getInput()) / FACTOR_SCALE);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.growthFactorMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.growthFactorMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.growthFactorMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getGrowthFactor().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.growthFactorMax.getInput()) / FACTOR_SCALE);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.growthFactorMax.getHeight() + yGap;

		this.gravityFactorMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.gravityFactorMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.gravityFactorMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getGravityFactor().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.gravityFactorMin.getInput()) / (FACTOR_SCALE * 50.0f));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.gravityFactorMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.gravityFactorMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.gravityFactorMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getGravityFactor().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.gravityFactorMax.getInput()) / (FACTOR_SCALE * 50.0f));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.gravityFactorMax.getHeight() + yGap;

		this.windFactorMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.windFactorMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.windFactorMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getWindFactor().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.windFactorMin.getInput()) / (FACTOR_SCALE * 50.0f));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.windFactorMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.windFactorMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.windFactorMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getWindFactor().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.windFactorMax.getInput()) / (FACTOR_SCALE * 50.0f));
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.windFactorMax.getHeight() + yGap;

		this.targetAlphaMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.targetAlphaMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.targetAlphaMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getStartAlpha().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.targetAlphaMin.getInput()));
					ParticleEmitterPanel.this.reSelectColors(ParticleEmitterPanel.this.wrapper);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.targetAlphaMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.targetAlphaMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.targetAlphaMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getStartAlpha().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.targetAlphaMax.getInput()));
					ParticleEmitterPanel.this.reSelectColors(ParticleEmitterPanel.this.wrapper);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
				}
			}
		});

		currentOffset += this.targetAlphaMax.getHeight() + yGap;

		this.fadeInMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.fadeInMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.fadeInMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getFadeInTime().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.fadeInMin.getInput()));
					ParticleEmitterPanel.this.wrapper.getEmitter().setShouldFadeIn(ParticleEmitterPanel.this.wrapper.getEmitter().getFadeInTime().averageValue() > 0);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
				}
			}
		});

		this.fadeInMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.fadeInMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.fadeInMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getFadeInTime().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.fadeInMax.getInput()));
					ParticleEmitterPanel.this.wrapper.getEmitter().setShouldFadeIn(ParticleEmitterPanel.this.wrapper.getEmitter().getFadeInTime().averageValue() > 0);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
				}
			}
		});

		currentOffset += this.fadeInMin.getHeight() + yGap;

		this.fadeOutMin = new UserInputLine(inputX, currentOffset, halfWidth, lineHeight);
		this.fadeOutMin.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.fadeOutMin.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getFadeOutTime().setMinValue(Integer.parseInt(ParticleEmitterPanel.this.fadeOutMin.getInput()));
					ParticleEmitterPanel.this.wrapper.getEmitter().setShouldFadeOut(ParticleEmitterPanel.this.wrapper.getEmitter().getFadeOutTime().averageValue() > 0);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		this.fadeOutMax = new UserInputLine(inputX + halfWidth, currentOffset, halfWidth, lineHeight);
		this.fadeOutMax.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.fadeOutMax.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.wrapper.getEmitter().getFadeOutTime().setMaxValue(Integer.parseInt(ParticleEmitterPanel.this.fadeOutMax.getInput()));
					ParticleEmitterPanel.this.wrapper.getEmitter().setShouldFadeOut(ParticleEmitterPanel.this.wrapper.getEmitter().getFadeOutTime().averageValue() > 0);
					ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, true);
				}
			}
		});

		currentOffset += this.fadeOutMax.getHeight() + yGap;

		this.startColor = new ColorPicker("Emitter Start Color", 1920 / 2 - 400, 1080 / 2 - 200, 0, 0);
		this.startColor.addColorPickerListener(new ColorPickerListener()
		{
			@Override
			public void colorChanged(Color newColor)
			{
				ParticleEmitterPanel.this.wrapper.setMainTint(newColor);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
				ParticleEmitterPanel.this.reSelectColors(ParticleEmitterPanel.this.wrapper);
			}
		});

		lineHeight *= 1.5f;

		this.startColorButton = new Button("", inputX, currentOffset, inputWidth / 2, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				ParticleEmitterPanel.this.startColor.init(ParticleEmitterPanel.this.wrapper.getStartEmitterTint());
				ParticleEmitterPanel.this.startColor.enable();
			}
		};

		this.endColor = new ColorPicker("Emitter End Color", 1920 / 2, 1080 / 2 - 200, 0, 0);
		this.endColor.addColorPickerListener(new ColorPickerListener()
		{
			@Override
			public void colorChanged(Color newColor)
			{
				ParticleEmitterPanel.this.wrapper.setEndTint(newColor);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
				ParticleEmitterPanel.this.reSelectColors(ParticleEmitterPanel.this.wrapper);
			}
		});

		this.endColorButton = new Button("", inputX + inputWidth / 2, currentOffset, inputWidth / 2, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				ParticleEmitterPanel.this.endColor.init(ParticleEmitterPanel.this.wrapper.getEndEmitterTint());
				ParticleEmitterPanel.this.endColor.enable();
			}
		};

		currentOffset += this.endColorButton.getHeight() + yGap;

		this.addColor = new ColorPicker("Emitter Add Color", 1920 / 2, 1080 / 2 - 200, 0, 0);
		this.addColor.addColorPickerListener(new ColorPickerListener()
		{
			@Override
			public void colorChanged(Color newColor)
			{
				ParticleEmitterPanel.this.wrapper.setAddTint(newColor);
				ParticleEmitterPanel.this.updateWrapper(ParticleEmitterPanel.this.wrapper, false);
			}
		});

		this.addColorButton = new Button("", inputX, currentOffset, inputWidth, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				ParticleEmitterPanel.this.addColor.init(ParticleEmitterPanel.this.wrapper.getAddEmitterTint());
				ParticleEmitterPanel.this.addColor.enable();
			}
		};

		currentOffset += this.addColorButton.getHeight() + yGap;

		lineHeight /= 1.5f;

		this.colorDistrModeButton = new Button("", inputX, currentOffset, inputWidth, lineHeight, false)
		{
			@Override
			public void onClick()
			{
				ParticleEmitterPanel.this.wrapper.setColorDistributionMode(ParticleEmitterPanel.this.wrapper.getColorDistributionMode().next());
				ParticleEmitterPanel.this.colorDistrModeButton.setText(ParticleEmitterPanel.this.wrapper.getColorDistributionMode().name());

				ParticleEmitterPanel.this.reSelectColors(ParticleEmitterPanel.this.wrapper);
			}
		};

		currentOffset += this.colorDistrModeButton.getHeight() + yGap;

		this.templateName = new UserInputLine(inputX, currentOffset, inputWidth, lineHeight);
		this.templateName.addInputChangedListener(new InputChangeListener()
		{
			@Override
			public void onInputChanged()
			{
				if (!ParticleEmitterPanel.this.templateName.getInput().isEmpty())
				{
					ParticleEmitterPanel.this.templateName.setInput(ParticleEmitterPanel.this.templateName.getInput().replaceAll("\\s", ""));
				}
			}
		});
		currentOffset += this.templateName.getHeight();

		this.setHeight(currentOffset);
	}

	public void updateWrapper(WrapperTexture wrapper, boolean reSimulate)
	{
		wrapper.getEmitter().initMirror();

		if (reSimulate)
		{
			wrapper.getEmitter().restart();
			wrapper.getLevel().getParticleSystem().simulateEmitter(wrapper.getEmitter(), 120000, 150);

			if (wrapper.getEmitter().hasMirror())
			{
				wrapper.getEmitter().getMirroredEmitter().restart();
				wrapper.getLevel().getParticleSystem().simulateEmitter(wrapper.getEmitter().getMirroredEmitter(), 120000, 150);
			}
		}
	}

	public void reSelectColors(WrapperTexture wrapper)
	{
		wrapper.getLevel().getParticleSystem().reSelectColors(wrapper.getEmitter());

		if (wrapper.getEmitter().hasMirror())
		{
			wrapper.getLevel().getParticleSystem().reSelectColors(wrapper.getEmitter().getMirroredEmitter());
		}
	}

	public void init(WrapperTexture wrapper)
	{
		this.wrapper = wrapper;

		if (wrapper != null)
		{
			ParticleEmitterImpl emitter = wrapper.getEmitter();

			this.title = "Particle Settings";

			this.limitToWrapper.setActive(true);
			this.limitToWrapper.setChecked(emitter.limittedToWrapper());

			this.circularEmitter.setActive(true);
			this.circularEmitter.setChecked(wrapper.getEmitter().isCircularEmitter());

			this.circularInversed.setActive(true);
			this.circularInversed.setChecked(wrapper.getEmitter().isCircularInversed());

			this.additiveMode.setActive(true);
			this.additiveMode.setChecked(wrapper.getEmitter().isAdditive());

			this.relativeAccelerations.setActive(true);
			this.relativeAccelerations.setChecked(wrapper.getEmitter().isRotateAccelerations());

			this.limitedLifetime.setActive(true);
			this.limitedLifetime.setChecked(wrapper.getEmitter().hasLimitedLifetime());

			this.wobbleMode.setActive(true);
			this.wobbleMode.setChecked(wrapper.getEmitter().isWobbleMode());

			this.scaleWithWidth.setActive(true);
			this.scaleWithWidth.setChecked(wrapper.getEmitter().isScaleWithWidth());

			this.scaleWithHeight.setActive(true);
			this.scaleWithHeight.setChecked(wrapper.getEmitter().isScaleWithHeight());

			this.scissorUp.setActive(true);
			this.scissorUp.setChecked(wrapper.getEmitter().shouldScissor(ParticleEmitter.UP));

			this.scissorDown.setActive(true);
			this.scissorDown.setChecked(wrapper.getEmitter().shouldScissor(ParticleEmitter.DOWN));

			this.scissorLeft.setActive(true);
			this.scissorLeft.setChecked(wrapper.getEmitter().shouldScissor(ParticleEmitter.LEFT));

			this.scissorRight.setActive(true);
			this.scissorRight.setChecked(wrapper.getEmitter().shouldScissor(ParticleEmitter.RIGHT));

			this.maxOutParticles.setActive(true);
			this.maxOutParticles.setChecked(wrapper.getEmitter().isMaxOutParticles());

			this.shouldBounceOutOfArea.setActive(true);
			this.shouldBounceOutOfArea.setChecked(emitter.shouldBounceOutOfArea());

			this.tightBounce.setActive(true);
			this.tightBounce.setChecked(emitter.isTightBounce());

			this.simpleName.setActive(true);
			this.simpleName.setInput(wrapper.getSimpleName());

			this.depth.setActive(true);
			this.depth.setInput(wrapper.getDepth() + "");

			this.parallax.setActive(true);
			this.parallax.setInput(wrapper.getEmitterParallax() + "");

			this.rotation.setActive(true);
			this.rotation.setInput(wrapper.getRotation() + "");

			this.maxParticles.setActive(true);
			this.maxParticles.setInput(wrapper.getMaxParticles() + "");

			this.emitterLifetime.setActive(true);
			this.emitterLifetime.setInput(wrapper.getEmitter().getLifeTime() + "");

			this.spawnIntervalMin.setActive(true);
			this.spawnIntervalMin.setInput(emitter.getSpawnInterval().minValue() + "");
			this.spawnIntervalMax.setActive(true);
			this.spawnIntervalMax.setInput(emitter.getSpawnInterval().maxValue() + "");

			this.spawnCountMin.setActive(true);
			this.spawnCountMin.setInput(emitter.getSpawnCount().minValue() + "");
			this.spawnCountMax.setActive(true);
			this.spawnCountMax.setInput(emitter.getSpawnCount().maxValue() + "");

			this.initialLifeMin.setActive(true);
			this.initialLifeMin.setInput(emitter.getInitialLife().minValue() + "");
			this.initialLifeMax.setActive(true);
			this.initialLifeMax.setInput(emitter.getInitialLife().maxValue() + "");

			this.initialSizeMin.setActive(true);
			this.initialSizeMin.setInput(emitter.getInitialSize().minValue() + "");
			this.initialSizeMax.setActive(true);
			this.initialSizeMax.setInput(emitter.getInitialSize().maxValue() + "");

			this.wobbleIntervalMin.setActive(true);
			this.wobbleIntervalMin.setInput(emitter.getWobbleInterval().minValue() + "");
			this.wobbleIntervalMax.setActive(true);
			this.wobbleIntervalMax.setInput(emitter.getWobbleInterval().maxValue() + "");

			this.xOffsetMin.setActive(true);
			this.xOffsetMin.setInput(emitter.getXOffset().minValue() + "");
			this.xOffsetMax.setActive(true);
			this.xOffsetMax.setInput(emitter.getXOffset().maxValue() + "");

			this.yOffsetMin.setActive(true);
			this.yOffsetMin.setInput(emitter.getYOffset().minValue() + "");
			this.yOffsetMax.setActive(true);
			this.yOffsetMax.setInput(emitter.getYOffset().maxValue() + "");

			this.velocityXMin.setActive(true);
			this.velocityXMin.setInput((int) (emitter.getVelocityX().minValue() * FACTOR_SCALE) + "");
			this.velocityXMax.setActive(true);
			this.velocityXMax.setInput((int) (emitter.getVelocityX().maxValue() * FACTOR_SCALE) + "");

			this.velocityYMin.setActive(true);
			this.velocityYMin.setInput((int) (emitter.getVelocityY().minValue() * FACTOR_SCALE) + "");
			this.velocityYMax.setActive(true);
			this.velocityYMax.setInput((int) (emitter.getVelocityY().maxValue() * FACTOR_SCALE) + "");

			this.fadeInMin.setActive(true);
			this.fadeInMin.setInput(emitter.getFadeInTime().minValue() + "");
			this.fadeInMax.setActive(true);
			this.fadeInMax.setInput(emitter.getFadeInTime().maxValue() + "");

			this.fadeOutMin.setActive(true);
			this.fadeOutMin.setInput(emitter.getFadeOutTime().minValue() + "");
			this.fadeOutMax.setActive(true);
			this.fadeOutMax.setInput(emitter.getFadeOutTime().maxValue() + "");

			this.startRotationMin.setActive(true);
			this.startRotationMin.setInput(emitter.getStartRotation().minValue() + "");
			this.startRotationMax.setActive(true);
			this.startRotationMax.setInput(emitter.getStartRotation().maxValue() + "");

			this.rotationVelocityMin.setActive(true);
			this.rotationVelocityMin.setInput(emitter.getRotationVelocity().minValue() + "");
			this.rotationVelocityMax.setActive(true);
			this.rotationVelocityMax.setInput(emitter.getRotationVelocity().maxValue() + "");

			this.growthFactorMin.setActive(true);
			this.growthFactorMin.setInput((int) (emitter.getGrowthFactor().minValue() * FACTOR_SCALE) + "");
			this.growthFactorMax.setActive(true);
			this.growthFactorMax.setInput((int) (emitter.getGrowthFactor().maxValue() * FACTOR_SCALE) + "");

			this.gravityFactorMin.setActive(true);
			this.gravityFactorMin.setInput((int) (emitter.getGravityFactor().minValue() * (FACTOR_SCALE * 50.0f)) + "");
			this.gravityFactorMax.setActive(true);
			this.gravityFactorMax.setInput((int) (emitter.getGravityFactor().maxValue() * (FACTOR_SCALE * 50.0f)) + "");

			this.windFactorMin.setActive(true);
			this.windFactorMin.setInput((int) (emitter.getWindFactor().minValue() * (FACTOR_SCALE * 50.0f)) + "");
			this.windFactorMax.setActive(true);
			this.windFactorMax.setInput((int) (emitter.getWindFactor().maxValue() * (FACTOR_SCALE * 50.0f)) + "");

			this.targetAlphaMin.setActive(true);
			this.targetAlphaMin.setInput(emitter.getStartAlpha().minValue() + "");
			this.targetAlphaMax.setActive(true);
			this.targetAlphaMax.setInput(emitter.getStartAlpha().maxValue() + "");

			this.startColor.init(wrapper.getStartEmitterTint());
			this.endColor.init(wrapper.getEndEmitterTint());
			this.addColor.init(wrapper.getAddEmitterTint());
			this.colorDistrModeButton.setText(wrapper.getColorDistributionMode().name());

			this.templateName.setActive(true);

			if (wrapper.getTemplateName() != null && !wrapper.getTemplateName().isEmpty())
			{
				this.templateName.setInput(wrapper.getTemplateName());
			}
			else
			{
				this.templateName.setInput("");
			}

			this.setActive(true);
		}
		else
		{
			this.setActive(false);
		}
	}

	@Override
	public void customUpdate(int delta)
	{
		this.limitToWrapper.update(delta);
		this.circularEmitter.update(delta);
		this.circularInversed.update(delta);
		this.additiveMode.update(delta);
		this.relativeAccelerations.update(delta);
		this.limitedLifetime.update(delta);
		this.wobbleMode.update(delta);
		this.shouldBounceOutOfArea.update(delta);
		this.tightBounce.update(delta);
		this.scaleWithWidth.update(delta);
		this.scaleWithHeight.update(delta);
		this.scissorUp.update(delta);
		this.scissorDown.update(delta);
		this.scissorLeft.update(delta);
		this.scissorRight.update(delta);
		this.maxOutParticles.update(delta);

		this.simpleName.update(delta);
		this.depth.update(delta);
		this.parallax.update(delta);
		this.rotation.update(delta);
		this.maxParticles.update(delta);
		this.emitterLifetime.update(delta);

		this.spawnIntervalMin.update(delta);
		this.spawnIntervalMax.update(delta);
		this.spawnCountMin.update(delta);
		this.spawnCountMax.update(delta);

		this.initialLifeMin.update(delta);
		this.initialLifeMax.update(delta);
		this.initialSizeMin.update(delta);
		this.initialSizeMax.update(delta);

		this.wobbleIntervalMin.update(delta);
		this.wobbleIntervalMax.update(delta);

		this.xOffsetMin.update(delta);
		this.xOffsetMax.update(delta);
		this.yOffsetMin.update(delta);
		this.yOffsetMax.update(delta);

		this.velocityXMin.update(delta);
		this.velocityXMax.update(delta);
		this.velocityYMin.update(delta);
		this.velocityYMax.update(delta);

		this.fadeInMin.update(delta);
		this.fadeInMax.update(delta);
		this.fadeOutMin.update(delta);
		this.fadeOutMax.update(delta);

		this.startRotationMin.update(delta);
		this.startRotationMax.update(delta);

		this.rotationVelocityMin.update(delta);
		this.rotationVelocityMax.update(delta);

		this.growthFactorMin.update(delta);
		this.growthFactorMax.update(delta);

		this.gravityFactorMin.update(delta);
		this.gravityFactorMax.update(delta);

		this.windFactorMin.update(delta);
		this.windFactorMax.update(delta);

		this.targetAlphaMin.update(delta);
		this.targetAlphaMax.update(delta);

		this.startColor.update(delta);
		this.startColorButton.update(delta);

		Color colorValue = new Color(this.wrapper.getStartEmitterTint());
		colorValue.a = 1.0f;

		this.startColorButton.setFillColor(colorValue);
		this.startColorButton.setHoverColor(colorValue);

		this.endColor.update(delta);
		this.endColorButton.update(delta);

		colorValue = new Color(this.wrapper.getEndEmitterTint());
		colorValue.a = 1.0f;

		this.endColorButton.setFillColor(colorValue);
		this.endColorButton.setHoverColor(colorValue);

		this.addColor.update(delta);
		this.addColorButton.update(delta);

		this.addColorButton.setFillColor(this.wrapper.getAddEmitterTint());
		this.addColorButton.setHoverColor(this.wrapper.getAddEmitterTint());

		this.colorDistrModeButton.update(delta);
		this.templateName.update(delta);

		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER))
		{
			if (this.templateName.isFocused())
			{
				String templateName = this.templateName.getInput();

				if (!templateName.isEmpty())
				{
					Debug.log("Saving selected emitter as \"" + templateName + "\" in \"" + FilePaths.EMITTER_TEMPLATES + "\" ...");

					this.wrapper.setTemplateName(templateName);
					ImportWrapperPanel.addTemplateEmitter(templateName, this.wrapper);

					Debug.log("Saved selected emitter as \"" + templateName + "\" in \"" + FilePaths.EMITTER_TEMPLATES + "\".");
					MenuHandler.displaySavedMessage("\"" + templateName + "\"", 500);
					EmitterType.reloadAllTemplates();

					if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
					{
						Debug.log("Applying new template to all existing implementations across all words...");

						WrapperTexture template = this.wrapper;
						ParticleEmitterImpl templateEmitter = this.wrapper.getEmitter();

						for (String levelID : Level.getAllLevelIDs())
						{
							try
							{
								WrapperTextureHandler handler = new WrapperTextureHandler(FileManager.getFile(FileManager.globaliseFile(Level.getMapPathByID(levelID) + "/wrappers.cfg")));

								Level dummyLevel = new Level();
								dummyLevel.setDummyLevel(true);
								dummyLevel.createParticleSystem();

								ArrayList<WrapperTexture> wrappers = handler.read(dummyLevel);

								for (WrapperTexture wrapper : wrappers)
								{
									if (templateName.equals(wrapper.getTemplateName()))
									{
										Vector2 originalPosition = wrapper.getPosition().cpy();

										IntegerRange templateSpawnCount = template.getEmitter().getSpawnCount();
										IntegerRange wrapperSpawnCount = wrapper.getEmitter().getSpawnCount();

										float particleScale = 1.0f;

										if (wrapper.getEmitter().isScaleWithWidth() && wrapper.getEmitter().isScaleWithHeight())
										{
											particleScale = (float) (wrapper.getEmitterWidth() * wrapper.getEmitterHeight()) / (float) (template.getEmitterWidth() * template.getEmitterHeight());
										}
										else if (wrapper.getEmitter().isScaleWithWidth())
										{
											particleScale = (float) wrapper.getEmitterWidth() / (float) template.getEmitterWidth();
										}
										else if (wrapper.getEmitter().isScaleWithHeight())
										{
											particleScale = (float) wrapper.getEmitterHeight() / (float) template.getEmitterHeight();
										}

										if (particleScale != 1.0f)
										{
											wrapper.setMaxParticles((int) (template.getMaxParticles() * particleScale));
											wrapperSpawnCount.setMinValue(Math.round(templateSpawnCount.minValue() * particleScale));
											wrapperSpawnCount.setMaxValue(Math.round(templateSpawnCount.maxValue() * particleScale));
											wrapper.recreateEmitter();
										}

										wrapper.setImages(template);
										wrapper.setAddTint(template.getAddTint());
										wrapper.setMainTint(template.getStartEmitterTint());
										wrapper.setEndTint(template.getEndEmitterTint());
										wrapper.setColorDistributionMode(template.getColorDistributionMode());
										wrapper.setFlippedVertically(template.isFlippedVertically());
										wrapper.setFlippedHorizontally(template.isFlippedHorizontally());
										wrapper.setShouldClip(template.isShouldClip());

										ParticleEmitterImpl emitter = wrapper.getEmitter();

										emitter.setInitalLife(templateEmitter.getInitialLife());
										emitter.setInitalSize(templateEmitter.getInitialSize());
										emitter.setCircularEmitter(templateEmitter.isCircularEmitter());
										emitter.setFadeInTime(templateEmitter.getFadeInTime());
										emitter.setFadeOutTime(templateEmitter.getFadeOutTime());
										emitter.setGravityFactor(templateEmitter.getGravityFactor());
										emitter.setGrowthFactor(templateEmitter.getGrowthFactor());
										emitter.setHasLimitedLifetime(templateEmitter.hasLimitedLifetime());
										emitter.setLimitToWrapper(templateEmitter.limittedToWrapper());
										emitter.setMaxOutParticles(templateEmitter.isMaxOutParticles());
										emitter.setRotateAccelerations(templateEmitter.isRotateAccelerations());
										emitter.setRotationVelocity(templateEmitter.getRotationVelocity());
										emitter.setScaleWithWidth(templateEmitter.isScaleWithWidth());
										emitter.setScaleWithHeight(templateEmitter.isScaleWithHeight());
										emitter.setScissoredDirections(templateEmitter.getScissoredDirections());
										emitter.setShouldBounceOutOfArea(templateEmitter.shouldBounceOutOfArea());
										emitter.setShouldFadeIn(templateEmitter.shouldFadeIn());
										emitter.setShouldFadeOut(templateEmitter.shouldFadeOut());
										emitter.setSpawnCount(templateEmitter.getSpawnCount());
										emitter.setSpawnInterval(templateEmitter.getSpawnInterval());
										emitter.setStartAlpha(templateEmitter.getStartAlpha());
										emitter.setStartRotation(templateEmitter.getStartRotation());
										emitter.setTightBounce(templateEmitter.isTightBounce());
										emitter.setVelocityX(templateEmitter.getVelocityX());
										emitter.setVelocityY(templateEmitter.getVelocityY());
										emitter.setWindFactor(templateEmitter.getWindFactor());
										emitter.setAdditive(templateEmitter.isAdditive());

										wrapper.setPosition(originalPosition.x, originalPosition.y);

										handler.write(wrappers);
									}
								}
							}
							catch (Exception e)
							{
								Debug.warn("Exception while applying new template to level " + levelID + ": " + e, e);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void customRender(float alphaFactor, Batch batch)
	{
		float maxedOutIgnoreAlpha = alphaFactor * 0.5f;
		boolean maxedOut = this.wrapper.getEmitter().isMaxOutParticles();

		this.limitToWrapper.render(alphaFactor, batch);
		this.circularEmitter.render(alphaFactor, batch);
		this.circularInversed.render(alphaFactor, batch);
		this.additiveMode.render(alphaFactor, batch);
		this.relativeAccelerations.render(alphaFactor, batch);
		this.limitedLifetime.render(alphaFactor, batch);
		this.wobbleMode.render(alphaFactor, batch);
		this.shouldBounceOutOfArea.render(alphaFactor, batch);
		this.tightBounce.render(alphaFactor, batch);
		this.scaleWithWidth.render(alphaFactor, batch);
		this.scaleWithHeight.render(alphaFactor, batch);
		this.scissorUp.render(alphaFactor, batch);
		this.scissorDown.render(alphaFactor, batch);
		this.scissorLeft.render(alphaFactor, batch);
		this.scissorRight.render(alphaFactor, batch);
		this.maxOutParticles.render(alphaFactor, batch);

		this.simpleName.render(alphaFactor, batch);
		this.depth.render(alphaFactor, batch);
		this.parallax.render(alphaFactor, batch);
		this.rotation.render(alphaFactor, batch);
		this.maxParticles.render(alphaFactor, batch);
		this.emitterLifetime.render(alphaFactor, batch);

		this.spawnIntervalMin.render(maxedOut ? maxedOutIgnoreAlpha : alphaFactor, batch);
		this.spawnIntervalMax.render(maxedOut ? maxedOutIgnoreAlpha : alphaFactor, batch);
		this.spawnCountMin.render(maxedOut ? maxedOutIgnoreAlpha : alphaFactor, batch);
		this.spawnCountMax.render(maxedOut ? maxedOutIgnoreAlpha : alphaFactor, batch);

		this.initialLifeMin.render(alphaFactor, batch);
		this.initialLifeMax.render(alphaFactor, batch);
		this.initialSizeMin.render(alphaFactor, batch);
		this.initialSizeMax.render(alphaFactor, batch);

		this.wobbleIntervalMin.render(alphaFactor, batch);
		this.wobbleIntervalMax.render(alphaFactor, batch);

		this.xOffsetMin.render(alphaFactor, batch);
		this.xOffsetMax.render(alphaFactor, batch);
		this.yOffsetMin.render(alphaFactor, batch);
		this.yOffsetMax.render(alphaFactor, batch);

		this.velocityXMin.render(alphaFactor, batch);
		this.velocityXMax.render(alphaFactor, batch);
		this.velocityYMin.render(alphaFactor, batch);
		this.velocityYMax.render(alphaFactor, batch);

		this.fadeInMin.render(alphaFactor, batch);
		this.fadeInMax.render(alphaFactor, batch);
		this.fadeOutMin.render(alphaFactor, batch);
		this.fadeOutMax.render(alphaFactor, batch);

		this.startRotationMin.render(alphaFactor, batch);
		this.startRotationMax.render(alphaFactor, batch);

		this.rotationVelocityMin.render(alphaFactor, batch);
		this.rotationVelocityMax.render(alphaFactor, batch);

		this.growthFactorMin.render(alphaFactor, batch);
		this.growthFactorMax.render(alphaFactor, batch);

		this.gravityFactorMin.render(alphaFactor, batch);
		this.gravityFactorMax.render(alphaFactor, batch);

		this.windFactorMin.render(alphaFactor, batch);
		this.windFactorMax.render(alphaFactor, batch);

		this.targetAlphaMin.render(alphaFactor, batch);
		this.targetAlphaMax.render(alphaFactor, batch);

		this.templateName.render(alphaFactor, batch);

		ShaderHandler.disableLighting();

		Renderer.setFont(Fonts.getSmallFont());

		Renderer.drawString("Name", this.xOffset + 5, this.simpleName.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Depth", this.xOffset + 5, this.depth.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Parallax", this.xOffset + 5, this.parallax.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Rotation", this.xOffset + 5, this.rotation.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Particles", this.xOffset + 5, this.maxParticles.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Emitter Lifetime", this.xOffset + 5, this.emitterLifetime.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Limit / Bounce / Tight", this.xOffset + 5, this.limitToWrapper.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("AutoScale W / H", this.xOffset + 5, this.scaleWithHeight.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Scissor T / B | L | R", this.xOffset + 5, this.scissorUp.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("MaxOutParticles", this.xOffset + 5, this.maxOutParticles.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Circular / Inversed", this.xOffset + 5, this.circularEmitter.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Additive", this.xOffset + 5, this.additiveMode.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Relative Accelerations", this.xOffset + 5, this.relativeAccelerations.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Limited Lifetime", this.xOffset + 5, this.limitedLifetime.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Wobble Mode", this.xOffset + 5, this.wobbleMode.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Wobble Interval", this.xOffset + 5, this.wobbleIntervalMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Spawn Interval", this.xOffset + 5, this.spawnIntervalMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Spawn Count", this.xOffset + 5, this.spawnCountMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Particle Lifetime", this.xOffset + 5, this.initialLifeMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Particle Size", this.xOffset + 5, this.initialSizeMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("xOffset", this.xOffset + 5, this.xOffsetMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("yOffset", this.xOffset + 5, this.yOffsetMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Velocity X", this.xOffset + 5, this.velocityXMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Velocity Y", this.xOffset + 5, this.velocityYMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Fade-In Time", this.xOffset + 5, this.fadeInMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Fade-Out Time", this.xOffset + 5, this.fadeOutMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Initial Rotation", this.xOffset + 5, this.startRotationMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Rotation Speed", this.xOffset + 5, this.rotationVelocityMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Growth", this.xOffset + 5, this.growthFactorMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Gravity", this.xOffset + 5, this.gravityFactorMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Wind", this.xOffset + 5, this.windFactorMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Transparency", this.xOffset + 5, this.targetAlphaMin.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Color Range", this.xOffset + 5, this.endColorButton.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Color Distr.", this.xOffset + 5, this.colorDistrModeButton.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Additive Color", this.xOffset + 5, this.addColorButton.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);
		Renderer.drawString("Save template: ", this.xOffset + 5, this.templateName.getCenterY() - Renderer.getLineHeight() / 2, 1.0f, batch);

		String usageText = this.wrapper.getMaxParticles() > 0 ? String.format("Used Particles: %5d (%3d%%)", this.wrapper.getEmitter().getParticleCount(), this.wrapper.getEmitter().getParticleCount() * 100 / this.wrapper.getEmitter().getMaxParticles()) : null;

		if (usageText != null)
		{
			Color usageColor = ColorHelper.buffer;

			float factor = 1.0f - (float) this.wrapper.getEmitter().getParticleCount() / (float) this.wrapper.getEmitter().getMaxParticles();

			usageColor.r = factor * 2;
			usageColor.g = (1.0f - factor) * 2;
			usageColor.b = 0.0f;
			usageColor.a = 0.5f;

			ShapeRenderer sr = Renderer.useShapeRenderer();

			sr.setColor(usageColor);
			sr.set(ShapeType.Filled);
			sr.rect(10, GameLauncher.getHeight() - Renderer.getLineHeight() - 10, Renderer.getWidth(usageText), Renderer.getLineHeight());

			Renderer.useSpriteBatch();
			batch.setColor(Color.WHITE);

			Renderer.drawString(usageText, Fonts.getBoldDefaultFont(), 10, GameLauncher.getHeight() - Renderer.getLineHeight() - 10, 1.0f, batch);
		}

		this.spawnHitbox.set(this.wrapper.getX() + this.wrapper.getHitbox().getRawWidth() * this.wrapper.getEmitter().getXOffset().minValue(), this.wrapper.getY() + this.wrapper.getHitbox().getRawHeight() * this.wrapper.getEmitter().getYOffset().minValue(), this.wrapper.getHitbox().getRawWidth() * (this.wrapper.getEmitter().getXOffset().maxValue() - this.wrapper.getEmitter().getXOffset().minValue()), this.wrapper.getHitbox().getRawHeight() * (this.wrapper.getEmitter().getYOffset().maxValue() - this.wrapper.getEmitter().getYOffset().minValue()));
		this.spawnHitbox.setRotation(this.wrapper.getRotation(), this.wrapper.getCenterOfRotationX() - this.spawnHitbox.getX(), this.wrapper.getCenterOfRotationY() - this.spawnHitbox.getY());

		ShapeRenderer sr = Renderer.useShapeRenderer();

		sr.setColor(ColorHelper.applyAlphaAndCopy(Color.GRAY, 0.1f));

		sr.set(ShapeType.Filled);
		sr.polygon(this.spawnHitbox.getPoints());

		sr.set(ShapeType.Line);
		sr.setColor(ColorHelper.applyAlphaAndCopy(Color.RED, 0.2f));
		sr.polygon(this.spawnHitbox.getPoints());

		this.startColor.render(1.0f, batch);
		this.startColorButton.render(1.0f, batch);

		Renderer.useShapeRenderer();
		sr.setColor(Color.BLACK);
		sr.set(ShapeType.Line);
		sr.rect(this.startColorButton.getButtonBox().x, this.startColorButton.getButtonBox().y, this.startColorButton.getButtonBox().width, this.startColorButton.getButtonBox().height);

		this.endColor.render(1.0f, batch);
		this.endColorButton.render(1.0f, batch);

		Renderer.useShapeRenderer();
		sr.setColor(Color.BLACK);
		sr.set(ShapeType.Line);
		sr.rect(this.endColorButton.getButtonBox().x, this.endColorButton.getButtonBox().y, this.endColorButton.getButtonBox().width, this.endColorButton.getButtonBox().height);

		this.addColor.render(1.0f, batch);
		this.addColorButton.render(1.0f, batch);

		Renderer.useShapeRenderer();
		sr.setColor(Color.BLACK);
		sr.set(ShapeType.Line);
		sr.rect(this.addColorButton.getButtonBox().x, this.addColorButton.getButtonBox().y, this.addColorButton.getButtonBox().width, this.addColorButton.getButtonBox().height);

		this.colorDistrModeButton.render(alphaFactor, batch);

		ShaderHandler.enableLighting();
	}

	@Override
	public void onPositionUpdate(int xChange, int yChange)
	{
		this.limitToWrapper.adjust(xChange, yChange);
		this.shouldBounceOutOfArea.adjust(xChange, yChange);
		this.tightBounce.adjust(xChange, yChange);
		this.circularEmitter.adjust(xChange, yChange);
		this.circularInversed.adjust(xChange, yChange);
		this.additiveMode.adjust(xChange, yChange);
		this.relativeAccelerations.adjust(xChange, yChange);
		this.limitedLifetime.adjust(xChange, yChange);
		this.wobbleMode.adjust(xChange, yChange);
		this.scaleWithWidth.adjust(xChange, yChange);
		this.scaleWithHeight.adjust(xChange, yChange);
		this.scissorUp.adjust(xChange, yChange);
		this.scissorDown.adjust(xChange, yChange);
		this.scissorLeft.adjust(xChange, yChange);
		this.scissorRight.adjust(xChange, yChange);
		this.maxOutParticles.adjust(xChange, yChange);

		this.simpleName.adjust(xChange, yChange);
		this.depth.adjust(xChange, yChange);
		this.parallax.adjust(xChange, yChange);
		this.rotation.adjust(xChange, yChange);
		this.maxParticles.adjust(xChange, yChange);
		this.emitterLifetime.adjust(xChange, yChange);

		this.spawnIntervalMin.adjust(xChange, yChange);
		this.spawnIntervalMax.adjust(xChange, yChange);
		this.spawnCountMin.adjust(xChange, yChange);
		this.spawnCountMax.adjust(xChange, yChange);

		this.initialLifeMin.adjust(xChange, yChange);
		this.initialLifeMax.adjust(xChange, yChange);
		this.initialSizeMin.adjust(xChange, yChange);
		this.initialSizeMax.adjust(xChange, yChange);

		this.wobbleIntervalMin.adjust(xChange, yChange);
		this.wobbleIntervalMax.adjust(xChange, yChange);

		this.xOffsetMin.adjust(xChange, yChange);
		this.xOffsetMax.adjust(xChange, yChange);
		this.yOffsetMin.adjust(xChange, yChange);
		this.yOffsetMax.adjust(xChange, yChange);

		this.velocityXMin.adjust(xChange, yChange);
		this.velocityXMax.adjust(xChange, yChange);
		this.velocityYMin.adjust(xChange, yChange);
		this.velocityYMax.adjust(xChange, yChange);

		this.fadeInMin.adjust(xChange, yChange);
		this.fadeInMax.adjust(xChange, yChange);
		this.fadeOutMin.adjust(xChange, yChange);
		this.fadeOutMax.adjust(xChange, yChange);

		this.startRotationMin.adjust(xChange, yChange);
		this.startRotationMax.adjust(xChange, yChange);

		this.rotationVelocityMin.adjust(xChange, yChange);
		this.rotationVelocityMax.adjust(xChange, yChange);

		this.growthFactorMin.adjust(xChange, yChange);
		this.growthFactorMax.adjust(xChange, yChange);

		this.gravityFactorMin.adjust(xChange, yChange);
		this.gravityFactorMax.adjust(xChange, yChange);

		this.windFactorMin.adjust(xChange, yChange);
		this.windFactorMax.adjust(xChange, yChange);

		this.targetAlphaMin.adjust(xChange, yChange);
		this.targetAlphaMax.adjust(xChange, yChange);

		this.startColorButton.adjust(xChange, yChange);
		this.endColorButton.adjust(xChange, yChange);
		this.addColorButton.adjust(xChange, yChange);
		this.colorDistrModeButton.adjust(xChange, yChange);
		this.templateName.adjust(xChange, yChange);
	}

	@Override
	protected void onClose()
	{
		super.onClose();

		this.startColor.disable();
		this.endColor.disable();
		this.addColor.disable();
	}

	@Override
	protected boolean subContains(float mouseX, float mouseY)
	{
		return super.subContains(mouseX, mouseY) || this.startColor.contains(mouseX, mouseY) || this.endColor.contains(mouseX, mouseY) || this.addColor.contains(mouseX, mouseY);
	}

	public boolean shouldLockInput()
	{
		return this.startColor.shouldLockInput() || this.endColor.shouldLockInput() || this.addColor.shouldLockInput() || this.simpleName.isFocused() || this.templateName.isFocused();
	}

	public UserInputLine getRotation()
	{
		return this.rotation;
	}

	public WrapperTexture getWrapper()
	{
		return this.wrapper;
	}
}

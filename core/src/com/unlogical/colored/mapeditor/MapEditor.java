package com.unlogical.colored.mapeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.audio.MusicManager;
import com.unlogical.colored.collision.Hitbox;
import com.unlogical.colored.configuration.DisplayConstants;
import com.unlogical.colored.debug.Debug;
import com.unlogical.colored.entity.Entity;
import com.unlogical.colored.entity.EntityType;
import com.unlogical.colored.entity.key.EntityKey;
import com.unlogical.colored.entity.key.ITileKeyInteractable;
import com.unlogical.colored.entity.key.KeyPort;
import com.unlogical.colored.filesystem.ConfigFileHandler;
import com.unlogical.colored.filesystem.FileManager;
import com.unlogical.colored.filesystem.files.EntityFileHandler;
import com.unlogical.colored.filesystem.files.TerrainFileHandler;
import com.unlogical.colored.filesystem.files.WrapperTextureHandler;
import com.unlogical.colored.gui.GUIItem;
import com.unlogical.colored.gui.button.Button;
import com.unlogical.colored.gui.button.ToggleButton;
import com.unlogical.colored.gui.menu.MenuHandler;
import com.unlogical.colored.gui.panel.CannonPanel;
import com.unlogical.colored.gui.panel.DoorPanel;
import com.unlogical.colored.gui.panel.EditWrapperPanel;
import com.unlogical.colored.gui.panel.GUIPanel;
import com.unlogical.colored.gui.panel.HolePanel;
import com.unlogical.colored.gui.panel.ImportWrapperPanel;
import com.unlogical.colored.gui.panel.LightPanel;
import com.unlogical.colored.gui.panel.MoveWrapperPanel;
import com.unlogical.colored.gui.panel.ParticleEmitterPanel;
import com.unlogical.colored.gui.panel.PlatformPanel;
import com.unlogical.colored.gui.panel.RenamePanel;
import com.unlogical.colored.gui.panel.SoundSourcePanel;
import com.unlogical.colored.gui.panel.TextPanel;
import com.unlogical.colored.gui.selection.EntitySelection;
import com.unlogical.colored.gui.selection.TerrainSelection;
import com.unlogical.colored.input.InputAction;
import com.unlogical.colored.input.InputHandler;
import com.unlogical.colored.level.Level;
import com.unlogical.colored.level.LevelType;
import com.unlogical.colored.levelmanaging.LevelManager;
import com.unlogical.colored.levelmanaging.MapManager;
import com.unlogical.colored.particle.ParticleEmitterImpl.IntegerRange;
import com.unlogical.colored.particle.ParticleSystem;
import com.unlogical.colored.profiling.Profiling;
import com.unlogical.colored.resources.font.Fonts;
import com.unlogical.colored.terrain.TerrainObject;
import com.unlogical.colored.terrain.TileType;
import com.unlogical.colored.terrain.TriggerPort;
import com.unlogical.colored.terrain.tile.Tile;
import com.unlogical.colored.terrain.tile.TileCameraControl;
import com.unlogical.colored.terrain.tile.TileDoor;
import com.unlogical.colored.terrain.tile.TileHole;
import com.unlogical.colored.terrain.tile.TileLight;
import com.unlogical.colored.terrain.tile.TileSoundSource;
import com.unlogical.colored.terrain.tile.TileText;
import com.unlogical.colored.terrain.tile.cage.TileCage;
import com.unlogical.colored.terrain.tile.cannon.ICannonBall;
import com.unlogical.colored.terrain.tile.cannon.TileCannon;
import com.unlogical.colored.terrain.tile.platform.TilePlatform;
import com.unlogical.colored.terrain.tile.properties.ITileReceiver;
import com.unlogical.colored.terrain.tile.properties.ITileTriggerable;
import com.unlogical.colored.util.ColorHelper;
import com.unlogical.colored.util.CursorType;
import com.unlogical.colored.util.Dimension;
import com.unlogical.colored.util.GameStatus;
import com.unlogical.colored.util.ICopyable;
import com.unlogical.colored.util.Renderer;
import com.unlogical.colored.util.ShaderHandler;
import com.unlogical.colored.util.ZHandler;
import com.unlogical.colored.wrapper.WrapperTexture;

public class MapEditor
{
	private static final EntityType[] entityList = new EntityType[] { EntityType.GOOMBA, EntityType.KEY, EntityType.STICKYKEY, EntityType.SHADOWBALL, EntityType.ANVIL, EntityType.ICE_SPIKE, EntityType.MINI_ICE_SPIKES, EntityType.SCIENTIST, EntityType.BOSS };

	public static final Color panelColor = new Color(0.1f, 0.1f, 0.3f, 1.0f);
	private static final Color portBoxBorderColor = Color.BLACK;
	private static final Color selectionColor = new Color(Color.ORANGE);

	private static final Vector2 buffer = new Vector2();
	private static final Vector2 secondBuffer = new Vector2();
	private static final Vector2 selectionStartPoint = new Vector2();

	private static final float PORT_COLOR_BOX_OFFSET = 0.45f;
	private static final float PORT_COLOR_BOX_SIZE = 0.35f;

	private static float alphaFactor = 1.0f;

	private static final int MAX_CONCATENATION_RANGE = 4;

	private static float rotationCenterX;
	private static float rotationCenterY;

	private static int[] lastSelectedBlock = new int[2];
	private static int leaveCooldown;
	private static int clickCooldown;
	private static int entityCooldown;
	private static int colorChangeCooldown;
	private static int movingTime;

	private static boolean useLineHelper;
	private static boolean init;
	private static boolean paused;
	private static boolean movingTiles;
	private static boolean movingWrappers;
	private static boolean movingEntities;
	private static boolean smoothMode;
	private static boolean lockHorizontal;
	private static boolean lockVertical;
	private static boolean selecting;
	private static boolean displaySelectedArea;
	private static boolean increasingSelection;
	private static boolean renderDebugTiles;

	private static Level level;

	private static String mapPath;
	private static String mapName;
	private static String mapID;
	private static String currentEntityType;

	private static Tile currentTile;

	private static ArrayList<Entity> entities = new ArrayList<Entity>();
	private static ArrayList<Tile> tiles = new ArrayList<Tile>();
	private static ArrayList<WrapperTexture> wrappers = new ArrayList<WrapperTexture>();

	private static Set<ICopyable<?>> hoveredObjects = new HashSet<ICopyable<?>>();
	private static Set<Entity> selectedEntities = new HashSet<Entity>();
	private static Set<Tile> selectedTiles = new HashSet<Tile>();
	private static Set<WrapperTexture> selectedWrappers = new HashSet<WrapperTexture>();
	private static Set<ICopyable<?>> selectedObjects = new HashSet<ICopyable<?>>();
	private static Set<ICopyable<?>> clipboard = new HashSet<ICopyable<?>>();

	private static TerrainSelection terrainSelection;
	private static EntitySelection entitySelection;

	private static TriggerPort lastTriggerPort;
	private static KeyPort lastKeyPort;

	private static GUIItem[] terrainItems;
	private static GUIItem[] entityItems;

	private static Visibility currentVisibilty = Visibility.ALL;
	private static Rectangle selectedArea = new Rectangle(0, 0, 0, 0);
	private static Hitbox bufferedHitbox = new Hitbox(0, 0, 0, 0);

	private static Button saveButton;
	private static Button reloadButton;
	private static Button levelTypeButton;
	private static Button addParticleWrapperButton;
	private static Button tileChooserButton;
	private static Button entityChooserButton;
	private static Button visibiltyButton;
	private static Button addWrapperButton;
	private static ToggleButton toggleLightsButton;

	private static GUIPanel controlPanel;
	private static EditWrapperPanel editWrapperPanel;
	private static ParticleEmitterPanel editEmitterPanel;
	private static MoveWrapperPanel moveWrapperPanel;
	private static ImportWrapperPanel importPanel;
	private static DoorPanel doorPanel;
	private static TextPanel textPanel;
	private static PlatformPanel platformPanel;
	private static CannonPanel cannonPanel;
	private static LightPanel lightPanel;
	private static SoundSourcePanel soundSourcePanel;
	private static HolePanel holePanel;
	private static RenamePanel renamePanel;

	public static void init()
	{
		if (!init)
		{
			InputHandler.addInputProcessor(new InputAdapter()
			{
				@Override
				public boolean scrolled(int change)
				{
					change = -change;
					float changeFactor = change;

					for (WrapperTexture wrapper : selectedWrappers)
					{
						if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
						{
							if (!wrapper.isParticleWrapper() && wrapper.hasFoundTextures())
							{
								float beforeWidth = wrapper.getHitbox().getWidth();
								float beforeHeight = wrapper.getHitbox().getHeight();

								float scaleChange = (smoothMode ? 0.01f : 0.02f) * changeFactor;

								if (wrapper.getScale() + scaleChange > 0.1f && wrapper.getScale() + scaleChange < 5.0f)
								{
									boolean uniformScale = (int) wrapper.getHitbox().getRawWidth() == (int) (wrapper.getImage().getRegionWidth() * wrapper.getScale()) && (int) wrapper.getHitbox().getRawHeight() == (int) (wrapper.getImage().getRegionHeight() * wrapper.getScale());

									wrapper.setScale(wrapper.getScale() + scaleChange);

									if (uniformScale)
									{
										wrapper.setWidth((int) (wrapper.getImage().getRegionWidth() * wrapper.getScale()));
										wrapper.setHeight((int) (wrapper.getImage().getRegionHeight() * wrapper.getScale()));
									}
								}

								wrapper.adjustX(-(wrapper.getHitbox().getWidth() - beforeWidth) / 2.0f);
								wrapper.adjustY(-(wrapper.getHitbox().getHeight() - beforeHeight) / 2.0f);

								wrapper.updateBorders();
							}
						}
						else if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT))
						{
							float parallaxChange = changeFactor * (smoothMode ? 0.01f : 0.02f);

							if (wrapper.getEmitterParallax() + parallaxChange > 0.0f)
							{
								wrapper.setParallax(wrapper.getEmitterParallax() + parallaxChange);

								wrapper.setParallax(Math.round(wrapper.getEmitterParallax() * 100) / 100.0f);
							}
							else
							{
								wrapper.setParallax(0.0f);
							}
						}
						else
						{
							if (wrapper.isInForeground())
							{
								wrapper.setDepth(wrapper.getDepth() - change);
							}
							else
							{
								wrapper.setDepth(wrapper.getDepth() + change);
							}

							if (wrapper.getDepth() < 0)
							{
								wrapper.setInBackground(!wrapper.isInBackground());
								wrapper.setDepth(0);
							}
						}

						checkWrapper(wrapper);

						if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT))
						{
							level.sortWrappers();
						}
					}

					for (Tile tile : selectedTiles)
					{
						if (change > 0)
						{
							for (int i = 0; i < tiles.size(); i++)
							{
								if (tile == tiles.get(i) && i >= 1)
								{
									Tile prevTile = tiles.get(i - 1);

									tiles.remove(i - 1);
									tiles.add(i, prevTile);

									break;
								}
							}
						}
						else
						{
							for (int i = 0; i < tiles.size(); i++)
							{
								if (tile == tiles.get(i) && i < tiles.size() - 1)
								{
									Tile nextTile = tiles.get(i + 1);

									tiles.remove(i);
									tiles.remove(i);

									tiles.add(i, nextTile);
									tiles.add(i + 1, tile);

									break;
								}
							}
						}
					}

					return false;
				}

				@Override
				public boolean touchDown(int screenX, int screenY, int pointer, int button)
				{
					Vector2 pos = Renderer.unproject(screenX, screenY);

					screenX = (int) pos.x;
					screenY = (int) pos.y;

					if (button == Input.Buttons.LEFT)
					{
						for (WrapperTexture wrapper : wrappers)
						{
							wrapper.mousePressed(screenX, screenY);
						}
					}

					return false;
				}

				@Override
				public boolean touchDragged(int newX, int newY, int pointer)
				{
					Vector2 pos = Renderer.unproject(newX, newY);

					newX = (int) pos.x;
					newY = (int) pos.y;

					boolean rotationMode = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && !Gdx.input.isButtonPressed(Input.Buttons.LEFT);
					float xChange = newX - InputHandler.getLastInputX();
					float yChange = newY - InputHandler.getLastInputY();
					float scale = smoothMode ? 0.5f : 1.0f;

					if (rotationMode)
					{
						for (ICopyable<?> object : selectedObjects)
						{
							if (object instanceof WrapperTexture || object instanceof Tile && ((Tile) object).getProperties().isRotatable())
							{
								rotationCenterX = object.getCenterOfRotationX();
								rotationCenterY = object.getCenterOfRotationY();

								break;
							}
						}
					}

					boolean anyOwnsMouse = false;

					for (ICopyable<?> object : selectedObjects)
					{
						if (object.ownsMouse())
						{
							anyOwnsMouse = true;

							break;
						}
					}

					if (!anyContains(InputHandler.getLastInputX(), InputHandler.getLastInputY()))
					{
						if (!selecting)
						{
							for (WrapperTexture wrapper : selectedWrappers)
							{
								if (anyOwnsMouse && !wrapper.ownsMouse())
								{
									continue;
								}

								if (movingTiles || movingWrappers || movingEntities)
								{
									if (!lockVertical)
									{
										wrapper.adjustX(xChange * scale);
									}

									if (!lockHorizontal)
									{
										wrapper.adjustY(yChange * scale);
									}

									if (wrapper.isParticleWrapper())
									{
										wrapper.getEmitter().moveEmitter(xChange * scale, yChange * scale);

										if (wrapper.getEmitter().hasMirror())
										{
											wrapper.getEmitter().getMirroredEmitter().moveEmitter(xChange * scale, yChange * scale);
										}
									}
								}
								else if (rotationMode)
								{
									float a, b, c, oldTheta, newTheta;

									a = buffer.set(rotationCenterX, rotationCenterY).sub(secondBuffer.set(InputHandler.getLastInputX(), InputHandler.getLastInputY())).len();

									oldTheta = buffer.angle();

									b = buffer.set(rotationCenterX, rotationCenterY).sub(secondBuffer.set(newX, newY)).len();

									newTheta = buffer.angle();

									c = buffer.set(InputHandler.getLastInputX(), InputHandler.getLastInputY()).sub(secondBuffer.set(newX, newY)).len();

									float angleChange = (float) Math.toDegrees(Math.acos((a * a + b * b - c * c) / (2 * a * b))) * (newTheta - oldTheta > 0 ? 1 : -1);

									if (!Float.isNaN(angleChange) && !Float.isInfinite(angleChange))
									{
										wrapper.setRotation(wrapper.getRotation() + angleChange);

										if (wrapper.hasBorders())
										{
											wrapper.updateBorders();
										}

										if (wrapper.isParticleWrapper())
										{
											wrapper.getEmitter().rotate(angleChange);

											if (wrapper.getEmitter().hasMirror())
											{
												wrapper.getEmitter().getMirroredEmitter().rotate(angleChange);
											}
										}

										checkWrapper(wrapper);

										if (wrapper.isParticleWrapper() && !wrapper.getEmitter().isRotateAccelerations())
										{
											level.getParticleSystem().simulateEmitter(wrapper.getEmitter(), 50000, 100);

											if (wrapper.getEmitter().hasMirror())
											{
												level.getParticleSystem().simulateEmitter(wrapper.getEmitter().getMirroredEmitter(), 50000, 100);
											}
										}
									}
									else
									{
										Debug.warn("Angle change for rotating via mouse-click is " + angleChange + ". That shouldn't happen. Did you click right in the center? (all your fault! ;))");
									}
								}

								if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
								{
									if (wrapper.isParticleWrapper() && wrapper.getEmitter().getOriginalWidth() == 0)
									{
										wrapper.updateEmitterOriginals();
									}

									float beforeWidth = wrapper.getWidth();
									float beforeHeight = wrapper.getHeight();
									float beforeCenterX = wrapper.getCenterOfRotationX();
									float beforeCenterY = wrapper.getCenterOfRotationY();

									wrapper.mouseDragged(InputHandler.getLastInputX(), InputHandler.getLastInputY(), newX, newY, xChange * scale, yChange * scale);

									if (wrapper.isParticleWrapper())
									{
										if (!wrapper.getEmitter().isCircularEmitter())
										{
											IntegerRange spawnCount = wrapper.getEmitter().getSpawnCount();

											if (wrapper.getEmitter().isScaleWithWidth() && wrapper.getWidth() != beforeWidth || wrapper.getEmitter().isScaleWithHeight() && wrapper.getHeight() != beforeHeight)
											{
												float particleScale = 1.0f;

												if (wrapper.getEmitter().isScaleWithWidth() && wrapper.getEmitter().isScaleWithHeight())
												{
													particleScale = (float) (wrapper.getEmitterWidth() * wrapper.getEmitterHeight()) / (float) (wrapper.getEmitter().getOriginalHeight() * wrapper.getEmitter().getOriginalWidth());
												}
												else if (wrapper.getEmitter().isScaleWithWidth())
												{
													particleScale = (float) wrapper.getEmitterWidth() / (float) wrapper.getEmitter().getOriginalWidth();
												}
												else if (wrapper.getEmitter().isScaleWithHeight())
												{
													particleScale = (float) wrapper.getEmitterHeight() / (float) wrapper.getEmitter().getOriginalHeight();
												}

												if (particleScale != 1.0f)
												{
													wrapper.setMaxParticles((int) (wrapper.getEmitter().getOriginalMaxParticles() * particleScale));
													spawnCount.setMinValue(Math.round(wrapper.getEmitter().getOriginalMinCount() * particleScale));
													spawnCount.setMaxValue(Math.round(wrapper.getEmitter().getOriginalMaxCount() * particleScale));
													wrapper.recreateEmitter();
												}

												level.getParticleSystem().simulateEmitter(wrapper.getEmitter(), 25000, 100);

												if (wrapper.getEmitter().hasMirror())
												{
													level.getParticleSystem().simulateEmitter(wrapper.getEmitter().getMirroredEmitter(), 25000, 100);
												}
											}
										}
										else if (wrapper.getWidth() != beforeWidth || wrapper.getHeight() != beforeHeight)
										{
											wrapper.getEmitter().moveParticles(wrapper.getCenterOfRotationX() - beforeCenterX, wrapper.getCenterOfRotationY() - beforeCenterY);
										}
									}

									checkWrapper(wrapper);
								}
							}
						}

						boolean changedCannon = false;

						for (Tile tile : selectedTiles)
						{
							if (anyOwnsMouse && !tile.ownsMouse())
							{
								continue;
							}

							boolean beforeLeftConnected = tile.getHitbox().isConnectedLeft();
							boolean beforeRightConnected = tile.getHitbox().isConnectedRight();

							if (!selecting)
							{
								if ((movingTiles || movingWrappers || movingEntities) && Gdx.input.isButtonPressed(Input.Buttons.LEFT))
								{
									if (!lockVertical)
									{
										tile.adjustX(xChange * scale);
									}

									if (!lockHorizontal)
									{
										tile.adjustY(yChange * scale);
									}

									tile.updateBorders();

									if (tile.getDimension() == Dimension.COLORED && tile.hasMirror())
									{
										Tile mirror = tile.getMirroredTile();

										if (!lockVertical)
										{
											mirror.adjustX(xChange * scale);
										}

										if (!lockHorizontal)
										{
											mirror.adjustY(yChange * scale);
										}

										tile.initMirror(mirror, level.getLevelType());

										mirror.updateBorders();
									}
								}
								else if (rotationMode && tile.getProperties().isRotatable())
								{
									float a, b, c, oldTheta, newTheta;

									a = buffer.set(rotationCenterX, rotationCenterY).sub(secondBuffer.set(InputHandler.getLastInputX(), InputHandler.getLastInputY())).len();

									oldTheta = buffer.angle();

									b = buffer.set(rotationCenterX, rotationCenterY).sub(secondBuffer.set(newX, newY)).len();

									newTheta = buffer.angle();

									c = buffer.set(InputHandler.getLastInputX(), InputHandler.getLastInputY()).sub(secondBuffer.set(newX, newY)).len();

									float angleChange = (float) Math.toDegrees(Math.acos((a * a + b * b - c * c) / (2 * a * b))) * (newTheta - oldTheta > 0 ? 1 : -1);

									if (!Float.isNaN(angleChange) && !Float.isInfinite(angleChange))
									{
										rotateTile(tile, angleChange);
									}
									else
									{
										Debug.warn("Angle change for rotating via mouse-click is " + angleChange + ". That shouldn't happen. Did you click right in the center? (all your fault! ;))");
									}
								}

								if (Gdx.input.isButtonPressed(Input.Buttons.LEFT))
								{
									float oldWidth = tile.getHitbox().getRawWidth();
									float oldHeight = tile.getHitbox().getRawHeight();

									tile.mouseDragged(InputHandler.getLastInputX(), InputHandler.getLastInputY(), newX, newY, xChange * scale, yChange * scale);

									if (oldWidth != tile.getHitbox().getRawWidth() || oldHeight != tile.getHitbox().getRawHeight())
									{
										if (tile.isShared())
										{
											tile.setShared(false);
											tile.setShared(true);
										}

										if (!tile.isTimeEffected())
										{
											tile.setTimeEffected(true);
											tile.setTimeEffected(false);
										}

										if (tile.blocksDimensionTravel())
										{
											tile.setBlocksDimensionTravel(false);
											tile.setBlocksDimensionTravel(true);
										}

										if (tile.getDimension() == Dimension.COLORED && tile.hasMirror())
										{
											Tile mirror = tile.getMirroredTile();

											float rotation = tile.getRotation();

											if (rotation != 0.0f)
											{
												tile.setRotation(0.0f);
												mirror.setRotation(0.0f);
											}

											mirror.setPosition(tile.getPosition().x, tile.getPosition().y);
											mirror.setWidth(tile.getHitbox().getWidth());
											mirror.setHeight(tile.getHitbox().getHeight());

											if (rotation != 0.0f)
											{
												tile.setRotation(rotation);
												mirror.setRotation(rotation);
											}
										}
									}
								}
							}

							tile.init();

							if (beforeLeftConnected ^ tile.getHitbox().isConnectedLeft())
							{
								tile.getLeftConnectedTile().init();

								if (!tile.getHitbox().isConnectedLeft())
								{
									tile.getLeftConnectedTile().disconnectRight(tile);

									tile.getLeftConnectedTile().init();
								}
							}

							if (beforeRightConnected ^ tile.getHitbox().isConnectedRight())
							{
								tile.getRightConnectedTile().init();

								if (!tile.getHitbox().isConnectedRight())
								{
									tile.getRightConnectedTile().disconnectLeft(tile);

									tile.getRightConnectedTile().init();
								}
							}

							if (tile instanceof TileCameraControl)
							{
								MapManager.initLevel(null, level);
							}

							if (tile == cannonPanel.getCannon())
							{
								changedCannon = true;
							}
						}

						if (changedCannon)
						{
							cannonPanel.updateLine();
						}

						if (!selecting && (movingTiles || movingWrappers || movingEntities))
						{
							for (Entity entity : selectedEntities)
							{
								if (!lockVertical)
								{
									entity.adjustX(xChange * scale);
								}

								if (!lockHorizontal)
								{
									entity.adjustY(yChange * scale);
								}

								if (entity.hasMirror())
								{
									entity.getMirroredEntity().adjustX(lockVertical ? 0.0f : xChange * scale);
									entity.getMirroredEntity().adjustY(lockHorizontal ? 0.0f : yChange * scale);
								}
							}
						}

						if (selecting)
						{
							selectedArea.set(Math.min(selectionStartPoint.x, newX), Math.min(selectionStartPoint.y, newY), Math.max(selectionStartPoint.x, newX) - Math.min(selectionStartPoint.x, newX), Math.max(selectionStartPoint.y, newY) - Math.min(selectionStartPoint.y, newY));
						}
					}

					return false;
				}

				@Override
				public boolean touchUp(int screenX, int screenY, int pointer, int button)
				{
					if (button == Input.Buttons.LEFT)
					{
						for (WrapperTexture wrapper : wrappers)
						{
							wrapper.mouseReleased(screenX, screenY);
						}
					}

					return false;
				}
			});

			level = new Level();
			level.createParticleSystem();
			level.initID(mapID);

			initGUI();

			init = true;
		}
	}

	public static void resetGUI()
	{
		controlPanel.setTitle(mapName + " (" + mapID + ")");

		importPanel.setPosition((int) (GameLauncher.getWidth() / 2 - importPanel.getWidth() / 2), (int) (GameLauncher.getHeight() / 2 - importPanel.getHeight() / 2));

		levelTypeButton.setText("Type: " + level.getLevelType().name());
		importPanel.setActive(false);
		editWrapperPanel.setActive(false);
		editEmitterPanel.setActive(false);
		moveWrapperPanel.setActive(false);
		doorPanel.setActive(false);
		textPanel.setActive(false);
		platformPanel.setActive(false);
		soundSourcePanel.setActive(false);
		holePanel.setActive(false);
		lightPanel.setActive(false);
		renamePanel.setActive(false);
		cannonPanel.setActive(false);
	}

	public static void initGUI()
	{
		ArrayList<GUIItem> cannonList = new ArrayList<GUIItem>();

		terrainItems = new GUIItem[TileType.values().length];

		for (int i = 0; i < TileType.values().length; i++)
		{
			terrainItems[i] = new GUIItem(TileType.values()[i].getTileName(), TerrainObject.getMainImage(TileType.values()[i]) == null ? null : TerrainObject.getMainImage(TileType.values()[i]), 1, 0, 0, 50, 50);

			Tile tile = Tile.createTile(TileType.values()[i], "", 0, 0.0f, buffer, level, Dimension.COLORED, TileType.values()[i].getProperties(), true);

			if (tile instanceof ICannonBall)
			{
				terrainItems[i].setMetadata(TileType.values()[i].getTileName());
				cannonList.add(terrainItems[i].copy());
			}
		}

		entityItems = new GUIItem[entityList.length];
		for (int i = 0; i < entityList.length; i++)
		{
			entityItems[i] = new GUIItem(entityList[i].getTypeName(), Entity.getMainImage(entityList[i]), 1, 0, 0, 50, 50);

			Entity entity = Entity.createEntity(entityList[i], buffer, level, Dimension.COLORED, false);

			if (entity instanceof ICannonBall)
			{
				entityItems[i].setMetadata(entityList[i].getTypeName());
				cannonList.add(entityItems[i].copy());
			}
		}

		terrainSelection = new TerrainSelection(terrainItems, 4, 6, 0, 0);
		terrainSelection.setXOffset(1920 - terrainSelection.getWidth() - 20);
		terrainSelection.setYOffset(50);

		entitySelection = new EntitySelection(entityItems, 4, 2, 0, 0);
		entitySelection.setXOffset(1920 - terrainSelection.getWidth() - 20);
		entitySelection.setYOffset(terrainSelection.getYOffset() + terrainSelection.getHeight() + 40);

		importPanel = new ImportWrapperPanel("Add Wrappers", 0, 0, 1000, 800);
		editWrapperPanel = new EditWrapperPanel(1920 - 410, 35, 400, 0);
		editEmitterPanel = new ParticleEmitterPanel(1920 - 410, 35, 400, 0);
		moveWrapperPanel = new MoveWrapperPanel("Change Wrapper Path", 1920 / 2 - 250, 1080 / 2 - 100, 500, 0);
		doorPanel = new DoorPanel("Configure Door", 1920 - 510, 35, 500, 0);
		textPanel = new TextPanel(1920 - 600, 1080 - 400, 600, 400);
		platformPanel = new PlatformPanel("Configure Platform", 1920 - 410, 35, 400, 0);
		lightPanel = new LightPanel(1920 - 400, 35, 400, 0);
		soundSourcePanel = new SoundSourcePanel("Configure SoundSource", 1920 - 500, 35, 500, 0);
		holePanel = new HolePanel("Configure TileHole", 1920 - 500, 35, 500, 0);
		renamePanel = new RenamePanel(1920 / 2 - 250, 1080 / 2 - 250, 500, 0);

		GUIItem[] cannonItemArray = new GUIItem[cannonList.size()];

		for (int i = 0; i < cannonList.size(); i++)
		{
			cannonItemArray[i] = cannonList.get(i);
		}

		cannonPanel = new CannonPanel(cannonItemArray, "Edit Cannon", 1920 - 420, 500, 400, 0);

		int boxWidth = 372;
		int boxHeight = 0;

		int buttonWidth = 366;
		int buttonHeight = 40;

		int currentOffset = 45;

		controlPanel = new GUIPanel("Map: " + mapName + " (" + mapID + ")", 10, 35, boxWidth, boxHeight)
		{
			@Override
			public void onPositionUpdate(int xChange, int yChange)
			{
				saveButton.adjust(xChange, yChange);
				reloadButton.adjust(xChange, yChange);
				levelTypeButton.adjust(xChange, yChange);
				tileChooserButton.adjust(xChange, yChange);
				entityChooserButton.adjust(xChange, yChange);
				addParticleWrapperButton.adjust(xChange, yChange);
				addWrapperButton.adjust(xChange, yChange);
				visibiltyButton.adjust(xChange, yChange);
				toggleLightsButton.adjust(xChange, yChange);
			}
		};
		controlPanel.setFillColor(panelColor);
		controlPanel.setActive(true);

		saveButton = new Button("Save", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth / 2 - 3, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				saveItAll();
			}
		};

		reloadButton = new Button("Reload", (int) (controlPanel.getCenterX() - buttonWidth / 2) + buttonWidth / 2 + 6, currentOffset, buttonWidth / 2 - 6, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				reload();
			}
		};

		currentOffset += reloadButton.getHeight() + 5;

		levelTypeButton = new Button("", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				level.setLevelType(level.getLevelType().next());

				saveItAll();

				reloadButton.onClick();
			}
		};

		currentOffset += reloadButton.getHeight() + 10;

		visibiltyButton = new Button("", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				toggleVisibilty();
			}
		};

		currentOffset += visibiltyButton.getHeight() + 5;

		toggleLightsButton = new ToggleButton("Lighting: ON", "Lighting: OFF", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth, buttonHeight)
		{
			@Override
			protected void toggledToToggledState()
			{
				ShaderHandler.disable();
			}

			@Override
			protected void toggledToInitialState()
			{
				ShaderHandler.enable();
			}
		};

		currentOffset += toggleLightsButton.getHeight() + 10;

		tileChooserButton = new Button("Add Tile", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				currentTile = null;
				terrainSelection.setActive(!terrainSelection.isActive());

				if (terrainSelection.isActive())
				{
					editWrapperPanel.setActive(false);
					editEmitterPanel.setActive(false);
					moveWrapperPanel.setActive(false);
				}
			}
		};

		currentOffset += tileChooserButton.getHeight() + 5;

		entityChooserButton = new Button("Add Entity", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				entitySelection.setActive(!entitySelection.isActive());
				currentEntityType = null;
			}
		};

		currentOffset += entityChooserButton.getHeight() + 5;

		addParticleWrapperButton = new Button("Add Particle Emitter", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				importPanel.setTitle("Add Particle Emitter");
				importPanel.setParticleCreationMode(true);

				try
				{
					importPanel.refresh();
				}
				catch (IOException e)
				{
					Debug.logStackTrace(e);
				}

				importPanel.setActive(true);

				currentTile = null;
				currentEntityType = null;
				terrainSelection.clearSelection();
				entitySelection.clearSelection();
				deselectAll();
			}
		};

		currentOffset += addParticleWrapperButton.getHeight() + 5;

		addWrapperButton = new Button("Add Wrappers", (int) (controlPanel.getCenterX() - buttonWidth / 2), currentOffset, buttonWidth, buttonHeight, false)
		{
			@Override
			public void onClick()
			{
				importPanel.setTitle("Add Wrapper");
				importPanel.setParticleCreationMode(false);

				try
				{
					importPanel.refresh();
				}
				catch (IOException e)
				{
					Debug.logStackTrace(e);
				}

				importPanel.setActive(true);

				currentTile = null;
				currentEntityType = null;
				terrainSelection.clearSelection();
				entitySelection.clearSelection();
				terrainSelection.setActive(false);
				deselectAll();
			}
		};

		currentOffset += addWrapperButton.getHeight();
		boxHeight = currentOffset;

		controlPanel.setHeight(boxHeight + 20);
	}

	public static void initLevel(String mapID, String mapPath)
	{
		MapEditor.mapID = mapID;
		MapEditor.mapPath = mapPath;

		if (GameLauncher.getLevelContext() != null)
		{
			GameLauncher.getLevelContext().getSoundManager().stopAll();
		}

		long beforeTime = System.currentTimeMillis();

		ZHandler.clearDistortions();

		level = new Level();
		level.createParticleSystem();
		level.initID(mapID);

		try
		{
			init();
		}
		catch (Exception e)
		{
			Debug.warnStackTrace(e);

			throw new RuntimeException("Error initialising mapeditor : " + e);
		}

		readConfig();

		resetGUI();

		selectedTiles.clear();
		selectedObjects.clear();

		lastTriggerPort = TriggerPort.YELLOW;

		TerrainFileHandler terrainFile = new TerrainFileHandler(FileManager.getFileQuietly(mapPath + "/terrain.map"));
		EntityFileHandler entityFile = new EntityFileHandler(FileManager.getFileQuietly(mapPath + "/entities.map"));

		try
		{
			entities.clear();
			entities = entityFile.read(level);
			tiles = terrainFile.read(level);
		}
		catch (IOException e)
		{
			Debug.logStackTrace(e);
		}

		wrappers.clear();
		selectedWrappers.clear();
		selectedObjects.clear();
		selectedTiles.clear();

		editWrapperPanel.init(null);
		editEmitterPanel.init(null);
		moveWrapperPanel.init(null);

		MapManager.initLevel(null, level);

		try
		{
			WrapperTextureHandler handler = new WrapperTextureHandler(FileManager.getFile(mapPath + "/wrappers.cfg"));

			wrappers = handler.read(level);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error loading wrappers: " + e, e);
		}

		level.init(mapID, mapName, tiles, wrappers, entities, new Vector2(0.0f, 0.0f), null);

		for (Tile tile : tiles)
		{
			tile.init();
		}

		for (Entity entity : entities)
		{
			entity.init();
		}

		if (currentTile instanceof Tile)
		{
			currentTile = Tile.createTile(currentTile.getType(), currentTile.getMetadata(), currentTile.getInnerType(), currentTile.getRotation(), currentTile.getPosition(), level, Dimension.COLORED, currentTile.getProperties(), true);
		}

		level.initBrightness(1.0f);
		level.transferBuffers();

		ShaderHandler.initlevel(level);
		MusicManager.initLevel(level);

		GameLauncher.setLevelContext(level);
		GameLauncher.setStatus(GameStatus.MAP_EDITOR);

		initVisibilty();

		level.initRNGs(1337);

		level.getPRNG(false, Dimension.COLORED).nextLong(); // seed rng

		simulateParticles(45000, 150);

		Debug.log("Loaded map " + mapName + " (" + mapID + ", " + mapPath + "), took " + (System.currentTimeMillis() - beforeTime) + "ms.");

		resume();

		System.gc();

		leaveCooldown = 500;
	}

	private static void checkWrapper(WrapperTexture wrapper)
	{
		if (wrapper == editWrapperPanel.getWrapper())
		{
			editWrapperPanel.init(wrapper);
		}
		else if (wrapper == editEmitterPanel.getWrapper())
		{
			editEmitterPanel.init(wrapper);
		}
	}

	private static void simulateParticles(int totalTime, int preferredDelta)
	{
		level.setSimulating(true);

		while (totalTime > 0)
		{
			int delta = totalTime > preferredDelta ? preferredDelta : totalTime;

			level.updateParticleSystem(delta, delta, delta);

			totalTime -= delta;
		}

		level.setSimulating(false);
	}

	private static void toggleVisibilty()
	{
		setVisibility(Visibility.values()[currentVisibilty.ordinal() + 1 >= Visibility.values().length ? 0 : currentVisibilty.ordinal() + 1]);
	}

	private static void setVisibility(Visibility visibilty)
	{
		currentVisibilty = visibilty;

		visibiltyButton.setText("Visible: " + visibilty.name());

		level.setRenderEntities(false);
		level.setRenderTiles(false);
		level.setRenderWrappers(false);
		level.setRenderParticleEmitters(false);
		level.setRenderForegroundWrappers(false);
		level.setRenderBackgroundWrappers(false);

		renderDebugTiles = true;

		if (currentVisibilty == Visibility.ALL || currentVisibilty == Visibility.IN_GAME)
		{
			level.setRenderEntities(true);
			level.setRenderTiles(true);
			level.setRenderWrappers(true);
			level.setRenderParticleEmitters(true);

			if (currentVisibilty == Visibility.IN_GAME)
			{
				renderDebugTiles = false;
			}
		}
		else if (currentVisibilty == Visibility.FOREGROUND)
		{
			level.setRenderForegroundWrappers(true);
		}
		else if (currentVisibilty == Visibility.BACKGROUND)
		{
			level.setRenderBackgroundWrappers(true);
		}
		else if (currentVisibilty == Visibility.COLLISION)
		{
			level.setRenderTiles(true);
			level.setRenderEntities(true);
		}
		else if (currentVisibilty == Visibility.PARTICLES)
		{
			level.setRenderParticleEmitters(true);
		}
	}

	private static void initVisibilty()
	{
		setVisibility(currentVisibilty);
	}

	public static void render(Batch batch)
	{
		boolean renderBGWrappersBefore = level.shouldRenderBackgroundWrappers();
		boolean renderFGWrappersBefore = level.shouldRenderForegroundWrappers();
		boolean renderEntitiesBefore = level.shouldRenderEntities();
		boolean renderEmittersBefore = level.shouldRenderParticleEmitters();
		boolean renderAllBefore = level.shouldRenderAll();
		boolean renderDebugTilesBefore = renderDebugTiles;

		Renderer.setSaturation(1.0f);

		if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && !isLockingInput())
		{
			level.setRenderTiles(true);
			level.setRenderBackgroundWrappers(true);
			level.setRenderForegroundWrappers(true);
			level.setRenderEntities(true);
			level.setRenderParticleEmitters(true);
			renderDebugTiles = false;
		}

		level.renderBackgroundWrappers(batch);

		MapManager.clearDrawnConnections();

		for (Tile tile : tiles)
		{
			if (tile.getDimension() == Dimension.MONOCHROME)
			{
				drawTile(tile, batch);
			}
		}

		for (Tile tile : tiles)
		{
			if (tile.getDimension() == Dimension.COLORED)
			{
				drawTile(tile, batch);
			}
		}

		if (level.shouldRenderEntities())
		{
			for (Entity entity : entities)
			{
				if (!entity.shouldRenderInBatch())
				{
					continue;
				}

				entity.draw(batch, entity.getDimension());

				if (entity.getDimension() == Dimension.COLORED && entity instanceof EntityKey && renderDebugTiles)
				{
					ShaderHandler.disableLighting();

					float size = DisplayConstants.TILE_SIZE / 1.5f;

					ShapeRenderer sr = Renderer.useShapeRenderer();

					sr.set(ShapeType.Line);
					sr.setColor(portBoxBorderColor);
					sr.circle(entity.getX() + entity.getImage().getRegionWidth() - size * PORT_COLOR_BOX_OFFSET, entity.getY() + entity.getImage().getRegionHeight() - size * PORT_COLOR_BOX_OFFSET, size * PORT_COLOR_BOX_SIZE);

					sr.set(ShapeType.Filled);
					sr.setColor(((EntityKey) entity).getPort().getColor());
					sr.circle(entity.getX() + entity.getImage().getRegionWidth() - size * PORT_COLOR_BOX_OFFSET, entity.getY() + entity.getImage().getRegionHeight() - size * PORT_COLOR_BOX_OFFSET, size * PORT_COLOR_BOX_SIZE);

					ShaderHandler.enableLighting();
				}

				if (level.shouldShowHitboxes())
				{
					ShaderHandler.disableLighting();

					ShapeRenderer sr = Renderer.useShapeRenderer();

					sr.set(ShapeType.Line);
					sr.setColor(Color.WHITE);
					sr.polygon(entity.getHitbox().getPoints());

					ShaderHandler.enableLighting();
				}
			}
		}

		boolean drawLogicalConnections = Gdx.input.isKeyPressed(Input.Keys.J) && !isLockingInput();

		if (drawLogicalConnections)
		{
			float boxOffset = DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE / 2;
			float boxOffsetEntity = DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET - DisplayConstants.TILE_SIZE * 1.5f * PORT_COLOR_BOX_SIZE / 2;

			ShaderHandler.disableLighting();

			ShapeRenderer sr = Renderer.useShapeRenderer();

			for (Tile tile : tiles)
			{
				if (tile.isSender())
				{
					for (Tile other : tiles)
					{
						if (other.isReceiver() && tile.getSendPort() == other.getReceivePort())
						{
							sr.setColor(ColorHelper.applyAlphaAndCopy(TriggerPort.getColor(tile.getSendPort()), 0.5f));
							sr.set(ShapeType.Filled);
							sr.rectLine(tile.getHitbox().getMaxX() - boxOffset, tile.getHitbox().getMaxY() - boxOffset, other.getHitbox().getMaxX() - boxOffset, other.getHitbox().getMaxY() - boxOffset, 5.0f);
						}
					}
				}
			}

			for (Entity entity : entities)
			{
				if (entity instanceof EntityKey)
				{
					for (Tile other : tiles)
					{
						if (other instanceof ITileKeyInteractable && ((ITileKeyInteractable) other).getKeyPort() == ((EntityKey) entity).getPort())
						{
							sr.setColor(ColorHelper.applyAlphaAndCopy(((EntityKey) entity).getPort().getColor(), 0.3f));
							sr.rectLine(entity.getHitbox().getMaxX() - boxOffsetEntity, entity.getHitbox().getMaxY() - boxOffsetEntity, other.getHitbox().getMaxX() - boxOffset, other.getHitbox().getMaxY() - boxOffset, 5.0f);
						}
					}
				}
			}

			ShaderHandler.enableLighting();
		}

		level.renderForegroundWrappers(batch);

		if (shouldDrawLines() && (editWrapperPanel.getWrapper() != null || editEmitterPanel.getWrapper() != null))
		{
			ShaderHandler.disableLighting();

			ShapeRenderer sr = Renderer.useShapeRenderer();

			sr.setColor(Color.RED);
			sr.set(ShapeType.Line);
			sr.polygon((editEmitterPanel.getWrapper() != null ? editEmitterPanel.getWrapper().getHitbox() : editWrapperPanel.getWrapper().getHitbox()).getPoints());

			ShaderHandler.enableLighting();
		}

		if (shouldDrawLines())
		{
			ShapeRenderer sr = Renderer.useShapeRenderer();

			sr.set(ShapeType.Line);

			for (ICopyable<?> object : hoveredObjects)
			{
				float originalAlpha = selectionColor.a;

				selectionColor.a /= 2.0f;

				sr.setColor(selectionColor);
				sr.polygon(object.getHitbox().getPoints());

				selectionColor.a = originalAlpha;
			}

			for (WrapperTexture wrapper : selectedWrappers)
			{
				wrapper.drawBorders(batch);
			}

			ICopyable<?> primaryObject = null;

			for (ICopyable<?> object : selectedObjects)
			{
				if (object instanceof WrapperTexture || object instanceof Tile && ((Tile) object).getProperties().isRotatable())
				{
					primaryObject = object;

					break;
				}
			}

			for (ICopyable<?> object : selectedObjects)
			{
				if (object instanceof WrapperTexture || object instanceof Tile && ((Tile) object).getProperties().isRotatable())
				{
					rotationCenterX = object.getCenterOfRotationX();
					rotationCenterY = object.getCenterOfRotationY();

					break;
				}
			}

			for (ICopyable<?> object : selectedObjects)
			{
				sr.setColor(selectionColor);
				sr.polygon(object.getHitbox().getPoints());

				if (object == primaryObject && Gdx.input.isButtonPressed(Input.Buttons.RIGHT))
				{
					sr.setColor(ColorHelper.applyAlphaAndCopy(Color.CYAN, 0.7f));

					sr.set(ShapeType.Filled);
					sr.circle(rotationCenterX, rotationCenterY, 8);

					sr.rectLine(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY(), rotationCenterX, rotationCenterY, 6);

					sr.circle(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY(), 6);
				}
			}
		}

		if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) && !isLockingInput())
		{
			level.setRenderTiles(renderAllBefore);
			level.setRenderBackgroundWrappers(renderBGWrappersBefore);
			level.setRenderForegroundWrappers(renderFGWrappersBefore);
			level.setRenderEntities(renderEntitiesBefore);
			level.setRenderParticleEmitters(renderEmittersBefore);
			renderDebugTiles = renderDebugTilesBefore;
		}

		ShaderHandler.updateAndRender(batch);

		ShapeRenderer sr = Renderer.useShapeRenderer();

		sr.setColor(ColorHelper.applyAlphaAndCopy(Color.GREEN, 0.6f));
		sr.set(ShapeType.Line);
		sr.rect(level.getAbsLeftLimit().getCenterX(), level.getAbsTopLimit().getCenterY(), level.getAbsRightLimit().getCenterX() - level.getAbsLeftLimit().getCenterX(), level.getAbsBottomLimit().getCenterY() - level.getAbsTopLimit().getCenterY());

		alphaFactor = 1.0f;

		if (shouldDisableGUI())
		{
			alphaFactor = 0.16f;
		}

		terrainSelection.render(alphaFactor, batch);
		entitySelection.render(alphaFactor, batch);

		sr = Renderer.useShapeRenderer();

		if (displaySelectedArea)
		{
			ShaderHandler.disableLighting();

			sr.setColor(selectionColor);
			sr.set(ShapeType.Line);
			sr.rect(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);
		}

		float borderWidth = GameLauncher.getWidth() * Renderer.getHorizontalScale() / 2.0f;
		float borderHeight = GameLauncher.getHeight() * Renderer.getVerticalScale() / 2.0f;

		sr.setColor(ColorHelper.applyAlphaAndCopy(Color.GRAY, 0.05f));
		sr.set(ShapeType.Filled);
		sr.rect(-borderWidth, 0, borderWidth, GameLauncher.getHeight());
		sr.rect(GameLauncher.getWidth(), 0, borderWidth, GameLauncher.getHeight());
		sr.rect(0, -borderHeight, GameLauncher.getWidth(), borderHeight);
		sr.rect(0, GameLauncher.getHeight(), GameLauncher.getWidth(), borderHeight);

		if (controlPanel.isActive())
		{
			controlPanel.render(alphaFactor, batch);

			saveButton.render(alphaFactor, batch);
			reloadButton.render(alphaFactor, batch);
			levelTypeButton.render(alphaFactor, batch);

			tileChooserButton.render(alphaFactor, batch);
			entityChooserButton.render(alphaFactor, batch);
			visibiltyButton.render(alphaFactor, batch);
			addParticleWrapperButton.render(alphaFactor, batch);
			addWrapperButton.render(alphaFactor, batch);
			toggleLightsButton.render(alphaFactor, batch);

			ParticleSystem ps = level.getParticleSystem();

			Renderer.setFont(Fonts.getSmallFont());

			int xPos = saveButton.getX();
			int yGap = Renderer.getLineHeight() + 5;
			int yOffset = addWrapperButton.getY() + 45 - yGap;

			Renderer.drawString("Particle - Render:" + ps.renderedParticleCount + " Active:" + ps.activeParticleCount + " Pool:" + ps.pooledParticleCount, Color.WHITE, xPos, yOffset += yGap, batch);

			Renderer.setFont(Fonts.getDefaultFont());
		}

		editWrapperPanel.render(alphaFactor, batch);
		editEmitterPanel.render(alphaFactor, batch);
		moveWrapperPanel.render(alphaFactor, batch);
		importPanel.render(alphaFactor, batch);
		doorPanel.render(alphaFactor, batch);
		textPanel.render(alphaFactor, batch);
		platformPanel.render(alphaFactor, batch);
		lightPanel.render(alphaFactor, batch);
		soundSourcePanel.render(alphaFactor, batch);
		holePanel.render(alphaFactor, batch);
		cannonPanel.render(alphaFactor, batch);
		renamePanel.render(alphaFactor, batch);
	}

	public static void drawTile(Tile tile, Batch batch)
	{
		if ((level.shouldRenderAll() || tile.shouldUseOriginalTextures() && (level.shouldRenderForegroundWrappers() || level.shouldRenderWrappers())) && (tile.shouldRenderInGame() || renderDebugTiles))
		{
			tile.initDraw();
			tile.draw(batch, tile.getDimension());

			if (tile instanceof TileCameraControl)
			{
				MapManager.drawCameraDebug((TileCameraControl) tile, false, batch);
			}

			if (shouldDrawLines() && selectedTiles.contains(tile))
			{
				tile.drawBorders(batch);
			}

			if (tile.getDimension() == Dimension.COLORED && renderDebugTiles)
			{
				ShapeRenderer sr = Renderer.useShapeRenderer();

				if (tile instanceof ITileKeyInteractable)
				{
					sr.setColor(((ITileKeyInteractable) tile).getKeyPort().getColor());
					sr.set(ShapeType.Filled);
					sr.circle(tile.getX() + tile.getImage().getRegionWidth() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, tile.getY() + tile.getImage().getRegionHeight() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE);

					sr.setColor(portBoxBorderColor);
					sr.set(ShapeType.Line);
					sr.circle(tile.getX() + tile.getImage().getRegionWidth() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, tile.getY() + tile.getImage().getRegionHeight() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE);
				}

				if (tile instanceof ITileTriggerable)
				{
					sr.setColor(TriggerPort.getColor(tile.getPort()));
					sr.set(ShapeType.Filled);
					sr.rect(tile.getHitbox().getMaxX() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, tile.getHitbox().getMaxY() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE);

					sr.setColor(portBoxBorderColor);
					sr.set(ShapeType.Line);
					sr.rect(tile.getHitbox().getMaxX() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, tile.getHitbox().getMaxY() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE);
				}

				if (level.shouldRenderAll() && (tile.getHitbox().isConnectedLeft() || tile.getHitbox().isConnectedRight()))
				{
					MapManager.drawConnectedTile(tile, batch);
				}
			}

			if (level.shouldShowHitboxes() && tile.isActive())
			{
				ShapeRenderer sr = Renderer.useShapeRenderer();

				sr.setColor(Color.WHITE);
				sr.set(ShapeType.Line);
				sr.polygon(tile.getHitbox().getPoints());

				sr.set(ShapeType.Filled);

				if (tile.getHitbox().isConnectedLeft())
				{
					sr.setColor(Color.GRAY);
					sr.rect(tile.getHitbox().getPoints()[tile.getHitbox().getTopLeftIndex()] - 5, tile.getHitbox().getPoints()[tile.getHitbox().getTopLeftIndex() + 1] - 5, 10, 10);
				}

				if (tile.getHitbox().isConnectedRight())
				{
					sr.setColor(Color.GRAY);
					sr.rect(tile.getHitbox().getPoints()[tile.getHitbox().getTopLeftIndex() + 2] - 5, tile.getHitbox().getPoints()[tile.getHitbox().getTopLeftIndex() + 3] - 5, 10, 10);
				}
			}
		}
	}

	public static void update(int delta)
	{
		Profiling.startSection("mapeditor");

		Input input = Gdx.input;

		boolean lockInput = renamePanel.isActive() || textPanel.isActive() || GameLauncher.getCheatPanel().isActive() || moveWrapperPanel.isActive() || importPanel.shouldLockInput() || editWrapperPanel.shouldLockInput() || editEmitterPanel.shouldLockInput() || soundSourcePanel.shouldLockInput() || holePanel.shouldLockInput() || lightPanel.shouldLockInput();
		boolean canGrab = false;

		smoothMode = input.isKeyPressed(Input.Keys.SHIFT_LEFT);

		hoveredObjects.clear();

		if (entityCooldown > 0)
		{
			entityCooldown -= delta;
		}

		if (colorChangeCooldown > 0)
		{
			colorChangeCooldown -= delta;
		}

		if (!paused)
		{
			lockHorizontal = input.isKeyPressed(Input.Keys.H);
			lockVertical = input.isKeyPressed(Input.Keys.V);

			boolean anyContained = anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY());

			if (!shouldDisableGUI())
			{
				if (controlPanel.isActive())
				{
					controlPanel.update(delta);
					saveButton.update(delta);
					reloadButton.update(delta);
					levelTypeButton.update(delta);
					tileChooserButton.update(delta);
					entityChooserButton.update(delta);
					visibiltyButton.update(delta);
					toggleLightsButton.update(delta);
					addParticleWrapperButton.update(delta);
					addWrapperButton.update(delta);
				}

				lightPanel.update(delta);
				soundSourcePanel.update(delta);
				holePanel.update(delta);
				platformPanel.update(delta);
				cannonPanel.update(delta);
				importPanel.update(delta);
				editWrapperPanel.update(delta);
				editEmitterPanel.update(delta);
				moveWrapperPanel.update(delta);
				doorPanel.update(delta);
				textPanel.update(delta);
				terrainSelection.update(delta);
				entitySelection.update(delta);
				renamePanel.update(delta);
			}

			for (WrapperTexture wrapper : level.getWrappers())
			{
				if (wrapper.isAnimated())
				{
					wrapper.updateAnimation(delta);
				}
			}

			boolean result = false;

			if ((!anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) || shouldDisableGUI()) && (input.isButtonPressed(Input.Buttons.LEFT) || InputHandler.isKeyJustPressed(InputAction.JUMP) && !input.isKeyPressed(Input.Keys.SPACE)))
			{
				for (WrapperTexture wrapper : selectedWrappers)
				{
					if (!wrapper.ownsMouse() && wrapper.getHitbox().contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
					{
						result = true;

						break;
					}
				}
			}

			movingWrappers = result;

			if (input.isKeyPressed(Input.Keys.F5))
			{
				float cameraOffsetX = level.getCameraOffset().x;
				float cameraOffsetY = level.getCameraOffset().y;

				Visibility oldVisibilty = currentVisibilty;

				saveItAll();
				initLevel(mapID, mapPath);

				setVisibility(oldVisibilty);

				adjustCameraOffset(cameraOffsetX, cameraOffsetY);
			}

			if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) && input.isKeyJustPressed(Input.Keys.P) && !lockInput || input.isKeyJustPressed(Input.Keys.F11) || input.isKeyJustPressed(Input.Keys.F1))
			{
				saveItAll();

				try
				{
					MapManager.transitionFrom(GameLauncher.getWidth() / 2, GameLauncher.getHeight() / 2, 1337 / 2, false);

					GameLauncher.setStatus(GameStatus.IN_LEVEL);

					LevelManager.initLevel(mapPath, false);
					LevelManager.resume();

					GameLauncher.clearRecords();

					CursorType.useCursor(CursorType.DEFAULT);
				}
				catch (Exception e)
				{
					Debug.warn("Tried going into play mode for map \"" + mapName + "\", encountered error:");
					Debug.warnStackTrace(e);
				}
			}

			if (selecting && !(input.isButtonPressed(Input.Buttons.LEFT) || InputHandler.isKeyJustPressed(InputAction.JUMP)))
			{
				selecting = false;
				displaySelectedArea = false;

				for (Entity entity : entities)
				{
					if (entity.getDimension() == Dimension.COLORED && isObjectVisible(entity) && selectedArea.contains(entity.getHitbox().getCenterX(), entity.getHitbox().getCenterY()))
					{
						selectedObjects.add(entity);
						selectedEntities.add(entity);
					}
				}

				for (Tile tile : tiles)
				{
					if (tile.getDimension() == Dimension.COLORED && isObjectVisible(tile) && selectedArea.contains(tile.getHitbox().getCenterX(), tile.getHitbox().getCenterY()))
					{
						selectedObjects.add(tile);
						selectedTiles.add(tile);

						tile.updateBorders();

						onSelectTile(tile);
					}
				}

				for (WrapperTexture wrapper : wrappers)
				{
					if (isObjectVisible(wrapper) && selectedArea.contains(wrapper.getHitbox().getCenterX(), wrapper.getHitbox().getCenterY()))
					{
						selectedObjects.add(wrapper);
						selectedWrappers.add(wrapper);

						wrapper.updateBorders();
					}
				}
			}

			boolean mousePressed = input.isButtonPressed(Input.Buttons.LEFT) && Gdx.input.justTouched() || InputHandler.isKeyJustPressed(InputAction.JUMP) && !lockInput && !anyContained;
			boolean anyOwnsMouse = false;
			boolean mouseMiddlePressed = input.isButtonPressed(Input.Buttons.MIDDLE) && input.justTouched();
			boolean canHoverObjects = !(movingEntities || movingTiles || movingWrappers);

			for (ICopyable<?> object : selectedObjects)
			{
				if (object.ownsMouse())
				{
					anyOwnsMouse = true;

					break;
				}
			}

			if (!anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
			{
				for (int i = level.getWrappers().size() - 1; i >= 0; i--)
				{
					WrapperTexture wrapper = level.getWrappers().get(i);

					if (wrapper.getHitbox().contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && wrapper.isInForeground() && isObjectVisible(wrapper) && !anyOwnsMouse)
					{
						if (canHoverObjects && hoveredObjects.isEmpty())
						{
							hoveredObjects.add(wrapper);
						}

						canGrab = true;

						if (mousePressed)
						{
							if (!input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !selectedWrappers.contains(wrapper))
							{
								deselectAll();
							}

							onWrapperSelection(wrapper);

							selectedObjects.add(wrapper);
							selectedWrappers.add(wrapper);
							wrapper.updateBorders();

							mousePressed = false;
						}

						if (mouseMiddlePressed)
						{
							duplicateWrapper(wrapper);

							mouseMiddlePressed = false;
						}
					}
				}
			}

			if (clickCooldown <= 0 && !anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
			{
				try
				{
					if (!selecting && !anyOwnsMouse)
					{
						Tile existingTile = getTileAtXY(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY());

						if (input.isButtonPressed(Input.Buttons.LEFT) || InputHandler.isKeyJustPressed(InputAction.JUMP))
						{
							if ((existingTile instanceof ITileTriggerable || existingTile instanceof ITileKeyInteractable) && renderDebugTiles)
							{
								if (colorChangeCooldown <= 0)
								{
									bufferedHitbox.set(existingTile.getHitbox().getMaxX() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, existingTile.getHitbox().getMaxY() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE);

									if (bufferedHitbox.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
									{
										if (existingTile instanceof ITileTriggerable)
										{
											existingTile.setPort(TriggerPort.getTrigger(existingTile.getPort()).next().toPort());

											colorChangeCooldown = 350;

											lastTriggerPort = TriggerPort.getTrigger(existingTile.getPort());
										}

										if (existingTile instanceof ITileKeyInteractable)
										{
											((ITileKeyInteractable) existingTile).setKeyPort(((ITileKeyInteractable) existingTile).getKeyPort().next());

											colorChangeCooldown = 350;

											lastKeyPort = ((ITileKeyInteractable) existingTile).getKeyPort();
										}
									}
								}
							}
							else if (currentTile != null && selectedTiles.size() == 0)
							{
								Tile tile = Tile.createTile(currentTile.getType(), "", 0, 0, new Vector2(), currentTile.getLevel(), Dimension.COLORED, TileType.getProperty(currentTile.getType()), false);

								if (tile != null && (getTileAtXY(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) == null || (getTileAtXY(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()).isHidden() || tile.isHidden() || !tile.isBlocked()) && getTileAtXY(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()).getType() != tile.getType()))
								{
									Debug.log("Creating tile " + currentTile.getType() + " ...");

									tile = Tile.createTile(currentTile.getType(), "", currentTile.getInnerType(), currentTile.getRotation(), useLineHelper || !currentTile.hasTextures() ? new Vector2(InputHandler.getCurrentInputX() / DisplayConstants.TILE_SIZE * DisplayConstants.TILE_SIZE, InputHandler.getCurrentInputY() / DisplayConstants.TILE_SIZE * DisplayConstants.TILE_SIZE) : new Vector2(InputHandler.getCurrentInputX() - TerrainObject.getMainImage(currentTile.getType()).getRegionWidth() / 2, InputHandler.getCurrentInputY() - TerrainObject.getMainImage(currentTile.getType()).getRegionHeight() / 2), currentTile.getLevel(), Dimension.COLORED, TileType.getProperty(currentTile.getType()), true);
									tile.setRotation(currentTile.getRotation());
									tile.setDimension(currentTile.getDimension());
									tile.readMetadata();
									tile.updateInnerType();

									if (tile.hasMirror())
									{
										tile.initMirror(tile.getMirroredTile(), level.getType());
									}

									level.addTile(tile);

									if (tile instanceof ITileTriggerable && lastTriggerPort != null)
									{
										tile.setPort(lastTriggerPort.toPort());
									}

									if (tile instanceof ITileKeyInteractable && lastKeyPort != null)
									{
										((ITileKeyInteractable) tile).setKeyPort(lastKeyPort);
									}

									tile.onCreated();
									tile.updateHitbox();

									if (tile instanceof TileCameraControl)
									{
										MapManager.initLevel(null, level);
									}
								}
							}
						}
						else if (existingTile != null && mouseMiddlePressed)
						{
							Tile duplicate = existingTile.copy(level);

							if (!input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !input.isKeyPressed(Input.Keys.SHIFT_LEFT))
							{
								deselectAll();
							}

							duplicate.onCreated();

							duplicate.adjustX(10);
							duplicate.adjustY(10);

							duplicate.createBorders();

							selectedObjects.add(duplicate);
							selectedTiles.add(duplicate);

							level.addTile(duplicate);

							mouseMiddlePressed = false;
						}
					}

					boolean inAny = false;

					if (!selecting && (!anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) || shouldDisableGUI()) && !anyOwnsMouse)
					{
						Entity existingEntity = getEntityAtXY(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY());

						bufferedHitbox.set(0, 0, 0, 0);

						if (existingEntity != null && isObjectVisible(existingEntity))
						{
							inAny = true;
							canGrab = true;

							if (canHoverObjects && hoveredObjects.isEmpty())
							{
								hoveredObjects.add(existingEntity);
							}

							if (existingEntity instanceof EntityKey)
							{
								bufferedHitbox.set(existingEntity.getX() + existingEntity.getImage().getRegionWidth() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, existingEntity.getY() + existingEntity.getImage().getRegionHeight() - DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_OFFSET, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE, DisplayConstants.TILE_SIZE * PORT_COLOR_BOX_SIZE);
							}
						}

						if (bufferedHitbox.contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
						{
							if ((input.isButtonPressed(Input.Buttons.LEFT) || InputHandler.isKeyJustPressed(InputAction.JUMP)) && entityCooldown <= 0 && renderDebugTiles)
							{
								((EntityKey) existingEntity).setPort(((EntityKey) existingEntity).getPort().next());

								entityCooldown = 500;
							}
						}

						movingEntities = false;
						if (existingEntity != null && isObjectVisible(existingEntity))
						{
							if (input.isButtonPressed(Input.Buttons.LEFT) || InputHandler.isKeyJustPressed(InputAction.JUMP))
							{
								movingEntities = true;
							}
						}

						if (mousePressed)
						{
							if (existingEntity != null && isObjectVisible(existingEntity))
							{
								if (!input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !input.isKeyPressed(Input.Keys.SHIFT_LEFT))
								{
									deselectAll();
								}

								selectedObjects.add(existingEntity);
								selectedEntities.add(existingEntity);

								mousePressed = false;
							}
							else if (currentTile == null && selectedObjects.isEmpty() && currentEntityType != null)
							{
								Entity entity = Entity.createEntity(EntityType.getTypeByNameTag(currentEntityType), new Vector2(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()), level, Dimension.COLORED, false);
								level.addEntity(entity);

								if (entity instanceof EntityKey && lastKeyPort != null)
								{
									((EntityKey) entity).setPort(lastKeyPort);
								}

								mousePressed = false;
							}
						}
					}
					else
					{
						lastSelectedBlock[1] = (int) (InputHandler.getCurrentInputX() - level.getCameraOffset().x) / DisplayConstants.TILE_SIZE;
						lastSelectedBlock[0] = (int) (InputHandler.getCurrentInputY() - level.getCameraOffset().x) / DisplayConstants.TILE_SIZE;
					}

					result = false;

					if ((!anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) || shouldDisableGUI()) && (input.isButtonPressed(Input.Buttons.LEFT) || InputHandler.isKeyJustPressed(InputAction.JUMP)))
					{
						currentEntityType = null;
						currentTile = null;

						for (Tile tile : selectedTiles)
						{
							if (tile.getHitbox().contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && !tile.bordersContain(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
							{
								result = true;
							}
						}
					}

					movingTiles = result;

					terrainSelection.clearSelection();
					entitySelection.clearSelection();

					if (!anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && !anyOwnsMouse)
					{
						for (int i = tiles.size() - 1; i >= 0; i--)
						{
							Tile tile = tiles.get(i);

							if (tile.getDimension() == Dimension.COLORED && tile.getHitbox().contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && isObjectVisible(tile))
							{
								inAny = true;
								canGrab = true;

								boolean hover = false;

								if (canHoverObjects && hoveredObjects.isEmpty())
								{
									hoveredObjects.add(tile);

									hover = true;
								}

								if ((mousePressed || hover) && input.isKeyPressed(Input.Keys.CONTROL_LEFT))
								{
									Tile leftTile = tile;
									Tile rightTile = tile;

									while (leftTile != null)
									{
										if (mousePressed)
										{
											selectedObjects.add(leftTile);
											selectedTiles.add(leftTile);
										}
										else
										{
											hoveredObjects.add(leftTile);
										}

										leftTile.updateBorders();

										if (!leftTile.getHitbox().isConnectedLeft())
										{
											break;
										}

										leftTile = leftTile.getLeftConnectedTile();
									}

									while (rightTile != null)
									{
										if (mousePressed)
										{
											selectedObjects.add(rightTile);
											selectedTiles.add(rightTile);
										}
										else
										{
											hoveredObjects.add(rightTile);
										}

										rightTile.updateBorders();

										if (!rightTile.getHitbox().isConnectedRight())
										{
											break;
										}

										rightTile = rightTile.getRightConnectedTile();
									}

									if (mousePressed)
									{
										mousePressed = false;
									}
								}
								else if (mousePressed)
								{
									if (!input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !anyContained)
									{
										deselectAll();
									}

									selectedTiles.add(tile);
									selectedObjects.add(tile);
									tile.updateBorders();
									onSelectTile(tile);

									mousePressed = false;
								}

								break;
							}
						}

						for (int i = level.getWrappers().size() - 1; i >= 0; i--)
						{
							WrapperTexture wrapper = level.getWrappers().get(i);

							if (wrapper.getHitbox().contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && wrapper.isInBackground() && isObjectVisible(wrapper))
							{
								if (canHoverObjects && hoveredObjects.isEmpty())
								{
									hoveredObjects.add(wrapper);
								}

								canGrab = true;

								if (mousePressed)
								{
									if (!input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !selectedWrappers.contains(wrapper) && !anyContained)
									{
										deselectAll();
									}

									onWrapperSelection(wrapper);

									selectedObjects.add(wrapper);
									selectedWrappers.add(wrapper);
									level.getWrappers().get(i).updateBorders();

									mousePressed = false;
								}

								if (mouseMiddlePressed)
								{
									duplicateWrapper(wrapper);

									mouseMiddlePressed = false;
								}
							}
						}
					}

					if (!inAny && mousePressed && !input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !anyOwnsMouse && !anyContained)
					{
						deselectAll();
					}
				}
				catch (ArrayIndexOutOfBoundsException e)
				{
					Debug.log("ArrayIndexOutOfBaconException: " + e);
					Debug.logStackTrace(e);
				}
			}

			if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) && mousePressed)
			{
				selecting = true;
				displaySelectedArea = true;

				selectionStartPoint.set(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY());

				selectedArea.setX(InputHandler.getCurrentInputX());
				selectedArea.setY(InputHandler.getCurrentInputY());
				selectedArea.setWidth(0.0f);
				selectedArea.setHeight(0.0f);
			}

			if (selectedTiles.size() == 1)
			{
				Tile tile = selectedTiles.iterator().next();

				if (tile instanceof TileCameraControl)
				{
					TileCameraControl camera = (TileCameraControl) tile;

					if (input.isKeyPressed(Input.Keys.NUM_1))
					{
						camera.setCameraDirection(1);
					}
					else if (input.isKeyPressed(Input.Keys.NUM_2))
					{
						camera.setCameraDirection(2);
					}
					else if (input.isKeyPressed(Input.Keys.NUM_3))
					{
						camera.setCameraDirection(3);
					}
					else if (input.isKeyPressed(Input.Keys.NUM_4))
					{
						camera.setCameraDirection(4);
					}
					else if (input.isKeyPressed(Input.Keys.NUM_0))
					{
						camera.setCameraDirection(-1);
					}
				}
			}

			if (input.isKeyPressed(Input.Keys.R) && !lockInput)
			{
				for (WrapperTexture wrapper : selectedWrappers)
				{
					if (input.isKeyPressed(Input.Keys.PLUS))
					{
						wrapper.setRotation(wrapper.getRotation() + (smoothMode ? 0.25f : 0.5f));
					}
					else if (input.isKeyPressed(Input.Keys.MINUS))
					{
						wrapper.setRotation(wrapper.getRotation() - (smoothMode ? 0.25f : 0.5f));
					}

					if (wrapper == editWrapperPanel.getWrapper())
					{
						editWrapperPanel.getRotation().setInput((int) wrapper.getRotation() + "");
					}
					else if (wrapper == editEmitterPanel.getWrapper())
					{
						editEmitterPanel.getRotation().setInput((int) wrapper.getRotation() + "");
					}

					if (wrapper.isParticleWrapper())
					{
						wrapper.updateBorders();
						wrapper.getEmitter().reset();

						if (wrapper.getEmitter().hasMirror())
						{
							wrapper.getEmitter().getMirroredEmitter().reset();
						}
					}
				}

				if (selectedTiles.size() > 0)
				{
					boolean addRotation = input.isKeyPressed(Input.Keys.PLUS);
					boolean subRotation = input.isKeyPressed(Input.Keys.MINUS);

					if (addRotation || subRotation)
					{
						for (Tile tile : selectedTiles)
						{
							float rotationChange = tile.getProperties().getRotationStep();

							if (!tile.getProperties().isForceRotationStep())
							{
								if (tile.getRotation() == 0.0f)
								{
									rotationChange = 4.0f;
								}
								else
								{
									if (smoothMode)
									{
										rotationChange *= 0.5f;
									}

									rotationChange *= delta / 60.0f;

									if (input.isKeyPressed(Input.Keys.NUM_2))
									{
										rotationChange *= 2.0f;
									}
									else if (input.isKeyPressed(Input.Keys.NUM_3))
									{
										rotationChange *= 3.0f;
									}
								}
							}
							else if (addRotation && !input.isKeyPressed(Input.Keys.PLUS) || subRotation && !input.isKeyPressed(Input.Keys.MINUS))
							{
								rotationChange = 0.0f;
							}

							if (rotationChange != 0.0f)
							{
								rotationChange *= subRotation ? -1 : 1;

								rotateTile(tile, rotationChange);
							}
						}
					}
				}
			}

			float cameraFactor = delta * (input.isKeyPressed(Input.Keys.CONTROL_LEFT) ? 2.0f : 1.0f);

			if (!lockInput)
			{
				if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) && input.isKeyJustPressed(Input.Keys.R))
				{
					renamePanel = new RenamePanel(1920 / 2 - 250, 1080 / 2 - 250, 500, 0);
					renamePanel.init(level);
					renamePanel.setActive(true);
				}

				if (input.isKeyJustPressed(Input.Keys.Z) && level.shouldCreateMirrors())
				{
					for (Tile tile : selectedTiles)
					{
						if (tile.isChangeable())
						{
							tile.setShared(!tile.isShared());
						}
					}

					for (Entity entity : selectedEntities)
					{
						entity.setShared(!entity.isShared());
					}
				}

				if (input.isKeyJustPressed(Input.Keys.R))
				{
					for (Tile tile : selectedTiles)
					{
						tile.setTimeEffected(!tile.isRawTimeEffected());

						if (tile.hasMirror())
						{
							tile.getMirroredTile().setTimeEffected(tile.isRawTimeEffected());
						}
					}

					for (Entity entity : selectedEntities)
					{
						entity.setTimeEffected(!entity.isRawTimeEffected());

						if (entity.hasMirror())
						{
							entity.getMirroredEntity().setTimeEffected(entity.isRawTimeEffected());
						}
					}

					if (selectedObjects.isEmpty())
					{
						terrainSelection.setActive(true);
					}
				}

				if (input.isKeyJustPressed(Input.Keys.Q))
				{
					deselectAll();
				}

				if (input.isKeyJustPressed(Input.Keys.U))
				{
					Debug.log("Searching for dead wrappers...");

					Set<WrapperTexture> deadWrappers = new HashSet<WrapperTexture>();

					for (WrapperTexture wrapper : wrappers)
					{
						if (wrapper.isParticleWrapper())
						{
							if (wrapper.getEmitter().getSpawnCount().averageValue() <= 0)
							{
								deadWrappers.add(wrapper);
							}
						}
						else
						{
							if (!wrapper.isDrawSubImages())
							{
								if (wrapper.getImage().getRegionWidth() > wrapper.getHitbox().getRawWidth() || wrapper.getImage().getRegionHeight() > wrapper.getHitbox().getRawHeight())
								{
									deadWrappers.add(wrapper);
								}
							}

							if (wrapper.getStartEmitterTint().a <= 0.0f)
							{
								deadWrappers.add(wrapper);
							}
						}
					}

					deselectAll();

					for (WrapperTexture wrapper : deadWrappers)
					{
						wrapper.createBorders();
						selectedObjects.add(wrapper);
						selectedWrappers.add(wrapper);
					}

					Debug.log("Found " + deadWrappers.size() + " dead wrappers.");
				}

				if (InputHandler.isKeyPressed(InputAction.MOVE_LEFT))
				{
					adjustCameraOffset(cameraFactor, 0);
				}

				if (InputHandler.isKeyPressed(InputAction.MOVE_RIGHT))
				{
					adjustCameraOffset(-cameraFactor, 0);
				}

				if (InputHandler.isKeyPressed(InputAction.MOVE_UP))
				{
					adjustCameraOffset(0, cameraFactor);
				}

				if (InputHandler.isKeyPressed(InputAction.MOVE_DOWN))
				{
					adjustCameraOffset(0, -cameraFactor);
				}
			}

			if (input.isKeyJustPressed(Input.Keys.ESCAPE) && leaveCooldown <= 0)
			{
				saveItAll();

				currentEntityType = null;
				currentTile = null;

				GameLauncher.setStatus(GameStatus.MAP_EDITOR_MENU);

				CursorType.useCursor(CursorType.DEFAULT);
			}

			if (input.isKeyPressed(Input.Keys.C) && input.isKeyJustPressed(Input.Keys.NUM_0))
			{
				adjustCameraOffset((int) -level.getCameraOffset().x, (int) -level.getCameraOffset().y);
			}

			if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) && (input.isKeyJustPressed(Input.Keys.C) || input.isKeyJustPressed(Input.Keys.X)) && !selectedObjects.isEmpty())
			{
				boolean cut = input.isKeyPressed(Input.Keys.X);

				clipboard.clear();

				for (ICopyable<?> object : selectedObjects)
				{
					ICopyable<?> copy = object.copy(level);
					copy.setPosition(copy.getPosition().x + (cut ? 0 : 7), copy.getPosition().y + (cut ? 0 : 7));
					clipboard.add(copy);

					if (cut)
					{
						if (object instanceof Tile)
						{
							level.removeTile((Tile) object);
							level.removeTile(((Tile) object).getMirroredTile());
						}
						else if (object instanceof Entity)
						{
							level.removeEntity((Entity) object);
							level.removeEntity(((Entity) object).getMirroredEntity());
						}
						else if (object instanceof WrapperTexture)
						{
							level.removeWrapper((WrapperTexture) object);
						}
					}
				}

				if (cut)
				{
					deselectAll();
				}

				Debug.log("Copied " + selectedObjects.size() + " objects to clipboard.");
			}

			if (input.isKeyPressed(Input.Keys.CONTROL_LEFT) && input.isKeyJustPressed(Input.Keys.V) && !clipboard.isEmpty())
			{
				selectedObjects.clear();
				selectedTiles.clear();
				selectedWrappers.clear();

				for (ICopyable<?> object : clipboard)
				{
					object.setLevel(level);

					if (object.shouldCreateMirror(true))
					{
						object.createMirror(level.getLevelType());
					}

					selectedObjects.add(object);

					if (object instanceof Tile)
					{
						((Tile) object).createBorders();
						((Tile) object).init();
						((Tile) object).onCreated();

						if (((Tile) object).hasMirror())
						{
							((Tile) object).getMirroredTile().setLevel(level);
							((Tile) object).getMirroredTile().init();
							((Tile) object).getMirroredTile().setRotation(0.0f);
							((Tile) object).getMirroredTile().updateInnerType();
							((Tile) object).getMirroredTile().setWidth(object.getHitbox().getRawWidth());
							((Tile) object).getMirroredTile().setHeight(object.getHitbox().getRawHeight());
							((Tile) object).getMirroredTile().setRotation(((Tile) object).getRotation());
						}

						level.addTile((Tile) object);
						selectedTiles.add((Tile) object);
					}

					if (object instanceof Entity)
					{
						level.addEntity((Entity) object);

						if (((Entity) object).hasMirror())
						{
							((Entity) object).getMirroredEntity().setLevel(level);
						}
					}

					if (object instanceof WrapperTexture)
					{
						level.addWrapper((WrapperTexture) object);
						selectedWrappers.add((WrapperTexture) object);
						((WrapperTexture) object).updateBorders();

						if (((WrapperTexture) object).isParticleWrapper())
						{
							level.addParticleEmitter(((WrapperTexture) object).getEmitter());
						}
					}
				}

				Set<ICopyable<?>> clipboardCopy = new HashSet<ICopyable<?>>();

				for (ICopyable<?> object : clipboard)
				{
					ICopyable<?> copy = object.copy(level);
					copy.setPosition(copy.getPosition().x + 7, copy.getPosition().y + 7);
					clipboardCopy.add(copy);
				}

				clipboard.clear();

				clipboard.addAll(clipboardCopy);

				Debug.log("Pasted " + selectedObjects.size() + " objects.");
			}

			if (!lockInput)
			{
				if (input.isKeyJustPressed(Input.Keys.TAB))
				{
					visibiltyButton.onClick();
				}

				if (input.isKeyJustPressed(Input.Keys.ENTER))
				{
					controlPanel.setActive(true);
				}
			}

			if (selectedObjects.size() == 1 && selectedWrappers.size() == 1 && !lockInput)
			{
				WrapperTexture wrapper = selectedWrappers.iterator().next();
				WrapperTexture next = null;

				if (input.isKeyJustPressed(Input.Keys.G))
				{
					next = importPanel.getNextWrapper(wrapper);
				}

				if (input.isKeyJustPressed(Input.Keys.F))
				{
					next = importPanel.getNextGroupWrapper(wrapper);
				}

				if (next != null)
				{
					float beforeX = wrapper.getHitbox().getCenterX();
					float beforeY = wrapper.getHitbox().getCenterY();

					wrapper.setImages(next);
					wrapper.softReset();

					wrapper.adjustX(beforeX - wrapper.getHitbox().getCenterX());
					wrapper.adjustY(beforeY - wrapper.getHitbox().getCenterY());

					wrapper.updateBorders();

					if (wrapper.isParticleWrapper())
					{
						level.getParticleSystem().reSelectImages(wrapper.getEmitter());

						if (wrapper.getEmitter().hasMirror())
						{
							level.getParticleSystem().reSelectImages(wrapper.getEmitter().getMirroredEmitter());
						}
					}

					onWrapperSelection(wrapper);
				}
			}

			if (input.isKeyJustPressed(Input.Keys.I))
			{
				for (Tile tile : selectedTiles)
				{
					tile.nextInnertype();
					tile.onInnerTypeChanged();

					if (tile.hasMirror())
					{
						tile.getMirroredTile().nextInnertype();
						tile.getMirroredTile().onInnerTypeChanged();
						tile.initMirror(tile.getMirroredTile(), level.getType());

						tile.getMirroredTile().updateBorders();
					}

					tile.updateBorders();

					if (tile.isShared())
					{
						tile.setShared(false);
						tile.setShared(true);
					}

					if (!tile.isTimeEffected())
					{
						tile.setTimeEffected(true);
						tile.setTimeEffected(false);
					}

					if (tile.blocksDimensionTravel())
					{
						tile.setBlocksDimensionTravel(false);
						tile.setBlocksDimensionTravel(true);
					}
				}
			}

			for (Tile tile : tiles)
			{
				if (tile instanceof ITileReceiver)
				{
					tile.listen();

					if (tile.isAnimated())
					{
						tile.updateAnimation(delta);
					}
				}
			}

			if (movingTiles && input.isKeyPressed(Input.Keys.C))
			{
				float shortestDistance = MAX_CONCATENATION_RANGE + 1;
				Vector2 firstPoint = new Vector2();
				Vector2 secondPoint = new Vector2();
				Vector2 firstPointBuffer = new Vector2();
				Vector2 secondPointBuffer = new Vector2();
				Tile selectedTile = null;

				for (Tile tile : selectedTiles)
				{
					tile.setX(Math.round(tile.getX()));
					tile.setY(Math.round(tile.getY()));

					for (Tile otherTile : tiles)
					{
						if (otherTile != tile && otherTile != tile.getMirror() && !selectedTiles.contains(otherTile))
						{
							for (int i = 0; i < tile.getHitbox().getPoints().length; i += 2)
							{
								firstPointBuffer.set(tile.getHitbox().getPoints()[i], tile.getHitbox().getPoints()[i + 1]);

								for (int j = 0; j < otherTile.getHitbox().getPoints().length; j += 2)
								{
									secondPointBuffer.set(otherTile.getHitbox().getPoints()[j], otherTile.getHitbox().getPoints()[j + 1]);

									buffer.set(secondPointBuffer);
									buffer.sub(firstPointBuffer);

									if (buffer.len() <= MAX_CONCATENATION_RANGE && buffer.len() < shortestDistance)
									{
										selectedTile = tile;
										firstPoint.set(firstPointBuffer);
										secondPoint.set(secondPointBuffer);

										shortestDistance = buffer.len();
									}
								}
							}
						}
					}
				}

				if (selectedTile != null)
				{
					for (Tile tile : selectedTiles)
					{
						boolean leftConnected = tile.getHitbox().isConnectedLeft();
						boolean rightConnected = tile.getHitbox().isConnectedRight();

						tile.adjustX(secondPoint.x - firstPoint.x);
						tile.adjustY(secondPoint.y - firstPoint.y);

						if (tile.getDimension() == Dimension.COLORED && tile.hasMirror())
						{
							Tile mirror = tile.getMirroredTile();

							mirror.adjustX(secondPoint.x - firstPoint.x);
							mirror.adjustY(secondPoint.y - firstPoint.y);

							mirror.updateBorders();
						}

						tile.updateBorders();
						tile.init();

						if (leftConnected ^ tile.getHitbox().isConnectedLeft())
						{
							tile.getLeftConnectedTile().init();
						}

						if (rightConnected ^ tile.getHitbox().isConnectedRight())
						{
							tile.getRightConnectedTile().init();
						}
					}

					for (Entity entity : selectedEntities)
					{
						entity.adjustX(secondPoint.x - firstPoint.x);
						entity.adjustY(secondPoint.y - firstPoint.y);
					}

					for (WrapperTexture wrapper : selectedWrappers)
					{
						wrapper.adjustX(secondPoint.x - firstPoint.x);
						wrapper.adjustY(secondPoint.y - firstPoint.y);
					}
				}
			}

			if (!input.isKeyJustPressed(Input.Keys.C) && !lockInput && input.isKeyJustPressed(Input.Keys.NUM_0) && !cannonPanel.isActive() && !editWrapperPanel.isActive() && !editEmitterPanel.isActive())
			{
				for (Tile tile : selectedTiles)
				{
					tile.changeRotationTo(0);
					tile.updateBorders();
				}

				for (WrapperTexture wrapper : selectedWrappers)
				{
					if (wrapper.isParticleWrapper())
					{
						wrapper.getEmitter().rotate(-wrapper.getRotation());

						if (wrapper.getEmitter().hasMirror())
						{
							wrapper.getEmitter().getMirroredEmitter().rotate(-wrapper.getRotation());
						}
					}

					wrapper.setRotation(0);
					wrapper.updateBorders();
				}
			}

			if (input.isKeyJustPressed(Input.Keys.FORWARD_DEL) && !lockInput)
			{
				if (!selectedTiles.isEmpty())
				{
					for (Tile tile : selectedTiles)
					{
						if (tile.hasMirror())
						{
							level.removeTile(tile.getMirroredTile());
						}

						level.removeTile(tile);
						level.transferBuffers();

						if (tile.getHitbox().isConnectedLeft())
						{
							tile.getLeftConnectedTile().disconnectRight(tile);
						}

						if (tile.getHitbox().isConnectedRight())
						{
							tile.getRightConnectedTile().disconnectLeft(tile);
						}

						if (tile instanceof TileCameraControl)
						{
							MapManager.initLevel(null, level);
						}
					}

					selectedObjects.removeAll(selectedTiles);
					selectedTiles.clear();
				}

				if (!selectedWrappers.isEmpty())
				{
					for (WrapperTexture wrapper : selectedWrappers)
					{
						level.removeWrapper(wrapper);

						if (wrapper == editWrapperPanel.getWrapper())
						{
							editWrapperPanel.init(null);
						}
						else if (wrapper == editEmitterPanel.getWrapper())
						{
							editEmitterPanel.init(null);
						}
					}

					selectedObjects.removeAll(selectedWrappers);
					selectedWrappers.clear();
				}

				if (!selectedObjects.isEmpty())
				{
					for (ICopyable<?> copyable : selectedObjects)
					{
						if (copyable instanceof Entity)
						{
							Entity entity = (Entity) copyable;

							if (entity.hasEvilTwin())
							{
								level.removeEntity(entity.getMirroredEntity());
							}

							level.removeEntity(entity);
						}
					}

					selectedObjects.removeAll(entities);
				}
			}

			if (clickCooldown >= 0)
			{
				clickCooldown -= delta;
			}

			if (leaveCooldown >= 0)
			{
				leaveCooldown -= delta;
			}

			level.updateTransitions(delta);
			level.updateParticleSystem(delta, delta, level.getLevelType() == LevelType.TIME_SCALE ? Level.TIME_SCALE_BACKGROUND_FACTOR * delta : delta);
		}

		toggleLightsButton.toggle(!ShaderHandler.isEnabled());

		if (anyContains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
		{
			CursorType.useCursor(CursorType.DEFAULT);
		}
		else
		{
			boolean anyOwnsMouse = false;

			for (ICopyable<?> object : selectedObjects)
			{
				if (object.ownsMouse())
				{
					anyOwnsMouse = true;

					break;
				}
			}

			if (movingWrappers || movingTiles || movingEntities || canGrab || anyOwnsMouse)
			{
				CursorType.useCursor(CursorType.GRAB);
			}
			else if (currentEntityType != null || currentTile != null)
			{
				CursorType.useCursor(CursorType.SET);
			}
			else
			{
				CursorType.useCursor(CursorType.DEFAULT);
			}
		}

		if (movingTiles || movingWrappers || movingEntities)
		{
			movingTime += delta;
		}
		else
		{
			movingTime = 0;
		}

		selectionColor.a += 0.00072f * delta * (increasingSelection ? 1.0f : -1.0f);

		if (selectionColor.a < 0.5f)
		{
			increasingSelection = true;
		}
		else if (selectionColor.a >= 1.0f)
		{
			selectionColor.a = 1.0f;

			increasingSelection = false;
		}

		selectionColor.g = selectionColor.a;

		Tile.hoverColor.a = selectionColor.a;

		level.transferBuffers();

		Profiling.endSection("mapeditor");
	}

	private static void onWrapperSelection(WrapperTexture wrapper)
	{
		if (!selectedWrappers.contains(wrapper) && wrapper.isParticleWrapper() && wrapper.getEmitter().hasLimitedLifetime())
		{
			wrapper.getEmitter().restart();

			if (wrapper.getEmitter().hasMirror())
			{
				wrapper.getEmitter().getMirroredEmitter().restart();
			}
		}

		if (wrapper.hasFoundTextures())
		{
			if (wrapper.isParticleWrapper())
			{
				editEmitterPanel.init(wrapper);
			}
			else
			{
				editWrapperPanel.init(wrapper);
			}
		}
		else
		{
			moveWrapperPanel.init(wrapper);
		}
	}

	private static boolean isObjectVisible(WrapperTexture wrapper)
	{
		return !wrapper.isParticleWrapper() && (level.shouldRenderWrappers() || level.shouldRenderBackgroundWrappers() && wrapper.isInBackground() || level.shouldRenderForegroundWrappers() && wrapper.isInForeground()) || level.shouldRenderParticleEmitters() && wrapper.isParticleWrapper();
	}

	private static boolean isObjectVisible(Tile tile)
	{
		return tile.isActive() && (level.shouldRenderAll() || tile.shouldUseOriginalTextures() && (level.shouldRenderForegroundWrappers() || level.shouldRenderWrappers())) && (tile.shouldRenderInGame() || renderDebugTiles);
	}

	private static boolean isObjectVisible(Entity entity)
	{
		return entity.isActive() && level.shouldRenderEntities();
	}

	private static void duplicateWrapper(WrapperTexture wrapper)
	{
		WrapperTexture duplicate = wrapper.duplicate(level);

		duplicate.adjustX(10);
		duplicate.adjustY(10);
		duplicate.createBorders();

		level.addWrapper(duplicate);

		if (duplicate.isParticleWrapper())
		{
			level.addParticleEmitter(duplicate.getEmitter());

			if (duplicate.getEmitter().hasMirror())
			{
				level.addParticleEmitter(duplicate.getEmitter().getMirroredEmitter());
			}
		}

		if (!Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT))
		{
			deselectAll();
		}

		onWrapperSelection(duplicate);

		selectedObjects.add(duplicate);
		selectedWrappers.add(duplicate);
	}

	private static void rotateTile(Tile tile, float rotationChange)
	{
		boolean leftConnected = tile.getHitbox().isConnectedLeft();
		boolean rightConnected = tile.getHitbox().isConnectedRight();

		float newRotation = tile.getRotation() + rotationChange;

		tile.setRotation(0.0f);

		if (tile.hasMirror())
		{
			tile.getMirroredTile().setRotation(0.0f);
		}

		if (Math.abs(newRotation) > 90.0f && tile.isRotationLimited())
		{
			newRotation = tile.onRotationOverflow(newRotation);

			if (tile.hasMirror())
			{
				tile.getMirroredTile().onRotationOverflow(newRotation);
			}
		}

		tile.setRotation(newRotation);

		if (tile.hasMirror())
		{
			tile.getMirroredTile().setRotation(newRotation);
		}

		tile.init();

		if (leftConnected ^ tile.getHitbox().isConnectedLeft())
		{
			tile.getLeftConnectedTile().init();
		}

		if (rightConnected ^ tile.getHitbox().isConnectedRight())
		{
			tile.getRightConnectedTile().init();
		}

		tile.updateBorders();
	}

	private static boolean anyContains(int mouseX, int mouseY)
	{
		return !selecting && !(movingTime >= 500 && (movingTiles || movingWrappers || movingEntities)) && (importPanel.contains(mouseX, mouseY) || terrainSelection.contains(mouseX, mouseY) || controlPanel.contains(mouseX, mouseY) || entitySelection.contains(mouseX, mouseY) || moveWrapperPanel.contains(mouseX, mouseY) || editWrapperPanel.contains(mouseX, mouseY) || editEmitterPanel.contains(mouseX, mouseY) || doorPanel.contains(mouseX, mouseY) || textPanel.contains(mouseX, mouseY) || platformPanel.contains(mouseX, mouseY) || cannonPanel.contains(mouseX, mouseY) || GameLauncher.getPerformancePanel().contains(mouseX, mouseY) || GameLauncher.getCakePanel().contains(mouseX, mouseY) || holePanel.contains(mouseX, mouseY) || soundSourcePanel.contains(mouseX, mouseY) || lightPanel.contains(mouseX, mouseY) || renamePanel.contains(mouseX, mouseY));
	}

	private static void onSelectTile(Tile tile)
	{
		if (tile.getHitbox().contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()))
		{
			if (tile instanceof TileDoor)
			{
				doorPanel.init((TileDoor) tile);
				doorPanel.setActive(true);
			}
			else if (tile instanceof TilePlatform)
			{
				platformPanel.init((TilePlatform) tile);
				platformPanel.setActive(true);
			}
			else if (tile instanceof TileCannon)
			{
				cannonPanel.init((TileCannon) tile);
				cannonPanel.setActive(true);
			}
			else if (tile instanceof TileLight)
			{
				lightPanel.init((TileLight) tile);
				lightPanel.setActive(true);
			}
			else if (tile instanceof TileSoundSource)
			{
				soundSourcePanel.init((TileSoundSource) tile);
				soundSourcePanel.setActive(true);
			}
			else if (tile instanceof TileHole)
			{
				holePanel.init((TileHole) tile);
				holePanel.setActive(true);
			}
			else if (tile instanceof TileText)
			{
				textPanel.init((TileText) tile);
				textPanel.setActive(true);
			}
		}
	}

	public static void saveItAll()
	{
		TerrainFileHandler terrainFile = new TerrainFileHandler(FileManager.getFileQuietly(mapPath + "/terrain.map"));
		EntityFileHandler entityFile = new EntityFileHandler(FileManager.getFileQuietly(mapPath + "/entities.map"));
		terrainFile.write(tiles, level);

		entityFile.write(entities);

		try
		{
			WrapperTextureHandler wrapperFiles = new WrapperTextureHandler(FileManager.getFile(mapPath + "/wrappers.cfg"));

			wrapperFiles.write(wrappers);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not save wrapper information. Damnit. " + e);
		}

		int cageCount = 0;

		for (Tile tile : tiles)
		{
			if (tile instanceof TileCage)
			{
				cageCount++;
			}
		}

		ConfigFileHandler cfg = new ConfigFileHandler(FileManager.getFileQuietly(mapPath + "/config.yml"));

		cfg.set("type", level.getType().ordinal() + "");
		cfg.set("totalcages", cageCount + "");

		cfg.writeQuietly();

		MenuHandler.displaySavedMessage("Saved " + mapID + " ...");
	}

	private static void readConfig()
	{
		ConfigFileHandler cfg = new ConfigFileHandler(FileManager.getFileQuietly(mapPath + "/config.yml"));

		mapName = cfg.getString("name");

		level.setLevelType(LevelType.values()[cfg.getInteger("type")]);
		level.createTimeStepHandler();
		level.createSoundManager();
	}

	public static void changeCurrentBlock(TileType tileType, int innerType)
	{
		tileType.getTileName();
	}

	public static void changeCurrentObject(String name)
	{
		Tile tile = Tile.createTile(TileType.getTileType(name), "", 0, 0, new Vector2(), level, Dimension.COLORED, TileType.getProperty(name), false);

		if (tile != null)
		{
			currentTile = tile;
			currentTile.setDimension(Dimension.COLORED);
		}
		else
		{
			changeCurrentBlock(TileType.getTileType(name), 0);
		}
	}

	public static void deselectAll()
	{
		selectedEntities.clear();
		selectedWrappers.clear();
		selectedTiles.clear();
		selectedObjects.clear();
		editWrapperPanel.init(null);
		editEmitterPanel.init(null);
		moveWrapperPanel.init(null);
		platformPanel.setActive(false);
		lightPanel.setActive(false);
		soundSourcePanel.setActive(false);
		holePanel.setActive(false);
		renamePanel.setActive(false);
		cannonPanel.setActive(false);
		doorPanel.setActive(false);
		textPanel.setActive(false);
	}

	public static void setEntityName(String entityName)
	{
		MapEditor.currentEntityType = entityName;
	}

	private static Tile getTileAtXY(float x, float y)
	{
		for (int i = tiles.size() - 1; i >= 0; i--)
		{
			Tile tile = tiles.get(i);

			if (isObjectVisible(tile) && tile.getDimension() == Dimension.COLORED && tile.getHitbox().contains(x, y))
			{
				return tile;
			}
		}

		return null;
	}

	private static Entity getEntityAtXY(float x, float y)
	{
		for (Entity entity : entities)
		{
			if (entity.getDimension() == Dimension.COLORED && entity.getHitbox().contains(x, y))
			{
				return entity;
			}
		}

		return null;
	}

	public static void reload()
	{
		float cameraOffsetX = level.getCameraOffset().x;
		float cameraOffsetY = level.getCameraOffset().y;

		initLevel(mapID, mapPath);

		adjustCameraOffset(cameraOffsetX, cameraOffsetY);
	}

	public static void adjustCameraOffset(float x, float y)
	{
		boolean lockSelected = false;

		for (ICopyable<?> object : selectedObjects)
		{
			if (object.getHitbox().contains(InputHandler.getCurrentInputX(), InputHandler.getCurrentInputY()) && (movingTiles || movingEntities || movingWrappers))
			{
				lockSelected = true;

				break;
			}
		}

		for (Entity entity : entities)
		{
			if (!(lockSelected && (selectedEntities.contains(entity) || selectedEntities.contains(entity.getMirroredEntity()))))
			{
				entity.setX(entity.getX() + x);
				entity.setY(entity.getY() + y);
			}
		}

		boolean changedCannon = false;

		for (Tile tile : tiles)
		{
			if (!(lockSelected && (selectedTiles.contains(tile) || selectedTiles.contains(tile.getMirroredTile()))))
			{
				tile.setX(tile.getX() + x);
				tile.setY(tile.getY() + y);
			}
			else
			{
				if (tile == cannonPanel.getCannon())
				{
					changedCannon = true;
				}
			}
		}

		if (changedCannon)
		{
			cannonPanel.updateLine();
		}

		for (Tile tile : selectedTiles)
		{
			tile.updateBorders();
		}

		level.updateWrapperPositions(x, y);

		for (WrapperTexture wrapper : selectedWrappers)
		{
			if (lockSelected)
			{
				wrapper.adjustX(-x * wrapper.getEmitterParallax());
				wrapper.adjustY(-y * wrapper.getEmitterParallax());

				if (wrapper.isParticleWrapper())
				{
					wrapper.getEmitter().moveEmitter(-x * wrapper.getEmitterParallax(), -y * wrapper.getEmitterParallax());

					if (wrapper.getEmitter().hasMirror())
					{
						wrapper.getEmitter().getMirroredEmitter().moveEmitter(-x * wrapper.getEmitterParallax(), -y * wrapper.getEmitterParallax());
					}
				}
			}

			if (wrapper.hasBorders())
			{
				wrapper.updateBorders();
			}
		}

		level.getCameraOffset().x += x;
		level.getCameraOffset().y += y;
	}

	private static boolean shouldDisableGUI()
	{
		return movingTime >= 500 && (movingTiles || movingWrappers || movingEntities) || selecting;
	}

	private static boolean shouldDrawLines()
	{
		return !Gdx.input.isKeyPressed(Input.Keys.Y);
	}

	public static void clearTerrainSelection()
	{
		terrainSelection.clearSelection();
	}

	public static void clearEntitySelection()
	{
		entitySelection.clearSelection();
	}

	public static void pause()
	{
		paused = true;
	}

	public static void resume()
	{
		paused = false;
		GameLauncher.clearRecords();
	}

	private enum Visibility
	{
		ALL, IN_GAME, FOREGROUND, BACKGROUND, COLLISION, PARTICLES;
	}

	public static boolean isPaused()
	{
		return paused;
	}

	public static void setClickCooldown(int clickCooldown)
	{
		MapEditor.clickCooldown = clickCooldown;
	}

	public static Level getLevel()
	{
		return level;
	}

	public static ImportWrapperPanel getImportPanel()
	{
		return importPanel;
	}

	public static void clearHoveredObjects()
	{
		hoveredObjects.clear();
	}

	public static Set<WrapperTexture> getSelectedWrappers()
	{
		return selectedWrappers;
	}

	public static Set<ICopyable<?>> getSelectedObjects()
	{
		return selectedObjects;
	}

	public static Set<Entity> getSelectedEntities()
	{
		return selectedEntities;
	}

	public static Set<Tile> getSelectedTiles()
	{
		return selectedTiles;
	}

	public static Set<ICopyable<?>> getHoveredObjects()
	{
		return hoveredObjects;
	}

	public static boolean isLockingInput()
	{
		return lightPanel != null && lightPanel.shouldLockInput() || renamePanel != null && renamePanel.isActive() || textPanel != null && textPanel.isActive() || moveWrapperPanel != null && moveWrapperPanel.isActive() || editWrapperPanel != null && editWrapperPanel.shouldLockInput() || editEmitterPanel != null && editEmitterPanel.shouldLockInput() || importPanel != null && importPanel.shouldLockInput();
	}
}
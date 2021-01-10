package com.jelte.norii.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.jelte.norii.entities.AiEntity;
import com.jelte.norii.entities.Entity;
import com.jelte.norii.entities.PlayerEntity;
import com.jelte.norii.magic.Ability;
import com.jelte.norii.profile.ProfileManager;
import com.jelte.norii.profile.ProfileObserver;
import com.jelte.norii.screen.BattleScreen;
import com.jelte.norii.utility.AssetManagerUtility;

public class Hud implements ProfileObserver {
	private Stage stage;
	private Image onTileHover;
	private Label playerVictoryMessage;
	private Label aiVictoryMessage;
	private HashMap<Integer, HpBar> entityIdWithHpBar;
	private HashMap<Integer, StatusUi> entityIdWithStatusUi;
	private HashMap<Integer, ActionsUi> entityIdWithActionUi;
	private HashMap<Integer, ActionInfoUiWindow> entityIdWithActionInfoUiWindow;
	private PortraitAndStats portraitAndStats;
	private BattleScreen battleScreen;

	private int mapWidth;
	private int mapHeight;

	private float tilePixelWidth;
	private float tilePixelHeight;

	public static final float UI_VIEWPORT_WIDTH = 400f;
	public static final float UI_VIEWPORT_HEIGHT = 400f;

	public Hud(List<PlayerEntity> playerUnits, List<AiEntity> aiUnits, SpriteBatch spriteBatch, int mapWidth, int mapHeight, BattleScreen battleScreen) {
		final List<Entity> allUnits = Stream.concat(playerUnits.stream(), aiUnits.stream()).collect(Collectors.toList());
		initVariables(spriteBatch, mapWidth, mapHeight, battleScreen);
		createEndGameMessages();
		createTileHoverParticle();
		createCharacterHUD();
		for (final Entity entity : allUnits) {
			addUnit(entity);
		}
	}

	private void initVariables(SpriteBatch spriteBatch, int mapWidth, int mapHeight, BattleScreen battleScreen) {
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		this.battleScreen = battleScreen;
		tilePixelWidth = UI_VIEWPORT_WIDTH / mapWidth;
		tilePixelHeight = UI_VIEWPORT_HEIGHT / mapHeight;
		entityIdWithHpBar = new HashMap<>();
		entityIdWithStatusUi = new HashMap<>();
		entityIdWithActionUi = new HashMap<>();
		entityIdWithActionInfoUiWindow = new HashMap<>();
		stage = new Stage(new FitViewport(UI_VIEWPORT_WIDTH, UI_VIEWPORT_HEIGHT), spriteBatch);
	}

	private void createEndGameMessages() {
		playerVictoryMessage = new Label("You Win!", AssetManagerUtility.getSkin(), "bigFont");
		aiVictoryMessage = new Label("You Lose!", AssetManagerUtility.getSkin(), "bigFont");

		playerVictoryMessage.setPosition((mapWidth / 8.0f) * tilePixelWidth, (mapHeight / 2.0f) * tilePixelHeight);
		aiVictoryMessage.setPosition((mapWidth / 8.0f) * tilePixelWidth, (mapHeight / 2.0f) * tilePixelHeight);

		playerVictoryMessage.setVisible(false);
		aiVictoryMessage.setVisible(false);

		stage.addActor(playerVictoryMessage);
		stage.addActor(aiVictoryMessage);
	}

	private void createTileHoverParticle() {
		final TextureRegion tr = new TextureRegion(AssetManagerUtility.getSprite(AssetManagerUtility.TILE_HOVER_IMAGE));
		final TextureRegionDrawable trd = new TextureRegionDrawable(tr);
		onTileHover = new Image(trd);
		onTileHover.setPosition(-1, -1);
		onTileHover.setSize(tilePixelWidth, tilePixelHeight);
		onTileHover.getDrawable().setMinHeight(tilePixelHeight);
		onTileHover.getDrawable().setMinWidth(tilePixelWidth);

		stage.addActor(onTileHover);
	}

	public void addUnit(Entity entity) {
		createHpBar(entity);
		portraitAndStats.linkUnit(entity);
		createStatusUI(entity);
		if (entity.isPlayerUnit()) {
			createActionUI((PlayerEntity) entity);
		}
	}

	public void removeUnit(Entity entity) {
		final Integer id = entity.getEntityID();
		if (entityIdWithStatusUi.containsKey(id)) {
			entityIdWithStatusUi.get(id).remove();
			entityIdWithStatusUi.remove(id);
		}

		if (entityIdWithHpBar.containsKey(id)) {
			entityIdWithHpBar.get(id).getHealthBar().remove();
			entityIdWithHpBar.remove(id);
		}

		if (entityIdWithActionUi.containsKey(id)) {
			entityIdWithActionUi.get(id).remove();
			entityIdWithActionUi.remove(id);
		}
	}

	private void createHpBar(Entity entity) {
		final HpBar hpBar = new HpBar(entity, mapWidth, mapHeight);
		stage.addActor(hpBar.getHealthBar());
		entityIdWithHpBar.put(entity.getEntityID(), hpBar);
	}

	private void createCharacterHUD() {
		portraitAndStats = new PortraitAndStats(mapWidth, mapHeight);
		stage.addActor(portraitAndStats.getTable());
	}

	private void createStatusUI(Entity entity) {
		final StatusUi statusui = new StatusUi(entity, mapWidth, mapHeight);
		entityIdWithStatusUi.put(entity.getEntityID(), statusui);

		statusui.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});

		stage.addActor(statusui);
	}

	private void createActionUI(PlayerEntity playerUnit) {
		final ActionsUi actionui = new ActionsUi(playerUnit, mapWidth, mapHeight, this);
		entityIdWithActionUi.put(playerUnit.getEntityID(), actionui);

		actionui.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return true;
			}
		});

		stage.addActor(actionui);

		for (final ActionInfoUiWindow popUp : actionui.getPopUps()) {
			entityIdWithActionInfoUiWindow.put(playerUnit.getEntityID(), popUp);
			stage.addActor(popUp);
		}
	}

	public void setPositionTileHover(int tileX, int tileY) {
		onTileHover.setPosition(tileX * tilePixelWidth, tileY * tilePixelHeight);
	}

	public void update(List<Entity> units) {
		for (final Entity entity : units) {
			final int id = entity.getEntityID();
			entityIdWithHpBar.get(id).update(entity);
			entityIdWithStatusUi.get(id).update(entity);
			entityIdWithActionUi.get(id).update(entity);
			entityIdWithActionInfoUiWindow.get(id).update();
		}
	}

	public void showPlayerWin() {
		playerVictoryMessage.setVisible(true);
	}

	public void showAiWin() {
		aiVictoryMessage.setVisible(true);
	}

	public Stage getStage() {
		return stage;
	}

	public Image getTileHoverImage() {
		return onTileHover;
	}

	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	public void render(float delta) {
		stage.act(delta);
		stage.draw();
	}

	public void dispose() {
		stage.dispose();
	}

	@Override
	public void onNotify(ProfileManager profileManager, ProfileEvent event) {
		// no-op
	}

	public List<ActionsUi> getActionUIs() {
		return (List<ActionsUi>) entityIdWithActionUi.values();
	}

	public List<StatusUi> getStatusUIs() {
		return (List<StatusUi>) entityIdWithStatusUi.values();
	}

	public Map<Integer, HpBar> getEntityIdWithHpBar() {
		return entityIdWithHpBar;
	}

	public Map<Integer, StatusUi> getEntityIdWithStatusUi() {
		return entityIdWithStatusUi;
	}

	public Map<Integer, ActionsUi> getEntityIdWithActionUi() {
		return entityIdWithActionUi;
	}

	public Map<Integer, ActionInfoUiWindow> getEntityIdWithActionInfoUiWindow() {
		return entityIdWithActionInfoUiWindow;
	}

	public void sendMessage(MessageToBattleScreen message, int entityID, Ability ability) {
		battleScreen.messageFromUi(message, entityID, ability);
	}
}

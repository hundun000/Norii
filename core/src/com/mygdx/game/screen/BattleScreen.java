package com.mygdx.game.screen;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.xguzm.pathfinding.grid.GridCell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.mygdx.game.ai.AITeamLeader;
import com.mygdx.game.ai.AITeams;
import com.mygdx.game.audio.AudioObserver;
import com.mygdx.game.battle.BattleManager;
import com.mygdx.game.battle.BattleScreenInputProcessor;
import com.mygdx.game.entities.AiEntity;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.EntityObserver;
import com.mygdx.game.entities.EntityStage;
import com.mygdx.game.entities.Player;
import com.mygdx.game.entities.PlayerEntity;
import com.mygdx.game.magic.Ability;
import com.mygdx.game.map.BattleMap;
import com.mygdx.game.map.Map;
import com.mygdx.game.map.MapManager;
import com.mygdx.game.map.TiledMapActor;
import com.mygdx.game.map.TiledMapObserver;
import com.mygdx.game.particles.ParticleMaker;
import com.mygdx.game.particles.ParticleType;
import com.mygdx.game.profile.ProfileManager;
import com.mygdx.game.ui.PlayerBattleHUD;
import com.mygdx.game.ui.StatusUI;

import utility.TiledMapPosition;
import utility.Utility;

public class BattleScreen extends GameScreen implements EntityObserver, TiledMapObserver {
	public static final int VISIBLE_WIDTH = 25;
	public static final int VISIBLE_HEIGHT = 25;
	private static OrthographicCamera mapCamera = null;

	private OrthogonalTiledMapRenderer mapRenderer = null;
	private MapManager mapMgr;
	private BattleMap currentMap;
	private BattleManager battlemanager;
	private AITeamLeader aiTeamLeader;
	private InputMultiplexer multiplexer;
	private BattleScreenInputProcessor battlescreenInputProcessor;
	private OrthographicCamera hudCamera;
	private PlayerBattleHUD playerBattleHUD;
	private PauseMenuScreen pauseMenu;
	private List<PlayerEntity> playerUnits;
	private List<AiEntity> aiUnits;
	private List<Entity> allUnits;
	private EntityStage entityStage;

	private boolean isPaused;

	private static class VIEWPORT {
		static float viewportWidth;
		static float viewportHeight;
		static float virtualWidth;
		static float virtualHeight;
		static float physicalWidth;
		static float physicalHeight;
		static float aspectRatio;

	}

	public BattleScreen(AITeams aiTeams) {
		initializeVariables();
		initializeAI(aiTeams);
		initializeEntityStage();
		initializeHUD();
		initializePauseMenu();
		initializeInput();
		initializeUnits();
		initializeMap();
		initializeObservers();
		spawnAI();
	}

	private void initializeVariables() {
		playerUnits = Player.getInstance().getPlayerUnits();
		mapCamera = new OrthographicCamera();
		mapCamera.setToOrtho(false, VISIBLE_WIDTH, VISIBLE_HEIGHT);
		isPaused = false;
	}

	private void initializeAI(AITeams ai) {
		aiTeamLeader = new AITeamLeader(ai);
		aiUnits = aiTeamLeader.getTeam();
	}

	private void initializeEntityStage() {
		allUnits = ListUtils.union(playerUnits, aiUnits);
		entityStage = new EntityStage(allUnits);
	}

	private void initializeHUD() {
		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, VIEWPORT.physicalWidth, VIEWPORT.physicalHeight);
		playerBattleHUD = new PlayerBattleHUD(hudCamera, playerUnits, aiUnits);
	}

	private void initializePauseMenu() {
		pauseMenu = new PauseMenuScreen(hudCamera, this);
	}

	private void initializeInput() {
		battlescreenInputProcessor = new BattleScreenInputProcessor(this, mapCamera);
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(battlescreenInputProcessor);
		multiplexer.addProcessor(playerBattleHUD.getStage());
		multiplexer.addProcessor(entityStage);
		multiplexer.addProcessor(pauseMenu.getStage());
	}

	private void initializeMap() {
		battlemanager = new BattleManager(playerUnits, aiUnits, aiTeamLeader);
		battlescreenInputProcessor.setBattleManager(battlemanager);
		mapMgr = new MapManager();
		currentMap = (BattleMap) mapMgr.getCurrentMap();
		currentMap.setStage(battlemanager);
		battlemanager.setPathFinder(currentMap.getPathfinder());
	}

	private void spawnAI() {
		final List<TiledMapPosition> enemyStartPositions = currentMap.getEnemyStartPositions();
		aiTeamLeader.spawnAiUnits(enemyStartPositions);
		aiTeamLeader.setPathFinder(currentMap.getPathfinder());
	}

	private void initializeUnits() {
		playerUnits.forEach(playerEntity -> playerEntity.addEntityObserver(this));
		aiUnits.forEach(aiEntity -> aiEntity.addEntityObserver(this));
	}

	private void initializeObservers() {
		ProfileManager.getInstance().addObserver(playerBattleHUD);

		for (final TiledMapActor[] tmpa : currentMap.getTiledMapStage().getTiledMapActors()) {
			for (final TiledMapActor actor : tmpa) {
				actor.addTilemapObserver(this);
			}
		}
	}

	public BattleManager getBattlemanager() {
		return battlemanager;
	}

	@Override
	public void show() {
		notifyAudio(AudioObserver.AudioCommand.MUSIC_STOP, AudioObserver.AudioTypeEvent.MUSIC_TITLE2);
		resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		mapMgr.getCurrentTiledMap();

		multiplexer.addProcessor(currentMap.getTiledMapStage());
		Gdx.input.setInputProcessor(multiplexer);

		setupViewport(VISIBLE_WIDTH, VISIBLE_HEIGHT);
		mapCamera.position.set(currentMap.getMapWidth() / 2f, currentMap.getMapHeight() / 2f, 0f);
		mapRenderer = new OrthogonalTiledMapRenderer(mapMgr.getCurrentTiledMap(), Map.UNIT_SCALE);
		mapRenderer.setView(mapCamera);
		currentMap.makeSpawnParticles();

		final ExtendViewport vp = new ExtendViewport(VISIBLE_WIDTH, VISIBLE_HEIGHT, mapCamera);
		currentMap.getTiledMapStage().setViewport(vp);
		entityStage.setViewport(vp);
	}

	@Override
	public void hide() {
		// no-op
	}

	@Override
	public void render(final float delta) {
		if (isPaused) {
			updatePauseMenu();
			renderPauseMenu(delta);
		} else {
			updateElements(delta);
			renderElements(delta);
		}
	}

	private void updatePauseMenu() {
		pauseMenu.getStage().act();
	}

	private void renderPauseMenu(final float delta) {
		pauseMenu.getStage().getViewport().apply();
		pauseMenu.render(delta);
	}

	private void updateElements(final float delta) {
		playerBattleHUD.update();
		battlescreenInputProcessor.update();
		updateAI(delta);
		updateUnits(delta);
		updateUIHover();
		updateStages();
		updateCameras();
	}

	private void updateAI(float delta) {
		GdxAI.getTimepiece().update(delta);
		MessageManager.getInstance().update();
	}

	private void updateUnits(final float delta) {
		Player.getInstance().updateUnits(delta);
		aiTeamLeader.updateUnits(delta);
	}

	private void updateUIHover() {
		boolean hoverResult = false;
		for (final Entity unit : Player.getInstance().getPlayerUnits()) {
			if (unit.getEntityactor().isActionsHovering()) {
				hoverResult = true;
			}
		}
		for (final StatusUI ui : playerBattleHUD.getStatusuis()) {
			ui.setActionsUIHovering(hoverResult);
		}
	}

	private void updateStages() {
		entityStage.act();
		playerBattleHUD.getStage().act();
		currentMap.getTiledMapStage().act();
	}

	private void updateCameras() {
		mapCamera.position.x = Utility.clamp(mapCamera.position.x, currentMap.getTilemapWidthInTiles() - (mapCamera.viewportWidth / 2), 0 + (mapCamera.viewportWidth / 2));
		mapCamera.position.y = Utility.clamp(mapCamera.position.y, currentMap.getTilemapHeightInTiles() - (mapCamera.viewportHeight / 2), 0 + (mapCamera.viewportHeight / 2));
		mapCamera.update();
		hudCamera.update();
	}

	private void renderElements(final float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.graphics.setTitle("fps = " + Gdx.graphics.getFramesPerSecond());
		renderMap();
		renderGrid();
		renderUnits();
		renderParticles(delta);
		renderHUD(delta);
	}

	private void renderMap() {
		mapRenderer.setView(mapCamera);
		currentMap.getTiledMapStage().getViewport().apply();
		mapRenderer.render();
	}

	private void renderUnits() {
		mapRenderer.getBatch().begin();
		entityStage.getViewport().apply();
		Player.getInstance().renderUnits(mapRenderer.getBatch());
		aiTeamLeader.renderUnits(mapRenderer.getBatch());
		mapRenderer.getBatch().end();
	}

	private void renderParticles(final float delta) {
		mapRenderer.getBatch().begin();
		ParticleMaker.drawAllActiveParticles((SpriteBatch) mapRenderer.getBatch(), delta);
		mapRenderer.getBatch().end();
	}

	private void renderHUD(final float delta) {
		playerBattleHUD.getStage().getViewport().apply();
		playerBattleHUD.render(delta);
	}

	public void renderGrid() {
		for (int x = 0; x < currentMap.getMapWidth(); x += 1)
			Utility.drawDebugLine(new Vector2(x, 0), new Vector2(x, currentMap.getMapHeight()), mapCamera.combined);
		for (int y = 0; y < currentMap.getMapHeight(); y += 1)
			Utility.drawDebugLine(new Vector2(0, y), new Vector2(currentMap.getMapWidth(), y), mapCamera.combined);
	}

	@Override
	public void resize(final int width, final int height) {
		currentMap.getTiledMapStage().getViewport().update(width, height, false);
		entityStage.getViewport().update(width, height, false);

		playerBattleHUD.resize(width, height);
		pauseMenu.resize(width, height);
	}

	@Override
	public void pause() {
		isPaused = true;
		notifyAudio(AudioObserver.AudioCommand.MUSIC_PAUSE, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
		notifyAudio(AudioObserver.AudioCommand.SOUND_STOP, AudioObserver.AudioTypeEvent.WALK_LOOP);
		pauseMenu.setVisible(true);
	}

	@Override
	public void resume() {
		isPaused = false;
		notifyAudio(AudioObserver.AudioCommand.MUSIC_RESUME, AudioObserver.AudioTypeEvent.MUSIC_BATTLE);
		pauseMenu.setVisible(false);
	}

	@Override
	public void dispose() {
		Player.getInstance().dispose();
		aiTeamLeader.dispose();
		mapRenderer.dispose();
		pauseMenu.dispose();
		currentMap.dispose();
	}

	private static void setupViewport(final int width, final int height) {
		VIEWPORT.virtualWidth = width;
		VIEWPORT.virtualHeight = height;

		VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
		VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;

		VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
		VIEWPORT.physicalHeight = Gdx.graphics.getHeight();

		VIEWPORT.aspectRatio = (VIEWPORT.virtualWidth / VIEWPORT.virtualHeight);

		// letterbox
		if (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio) {
			VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight);
			VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
		} else {
			VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
			VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight / VIEWPORT.physicalWidth);
		}
	}

	@Override
	public void onEntityNotify(final EntityCommand command, final Entity unit) {
		switch (command) {
		case IN_MOVEMENT:
			prepareMove(unit);
			break;
		case IN_ATTACK_PHASE:
			prepareAttack(unit);
			break;
		case UNIT_LOCKED:
			battlemanager.setLockedUnit(unit);
			break;
		case CLICKED:
			battlemanager.getCurrentBattleState().clickedOnUnit(unit);
			break;
		case SKIP:
			battlemanager.setLockedUnit(null);
			battlemanager.setCurrentBattleState(battlemanager.getActionBattleState());
			battlemanager.getCurrentBattleState().exit();
			break;
		default:
			break;
		}
	}

	@Override
	public void onEntityNotify(final EntityCommand command, final AiEntity aiUnit) {
		switch (command) {
		case AI_FINISHED_TURN:
			aiUnit.setAp(aiUnit.getEntityData().getMaxAP());
			battlemanager.swapTurn();
			break;
		case FOCUS_CAMERA:
			mapCamera.position.set(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY(), 0f);
			break;
		case CLICKED:
			battlemanager.getCurrentBattleState().clickedOnUnit(aiUnit);
			break;
		default:
			break;
		}
	}

	@Override
	public void onEntityNotify(final EntityCommand command, final Entity unit, final Ability ability) {
		switch (command) {
		case IN_SPELL_PHASE:
			prepareSpell(unit, ability);
			break;
		default:
			break;
		}
	}

	private void prepareMove(final Entity unit) {
		final List<GridCell> path = currentMap.getPathfinder().getCellsWithinCircle(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY(), unit.getAp());
		for (final GridCell cell : path) {
			if (!isUnitOnCell(cell)) {
				final TiledMapPosition positionToPutMoveParticle = new TiledMapPosition().setPositionFromTiles(cell.x, cell.y);
				ParticleMaker.addParticle(ParticleType.MOVE, positionToPutMoveParticle, 0);
				battlemanager.setCurrentBattleState(battlemanager.getMovementBattleState());
			}
		}
	}

	private void prepareAttack(final Entity unit) {
		final List<GridCell> attackPath = currentMap.getPathfinder().getCellsWithinCircle(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY(), unit.getEntityData().getAttackRange());
		for (final GridCell cell : attackPath) {
			final TiledMapPosition positionToPutAttackParticle = new TiledMapPosition().setPositionFromTiles(cell.x, cell.y);
			ParticleMaker.addParticle(ParticleType.ATTACK, positionToPutAttackParticle, 0);
		}
		battlemanager.setCurrentBattleState(battlemanager.getAttackBattleState());
	}

	private void prepareSpell(final Entity unit, final Ability ability) {
		final List<Entity> allEntities = ListUtils.union(playerUnits, aiUnits);
		final List<TiledMapPosition> positions = allEntities.stream().map(Entity::getCurrentPosition).collect(Collectors.toList());
		final List<GridCell> spellPath = calculateSpellPath(unit, ability, positions);

		for (final GridCell cell : spellPath) {
			final TiledMapPosition positionToPutSpellParticle = new TiledMapPosition().setPositionFromTiles(cell.x, cell.y);
			ParticleMaker.addParticle(ParticleType.SPELL, positionToPutSpellParticle, 0);
		}

		battlemanager.setCurrentSpell(ability);
		battlemanager.setCurrentBattleState(battlemanager.getSpellBattleState());
	}

	private List<GridCell> calculateSpellPath(final Entity unit, final Ability ability, final List<TiledMapPosition> positions) {
		List<GridCell> spellPath = null;

		switch (ability.getLineOfSight()) {
		case CIRCLE:
			spellPath = currentMap.getPathfinder().getLineOfSightWithinCircle(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY(), ability.getSpellData().getRange(), positions);
			break;
		case CROSS:
			// TODO
			break;
		case LINE:
			spellPath = currentMap.getPathfinder().getLineOfSightWithinLine(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY(), ability.getSpellData().getRange(), unit.getEntityAnimation().getCurrentDirection(), positions);
			break;
		default:
			break;
		}
		return spellPath;
	}

	private boolean isUnitOnCell(final GridCell cell) {
		final TiledMapPosition cellToTiled = new TiledMapPosition().setPositionFromTiles(cell.x, cell.y);
		for (final Entity entity : allUnits) {
			if (entity.getCurrentPosition().isTileEqualTo(cellToTiled)) {
				return true;
			}
		}
		return false;
	}

	public static OrthographicCamera getCamera() {
		return mapCamera;
	}

	@Override
	public void onTiledMapNotify(final TilemapCommand command, final TiledMapPosition pos) {
		switch (command) {
		case HOVER_CHANGED:
			playerBattleHUD.getTileHoverImage().setPosition(pos.getCameraX(), pos.getCameraY());
			break;
		}
	}

	public boolean isPaused() {
		return isPaused;
	}
}

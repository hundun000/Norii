package com.jelte.norii.ai;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.jelte.norii.battle.BattleManager;
import com.jelte.norii.battle.MessageToBattleScreen;
import com.jelte.norii.battle.battleState.BattleState;
import com.jelte.norii.entities.Entity;
import com.jelte.norii.entities.EntityTypes;
import com.jelte.norii.entities.UnitOwner;
import com.jelte.norii.utility.TiledMapPosition;

public class AITeamLeader implements UnitOwner {
	private static final String TAG = AITeamLeader.class.getSimpleName();

	private List<Entity> team;
	private final AITeamData aiTeamData;
	private final AIDecisionMaker aiDecisionMaker;
	private BattleManager battleManager;

	public AITeamLeader(final AITeams type) {
		aiTeamData = AITeamFileReader.getAITeamData().get(type.ordinal());
		aiDecisionMaker = new AIDecisionMaker();
		initiateUnits();
	}

	private void initiateUnits() {
		team = new ArrayList<>();
		for (final String name : aiTeamData.getUnits()) {
			for (final EntityTypes type : EntityTypes.values()) {
				if (name.equals(type.getEntityName())) {
					final Entity entity = new Entity(type, this);
					entity.setPlayerUnit(false);
					team.add(entity);
				}
			}
		}
	}

	public void spawnAiUnits(List<TiledMapPosition> spawnPositions) {
		for (final Entity unit : team) {
			if (!spawnPositions.isEmpty()) {
				unit.setCurrentPosition(spawnPositions.get(0));
				unit.setPlayerUnit(false);
				unit.getVisualComponent().spawn(spawnPositions.get(0));
				spawnPositions.remove(0);
				battleManager.addUnit(unit);
			} else {
				Gdx.app.debug(TAG, "maybe no more room to spawn ai units!");
			}
		}
	}

	public void startCalculatingNextMove(BattleState stateOfBattle) {
		aiDecisionMaker.startCalculatingNextMove(stateOfBattle);
	}

	public void processAi() {
		if (aiDecisionMaker.processAi()) {
			sendMessageToBattleManager(MessageToBattleScreen.AI_FINISHED_CALCULATING, battleManager.getActiveUnit());
		}

	}

	public BattleState getNextBattleState() {
		return aiDecisionMaker.getResult();
	}

	@Override
	public void updateUnits(final float delta) {
		team.removeIf(Entity::isDead);

		for (final Entity entity : team) {
			entity.update(delta);
		}
	}

	@Override
	public void renderUnits(final Batch batch) {
		for (final Entity entity : team) {
			entity.draw(batch);
		}
	}

	@Override
	public void setTeam(final List<Entity> team) {
		this.team = team;
	}

	@Override
	public List<Entity> getTeam() {
		return team;
	}

	public void dispose() {
		for (final Entity entity : team) {
			entity.dispose();
		}
	}

	@Override
	public String toString() {
		return "AITeamLeader with team : " + team;
	}

	@Override
	public void applyModifiers() {
		team.forEach(Entity::applyModifiers);
	}

	@Override
	public boolean isPlayer() {
		return false;
	}

	@Override
	public void sendMessageToBattleManager(MessageToBattleScreen message, Entity entity) {
		battleManager.sendMessageToBattleScreen(message, entity);
	}

	@Override
	public void setBattleManager(BattleManager battleManager) {
		this.battleManager = battleManager;
	}

	@Override
	public void removeUnit(Entity unit) {
		team.remove(unit);
	}

	@Override
	public void addUnit(Entity unit) {
		team.add(unit);
	}

	@Override
	public void sendMessageToBattleManager(MessageToBattleScreen message, Entity entity, TiledMapPosition oldPosition) {
		battleManager.sendMessageToBattleScreen(message, entity, oldPosition);
	}
}

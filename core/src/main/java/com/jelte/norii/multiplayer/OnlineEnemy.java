package com.jelte.norii.multiplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.jelte.norii.ai.EnemyType;
import com.jelte.norii.battle.ApFileReader;
import com.jelte.norii.battle.BattleManager;
import com.jelte.norii.battle.MessageToBattleScreen;
import com.jelte.norii.battle.battleState.BattleState;
import com.jelte.norii.entities.Entity;
import com.jelte.norii.entities.EntityTypes;
import com.jelte.norii.entities.Player;
import com.jelte.norii.entities.UnitOwner;
import com.jelte.norii.multiplayer.NetworkMessage.MessageType;
import com.jelte.norii.utility.TiledMapPosition;

public class OnlineEnemy implements UnitOwner {

	private static final String TAG = OnlineEnemy.class.getSimpleName();

	private List<Entity> team;
	private BattleManager battleManager;
	private int ap;
	private EnemyType type;
	private String ownerName;
	private Json json;
	private boolean myTurn;
	private String gameID;
	private Alliance alliance;

	public OnlineEnemy(String ownerName, String teamAsString, boolean playerStart, String gameID) {
		team = new ArrayList<>();
		type = EnemyType.ONLINE_PLAYER;
		this.ownerName = ownerName;
		json = new Json();
		this.myTurn = playerStart;
		this.gameID = gameID;
		initiateUnits(teamAsString);
		if (playerStart) {
			alliance = Alliance.TEAM_BLUE;
			Player.getInstance().setAlliance(Alliance.TEAM_RED);
		} else {
			alliance = Alliance.TEAM_RED;
			Player.getInstance().setAlliance(Alliance.TEAM_BLUE);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void synchronizeMultiplayerUnitsWithLocal(String teamWithIdAsString) {
		HashMap<String, String> idWithUnitNames = json.fromJson(HashMap.class, teamWithIdAsString);
		List<String> synchronizedUnitIds = new ArrayList<>();
		for (final Entry<String, String> idWithName : idWithUnitNames.entrySet()) {
			for (final Entity entity : team) {
				if (!synchronizedUnitIds.contains(idWithName.getKey()) && (idWithName.getValue().equals(entity.getEntityData().getName())) && !synchronizedUnitIds.contains(Integer.toString(entity.getEntityID()))) {
					entity.setEntityID(Integer.parseInt(idWithName.getKey()));
					synchronizedUnitIds.add(idWithName.getKey());
				}
			}
		}
		ap = ApFileReader.getApData(0);
	}

	@SuppressWarnings("unchecked")
	private void initiateUnits(String teamAsString) {
		Array<String> teamNames = json.fromJson(Array.class, teamAsString);
		for (final String name : teamNames) {
			for (final EntityTypes entityType : EntityTypes.values()) {
				if (name.equals(entityType.getEntityName())) {
					final Entity entity = new Entity(entityType, this, true);
					entity.setPlayerUnit(false);
					team.add(entity);
				}
			}
		}
		ap = ApFileReader.getApData(0);
	}

	@Override
	public void spawnUnits(List<TiledMapPosition> spawnPositions) {
		// do nothing
	}

	@Override
	public void spawnUnit(String unitName, int unitID, TiledMapPosition spawnPosition) {
		for (final Entity unit : team) {
			if ((unit.getEntityID() == unitID) && unitName.equals(unit.getEntityType().name()) && !unit.isInBattle()) {
				unit.setCurrentPosition(spawnPosition);
				unit.setPlayerUnit(false);
				unit.getVisualComponent().spawn(spawnPosition);
				battleManager.addUnit(unit);
			}
		}
	}

	@Override
	public void resetAI(BattleState stateOfBattle) {
		// do nothing
	}

	@Override
	public void processAi() {
		// do nothing
	}

	@Override
	public BattleState getNextBattleState() {
		return null;
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

	@Override
	public void dispose() {
		for (final Entity entity : team) {
			entity.dispose();
		}
	}

	@Override
	public String toString() {
		return "Online player : " + ownerName + " with team : " + team;
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

	@Override
	public void sendMessageToBattleManager(MessageToBattleScreen message, Entity entity, int damage) {
		battleManager.sendMessageToBattleScreen(message, entity, damage);
	}

	@Override
	public int getAp() {
		return ap;
	}

	@Override
	public void setAp(int ap) {
		this.ap = ap;
	}

	@Override
	public EnemyType getType() {
		return type;
	}

	@Override
	public void setName(String name) {
		this.ownerName = name;
	}

	@Override
	public String getName() {
		return ownerName;
	}

	@Override
	public void playerUnitSpawned(Entity entity, TiledMapPosition pos) {
		NetworkMessage message = new NetworkMessage(MessageType.UNIT_DEPLOYED);
		message.makeUnitDeployedMessage(entity.getEntityType().name(), entity.getEntityID(), pos.toString(), gameID);
		ServerCommunicator.getInstance().sendMessage(message);
	}

	@Override
	public boolean isMyTurn() {
		return myTurn;
	}

	@Override
	public void setMyTurn(boolean myTurn) {
		this.myTurn = myTurn;
	}

	@Override
	public boolean isAI() {
		return false;
	}

	@Override
	public boolean isOnlinePlayer() {
		return true;
	}

	@Override
	public String getGameID() {
		return gameID;
	}

	@Override
	public Alliance getAlliance() {
		return alliance;
	}

}

package com.jelte.norii.entities;

import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.jelte.norii.battle.BattleManager;
import com.jelte.norii.battle.MessageToBattleScreen;

public class Player implements UnitOwner {
	private static Player instance;
	private List<Entity> team;
	private BattleManager battleManager;

	@Override
	public void updateUnits(final float delta) {
		for (final Entity entity : team) {
			entity.update(delta);

			if (!entity.getOldPlayerPosition().isTileEqualTo(entity.getCurrentPosition())) {
				entity.setOldPlayerPosition(entity.getCurrentPosition());
			}
		}
	}

	@Override
	public void renderUnits(final Batch batch) {
		for (final Entity entity : team) {
			entity.draw(batch);
		}
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
	public void setTeam(List<Entity> playerMonsters) {
		team = playerMonsters;
	}

	@Override
	public void applyModifiers() {
		// team.forEach(Entity::applyModifiers);
		final Iterator<Entity> entityIterator = team.iterator();
		while (entityIterator.hasNext()) {
			entityIterator.next().applyModifiers();
		}
	}

	@Override
	public String toString() {
		return "player with team : " + team;
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	public static Player getInstance() {
		if (instance == null) {
			instance = new Player();
		}
		return instance;
	}

	private Player() {

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
}

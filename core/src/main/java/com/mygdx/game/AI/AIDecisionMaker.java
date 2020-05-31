package com.mygdx.game.AI;

import java.util.ArrayList;
import java.util.List;

import org.xguzm.pathfinding.grid.GridCell;

import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Entities.EntityObserver.EntityCommand;
import com.mygdx.game.Map.MyPathFinder;

import Utility.TiledMapPosition;
import Utility.Utility;

public class AIDecisionMaker {
	private final AITeam aiTeam;
	private boolean actionTaken = false;

	public AIDecisionMaker(final AITeam aiTeam) {
		this.aiTeam = aiTeam;
	}

	public void makeDecision(final Entity unit, final ArrayList<Entity> entities) {

		rule1CanKill(unit, entities);

		if (!actionTaken) {
			rule2ShouldRun(unit, entities);
		}

		if (!actionTaken) {
			rule3SpellSpecific(unit, entities);
		}

		if (!actionTaken) {
			unit.notifyEntityObserver(EntityCommand.AI_FINISHED_TURN);
		}
		actionTaken = false;
	}

	private void rule1CanKill(final Entity unit, final ArrayList<Entity> entities) {
		for (final Entity target : entities) {
			if (isEnemy(unit, target) && canMoveAttack(unit, target) && canKill(unit, target)) {
				walkOverAndAttack(unit, target);
				actionTaken = true;
			}
		}
	}

	private boolean isEnemy(final Entity attacker, final Entity target) {
		return attacker.isPlayerUnit() != target.isPlayerUnit();
	}

	private boolean canMoveAttack(final Entity attacker, final Entity target) {
		final int distance = Utility.getDistanceBetweenUnits(attacker, target) - 1;
		final int ap = attacker.getAp();
		final int basicAttackPoints = attacker.getEntityData().getBasicAttackCost();
		return (ap >= (distance + basicAttackPoints));
	}

	private boolean canKill(final Entity attacker, final Entity target) {
		return target.getHp() < attacker.getEntityData().getAttackPower();
	}

	private void walkOverAndAttack(final Entity attacker, final Entity target) {
		final MyPathFinder pathFinder = aiTeam.getMyPathFinder();
		final List<GridCell> path = pathFinder.getPathFromUnitToUnit(attacker, target);
		attacker.moveAttack(path, target);
	}

	private void rule2ShouldRun(final Entity unit, final ArrayList<Entity> entities) {
		if (healthIsLow(unit)) {
			runToSafety(unit, entities);
			actionTaken = true;
		}
	}

	private boolean healthIsLow(final Entity unit) {
		final int currentHP = unit.getHp();
		final int maxHP = unit.getEntityData().getMaxHP();
		return (currentHP / maxHP) <= 0.1f;
	}

	private void runToSafety(final Entity unit, final ArrayList<Entity> entities) {
		final ArrayList<TiledMapPosition> positions = Utility.collectPositionsEnemeyUnits(entities, unit.isPlayerUnit());
		unit.move(getSafestPath(unit, positions));
	}

	private List<GridCell> getSafestPath(final Entity unit, final ArrayList<TiledMapPosition> positions) {
		final TiledMapPosition centerOfGravity = calculateCenterOfGravity(positions);
		final TiledMapPosition furthestPoint = aiTeam.getMyPathFinder().getPositionFurthestAwayFrom(centerOfGravity);
		return aiTeam.getMyPathFinder().pathTowards(unit.getCurrentPosition(), furthestPoint, unit.getAp());
	}

	private TiledMapPosition calculateCenterOfGravity(final ArrayList<TiledMapPosition> positions) {
		final int numberOfElements = positions.size();
		int sumX = 0;
		int sumY = 0;

		for (int i = 0; i < numberOfElements; i++) {
			sumX += positions.get(i).getTileX();
			sumY += positions.get(i).getTileY();
		}

		return new TiledMapPosition().setPositionFromTiles(sumX / numberOfElements, sumY / numberOfElements);
	}

	private void rule3SpellSpecific(final Entity unit, final ArrayList<Entity> entities) {

	}
}

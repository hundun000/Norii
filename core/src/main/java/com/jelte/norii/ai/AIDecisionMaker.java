package com.jelte.norii.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xguzm.pathfinding.grid.GridCell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.jelte.norii.battle.battleState.BattleState;
import com.jelte.norii.battle.battleState.BattleStateGridHelper;
import com.jelte.norii.battle.battleState.Move;
import com.jelte.norii.battle.battleState.MoveType;
import com.jelte.norii.battle.battleState.SpellMove;
import com.jelte.norii.entities.Entity;
import com.jelte.norii.entities.EntityTypes;
import com.jelte.norii.entities.FakeEntityVisualComponent;
import com.jelte.norii.magic.AbilitiesEnum;
import com.jelte.norii.magic.Ability;
import com.jelte.norii.magic.Ability.Target;
import com.jelte.norii.magic.Modifier;
import com.jelte.norii.magic.ModifiersEnum;
import com.jelte.norii.map.MyPathFinder;
import com.jelte.norii.utility.MyPoint;
import com.jelte.norii.utility.TiledMapPosition;
import com.jelte.norii.utility.Utility;

public class AIDecisionMaker {
	private final SortedMap<Integer, BattleState> statesWithScores;
	private final Array<Array<BattleState>> allBattleStates = new Array<>();
	private int turnIndex;
	private int entityIndex;
	private int currentStep;
	private int battleStateIndex;
	private int numberOfBattleStatesThisRound;
	private BattleState currentBattleState;

	private static final int NUMBER_OF_LAYERS = 3;
	private static final String TAG = AIDecisionMaker.class.getSimpleName();

	public AIDecisionMaker() {
		statesWithScores = new TreeMap<>();
		for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
			allBattleStates.add(new Array<>());
		}
	}

	public void startCalculatingNextMove(BattleState battleState) {
		currentBattleState = battleState;
		statesWithScores.clear();
		turnIndex = 0;
		numberOfBattleStatesThisRound = 1;
		for (int i = 0; i < NUMBER_OF_LAYERS; i++) {
			allBattleStates.get(i).clear();
		}
	}

	// returns true when finished
	public boolean processAi() {
		Entity unit;
		BattleState startingState;

		if (turnIndex == 0) {
			startingState = currentBattleState;
		} else {
			startingState = allBattleStates.get(turnIndex - 1).get(battleStateIndex);
		}

		// if turn is even, play AI
		if ((turnIndex % 2) == 0) {
			unit = startingState.getAiUnits().get(entityIndex);
		} else {
			unit = startingState.getPlayerUnits().get(entityIndex);
		}

		for (final Ability ability : unit.getAbilities()) {
			final Array<UnitTurn> turns = generateMoves(ability, unit, startingState);
			for (final UnitTurn turn : turns) {
				final BattleState newState = applyTurnToBattleState(unit, turn, startingState);
				newState.setTurn(turn);
				if (turnIndex != 0) {
					newState.setParentState(startingState);
				}
				allBattleStates.get(turnIndex).add(newState);
			}
		}

		entityIndex++;

		if ((turnIndex % 2) == 0) {
			if (entityIndex >= startingState.getAiUnits().size) {
				battleStateIndex++;
				entityIndex = 0;
			}
		} else {
			if (entityIndex >= startingState.getPlayerUnits().size) {
				battleStateIndex++;
				entityIndex = 0;
			}
		}

		if (battleStateIndex >= (numberOfBattleStatesThisRound - 1)) {
			// all steps from this round done, next round
			reduceModifierCount(allBattleStates.get(turnIndex));
			turnIndex++;
			currentStep = 0;
			battleStateIndex = 0;
			entityIndex = 0;

			if ((turnIndex % 2) == 0) {
				numberOfBattleStatesThisRound = allBattleStates.get(turnIndex - 1).size;
			} else {
				numberOfBattleStatesThisRound = allBattleStates.get(turnIndex - 1).size;
			}

			if (turnIndex >= NUMBER_OF_LAYERS) {
				// were done
				return true;
			}
		}
		return false;
	}

	public BattleState getResult() {
		allBattleStates.get(NUMBER_OF_LAYERS - 1).sort();
		return getInitialMoves(allBattleStates.get(NUMBER_OF_LAYERS - 1).get(0));
	}

	private boolean checkEndConditions(BattleState battleState) {
		return battleState.getPlayerUnits().isEmpty() || battleState.getAiUnits().isEmpty();
	}

	private BattleState getInitialMoves(BattleState battleState) {
		BattleState initialState = battleState;
		while (initialState.getParentState() != null) {
			initialState = initialState.getParentState();
		}
		return initialState;
	}

	private void reduceModifierCount(Array<BattleState> battleStates) {
		for (final BattleState battleState : battleStates) {
			battleState.reduceModifierCounts();
		}
	}

	private BattleState applyTurnToBattleState(Entity aiUnit, UnitTurn turn, BattleState battleState) {
		final BattleState newState = battleState.makeCopy();
		final Entity copyUnit = newState.get(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY()).getUnit();
		for (final Move move : turn.getMoves()) {
			switch (move.getMoveType()) {
			case SPELL:
				applySpellOnBattleState(copyUnit, (SpellMove) move, newState);
				break;
			case ATTACK:
				applyAttackOnBattleState(copyUnit, move, newState);
				break;
			case MOVE:
				newState.moveUnitTo(copyUnit, move.getLocation());
				break;
			case DUMMY:
				// do nothing
			default:
				// do nothing
			}
		}
		return newState;
	}

	private void applyAttackOnBattleState(Entity aiUnit, Move move, BattleState battleState) {
		final MyPoint attackLocation = move.getLocation();
		final int damage = aiUnit.getEntityData().getAttackPower();
		battleState.damageUnit(attackLocation, damage);
	}

	private void applySpellOnBattleState(Entity unit, SpellMove move, BattleState battleState) {
		final MyPoint casterPos = new MyPoint(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY());
		final Array<MyPoint> targets = move.getAffectedUnits();
		final MyPoint location = move.getLocation();
		final int damage = move.getAbility().getSpellData().getDamage();
		switch (move.getAbility().getAbilityEnum()) {
		case FIREBALL:
			battleState.damageUnit(location, damage);
			break;
		case INVISIBLE:
			battleState.addModifierToUnit(location.x, location.y, new Modifier(ModifiersEnum.INVISIBLE, 2, 0));
			break;
		case PUSH:
			battleState.pushOrPullUnit(casterPos, location, damage, false);
			break;
		case PULL:
			battleState.pushOrPullUnit(casterPos, location, damage, true);
			break;
		case ARROW:
			if (targets != null) {
				for (final MyPoint point : targets) {
					battleState.damageUnit(point, damage);
				}
			}
			break;
		case ICEFIELD:
			if (targets != null) {
				for (final MyPoint point : targets) {
					battleState.damageUnit(point, damage);
				}
			}
			break;
		case TURN_TO_STONE:
			battleState.addModifierToUnit(location.x, location.y, new Modifier(ModifiersEnum.STUNNED, 2, 0));
			break;
		case SWAP:
			final Entity placeHolder = battleState.get(location.x, location.y).getUnit();
			battleState.swapPositions(unit, placeHolder);
			break;
		case HAMMERBACK:
			final List<MyPoint> crossedCells = findLine(casterPos.x, casterPos.y, location.x, location.y);
			for (final MyPoint point : crossedCells) {
				if (battleState.get(point.x, point.y).isOccupied()) {
					battleState.damageUnit(point, damage);
				}
			}
			final Entity hammerBackUnit = new Entity(EntityTypes.BOOMERANG, unit.getOwner());
			hammerBackUnit.setVisualComponent(new FakeEntityVisualComponent());
			hammerBackUnit.setCurrentPosition(new TiledMapPosition().setPositionFromTiles(location.x, location.y));
			battleState.addEntity(hammerBackUnit);
			battleState.get(location.x, location.y).getUnit().addModifier(new Modifier(ModifiersEnum.DAMAGE_OVER_TIME, 3, 1));
			battleState.get(location.x, location.y).getUnit().addAbility(AbilitiesEnum.HAMMERBACKBACK, casterPos);
			break;
		case HAMMERBACKBACK:
			final List<MyPoint> crossedCellsBack = findLine(casterPos.x, casterPos.y, location.x, location.y);
			for (final MyPoint point : crossedCellsBack) {
				if (battleState.get(point.x, point.y).isOccupied()) {
					battleState.damageUnit(point, damage);
				}
			}
			break;
		default:
			// nothing
		}
	}

	/** Bresenham algorithm to find all cells crossed by a line **/
	public static List<MyPoint> findLine(int x0, int y0, int x1, int y1) {
		final List<MyPoint> line = new ArrayList<>();

		final int dx = Math.abs(x1 - x0);
		final int dy = Math.abs(y1 - y0);

		final int sx = x0 < x1 ? 1 : -1;
		final int sy = y0 < y1 ? 1 : -1;

		int err = dx - dy;
		int e2;

		while (true) {
			line.add(new MyPoint(x0, y0));

			if ((x0 == x1) && (y0 == y1))
				break;

			e2 = 2 * err;
			if (e2 > -dy) {
				err = err - dy;
				x0 = x0 + sx;
			}

			if (e2 < dx) {
				err = err + dx;
				y0 = y0 + sy;
			}
		}
		return line;
	}

	private Array<UnitTurn> generateMoves(Ability ability, Entity aiUnit, BattleState battleState) {
		final Array<UnitTurn> unitTurns = new Array<>();

		// if the ability is cast on self or no target, just cast it and move

		if ((ability.getTarget() == Target.NO_TARGET) || (ability.getTarget() == Target.SELF)) {
			final UnitTurn spellAndMove = new UnitTurn(aiUnit.getEntityID(), new SpellMove(MoveType.SPELL, new MyPoint(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY()), ability));
			spellAndMove.addMove(decideMove(ability, aiUnit, battleState));
			unitTurns.add(spellAndMove);
		}

		// if the ability has cell targets, try out all of them + move and make a new
		// state for each
		if ((ability.getTarget() == Target.CELL) || (ability.getTarget() == Target.CELL_BUT_NO_UNIT)) {
			final MyPoint center = new MyPoint(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY());
			final Set<MyPoint> cellsToCastOn = BattleStateGridHelper.getInstance().getAllPointsASpellCanHit(center, ability.getLineOfSight(), ability.getSpellData().getRange(), battleState);
			if (ability.getTarget() == Target.CELL_BUT_NO_UNIT) {
				filterUnits(cellsToCastOn, battleState);
			}
			for (final MyPoint point : cellsToCastOn) {
				final UnitTurn spellAndMove = new UnitTurn(aiUnit.getEntityID(), new SpellMove(MoveType.SPELL, point, ability));
				spellAndMove.addMove(decideMove(ability, aiUnit, battleState));
				unitTurns.add(spellAndMove);
			}
		}

		// get the distance between unit and possible targets
		final TreeMap<Integer, List<Entity>> distancesWithAbilityTargetUnits = (TreeMap<Integer, List<Entity>>) getDistancesToTargets(aiUnit, battleState, ability);

		if (!distancesWithAbilityTargetUnits.isEmpty() && (distancesWithAbilityTargetUnits.firstKey() > (ability.getSpellData().getRange() + ability.getSpellData().getAreaOfEffectRange() + aiUnit.getAp()))) {
			if (distancesWithAbilityTargetUnits.firstKey() > (aiUnit.getAttackRange() + aiUnit.getAp())) {
				// just walk
				final Entity closestUnit = distancesWithAbilityTargetUnits.firstEntry().getValue().get(0);
				final TiledMapPosition closestUnitPos = new TiledMapPosition().setPositionFromTiles(closestUnit.getCurrentPosition().getTileX(), closestUnit.getCurrentPosition().getTileY());
				final List<GridCell> path = MyPathFinder.getInstance().pathTowards(new TiledMapPosition().setPositionFromTiles(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY()), closestUnitPos, aiUnit.getAp());
				MyPoint goal = new MyPoint(path.get(path.size() - 1).x, path.get(path.size() - 1).y);
				int i = 2;
				while (checkIfUnitOnPoint(goal, battleState, aiUnit)) {
					if ((path.size() - i) <= 0) {
						return unitTurns;
					}
					goal = new MyPoint(path.get(path.size() - i).x, path.get(path.size() - i).y);
					i++;
				}
				unitTurns.add(new UnitTurn(aiUnit.getEntityID(), new Move(MoveType.MOVE, goal)));
				return unitTurns;
			} else {
				// move attack
				final Entity closestUnit = distancesWithAbilityTargetUnits.firstEntry().getValue().get(0);
				final TiledMapPosition closestUnitPos = new TiledMapPosition().setPositionFromTiles(closestUnit.getCurrentPosition().getTileX(), closestUnit.getCurrentPosition().getTileY());
				final List<GridCell> path = MyPathFinder.getInstance().pathTowards(new TiledMapPosition().setPositionFromTiles(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY()), closestUnitPos, aiUnit.getAp());
				MyPoint moveGoal = new MyPoint(path.get(path.size() - 1).x, path.get(path.size() - 1).y);
				int i = 2;
				while (checkIfUnitOnPoint(moveGoal, battleState, aiUnit)) {
					if ((path.size() - i) <= 0) {
						moveGoal = new MyPoint(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY());
						break;
					}
					moveGoal = new MyPoint(path.get(path.size() - i).x, path.get(path.size() - i).y);
					i++;
				}
				final MyPoint attackGoal = new MyPoint(distancesWithAbilityTargetUnits.firstEntry().getValue().get(0).getCurrentPosition().getTileX(),
						distancesWithAbilityTargetUnits.firstEntry().getValue().get(0).getCurrentPosition().getTileY());
				final UnitTurn moveAttack = new UnitTurn(aiUnit.getEntityID(), new Move(MoveType.MOVE, moveGoal));
				moveAttack.addMove(new Move(MoveType.ATTACK, attackGoal));
				unitTurns.add(moveAttack);
				return unitTurns;
			}
		}

		// decide where to cast spell
		final MyPoint casterPos = new MyPoint(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY());
		Array<MyPoint> abilityTargets = getAbilityTargets(ability, casterPos, aiUnit.isPlayerUnit(), battleState);

		// no units found in immediate vicinity, so move
		if (abilityTargets.isEmpty()) {
			MyPoint endMyPoint = new MyPoint(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY());
			final UnitTurn moveAndSpell = new UnitTurn(aiUnit.getEntityID(), new Move(MoveType.MOVE, endMyPoint));
			int ap = aiUnit.getAp();
			final BattleState copyBattleState = battleState.makeCopy();
			final Entity copyUnit = copyBattleState.get(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY()).getUnit();
			while (abilityTargets.isEmpty() && (ap > 0)) {
				final Entity closestUnit = distancesWithAbilityTargetUnits.firstEntry().getValue().get(0);
				final TiledMapPosition closestUnitPos = new TiledMapPosition().setPositionFromTiles(closestUnit.getCurrentPosition().getTileX(), closestUnit.getCurrentPosition().getTileY());
				final List<GridCell> path = MyPathFinder.getInstance().pathTowards(new TiledMapPosition().setPositionFromTiles(copyUnit.getCurrentPosition().getTileX(), copyUnit.getCurrentPosition().getTileY()), closestUnitPos,
						copyUnit.getAp());
				endMyPoint = new MyPoint(path.get(0).x, path.get(0).y);
				int i = 0;
				while (checkIfUnitOnPoint(endMyPoint, copyBattleState, copyUnit)) {
					endMyPoint = tryAdjacentPoint(i, new MyPoint(copyUnit.getCurrentPosition().getTileX(), copyUnit.getCurrentPosition().getTileY()),
							new MyPoint(closestUnit.getCurrentPosition().getTileX(), closestUnit.getCurrentPosition().getTileY()));
					i++;
				}
				copyBattleState.moveUnitTo(copyUnit, endMyPoint);
				moveAndSpell.addMove(new Move(MoveType.MOVE, endMyPoint));
				ap--;
				abilityTargets = getAbilityTargets(ability, endMyPoint, copyUnit.isPlayerUnit(), copyBattleState);
			}
			if (!abilityTargets.isEmpty()) {
				for (final MyPoint target : abilityTargets) {
					final Set<MyPoint> positionsToCastSpell = BattleStateGridHelper.getInstance().getAllCastPointsWhereTargetIsHit(ability, target,
							new MyPoint(copyUnit.getCurrentPosition().getTileX(), copyUnit.getCurrentPosition().getTileY()), copyBattleState);
					for (final MyPoint MyPoint : positionsToCastSpell) {
						final UnitTurn moveAndSpellCopy = moveAndSpell.makeCopy();
						final Array<MyPoint> affectedUnits = BattleStateGridHelper.getInstance().getTargetsAbility(ability, MyPoint, getUnitPositions(false, ability, copyBattleState));
						moveAndSpellCopy.addMove(new SpellMove(MoveType.SPELL, MyPoint, ability, affectedUnits));
						unitTurns.add(moveAndSpellCopy);
					}
				}
			} else {
				// units were in range, but moving did not bring them into line of sight
				unitTurns.add(moveAndSpell);
			}
		}

		if (!abilityTargets.isEmpty()) {
			for (final MyPoint target : abilityTargets) {
				final Set<MyPoint> positionsToCastSpell = BattleStateGridHelper.getInstance().getAllCastPointsWhereTargetIsHit(ability, target, new MyPoint(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY()),
						battleState);
				final Move moveAfterSpell = decideMove(ability, aiUnit, battleState);
				for (final MyPoint MyPoint : positionsToCastSpell) {
					final Array<MyPoint> affectedUnits = BattleStateGridHelper.getInstance().getTargetsAbility(ability, MyPoint, getUnitPositions(false, ability, battleState));
					final UnitTurn spellAndMove = new UnitTurn(aiUnit.getEntityID(), new SpellMove(MoveType.SPELL, MyPoint, ability, affectedUnits));
					spellAndMove.addMove(moveAfterSpell);
					unitTurns.add(spellAndMove);
				}
			}
		}
		return unitTurns;
	}

	private void filterUnits(Set<MyPoint> cellsToCastOn, BattleState battleState) {
		for (final Entity unit : battleState.getAllUnits()) {
			if (cellsToCastOn.contains(unit.getCurrentPosition().getTilePosAsPoint())) {
				cellsToCastOn.remove(unit.getCurrentPosition().getTilePosAsPoint());
			}
		}
	}

	private MyPoint tryAdjacentPoint(int i, MyPoint unitPoint, MyPoint target) {
		if (i == 0) {
			if (target.x < unitPoint.x) {
				i++;
			} else {
				return new MyPoint(unitPoint.x + 1, unitPoint.y);
			}
		}

		if (i == 1) {
			if (target.x > unitPoint.x) {
				i++;
			} else {
				return new MyPoint(unitPoint.x - 1, unitPoint.y);
			}
		}

		if (i == 2) {
			if (target.y < unitPoint.y) {
				i++;
			} else {
				return new MyPoint(unitPoint.x, unitPoint.y + 1);
			}
		}

		if (i == 3) {
			return new MyPoint(unitPoint.x, unitPoint.y - 1);
		} else {
			return new MyPoint(unitPoint.x, unitPoint.y);
		}
	}

	private boolean checkIfUnitOnPoint(MyPoint goal, BattleState battleState, Entity copyUnit) {
		for (final Entity unit : battleState.getAllUnits()) {
			if ((goal.equals(new MyPoint(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY()))) && !((copyUnit.getX() == unit.getX()) && (copyUnit.getY() == unit.getY()))) {
				return true;
			}
		}
		return false;
	}

	private Move decideMove(Ability ability, Entity aiUnit, BattleState battleState) {
		final MyPoint centerOfGravityEnemies = Utility.getCenterOfGravityPlayers(battleState);
		final MyPoint centerOfGravityAllies = Utility.getCenterOfGravityAi(battleState);
		final MyPoint centerOfGravityAllUnits = Utility.getCenterOfGravityAllUnits(battleState);

		// if low hp run away
		if (aiUnit.getHp() <= ((aiUnit.getEntityData().getMaxHP() / 100.0f) * 10)) {
			final MyPoint originalGoal = MyPathFinder.getInstance().getPositionFurthestAwayFrom(centerOfGravityEnemies);
			final MyPoint trimmedGoal = trimPathConsideringApAndReachable(originalGoal, aiUnit);
			return new Move(MoveType.MOVE, trimmedGoal);
		}

		switch (ability.getAffectedTeams()) {
		case FRIENDLY:
			return new Move(MoveType.MOVE, trimPathConsideringApAndReachable(centerOfGravityAllies, aiUnit));
		case ENEMY:
			return new Move(MoveType.MOVE, trimPathConsideringApAndReachable(centerOfGravityEnemies, aiUnit));
		case BOTH:
			return new Move(MoveType.MOVE, trimPathConsideringApAndReachable(centerOfGravityAllUnits, aiUnit));
		case NONE:
			return new Move(MoveType.MOVE, trimPathConsideringApAndReachable(centerOfGravityAllUnits, aiUnit));
		default:
			Gdx.app.debug(TAG, "ability does not have one of these affected teams : FRIENDLY, ENEMY, BOTH or NONE, returning null");
			return null;
		}
	}

	private MyPoint trimPathConsideringApAndReachable(MyPoint originalGoal, Entity aiUnit) {
		final TiledMapPosition start = new TiledMapPosition().setPositionFromTiles(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY());
		final TiledMapPosition goal = new TiledMapPosition().setPositionFromTiles(originalGoal.x, originalGoal.y);
		final List<GridCell> path = MyPathFinder.getInstance().pathTowards(start, goal, aiUnit.getAp());
		if (path.isEmpty()) {
			return new MyPoint(start.getTileX(), start.getTileY());
		}

		return new MyPoint(path.get(path.size() - 1).x, path.get(path.size() - 1).y);
	}

	private Map<Integer, List<Entity>> getDistancesToTargets(Entity unit, BattleState battleState, Ability ability) {
		final Map<Integer, List<Entity>> distances = new TreeMap<>();
		Array<Entity> unitsToCheck;
		switch (ability.getAffectedTeams()) {
		case FRIENDLY:
			if (unit.isPlayerUnit()) {
				unitsToCheck = battleState.getVisiblePlayerUnits();
			} else {
				unitsToCheck = battleState.getVisibleAiUnits();
			}
			for (final Entity entity : unitsToCheck) {
				if (unit.getEntityID() != entity.getEntityID()) {
					distances.computeIfAbsent(calculateDistanceTwoUnits(unit, entity), k -> new ArrayList<Entity>()).add(entity);
				}
			}
			break;
		case ENEMY:
			if (unit.isPlayerUnit()) {
				unitsToCheck = battleState.getVisibleAiUnits();
			} else {
				unitsToCheck = battleState.getVisiblePlayerUnits();
			}
			for (final Entity entity : unitsToCheck) {
				if (unit.getEntityID() != entity.getEntityID()) {
					distances.computeIfAbsent(calculateDistanceTwoUnits(unit, entity), k -> new ArrayList<Entity>()).add(entity);
				}
			}
			break;
		case BOTH:
			for (final Entity entity : battleState.getAllVisibleUnits()) {
				if (unit.getEntityID() != entity.getEntityID()) {
					distances.computeIfAbsent(calculateDistanceTwoUnits(unit, entity), k -> new ArrayList<Entity>()).add(entity);
				}
			}
			break;
		case NONE:
			return distances;
		}
		return distances;
	}

	private Integer calculateDistanceTwoUnits(Entity aiUnit, Entity entity) {
		final TiledMapPosition aiUnitPosition = new TiledMapPosition().setPositionFromTiles(aiUnit.getCurrentPosition().getTileX(), aiUnit.getCurrentPosition().getTileY());
		final TiledMapPosition entityPosition = new TiledMapPosition().setPositionFromTiles(entity.getCurrentPosition().getTileX(), entity.getCurrentPosition().getTileY());
		return aiUnitPosition.getDistance(entityPosition);
	}

	private Array<MyPoint> getAbilityTargets(Ability ability, MyPoint casterPos, boolean isPlayerUnit, BattleState battleState) {

		final Array<MyPoint> unitPositions = getVisibleUnitPositions(isPlayerUnit, ability, battleState);
		return BattleStateGridHelper.getInstance().getTargetPositionsInRangeAbility(casterPos, ability, unitPositions);
	}

	private Array<MyPoint> getVisibleUnitPositions(boolean isPlayerUnit, Ability ability, BattleState battleState) {
		switch (ability.getAffectedTeams()) {
		case FRIENDLY:
			if (isPlayerUnit) {
				return collectPoints(battleState.getVisiblePlayerUnits());
			} else {
				return collectPoints(battleState.getVisibleAiUnits());
			}
		case ENEMY:
			if (!isPlayerUnit) {
				return collectPoints(battleState.getVisiblePlayerUnits());
			} else {
				return collectPoints(battleState.getVisibleAiUnits());
			}
		case BOTH:
			return collectPoints(battleState.getAllVisibleUnits());
		case NONE:
			return new Array<>();
		default:
			Gdx.app.debug(TAG, "ability does not have one of these affected teams : FRIENDLY, ENEMY, BOTH or NONE, returning null");
			return null;
		}
	}

	private Array<MyPoint> getUnitPositions(boolean isPlayerUnit, Ability ability, BattleState battleState) {
		switch (ability.getAffectedTeams()) {
		case FRIENDLY:
			if (isPlayerUnit) {
				return collectPoints(battleState.getPlayerUnits());
			} else {
				return collectPoints(battleState.getAiUnits());
			}
		case ENEMY:
			if (!isPlayerUnit) {
				return collectPoints(battleState.getPlayerUnits());
			} else {
				return collectPoints(battleState.getAiUnits());
			}
		case BOTH:
			return collectPoints(battleState.getAllUnits());
		case NONE:
			return new Array<>();
		default:
			Gdx.app.debug(TAG, "ability does not have one of these affected teams : FRIENDLY, ENEMY, BOTH or NONE, returning null");
			return null;
		}
	}

	private Array<MyPoint> collectPoints(Array<Entity> allUnits) {
		final Array<MyPoint> points = new Array<>();
		for (final Entity unit : allUnits) {
			points.add(new MyPoint(unit.getCurrentPosition().getTileX(), unit.getCurrentPosition().getTileY()));
		}
		return points;
	}
}

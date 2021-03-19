package com.jelte.norii.battle.battlePhase;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.AlphaAction;
import com.badlogic.gdx.utils.Array;
import com.jelte.norii.ai.AIDecisionMaker;
import com.jelte.norii.audio.AudioCommand;
import com.jelte.norii.audio.AudioManager;
import com.jelte.norii.audio.AudioTypeEvent;
import com.jelte.norii.battle.BattleManager;
import com.jelte.norii.battle.MessageToBattleScreen;
import com.jelte.norii.battle.battleState.BattleStateGridHelper;
import com.jelte.norii.entities.Entity;
import com.jelte.norii.entities.EntityAnimation;
import com.jelte.norii.entities.EntityAnimationType;
import com.jelte.norii.entities.EntityTypes;
import com.jelte.norii.magic.AbilitiesEnum;
import com.jelte.norii.magic.Ability;
import com.jelte.norii.magic.Ability.AffectedTeams;
import com.jelte.norii.magic.ModifiersEnum;
import com.jelte.norii.map.MyPathFinder;
import com.jelte.norii.map.TiledMapActor;
import com.jelte.norii.particles.ParticleMaker;
import com.jelte.norii.particles.ParticleType;
import com.jelte.norii.utility.MyPoint;
import com.jelte.norii.utility.TiledMapPosition;

public class SpellBattlePhase extends BattlePhase {
	private final BattleManager battlemanager;

	public SpellBattlePhase(final BattleManager battlemanager) {
		this.battlemanager = battlemanager;
	}

	@Override
	public void clickedOnTile(final TiledMapActor actor) {
		possibleTileSpell(actor.getActorPos());
		ParticleMaker.deactivateAllParticlesOfType(ParticleType.SPELL);
		ParticleMaker.deactivateAllParticlesOfType(ParticleType.ATTACK);
		exit();
	}

	@Override
	public void hoveredOnTile(TiledMapActor actor) {
		showCellsThatSpellWillAffect(actor.getActorPos());
	}

	private void showCellsThatSpellWillAffect(TiledMapPosition actorPos) {
		final MyPoint casterPos = battlemanager.getActiveUnit().getCurrentPosition().getTilePosAsPoint();
		ParticleMaker.deactivateAllParticlesOfType(ParticleType.ATTACK);
		final Set<MyPoint> pointsToColor = BattleStateGridHelper.getInstance().getAllPointsASpellCanHit(casterPos, actorPos.getTilePosAsPoint(), ability.getAreaOfEffect(), ability.getSpellData().getRange(), battlemanager.getBattleState());
		for (final MyPoint point : pointsToColor) {
			ParticleMaker.addParticle(ParticleType.ATTACK, point, 0);
		}
	}

	private void possibleTileSpell(final TiledMapPosition targetPos) {
		final Entity currentUnit = battlemanager.getActiveUnit();

		if (isValidTileTarget(currentUnit, targetPos, ability)) {
			currentUnit.getVisualComponent().setAnimationType(EntityAnimationType.WALK);
			selectSpell(null, ability, currentUnit, targetPos);
			currentUnit.getVisualComponent().setAnimationType(EntityAnimationType.WALK);
		}
		exit();
	}

	private boolean isValidTileTarget(Entity caster, TiledMapPosition targetPos, Ability ability) {
		final boolean correctAreaOfEffect = checkAreaOfEffect(caster, targetPos, ability);
		final boolean correctVisibility = checkVisibility(caster, targetPos, ability);
		final boolean correctTarget = checkTarget(caster, targetPos, ability, true);

		return correctAreaOfEffect && correctVisibility && correctTarget;
	}

	@Override
	public void clickedOnUnit(final Entity entity) {
		possibleUnitTargetSpell(entity);
		ParticleMaker.deactivateAllParticlesOfType(ParticleType.SPELL);
		ParticleMaker.deactivateAllParticlesOfType(ParticleType.ATTACK);
	}

	@Override
	public void exit() {
		battlemanager.setCurrentBattleState(battlemanager.getActionBattleState());
		battlemanager.getCurrentBattleState().entry();
	}

	private void possibleUnitTargetSpell(final Entity target) {
		final Entity currentUnit = battlemanager.getActiveUnit();

		if (isValidUnitTarget(currentUnit, target)) {
			selectSpell(target, ability, currentUnit, target.getCurrentPosition());
			currentUnit.getVisualComponent().setLocked(true);
			battlemanager.setLockedUnit(currentUnit);
		}
		exit();
	}

	private boolean isValidUnitTarget(Entity caster, Entity target) {
		final AffectedTeams affectedTeams = ability.getAffectedTeams();

		final boolean correctTeam = checkTeams(caster, target, affectedTeams);
		final boolean correctAreaOfEffect = checkAreaOfEffect(caster, target.getCurrentPosition(), ability);
		final boolean correctVisibility = checkVisibility(caster, target.getCurrentPosition(), ability);
		final boolean correctTarget = checkTarget(caster, target.getCurrentPosition(), ability, false);

		return correctAreaOfEffect && correctTeam && correctVisibility && correctTarget;
	}

	private boolean checkTeams(Entity caster, Entity target, final AffectedTeams affectedTeams) {
		if (affectedTeams == AffectedTeams.BOTH) {
			return true;
		}

		if (affectedTeams == AffectedTeams.ENEMY) {
			return caster.isPlayerUnit() != target.isPlayerUnit();
		}

		return ((affectedTeams == AffectedTeams.FRIENDLY) && (caster.isPlayerUnit() == target.isPlayerUnit()));
	}

	private boolean checkAreaOfEffect(Entity caster, TiledMapPosition targetPos, Ability ability) {
		final List<TiledMapPosition> positions = battlemanager.getUnits().stream().map(Entity::getCurrentPosition).collect(Collectors.toList());
		final Set<MyPoint> spellPath = BattleStateGridHelper.getInstance().calculateSpellPath(caster, ability, positions);
		final MyPoint target = targetPos.getTilePosAsPoint();
		return spellPath.contains(target);
	}

	private boolean checkVisibility(Entity caster, TiledMapPosition targetPos, Ability ability) {
		if (ability.getGoesTroughObstacles()) {
			return true;
		}

		if (ability.getGoesTroughUnits()) {
			return MyPathFinder.getInstance().lineOfSight(caster, targetPos, battlemanager.getUnits(), false);
		} else {
			return MyPathFinder.getInstance().lineOfSight(caster, targetPos, battlemanager.getUnits(), true);
		}

	}

	private boolean checkTarget(Entity caster, TiledMapPosition targetPos, Ability ability, boolean isTile) {
		switch (ability.getTarget()) {
		case CELL:
			return isTile;
		case UNIT:
			return !isTile;
		case CELL_BUT_NO_UNIT:
			return isTile;
		case SELF:
			return (!isTile && caster.getCurrentPosition().isTileEqualTo(targetPos));
		case NO_TARGET:
			return true;
		default:
			Gdx.app.debug("SpellBattlePhase", "ability : " + ability + " has no valid target : " + ability.getTarget());
			return false;
		}
	}

	public void executeSpellForAi(Entity entity, Ability ability, MyPoint target) {
		final TiledMapPosition targetPos = new TiledMapPosition().setPositionFromTiles(target.x, target.y);
		final List<Entity> units = battlemanager.getUnits();
		for (final Entity unit : units) {
			if (unit.getCurrentPosition().isTileEqualTo(targetPos)) {
				selectSpell(unit, ability, entity, targetPos);
			}
		}
	}

	private void selectSpell(final Entity target, final Ability ability, final Entity currentUnit, final TiledMapPosition targetPos) {
		switch (ability.getAbilityEnum()) {
		case FIREBALL:
			castFireBall(currentUnit, targetPos, ability);
			break;
		case HEAL:
			castHeal(currentUnit, targetPos, ability);
			break;
		case ARROW:
			castArrow(currentUnit, targetPos, ability);
			break;
		case INVISIBLE:
			castInvis(currentUnit, ability);
			break;
		case ICEFIELD:
			castIceField(currentUnit, targetPos, ability);
			break;
		case PUSH:
			castPush(currentUnit, targetPos, ability);
			break;
		case PULL:
			castPull(currentUnit, targetPos, ability);
			break;
		case SWAP:
			castSwap(currentUnit, target, ability);
			break;
		case TURN_TO_STONE:
			castTurnToStone(currentUnit, target, ability);
			break;
		case HAMMERBACK:
			castHammerback(currentUnit, targetPos, ability);
			break;
		case PORTAL:
			castPortal(currentUnit, targetPos, ability);
			break;
		case TRANSPORT:
			castTransport(currentUnit, targetPos, ability);
			break;
		case HAMMERBACKBACK:
			castHammerbackBack(currentUnit, targetPos, ability);
			break;
		default:
			break;
		}
	}

	private void castTransport(Entity currentUnit, TiledMapPosition targetPos, Ability ability) {
		// TODO Auto-generated method stub

	}

	private void castInvis(Entity caster, Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.INVISIBLE);
		caster.addModifier(ModifiersEnum.INVISIBLE, ability.getSpellData().getDamage(), 0);
		caster.setInvisible(true);
		final AlphaAction action = new AlphaAction();
		action.setAlpha(.5f);
		action.setDuration(1);
		action.setInterpolation(Interpolation.linear);
		caster.getVisualComponent().getEntityactor().addAction(action);
	}

	private void castArrow(Entity caster, TiledMapPosition targetPos, Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.ARROW);

		final List<MyPoint> crossedCells = AIDecisionMaker.findLine(caster.getCurrentPosition().getTileX(), caster.getCurrentPosition().getTileY(), targetPos.getTileX(), targetPos.getTileY());
		battlemanager.getBattleState();
		for (final MyPoint point : crossedCells) {
			if (battlemanager.getBattleState().get(point.x, point.y).isOccupied()) {
				final Entity unit = battlemanager.getBattleState().get(point.x, point.y).getUnit();
				battlemanager.getEntityByID(unit.getEntityID()).damage(ability.getSpellData().getDamage());
			}
		}
	}

	private void castPull(Entity currentUnit, TiledMapPosition targetPos, Ability ability) {
		final MyPoint casterPos = currentUnit.getCurrentPosition().getTilePosAsPoint();
		final MyPoint location = targetPos.getTilePosAsPoint();
		final int pushRange = ability.getSpellData().getDamage();
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.PULL);
		battlemanager.getBattleState().pushOrPullUnit(casterPos, location, pushRange, true);
		ParticleMaker.addParticle(ParticleType.WIND, targetPos, 0);
	}

	private void castPush(Entity currentUnit, TiledMapPosition targetPos, Ability ability) {
		final MyPoint casterPos = currentUnit.getCurrentPosition().getTilePosAsPoint();
		final MyPoint location = targetPos.getTilePosAsPoint();
		final int pushRange = ability.getSpellData().getDamage();
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.PUSH);
		battlemanager.getBattleState().pushOrPullUnit(casterPos, location, pushRange, false);
		ParticleMaker.addParticle(ParticleType.WIND, targetPos, 0);
	}

	private void castFireBall(final Entity caster, final TiledMapPosition targetPos, final Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.FIREBALL_SOUND);
		ParticleMaker.addParticle(ParticleType.FIREBALL, targetPos, 0);

		final Entity possibleTarget = getEntityAtPosition(targetPos.getTilePosAsPoint());
		if (possibleTarget != null) {
			possibleTarget.damage(ability.getSpellData().getDamage());
			battlemanager.sendMessageToBattleScreen(MessageToBattleScreen.UPDATE_UI, possibleTarget);
		}
	}

	private void castHeal(final Entity caster, final TiledMapPosition targetPos, final Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.HEAL);
		ParticleMaker.addParticle(ParticleType.HEAL, targetPos, 0);

		final Entity possibleTarget = getEntityAtPosition(targetPos.getTilePosAsPoint());
		if (possibleTarget != null) {
			possibleTarget.heal(ability.getSpellData().getDamage());
			battlemanager.sendMessageToBattleScreen(MessageToBattleScreen.UPDATE_UI, possibleTarget);
		}
	}

	private void castIceField(final Entity caster, final TiledMapPosition targetPos, final Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.ICE);

		final Set<MyPoint> targetCells = BattleStateGridHelper.getInstance().getAllPointsASpellCanHit(caster.getCurrentPosition().getTilePosAsPoint(), targetPos.getTilePosAsPoint(), ability.getAreaOfEffect(),
				ability.getSpellData().getRange(), battlemanager.getBattleState());

		for (final MyPoint cell : targetCells) {
			ParticleMaker.addParticle(ParticleType.ICE, cell, 0);
			final Entity possibleTarget = getEntityAtPosition(cell);
			if (possibleTarget != null) {
				possibleTarget.damage(ability.getSpellData().getDamage());
				battlemanager.sendMessageToBattleScreen(MessageToBattleScreen.UPDATE_UI, possibleTarget);
			}
		}
	}

	private void castSwap(final Entity caster, final Entity target, final Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.SWAP_SOUND);
		ParticleMaker.addParticle(ParticleType.SWAP, caster.getCurrentPosition(), 0);
		final TiledMapPosition posCaster = caster.getCurrentPosition();
		caster.setCurrentPosition(target.getCurrentPosition());
		target.setCurrentPosition(posCaster);
	}

	private void castTurnToStone(final Entity caster, final Entity target, final Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.STONE_SOUND);
		target.getVisualComponent().changeAnimation(new EntityAnimation("Rock"));
		target.addModifier(ModifiersEnum.IMAGE_CHANGED, 2, 0);
		target.addModifier(ModifiersEnum.STUNNED, 2, 0);
	}

	private void castHammerback(final Entity caster, final TiledMapPosition targetPos, final Ability ability) {
		caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.HAMMER_SOUND);

		final List<MyPoint> crossedCells = AIDecisionMaker.findLine(caster.getCurrentPosition().getTileX(), caster.getCurrentPosition().getTileY(), targetPos.getTileX(), targetPos.getTileY());
		battlemanager.getBattleState();
		for (final MyPoint point : crossedCells) {
			if (battlemanager.getBattleState().get(point.x, point.y).isOccupied()) {
				final Entity unit = battlemanager.getBattleState().get(point.x, point.y).getUnit();
				battlemanager.getEntityByID(unit.getEntityID()).damage(ability.getSpellData().getDamage());
			}
		}

		final Entity hammerEntity = new Entity(EntityTypes.BOOMERANG, caster.getOwner());
		caster.getOwner().addUnit(hammerEntity);
		battlemanager.sendMessageToBattleScreen(MessageToBattleScreen.ADD_UNIT_ENTITYSTAGE, hammerEntity);
		hammerEntity.getVisualComponent().initiateInBattle(targetPos);
		hammerEntity.setCurrentPosition(targetPos);
		hammerEntity.addModifier(ModifiersEnum.DAMAGE_OVER_TIME, 3, 1);
		hammerEntity.addAbility(AbilitiesEnum.HAMMERBACKBACK, caster.getCurrentPosition().getTilePosAsPoint());
		battlemanager.addUnit(hammerEntity);
		battlemanager.sendMessageToBattleScreen(MessageToBattleScreen.ADD_UNIT_UI, hammerEntity);
	}

	private void castPortal(final Entity caster, final TiledMapPosition targetPos, final Ability ability) {
		final boolean playerUnit = caster.isPlayerUnit();
		Array<Entity> entities;
		if (playerUnit) {
			entities = battlemanager.getBattleState().getPlayerUnits();
		} else {
			entities = battlemanager.getBattleState().getAiUnits();
		}
		int portalCount = 0;
		for (final Entity entity : entities) {
			if (entity.getEntityType() == EntityTypes.PORTAL) {
				portalCount++;
			}
		}

		if (portalCount < 2) {
			caster.setAp(caster.getAp() - ability.getSpellData().getApCost());
			AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.PORTAL);

			final Entity portalEntity = new Entity(EntityTypes.PORTAL, caster.getOwner());
			caster.getOwner().addUnit(portalEntity);
			battlemanager.sendMessageToBattleScreen(MessageToBattleScreen.ADD_UNIT_ENTITYSTAGE, portalEntity);
			portalEntity.getVisualComponent().initiateInBattle(targetPos);
			portalEntity.setCurrentPosition(targetPos);
			battlemanager.addUnit(portalEntity);
			battlemanager.sendMessageToBattleScreen(MessageToBattleScreen.ADD_UNIT_UI, portalEntity);
		}
	}

	private void castHammerbackBack(final Entity caster, final TiledMapPosition targetPos, final Ability ability) {
		AudioManager.getInstance().onNotify(AudioCommand.SOUND_PLAY_ONCE, AudioTypeEvent.HAMMER_SOUND);
		final List<MyPoint> crossedCells = AIDecisionMaker.findLine(caster.getCurrentPosition().getTileX(), caster.getCurrentPosition().getTileY(), targetPos.getTileX(), targetPos.getTileY());
		battlemanager.getBattleState();
		for (final MyPoint point : crossedCells) {
			if (battlemanager.getBattleState().get(point.x, point.y).isOccupied()) {
				final Entity unit = battlemanager.getBattleState().get(point.x, point.y).getUnit();
				if (unit.getEntityID() != caster.getEntityID()) {
					battlemanager.getEntityByID(unit.getEntityID()).damage(ability.getSpellData().getDamage());
				}
			}
		}
	}

	private Entity getEntityAtPosition(MyPoint targetPos) {
		final List<Entity> units = battlemanager.getUnits();
		for (final Entity unit : units) {
			if (unit.getCurrentPosition().isTileEqualTo(targetPos)) {
				return unit;
			}
		}
		return null;
	}

	@Override
	public void setAbility(Ability ability) {
		this.ability = ability;
	}

	@Override
	public void buttonPressed(final int button) {
		switch (button) {
		case Buttons.RIGHT:
			ParticleMaker.deactivateAllParticlesOfType(ParticleType.SPELL);
			ParticleMaker.deactivateAllParticlesOfType(ParticleType.ATTACK);
			exit();
			break;
		case Buttons.LEFT:
			break;
		case Buttons.MIDDLE:
			break;
		default:
			break;
		}
	}
}

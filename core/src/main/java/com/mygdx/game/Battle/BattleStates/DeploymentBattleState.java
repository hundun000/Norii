package com.mygdx.game.Battle.BattleStates;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.Battle.BattleManager;
import com.mygdx.game.Entities.Entity;
import com.mygdx.game.Entities.Player;
import com.mygdx.game.Map.TiledMapActor;
import com.mygdx.game.Particles.ParticleMaker;
import com.mygdx.game.Particles.ParticleType;

import Utility.TiledMapPosition;

public class DeploymentBattleState extends BattleState {
	private static final String TAG = DeploymentBattleState.class.getSimpleName();

	private final BattleManager battlemanager;
	private int deployingUnitNumber;
	private final Entity[] playerUnits;

	public DeploymentBattleState(final BattleManager battlemanager) {
		this.battlemanager = battlemanager;
		deployingUnitNumber = 0;
		playerUnits = Player.getInstance().getPlayerUnits();
		playerUnits[0].setInDeploymentPhase(true);
	}

	@Override
	public void exit() {
		ParticleMaker.deactivateAllParticlesOfType(ParticleType.SPAWN);
		battlemanager.setCurrentBattleState(battlemanager.getSelectUnitBattleState());
		battlemanager.getCurrentBattleState().entry();
	}

	@Override
	public void clickedOnTile(final TiledMapActor actor) {
		deployUnit(actor);
	}

	private void deployUnit(final TiledMapActor actor) {
		if (Boolean.TRUE.equals(actor.getIsFreeSpawn())) {
			final TiledMapPosition newPosition = actor.getActorPos();

			if ((playerUnits != null) && (deployingUnitNumber < playerUnits.length)) {
				deployUnit(newPosition);
				actor.setIsFreeSpawn(false);
				ParticleMaker.deactivateParticle(ParticleMaker.getParticle(ParticleType.SPAWN, newPosition));
				nextDeployment();
			} else {
				Gdx.app.debug(TAG, "can't deploy unit, units is null or activeunitindex is > the length of units");
			}
		}
	}

	private void deployUnit(final TiledMapPosition newPosition) {
		final Entity unitToDeploy = playerUnits[deployingUnitNumber];
		unitToDeploy.setInDeploymentPhase(false);
		initiateUnitInBattle(unitToDeploy, newPosition);
	}

	private void nextDeployment() {
		deployingUnitNumber++;
		if (deployingUnitNumber < playerUnits.length) {
			playerUnits[deployingUnitNumber].setInDeploymentPhase(true);
		}
		checkIfLastUnit();
	}

	private void initiateUnitInBattle(final Entity unit, final TiledMapPosition pos) {
		unit.setInBattle(true);
		unit.setCurrentPosition(pos);
	}

	private void checkIfLastUnit() {
		if (deployingUnitNumber >= playerUnits.length) {
			exit();
		}
	}
}

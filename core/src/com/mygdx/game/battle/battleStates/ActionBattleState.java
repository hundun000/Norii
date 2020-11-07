package com.mygdx.game.battle.battleStates;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.mygdx.game.battle.BattleManager;
import com.mygdx.game.entities.Entity;
import com.mygdx.game.entities.EntityAnimation.Direction;

public class ActionBattleState extends BattleState {
	private final BattleManager battlemanager;

	public ActionBattleState(final BattleManager battlemanager) {
		this.battlemanager = battlemanager;
	}

	@Override
	public void entry() {
		battlemanager.getActiveUnit().setInActionPhase(true);
	}

	@Override
	public void exit() {
		battlemanager.swapTurn();
	}

	@Override
	public void keyPressed(final int key) {
		final Entity activeUnit = battlemanager.getActiveUnit();
		switch (key) {
		case Keys.Z:
			activeUnit.setDirection(Direction.UP);
			break;
		case Keys.Q:
			activeUnit.setDirection(Direction.LEFT);
			break;
		case Keys.S:
			activeUnit.setDirection(Direction.DOWN);
			break;
		case Keys.D:
			activeUnit.setDirection(Direction.RIGHT);
			break;
		default:
			break;
		}
	}

	@Override
	public void buttonPressed(int button) {
		switch (button) {
		case Buttons.RIGHT:
			battlemanager.getActiveUnit().getActionsui().setVisible(false);
			battlemanager.setCurrentBattleState(battlemanager.getSelectUnitBattleState());
			battlemanager.getCurrentBattleState().entry();
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

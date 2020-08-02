package com.mygdx.game.Magic;

import com.mygdx.game.Entities.Entity;

public class Modifier {
	private ModifiersEnum type;
	private int turns;
	private int amount;

	public Modifier(final ModifiersEnum type, final int turns, final int amount) {
		super();
		this.type = type;
		this.turns = turns;
		this.amount = amount;
	}

	public void applyModifier(final Entity unit) {
		switch (type) {
		case DAMAGE:
			unit.damage(amount);
			break;
		case REMOVE_AP:
			unit.setAp(unit.getAp() - amount);
			break;
		case REDUCE_DAMAGE:
			unit.getEntityData().setAttackPower(unit.getEntityData().getAttackPower() - amount);
			break;
		case IMPROVE_DAMAGE:
			unit.getEntityData().setAttackPower(unit.getEntityData().getAttackPower() + amount);
			break;
		case STUNNED:
			break;
		default:
			break;

		}
		reduceTurn();
	}

	public void removeModifier(final Entity unit) {
		switch (type) {
		case REDUCE_DAMAGE:
			unit.getEntityData().setAttackPower(unit.getEntityData().getAttackPower() + amount);
			break;
		case IMPROVE_DAMAGE:
			unit.getEntityData().setAttackPower(unit.getEntityData().getAttackPower() - amount);
			break;
		case IMAGE_CHANGED:
			unit.restoreAnimation();
			break;
		default:
			break;
		}
	}

	private void reduceTurn() {
		turns -= 1;
	}

	public ModifiersEnum getType() {
		return type;
	}

	public void setType(final ModifiersEnum type) {
		this.type = type;
	}

	public int getTurns() {
		return turns;
	}

	public void setTurns(final int turns) {
		this.turns = turns;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(final int amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "modifier name : " + type;
	}

}

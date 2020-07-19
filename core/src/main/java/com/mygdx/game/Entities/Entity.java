package com.mygdx.game.Entities;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.xguzm.pathfinding.grid.GridCell;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Entities.EntityAnimation.Direction;
import com.mygdx.game.Entities.EntityObserver.EntityCommand;
import com.mygdx.game.Magic.AbilitiesEnum;
import com.mygdx.game.Magic.Ability;
import com.mygdx.game.Magic.Modifier;
import com.mygdx.game.Magic.ModifiersEnum;
import com.mygdx.game.UI.ActionsUI;
import com.mygdx.game.UI.CharacterHud;
import com.mygdx.game.UI.StatusUI;

import Utility.TiledMapPosition;
import Utility.Utility;

public class Entity extends Actor implements EntitySubject {
	private final EntityData entityData;

	private int ap;
	private int hp;

	private int basicAttackCost;
	private int currentInitiative;

	private boolean inBattle;
	private boolean isInAttackPhase;
	private boolean isDead;
	protected boolean isPlayerUnit;
	private boolean isActive;
	private int entityID;

	protected TiledMapPosition oldPlayerPosition;
	protected TiledMapPosition currentPlayerPosition;
	protected Direction direction;

	private StatusUI statusui;
	private ActionsUI actionsui;
	private CharacterHud bottomMenu;

	private EntityAnimation entityAnimation;
	private EntityAnimation entityTemporaryAnimation;
	protected EntityActor entityactor;

	private Array<EntityObserver> observers;
	private Collection<Ability> abilities;
	private Collection<Modifier> modifiers;

	private Runnable updatePositionAction;
	private Runnable aiFinishTurn;

	public Entity(final EntityTypes type) {
		entityData = EntityFileReader.getUnitData().get(type.ordinal());
		entityData.setEntity(this);
		entityAnimation = new EntityAnimation(entityData.getEntitySpriteFilePath());
		initEntity();
	}

	public void initEntity() {
		observers = new Array<>();
		oldPlayerPosition = new TiledMapPosition().setPositionFromScreen(-1000, -1000);
		currentPlayerPosition = new TiledMapPosition().setPositionFromScreen(-1000, -1000);
		hp = entityData.getMaxHP();
		ap = entityData.getMaxAP();
		currentInitiative = entityData.getBaseInitiative();
		isDead = false;
		inBattle = false;
		isInAttackPhase = false;
		isPlayerUnit = true;
		abilities = new ArrayList<>();
		modifiers = new ArrayList<>();
		entityID = java.lang.System.identityHashCode(this);
		initAbilities();
		initActions();
	}

	private void initAbilities() {
		for (final String abilityString : entityData.getAbilties()) {
			addAbility(AbilitiesEnum.valueOf(abilityString));
		}
	}

	private void initActions() {
		updatePositionAction = () -> updatePositionFromActor();
		aiFinishTurn = () -> notifyEntityObserver(EntityCommand.AI_FINISHED_TURN);
	}

	public void update(final float delta) {
		entityAnimation.update(delta);
	}

	public EntityData getEntityData() {
		return entityData;
	}

	public StatusUI getStatusui() {
		return statusui;
	}

	public void setStatusui(final StatusUI statusui) {
		this.statusui = statusui;
	}

	public ActionsUI getActionsui() {
		return actionsui;
	}

	public void setActionsui(final ActionsUI actionsui) {
		this.actionsui = actionsui;
	}

	public void setbottomMenu(final CharacterHud bottomMenu) {
		this.bottomMenu = bottomMenu;
	}

	public void dispose() {
		Utility.unloadAsset(entityAnimation.getSpritePath());
	}

	public TiledMapPosition getCurrentPosition() {
		return currentPlayerPosition;
	}

	public TiledMapPosition getOldPlayerPosition() {
		return oldPlayerPosition;
	}

	public void setOldPlayerPosition(final TiledMapPosition oldPlayerPosition) {
		this.oldPlayerPosition = oldPlayerPosition;
	}

	public void setCurrentPosition(final TiledMapPosition pos) {
		currentPlayerPosition = pos;
		entityactor.setPos();

		updateUI();
	}

	public void updateUI() {
		statusui.update();
		bottomMenu.update();
	}

	public EntityActor getEntityactor() {
		return entityactor;
	}

	public void setEntityactor(final EntityActor entityactor) {
		this.entityactor = entityactor;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setActive(final boolean isActive) {
		this.isActive = isActive;
		if (isPlayerUnit) {
			actionsui.update();
		}
	}

	public boolean isActive() {
		return isActive;
	}

	public void setInMovementPhase(final boolean isInMovementPhase) {
		if (isInMovementPhase) {
			actionsui.setVisible(false);
			notifyEntityObserver(EntityCommand.IN_MOVEMENT);
		}
	}

	public boolean isInAttackPhase() {
		return isInAttackPhase;
	}

	public void setInAttackPhase(final boolean isInAttackPhase) {
		if (canAttack()) {
			this.isInAttackPhase = isInAttackPhase;
			if (isInAttackPhase) {
				actionsui.setVisible(false);
				notifyEntityObserver(EntityCommand.IN_ATTACK_PHASE);
			}
		}
	}

	public void setInSpellPhase(final boolean isInSpellPhase, final Ability ability) {
		if (isInSpellPhase) {
			actionsui.setVisible(false);
			notifyEntityObserver(EntityCommand.IN_SPELL_PHASE, ability);
		}
	}

	public void attack(final Entity target) {
		target.damage(entityData.getAttackPower());
	}

	public boolean canAttack() {
		return ap > basicAttackCost;
	}

	public void damage(final int damage) {
		if (damage >= hp) {
			removeUnit();
		} else {
			hp = hp - damage;
		}

		updateUI();
	}

	private void removeUnit() {
		hp = 0;
		isDead = true;
		inBattle = false;
		isActive = false;
		setCurrentPosition(new TiledMapPosition().setPositionFromScreen(-100, -100));
		getEntityactor().setPosition(-100, -100);
		setVisible(false);
	}

	public boolean canMove() {
		return (ap > 0);
	}

	public boolean isInBattle() {
		return inBattle;
	}

	public void setInBattle(final boolean inBattle) {
		this.inBattle = inBattle;
	}

	public void setInActionPhase(final boolean isInActionPhase) {
		notifyEntityObserver(EntityCommand.UNIT_ACTIVE);

		if (isInActionPhase) {
			if (isPlayerUnit) {
				actionsui.update();
				actionsui.setVisible(true);
			} else {
				notifyEntityObserver(EntityCommand.AI_ACT);
			}
		}
	}

	public void setInDeploymentPhase(final boolean isInDeploymentPhase) {
		if (isInDeploymentPhase) {
			bottomMenu.setHero(this);
			notifyEntityObserver(EntityCommand.UNIT_ACTIVE);
		}
	}

	public void setFocused(final boolean isFocused) {
		if (isFocused) {
			bottomMenu.setHero(this);
		} else {
			bottomMenu.setHero(null);
		}
	}

	public boolean isPlayerUnit() {
		return isPlayerUnit;
	}

	public void setPlayerUnit(boolean isPlayerUnit) {
		this.isPlayerUnit = isPlayerUnit;
	}

	public int getAp() {
		return ap;
	}

	public void setAp(final int ap) {
		this.ap = ap;
		updateUI();
	}

	public int getHp() {
		return hp;
	}

	public void setHp(final int hp) {
		this.hp = hp;
		updateUI();
	}

	public int getEntityID() {
		return entityID;
	}

	public int getCurrentInitiative() {
		return currentInitiative;
	}

	public void setCurrentInitiative(final int currentInitiative) {
		this.currentInitiative = currentInitiative;
	}

	public TextureRegion getFrame() {
		return entityAnimation.getFrame();
	}

	public void setDirection(final Direction direction) {
		entityAnimation.setDirection(direction);
	}

	public EntityAnimation getEntityAnimation() {
		return entityAnimation;
	}

	public void changeAnimation(final EntityAnimation tempAnimation) {
		entityTemporaryAnimation = entityAnimation;
		entityAnimation = tempAnimation;
	}

	public void restoreAnimation() {
		entityAnimation = entityTemporaryAnimation;
	}

	public void addAbility(final AbilitiesEnum abilityEnum) {
		final Ability ability = new Ability(abilityEnum);
		abilities.add(ability);
	}

	public void removeAbility(final AbilitiesEnum abilityEnum) {
		abilities.removeIf(ability -> (ability.getId() == abilityEnum.ordinal()));
	}

	public void addModifier(final ModifiersEnum type, final int turns, final int amount) {
		final Modifier modifier = new Modifier(type, turns, amount);
		modifiers.add(modifier);
	}

	public void applyModifiers() {
		for (final Modifier mod : modifiers) {
			mod.applyModifier(this);

			if (mod.getTurns() == 0) {
				mod.removeModifier(this);
				modifiers.remove(mod);
			}
		}
	}

	public Collection<Ability> getAbilities() {
		return abilities;
	}

	@Override
	public void addEntityObserver(final EntityObserver entityObserver) {
		observers.add(entityObserver);
	}

	@Override
	public void removeObserver(final EntityObserver entityObserver) {
		observers.removeValue(entityObserver, true);
	}

	@Override
	public void removeAllObservers() {
		observers.removeAll(observers, true);
	}

	@Override
	public void notifyEntityObserver(final EntityObserver.EntityCommand command) {
		for (int i = 0; i < observers.size; i++) {
			observers.get(i).onEntityNotify(command, this);
		}
	}

	public void notifyEntityObserver(final EntityObserver.EntityCommand command, final Ability ability) {
		for (int i = 0; i < observers.size; i++) {
			observers.get(i).onEntityNotify(command, this, ability);
		}
	}

	public void move(List<GridCell> path) {
		final SequenceAction sequence = createMoveSequence(path);
		sequence.addAction(run(aiFinishTurn));
		this.getEntityactor().addAction(sequence);
		this.setAp(this.getAp() - path.size());
	}

	public void moveAttack(List<GridCell> path, Entity target) {
		final SequenceAction sequence = createMoveSequence(path);
		sequence.addAction(new AttackAction(target));
		sequence.addAction(run(aiFinishTurn));

		this.getEntityactor().addAction(sequence);
		this.setAp(this.getAp() - path.size() - this.getEntityData().getBasicAttackCost());
	}

	private SequenceAction createMoveSequence(List<GridCell> path) {
		GridCell oldCell = new GridCell(this.getCurrentPosition().getTileX(), this.getCurrentPosition().getTileY());
		final SequenceAction sequence = Actions.sequence();
		for (final GridCell cell : path) {
			sequence.addAction(Actions.rotateTo(decideRotation(oldCell, cell), 0.1f));
			sequence.addAction(moveTo(cell.x, cell.y, 0.2f));
			sequence.addAction(run(updatePositionAction));
			oldCell = cell;
		}
		return sequence;
	}

	private float decideRotation(GridCell oldCell, GridCell cell) {
		if ((oldCell.x == cell.x) && (oldCell.y > cell.y)) {
			return 270.0f;
		} else if ((oldCell.x == cell.x) && (oldCell.y < cell.y)) {
			return 180.0f;
		} else if ((oldCell.x > cell.x) && (oldCell.y == cell.y)) {
			return 270.0f;
		}
		return 90.0f;
	}

	private void updatePositionFromActor() {
		this.setCurrentPosition(new TiledMapPosition().setPositionFromTiles((int) this.getEntityactor().getX(), (int) this.getEntityactor().getY()));
		this.setDirection(decideDirection(this.getEntityactor().getRotation()));
	}

	private Direction decideDirection(float rotation) {
		if (rotation >= 45 && rotation < 135) {
			return Direction.RIGHT;
		} else if (rotation >= 135 && rotation < 225) {
			return Direction.UP;
		} else if (rotation >= 225 && rotation < 315) {
			return Direction.LEFT;
		}
		return Direction.DOWN;
	}
}

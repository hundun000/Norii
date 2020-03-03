package com.mygdx.game.Particles;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.game.Particles.ParticleType;

import Utility.TiledMapPosition;

public class Particle extends Actor{
	
	private TiledMapPosition pos;
	private Boolean active;
	private ParticleType type;
	private PooledEffect particleEffect;
	
	Particle(TiledMapPosition pos, PooledEffect pe, ParticleType type){
		super();
		this.pos = pos;
		this.active = true;
		this.type = type;
		this.particleEffect = pe;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void deactivate() {
		this.active = false;
	}
    
	public void draw(SpriteBatch spriteBatch, float delta) {
		particleEffect.draw(spriteBatch, delta);
	}
	
	public void delete() {
		particleEffect.free();
	}
	
	public ParticleType getParticleType() {
		return type;
	}
	
	public TiledMapPosition getPosition() {
		return pos;
	}
	
	public void update(float delta) {
		this.particleEffect.update(delta);
	}
	
	public boolean isComplete() {
		return this.particleEffect.isComplete();
	}
	
	public PooledEffect getParticleEffect() {
		return particleEffect;
	}
}

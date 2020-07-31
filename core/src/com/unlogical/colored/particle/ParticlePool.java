package com.unlogical.colored.particle;

import java.util.ArrayList;
import java.util.Map;

import com.unlogical.colored.level.Level;
import com.unlogical.colored.util.IDeepCopyable;

public class ParticlePool implements IDeepCopyable<ParticlePool>
{
	public Particle[] particles;
	public ArrayList<Particle> availableParticles;

	private ParticleSystem system;
	private ParticleEmitter emitter;

	public static int totalParticles;

	public ParticlePool(ParticleEmitter emitter, ParticleSystem system, int maxParticles)
	{
		this.init(emitter, system, maxParticles);
	}

	private ParticlePool()
	{

	}

	private void init(ParticleEmitter emitter, ParticleSystem system, int maxParticles)
	{
		this.system = system;
		this.emitter = emitter;

		this.particles = new Particle[maxParticles];
		this.availableParticles = new ArrayList<Particle>(maxParticles);

		for (int i = 0; i < this.particles.length; i++)
		{
			this.particles[i] = new Particle(system);
		}

		this.reset();
	}

	@Override
	public ParticlePool deepCopy(Map<Object, Object> copiedReferences)
	{
		ParticlePool copy = new ParticlePool();

		Level.putCopy(this, copy, copiedReferences);

		copy.init((ParticleEmitter) Level.getCopy(this.emitter, copiedReferences), (ParticleSystem) Level.getCopy(this.system, copiedReferences), this.particles.length);
		copy.availableParticles.clear();

		for (int i = 0; i < this.particles.length; i++)
		{
			copy.particles[i] = this.particles[i].deepCopy(copiedReferences);

			if (copy.particles[i].getLifetime() <= 0)
			{
				copy.availableParticles.add(copy.particles[i]);
			}
		}

		return copy;
	}

	public void setSystem(ParticleSystem system)
	{
		this.system = system;

		for (Particle particle : this.particles)
		{
			particle.setSystem(system);
		}
	}

	@Override
	public boolean shouldCopy()
	{
		return this.emitter.shouldCopy();
	}

	public Particle getUnusedParticle()
	{
		return this.availableParticles.get(0);
	}

	public void reset()
	{
		this.availableParticles.clear();

		for (Particle particle : this.particles)
		{
			this.availableParticles.add(particle);
		}
	}
}

package com.unlogical.colored.profiling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.utils.PerformanceCounter;
import com.unlogical.colored.GameLauncher;
import com.unlogical.colored.debug.Debug;

public class Profiling
{
	public static final Section root = new Section(null, "root");
	private static Section currentSection = root;
	public static boolean enabled = GameLauncher.isDeveloperMode();

	public static void tick()
	{
		root.counter.stop();

		root.tick();

		root.counter.start();
	}

	public static void reset()
	{
		// root.reset();
		root.clear();

		currentSection = root;
	}

	public static void startSection(String section)
	{
		if (!enabled)
		{
			return;
		}

		if (!currentSection.subSections.containsKey(section))
		{
			currentSection.subSections.put(section, new Section(currentSection, section));
		}

		currentSection = currentSection.subSections.get(section);

		currentSection.counter.start();
	}

	public static void endSection(String section)
	{
		if (!enabled)
		{
			return;
		}

		if (currentSection == root)
		{
			Debug.warn("Cannot end root section, must call start() before");
		}

		if (!currentSection.name.equals(section))
		{
			Debug.warn("Currently in session " + currentSection.name + ", cannot end " + section);
		}

		currentSection.counter.stop();

		currentSection.lastUsed = System.currentTimeMillis();

		currentSection = currentSection.parent;
	}

	@SuppressWarnings("unused")
	public static class Section
	{
		private Set<String> toDelete = new HashSet<String>();

		private Section parent;
		private Map<String, Section> subSections = new HashMap<String, Section>();
		private String name;
		private String entireName;
		private long lastUsed = System.currentTimeMillis();
		public final PerformanceCounter counter;

		public Section(Section parent, String name)
		{
			this.parent = parent;
			this.name = name;
			this.counter = new PerformanceCounter(name);
			this.entireName = this.getEntireSectionName();
		}

		private void tick()
		{
			if (this.counter.valid)
			{
				this.counter.tick();
			}

			for (Section sub : this.subSections.values())
			{
				sub.tick();
			}
		}

		private void reset()
		{
			this.counter.reset();

			for (Section sub : this.subSections.values())
			{
				sub.tick();
			}
		}

		private boolean clear()
		{
			for (Section sub : this.subSections.values())
			{
				if (sub.clear())
				{
					this.toDelete.add(sub.name);
				}
			}

			for (String name : this.toDelete)
			{
				this.subSections.remove(name);
			}

			this.toDelete.clear();

			return System.currentTimeMillis() - this.lastUsed > 400;
		}

		private String getEntireSectionName()
		{
			return (this.parent != null ? this.parent.getEntireSectionName() + "." : "") + this.name;
		}

		@Override
		public String toString()
		{
			return this.name + ": " + this.counter.load.latest;
		}

		public String fancyNameString(String preamble)
		{
			String str = preamble + this.name + "\n";

			for (Section sub : this.subSections.values())
			{
				str += sub.fancyNameString(preamble + "|--");
			}

			return str;
		}

		public String fancyInfoString()
		{
			String info = String.format("%3.3f", this.counter.load.latest * 100) + "\n";

			for (Section sub : this.subSections.values())
			{
				info += sub.fancyInfoString();
			}

			return info;
		}

		public Set<Section> fetchAllSections(Set<Section> allSections)
		{
			allSections.add(this);

			for (Section sub : this.subSections.values())
			{
				sub.fetchAllSections(allSections);
			}

			return allSections;
		}
	}

	public static Section getCurrentSection()
	{
		return currentSection;
	}

	public static Section getRoot()
	{
		return root;
	}
}

package se.nielstrom.greed.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Die {
	private static final int DEFAULT_NR_SIDES = 6;
	public final int NR_OF_SIDES;
	
	private final List<DieChangeListener> changeListeners;
	
	private int value;
	private boolean locked;
	
	public Die() {
		this(DEFAULT_NR_SIDES);
	}
	
	public Die(int nrOfSides) {
		this(nrOfSides, nrOfSides);
	}
	
	public Die(int nrOfSides, int value) {
		changeListeners = new ArrayList<>();
		this.NR_OF_SIDES = nrOfSides;
		
		setValue(value);
		setLocked(false);
	}
	
	public void roll() {
		if (!isLocked()) {
			setValue((new Random()).nextInt(NR_OF_SIDES) + 1);
		}
	}

	public int getValue() {
		return value;
	}

	public Die setValue(int value) {
		this.value = value;
		
		for(DieChangeListener listener : changeListeners) {
			listener.onDieChanged(this);
		}
		
		return this;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void addChangeListener(DieChangeListener listener) {
		changeListeners.add(listener);
		listener.onDieChanged(this);
	}
	
	public void removeChangeListener(DieChangeListener listener) {
		changeListeners.remove(listener);
	}
	
	public interface DieChangeListener {
		public void onDieChanged(Die die);
	}
}

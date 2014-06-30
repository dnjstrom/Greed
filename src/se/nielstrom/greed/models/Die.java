package se.nielstrom.greed.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Die {
	private final int NR_OF_SIDES;
	private final List<DieChangeListener> changeListeners;
	
	private int value;
	private boolean locked;

	public Die() {
		this(6);
	}

	public Die(int nrOfSides) {
		NR_OF_SIDES = nrOfSides;
		changeListeners = new ArrayList<>();
		setValue(NR_OF_SIDES);
	}

	public Die roll() {
		return setValue( (new Random()).nextInt(NR_OF_SIDES) + 1 );
	}

	public int getValue() {
		return value;
	}

	public Die setValue(int value) {
		if (isLocked()) {
			return this;
		} else if (value < 1 || NR_OF_SIDES < value) {
			throw new IllegalArgumentException("Can only set a number between 1 and "
				+ NR_OF_SIDES
				+ " - " + value + " attempted.");
		}

		this.value = value;
		
		for(DieChangeListener listener : changeListeners) {
			listener.onDieChanged(this);
		}
		
		return this;
	}

	public boolean isLocked() {
		return locked;
	}

	public Die setLocked(boolean locked) {
		this.locked = locked;
		for(DieChangeListener listener : changeListeners) {
			listener.onDieChanged(this);
		}
		return this;
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

	public void toggleLocked() {
		setLocked(!isLocked());
	}
}
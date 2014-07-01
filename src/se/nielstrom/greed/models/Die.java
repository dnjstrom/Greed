package se.nielstrom.greed.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Models a die with any number of sides and which value can be locked, disallowing
 * any changes until it's unlocked again. Classes interested in the value of the
 * die can implement the DieChangeListener interface and register themselves to
 * get updates.
 * 
 * @author Daniel Ström
 */
public class Die {
	private final int NR_OF_SIDES;
	private final List<DieChangeListener> changeListeners;
	
	private int value;
	private boolean locked;

	public Die() {
		this(6); // Default as 6-sided die
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
		
		// send update to listeners
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

	public void toggleLocked() {
		setLocked(!isLocked());
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
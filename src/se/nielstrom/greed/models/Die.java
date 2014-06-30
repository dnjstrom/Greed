package se.nielstrom.greed.models;

import java.util.Random;

public class Die {
	private final int NR_OF_SIDES;
	private int value;
	private boolean locked;

	public Die() {
		this(6);
	}

	public Die(int nrOfSides) {
		NR_OF_SIDES = nrOfSides;
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
		return this;
	}

	public boolean isLocked() {
		return locked;
	}

	public Die setLocked(boolean locked) {
		this.locked = locked;
		return this;
	}

	public static void main(String[] args) {
		Die die = new Die(6);
		System.out.println("" + die.getValue());

		System.out.println("-----");

		System.out.println("" + die.setValue(1).getValue());
		System.out.println("" + die.setValue(3).getValue());
		System.out.println("" + die.setValue(6).getValue());

		System.out.println("-----");

		for (int i=1; i<=100; i++) {
			System.out.print(die.roll().getValue());
			if (i % 20 == 0) {
				System.out.println("");
			}
		}

		System.out.println("-----");

		die.setValue(6);
		die.setLocked(true);
		for (int i=1; i<=10; i++) {
			System.out.print(die.roll().getValue());
		}

	}
}
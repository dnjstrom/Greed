package se.nielstrom.greed.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.nielstrom.greed.views.DieButton;

public class Greed {
	public static final int MIN_SCORE = 300;
	public final int NR_OF_DICE;
	private int totalScore;
	private int roundScore;
	private int rounds;
	private Die[] dice;
	
	public Greed(int nrOfDice) {
		roundScore = 0;
		totalScore = 0;
		rounds = 0;
		NR_OF_DICE = nrOfDice;
		
		dice = new Die[NR_OF_DICE];
		for(int i=0; i<NR_OF_DICE; i++) {
			dice[i] = new Die();
		}
	}
	
	public boolean roll() {
		if (allDiceAreLocked() && allDiceAreUsed(dice)) {
			
			unlockDice();
		}
		rollDice();
		
		int turnScore = calculateScore(dice);
		
		if ((turnScore+roundScore) > Math.max(roundScore, MIN_SCORE-1)) {
			roundScore += turnScore;
			return true;
		} else {
			roundScore = 0;
			return false;
		}		
	}
	
	public void claim() {
		totalScore += roundScore;
		rounds++;
	}
	
	public boolean allDiceAreLocked() {
		for(Die die : dice) {
			if (!die.isLocked()) {
				return false;
			}
		}
		return true;
	}
	
	private void unlockDice() {
		for(Die die: dice) {
			die.setLocked(false);
		}
	}
	
	private void rollDice() {
		for(Die die: dice) {
			die.roll();
		}
	}


	/**
	 * @param dice	A parameter list or array of Integers representing the side
	 * 				of a dice (valid sides: 1-6).
	 * @return		True if all dice contribute points to the total score, False
	 * 				otherwise
	 */
	public boolean allDiceAreUsed(Die... dice) {
		// Count the number of dice of each side
		Map<Integer, Integer> sideAmount = aggregateDice(dice);

		if (sideAmount.size() != NR_OF_DICE) { // unless we have a ladder
			for (Entry<Integer, Integer> entry : sideAmount.entrySet()) { // for each kind of die
				if ((entry.getKey() != 1 && entry.getKey() != 5) // if the side is 2,3,4 or 6
						&& entry.getValue() % 3 != 0) { // and the number of dice is not a multiple of 3
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Counts the number of dice showing a given side.
	 * 
	 * @param dice	A parameter list or array of Integers representing the side
	 * 				of a dice (valid sides: 1-6).
	 * @return		A map of (side, number of dice with this side) pairs. 
	 */
	private Map<Integer, Integer> aggregateDice(Die... dice) {
		Map<Integer, Integer> sideAmount = new HashMap<>(dice.length);

		for (Die die : dice) {
			Integer previousNumber = sideAmount.get(die.getValue());
			int newNumber = (previousNumber == null) ? 1 : previousNumber + 1;
			sideAmount.put(die.getValue(), newNumber);
		}

		return sideAmount;
	}

	/**
	 * Calculates the score given a set of dice.
	 * 
	 * @param dice	A parameter list or array of Integers representing the side
	 * 				of a dice (valid sides: 1-6).
	 * @return		The total score for all dice.
	 */
	public int calculateScore(Die... dice) {
		int score = 0;

		// Count the dice by value
		Map<Integer, Integer> sideAmount = aggregateDice(dice);

		if (sideAmount.size() == NR_OF_DICE) { // 6 different sides means it's a ladder
			score = 1000;
		} else {
			// Calculate the score for each kind of side
			for (Entry<Integer, Integer> entry : sideAmount.entrySet()) {
				score += scoreHelper(entry.getKey(), entry.getValue());
			}
		}

		return score;
	}

	/**
	 * Recursive method which returns the score for a number of dice showing
	 * the same side.
	 * 
	 * @param value		The side shown by each dice.
	 * @param number	The number of dice showing said side.
	 * @return			The calculated score
	 */
	private int scoreHelper(int value, int number) {
		if (number >= 3) {
			int score = (value == 1) ? 1000 : 100 * value;
			return score + scoreHelper(value, number - 3);
		} else if (value == 1) {
			return number * 100;
		} else if (value == 5) {
			return number * 50;
		} else {
			return 0;
		}
	}
}

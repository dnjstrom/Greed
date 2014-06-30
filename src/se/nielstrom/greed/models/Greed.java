import java.util.*;
import java.util.Map.Entry;

public class Greed {
	private static final int NR_OF_DICE = 6;
	private static final int NR_OF_SIDES = 6;
	private static final int MIN_SCORE = 300;

	private final Die[] dice;
	private int scoreTotal;
	private int scoreRound;
	private int scoreRoundBonus;
	private int round;

	public Greed() {
		dice = new Die[6];
		for (int i=0; i<NR_OF_DICE; i++) {
			dice[i] = new Die(NR_OF_SIDES);
		}

		scoreTotal = 0;
		scoreRound = 0;
		scoreRoundBonus = 0;
		round = 0;
	}

	public Greed roll() {
		if (allDiceAreLocked() && allDiceAreUsed()) {
			scoreRoundBonus += getScoreRound();
			setAllDiceLocked(false);
		}

		rollDice();

		int score = calculateScore() + scoreRoundBonus;

		// Low score or bust
		if ( score <= Math.max(MIN_SCORE-1, getScoreRound()) ) {

		}

		setScoreRound(score);

		return this;
	}

	public int getScoreRound() {
		return scoreRound;
	}

	public Greed setScoreRound(int roundScore) {
		this.scoreRound = roundScore;
		return this;
	}

	public int getScoreTotal() {
		return scoreTotal;
	}

	public Greed setScoreTotal(int totalScore) {
		this.scoreTotal = totalScore;
		return this;
	}

	public Greed appendScoreTotal(int score) {
		setScoreTotal(getScoreTotal() + score);
		return this;
	}

	public Greed claim() {
		appendScoreTotal(getScoreRound());
		setScoreRound(0);
		setAllDiceLocked(false);
		setRound( getRound() + 1 );
		return this;
	}

	public Greed rollDice() {
		for (Die die : dice) {
			die.roll();
		}
		return this;
	}

	public Greed lockDie(int i) {
		dice[i].setLocked(true);
		return this;
	}

	public Greed setAllDiceLocked(boolean locked) {
		for (Die die : dice) {
			die.setLocked(locked);
		}
		return this;
	}

	public boolean allDiceAreLocked() {
		for (Die die : dice) {
			if (!die.isLocked()) {
				return false;
			}
		}
		return true;
	}

	public int getRound() {
		return round;
	}

	public Greed setRound(int round) {
		this.round = round;
		return this;
	}

	public Greed reset() {
		setScoreTotal(0);
		setScoreRound(0);
		scoreRoundBonus = 0;
		setAllDiceLocked(false);
		return this;
	}

	/**
	 * Convenience version of {@link #allDiceAreUsed(Integer... sides) allDiceAreUsed}
	 * which uses the instance dice automatically.
	 */
	private boolean allDiceAreUsed() {
		List<Integer> sides = new ArrayList<>();
		for (Die die : dice) {
			sides.add(die.getValue());
		}
		return allDiceAreUsed(sides);
	}

	/**
	 * Overloads {@link #allDiceAreUsed(Integer... sides) allDiceAreUsed} to
	 * work with any Collection<Integer>.
	 * 
	 * @param dice An Integer collection of sides (ranging from 1 through 6)
	 */
	private boolean allDiceAreUsed(Collection<Integer> dice) {
		Integer[] diceArray = dice.toArray(new Integer[dice.size()]);
		return allDiceAreUsed(diceArray);
	}

	/**
	 * @param dice	A parameter list or array of Integers representing the side
	 * 				of a dice (valid sides: 1-6).
	 * @return		True if all dice contribute points to the total score, False
	 * 				otherwise
	 */
	private boolean allDiceAreUsed(Integer... sides) {
		// Count the number of dice of each side
		Map<Integer, Integer> diceMap = aggregateDice(sides);

		if (diceMap.size() != NR_OF_DICE) { // unless we have a ladder
			for (Entry<Integer, Integer> entry : diceMap.entrySet()) { // for each kind of die
				if ((entry.getKey() != 1 && entry.getKey() != 5) // if the side is 2,3,4 or 6
						&& entry.getValue() % 3 != 0) { // and the number of dice is not a multiple of 3
					return false;
				}
			}
		}

		return true;
	}


	/**
	 * Convenience version of {@link #aggregateDice(Integer... dice) aggregateDice}
	 * which uses the instance dice automatically.
	 */
	private Map<Integer, Integer> aggregateDice() {
		List<Integer> sides = new ArrayList<>();
		for (Die die : dice) {
			sides.add(die.getValue());
		}
		return aggregateDice(sides);
	}

	/**
	 * Overloads {@link #aggregateDice(Integer... dice) aggregateDice} to
	 * work with any Collection<Integer>.
	 * 
	 * @param dice An Integer collection of sides (ranging from 1 through 6)
	 */
	private Map<Integer, Integer> aggregateDice(Collection<Integer> dice) {
		Integer[] diceArray = dice.toArray(new Integer[dice.size()]);
		return aggregateDice(diceArray);
	}

	/**
	 * Counts the number of dice showing a given side.
	 * 
	 * @param dice	A parameter list or array of Integers representing the side
	 * 				of a dice (valid sides: 1-6).
	 * @return		A map of (side, number of dice with this side) pairs. 
	 */
	private Map<Integer, Integer> aggregateDice(Integer... dice) {
		Map<Integer, Integer> diceMap = new HashMap<>(dice.length);

		for (Integer side : dice) {
			Integer previousNumber = diceMap.get(side);
			int newNumber = (previousNumber == null) ? 1 : previousNumber + 1;
			diceMap.put(side, newNumber);
		}

		return diceMap;
	}


	/**
	 * Convenience version of {@link #calculateScore(Integer... dice) calculateScore}
	 * which uses the instance dice automatically.
	 */
	private int calculateScore() {
		List<Integer> sides = new ArrayList<>();
		for (Die die : dice) {
			sides.add(die.getValue());
		}
		return calculateScore(sides);
	}

	/**
	 * Overloads {@link #calculateScore(Integer... dice) calculateScore} to
	 * work with any Collection<Integer>.
	 * 
	 * @param dice An Integer collection of sides (ranging from 1 through 6)
	 */
	private int calculateScore(Collection<Integer> dice) {
		Integer[] diceArray = dice.toArray(new Integer[dice.size()]);
		return calculateScore(diceArray);
	}

	/**
	 * Calculates the score given a set of dice.
	 * 
	 * @param dice	A parameter list or array of Integers representing the side
	 * 				of a dice (valid sides: 1-6).
	 * @return		The total score for all dice.
	 */
	private int calculateScore(Integer... dice) {
		int score = 0;

		// Count the dice by value
		Map<Integer, Integer> diceMap = aggregateDice(dice);

		if (diceMap.size() == NR_OF_DICE) { // 6 different sides means it's a ladder
			score = 1000;
		} else {
			// Calculate the score for each kind of side
			for (Entry<Integer, Integer> entry : diceMap.entrySet()) {
				score += scoreHelper(entry.getKey(), entry.getValue());
			}
		}

		return score;
	}

	/**
	 * Recursive method which returns the score for a number of dice showing
	 * the same side.
	 * 
	 * @param side		The side shown by each dice.
	 * @param number	The number of dice showing said side.
	 * @return			The calculated score
	 */
	private int scoreHelper(int side, int number) {
		if (number >= 3) {
			int score = (side == 1) ? 1000 : 100 * side;
			return score + scoreHelper(side, number - 3);
		} else if (side == 1) {
			return number * 100;
		} else if (side == 5) {
			return number * 50;
		} else {
			return 0;
		}
	}


	////////////////////////////////////////
	/////          Debugging           /////
	/////   (poor mans Unit-testing)   /////
	////////////////////////////////////////

	public static void main(String[] args) {
		Greed game = new Greed();
		game.printDiceLn();

		game.printHeadline("Rolling dice");

		for (int i=0; i<5; i++) {
			game.rollDice().printDice();
			System.out.println(": " + game.calculateScore());
		}

		game.printHeadline("Overflow turn")
			.reset()
			.setDice(1,2,3,4,5,6)
			.setAllDiceLocked(true)
			.setScoreRound(game.calculateScore())
			.printState();

		game.roll().printState();

		game.printHeadline("Claiming a turn")
			.reset()
			.setDice(1,2,3,4,5,6)
			.setAllDiceLocked(true)
			.setScoreRound(game.calculateScore())
			.printState();

		game.claim().printState();

	}

	private Greed printState() {
		printDice();
		System.out.println(" ("
			+ getRound()
			+ "): "
			+ getScoreRound()
			+ ", "
			+ getScoreTotal()
		);
		return this;
	}

	private Greed setDice(Integer... values) {
		for (int i=0; i<dice.length && i<values.length; i++) {
			dice[i].setValue(values[i]);
		}
		return this;
	}

	private Greed printDice() {
		for (Die die : dice) {
			System.out.print("" + die.getValue());
		}
		return this;
	}

	private Greed printDiceLn() {
		printDice();
		System.out.println("");
		return this;
	}

	private Greed printHeadline(String headline) {
		System.out.println("");
		System.out.println(headline);

		for (char c : headline.toCharArray()) {
			System.out.print("-");
		}

		System.out.println("");
		return this;
	}

}

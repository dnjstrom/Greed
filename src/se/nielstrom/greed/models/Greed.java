package se.nielstrom.greed.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Greed class implements the rules and functionality of the game.
 * 
 * @author Daniel
 */
public class Greed implements Parcelable{
	public static final int NR_OF_DICE = 6;
	public static final int NR_OF_SIDES = 6;
	public static final int MIN_SCORE = 300;
	public static final int WINNING_SCORE = 10000;
	
	public static final String ROUND_SCORE = "scoreRound";
	public static final String TOTAL_SCORE = "scoreTotal";
	public static final String ROUNDS = "rounds";
	public static final String STATE = "state";
	public static final String WIN = "win";

	private final Die[] dice;
	private int totalScore;
	private int roundScore;
	private int scoreRoundBonus;
	private int round;
	
	private PropertyChangeSupport propertyListeners;
	
	public static enum State {
		BUST, LOW, OK, WIN
	};
	
	private State state;
	private int previousScore;

	public Greed() {
		propertyListeners = new PropertyChangeSupport(this);
		dice = new Die[6];
		for (int i=0; i<NR_OF_DICE; i++) {
			dice[i] = new Die(NR_OF_SIDES);
		}

		totalScore = 0;
		roundScore = 0;
		scoreRoundBonus = 0;
		round = 0;
		previousScore = 0;
		state = State.BUST;
	}

	public Greed(Parcel parcel) {
		propertyListeners = new PropertyChangeSupport(this);
		dice = new Die[6];
		for (int i=0; i<NR_OF_DICE; i++) {
			dice[i] = new Die(NR_OF_SIDES);
			dice[i].setValue(parcel.readInt());
			dice[i].setLocked(parcel.readByte() != 0);
		}
		
		setRound( parcel.readInt() );
		setScoreRound( parcel.readInt() );
		setScoreTotal( parcel.readInt() );
		previousScore = parcel.readInt();
		scoreRoundBonus = parcel.readInt();
		setState( (State) parcel.readSerializable() );
	}

	/**
	 * One of two primary actions  
	 * @return
	 */
	public Greed roll() {
		if (allDiceAreLocked() && allDiceAreUsed()) {
			scoreRoundBonus += getScoreRound();
			setAllDiceLocked(false);
		}

		previousScore = (getState() == State.OK) ? getScoreRound() : 0;
		
		rollDice();
		
		int score = calculateScore() + scoreRoundBonus;

		// Bust or Low score
		if (getState() == State.OK && score <= getScoreRound()) {
			setRound(getRound() + 1);
			setAllDiceLocked(false);
			scoreRoundBonus = 0;
			setState(State.BUST);
		} else if (score < MIN_SCORE-1) {
			setRound(getRound() + 1);
			setAllDiceLocked(false);
			scoreRoundBonus = 0;
			setState(State.LOW);
		} else {
			setState(State.OK);
		}

		setScoreRound(score);

		return this;
	}

	public State getState() {
		return state;
	}

	public Greed setState(State state) {
		if (this.state != state) {			
			propertyListeners.firePropertyChange(STATE, this.state, state);
			this.state = state;
		}
		return this;
	}

	public int getScoreRound() {
		return roundScore;
	}

	public Greed setScoreRound(int roundScore) {
		propertyListeners.firePropertyChange(ROUND_SCORE, this.roundScore, roundScore);
		this.roundScore = roundScore;
		return this;
	}

	public int getScoreTotal() {
		return totalScore;
	}

	public Greed setScoreTotal(int totalScore) {
		propertyListeners.firePropertyChange(TOTAL_SCORE, this.totalScore, totalScore);
		this.totalScore = totalScore;
		return this;
	}

	public Greed appendScoreTotal(int score) {
		setScoreTotal(getScoreTotal() + score);
		return this;
	}

	public Greed claim() {
		if (getState() == State.OK) {
			appendScoreTotal(calculateScore() + scoreRoundBonus);
			setScoreRound(0);
			scoreRoundBonus = 0;
			setAllDiceLocked(false);
			setRound( getRound() + 1 );
			setState(State.BUST);
			
			if (getScoreTotal() >= WINNING_SCORE) {
				setState(State.WIN);
			}
		}
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
		propertyListeners.firePropertyChange(ROUNDS, this.round, round);
		this.round = round;
		return this;
	}
	
	public Die[] getDice() {
		return dice;
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
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyListeners.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyListeners.removePropertyChangeListener(listener);
    }

	public boolean updateScore() {
		List<Integer> lockedDice = new ArrayList<>();

		// Only care about checked dice
		for (Die die : dice) {
			if (die.isLocked()) {
				lockedDice.add(die.getValue());
			}
		}

		int score = calculateScore(lockedDice) + scoreRoundBonus;

		setScoreRound(score); // Update the score
		return getState() != State.OK || score > Math.max(MIN_SCORE-1, previousScore);
	}

	public void resetGame() {
		setRound(0);
		setScoreRound(0);
		setScoreTotal(0);
		for(Die die : dice) {
			die.setValue(6);
			die.setLocked(false);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		for(int i=0; i<dice.length; i++) {
			dest.writeInt(dice[i].getValue());
			dest.writeByte( (byte) (dice[i].isLocked() ? 1 : 0) );
		}
		dest.writeInt(getRound());
		dest.writeInt(getScoreRound());
		dest.writeInt(getScoreTotal());
		dest.writeInt(previousScore);
		dest.writeInt(scoreRoundBonus);
		dest.writeSerializable(getState());
	}
	
	public static final Parcelable.Creator<Greed> CREATOR = new Parcelable.Creator<Greed>() {
		@Override
		public Greed createFromParcel(Parcel parcel) {
			return new Greed(parcel);
		}

		@Override
		public Greed[] newArray(int size) {
			return new Greed[size];
		}
		
	};
}
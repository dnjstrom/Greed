package se.nielstrom.greed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.nielstrom.greed.DieButton.StateChangeListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is the main activity which houses the game logic.
 * 
 * @author Daniel Ström
 *
 */
public class GameActivity extends Activity {
	public static final int MAX_SCORE = 10000;
	public static final int MIN_SCORE = 300;
	public static final int NR_OF_DICE = 6;

	private DieButton[] diceButtons = new DieButton[NR_OF_DICE];
	private int round = 0;
	private int totalScore = 0;
	private int previousScore = 0;
	private int roundScore = 0;
	private int roundScoreBonus = 0;
	private boolean firstRoll = true;
	private int nrChecked = NR_OF_DICE;
	private Button claimButton;
	private Button rollButton;

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_game);

		// Store a reference to all six custom die views
		diceButtons[0] = (DieButton) findViewById(R.id.die_a);
		diceButtons[1] = (DieButton) findViewById(R.id.die_b);
		diceButtons[2] = (DieButton) findViewById(R.id.die_c);
		diceButtons[3] = (DieButton) findViewById(R.id.die_d);
		diceButtons[4] = (DieButton) findViewById(R.id.die_e);
		diceButtons[5] = (DieButton) findViewById(R.id.die_f);

		// Action button references
		rollButton = (Button) findViewById(R.id.roll_button);
		claimButton = (Button) findViewById(R.id.claim_button);

		// Listen to state changes on the button, that is, whether
		// a dice is "checked" or "unchecked".
		for (DieButton die : diceButtons) {
			die.addStateChangeListener(new StateChangeListener() {
				@Override
				public void onStateChange(DieButton button) {
					nrChecked += (button.isChecked()) ? 1 : -1;
					checkMinScore();
				}
			});
		}

		// Initialize the game state if there's no previous state.
		if (state == null) {
			resetGame();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt("round", getRound());
		bundle.putInt("totalScore", getTotalScore());
		bundle.putInt("previousScore", previousScore);
		bundle.putInt("roundScore", getRoundScore());
		bundle.putInt("roundScoreBonus", roundScoreBonus);
		bundle.putBoolean("firstRoll", firstRoll);
		bundle.putInt("nrChecked", nrChecked);

		bundle.putBoolean("rollButtonEnabled", rollButton.isEnabled());
		bundle.putBoolean("claimButtonEnabled", claimButton.isEnabled());
	}

	@Override
	protected void onRestoreInstanceState(Bundle bundle) {
		super.onRestoreInstanceState(bundle);
		setRound(bundle.getInt("round"));
		setTotalScore(bundle.getInt("totalScore"));
		previousScore = bundle.getInt("previousScore");
		setRoundScore(bundle.getInt("roundScore"));
		roundScoreBonus = bundle.getInt("roundScoreBonus");
		firstRoll = bundle.getBoolean("firstRoll");
		nrChecked = bundle.getInt("nrChecked");

		rollButton.setEnabled(bundle.getBoolean("rollButtonEnabled"));
		claimButton.setEnabled(bundle.getBoolean("claimButtonEnabled"));
	}

	/**
	 * The callback run when the player clicks the roll button.
	 * 
	 * @param v	Not used, called by the system when specifying onClick
	 * 			callback in xml.
	 */
	public void rollDice(View v) {
		if (isFirstRoll()) { // Set up the dice for first use
			for (DieButton die : diceButtons) {
				die.setEnabled(true);
				die.setChecked(true);
			}
			nrChecked = NR_OF_DICE;
			previousScore = 0;
		} else if (allDiceAreUsed() && nrChecked == 0) { // Reset dice but retain score
			previousScore = roundScoreBonus = getRoundScore();
			for (DieButton die : diceButtons) {
				die.setEnabled(true);
				die.setChecked(true);
			}
			nrChecked = NR_OF_DICE;
			rollButton.setEnabled(false);
		} else {
			previousScore = getRoundScore();
		}

		// Roll the checked dice
		for (DieButton die : diceButtons) {
			if (die.isChecked()) {
				die.roll();
			} else {
				die.setEnabled(false); // Disallow changing saved dice
			}
		}

		int score = calculateScore() + roundScoreBonus;

		if (isFirstRoll() && score < MIN_SCORE) { // Record a round but no points
			setRoundScore(score); // Display the too low score
			claimHelper(0);
			rollButton.setEnabled(true);
		} else if (score <= previousScore) { // Record a round but no points 
			setRoundScore(0); // Set the round score to 0 
			claimHelper(0);
			rollButton.setEnabled(true);
		} else { // Record the new score
			setRoundScore(score);
			rollButton.setEnabled(false);
			setFirstRoll(false);
		}
	}

	/**
	 * Convenience version of {@link #allDiceAreUsed(Integer... sides) allDiceAreUsed}
	 * which uses the instance diceButtons automatically.
	 */
	private boolean allDiceAreUsed() {
		List<Integer> sides = new ArrayList<>();
		for (DieButton die : diceButtons) {
			sides.add(die.getCurrentSide());
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
	 * which uses the instance diceButtons automatically.
	 */
	private Map<Integer, Integer> aggregateDice() {
		List<Integer> sides = new ArrayList<>();
		for (DieButton die : diceButtons) {
			sides.add(die.getCurrentSide());
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
	 * which uses the instance diceButtons automatically.
	 */
	private int calculateScore() {
		List<Integer> sides = new ArrayList<>();
		for (DieButton die : diceButtons) {
			sides.add(die.getCurrentSide());
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

	/**
	 * Updates the score and available actions depending on what dice are checked. 
	 */
	private void checkMinScore() {
		List<Integer> uncheckedDice = new ArrayList<>();

		// Only care about checked dice
		for (DieButton die : diceButtons) {
			if (!die.isChecked()) {
				uncheckedDice.add(die.getCurrentSide());
			}
		}

		int score = calculateScore(uncheckedDice) + roundScoreBonus;

		setRoundScore(score); // Update the score

		// Allow to roll the dice if the checked dice contribute to a score thats
		// 300 or more, or more than the previous score - whichever is higher. 
		rollButton.setEnabled(score > Math.max(MIN_SCORE - 1, previousScore));
	}

	/**
	 * Called by the system when the user clicks the claim button.
	 * @param v Not used
	 */
	public void claimRound(View v) {
		claimHelper(calculateScore() + roundScoreBonus);
		setRoundScore(0);
		rollButton.setEnabled(true);
	}

	/**
	 * This method does the real work when claiming a round (adding the rounds
	 * score to the total).
	 * 
	 * @param score The score to add to the total.
	 */
	public void claimHelper(int score) {
		setTotalScore(getTotalScore() + score);
		setRound(getRound() + 1);
		setFirstRoll(true);
		roundScoreBonus = 0;

		for (DieButton die : diceButtons) {
			die.setEnabled(false);
		}

		if (getTotalScore() >= MAX_SCORE) {
			// The game is over, start the score activity.
			Intent intent = new Intent(this, ScoreActivity.class);
			intent.putExtra(ScoreActivity.SCORE, getTotalScore());
			intent.putExtra(ScoreActivity.ROUNDS, getRound());
			// I use the "startActivityForResult" method so I can reset the
			// game on returning to the game activity instead of doing it right
			// away. Makes for a smoother transition between the two activities.
			startActivityForResult(intent, 0);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Returning from the score activity, start a new game.
		resetGame();
	}

	/**
	 * Reset the game state in preparation for a new game.
	 */
	private void resetGame() {
		for (DieButton die : diceButtons) {
			die.setChecked(false);
			die.setEnabled(false);
		}
		
		rollButton.setEnabled(true);
		claimButton.setEnabled(false);
		
		setTotalScore(0);
		setRoundScore(0);
		setRound(0);
		setFirstRoll(true);
		roundScoreBonus = 0;
		previousScore = 0;
	}

	
	/*
	 * Setters and Getters 
	 */
	
	public boolean isFirstRoll() {
		return firstRoll;
	}

	public void setFirstRoll(boolean firstRoll) {
		this.firstRoll = firstRoll;
		claimButton.setEnabled(!firstRoll);
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
		TextView roundText = (TextView) findViewById(R.id.rounds_label);
		roundText.setText(round + " " + getResources().getString(R.string.rounds));
	}

	public int getRoundScore() {
		return roundScore;
	}

	public void setRoundScore(int score) {
		roundScore = score;
		TextView text = (TextView) findViewById(R.id.round_points);
		text.setText(roundScore + " " + getResources().getString(R.string.points));

		int color_id;

		if (score == 0) {
			color_id = R.color.score_bust;
		} else if (score < MIN_SCORE) {
			color_id = R.color.score_low;
		} else {
			color_id = R.color.score_ok;
		}

		text.setTextColor(getResources().getColor(color_id));
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int score) {
		totalScore = score;
		TextView totalText = (TextView) findViewById(R.id.total_points);
		totalText.setText((totalScore) + " "
				+ getResources().getString(R.string.total));
	}
}
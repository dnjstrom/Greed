package se.nielstrom.greed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.nielstrom.greed.DieButton.StateChangeListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity {
	public final int minScore = 300;
	public final int nrDice = 6;
	
	private DieButton[] diceButtons = new DieButton[nrDice];
	private int round = 0;
	private int totalScore = 0;
	private int previousScore = 0;
	private int roundScore = 0;
	private int roundScoreBonus = 0;
	private boolean firstRoll = true;
	private int nrChecked = nrDice;
	private Button claimButton;
	private Button rollButton;
	
	private enum Score { OK, LOW, BUST }
	
	private Score scoreState = Score.OK;
			
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_game);
        
        diceButtons[0] = (DieButton)findViewById(R.id.die_a);
    	diceButtons[1] = (DieButton)findViewById(R.id.die_b);
    	diceButtons[2] = (DieButton)findViewById(R.id.die_c);
    	diceButtons[3] = (DieButton)findViewById(R.id.die_d);
    	diceButtons[4] = (DieButton)findViewById(R.id.die_e);
    	diceButtons[5] = (DieButton)findViewById(R.id.die_f);
    	
    	rollButton = (Button) findViewById(R.id.roll_button);
    	claimButton = (Button) findViewById(R.id.claim_button);
        
    	for(DieButton die : diceButtons) {
    		die.addStateChangeListener(new StateChangeListener() {
				@Override
				public void onStateChange(DieButton button) {
					nrChecked += (button.isChecked()) ? 1 : -1;
					checkMinScore();
				}
			});
    	}
    	
    	// Create activity from scratch
        if (state == null) {
        	for(DieButton die : diceButtons) {
        		die.setEnabled(false);
        		claimButton.setEnabled(false);
        	}
        	
        	//setRound(0);
        	//setRoundScore(0);
        	//setTotalScore(0);
        	//setFirstRoll(true);
		} 
    	
    }

    @Override
    protected void onSaveInstanceState (Bundle bundle) {
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
    	bundle.putSerializable("scoreState", scoreState);
    }
    
    @Override
    protected void onRestoreInstanceState (Bundle bundle) {
    	super.onRestoreInstanceState(bundle);
    	setRound( bundle.getInt("round") );
    	setTotalScore( bundle.getInt("totalScore") );
    	previousScore = bundle.getInt("previousScore");
    	setRoundScore( bundle.getInt("roundScore") );
    	roundScoreBonus = bundle.getInt("roundScoreBonus");
    	firstRoll = bundle.getBoolean("firstRoll");
    	nrChecked = bundle.getInt("nrChecked");
    	
    	rollButton.setEnabled( bundle.getBoolean("rollButtonEnabled") );
    	claimButton.setEnabled( bundle.getBoolean("claimButtonEnabled") );
    	setScoreState( (Score) bundle.getSerializable("scoreState") );
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    public void rollDice(View v) {
    	if (isFirstRoll()) {    		
    		for(DieButton die : diceButtons) {
        		die.setEnabled(true);
        		die.setChecked(true);
        	}
    		nrChecked = nrDice;
    		previousScore = 0;
		} else if (allDiceAreUsed() && nrChecked == 0) {
			previousScore = roundScoreBonus = getRoundScore();
    		for(DieButton die : diceButtons) {
        		die.setEnabled(true);
        		die.setChecked(true);
        	}
    		nrChecked = nrDice;
    		rollButton.setEnabled(false);
		} else {
			previousScore = getRoundScore();
		}
    	
    	for(DieButton die : diceButtons) {
    		if (die.isChecked()) {
	    		die.roll();
    		} else {
    			die.setEnabled(false);
    		}
    	}
    	
    	int score = calculateScore() + roundScoreBonus;
    	
    	if (isFirstRoll() && score < minScore) { // Low initial score
    		setRoundScore(score);
    		setScoreState(Score.LOW);
			claimHelper(0);
			rollButton.setEnabled(true);
		} else if (score <= previousScore) { // No new points, bust
			setRoundScore(0);
			setScoreState(Score.BUST);
			claimRound(null);
			rollButton.setEnabled(true);
		}else {
			setRoundScore(score);
			setScoreState(Score.OK);	
			rollButton.setEnabled(false);
			setFirstRoll(false);
		}
    }
    
    private boolean allDiceAreUsed() {
    	List<Integer> sides = new ArrayList<>();
    	for(DieButton die : diceButtons) {
    		sides.add(die.getCurrentSide());
    	}
    	return allDiceAreUsed(sides);
    }
    
    private boolean allDiceAreUsed(Collection<Integer> dice) {
    	Integer[] diceArray = dice.toArray(new Integer[dice.size()]);
    	return allDiceAreUsed(diceArray);
	}
    
    private boolean allDiceAreUsed(Integer... sides) {
		Map<Integer, Integer> diceMap = aggregateDice(sides);
    	
    	if(diceMap.size() != nrDice) { // unless we have a ladder
    		for(Entry<Integer, Integer> entry : diceMap.entrySet()) {
        		if(( entry.getKey() != 1 && entry.getKey() != 5) // if side is 2,3,4 or 6
    				 && entry.getValue() % 3 != 0 ) { // and the number of dice is not a multiple of 3
        			return false;
        		}
        	}
    	}
    	
    	return true;
    }
    
    private Map<Integer, Integer> aggregateDice() {
    	List<Integer> sides = new ArrayList<>();
    	for(DieButton die : diceButtons) {
    		sides.add(die.getCurrentSide());
    	}
    	return aggregateDice(sides);
    }
    
    private Map<Integer, Integer> aggregateDice(Collection<Integer> dice) {
    	Integer[] diceArray = dice.toArray(new Integer[dice.size()]);
    	return aggregateDice(diceArray);
    }
	
	private Map<Integer, Integer> aggregateDice(Integer... dice) {
		Map<Integer, Integer> diceMap = new HashMap<>(dice.length);
		
		for(Integer side: dice) {
			Integer previousNumber = diceMap.get(side);
			int newNumber = (previousNumber == null) ? 1 : previousNumber + 1; 
			diceMap.put(side, newNumber);
		}
		
		return diceMap;
	}
	
	
	private int calculateScore() {
		List<Integer> sides = new ArrayList<>();
    	for(DieButton die : diceButtons) {
    		sides.add(die.getCurrentSide());
    	}
    	return calculateScore(sides);
	}
	
	private int calculateScore(Collection<Integer> dice) {
		Integer[] diceArray = dice.toArray(new Integer[dice.size()]);
    	return calculateScore(diceArray);
	}
	
	private int calculateScore(Integer... dice) {
		int score = 0;
		
		Map<Integer, Integer> diceMap = aggregateDice(dice);
		
		if (diceMap.size() == nrDice) { // 6 different sides means it's a ladder
			score = 1000;
		} else {
			// Calculate the score for each kind of side
			for (Entry<Integer, Integer> entry : diceMap.entrySet()){
				score += scoreHelper(entry.getKey(), entry.getValue());
			}			
		}
		
		return score;
	}

	private int scoreHelper(int side, int number) {
		if(number >= 3) {
			int score = (side == 1) ? 1000 : 100*side;
			return score + scoreHelper(side, number - 3);
		} else if (side == 1) {
			return number * 100;
		} else if (side == 5) {
			return number * 50;
		} else {
			return 0;
		}
	}
	
	private void checkMinScore() {
    	List<Integer> uncheckedDice = new ArrayList<>();
    	
    	for(DieButton die : diceButtons) {
    		if (!die.isChecked()) {
    			uncheckedDice.add(die.getCurrentSide());
    		}
    	}
    	
    	int score = calculateScore(uncheckedDice) + roundScoreBonus;
    	
    	setRoundScore(score);
    	
    	rollButton.setEnabled(score > Math.max(minScore-1, previousScore));
    }
    
    public void claimRound(View v) {
    	claimHelper(calculateScore() + roundScoreBonus);
    	setRoundScore(0);
    	rollButton.setEnabled(true);
    }
    
    public void claimHelper(int score) {
    	setTotalScore(getTotalScore() + score);
    	setRound(getRound() + 1);
    	setFirstRoll(true);
    	roundScoreBonus = 0;
    	
    	for(DieButton die : diceButtons) {
    		die.setEnabled(false);
    	}
    }
    
	private void setScoreState(Score state) {
		scoreState  = state;
    	TextView text = (TextView) findViewById(R.id.round_points);
    	int color_id;
    	
    	switch (state) {
		case LOW:
			color_id = R.color.score_low;
			break;
		case BUST:
			color_id = R.color.score_bust;
			break;
		case OK:
		default:
			color_id = R.color.score_ok;
			break;
		}
    	
    	text.setTextColor(getResources().getColor(color_id));
    }
    
    
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
		TextView roundText = (TextView) findViewById(R.id.rounds);
    	roundText.setText(round + " " + getResources().getString(R.string.rounds));
	}

	public int getRoundScore() {
    	return roundScore;
    }
    
    public void setRoundScore(int score) {
    	roundScore = score;
    	TextView roundText = (TextView) findViewById(R.id.round_points);
    	roundText.setText(roundScore + " " + getResources().getString(R.string.points));
    }
    
	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int score) {
		totalScore = score;
		TextView totalText = (TextView) findViewById(R.id.total_points);
    	totalText.setText((totalScore) + " " + getResources().getString(R.string.total));
	}
}
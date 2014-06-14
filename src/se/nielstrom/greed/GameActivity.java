package se.nielstrom.greed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import se.nielstrom.greed.DieButton.StateChangeListener;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity {
	private DieButton[] diceButtons = new DieButton[6];
	private int round = 0;
	private int totalScore = 0;
	private int previousScore = 0;
	private int roundScore = 0;
	private int roundScoreBonus = 0;
	private boolean firstRoll = true;
	
	public final int minScore = 300;
	private boolean allDiceUsed;
	
	private enum Score { OK, LOW, BUST }
			
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	diceButtons[0] = (DieButton)findViewById(R.id.die_a);
    	diceButtons[1] = (DieButton)findViewById(R.id.die_b);
    	diceButtons[2] = (DieButton)findViewById(R.id.die_c);
    	diceButtons[3] = (DieButton)findViewById(R.id.die_d);
    	diceButtons[4] = (DieButton)findViewById(R.id.die_e);
    	diceButtons[5] = (DieButton)findViewById(R.id.die_f);
    	
    	for(DieButton die : diceButtons) {
    		die.addStateChangeListener(new StateChangeListener() {
				@Override
				public void onStateChange() {
					checkMinScore();
				}
			});
    	}
    	
    	setRound(0);
    	setRoundScore(0);
    	setTotalScore(0);
    	setFirstRoll(true); 
    }
    
    public void rollDice(View v) {
    	if (isFirstRoll()) {    		
    		for(DieButton die : diceButtons) {
        		die.setEnabled(true);
        		die.setChecked(true);
        	}
    		previousScore = 0;
		} else if (allDiceUsed) {
			allDiceUsed = false;
			
    		for(DieButton die : diceButtons) {
        		die.setEnabled(true);
        		die.setChecked(true);
        	}
    		
    		roundScoreBonus = previousScore;
		}
    	
    	List<Integer> dice = new ArrayList<>(6);
    	
    	for(DieButton die : diceButtons) {
    		if (die.isChecked()) {
	    		int side = (new Random()).nextInt(5) + 1;
				die.setText("" + side);
				dice.add(side);
    		} else {
    			die.setEnabled(false);
    			int side = Integer.parseInt((String) die.getText());
    			dice.add(side);
    		}
    	}
    	
    	allDiceUsed = allDiceAreUsed(dice);
    	
    	if(allDiceUsed) {
    		System.out.println("");
    	}
    	
    	int score = calculateScore(dice) + roundScoreBonus;
    	
    	if (isFirstRoll() && score < minScore) { // Low initial score
    		setRoundScore(score);
    		setScoreState(Score.LOW);
			claimHelper(0);
			enableRollButton(true);
		} else if (score <= previousScore) { // No new points, bust
			setRoundScore(0);
			setScoreState(Score.BUST);
			claimRound(null);
			enableRollButton(true);
		}else {
			setRoundScore(score);
			setScoreState(Score.OK);
			
			// Force user to select 300 points worth of dice
			if (isFirstRoll()) {
				enableRollButton(false);
			}
			
			setFirstRoll(false);
			previousScore = score;
		}
    }
    
    private boolean allDiceAreUsed(List<Integer> dice) {
    	Map<Integer, Integer> diceMap = aggregateDice(dice);
    	
    	if(diceMap.size() != 6) { // unless we have a ladder
    		for(Entry<Integer, Integer> entry : diceMap.entrySet()) {
        		if(( entry.getKey() != 1 && entry.getKey() != 5) // if side is 2,3,4 or 6
    				 && entry.getValue() % 3 != 0 ) { // and the number of dice is not a multiple of 3
        			return false;
        		}
        	}
    	}
    	
    	return true;
	}

	private void setScoreState(Score state) {
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
    
    private void checkMinScore() {
    	List<Integer> dice = new ArrayList<>();
    	
    	for(DieButton die : diceButtons) {
    		if (!die.isChecked()) {
    			int side = Integer.parseInt((String) die.getText());
    			dice.add(side);
    		}
    	}
    	
    	int score = calculateScore(dice) + roundScoreBonus;
    	
    	enableRollButton(score > Math.min(minScore-1, previousScore));
    }
    
    public void claimRound(View v) {
    	claimHelper(getRoundScore());
    	setRoundScore(0);
    	enableRollButton(true);
    }
    
    private void enableRollButton(boolean enable) {
    	Button button = (Button) findViewById(R.id.roll_button);
    	button.setEnabled(enable);
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
    
    
    public boolean isFirstRoll() {
		return firstRoll;
	}

	public void setFirstRoll(boolean firstRoll) {
		this.firstRoll = firstRoll;
		Button button = (Button) findViewById(R.id.claim_button);	
		button.setEnabled(!firstRoll);
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

	private int calculateScore(List<Integer> dice) {
		int score = 0;
		
		Map<Integer, Integer> diceMap = aggregateDice(dice);
		
		if (diceMap.size() == 6) { // 6 different sides means it's a ladder
			score = 1000;
		} else {
			// Calculate the score for each kind of side
			for (Entry<Integer, Integer> entry : diceMap.entrySet()){
				score += scoreHelper(entry.getKey(), entry.getValue());
			}			
		}
		
		return score;
	}
	
	private Map<Integer, Integer> aggregateDice(List<Integer> dice) {
		Map<Integer, Integer> diceMap = new HashMap<>(6);
		
		for(Integer side: dice) {
			Integer previousNumber = diceMap.get(side);
			int newNumber = (previousNumber == null) ? 1 : previousNumber + 1; 
			diceMap.put(side, newNumber);
		}
		
		return diceMap;
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
}
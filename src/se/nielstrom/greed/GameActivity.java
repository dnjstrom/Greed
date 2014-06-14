package se.nielstrom.greed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameActivity extends Activity {
	private DieButton[] diceButtons = new DieButton[6];
	private int round = 0;
	private int totalScore = 0;
	private int roundScore = 0;
	private boolean firstRoll = true;
	
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
    	
    	setRound(0);
    	setRoundScore(0);
    	setTotalScore(0);
    	setFirstRoll(true); 
    }
    
    public void rollDice(View v) {
    	int previousScore = 0;
    	
    	if (isFirstRoll()) {
    		for(DieButton die : diceButtons) {
        		die.setEnabled(true);
        	}
		} else {
			previousScore = getRoundScore();
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
    	
    	int score = calculateScore(dice);
    	
    	if (isFirstRoll() && score < 300) {
    		setRoundScore(score);
    		setScoreState(Score.LOW);
			claimHelper(0);
		} else if (score <= previousScore) {
			setRoundScore(0);
			setScoreState(Score.BUST);
			claimRound(null);;
		}else {		
			setRoundScore(score);
			setScoreState(Score.OK);
			setFirstRoll(false);
		}
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
    
    
    public void claimRound(View v) {
    	claimHelper(getRoundScore());
    }
    
    public void claimHelper(int score) {
    	setTotalScore(getTotalScore() + score);
    	setRound(getRound() + 1);
    	setFirstRoll(true);
    	
    	for(DieButton die : diceButtons) {
    		die.setChecked(true);
    		die.setEnabled(false);
    	}
    }
    
    
    public boolean isFirstRoll() {
		return firstRoll;
	}

	public void setFirstRoll(boolean firstRoll) {
		this.firstRoll = firstRoll;
		Button button = (Button) findViewById(R.id.claim_button);
		
		if (firstRoll) {	
			button.setEnabled(false);
		} else {
			button.setEnabled(true);
		}
		
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
		
		// Count the number of sides
		Map<Integer, Integer> diceMap = new HashMap<>(6);
		for(Integer side: dice) {
			Integer previousNumber = diceMap.get(side);
			int newNumber = (previousNumber == null) ? 1 : previousNumber + 1; 
			diceMap.put(side, newNumber);
		}
		
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
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
import android.widget.TextView;

public class GameActivity extends Activity {
	private DieButton[] diceButtons = new DieButton[6];
	private int round = 1;
	private int totalScore = 0;
	private int roundScore = 0;
	private boolean hasRolled = false;
			
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
    	
    	setRoundScore(0);
    	setTotalScore(0);
    }
    
    public void rollDice(View v) {
    	hasRolled = true;
    	
    	List<Integer> dice = new ArrayList<>(6);
    	
    	for(DieButton die : diceButtons) {
    		if (die.isChecked()) {
    			int side = (new Random()).nextInt(5) + 1;
				die.setText("" + side);
				dice.add(side);
			}
    	}
    	
    	int score = calculateScore(dice);
    	setRoundScore(score);
    }
    
    public void claimRound(View v) {
    	if(!hasRolled) {
    		return;
    	}
    	
    	setTotalScore(getTotalScore() + getRoundScore());
    	setRoundScore(0);
    	round++;
    	hasRolled = false;
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
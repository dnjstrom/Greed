package se.nielstrom.greed;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Displays the score and number of rounds in a super rad way.
 * 
 * @author Daniel Ström
 */
public class ScoreActivity extends Activity {
	public static final String SCORE = "se.nielstrom.greed.SCORE";
	public static final String ROUNDS = "se.nielstrom.greed.ROUNDS";
	
	private TextView score;
	private TextView rounds;
	
	@Override
    protected void onCreate(Bundle state) {
		super.onCreate(state);
        setContentView(R.layout.activity_score);
        
        score = (TextView) findViewById(R.id.score);
        rounds = (TextView) findViewById(R.id.rounds);
        
        Intent intent = getIntent();
        score.setText( intent.getIntExtra(SCORE, 0) + "");
        rounds.setText( intent.getIntExtra(ROUNDS, 0) + "");
	}
	
	@Override
	public void onBackPressed() {
		// Have the game activity start a new game
		setResult(RESULT_OK);
		finish();
	}
}
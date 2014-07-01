package se.nielstrom.greed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import se.nielstrom.greed.models.Die;
import se.nielstrom.greed.models.Greed;
import se.nielstrom.greed.views.DieButton;
import se.nielstrom.greed.views.DieButton.StateChangeListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class GameActivity extends Activity implements StateChangeListener {
	private DieButton[] dieButtons;
	private Button rollButton;
	private Button claimButton;
	private Greed game;
	private TextView totalPoints;
	private TextView roundPoints;
	private TextView rounds;

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_game);
		
		if (state == null) {
			game = new Greed();
		} else {
			game = state.getParcelable("game");
		}
		
		game.addPropertyChangeListener(new GameChangeListener());
		
		// Store a reference to all six custom die views
		dieButtons = new DieButton[6];
		dieButtons[0] = (DieButton) findViewById(R.id.die_a);
		dieButtons[1] = (DieButton) findViewById(R.id.die_b);
		dieButtons[2] = (DieButton) findViewById(R.id.die_c);
		dieButtons[3] = (DieButton) findViewById(R.id.die_d);
		dieButtons[4] = (DieButton) findViewById(R.id.die_e);
		dieButtons[5] = (DieButton) findViewById(R.id.die_f);
		
		Die[] dice = game.getDice();
		for(int i=0; i<dieButtons.length; i++) {
			dieButtons[i].setDie(dice[i]);
			dieButtons[i].addStateChangeListener(this);
		}
		
		// Action button references
		rollButton = (Button) findViewById(R.id.roll_button);
		rollButton.setEnabled( state != null ? state.getBoolean("roll") : true );
		claimButton = (Button) findViewById(R.id.claim_button);
		claimButton.setEnabled( state != null ? state.getBoolean("claim") : false );
		
		totalPoints = (TextView) findViewById(R.id.total_points);
		roundPoints = (TextView) findViewById(R.id.round_points);
		rounds = (TextView) findViewById(R.id.rounds_label);
		
		if (state != null) {
			updateGameState();
		} else {
			
			for(DieButton button : dieButtons) {
				button.setEnabled(false);
			}
		}
	}
	
	private void updateGameState() {
		setPoints(roundPoints, game.getScoreRound());
		setPoints(totalPoints, game.getScoreTotal());
		setRound(rounds, game.getRound());
	}
	
	private void setPoints(TextView text, int points) {
		text.setText(points + " " + getResources().getString(R.string.points));
	}
	
	private void setRound(TextView text, int rounds) {
		text.setText(rounds + " " + getResources().getString(R.string.rounds));
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putParcelable("game", game);
		bundle.putBoolean("claim", claimButton.isEnabled());
		bundle.putBoolean("roll", rollButton.isEnabled());
	}
	
	/**
	 * The callback run when the player clicks the roll button.
	 * 
	 * @param v	Not used, called by the system when specifying onClick
	 * 			callback in xml.
	 */
	public void rollDice(View v) {
		game.roll();
	}
	
	/**
	 * Called by the system when the user clicks the claim button.
	 * @param v Not used
	 */
	public void claimRound(View v) {
		game.claim();
	}

	@Override
	public void onStateChange(DieButton button) {
		rollButton.setEnabled(game.updateScore());
		roundPoints.setTextColor(getScoreColor(game.getScoreRound()));
	}
	
	private void setButtonsEnabled(boolean enabled) {
		for(DieButton button : dieButtons) {
			button.setEnabled(enabled);
		}
	}
	
	private int getScoreColor(int score) {
		int color_id;
		
		if (score == 0) {
			color_id = R.color.score_bust;
		} else if (score < Greed.MIN_SCORE) {
			color_id = R.color.score_low;
		} else {
			color_id = R.color.score_ok;
		}

		return getResources().getColor(color_id);

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Returning from the score activity, start a new game.
		game.resetGame();
	}
	
	private class GameChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			switch (event.getPropertyName()) {
			case Greed.ROUND_SCORE:
				setPoints(roundPoints, (int) event.getNewValue());
				break;
			case Greed.TOTAL_SCORE:
				setPoints(totalPoints, (int) event.getNewValue());
				break;
			case Greed.ROUNDS:
				setRound(rounds, (int) event.getNewValue());
				break;
			case Greed.STATE:
				switch ((Greed.State) event.getNewValue()) {
				case OK:			
					claimButton.setEnabled(true);
					rollButton.setEnabled(false);
					roundPoints.setTextColor(getResources().getColor(R.color.score_ok));
					setButtonsEnabled(true);
					break;
				case LOW:
					claimButton.setEnabled(false);
					rollButton.setEnabled(true);
					roundPoints.setTextColor(getResources().getColor(R.color.score_low));
					setButtonsEnabled(false);
					break;
				case BUST:
					claimButton.setEnabled(false);
					rollButton.setEnabled(true);
					roundPoints.setTextColor(getResources().getColor(R.color.score_bust));
					setButtonsEnabled(false);
					break;
				case WIN:
					// The game is over, start the score activity.
					Intent intent = new Intent(GameActivity.this, ScoreActivity.class);
					intent.putExtra(ScoreActivity.SCORE, game.getScoreTotal());
					intent.putExtra(ScoreActivity.ROUNDS, game.getRound());
					// I use the "startActivityForResult" method so I can reset the
					// game on returning to the game activity instead of doing it right
					// away. Makes for a smoother transition between the two activities.
					startActivityForResult(intent, 0);
					break;
				}
				break;
			}
		}
		
	}
}

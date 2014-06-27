package se.nielstrom.greed;

import se.nielstrom.greed.views.DieButton;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class GreedActivity extends Activity {

	private DieButton[] dieButtons;
	private Button rollButton;
	private Button claimButton;

	@Override
	protected void onCreate(Bundle state) {
		super.onCreate(state);
		setContentView(R.layout.activity_game);
		
		// Store a reference to all six custom die views
		dieButtons[0] = (DieButton) findViewById(R.id.die_a);
		dieButtons[1] = (DieButton) findViewById(R.id.die_b);
		dieButtons[2] = (DieButton) findViewById(R.id.die_c);
		dieButtons[3] = (DieButton) findViewById(R.id.die_d);
		dieButtons[4] = (DieButton) findViewById(R.id.die_e);
		dieButtons[5] = (DieButton) findViewById(R.id.die_f);
		
		// Action button references
		rollButton = (Button) findViewById(R.id.roll_button);
		claimButton = (Button) findViewById(R.id.claim_button);
	}
}

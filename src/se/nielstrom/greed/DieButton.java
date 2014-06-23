package se.nielstrom.greed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * A custom view basically existing as a cross between a ToggleButton and an
 * ImageButton. Tracks the state of a single 6-sided die and displays an
 * appropriate icon.
 * 
 * Besides the normal button states, a DieButton can be checked or unchecked.
 * These states are implemented using attr.xml and onCreateDrawableState in
 * order to allow for simple styling using xml drawables and solid color
 * backgrounds.
 * 
 * @author Daniel Ström
 *
 */
public class DieButton extends ImageButton implements OnClickListener {
	private static final int[] STATE_CHECKED = {R.attr.state_checked};
	public final int nrOfSides = 6;
	
	private boolean checked;
	private List<StateChangeListener> listeners;
	private int currentSide = nrOfSides;
	
	private Drawable[] drawables = {
		getResources().getDrawable(R.drawable.die1),
		getResources().getDrawable(R.drawable.die2),
		getResources().getDrawable(R.drawable.die3),
		getResources().getDrawable(R.drawable.die4),
		getResources().getDrawable(R.drawable.die5),
		getResources().getDrawable(R.drawable.die6)
	};

	public DieButton(Context context) {
		super(context);
		init(context);
	}
	
	public DieButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		handleAttributes(context, attrs);
	}

	public DieButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);		
		init(context);
		handleAttributes(context, attrs);
	}
	

	/**
	 * The common constructor operations.
	 */
	private void init(Context ctx) {
		listeners = new ArrayList<>();
		setCurrentSide(currentSide);
		setOnClickListener(this);
	}
	
	/**
	 * Allows the checked state of the view to be set from xml.
	 */
	private void handleAttributes(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.die_button, 0, 0);

		try {
			setChecked(a.getBoolean(R.styleable.die_button_state_checked, false));
		} finally {
			a.recycle();
		}
	}

	/**
	 * Randomly assigns a value to the die.
	 * @return The new value
	 */
	public int roll() {
		setCurrentSide( (new Random()).nextInt(nrOfSides - 1) + 1 );
		return currentSide;
	}
	
	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putBoolean("isEnabled", isEnabled());
		bundle.putBoolean("isChecked", isChecked());
		bundle.putInt("side", getCurrentSide());
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			setCurrentSide(bundle.getInt("side"));
			setChecked(bundle.getBoolean("isChecked"));
			setEnabled(bundle.getBoolean("isEnabled"));
			state = bundle.getParcelable("instanceState");
		}

		super.onRestoreInstanceState(state);
	}
	
	/**
	 * This methods allow me to add the checked state to the drawable state,
	 * allowing the view to be styled using xml drawables.
	 */
	@Override
	public int[] onCreateDrawableState(int extraSpace) {
	    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
	    if (isChecked()) {
	        mergeDrawableStates(drawableState, STATE_CHECKED);
	    }
	    return drawableState;
	}
	
	public void addStateChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}

	public void removeStateChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	@Override
	public void onClick(View v) {
		setChecked(!isChecked());
	}
	
	public boolean isChecked() {
		return checked;
	}

	public int getCurrentSide() {
		return currentSide;
	}

	public void setCurrentSide(int currentSide) {
		this.currentSide = currentSide;
		setImageDrawable(drawables[currentSide-1]);
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
		
		refreshDrawableState(); // Invalidates the drawable state
		
		// Notify listeners of state change
		for(StateChangeListener listener : listeners) {
			listener.onStateChange(this);
		}
	}
	
	/**
	 * Listener interface for entities that want to be notified when a button's
	 * checked state is changed.
	 */
	public interface StateChangeListener {
		public void onStateChange(DieButton button);
	}
}

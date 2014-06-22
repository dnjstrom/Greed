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
	}

	public DieButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray a = context.getTheme().obtainStyledAttributes(
		        attrs,
		        R.styleable.die_button,
		        0, 0);
		
		try {
			a.getBoolean(R.styleable.die_button_state_checked, false);
		} finally {
			a.recycle();
		}
		
		init(context);
	}
	
	private void init(Context ctx) {
		listeners = new ArrayList<>();
		setCurrentSide(currentSide);
		setOnClickListener(this);
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

	public int roll() {
		setCurrentSide( (new Random()).nextInt(nrOfSides - 1) + 1 );
		return currentSide;
	}
	
	public void setChecked(boolean checked) {
		this.checked = checked;
		
		refreshDrawableState();
		
		// Notify listeners
		for(StateChangeListener listener : listeners) {
			listener.onStateChange(this);
		}
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
	
	@Override
	public void setEnabled(boolean bool) {
		super.setEnabled(bool);
	}
	
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
	
	public interface StateChangeListener {
		public void onStateChange(DieButton button);
	}
}

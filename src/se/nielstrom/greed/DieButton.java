package se.nielstrom.greed;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DieButton extends Button implements OnClickListener {
	private boolean checked;
	private List<StateChangeListener> listeners;

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
		init(context);
	}
	
	private void init(Context ctx) {
		listeners = new ArrayList<>();
		setOnClickListener(this);
		setChecked(true);
		setTextColor(Color.BLACK);
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		
		if (isChecked()) {
			setBackgroundColor(getResources().getColor(R.color.action));
		} else {
			setTextColor(Color.DKGRAY);
			setBackgroundColor(getResources().getColor(android.R.color.background_dark));
		}
		
		// Notify listeners
		for(StateChangeListener listener : listeners) {
			listener.onStateChange(this);
		}
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

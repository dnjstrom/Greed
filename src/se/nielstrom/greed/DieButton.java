package se.nielstrom.greed;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DieButton extends Button implements OnClickListener {
	private boolean checked;

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
			setBackgroundColor(getResources().getColor(android.R.color.background_dark));
		}
	}

	@Override
	public void onClick(View v) {
		setChecked(!isChecked());
	}

}

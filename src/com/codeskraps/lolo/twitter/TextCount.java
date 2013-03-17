package com.codeskraps.lolo.twitter;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class TextCount implements TextWatcher {

	private boolean mEditing;
	private TextView txtCount = null;

	public TextCount(TextView txtCount) {
		this.txtCount = txtCount;
	}

	public boolean ismEditing() {
		return mEditing;
	}

	public void setmEditing(boolean mEditing) {
		this.mEditing = mEditing;
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (!ismEditing()) {
			setmEditing(true);
			txtCount.setText(String.valueOf(s.length()));
			setmEditing(false);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

}

package gov.cdc.epiinfo.etc;

import android.content.Context;
import android.widget.ImageButton;
import android.widget.TextView;

public class DateButton extends ImageButton {

	private TextView textField;
	
	public DateButton(Context context) {
		super(context);
		
	}
	
	public void setTextField(TextView textField)
	{
		this.textField = textField;
	}
	
	public TextView getTextField()
	{
		return this.textField;
	}

}

package gov.cdc.epiinfo.analysis;

import gov.cdc.epiinfo.EpiDbHelper;
import gov.cdc.epiinfo.Field;
import gov.cdc.epiinfo.FormMetadata;
import gov.cdc.epiinfo.R;

import java.util.LinkedList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


public class FrequencyView extends RelativeLayout {

	private Context context;
	private EpiDbHelper mDbHelper;
	private LinearLayout outputLayout;
	private FormMetadata formMetadata;

	public FrequencyView(Context context, FormMetadata formMetadata, EpiDbHelper mDbHelper) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.analysis_frequency, this, true);

		this.context = context;
		this.mDbHelper = mDbHelper;
		this.formMetadata = formMetadata;
		SetupFrequencyGadget();
	}

	private void SetupFrequencyGadget()
	{	
		final FrequencyView self = this;
		ImageView closeImage = findViewById(R.id.btnClose);
		closeImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((ViewManager)self.getParent()).removeView(self);
			}
		});

		Spinner freqSpinner = this.findViewById(R.id.cbxFreqVar);
		freqSpinner.setPrompt("Please select a field");

		String[] stringValues = new String[formMetadata.DataFields.size() + 1];
		stringValues[0] = context.getString(R.string.analysis_select);
		for (int x=1;x<=formMetadata.DataFields.size();x++)
		{
			stringValues[x] = formMetadata.DataFields.get(x-1).getName();
		}

		outputLayout = this.findViewById(R.id.freqOutput);

		ArrayAdapter<CharSequence> latAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, stringValues);
		latAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		freqSpinner.setAdapter(latAdapter);
		freqSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
			{         
				try
				{
					outputLayout.removeAllViews();

					if (pos > 0)
					{
						String fieldName = parent.getItemAtPosition(pos).toString();

						Field f = formMetadata.GetFieldByName(fieldName);

						boolean fieldYN = false;
						if (f.getType().equals("11"))
						{
							fieldYN = true;
						}

						boolean fieldChk = false;
						if (f.getType().equals("10"))
						{
							fieldChk = true;
						}

						Cursor c;
						if (fieldChk)
						{
							c = mDbHelper.getFrequency(fieldName, true);
						}
						else
						{
							c = mDbHelper.getFrequency(fieldName, false);
						}
						if (c.moveToFirst())
						{
							LinearLayout header = new LinearLayout(context);
							RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
							header.setBackgroundColor(Color.parseColor("#42638c"));
							header.setLayoutParams(params);
							header.setOrientation(LinearLayout.HORIZONTAL);
							header.setWeightSum(1f);

							LinearLayout.LayoutParams cellParams = new LinearLayout.LayoutParams(1, LayoutParams.WRAP_CONTENT);
							cellParams.weight = 0.5f;

							TextView txtField = new TextView(context);
							txtField.setText(fieldName);
							txtField.setLayoutParams(cellParams);
							txtField.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
							txtField.setTextColor(Color.WHITE);
							txtField.setGravity(Gravity.CENTER);
							txtField.setTypeface(null, Typeface.BOLD);
							header.addView(txtField);

							TextView txtFreq = new TextView(context);
							txtFreq.setText(context.getText(R.string.analysis_freq) + "    ");
							txtFreq.setLayoutParams(cellParams);
							txtFreq.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
							txtFreq.setTextColor(Color.WHITE);
							txtFreq.setGravity(Gravity.RIGHT);
							txtFreq.setTypeface(null, Typeface.BOLD);
							header.addView(txtFreq);

							outputLayout.addView(header);

							do
							{
								String val = c.getString(c.getColumnIndexOrThrow(fieldName));
								if (fieldChk)
								{
									if (val.equals("0"))
									{
										val = "No";
									}
									else if (val.equals("1"))
									{
										val = "Yes";
									}
								}
								if (fieldYN)
								{
									if (val.equals("0"))
									{
										val = "Missing";
									}
									else if (val.equals("1"))
									{
										val = "Yes";
									}
									else if (val.equals("2"))
									{
										val = "No";
									}
								}
								if (val == null || val.equals("Inf") || val.equals("")) 
								{
									val = "Missing";
								}

								LinkedList<String> listValues = f.getListValues();
								if (listValues != null)
								{
									if (f.getType().equals("12"))
									{
										try
										{
											int mod = Integer.parseInt(val) % 100;
											if (mod < 0)
											{
												val = "Missing";
											}
											else
											{
												val = listValues.get(mod);
											}
										}
										catch (Exception ex)
										{

										}
									}
									else
									{
										if (val != "Missing")
										{
											val = listValues.get(Integer.parseInt(val));
										}

									}
								}

								int count = c.getInt(c.getColumnIndexOrThrow("COUNT(*)"));

								LinearLayout row = new LinearLayout(context);
								row.setLayoutParams(params);
								row.setBackgroundColor(Color.WHITE);
								row.setOrientation(LinearLayout.HORIZONTAL);

								TextView txtValue = new TextView(context);
								txtValue.setText(val + "");
								txtValue.setLayoutParams(cellParams);
								txtValue.setTextSize(18);
								txtValue.setTextColor(Color.parseColor("#42638c"));
								txtValue.setGravity(Gravity.CENTER);
								row.addView(txtValue);

								TextView txtCount = new TextView(context);
								txtCount.setText(count + "         ");
								txtCount.setLayoutParams(cellParams);
								txtCount.setGravity(Gravity.RIGHT);
								txtCount.setTypeface(null, Typeface.BOLD);
								row.addView(txtCount);

								outputLayout.addView(row);


							}while(c.moveToNext());
						}
					}
				}
				catch (Exception ex)
				{
					int x = 5;
					x++;
				}
			}     

			public void onNothingSelected(AdapterView<?> parent) 
			{     

			}
		});
	}


}

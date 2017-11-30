package gov.cdc.epiinfo.analysis;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.PieRenderer.DonutMode;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;

import java.util.LinkedList;

import gov.cdc.epiinfo.EpiDbHelper;
import gov.cdc.epiinfo.Field;
import gov.cdc.epiinfo.FormMetadata;
import gov.cdc.epiinfo.R;


public class PieChartView extends RelativeLayout {

	private Context context;
	private EpiDbHelper mDbHelper;
	//private LinearLayout outputLayout;
	private FormMetadata formMetadata;

	public PieChartView(Context context, FormMetadata formMetadata, EpiDbHelper mDbHelper) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.analysis_chart_pie, this, true);

		this.context = context;
		this.mDbHelper = mDbHelper;
		this.formMetadata = formMetadata;
		SetupGadget();
	}

	private void SetupGadget()
	{	
		final PieChartView self = this;
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

		final PieChart pie = findViewById(R.id.plot);
		pie.getBackgroundPaint().setColor(Color.TRANSPARENT);
		pie.getBorderPaint().setColor(Color.TRANSPARENT);

		final TextView chartTitle = findViewById(R.id.chartTitle);

		ArrayAdapter<CharSequence> latAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, stringValues);
		latAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		freqSpinner.setAdapter(latAdapter);

		freqSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
			{         
				pie.clear();

				if (pos > 0)
				{
					String fieldName = parent.getItemAtPosition(pos).toString();

					Field f = formMetadata.GetFieldByName(fieldName);
					String prompt = f.getPrompt();
					if (prompt != null)
					{
						chartTitle.setText(prompt);
					}

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
						int[] colors = {0xFF00AAF8, 0xFF006CF8, 0xFF635EFF, 0xFFBD0CF9, 0xFFF73BD7, 0xFFF30000, 0xFFFF7700, 0xFFFFCB00, 0xFFFFEE00, 0xFFB0F007, 0xFF23CC06, 0xFF1CF1CE};
						int counter = 0;
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
							if (val == null || val.toLowerCase().equals("inf") || val.equals("")) 
							{
								val = "Missing";
							}

							LinkedList<String> listValues = f.getListValues();
							if (listValues != null)
							{
								if (f.getType().equals("12"))
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
								else
								{
									if (val != "Missing")
									{
										val = listValues.get(Integer.parseInt(val));
									}
								}
							}

							int count = c.getInt(c.getColumnIndexOrThrow("COUNT(*)"));
							pie.addSeries(new Segment(val + " (" + count + ")",count), new SegmentFormatter(colors[counter % 12], colors[counter % 12] - 0x33000000));
							counter++;
						}while(c.moveToNext());
						try
						{
							pie.getRenderer(PieRenderer.class).setDonutSize(0, DonutMode.PERCENT);
							pie.redraw();
						}
						catch (Exception ex)
						{

						}
					}
				}
			}     

			public void onNothingSelected(AdapterView<?> parent) 
			{     

			}
		});
	}


}

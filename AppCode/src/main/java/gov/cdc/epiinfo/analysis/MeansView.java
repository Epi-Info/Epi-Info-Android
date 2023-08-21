package gov.cdc.epiinfo.analysis;

import gov.cdc.epiinfo.EpiDbHelper;
import gov.cdc.epiinfo.FormMetadata;
import gov.cdc.epiinfo.R;

import java.text.DecimalFormat;
import java.util.Arrays;

import android.content.Context;
import android.database.Cursor;
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


public class MeansView extends RelativeLayout {

	private Context context;
	private EpiDbHelper mDbHelper;
	private LinearLayout outputLayout;
	private FormMetadata formMetadata;
	
	public MeansView(Context context, FormMetadata formMetadata, EpiDbHelper mDbHelper) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    inflater.inflate(R.layout.analysis_means, this, true);
		
		this.context = context;
		this.mDbHelper = mDbHelper;
		this.formMetadata = formMetadata;
		SetupMeansGadget();
	}

	private void SetupMeansGadget()
	{		
		final MeansView self = this;
		ImageView closeImage = findViewById(R.id.btnClose);
		closeImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((ViewManager)self.getParent()).removeView(self);
			}
		});
		Spinner meansSpinner = findViewById(R.id.meansField);
		meansSpinner.setPrompt("Please select a field");
    	
    	String[] stringValues = new String[formMetadata.NumericFields.size() + 1];
    	stringValues[0] = context.getString(R.string.analysis_select);
    	for (int x=1;x<=formMetadata.NumericFields.size();x++)
    	{
    		stringValues[x] = formMetadata.NumericFields.get(x-1).getName();
    	}
    	
    	ArrayAdapter<CharSequence> meansAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, stringValues);
    	meansAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        meansSpinner.setAdapter(meansAdapter);
		
        meansSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
        	{         
        		
        		
        		if (pos > 0)
        		{
        			String fieldName = parent.getItemAtPosition(pos).toString();
        			Cursor c = mDbHelper.getNumericValues(fieldName);
        			double[] vals = new double[c.getCount()];
        			int counter = 0;
        			if (c.moveToFirst())
        			{
        				do
        				{
        					vals[counter] = c.getDouble(c.getColumnIndexOrThrow(fieldName));
        					counter++;
        				}while(c.moveToNext());
        			}
        			
        			TextView lblMean = findViewById(R.id.lblMean);
        			TextView lblStdDev = findViewById(R.id.lblStdDev);
        			TextView lblVariance = findViewById(R.id.lblVariance);
        			TextView lblMin = findViewById(R.id.lblMin);
        			TextView lblMax = findViewById(R.id.lblMax);
        			TextView lblMedian = findViewById(R.id.lblMedian);
        			TextView lblObs = findViewById(R.id.lblObs);
        			TextView lblTotal = findViewById(R.id.lblTotal);

        			DecimalFormat formatter = new DecimalFormat("#.####");
        			lblMean.setText(formatter.format(mean(vals)));
        			lblVariance.setText(formatter.format(var(vals)));
        			lblStdDev.setText(formatter.format(stddev(vals)));
        			lblMin.setText(formatter.format(min(vals)));
        			lblMax.setText(formatter.format(max(vals)));
        			lblMedian.setText(formatter.format(median(vals)));
        			lblObs.setText(vals.length + "");
        			lblTotal.setText(formatter.format(sum(vals)));
        		}
        	}     
        	
        	public void onNothingSelected(AdapterView<?> parent) 
        	{     
        		
        	}
		});
	}
	
	private double max(double[] a) {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > max) max = a[i];
        }
        return max;
    }

    private double min(double[] a) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < a.length; i++) {
            if (a[i] < min) min = a[i];
        }
        return min;
    }
    
    private double median(double[] values)
    {
    	try
    	{
    	Arrays.sort(values);
     
        if (values.length % 2 == 1)
        {
        	return values[(values.length+1)/2-1];
        }
        else
        {
        	double lower = values[values.length/2-1];
        	double upper = values[values.length/2];
     
        	return (lower + upper) / 2.0;
        }
    	}
    	catch (Exception ex)
    	{
    		return Double.NaN;
    	}
    }
    
    private double sum(double[] a) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i];
        }
        return sum;
    }
	
	private double stddev(double[] a) {
        return Math.sqrt(var(a));
    }
	
	private double var(double[] a) {
        if (a.length == 0) return Double.NaN;
        double avg = mean(a);
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += (a[i] - avg) * (a[i] - avg);
        }
        return sum / (a.length - 1);
    }
	
	private double mean(double[] a) {
        if (a.length == 0) return Double.NaN;
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum = sum + a[i];
        }
        return sum / a.length;
    }

	
}

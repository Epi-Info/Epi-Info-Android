package gov.cdc.epiinfo.statcalc;

import gov.cdc.epiinfo.DeviceManager;
import gov.cdc.epiinfo.R;
import gov.cdc.epiinfo.statcalc.calculators.ChiSquare;
import gov.cdc.epiinfo.statcalc.etc.ChiSquareResult;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;


public class ChiSquareActivity extends Activity {
	
	public class LocalTextWatcher implements TextWatcher
	{

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			Calculate();
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private TextView lblOr1;
	private TextView lblOr2;
	private TextView lblOr3;
	private TextView lblOr4;
	private TextView lblOr5;
	private TextView lblOr6;
	private TextView lblOr7;
	private TextView lblOr8;
	private TextView lblOr9;
	private TextView lblOr10;
	private TextView lblChi;
	private TextView lblPValue;
	private EditText txtA1;
	private EditText txtA2;
	private EditText txtA3;
	private EditText txtA4;
	private EditText txtA5;
	private EditText txtA6;
	private EditText txtA7;
	private EditText txtA8;
	private EditText txtA9;
	private EditText txtA10;
	private EditText txtB1;
	private EditText txtB2;
	private EditText txtB3;
	private EditText txtB4;
	private EditText txtB5;
	private EditText txtB6;
	private EditText txtB7;
	private EditText txtB8;
	private EditText txtB9;
	private EditText txtB10;
	private EditText txtC1;
	private EditText txtC2;
	private EditText txtC3;
	private EditText txtC4;
	private EditText txtC5;
	private EditText txtC6;
	private EditText txtC7;
	private EditText txtC8;
	private EditText txtC9;
	private EditText txtC10;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this, false);
		this.setTheme(R.style.AppThemeNoBar);
        
        setContentView(R.layout.statcalc_chi_square);
        
        lblChi = findViewById(R.id.lblChi);
        lblOr1 = findViewById(R.id.lblOr1);
        lblOr2 = findViewById(R.id.lblOr2);
        lblOr3 = findViewById(R.id.lblOr3);
        lblOr4 = findViewById(R.id.lblOr4);
        lblOr5 = findViewById(R.id.lblOr5);
        lblOr6 = findViewById(R.id.lblOr6);
        lblOr7 = findViewById(R.id.lblOr7);
        lblOr8 = findViewById(R.id.lblOr8);
        lblOr9 = findViewById(R.id.lblOr9);
        lblOr10 = findViewById(R.id.lblOr10);
        lblPValue = findViewById(R.id.lblPValue);
        txtA1 = findViewById(R.id.txtA1);
        txtA2 = findViewById(R.id.txtA2);
        txtA3 = findViewById(R.id.txtA3);
        txtA4 = findViewById(R.id.txtA4);
        txtA5 = findViewById(R.id.txtA5);
        txtA6 = findViewById(R.id.txtA6);
        txtA7 = findViewById(R.id.txtA7);
        txtA8 = findViewById(R.id.txtA8);
        txtA9 = findViewById(R.id.txtA9);
        txtA10 = findViewById(R.id.txtA10);
        txtB1 = findViewById(R.id.txtB1);
        txtB2 = findViewById(R.id.txtB2);
        txtB3 = findViewById(R.id.txtB3);
        txtB4 = findViewById(R.id.txtB4);
        txtB5 = findViewById(R.id.txtB5);
        txtB6 = findViewById(R.id.txtB6);
        txtB7 = findViewById(R.id.txtB7);
        txtB8 = findViewById(R.id.txtB8);
        txtB9 = findViewById(R.id.txtB9);
        txtB10 = findViewById(R.id.txtB10);
        txtC1 = findViewById(R.id.txtC1);
        txtC2 = findViewById(R.id.txtC2);
        txtC3 = findViewById(R.id.txtC3);
        txtC4 = findViewById(R.id.txtC4);
        txtC5 = findViewById(R.id.txtC5);
        txtC6 = findViewById(R.id.txtC6);
        txtC7 = findViewById(R.id.txtC7);
        txtC8 = findViewById(R.id.txtC8);
        txtC9 = findViewById(R.id.txtC9);
        txtC10 = findViewById(R.id.txtC10);
        
        
txtA1.addTextChangedListener(new LocalTextWatcher());
txtA2.addTextChangedListener(new LocalTextWatcher());
txtA3.addTextChangedListener(new LocalTextWatcher());
txtA4.addTextChangedListener(new LocalTextWatcher());
txtA5.addTextChangedListener(new LocalTextWatcher());
txtA6.addTextChangedListener(new LocalTextWatcher());
txtA7.addTextChangedListener(new LocalTextWatcher());
txtA8.addTextChangedListener(new LocalTextWatcher());
txtA9.addTextChangedListener(new LocalTextWatcher());
txtA10.addTextChangedListener(new LocalTextWatcher());

txtB1.addTextChangedListener(new LocalTextWatcher());
txtB2.addTextChangedListener(new LocalTextWatcher());
txtB3.addTextChangedListener(new LocalTextWatcher());
txtB4.addTextChangedListener(new LocalTextWatcher());
txtB5.addTextChangedListener(new LocalTextWatcher());
txtB6.addTextChangedListener(new LocalTextWatcher());
txtB7.addTextChangedListener(new LocalTextWatcher());
txtB8.addTextChangedListener(new LocalTextWatcher());
txtB9.addTextChangedListener(new LocalTextWatcher());
txtB10.addTextChangedListener(new LocalTextWatcher());

txtC1.addTextChangedListener(new LocalTextWatcher());
txtC2.addTextChangedListener(new LocalTextWatcher());
txtC3.addTextChangedListener(new LocalTextWatcher());
txtC4.addTextChangedListener(new LocalTextWatcher());
txtC5.addTextChangedListener(new LocalTextWatcher());
txtC6.addTextChangedListener(new LocalTextWatcher());
txtC7.addTextChangedListener(new LocalTextWatcher());
txtC8.addTextChangedListener(new LocalTextWatcher());
txtC9.addTextChangedListener(new LocalTextWatcher());
txtC10.addTextChangedListener(new LocalTextWatcher());
        
    }
    
    private void Calculate()
    {
    	double[] a = new double[10];
    	double[] b = new double[10];
    	double[] c = new double[10];
    	
    	if (txtA1.getText().toString().length()>0)
    		a[0] = Double.parseDouble(txtA1.getText().toString());
    	if (txtA2.getText().toString().length()>0)
    		a[1] = Double.parseDouble(txtA2.getText().toString());
    	if (txtA3.getText().toString().length()>0)
    		a[2] = Double.parseDouble(txtA3.getText().toString());
    	if (txtA4.getText().toString().length()>0)
    		a[3] = Double.parseDouble(txtA4.getText().toString());
    	if (txtA5.getText().toString().length()>0)
    		a[4] = Double.parseDouble(txtA5.getText().toString());
    	if (txtA6.getText().toString().length()>0)
    		a[5] = Double.parseDouble(txtA6.getText().toString());
    	if (txtA7.getText().toString().length()>0)
    		a[6] = Double.parseDouble(txtA7.getText().toString());
    	if (txtA8.getText().toString().length()>0)
    		a[7] = Double.parseDouble(txtA8.getText().toString());
    	if (txtA9.getText().toString().length()>0)
    		a[8] = Double.parseDouble(txtA9.getText().toString());
    	if (txtA10.getText().toString().length()>0)
    		a[9] = Double.parseDouble(txtA10.getText().toString());
    	
    	if (txtB1.getText().toString().length()>0)
    		b[0] = Double.parseDouble(txtB1.getText().toString());
    	if (txtB2.getText().toString().length()>0)
    		b[1] = Double.parseDouble(txtB2.getText().toString());
    	if (txtB3.getText().toString().length()>0)
    		b[2] = Double.parseDouble(txtB3.getText().toString());
    	if (txtB4.getText().toString().length()>0)
    		b[3] = Double.parseDouble(txtB4.getText().toString());
    	if (txtB5.getText().toString().length()>0)
    		b[4] = Double.parseDouble(txtB5.getText().toString());
    	if (txtB6.getText().toString().length()>0)
    		b[5] = Double.parseDouble(txtB6.getText().toString());
    	if (txtB7.getText().toString().length()>0)
    		b[6] = Double.parseDouble(txtB7.getText().toString());
    	if (txtB8.getText().toString().length()>0)
    		b[7] = Double.parseDouble(txtB8.getText().toString());
    	if (txtB9.getText().toString().length()>0)
    		b[8] = Double.parseDouble(txtB9.getText().toString());
    	if (txtB10.getText().toString().length()>0)
    		b[9] = Double.parseDouble(txtB10.getText().toString());
    	
    	if (txtC1.getText().toString().length()>0)
    		c[0] = Double.parseDouble(txtC1.getText().toString());
    	if (txtC2.getText().toString().length()>0)
    		c[1] = Double.parseDouble(txtC2.getText().toString());
    	if (txtC3.getText().toString().length()>0)
    		c[2] = Double.parseDouble(txtC3.getText().toString());
    	if (txtC4.getText().toString().length()>0)
    		c[3] = Double.parseDouble(txtC4.getText().toString());
    	if (txtC5.getText().toString().length()>0)
    		c[4] = Double.parseDouble(txtC5.getText().toString());
    	if (txtC6.getText().toString().length()>0)
    		c[5] = Double.parseDouble(txtC6.getText().toString());
    	if (txtC7.getText().toString().length()>0)
    		c[6] = Double.parseDouble(txtC7.getText().toString());
    	if (txtC8.getText().toString().length()>0)
    		c[7] = Double.parseDouble(txtC8.getText().toString());
    	if (txtC9.getText().toString().length()>0)
    		c[8] = Double.parseDouble(txtC9.getText().toString());
    	if (txtC10.getText().toString().length()>0)
    		c[9] = Double.parseDouble(txtC10.getText().toString());
    	
    	if ((txtA1.getText().toString().length()>0)
    			&& (txtA2.getText().toString().length()>0)
    			&& (txtB1.getText().toString().length()>0)
    			&& (txtB2.getText().toString().length()>0)
    			&& (txtC1.getText().toString().length()>0)
    			&& (txtC2.getText().toString().length()>0))
    	{
    	
    	ChiSquare calc = new ChiSquare();
    	ChiSquareResult results = calc.GetChiSquareForTrend(a, b, c);
    	lblChi.setText(new DecimalFormat("#.###").format(results.GetChi()));
    	lblPValue.setText(new DecimalFormat("#.#######").format(results.GetPValue()));
    	lblOr1.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[0]));
    	lblOr2.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[1]));
    	lblOr3.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[2]));
    	lblOr4.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[3]));
    	lblOr5.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[4]));
    	lblOr6.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[5]));
    	lblOr7.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[6]));
    	lblOr8.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[7]));
    	lblOr9.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[8]));
    	lblOr10.setText(new DecimalFormat("#.###").format(results.GetOddsRatios()[9]));
    	}
    }
}
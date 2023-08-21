package gov.cdc.epiinfo.statcalc;

import gov.cdc.epiinfo.DeviceManager;
import gov.cdc.epiinfo.R;
import gov.cdc.epiinfo.statcalc.calculators.MatchedPair;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


public class MatchedPairActivity extends Activity {
	private EditText txtA;
	private EditText txtX;
	private EditText txtY;
	private EditText txtD;
	
	private TextView lblsH1;
	private TextView lblsH2;
	private TextView lblsV1;
	private TextView lblsV2;
	private TextView lblsTotal;
    
	private TextView lblOdds;
	private TextView lblOddsLower;
	private TextView lblOddsUpper;
	private TextView lblFisherLower;
	private TextView lblFisherUpper;

	private TextView lblChiSqUnc;
	private TextView lblPUnc;
	private TextView lblChiSqCor;
	private TextView lblPCor;
	private TextView lblFisher1T;
	private TextView lblFisher2T;
	
	private TextView disclaimer1;
	private TextView disclaimer2;

	InputMethodManager imm;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this, false);
		this.setTheme(R.style.AppThemeNoBar);
        
        setContentView(R.layout.statcalc_matched_pair);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        txtA = findViewById(R.id.txtA);
        txtX = findViewById(R.id.txtX);
        txtY = findViewById(R.id.txtY);
        txtD = findViewById(R.id.txtD);

        lblsH1 = findViewById(R.id.lblH1);
        lblsH2 = findViewById(R.id.lblH2);
        lblsV1 = findViewById(R.id.lblV1);
        lblsV2 = findViewById(R.id.lblV2);
        lblsTotal = findViewById(R.id.lblTotal);
        
        lblOdds = findViewById(R.id.lblOdds);
        lblOddsLower = findViewById(R.id.lblOddsLower);
        lblOddsUpper = findViewById(R.id.lblOddsUpper);
        lblFisherLower = findViewById(R.id.lblFisherLower);
        lblFisherUpper = findViewById(R.id.lblFisherUpper);
        lblChiSqUnc = findViewById(R.id.lblChiSqUnc);
        lblPUnc = findViewById(R.id.lblPUnc);
        lblChiSqCor = findViewById(R.id.lblChiSqCor);
        lblPCor = findViewById(R.id.lblPCor);
        lblFisher1T = findViewById(R.id.lblFisher1T);
        lblFisher2T = findViewById(R.id.lblFisher2T);

        disclaimer1 = findViewById(R.id.disclaimer1);
        disclaimer2 = findViewById(R.id.disclaimer2);
        
        txtA.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				Calculate();
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
			
		});

        txtX.addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate();
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        txtY.addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate();
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}

        });

        txtD.addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate();
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });
    }
    
    private void Calculate()
    {
		lblOdds.setText("...");
		lblOddsLower.setText("...");
		lblOddsUpper.setText("...");
		lblFisherLower.setText("...");
		lblFisherUpper.setText("...");
        lblChiSqUnc.setText("...");
        lblPUnc.setText("...");
        lblChiSqCor.setText("...");
        lblPCor.setText("...");
        lblFisher1T.setText("...");
        lblFisher2T.setText("...");
        disclaimer1.setText("");
        disclaimer2.setText("");

		int a = 0;
    	int x = 0;
    	int y = 0;
    	int d = 0;
    	
    	if (txtA.getText().toString().length()>0)
    		a = Integer.parseInt(txtA.getText().toString());
    	if (txtX.getText().toString().length()>0)
    		x = Integer.parseInt(txtX.getText().toString());
    	if (txtY.getText().toString().length()>0)
    		y = Integer.parseInt(txtY.getText().toString());
    	if (txtD.getText().toString().length()>0)
    		d = Integer.parseInt(txtD.getText().toString());
    	
    	int h1 = a + x;
    	int h2 = y + d;
    	int v1 = a + y;
    	int v2 = x + d;
    	int total = v1 + v2;
    	
    	lblsH1.setText(h1 + "");
    	lblsH2.setText(h2 + "");
    	lblsV1.setText(v1 + "");
    	lblsV2.setText(v2 + "");
    	lblsTotal.setText(total + "");
    	
    	if ((txtX.getText().toString().length()>0)
    			&& (txtY.getText().toString().length()>0))
    	{
    		MatchedPair calc = new MatchedPair();
    		
    		DecimalFormat formatter = new DecimalFormat("#.####");
    		DecimalFormat formatter2 = new DecimalFormat("#.########");
    		
    		double[] or = calc.oddsRatio(x, y);
    		lblOdds.setText(formatter.format(or[0]));
    		lblOddsLower.setText(formatter.format(or[1]));
    		lblOddsUpper.setText(formatter.format(or[2]));
    		
    		double[] fisherLimits = calc.fisherLimits(x, y);
    		lblFisherLower.setText(formatter.format(fisherLimits[0]));
    		lblFisherUpper.setText(formatter.format(fisherLimits[1]));
    		
    		double[] mcNemarUncorrected = calc.mcNemarUncorrected(x, y);
            lblChiSqUnc.setText(formatter.format(mcNemarUncorrected[0]));
            lblPUnc.setText(formatter.format(mcNemarUncorrected[1]));

            double[] mcNemarCorrected = calc.mcNemarCorrected(x, y);
            lblChiSqCor.setText(formatter.format(mcNemarCorrected[0]));
            lblPCor.setText(formatter.format(mcNemarCorrected[1]));
            
            double[] fisherExactTests = calc.fisherExactTests(x, y);
            lblFisher1T.setText(formatter2.format(fisherExactTests[0]));
            lblFisher2T.setText(formatter2.format(fisherExactTests[1]));
            
            String txtDisclaimer1 = calc.disclaimer(x, y);
            disclaimer1.setText(txtDisclaimer1);
            
            String txtDisclaimer2 = calc.adjustmentDisclaimer(x, y);
            disclaimer2.setText(txtDisclaimer2);
    	}
    }
}

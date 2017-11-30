package gov.cdc.epiinfo.statcalc;

import gov.cdc.epiinfo.DeviceManager;
import gov.cdc.epiinfo.R;
import gov.cdc.epiinfo.analysis.ExactOR;
import gov.cdc.epiinfo.analysis.OddsAndRisk;
import gov.cdc.epiinfo.statcalc.calculators.Strat2x2;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


public class TwoByTwoActivity extends Activity {
		
	private TextView[] lblsH1;
	private TextView[] lblsH2;
	private TextView[] lblsV1;
	private TextView[] lblsV2;
	private TextView[] lblsTotal;

	private EditText[] txtsYY;
	private EditText[] txtsYN;
	private EditText[] txtsNY;
	private EditText[] txtsNN;
	
	private TextView[] lblOdds;
	private TextView[] lblOddsLower;
	private TextView[] lblOddsUpper;
	private TextView[] lblMLE;
	private TextView[] lblMLELower;
	private TextView[] lblMLEUpper;
	private TextView[] lblFisherLower;
	private TextView[] lblFisherUpper;
	private TextView[] lblRisk;
	private TextView[] lblRRLower;
	private TextView[] lblRRUpper;
	private TextView[] lblRiskDiff;
	private TextView[] lblRDLower;
	private TextView[] lblRDUpper;
	private TextView[] lblChiSqUnc;
	private TextView[] lblPUnc;
	private TextView[] lblChiSqMH;
	private TextView[] lblPMH;
	private TextView[] lblChiSqCor;
	private TextView[] lblPCor;
	private TextView[] lblMidP1T;
	private TextView[] lblFisher1T;
	private TextView[] lblFisher2T;
	
	private Button btnSummary;
	
	private double[] yySet;
	private double[] ynSet;
	private double[] nySet;
	private double[] nnSet;
	
	private boolean[] strataCalculates;
	
	private TextView lblMHAdjusted;
	private TextView lblMHLower;
	private TextView lblMHUpper;
	private TextView lblExactAdjusted;
	private TextView lblExactLower;
	private TextView lblExactUpper;
	private TextView lblRiskAdjusted;
	private TextView lblRiskLower;
	private TextView lblRiskUpper;
	private TextView lblUncorrectedChi;
	private TextView lblUncorrectedP;
	private TextView lblCorrectedChi;
	private TextView lblCorrectedP;
	private ProgressBar summaryWaitCursor;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this, false);
		this.setTheme(R.style.AppThemeNoBar);
        
        setContentView(R.layout.statcalc_two_by_two);
        
        try
        {
        
        lblsH1 = new TextView[5];
        lblsH2 = new TextView[5];
        lblsV1 = new TextView[5];
        lblsV2 = new TextView[5];
        lblsTotal = new TextView[5];
        txtsYY = new EditText[5];
        txtsYN = new EditText[5];
        txtsNY = new EditText[5];
        txtsNN = new EditText[5];
        lblOdds = new TextView[5];
    	lblOddsLower = new TextView[5];
    	lblOddsUpper = new TextView[5];
    	lblMLE = new TextView[5];
    	lblMLELower = new TextView[5];
    	lblMLEUpper = new TextView[5];
    	lblFisherLower = new TextView[5];
    	lblFisherUpper = new TextView[5];
    	lblRisk = new TextView[5];
    	lblRRLower = new TextView[5];
    	lblRRUpper = new TextView[5];
    	lblRiskDiff = new TextView[5];
    	lblRDLower = new TextView[5];
    	lblRDUpper = new TextView[5];
    	lblChiSqUnc = new TextView[5];
    	lblPUnc = new TextView[5];
    	lblChiSqMH = new TextView[5];
    	lblPMH = new TextView[5];
    	lblChiSqCor = new TextView[5];
    	lblPCor = new TextView[5];
    	lblMidP1T = new TextView[5];
    	lblFisher1T = new TextView[5];
    	lblFisher2T = new TextView[5];
    	strataCalculates = new boolean[5];
        
    	btnSummary = findViewById(R.id.btnSummary);
    	btnSummary.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CalculateSummary();				
			}
		});
        
        lblsH1[0] = findViewById(R.id.lblH1);
        lblsH2[0] = findViewById(R.id.lblH2);
        lblsV1[0] = findViewById(R.id.lblV1);
        lblsV2[0] = findViewById(R.id.lblV2);
        lblsTotal[0] = findViewById(R.id.lblTotal);
        
        lblOdds[0] = findViewById(R.id.lblOdds);
        lblOddsLower[0] = findViewById(R.id.lblOddsLower);
        lblOddsUpper[0] = findViewById(R.id.lblOddsUpper);
        lblMLE[0] = findViewById(R.id.lblMLE);
        lblMLELower[0] = findViewById(R.id.lblMLELower);
        lblMLEUpper[0] = findViewById(R.id.lblMLEUpper);
        lblFisherLower[0] = findViewById(R.id.lblFisherLower);
        lblFisherUpper[0] = findViewById(R.id.lblFisherUpper);
        lblRisk[0] = findViewById(R.id.lblRisk);
        lblRRLower[0] = findViewById(R.id.lblRRLower);
        lblRRUpper[0] = findViewById(R.id.lblRRUpper);
        lblRiskDiff[0] = findViewById(R.id.lblRiskDiff);
        lblRDLower[0] = findViewById(R.id.lblRDLower);
        lblRDUpper[0] = findViewById(R.id.lblRDUpper);
        lblChiSqUnc[0] = findViewById(R.id.lblChiSqUnc);
        lblPUnc[0] = findViewById(R.id.lblPUnc);
        lblChiSqMH[0] = findViewById(R.id.lblChiSqMH);
        lblPMH[0] = findViewById(R.id.lblPMH);
        lblChiSqCor[0] = findViewById(R.id.lblChiSqCor);
        lblPCor[0] = findViewById(R.id.lblPCor);
        lblMidP1T[0] = findViewById(R.id.lblMidP1T);
        lblFisher1T[0] = findViewById(R.id.lblFisher1T);
        lblFisher2T[0] = findViewById(R.id.lblFisher2T);
        
        lblOdds[1] = findViewById(R.id.lblOdds2);
        lblOddsLower[1] = findViewById(R.id.lblOddsLower2);
        lblOddsUpper[1] = findViewById(R.id.lblOddsUpper2);
        lblMLE[1] = findViewById(R.id.lblMLE2);
        lblMLELower[1] = findViewById(R.id.lblMLELower2);
        lblMLEUpper[1] = findViewById(R.id.lblMLEUpper2);
        lblFisherLower[1] = findViewById(R.id.lblFisherLower2);
        lblFisherUpper[1] = findViewById(R.id.lblFisherUpper2);
        lblRisk[1] = findViewById(R.id.lblRisk2);
        lblRRLower[1] = findViewById(R.id.lblRRLower2);
        lblRRUpper[1] = findViewById(R.id.lblRRUpper2);
        lblRiskDiff[1] = findViewById(R.id.lblRiskDiff2);
        lblRDLower[1] = findViewById(R.id.lblRDLower2);
        lblRDUpper[1] = findViewById(R.id.lblRDUpper2);
        lblChiSqUnc[1] = findViewById(R.id.lblChiSqUnc2);
        lblPUnc[1] = findViewById(R.id.lblPUnc2);
        lblChiSqMH[1] = findViewById(R.id.lblChiSqMH2);
        lblPMH[1] = findViewById(R.id.lblPMH2);
        lblChiSqCor[1] = findViewById(R.id.lblChiSqCor2);
        lblPCor[1] = findViewById(R.id.lblPCor2);
        lblMidP1T[1] = findViewById(R.id.lblMidP1T2);
        lblFisher1T[1] = findViewById(R.id.lblFisher1T2);
        lblFisher2T[1] = findViewById(R.id.lblFisher2T2);
        
        lblOdds[2] = findViewById(R.id.lblOdds3);
        lblOddsLower[2] = findViewById(R.id.lblOddsLower3);
        lblOddsUpper[2] = findViewById(R.id.lblOddsUpper3);
        lblMLE[2] = findViewById(R.id.lblMLE3);
        lblMLELower[2] = findViewById(R.id.lblMLELower3);
        lblMLEUpper[2] = findViewById(R.id.lblMLEUpper3);
        lblFisherLower[2] = findViewById(R.id.lblFisherLower3);
        lblFisherUpper[2] = findViewById(R.id.lblFisherUpper3);
        lblRisk[2] = findViewById(R.id.lblRisk3);
        lblRRLower[2] = findViewById(R.id.lblRRLower3);
        lblRRUpper[2] = findViewById(R.id.lblRRUpper3);
        lblRiskDiff[2] = findViewById(R.id.lblRiskDiff3);
        lblRDLower[2] = findViewById(R.id.lblRDLower3);
        lblRDUpper[2] = findViewById(R.id.lblRDUpper3);
        lblChiSqUnc[2] = findViewById(R.id.lblChiSqUnc3);
        lblPUnc[2] = findViewById(R.id.lblPUnc3);
        lblChiSqMH[2] = findViewById(R.id.lblChiSqMH3);
        lblPMH[2] = findViewById(R.id.lblPMH3);
        lblChiSqCor[2] = findViewById(R.id.lblChiSqCor3);
        lblPCor[2] = findViewById(R.id.lblPCor3);
        lblMidP1T[2] = findViewById(R.id.lblMidP1T3);
        lblFisher1T[2] = findViewById(R.id.lblFisher1T3);
        lblFisher2T[2] = findViewById(R.id.lblFisher2T3);
        
        lblOdds[3] = findViewById(R.id.lblOdds4);
        lblOddsLower[3] = findViewById(R.id.lblOddsLower4);
        lblOddsUpper[3] = findViewById(R.id.lblOddsUpper4);
        lblMLE[3] = findViewById(R.id.lblMLE4);
        lblMLELower[3] = findViewById(R.id.lblMLELower4);
        lblMLEUpper[3] = findViewById(R.id.lblMLEUpper4);
        lblFisherLower[3] = findViewById(R.id.lblFisherLower4);
        lblFisherUpper[3] = findViewById(R.id.lblFisherUpper4);
        lblRisk[3] = findViewById(R.id.lblRisk4);
        lblRRLower[3] = findViewById(R.id.lblRRLower4);
        lblRRUpper[3] = findViewById(R.id.lblRRUpper4);
        lblRiskDiff[3] = findViewById(R.id.lblRiskDiff4);
        lblRDLower[3] = findViewById(R.id.lblRDLower4);
        lblRDUpper[3] = findViewById(R.id.lblRDUpper4);
        lblChiSqUnc[3] = findViewById(R.id.lblChiSqUnc4);
        lblPUnc[3] = findViewById(R.id.lblPUnc4);
        lblChiSqMH[3] = findViewById(R.id.lblChiSqMH4);
        lblPMH[3] = findViewById(R.id.lblPMH4);
        lblChiSqCor[3] = findViewById(R.id.lblChiSqCor4);
        lblPCor[3] = findViewById(R.id.lblPCor4);
        lblMidP1T[3] = findViewById(R.id.lblMidP1T4);
        lblFisher1T[3] = findViewById(R.id.lblFisher1T4);
        lblFisher2T[3] = findViewById(R.id.lblFisher2T4);
        
        lblOdds[4] = findViewById(R.id.lblOdds5);
        lblOddsLower[4] = findViewById(R.id.lblOddsLower5);
        lblOddsUpper[4] = findViewById(R.id.lblOddsUpper5);
        lblMLE[4] = findViewById(R.id.lblMLE5);
        lblMLELower[4] = findViewById(R.id.lblMLELower5);
        lblMLEUpper[4] = findViewById(R.id.lblMLEUpper5);
        lblFisherLower[4] = findViewById(R.id.lblFisherLower5);
        lblFisherUpper[4] = findViewById(R.id.lblFisherUpper5);
        lblRisk[4] = findViewById(R.id.lblRisk5);
        lblRRLower[4] = findViewById(R.id.lblRRLower5);
        lblRRUpper[4] = findViewById(R.id.lblRRUpper5);
        lblRiskDiff[4] = findViewById(R.id.lblRiskDiff5);
        lblRDLower[4] = findViewById(R.id.lblRDLower5);
        lblRDUpper[4] = findViewById(R.id.lblRDUpper5);
        lblChiSqUnc[4] = findViewById(R.id.lblChiSqUnc5);
        lblPUnc[4] = findViewById(R.id.lblPUnc5);
        lblChiSqMH[4] = findViewById(R.id.lblChiSqMH5);
        lblPMH[4] = findViewById(R.id.lblPMH5);
        lblChiSqCor[4] = findViewById(R.id.lblChiSqCor5);
        lblPCor[4] = findViewById(R.id.lblPCor5);
        lblMidP1T[4] = findViewById(R.id.lblMidP1T5);
        lblFisher1T[4] = findViewById(R.id.lblFisher1T5);
        lblFisher2T[4] = findViewById(R.id.lblFisher2T5);
        
        lblsH1[1] = findViewById(R.id.lblH12);
        lblsH2[1] = findViewById(R.id.lblH22);
        lblsV1[1] = findViewById(R.id.lblV12);
        lblsV2[1] = findViewById(R.id.lblV22);
        lblsTotal[1] = findViewById(R.id.lblTotal2);
        
        lblsH1[2] = findViewById(R.id.lblH13);
        lblsH2[2] = findViewById(R.id.lblH23);
        lblsV1[2] = findViewById(R.id.lblV13);
        lblsV2[2] = findViewById(R.id.lblV23);
        lblsTotal[2] = findViewById(R.id.lblTotal3);
        
        lblsH1[3] = findViewById(R.id.lblH14);
        lblsH2[3] = findViewById(R.id.lblH24);
        lblsV1[3] = findViewById(R.id.lblV14);
        lblsV2[3] = findViewById(R.id.lblV24);
        lblsTotal[3] = findViewById(R.id.lblTotal4);
        
        lblsH1[4] = findViewById(R.id.lblH15);
        lblsH2[4] = findViewById(R.id.lblH25);
        lblsV1[4] = findViewById(R.id.lblV15);
        lblsV2[4] = findViewById(R.id.lblV25);
        lblsTotal[4] = findViewById(R.id.lblTotal5);
        
        txtsYY[0] = findViewById(R.id.txtYY);
        txtsYN[0] = findViewById(R.id.txtYN);
        txtsNY[0] = findViewById(R.id.txtNY);
        txtsNN[0] = findViewById(R.id.txtNN);
        
        txtsYY[1] = findViewById(R.id.txtYY2);
        txtsYN[1] = findViewById(R.id.txtYN2);
        txtsNY[1] = findViewById(R.id.txtNY2);
        txtsNN[1] = findViewById(R.id.txtNN2);
        
        txtsYY[2] = findViewById(R.id.txtYY3);
        txtsYN[2] = findViewById(R.id.txtYN3);
        txtsNY[2] = findViewById(R.id.txtNY3);
        txtsNN[2] = findViewById(R.id.txtNN3);
        
        txtsYY[3] = findViewById(R.id.txtYY4);
        txtsYN[3] = findViewById(R.id.txtYN4);
        txtsNY[3] = findViewById(R.id.txtNY4);
        txtsNN[3] = findViewById(R.id.txtNN4);
        
        txtsYY[4] = findViewById(R.id.txtYY5);
        txtsYN[4] = findViewById(R.id.txtYN5);
        txtsNY[4] = findViewById(R.id.txtNY5);
        txtsNN[4] = findViewById(R.id.txtNN5);
        
        txtsYY[0].addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				Calculate(0);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
			
		});

        txtsYN[0].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(0);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        txtsNY[0].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(0);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}

        });

        txtsNN[0].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(0);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        
        txtsYY[1].addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				Calculate(1);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
			
		});

        txtsYN[1].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(1);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        txtsNY[1].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(1);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}

        });

        txtsNN[1].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(1);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        
        txtsYY[2].addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				Calculate(2);			
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
			
		});

        txtsYN[2].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(2);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        txtsNY[2].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(2);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}

        });

        txtsNN[2].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(2);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        
        txtsYY[3].addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				Calculate(3);			
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
			
		});

        txtsYN[3].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(3);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        txtsNY[3].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(3);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}

        });

        txtsNN[3].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(3);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        
        txtsYY[4].addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				Calculate(4);			
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
			
		});

        txtsYN[4].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(4);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });

        txtsNY[4].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(4);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}

        });

        txtsNN[4].addTextChangedListener(new TextWatcher() {
	
        	@Override
        	public void onTextChanged(CharSequence s, int start, int before, int count) 
        	{
        		Calculate(4);
        	}
	
        	@Override
        	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	
        	@Override
        	public void afterTextChanged(Editable s) {}
        
        });
        }
        catch (Exception ex)
        {
        	
        }

    }
    
    private void Calculate(int strata)
    {
    	strataCalculates[strata] = false;
    	
    	int yy = 0;
    	int yn = 0;
    	int ny = 0;
    	int nn = 0;
    	
    	if (txtsYY[strata].getText().toString().length()>0)
    		yy = Integer.parseInt(txtsYY[strata].getText().toString());
    	if (txtsYN[strata].getText().toString().length()>0)
    		yn = Integer.parseInt(txtsYN[strata].getText().toString());
    	if (txtsNY[strata].getText().toString().length()>0)
    		ny = Integer.parseInt(txtsNY[strata].getText().toString());
    	if (txtsNN[strata].getText().toString().length()>0)
    		nn = Integer.parseInt(txtsNN[strata].getText().toString());
    	
    	int h1 = yy + yn;
    	int h2 = ny + nn;
    	int v1 = yy + ny;
    	int v2 = yn + nn;
    	int total = v1 + v2;
    	
    	lblsH1[strata].setText(h1 + "");
    	lblsH2[strata].setText(h2 + "");
    	lblsV1[strata].setText(v1 + "");
    	lblsV2[strata].setText(v2 + "");
    	lblsTotal[strata].setText(total + "");
    	
    	if ((txtsYY[strata].getText().toString().length()>0)
    			&& (txtsYN[strata].getText().toString().length()>0)
    			&& (txtsNY[strata].getText().toString().length()>0)
    			&& (txtsNN[strata].getText().toString().length()>0))
    	{
    	
   		double singleTableStats[] = ExactOR.CalcPoly(yy, yn, ny, nn);
		double oddsRisk[] = OddsAndRisk.MHStats(yy, yn, ny, nn, 0.95);
    		
		DecimalFormat formatter = new DecimalFormat("#.####");
		DecimalFormat formatter2 = new DecimalFormat("#.########");
		
		lblOdds[strata].setText(formatter.format(oddsRisk[0]));
		lblOddsLower[strata].setText(formatter.format(oddsRisk[1]));
		lblOddsUpper[strata].setText(formatter.format(oddsRisk[2]));
		
		lblMLE[strata].setText(formatter.format(singleTableStats[0]));
		lblMLELower[strata].setText(formatter.format(singleTableStats[4]));
		lblMLEUpper[strata].setText(formatter.format(singleTableStats[2]));
		
		lblFisherLower[strata].setText(formatter.format(singleTableStats[3]));
		lblFisherUpper[strata].setText(formatter.format(singleTableStats[1]));
		
		lblRisk[strata].setText(formatter.format(oddsRisk[3]));
		lblRRLower[strata].setText(formatter.format(oddsRisk[4]));
		lblRRUpper[strata].setText(formatter.format(oddsRisk[5]));
		
		lblRiskDiff[strata].setText(formatter.format(oddsRisk[6]));
		lblRDLower[strata].setText(formatter.format(oddsRisk[7]));
		lblRDUpper[strata].setText(formatter.format(oddsRisk[8]));
		
		lblChiSqUnc[strata].setText(formatter.format(oddsRisk[9]));			
		lblPUnc[strata].setText(formatter2.format(oddsRisk[10]));			
		lblChiSqMH[strata].setText(formatter.format(oddsRisk[11]));			
		lblPMH[strata].setText(formatter2.format(oddsRisk[12]));			
		lblChiSqCor[strata].setText(formatter.format(oddsRisk[13]));			
		lblPCor[strata].setText(formatter2.format(oddsRisk[14]));
		
		lblMidP1T[strata].setText(formatter2.format(singleTableStats[5]));
		lblFisher1T[strata].setText(formatter2.format(singleTableStats[6]));
		lblFisher2T[strata].setText(formatter2.format(singleTableStats[7]));
		
		strataCalculates[strata] = true;
		EnableDisableSummary();
    	}
    }
    
    private void EnableDisableSummary()
    {
    	btnSummary.setEnabled(false);
    	ArrayList<Double> a = new ArrayList<Double>();
    	ArrayList<Double> b = new ArrayList<Double>();
    	ArrayList<Double> c = new ArrayList<Double>();
    	ArrayList<Double> d = new ArrayList<Double>();
    	
    	for (int i = 0; i < 5; i++)
    	{
    		if (strataCalculates[i])
    		{
    			a.add(Double.parseDouble(txtsYY[i].getText().toString()));
    			b.add(Double.parseDouble(txtsYN[i].getText().toString()));
    			c.add(Double.parseDouble(txtsNY[i].getText().toString()));
    			d.add(Double.parseDouble(txtsNN[i].getText().toString()));
    		}
    	}
    	
    	if (a.size() > 1)
    	{    	
    		btnSummary.setEnabled(true);
    		yySet = convertArrayList(a);
    		ynSet = convertArrayList(b);
    		nySet = convertArrayList(c);
    		nnSet = convertArrayList(d);
    	}
    }
    
    private void CalculateSummary()
    {
    	removeDialog(1);
    	showDialog(1);
    }
    
    @Override
    protected Dialog onCreateDialog(int id)
	{
    	    	
    	Dialog summaryDialog = new Dialog(this);
    	summaryDialog.setTitle(R.string.statcalc_summary_stat);
		summaryDialog.setContentView(R.layout.summary_2x2_dialog);
		summaryDialog.setCancelable(true);
		
		lblMHAdjusted = summaryDialog.findViewById(R.id.lblMHAdjusted);
		lblMHLower = summaryDialog.findViewById(R.id.lblMHLower);
		lblMHUpper = summaryDialog.findViewById(R.id.lblMHUpper);
		lblExactAdjusted = summaryDialog.findViewById(R.id.lblExactAdjusted);
		lblExactLower = summaryDialog.findViewById(R.id.lblExactLower);
		lblExactUpper = summaryDialog.findViewById(R.id.lblExactUpper);
		lblRiskAdjusted = summaryDialog.findViewById(R.id.lblRiskAdjusted);
		lblRiskLower = summaryDialog.findViewById(R.id.lblRiskLower);
		lblRiskUpper = summaryDialog.findViewById(R.id.lblRiskUpper);
		lblUncorrectedChi = summaryDialog.findViewById(R.id.lblUncorrectedChi);
		lblUncorrectedP = summaryDialog.findViewById(R.id.lblUncorrectedP);
		lblCorrectedChi = summaryDialog.findViewById(R.id.lblCorrectedChi);
		lblCorrectedP = summaryDialog.findViewById(R.id.lblCorrectedP);
		summaryWaitCursor = summaryDialog.findViewById(R.id.waitCursor);

		summaryWaitCursor.setVisibility(View.VISIBLE);
		new SummaryProcessor().execute(yySet,ynSet,nySet,nnSet);
		
		return summaryDialog;
	}
    
    private double[] convertArrayList(ArrayList<Double> doubles)
    {
        double[] ret = new double[doubles.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = doubles.get(i).doubleValue();
        }
        return ret;
    }
    
    private class SummaryProcessor extends AsyncTask<double[],Void, double[]>
    {

		@Override
		protected double[] doInBackground(double[]... params) {
			
			return Strat2x2.StratStats(params[0], params[1], params[2], params[3]);
			
		}
		
		@Override
        protected void onPostExecute(double[] results) {
            
			DecimalFormat formatter = new DecimalFormat("#.####");
			DecimalFormat formatter2 = new DecimalFormat("#.########");
			
			lblMHAdjusted.setText(formatter.format(results[0]));
			lblMHLower.setText(formatter.format(results[1]));
			lblMHUpper.setText(formatter.format(results[2]));
			lblExactAdjusted.setText(formatter.format(results[10]));
			lblExactLower.setText(formatter.format(results[11]));
			lblExactUpper.setText(formatter.format(results[12]));
			lblRiskAdjusted.setText(formatter.format(results[3]));
			lblRiskLower.setText(formatter.format(results[4]));
			lblRiskUpper.setText(formatter.format(results[5]));
			lblUncorrectedChi.setText(formatter.format(results[6]));
			lblUncorrectedP.setText(formatter2.format(results[7]));
			lblCorrectedChi.setText(formatter.format(results[8]));
			lblCorrectedP.setText(formatter2.format(results[9]));
			
			summaryWaitCursor.setVisibility(View.GONE);
        }
    	
    }
}
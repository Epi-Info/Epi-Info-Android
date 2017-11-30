package gov.cdc.epiinfo.statcalc;

import gov.cdc.epiinfo.DeviceManager;
import gov.cdc.epiinfo.R;
import gov.cdc.epiinfo.statcalc.calculators.Cohort;
import gov.cdc.epiinfo.statcalc.etc.CCResult;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;


public class CohortActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	
	Spinner ddlConfidence;
	TextView txtPower;
	SeekBar skbPower;
	EditText txtControlRatio;
	TextView txtPercentExposed;
	SeekBar skbPercentExposed;
	EditText txtOddsRatio;
	EditText txtRiskRatio;
	TextView txtPercentCasesExposure;
	SeekBar skbPercentCasesExposure;
	TextView txtKelseyCases;
	TextView txtKelseyControls;
	TextView txtKelseyTotal;
	TextView txtFleissCases;
	TextView txtFleissControls;
	TextView txtFleissTotal;
	TextView txtFleissCCCases;
	TextView txtFleissCCControls;
	TextView txtFleissCCTotal;
	InputMethodManager imm;
	Cohort calc;
	boolean riskChanging;
	boolean oddsChanging;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DeviceManager.SetOrientation(this, false);
		this.setTheme(R.style.AppThemeNoBar);
        
        setContentView(R.layout.statcalc_cohort);
        riskChanging = false;
        oddsChanging = false;
        calc = new Cohort();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ddlConfidence = findViewById(R.id.ddlConfidence);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.confidence_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ddlConfidence.setAdapter(adapter);
        ddlConfidence.setOnItemSelectedListener(
        		new OnItemSelectedListener() {
        				public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
        				{
        					Calculate();
        				}
        				
        				public void onNothingSelected(AdapterView<?> parent)
        				{
        					// do nothing
        				}
        		}
        );
        txtPower = findViewById(R.id.txtPower);
        skbPower = findViewById(R.id.skbPower);
        skbPower.setOnSeekBarChangeListener(this);
        txtControlRatio = findViewById(R.id.txtControlRatio);
        txtControlRatio.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				if (txtControlRatio.getText().toString().length() > 0)
				{
					Calculate();
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
        txtPercentExposed = findViewById(R.id.txtPercentExposed);
        skbPercentExposed = findViewById(R.id.skbPercentExposed);
        skbPercentExposed.setOnSeekBarChangeListener(this);
        txtRiskRatio = findViewById(R.id.txtRiskRatio);
        txtRiskRatio.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (txtRiskRatio.getText().toString().length() > 0)
				{
					double riskRatioRaw = Double.parseDouble(txtRiskRatio.getText().toString());
					double percentExposed = ((skbPercentExposed.getProgress() / 10) / 10.0)/100.0;
					double oddsRatioRaw = calc.RiskToOdds(riskRatioRaw, percentExposed);
				
					if (!oddsChanging)
					{
						if (oddsRatioRaw < 999)
						{
							txtOddsRatio.setText(new DecimalFormat("#.##").format(oddsRatioRaw));
						}
						else
						{
							txtOddsRatio.setText(new DecimalFormat("#").format(oddsRatioRaw));
						}
					}
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				riskChanging = true;
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
				riskChanging = false;				
			}
		});
        txtOddsRatio = findViewById(R.id.txtOddsRatio);
        txtOddsRatio.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (txtOddsRatio.getText().toString().length() > 0)
				{
					double oddsRatioRaw = Double.parseDouble(txtOddsRatio.getText().toString());
					double percentExposed = ((skbPercentExposed.getProgress() / 10) / 10.0)/100.0;
					double percentCases = calc.OddsToPercentCases(oddsRatioRaw, percentExposed);
					double riskRatioRaw = calc.OddsToRisk(oddsRatioRaw, percentExposed);
					
					if (!riskChanging)
					{
						if (riskRatioRaw < 999)
						{
							txtRiskRatio.setText(new DecimalFormat("#.##").format(riskRatioRaw));
						}
						else
						{
							txtRiskRatio.setText(new DecimalFormat("#").format(riskRatioRaw));
						}
					}
					skbPercentCasesExposure.setProgress((int)Math.round(percentCases * 100.0));
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				oddsChanging = true;				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				oddsChanging = false;
			}
		});
        txtPercentCasesExposure = findViewById(R.id.txtPercentCasesExposure);
        skbPercentCasesExposure = findViewById(R.id.skbPercentCasesExposure);
        skbPercentCasesExposure.setOnSeekBarChangeListener(this);
        txtKelseyCases = findViewById(R.id.txtKelseyCases);
        txtKelseyControls = findViewById(R.id.txtKelseyControls);
        txtKelseyTotal = findViewById(R.id.txtKelseyTotal);
        txtFleissCases = findViewById(R.id.txtFleissCases);
        txtFleissControls = findViewById(R.id.txtFleissControls);
        txtFleissTotal = findViewById(R.id.txtFleissTotal);
        txtFleissCCCases = findViewById(R.id.txtFleissCCCases);
        txtFleissCCControls = findViewById(R.id.txtFleissCCControls);
        txtFleissCCTotal = findViewById(R.id.txtFleissCCTotal);
        Calculate();
		imm.hideSoftInputFromWindow(txtControlRatio.getWindowToken(), 0);
    }
    
    private void Calculate()
    {
    	calc = new Cohort();
    	String confidenceRaw = ddlConfidence.getSelectedItem().toString().split("%")[0];
		double confidence = (100.0 - Double.parseDouble(confidenceRaw)) / 100.0;
		double power = (skbPower.getProgress() / 100.0) / 100.0;
		double controlRatio = 1;
		if (txtControlRatio.getText().toString().length() > 0)
		{
			controlRatio = Double.parseDouble(txtControlRatio.getText().toString());
		}
		else
		{
			txtControlRatio.setText("1");
		}
		double percentExposed = (skbPercentExposed.getProgress() / 100.0) / 100.0;
		double oddsRatio = 3;
		if (txtOddsRatio.getText().toString().length() > 0)
		{
			oddsRatio = Double.parseDouble(txtOddsRatio.getText().toString());
		}
		else
		{
			txtOddsRatio.setText("3");
		}
		double percentCasesExposure = (skbPercentCasesExposure.getProgress() / 100.0) / 100.0;
		CCResult results = calc.CalculateUnmatchedCaseControl(confidence, power, controlRatio, percentExposed, oddsRatio, percentCasesExposure);
		txtKelseyCases.setText(results.GetKelseyCases() + "");
		txtKelseyControls.setText(results.GetKelseyControls() + "");
		txtKelseyTotal.setText((results.GetKelseyCases() + results.GetKelseyControls()) + "");
		txtFleissCases.setText(results.GetFleissCases() + "");
		txtFleissControls.setText(results.GetFleissControls() + "");
		txtFleissTotal.setText((results.GetFleissCases() + results.GetFleissControls()) + "");
		txtFleissCCCases.setText(results.GetFleissCCCases() + "");
		txtFleissCCControls.setText(results.GetFleissCCControls() + "");
		txtFleissCCTotal.setText((results.GetFleissCCCases() + results.GetFleissCCControls()) + "");
		
		//scroller.fullScroll(ScrollView.FOCUS_DOWN);
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (seekBar.getId() == R.id.skbPower)
		{
			txtPower.setText(((progress / 10) / 10.0) + "%");
		}
		else if (seekBar.getId() == R.id.skbPercentExposed)
		{
			double percentExposedRaw = (progress / 10) / 10.0;
			double percentExposed = percentExposedRaw / 100.0;
			double percentCases = ((skbPercentCasesExposure.getProgress() / 10) / 10.0)/100.0;
			double oddsRatioRaw = calc.PercentCasesToOdds(percentCases, percentExposed);
			
			txtPercentExposed.setText(percentExposedRaw + "%");
			if (oddsRatioRaw < 999)
			{
				txtOddsRatio.setText(new DecimalFormat("#.##").format(oddsRatioRaw));
			}
			else
			{
				txtOddsRatio.setText(new DecimalFormat("#").format(oddsRatioRaw));
			}
		}		
		else
		{
			double percentCasesRaw = (progress / 10) / 10.0;
			double percentCases = percentCasesRaw / 100.0;
			double percentExposed = ((skbPercentExposed.getProgress() / 10) / 10.0)/100.0;
			double oddsRatioRaw = calc.PercentCasesToOdds(percentCases, percentExposed);
			
			txtPercentCasesExposure.setText(percentCasesRaw + "%");
			if (fromUser)
			{
				if (oddsRatioRaw < 999)
				{
					txtOddsRatio.setText(new DecimalFormat("#.##").format(oddsRatioRaw));
				}
				else
				{
					txtOddsRatio.setText(new DecimalFormat("#").format(oddsRatioRaw));
				}
			}
		}
		if (fromUser)
		{
			imm.hideSoftInputFromWindow(txtOddsRatio.getWindowToken(), 0);
		}
		Calculate();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
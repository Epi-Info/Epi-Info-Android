package gov.cdc.epiinfo.statcalc;

import gov.cdc.epiinfo.DeviceManager;
import gov.cdc.epiinfo.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class StatCalcMain extends Activity {

	
	private void LoadActivity(Class c)
	{
		startActivity(new Intent(this, c));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DeviceManager.SetOrientation(this, false);
		this.setTheme(R.style.AppThemeNoBar);
		
		setContentView(R.layout.statcalc_main);
		Button btnPS = findViewById(R.id.btnPS);
		btnPS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(PopulationSurveyActivity.class);				
			}
		});
		Button btnUCC = findViewById(R.id.btnUCC);
		btnUCC.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(UnmatchedActivity.class);
			}
		});
		Button btnCCS = findViewById(R.id.btnCCS);
		btnCCS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(CohortActivity.class);
			}
		});
		Button btn2x2 = findViewById(R.id.btn2x2);
		btn2x2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(TwoByTwoActivity.class);
			}
		});
		try
		{
			Button btnMatchedPair = findViewById(R.id.btnMatchedPair);
			btnMatchedPair.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					LoadActivity(MatchedPairActivity.class);
				}
			});
		}
		catch (Exception e) { }
		Button btnChiSq = findViewById(R.id.btnChiSq);
		btnChiSq.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LoadActivity(ChiSquareActivity.class);
			}
		});
		try
		{
			Button btnPoisson = findViewById(R.id.btnPoisson);
			btnPoisson.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					LoadActivity(PoissonActivity.class);
				}
			});
			Button btnBinomial = findViewById(R.id.btnBinomial);
			btnBinomial.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					LoadActivity(BinomialActivity.class);
				}
			});
		}
		catch (Exception e) { }
		Button btnOpenEpi = findViewById(R.id.btnOpenEpi);
		btnOpenEpi.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uriUrl = Uri.parse("http://www.openepi.com/");
				startActivity(new Intent(Intent.ACTION_VIEW, uriUrl));
			}
		});
	}


}
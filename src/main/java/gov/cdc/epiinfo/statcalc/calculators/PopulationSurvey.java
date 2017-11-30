package gov.cdc.epiinfo.statcalc.calculators;

public class PopulationSurvey {
	public int[] CalculateSampleSizes(int pop, double freqq, double worstt)
    {
		double freq = Math.floor(10 * freqq) / 10;
		double worst = Math.floor(10 * worstt) / 10;
        double[] percentiles = new double[] { 0.80, 0.90, 0.95, 0.97, 0.99, 0.999, 0.9999 };
        int[] sizes = new int[7];
        //double d = Math.abs(freq - worst);
        double d = Math.abs(worst);
        double factor = freq * (100 - freq) / (d * d);
        for (int i = 0; i < percentiles.length; i++)
        {
            double twoTail = ANorm(1 - percentiles[i]);
            double n = twoTail * twoTail * factor;
            double sampleSize = n / (1 + (n / pop));
            sizes[i] = (int) Math.round(sampleSize);
        }
        return sizes;
    }

	public void UnmatchedCaseControl(double a, double b, double vr, double v2, double vor, double v1)
    {
        double Za = ANorm(a);
        double Zb = 0;

        if (b >= 1) 
        { 
            b = b / 100.0; 
        }
        if (b < 0.5) 
        { 
            Zb = -ANorm(2.0 * b); 
        } 
        else 
        { 
            Zb = ANorm(2.0 - 2.0 * b); 
        }
        if (vor != 0)
        {
            v1 = v2 * vor / (1.0 + v2 * (vor - 1.0));
        }
        double pbar = (v1 + vr * v2) / (1.0 + vr);
        double qbar = 1.0 - pbar;
        double vn = ((Math.pow((Za + Zb), 2.0)) * pbar * qbar * (vr + 1.0)) / ((Math.pow((v1 - v2), 2.0)) * vr);
        double vn1 = Math.pow(((Za * Math.sqrt((vr + 1.0) * pbar * qbar)) + (Zb * Math.sqrt((vr * v1 * (1.0 - v1)) + (v2 * (1.0 - v2))))), 2.0) / (vr * Math.pow((v2 - v1), 2.0));
        double vn2 = Math.pow(Za * Math.sqrt((vr + 1.0) * pbar * qbar) + Zb * Math.sqrt(vr * v1 * (1.0 - v1) + v2 * (1.0 - v2)), 2.0) / (vr * Math.pow(Math.abs(v1 - v2), 2.0));
        vn2 = vn2 * Math.pow((1.0 + Math.sqrt(1.0 + 2.0 * (vr + 1.0) / (vn2 * vr * Math.abs(v2 - v1)))), 2.0) / 4.0;
    }

    public void Cohort(double a, double b, double vr, double v2, double vor, double v1, double rr, double dd)
    {
        double Za = ANorm(a);
        if (b >= 1) 
        { 
            b = b / 100.0; 
        }
        double Zb;
        if (b < 0.5) 
        { 
            Zb = -ANorm(2.0 * b); 
        } 
        else 
        { 
            Zb = ANorm(2.0 - (2.0 * b)); 
        }
        if (vor != 0)
        {
            v1 = v2 * vor / (1.0 + v2 * (vor - 1.0));
        }
        double pbar = (v1 + vr * v2) / (1.0 + vr);
        double qbar = 1.0 - pbar;
        double vn = ((Math.pow((Za + Zb), 2.0)) * pbar * qbar * (vr + 1.0)) / ((Math.pow((v1 - v2), 2.0)) * vr);
        double vn1 = Math.pow(((Za * Math.sqrt((vr + 1.0) * pbar * qbar)) + (Zb * Math.sqrt((vr * v1 * (1.0 - v1)) + (v2 * (1.0 - v2))))), 2.0) / (vr * Math.pow((v2 - v1), 2.0));
        double vn2 = Math.pow(Za * Math.sqrt((vr + 1.0) * pbar * qbar) + Zb * Math.sqrt(vr * v1 * (1.0 - v1) + v2 * (1.0 - v2)), 2.0) / (vr * Math.pow(Math.abs(v1 - v2), 2.0));
        vn2 = vn2 * Math.pow((1.0 + Math.sqrt(1.0 + 2.0 * (vr + 1.0) / (vn2 * vr * Math.abs(v2 - v1)))), 2.0) / 4.0;
    }
	
    private double ANorm(double p)
    {
        double v = 0.5;
        double dv = 0.5;
        double z = 0;

        while (dv > 1e-6)
        {
            z = 1.0 / v - 1.0;
            dv = dv / 2.0;
            if (Norm(z) > p)
            {
                v = v - dv;
            }
            else
            {
                v = v + dv;
            }
        }

        return z;
    }

    private double Norm(double z)
    {
        z = Math.sqrt(z * z);
        double p = 1.0 + z * (0.04986735 + z * (0.02114101 + z * (0.00327763 + z * (0.0000380036 + z * (0.0000488906 + z * 0.000005383)))));
        p = p * p; p = p * p; p = p * p;
        return 1.0 / (p * p);
    }
	
    /*static void Main(string[] args)
    {
        //95,80,1.0,40,10,86.96
        UnmatchedCaseControl(0.05, 80.0, 1.0, 0.4, 10.0, 0.8696);
        //95,80,1.0,5,24,55.81,11.16,50.81
        Cohort(0.05, 80.0, 1.0, 0.05, 24.0, 0.5581, 11.16, 0.5081);
        OddsToPercentCases(13, 0.37);
        PercentCasesToOdds(0.8842, 0.37);
        SampleSize(999999, 50, 11);
    }*/

    private double OddsToPercentCases(double oddsRatio, double percentControls)
    {
        double rawVal = 0;
        if (oddsRatio != 0)
        {
            rawVal = 100 * percentControls * oddsRatio / (1 + percentControls * (oddsRatio - 1));
        }
        return Math.round(100000 * rawVal) / 100000;
    }

    private double PercentCasesToOdds(double percentCases, double percentControls)
    {
        return Math.round(100000 * (percentCases * (1 - percentControls)) / (percentControls * (1 - percentCases))) / 100000;
    }
    
}

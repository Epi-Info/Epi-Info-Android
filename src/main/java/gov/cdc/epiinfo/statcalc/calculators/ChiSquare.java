package gov.cdc.epiinfo.statcalc.calculators;

import gov.cdc.epiinfo.statcalc.etc.ChiSquareResult;

public class ChiSquare {

	public double calcP(double q, double df)
    {
        double tk = 0;
        double CFL = 0;
        double CFU = 0;
        double prob = 0;
        double df2 = df / 2.0;
        double q2 = q / 2.0;
        int nn = 5;
        if (q <= 0 || df <= 0) return -1;
        if (q < df)
        {
            tk = q2 * (1 - nn - df2) / (df2 + 2 * nn - 1 + nn * q2 / (df2 + 2 * nn));
            for (int kk = nn - 1; kk > 1; kk--)
                tk = q2 * (1 - kk - df2) / (df2 + 2 * kk - 1 + kk * q2 / (df2 + 2 * kk + tk));
            CFL = 1 - q2 / (df2 + 1 + q2 / (df2 + 2 + tk));
            prob = Math.exp(df2 * Math.log(q2) - q2 - lngamma(df2 + 1) - Math.log(CFL));
        }
        else
        {
            tk = (nn - df2) / (q2 + nn);
            for (int kk = nn - 1; kk > 1; kk--)
                tk = (kk - df2) / (q2 + kk / (1 + tk));
            CFU = 1 + (1 - df2) / (q2 + 1 / (1 + tk));
            prob = 1 - Math.exp((df2 - 1) * Math.log(q2) - q2 - lngamma(df2) - Math.log(CFU));
        }
        prob = 1 - prob;
        return prob;
    }

    public double lngamma(double c)
    {
        double[] cof = new double[6];
        cof[0] = 76.18009172947146;
        cof[1] = -86.50532032941677;
        cof[2] = 24.01409824083091;
        cof[3] = -1.231739572450155;
        cof[4] = 0.1208650973866179e-2;
        cof[5] = -0.5395239384953e-5;
        double xx = c;
        double yy = c;
        double tmp = xx + 5.5 - (xx + 0.5) * Math.log(xx + 5.5);
        double ser = 1.000000000190015;
        for (int j = 0; j <= 5; j++)
            ser += (cof[j] / ++yy);
        return (Math.log(2.5066282746310005 * ser / xx) - tmp);
    }

    public ChiSquareResult GetChiSquareForTrend(double[] col1, double[] col2, double[] col3)
    {
        //double[] col1 = new double[] { 0, 1, 2 };
        //double[] col2 = new double[] { 8, 10, 12 };
        //double[] col3 = new double[] { 9, 11, 13 };
        ChiSquareResult result = new ChiSquareResult();
    	double[] acrude = new double[col1.length];
        double[] bcrude = new double[col1.length];
        double[] mhORad = new double[col1.length];
        double[] mhORbc = new double[col1.length];
        double[] oddsRatios = new double[col1.length];
        
        int levels = col1.length;
        double ccrude = 0;
        double dcrude = 0;
        double ttot;
        double Vsum = 0;
        double V1sum = 0;
        double XMHchisq = 0;

        double T1 = 0;
        double T2 = 0;
        double T3 = 0;
        double n1 = 0;
        double n2 = 0;
        double n = 0;
        double OR = 1.0;

        double x, a, b, m;

        double abase = col2[0];
        double bbase = col3[0];
        ccrude += abase;
        dcrude += bbase;

        for (int t = 0; t < levels; t++)
        {
            x = col1[t];

            a = col2[t];
            b = col3[t];
            acrude[t] += a;
            bcrude[t] += b;
            m = a + b;
            T1 += a * x;
            T2 += m * x;
            T3 += m * x * x;
            n1 += a;
            n2 += b;
            n += a + b;

            OR = (a * bbase) / (b * abase);
            if (t > 0)
            {
                ttot = a + bbase + b + abase;
                mhORad[t] += a * bbase / ttot;
                mhORbc[t] += b * abase / ttot;
            }
            oddsRatios[t] = OR;            
        }
        result.SetOddsRatios(oddsRatios);
        
        Vsum += (n1 * n2 * (n * T3 - (T2 * T2))) / (n * n * (n - 1));
        V1sum += T1 - ((n1 / n) * T2);
        XMHchisq += (V1sum * V1sum) / Vsum;
        result.SetChi(XMHchisq);
        result.SetPValue(SharedResources.PValFromChiSq(XMHchisq,1));
        return result;
    }
	
}

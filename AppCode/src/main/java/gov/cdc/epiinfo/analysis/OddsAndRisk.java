package gov.cdc.epiinfo.analysis;

public class OddsAndRisk
{
    public static double[] MHStats(double a, double b, double c, double d, double P)
    {
    	double[] MHStats = new double[15];
    	double z = 0.0;
        if (P < 0.9500001 && P > 0.95 - 0.00001)
            z = 1.96;
        else if (P < 0.9900001 && P > 0.99 - 0.00001)
            z = 2.58;
        else if (P < 0.900001 && P > 0.9 + 0.00001)
            z = 1.64;
        else
            z = SharedResources.ZFromP(P);

        double n1 = a + b;
        double n0 = c + d;
        double m1 = a + c;
        double m0 = b + d;
        double n = m1 + m0;
        double re = a / n1;
        double ru = c / n0;
        if (b * c < 0.00000001)
        {
            MHStats[0] = -1.0; /* Odds Ratio */
            MHStats[1] = -1.0; /* Odds Ratio Lower CL */
            MHStats[2] = -1.0; /* Odds Ratio Upper CL */
        }
        else
        {
            MHStats[0] = (a * d) / (b * c);
            if (d * a < 0.000001)
            {
                MHStats[1] = -1.0;
                MHStats[2] = -1.0;
            }
            else
            {
                MHStats[1] = Math.exp(Math.log((a * d) / (b * c)) - z * Math.sqrt(1 / a + 1 / b + 1 / c + 1 / d));
                MHStats[2] = Math.exp(Math.log((a * d) / (b * c)) + z * Math.sqrt(1 / a + 1 / b + 1 / c + 1 / d));
            }
        }

        if (ru < 0.00001)
        {
            MHStats[3] = -1.0; /* Relative Risk */
            MHStats[4] = -1.0; /* Relative Risk Lower CL */
            MHStats[5] = -1.0; /* Relative Risk Upper CL */
        }
        else
        {
            MHStats[3] = re / ru;
            if (re < 0.00001)
            {
                MHStats[4] = -1.0;
                MHStats[5] = -1.0;
            }
            else
            {
                MHStats[4] = Math.exp(Math.log((a / n1) / (c / n0)) - z * Math.sqrt(d / (c * n0) + b / (n1 * a)));
                MHStats[5] = Math.exp(Math.log((a / n1) / (c / n0)) + z * Math.sqrt(d / (c * n0) + b / (n1 * a)));
            }
        }

        MHStats[6] = (re - ru) * 100;                                                          /* Risk Difference */
        MHStats[7] = (re - ru - z * Math.sqrt(re * (1 - re) / n1 + ru * (1 - ru) / n0)) * 100; /* Risk Difference Lower CL */
        MHStats[8] = (re - ru + z * Math.sqrt(re * (1 - re) / n1 + ru * (1 - ru) / n0)) * 100; /* Risk Difference Upper CL */

        double h3 = m1 * m0 * n1 * n0;
        double phi = ((a * d) - b * c) / Math.sqrt(h3);
        MHStats[9] = n * Math.pow(phi, 2.0);                          /* Uncorrected Chi-Sq */
        MHStats[10] = SharedResources.PValFromChiSq(MHStats[9], 1.0);
        MHStats[11] = (n - 1) / h3 * Math.pow((a * d - b * c), 2.0);  /* Mantel-Haenszel Chi-Sq */
        MHStats[12] = SharedResources.PValFromChiSq(MHStats[11], 1.0);
        MHStats[13] = n / h3 * Math.pow(Math.max(0.0, (Math.abs(a * d - b * c) - n * 0.5)), 2.0); /* Corrected Chi-Sq */
        MHStats[14] = SharedResources.PValFromChiSq(MHStats[13], 1.0);

        return MHStats;
    }
}

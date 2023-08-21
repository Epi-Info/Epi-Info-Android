package gov.cdc.epiinfo.analysis;

import java.util.ArrayList;
import java.util.List;


public class ExactOR
{
    public static double[] CalcPoly(double yy, double yn, double ny, double nn)
    {
        double SingleTableStats[] = new double[8];
        double n0 = ny + nn;
        double n1 = yy + yn;
        double m1 = yy + ny;
        double minA = Math.max(0.0, m1 - n0);
        double maxA = Math.min(m1, n1);
        double polyDD[] = new double[(int) (maxA - minA + 1)];
        polyDD[0] = 1.0;
        double aa = minA;
        double bb = m1 - minA + 1.0;
        double cc = n1 - minA + 1.0;
        double dd = n0 - m1 + minA;

        for (int i = 1; i < (maxA - minA) + 1; i++)
        {
            polyDD[i] = polyDD[i - 1] * ((bb - i) / (aa + i)) * ((cc - i) / (dd + i));
        }
        
        SingleTableStats[0] = CalcCmle(1.0, minA, yy, maxA, polyDD);
        SingleTableStats[1] = CalcExactLim(false, true, SingleTableStats[0], minA, yy, maxA, polyDD);   /* Up Fisher Limit  */
        SingleTableStats[2] = CalcExactLim(false, false, SingleTableStats[0], minA, yy, maxA, polyDD);  /* Up MidP Limit    */
        SingleTableStats[3] = CalcExactLim(true, true, SingleTableStats[0], minA, yy, maxA, polyDD);    /* Low Fisher Limit */
        SingleTableStats[4] = CalcExactLim(true, false, SingleTableStats[0], minA, yy, maxA, polyDD);   /* Low MidP Limit   */
        
        double ExactTests[] = ExactTests(minA, yy, polyDD);
        
        SingleTableStats[5] = Math.min(ExactTests[3], ExactTests[4]);                                   /* 1-tail MidP Exact Test   */
        SingleTableStats[6] = Math.min(ExactTests[0], ExactTests[1]);                                   /* 1-tail Fisher Exact Test */
        SingleTableStats[7] = ExactTests[2];                                                            /* 2-tail Fisher Exact Test */
        
        return SingleTableStats;
    }
    
    public static double[] ExactTests(double minSumA, double sumA, double polyD[])
    {
    	double ExactTests[] = new double[5];
    	int diff = (int) (sumA - minSumA);
    	int degD = polyD.length - 1;
    	double upTail = polyD[degD];
    	double twoTail = 0.0;
    	
    	if (upTail <= 1.000001 * polyD[diff])
    		twoTail = twoTail + upTail;
    	for (int i = degD - 1; i >= diff; i--)
    	{
    		upTail = upTail + polyD[i];
    		if (polyD[i] <= 1.000001 * polyD[diff])
    			twoTail = twoTail + polyD[i];
    	}
    	
    	Double denom = upTail;
    	for (int i = diff - 1; i >= 0; i--)
    	{
    		denom = denom + polyD[i];
    		if (polyD[i] <= 1.000001 * polyD[diff])
    			twoTail = twoTail + polyD[i];
    	}
    	
    	ExactTests[0] = 1.0 - (upTail - polyD[diff]) / denom;        /* Lower Fish */
    	ExactTests[1] = upTail / denom;                              /* Upper Fish */
    	ExactTests[2] = twoTail / denom;                             /*   Two Fish */
    	ExactTests[3] = 1.0 - (upTail - 0.5 * polyD[diff]) / denom;  /* Lower MidP */
    	ExactTests[4] = (upTail - 0.5 * polyD[diff]) / denom;        /* Upper MidP */
    	
    	return ExactTests;
    }

    public static double CalcExactLim(Boolean pbLower, Boolean pbFisher, double approx, double minSumA, double sumA, double maxSumA, double polyD[])
    {
    	double limit = 0.0;
        if (minSumA < sumA && sumA < maxSumA)
            limit = GetExactLim(pbLower, pbFisher, approx, minSumA, sumA, polyD);
        else if (sumA == minSumA)
        {
            if (!pbLower)
                limit = GetExactLim(pbLower, pbFisher, approx, minSumA, sumA, polyD);
        }
        else if (sumA == maxSumA)
        {
            if (pbLower)
                limit = GetExactLim(pbLower, pbFisher, approx, minSumA, sumA, polyD);
            else limit = Double.POSITIVE_INFINITY;
        }

        return limit;
    }

    public static double CalcCmle(double approx, double minSumA, double sumA, double maxSumA, double polyD[])
    {
    	double cmle = 0.0;
        if (minSumA < sumA && sumA < maxSumA)
        {
            cmle = GetCmle(approx, minSumA, sumA, polyD);
        }
        else if (sumA == maxSumA)
            cmle = Double.POSITIVE_INFINITY;

        return cmle;
    }

    public static double GetExactLim(Boolean pbLower, Boolean pbFisher, double approx, double minSumA, double sumA, double polyD[])
    {
        int degN = (int) (sumA - minSumA);
        Double pnConfLevel = 0.95;
        Double value = 0.5 * (1.0 - pnConfLevel);
        if (pbLower)
            value = 0.5 * (1 + pnConfLevel);
        if (pbLower && pbFisher)
            degN = (int) (sumA - minSumA - 1);
        double polyN[] = new double[degN + 1];
        for (int i = 0; i <= degN; i++)
            polyN[i] = polyD[i];
        if (!pbFisher)
            polyN[degN] = 0.5 * polyD[degN];
        double limit = Converge(approx, polyN, polyD, sumA, value);

        return (double) Math.round(10000 * limit) / 10000;
    }

    public static double GetCmle(double approx, double minSumA, double sumA, double polyD[])
    {
    	double value = sumA;
        int degN = polyD.length;
        double polyN[] = new double[degN];
        for (int i = 0; i< degN; i++)
        {
            polyN[i] = (minSumA + i) * polyD[i];
        }
        double cmle = Converge(approx, polyN, polyD, sumA, value);

        return (double) Math.round(10000 * cmle) / 10000;
    }
    
    public static double Converge(double approx, double polyN[], double polyD[], double sumA, double value)
    {
    	double f0 = 0.0;
    	double x0 = 0.0;
    	double x1 = 0.0;
    	double f1 = 0.0;
        List<Double> coordinates = BracketRoot(approx, x0, x1, f0, f1, polyN, polyD, sumA, value);
        x0 = coordinates.get(0);
        x1 = coordinates.get(1);
        f0 = coordinates.get(2);
        f1 = coordinates.get(3);
        double cmle = Zero(x0, x1, f0, f1, polyN, polyD, sumA, value);

        return cmle;
    }
    
    public static List<Double> BracketRoot(double approx, double x0, double x1, double f0, double f1, double polyN[], double polyD[], double sumA, double value)
    {
        int iter = 0;
        x1 = Math.max(0.5, approx);
        f0 = Func(x0, polyN, polyD, sumA, value);
        f1 = Func(x1, polyN, polyD, sumA, value);
        while (f1 * f0 > 0.0 && iter < 10000)
        {
            iter = iter + 1;
            x0 = x1;
            f0 = f1;
            x1 = x1 * 1.5 * iter;
            f1 = Func(x1, polyN, polyD, sumA, value);
        }
        List<Double> coordinates = new ArrayList<Double>();
        coordinates.add(x0);
        coordinates.add(x1);
        coordinates.add(f0);
        coordinates.add(f1);
        return coordinates;
    }
    
    public static double Func(double r, double polyN[], double polyD[], double sumA, double value)
    {
    	double numer = EvalPoly(polyN, polyN.length - 1, r);
    	double denom = EvalPoly(polyD, polyD.length - 1, r);
    	double func = 0.0;
        if (r <= 1.0)
            func = numer / denom - value;
        else
            func = (numer / Math.pow(r, polyD.length - polyN.length)) / denom - value;

        return func;
    }
    
    public static double EvalPoly(double c[], int degC, double r)
    {
        Double y = 0.0;
        if (r == 0.0)
            y = c[0];
        else if (r <= 1.0)
        {
            y = c[degC];
            if (r < 1.0)
            {
                for (int i = degC - 1; i >= 0; i--)
                    y = y * r + c[i];
            }
            else
            {
                for (int i = degC - 1; i >= 0; i--)
                    y = y + c[i];
            }
        }
        else if (r > 1.0)
        {
            y = c[0];
            r = 1.0 / r;
            for (int i = 1; i <= degC; i++)
                y = y * r + c[i];
        }

        return y;
    }

    public static double Zero(double x0, double x1, double f0, double f1, double polyN[], double polyD[], double sumA, double value)
    {
        Boolean found = false;
        double f2 = 0.0;
        double x2 = 0.0;
        double swap = 0.0;
        int iter = 0;
        int errorRenamed = 0;

        if (Math.abs(f0) < Math.abs(f1))
        {
            swap = x0; x0 = x1; x1 = swap;
            swap = f0; f0 = f1; f1 = swap;
        }
        found = (f1 == 0.0);
        if (!found && f0 * f1 > 0.0)
            errorRenamed = 1;

        while (!found && iter < 10000 && errorRenamed == 0)
        {
            iter++;
            x2 = x1 - f1 * (x1 - x0) / (f1 - f0);
            f2 = Func(x2, polyN, polyD, sumA, value);
            if (f1 * f2 < 0.0)
            {
                x0 = x1;
                f0 = f1;
            }
            else
                f0 = f0 * f1 / (f1 + f2);
            x1 = x2;
            f1 = f2;
            found = (Math.abs(x1 - x0) < Math.abs(x1) * 0.0000001 || f1 == 0.0);
        }
        double cmle = x1;
        if (!found && iter > 10000)
            cmle = Double.NaN;

        return cmle;
    }
}

package gov.cdc.epiinfo.statcalc.calculators;


public class Strat2x2
{
	public static double[] StratStats(double a[], double b[], double c[], double d[])
	{
		double stratStats[] = new double[13];
		
		stratStats[0] = ComputeOddsRatio(a, b, c, d);                      /* MH Adjusted Odds Ratio */
		double zSElnOR = ZSElnOR(a, b, c, d);
		stratStats[1] = stratStats[0] * Math.exp(-zSElnOR);                /* MH Adjusted OR Lower Limit */
		stratStats[2] = stratStats[0] * Math.exp(zSElnOR);                 /* MH Adjusted OR Upper Limit */
		stratStats[3] = ComputedRR(a, b, c, d);                            /* Adjusted Risk Ratio */
		zSElnOR = ZSElnRR(a, b, c, d);
		stratStats[4] = stratStats[3] * Math.exp(-zSElnOR);                /* Adjusted RR Lower Limit */
		stratStats[5] = stratStats[3] * Math.exp(zSElnOR);                 /* Adjusted RR Upper Limit */
		stratStats[6] = ComputeUnChisq(a, b, c, d);                        /* Uncorrected Chi-Sq */
		stratStats[7] = SharedResources.PValFromChiSq(stratStats[6], 1);   /* 2-Tailed P */
		stratStats[8] = ComputeCorrChisq(a, b, c, d);                      /* Corrected Chi-Sq */
		stratStats[9] = SharedResources.PValFromChiSq(stratStats[8], 1);   /* 2-Tailed P */
		stratStats[11] = ExactORLL(a, b, c, d);                            /* Exact Adjusted OR Lower Limit */
		stratStats[10] = MLEOR(a, b, c, d, stratStats[11]);                /* Exact Adjusted Odds Ratio */
		stratStats[12] = ExactORUL(a, b, c, d, stratStats[10]);            /* Exact Adjusted OR Upper Limit */
		
		return stratStats;
	}
	
	public static double MLEOR(double a[], double b[], double c[], double d[], double LL)
	{
		double mleOR = 0.0;
		boolean yyZero = true;
		boolean ynZero = true;
		boolean nyZero = true;
		boolean nnZero = true;
		for (int i = 0; i < a.length; i++)
		{
			if (a[i] > 0.0)
				yyZero = false;
			if (b[i] > 0.0)
				ynZero = false;
			if (c[i] > 0.0)
				nyZero = false;
			if (d[i] > 0.0)
				nnZero = false;
		}
		if (ynZero || nyZero)
			mleOR = Double.POSITIVE_INFINITY;
		if (!(yyZero || ynZero || nyZero || nnZero))
			mleOR = ucestimaten(a, b, c, d, LL);
		
		return mleOR;
	}
	
	public static double ExactORUL(double a[], double b[], double c[], double d[], double MLE)
	{
		double exactORUL = Double.POSITIVE_INFINITY;
		boolean ynZero = true;
		boolean nyZero = true;
		for (int i = 0; i < a.length; i++)
		{
			if (b[i] > 0.0)
				ynZero = false;
			if (c[i] > 0.0)
				nyZero = false;
		}
		if (!(ynZero || nyZero))
			exactORUL = exactorun(a, b, c, d, MLE);
		
		return exactORUL;
	}
	
	public static double ExactORLL(double a[], double b[], double c[], double d[])
	{
		double exactORLL = 0.0;
		boolean yyZero = true;
		boolean nnZero = true;
		for (int i = 0; i < a.length; i++)
		{
			if (a[i] > 0.0)
				yyZero = false;
			if (d[i] > 0.0)
				nnZero = false;
		}
		if (!(yyZero || nnZero))
			exactORLL = exactorln(a, b, c, d);
		
		return exactORLL;
	}
	
	public static double ucestimaten(double a[], double b[], double c[], double d[], double LL)
	{
		int M1[] = new int[a.length];
		int M0[] = new int[a.length];
		int N1[] = new int[a.length];
		int N0[] = new int[a.length];
		int ls[] = new int[a.length];
		int us[] = new int[a.length];
		double xs[] = a;
		int x = 0;
		int l = 0;
		int u = 0;

		for (int i = 0; i < a.length; i++)
		{
			M1[i] = (int) (a[i] + c[i]);
			M0[i] = (int) (b[i] + d[i]);
			N1[i] = (int) (a[i] + b[i]);
			N0[i] = (int) (c[i] + d[i]);
			ls[i] = Math.max(0, N1[i] - M0[i]);
			us[i] = Math.min(M1[i], N1[i]);
			x = (int) (x + xs[i]);
			l = l + ls[i];
			u = u + us[i];
		}
		
		int dimC2 = 0;
		for (int i = 0; i < a.length; i++)
			dimC2 += (us[i] - ls[i]);
		double maxuldiff = 0.0;
		
		double Cs[][] = new double[a.length][];
		for (int i = 0; i < a.length; i++)
		{
			if (us[i] - ls[i] > maxuldiff)
				maxuldiff = us[i] - ls[i];
			Cs[i] = new double[us[i] - ls[i] + 1];
			for (int s = ls[i]; s <= us[i]; s++)
				Cs[i][s - ls[i]] = choosey(M1[i], s) * choosey(M0[i], N1[i] - s);
		}

		double C2[] = new double[dimC2 + 1];
		for (int j = 0; j <= us[0] - ls[0]; j++)
			for (int k = 0; k <= us[1] - ls[1]; k++)
				C2[j + k] = C2[j + k] + Cs[0][j] * Cs[1][k];
		
		int bound = 0;
		double Y[] = new double[u - l + 1];
		for (int i = 2; i < a.length; i++)
		{
			for (int j = 0; j <= u - l; j++)
			{
				Y[j] = C2[j];
				C2[j] = 0.0;
			}
			bound = 0;
			for (int j = 0; j < i; j++)
				bound = bound + (us[i] - ls[i]);
			for (int j = 0; j <= u - l; j++)
				for (int k = 0; k <= us[i] - ls[i]; k++)
					if (j + k <= u - l)
						C2[j + k] = C2[j + k] + Y[j] * Cs[i][k];
		}
		
		double R = 0.0;
		double Ds[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			Ds[i] = 0.0;
		double d2 = 1.0;
		double FR = 0.0;
		
//		while (Math.abs(FR - x) > 0.002)
		while (FR < x)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 1;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
//				if (Double.isInfinite(d2))
//					return Double.NaN;
			}
			for (int i = 0; i <= u - l; i++)
			{
				double adder = ((i + l) * C2[i]);
				for (int j = 0; j < Ds.length; j++)
					adder /= Ds[j];
				adder *=  Math.pow(R,  i + l);
				FR += adder;
//				FR = FR + ((i + l) * C2[i] * Math.pow(R,  i + l)) / d2;
			}
		}
		double aa = R - 1.0;
		double bb = R + 0.5;
		double precision = 0.00001;
		while (bb - aa > precision)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R =(bb + aa) / 2.0;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= u - l; i++)
			{
				double adder = ((i + l) * C2[i]);
				for (int j = 0; j < Ds.length; j++)
					adder /= Ds[j];
				adder *=  Math.pow(R,  i + l);
				FR += adder;
//				FR = FR + ((i + l) * C2[i] * Math.pow(R,  i + l)) / d2;
			}
			if (FR < x)
				aa = R;
			else
				bb = R;
		}
		/*
		R -= 0.0002;
		while (Math.abs(FR - x) > 0.0002)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 0.00002;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= u - l; i++)
				FR = FR + ((i + l) * C2[i] * Math.pow(R,  i + l)) / d2;
		}
		R -= 0.00002;
		while (Math.abs(FR - x) > 0.0001)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 0.00001;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= u - l; i++)
				FR = FR + ((i + l) * C2[i] * Math.pow(R,  i + l)) / d2;
		}
*/
		return R;
	}
	
	public static double exactorun(double a[], double b[], double c[], double d[], double MLE)
	{
		int M1[] = new int[a.length];
		int M0[] = new int[a.length];
		int N1[] = new int[a.length];
		int N0[] = new int[a.length];
		int ls[] = new int[a.length];
		int us[] = new int[a.length];
		double xs[] = a;
		int x = 0;
		int l = 0;
		int u = 0;

		for (int i = 0; i < a.length; i++)
		{
			M1[i] = (int) (a[i] + c[i]);
			M0[i] = (int) (b[i] + d[i]);
			N1[i] = (int) (a[i] + b[i]);
			N0[i] = (int) (c[i] + d[i]);
			ls[i] = Math.max(0, N1[i] - M0[i]);
			us[i] = Math.min(M1[i], N1[i]);
			x = (int) (x + xs[i]);
			l = l + ls[i];
			u = u + us[i];
		}
		
		int dimC2 = 0;
		for (int i = 0; i < a.length; i++)
			dimC2 += (us[i] - ls[i]);
		double maxuldiff = 0.0;
		
		double Cs[][] = new double[a.length][];
		for (int i = 0; i < a.length; i++)
		{
			if (us[i] - ls[i] > maxuldiff)
				maxuldiff = us[i] - ls[i];
			Cs[i] = new double[us[i] - ls[i] + 1];
			for (int s = ls[i]; s <= us[i]; s++)
				Cs[i][s - ls[i]] = choosey(M1[i], s) * choosey(M0[i], N1[i] - s);
		}

		double C2[] = new double[dimC2 + 1];
		for (int j = 0; j <= us[0] - ls[0]; j++)
			for (int k = 0; k <= us[1] - ls[1]; k++)
				C2[j + k] = C2[j + k] + Cs[0][j] * Cs[1][k];
		
		int bound = 0;
		double Y[] = new double[u - l + 1];
		for (int i = 2; i < a.length; i++)
		{
			for (int j = 0; j <= u - l; j++)
			{
				Y[j] = C2[j];
				C2[j] = 0.0;
			}
			bound = 0;
			for (int j = 0; j < i; j++)
				bound = bound + (us[i] - ls[i]);
			for (int j = 0; j <= u - l; j++)
				for (int k = 0; k <= us[i] - ls[i]; k++)
					if (j + k <= u - l)
						C2[j + k] = C2[j + k] + Y[j] * Cs[i][k];
		}
		
		double R = ((double) Math.round(MLE * 10000) / 10000) - 0.0002;
		double Ds[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			Ds[i] = 0.0;
		double d2 = 1.0;
		double FR = 1.0;
		
//		while (Math.abs(FR - 0.025) > 0.0002)
		while (FR > 0.025)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 0.1;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= x - l; i++)
			{
				double adder = C2[i];
				for (int j = 0; j < Ds.length; j++)
					adder /= Ds[j];
				adder *= Math.pow(R,  i + l);
				FR += adder;
//				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
			}
		}
		double aa = R - 1.0;
		double bb = R + 0.5;
		double precision = 0.000001;
		while (bb - aa > precision)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R =(bb + aa) / 2.0;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= x - l; i++)
			{
				double adder = C2[i];
				for (int j = 0; j < Ds.length; j++)
					adder /= Ds[j];
				adder *= Math.pow(R,  i + l);
				FR += adder;
//				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
			}
			if (FR > 0.025)
				aa = R;
			else
				bb = R;
		}
		/*
		R -= 0.0002;
		while (Math.abs(FR - 0.025) > 0.00002)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 0.00002;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= x - l; i++)
				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
		}
		R -= 0.00002;
		while (Math.abs(FR - 0.025) > 0.00001)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 0.00001;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= x - l; i++)
				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
		}
*/
		return R;
	}
	
	public static double exactorln(double a[], double b[], double c[], double d[])
	{
		int M1[] = new int[a.length];
		int M0[] = new int[a.length];
		int N1[] = new int[a.length];
		int N0[] = new int[a.length];
		int ls[] = new int[a.length];
		int us[] = new int[a.length];
		double xs[] = a;
		int x = 0;
		int l = 0;
		int u = 0;

		for (int i = 0; i < a.length; i++)
		{
			M1[i] = (int) (a[i] + c[i]);
			M0[i] = (int) (b[i] + d[i]);
			N1[i] = (int) (a[i] + b[i]);
			N0[i] = (int) (c[i] + d[i]);
			ls[i] = Math.max(0, N1[i] - M0[i]);
			us[i] = Math.min(M1[i], N1[i]);
			x = (int) (x + xs[i]);
			l = l + ls[i];
			u = u + us[i];
		}
		
		int dimC2 = 0;
		for (int i = 0; i < a.length; i++)
			dimC2 += (us[i] - ls[i]);
		double maxuldiff = 0.0;
		
		double Cs[][] = new double[a.length][];
		for (int i = 0; i < a.length; i++)
		{
			if (us[i] - ls[i] > maxuldiff)
				maxuldiff = us[i] - ls[i];
			Cs[i] = new double[us[i] - ls[i] + 1];
			for (int s = ls[i]; s <= us[i]; s++)
				Cs[i][s - ls[i]] = choosey(M1[i], s) * choosey(M0[i], N1[i] - s);
		}

		double C2[] = new double[dimC2 + 1];
		for (int j = 0; j <= us[0] - ls[0]; j++)
			for (int k = 0; k <= us[1] - ls[1]; k++)
				C2[j + k] = C2[j + k] + Cs[0][j] * Cs[1][k];
		
		int bound = 0;
		double Y[] = new double[u - l + 1];
		for (int i = 2; i < a.length; i++)
		{
			for (int j = 0; j <= u - l; j++)
			{
				Y[j] = C2[j];
				C2[j] = 0.0;
			}
			bound = 0;
			for (int j = 0; j < i; j++)
				bound = bound + (us[i] - ls[i]);
			for (int j = 0; j <= u - l; j++)
				for (int k = 0; k <= us[i] - ls[i]; k++)
					if (j + k <= u - l)
						C2[j + k] = C2[j + k] + Y[j] * Cs[i][k];
		}
		
		double R = 0.0;
		double Ds[] = new double[a.length];
		for (int i = 0; i < a.length; i++)
			Ds[i] = 0.0;
		double d2 = 1.0;
		double FR = 1.0;
		
//		while (Math.abs(FR - 0.975) > 0.0002)
		while (FR > 0.975)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 1;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= (x - 1) - l; i++)
			{
				double adder = C2[i];
				for (int j = 0; j < Ds.length; j++)
					adder /= Ds[j];
				adder *= Math.pow(R,  i + l);
				FR += adder;
//				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
			}
		}
		double aa = R - 1.0;
		double bb = R + 0.5;
		double precision = 0.000001;
		while (bb - aa > precision)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R =(bb + aa) / 2.0;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= (x - 1) - l; i++)
			{
				double adder = C2[i];
				for (int j = 0; j < Ds.length; j++)
					adder /= Ds[j];
				adder *= Math.pow(R,  i + l);
				FR += adder;
//				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
			}
			if (FR > 0.975)
				aa = R;
			else
				bb = R;
		}
		/*
		R -= 0.0002;
		while (Math.abs(FR - 0.975) > 0.00002)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 0.00002;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= (x - 1) - l; i++)
				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
		}
		R -= 0.00002;
		while (Math.abs(FR - 0.975) > 0.00001)
		{
			for (int i = 0; i < a.length; i++)
				Ds[i] = 0.0;
			d2 = 1.0;
			FR = 0.0;
			R += 0.00001;
			for (int j = 0; j < a.length; j++)
			{
				for (int i = 0; i <= us[j] - ls[j]; i++)
					Ds[j] = Ds[j] + Cs[j][i] * Math.pow(R, ls[j] + i);
				d2 = d2 * Ds[j];
			}
			for (int i = 0; i <= (x - 1) - l; i++)
				FR = FR + (C2[i] * Math.pow(R,  i + l)) / d2;
		}
*/
		return R;
	}
	
	public static double choosey(double chooa, double choob)
	{
		double ccccc = chooa - choob;
		if (choob < chooa / 2)
			choob = ccccc;
		double choosey = 1.0;
		for (int i = (int) choob + 1; i <= (int) chooa; i++)
			choosey = (choosey * i) / (chooa - (i - 1));
		
		return choosey;
	}
	
	public static double ComputeOddsRatio(double a[], double b[], double c[], double d[])
	{
		double numerator = 0.0;
		double denominator = 0.0;
		
		for (int i = 0; i < a.length; i++)
		{
			numerator = numerator + (a[i] * d[i]) / (a[i] + b[i] + c[i] + d[i]);
			denominator = denominator + (b[i] * c[i]) / (a[i] + b[i] + c[i] + d[i]);
		}
		
		return numerator / denominator;
	}
	
	public static double ComputedRR(double a[], double b[], double c[], double d[])
	{
		double numerator = 0.0;
		double denominator = 0.0;
		
		for (int i = 0; i < a.length; i++)
		{
			numerator = numerator + (a[i] * (c[i] + d[i])) / (a[i] + b[i] + c[i] + d[i]);
			denominator = denominator + ((a[i] + b[i]) * c[i]) / (a[i] + b[i] + c[i] + d[i]);
		}
		
		return numerator / denominator;
	}

	public static double ComputeUnChisq(double a[], double b[], double c[], double d[])
	{
		double p[] = new double[2];
		
		for (int i = 0; i < a.length; i++)
		{
            p[0] = p[0] + (a[i] * d[i] - b[i] * c[i]) / (a[i] + b[i] + c[i] + d[i]);
            p[1] = p[1] + ((a[i] + b[i]) * (c[i] + d[i]) * (a[i] + c[i]) * (b[i] + d[i])) / (((a[i] + b[i] + c[i] + d[i]) - 1) * (a[i] + b[i] + c[i] + d[i]) * (a[i] + b[i] + c[i] + d[i]));
		}
		
		return (p[0] * p[0]) / p[1];
	}

	public static double ComputeCorrChisq(double a[], double b[], double c[], double d[])
	{
		double p[] = new double[2];
		
		for (int i = 0; i < a.length; i++)
		{
            p[0] = p[0] + (a[i] * d[i] - b[i] * c[i]) / (a[i] + b[i] + c[i] + d[i]);
            p[1] = p[1] + ((a[i] + b[i]) * (c[i] + d[i]) * (a[i] + c[i]) * (b[i] + d[i])) / (((a[i] + b[i] + c[i] + d[i]) - 1) * (a[i] + b[i] + c[i] + d[i]) * (a[i] + b[i] + c[i] + d[i]));
		}
		
		return ((Math.abs(p[0]) - 0.5) * (Math.abs(p[0]) - 0.5)) / p[1];
	}

	public static double ZSElnOR(double a[], double b[], double c[], double d[])
	{
		double p[] = new double[5];
		
		for (int i = 0; i < a.length; i++)
		{
            p[0] = p[0] + ((a[i] + d[i]) / (a[i] + b[i] + c[i] + d[i])) * (a[i] * d[i] / (a[i] + b[i] + c[i] + d[i]));
            p[1] = p[1] + ((a[i] + d[i]) / (a[i] + b[i] + c[i] + d[i])) * (b[i] * c[i] / (a[i] + b[i] + c[i] + d[i])) + ((b[i] + c[i]) / (a[i] + b[i] + c[i] + d[i])) * (a[i] * d[i] / (a[i] + b[i] + c[i] + d[i]));
            p[2] = p[2] + ((b[i] + c[i]) / (a[i] + b[i] + c[i] + d[i])) * (b[i] * c[i] / (a[i] + b[i] + c[i] + d[i]));
            p[3] = p[3] + (a[i] * d[i] / (a[i] + b[i] + c[i] + d[i]));
            p[4] = p[4] + (b[i] * c[i] / (a[i] + b[i] + c[i] + d[i]));
		}
		
		return 1.96 * Math.sqrt(p[0] / (2 * p[3] * p[3]) + p[1] / (2 * p[3] * p[4]) + p[2] / (2 * p[4] * p[4]));
	}
	
	public static double ZSElnRR(double a[], double b[], double c[], double d[])
	{
		double p[] = new double[3];
		
		for (int i = 0; i < a.length; i++)
		{
            p[0] = p[0] + ((a[i] + c[i]) * (a[i] + b[i]) * (c[i] + d[i]) - a[i] * c[i] * (a[i] + b[i] + c[i] + d[i])) / ((a[i] + b[i] + c[i] + d[i]) * (a[i] + b[i] + c[i] + d[i]));
            p[1] = p[1] + (a[i] * (c[i] + d[i])) / (a[i] + b[i] + c[i] + d[i]);
            p[2] = p[2] + (c[i] * (a[i] + b[i])) / (a[i] + b[i] + c[i] + d[i]);
		}
		
		return 1.96 * Math.sqrt(p[0] / (p[1] * p[2]));
	}
}

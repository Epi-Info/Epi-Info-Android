package gov.cdc.epiinfo.statcalc.calculators;

import gov.cdc.epiinfo.statcalc.calculators.SharedResources;

public class MatchedPair {
	public double[] oddsRatio(double x, double y)
	{
		if (x == 0.0 || y == 0.0)
		{
			x += 0.5;
			y += 0.5;
		}
		
		double[] values = {0.0, 0.0, 0.0};
		
		values[0] = x / y;
		values[1] = Math.exp(Math.log(values[0]) - 1.96 * Math.pow(1 / x + 1 / y, 0.5));
		values[2] = Math.exp(Math.log(values[0]) + 1.96 * Math.pow(1 / x + 1 / y, 0.5));
		
		return values;
	}

	public double[] mcNemarUncorrected(double x, double y)
	{
		if (x == 0.0 || y == 0.0)
		{
			x += 0.5;
			y += 0.5;
		}
		
		double[] values = {0.0, 0.0};
		
		values[0] = Math.pow(x - y, 2.0) / (x + y);
		values[1] = SharedResources.PValFromChiSq(values[0], 1.0);
		
		return values;
	}
	
	public double[] mcNemarCorrected(double x, double y)
	{
		if (x == 0.0 || y == 0.0)
		{
			x += 0.5;
			y += 0.5;
		}
		
		double[] values = {0.0, 0.0};
		
		values[0] = Math.pow(Math.abs(x - y) - 1.0, 2.0) / (x + y);
		values[1] = SharedResources.PValFromChiSq(values[0], 1.0);
		
		return values;
	}
	
	public double[] fisherLimits(double x, double y)
	{
		if (x == 0.0 || y == 0.0)
		{
			x += 0.5;
			y += 0.5;
		}
		
		double[] values = new double[2];
		
		values[0] = Double.NaN;
		values[1] = Double.NaN;

		if (y > 0.5)
		{
			double F = 0.0;
			double p = 1.0;
			//The first loop quickly establishes a small range in which F must fall.
			while (p > 0.975)
			{
				F += 1;
				p = SharedResources.PFromF(F, 2.0 * x, 2.0 * (y + 1.0));
			}
			//The second loop quickly hones F's precision
			double aa = F - 1.0;
			double bb = F;
			double precision = 0.0000001;
			while (bb - aa > precision)
			{
				F = (bb + aa) / 2.0;
				if (SharedResources.PFromF(F, 2.0 * x, 2.0 * (y + 1.0)) > 0.975)
					aa = F;
				else
					bb = F;
			}
			values[0] =  x * F / (y + 1.0);
			while (p > 0.025)
			{
				F += 1;
	            p = SharedResources.PFromF(F, 2.0 * (x + 1.0), 2.0 * y);
			}
			aa = F - 1.0;
			bb = F + 0.5;
			while (bb - aa > precision)
			{
				F = (bb + aa) / 2.0;
				if (SharedResources.PFromF(F, 2.0 * (x + 1.0), 2.0 * y) > 0.025)
						aa = F;
				else
					bb = F;
			}
			values[1] =  (x + 1.0) * F / y;
		}
		return values;
	}
	
	public double[] fisherExactTests(double x, double y)
	{
		if (x == 0.0 || y == 0.0)
		{
			x += 0.5;
			y += 0.5;
		}
		
		double[] values = new double[2];
		
		values[0] = Double.NaN;
		values[1] = Double.NaN;
		
		if (y > 0.5)
		{
			double[] oneTailFish = oneFish(Math.floor(x), Math.floor(y));
			values[0] = oneTailFish[0];
			values[1] = twoFish(oneTailFish[0], oneTailFish[1], Math.floor(x), Math.floor(y));
		}
		
		return values;
	}
	
	public String disclaimer(double x, double y)
	{
		if (x + y >= 20)
			return "There are " + (int)(x + y) + " discordant pairs.  Because this number " +
				"is >= 20, the McNemar test may be used.";
		else
			return "There are " + (int)(x + y) + " discordant pairs.  Because this number " +
				"is fewer than 20, it is recommended that only the exact results be used.";
	}
	
	public String adjustmentDisclaimer(double x, double y)
	{
		if (x == 0 || y == 0)
			return "0.5 has been added to each cell for calculations.";
		return "";
	}

    private double[] oneFish(double x, double y)
    {
        double lowfish = 0.0;
        double upfish = 0.0;
        for (int k = 0; k <= (int) x; k++)
        {
            lowfish += SharedResources.choosey(x + y, (double) k) * Math.pow(0.5, x + y);
        }
        for (int k = (int) x; k <= (int) (x + y); k++)
        {
            upfish += SharedResources.choosey(x + y, (double) k) * Math.pow(0.5, x + y);
        }
        double lowup = 0.0;
        if (upfish < lowfish) lowup = 1.0;
        double[] values = new double[2];
        values[0] = Math.min(lowfish, upfish);
        values[1] = lowup;
        return values;
    }

    private double twoFish(double oneFish, double lowup, double x, double y)
    {
        double p = oneFish;
        double xp = SharedResources.choosey(x + y, x) * Math.pow(0.5, x + y);
        if (lowup == 1.0)
        {
            for (int k = 0; k < (int) x; k++)
            {
                double tempP = SharedResources.choosey(x + y, (double) k) * Math.pow(0.5, x + y);
                if (tempP < xp)
                    p += tempP;
            }
        }
        else
        {
            for (int k = (int) x; k < (int) (x + y); k++)
            {
                double tempP = SharedResources.choosey(x + y, (double) k) * Math.pow(0.5, x + y);
                if (tempP < xp)
                    p += tempP;
            }
        }
        return p;
    }
}

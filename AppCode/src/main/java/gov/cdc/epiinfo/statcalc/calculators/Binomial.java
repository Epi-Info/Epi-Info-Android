package gov.cdc.epiinfo.statcalc.calculators;

import gov.cdc.epiinfo.statcalc.calculators.SharedResources;

public class Binomial {
	public double[] CalculateBinomialProbabilities(int observations, int numerator, double pp)
	{
		double p = Math.floor(1000 * pp) / 1000;
		double[] probabilities = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		
		probabilities[2] = SharedResources.chooseyForLep(observations, numerator, p);
		
		for (int i = 1; i <= numerator; i++)
		{
			probabilities[0] += SharedResources.chooseyForLep(observations, numerator - i, p);
		}
		for (int i = 0; i <= numerator; i++)
		{
			probabilities[1] += SharedResources.chooseyForLep(observations, numerator - i, p);
		}
		for (int i = 0; i <= observations - numerator; i++)
		{
			probabilities[3] += SharedResources.chooseyForLep(observations, numerator + i, p);
		}
		for (int i = 1; i <= observations - numerator; i++)
		{
			probabilities[4] += SharedResources.chooseyForLep(observations, numerator + i, p);
		}
		
		probabilities[5] = Math.min(2 * Math.min(probabilities[1], probabilities[3]), 1.0);
		
		return probabilities;
	}
	
	public int[] CalculateBinomialCI(int observations, int numerator)
	{
		int[] ci = {0, observations};
		
		double pp = 0.0;
		double pvalue = 0.0;
		double LowerDesiredPValue = 0.025;
		double UpperDesiredPValue = 0.975;
		
		if (numerator > 0)
		{
			while (Math.abs(pvalue - LowerDesiredPValue) > 0.0001)
			{
				pp += 0.00001;
				pvalue = SharedResources.ribetafunction(pp, numerator, observations - numerator + 1);
			}
			ci[0] = Math.round((float)pp * (float)observations);
		}
		
		pp = 1.0;
		pvalue = 0.0;
		
		if (numerator < observations)
		{
			while (Math.abs(pvalue - UpperDesiredPValue) > 0.0001)
			{
				pp -= 0.00001;
				pvalue = SharedResources.ribetafunction(pp, numerator + 1, observations - numerator);
			}
			ci[1] = Math.round((float)pp * (float)observations);
		}
		
		return ci;
	}
	
	public int CalculateBinomialLowerLimit(int observations, int numerator)
	{
		int ci = 0;
		double a = 0.0;
		double b = 1.0;
		double pp = 0.0;
		double precision = 0.00001;
		
		try
		{
			while (b - a > precision)
			{
				pp = (a + b) / 2.0;
				if (SharedResources.ribetafunction(pp, numerator, observations - numerator + 1, true) > 0.025)
					b = pp;
				else
					a = pp;
			}
			ci = Math.round((float)pp * (float)observations);
		}
		catch (Exception e) { }
		
		return ci;
	}
	
	public int CalculateBinomialLowerLimit(int observations, int numerator, boolean bv)
	{
		int ci = 0;
		
		double pp = 0.0;
		double pvalue = 0.0;
		double LowerDesiredPValue = 0.025;
		
		if (numerator > 0)
		{
			while (Math.abs(pvalue - LowerDesiredPValue) > 0.0001)
			{
				pp += 0.00001;
				pvalue = SharedResources.ribetafunction(pp, numerator, observations - numerator + 1, true);
			}
			ci = Math.round((float)pp * (float)observations);
		}
		
		return ci;
	}
	
	public int CalculateBinomialUpperLimit(int observations, int numerator)
	{
		int ci = observations;
		double a = 0.0;
		double b = 1.0;
		double pp = 0.0;
		double precision = 0.00001;
		
		try
		{
			while (b - a > precision)
			{
				pp = (a + b) / 2.0;
				if (SharedResources.ribetafunction(pp, numerator + 1, observations - numerator, true) > 0.975)
					b = pp;
				else
					a = pp;
			}
			ci = Math.round((float)pp * (float)observations);
		}
		catch (Exception e) { }
		
		return ci;
	}
	
	public int CalculateBinomialUpperLimit(int observations, int numerator, boolean bv)
	{
		int ci = observations;
		
		double pp = 1.0;
		double pvalue = 0.0;
		double UpperDesiredPValue = 0.975;
		
		if (numerator < observations)
		{
			while (Math.abs(pvalue - UpperDesiredPValue) > 0.0001)
			{
				pp -= 0.00001;
				pvalue = SharedResources.ribetafunction(pp, numerator + 1, observations - numerator, true);
			}
			ci = Math.round((float)pp * (float)observations);
		}
		
		return ci;
	}
}

package gov.cdc.epiinfo.statcalc.calculators;

public class Poisson {
	public double[] CalculatePoisson(int observed, double expected)
	{
		double denominator = 0.0;
		double[] probabilities = {0.0, 0.0, 0.0, 0.0, 0.0};
		
		if (observed > 0)
		{
			probabilities[0] = Math.exp(-expected);
			for (int j = 1; j < observed; j++)
			{
				denominator = 1.0;
				for (int i = 0; i < j; i++)
				{
					denominator *= (j - i);
				}
				probabilities[0] += (Math.pow(expected, (double)j) * Math.exp(-expected)) / denominator;
			}
		}
		
		probabilities[1] = Math.exp(-expected);
		for (int j = 1; j <= observed; j++)
		{
			denominator = 1.0;
			for (int i = 0; i < j; i++)
			{
				denominator *= (j - i);
			}
			probabilities[1] += (Math.pow(expected, (double)j) * Math.exp(-expected)) / denominator;
		}
		
		denominator = 1.0;
		for (int i = 0; i < observed; i++)
		{
			denominator *= (observed - i);
		}
		
		probabilities[2] = (Math.pow(expected, (double)observed) * Math.exp(-expected)) / denominator;
		
		probabilities[3] = 1 - probabilities[0];
		
		probabilities[4] = 1 - probabilities[1];
		
		return probabilities;
	}
}

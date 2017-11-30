package gov.cdc.epiinfo.statcalc.etc;

public class ChiSquareResult {

	private double[] oddsRatios;
	private double chi;
	private double pValue;
	
	public double[] GetOddsRatios()
	{
		return oddsRatios;
	}
	
	public void SetOddsRatios(double[] oddsRatios)
	{
		this.oddsRatios = oddsRatios;
	}
	
	public double GetChi()
	{
		return chi;
	}
	
	public void SetChi(double chi)
	{
		this.chi = chi;
	}
	
	public double GetPValue()
	{
		return pValue;
	}
	
	public void SetPValue(double pValue)
	{
		this.pValue = pValue;
	}
	
}

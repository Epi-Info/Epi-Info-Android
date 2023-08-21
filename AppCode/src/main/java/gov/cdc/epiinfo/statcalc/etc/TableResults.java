package gov.cdc.epiinfo.statcalc.etc;

public class TableResults {
	
	private double uncorrectedChi;
	private double uncorrectedP;
	private double mantelChi;
	private double mantelP;
	private double yatesChi;
	private double yatesP;
	private double risk;
	private double riskLo;
	private double riskHi;
	private double odds;
	private double oddsLo;
	private double oddsHi;
	
	public double GetUncorrectedChi()
	{
		return uncorrectedChi;
	}
	
	public void SetUncorrectedChi(double val)
	{
		this.uncorrectedChi = val;
	}
	
	public double GetUncorrectedP()
	{
		return uncorrectedP;
	}
	
	public void SetUncorrectedP(double val)
	{
		this.uncorrectedP = val;
	}
	
	public double GetMantelChi()
	{
		return mantelChi;
	}
	
	public void SetMantelChi(double val)
	{
		this.mantelChi = val;
	}
	
	public double GetMantelP()
	{
		return mantelP;
	}
	
	public void SetMantelP(double val)
	{
		this.mantelP = val;
	}
	
	public double GetYatesChi()
	{
		return yatesChi;
	}
	
	public void SetYatesChi(double val)
	{
		this.yatesChi = val;
	}
	
	public double GetYatesP()
	{
		return yatesP;
	}
	
	public void SetYatesP(double val)
	{
		this.yatesP = val;
	}
	
	public double GetRisk()
	{
		return risk;
	}
	
	public void SetRisk(double val)
	{
		this.risk = val;
	}
	
	public double GetRiskLo()
	{
		return riskLo;
	}
	
	public void SetRiskLo(double val)
	{
		this.riskLo = val;
	}
	
	public double GetRiskHi()
	{
		return riskHi;
	}
	
	public void SetRiskHi(double val)
	{
		this.riskHi = val;
	}
	
	public double GetOdds()
	{
		return odds;
	}
	
	public void SetOdds(double val)
	{
		this.odds = val;
	}
	
	public double GetOddsLo()
	{
		return oddsLo;
	}
	
	public void SetOddsLo(double val)
	{
		this.oddsLo = val;
	}
	
	public double GetOddsHi()
	{
		return oddsHi;
	}
	
	public void SetOddsHi(double val)
	{
		this.oddsHi = val;
	}
	
}

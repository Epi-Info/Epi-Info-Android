package gov.cdc.epiinfo.statcalc.etc;

public class CCResult {
	
	private int kelseyCases;
	private int kelseyControls;
	private int fleissCases;
	private int fleissControls;
	private int fleissCCCases;
	private int fleissCCControls;
	
	public CCResult(int kelseyCases, int kelseyControls, int fleissCases, int fleissControls, int fleissCCCases, int fleissCCControls)
	{
		this.kelseyCases = kelseyCases;
		this.kelseyControls = kelseyControls;
		this.fleissCases = fleissCases;
		this.fleissControls = fleissControls;
		this.fleissCCCases = fleissCCCases;
		this.fleissCCControls = fleissCCControls;
	}
	
	public int GetKelseyCases()
	{
		return this.kelseyCases;
	}
	
	public int GetKelseyControls()
	{
		return this.kelseyControls;
	}
	
	public int GetFleissCases()
	{
		return this.fleissCases;
	}
	
	public int GetFleissControls()
	{
		return this.fleissControls;
	}
	
	public int GetFleissCCCases()
	{
		return this.fleissCCCases;
	}
	
	public int GetFleissCCControls()
	{
		return this.fleissCCControls;
	}

}

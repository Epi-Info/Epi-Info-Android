package gov.cdc.epiinfo.statcalc.calculators;

import gov.cdc.epiinfo.statcalc.etc.Ref;
import gov.cdc.epiinfo.statcalc.etc.TableResults;

public class TwoByTwo {
    private double[] polyN;
    private double[] polyD;
    private int degN;
    private int degD;
    private double value;
    private int sumA;
    private int minSumA;
    private int maxSumA;
    private Table[] Tables;

    public double pchisq(double q, double df, boolean numonly)
    {
        double df2 = df / 2.0;
        double q2 = q / 2.0;
        int nn = 5;
        double tk = 0;
        double CFL;
        double CFU;
        double prob;
        if (q <= 0 || df <= 0)
            return -1;
        if (q < df)
        {
            tk = q2 * (1.0 - nn - df2) / (df2 + 2.0 * nn - 1.0 + nn * q2 / (df2 + 2.0 * nn));
            for (int kk = nn - 1; kk > 1; kk--)
                tk = q2 * (1.0 - kk - df2) / (df2 + 2.0 * kk - 1.0 + kk * q2 / (df2 + 2.0 * kk + tk));
            CFL = 1.0 - q2 / (df2 + 1.0 + q2 / (df2 + 2.0 + tk));
            prob = Math.exp(df2 * Math.log(q2) - q2 - lngamma(df2 + 1) - Math.log(CFL));
        }
        else
        {
            tk = (nn - df2) / (q2 + nn);
            for (int kk = nn - 1; kk > 1; kk--)
                tk = (kk - df2) / (q2 + kk / (1.0 + tk));
            CFU = 1.0 + (1.0 - df2) / (q2 + 1.0 / (1.0 + tk));
            prob = 1 - Math.exp((df2 - 1.0) * Math.log(q2) - q2 - lngamma(df2) - Math.log(CFU));
        }
        prob = 1 - prob;

        return prob;
    }

    public static double lngamma(double c)
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

    public class Nums
    {
        public double x1;
        public double x0;
        public double f1;
        public double f0;
        public int error;
    }

    public class Table
    {
        public int a;
        public int b;
        public int c;
        public int d;
        public int freq;
        public int m1;
        public int n1;
        public int n0;
        public boolean informative;
    }

    public int PolyStratCC(Ref<Table> refTable, Ref<double[]> refPolyDi)
    {
    	Table table = refTable.get();
    	double[] polyDi = refPolyDi.get();
    	//(ref Table table, ref double[] polyDi)
    	
        int i;
        int minA, maxA, aa, bb, cc, dd;

        int degDi = 0;
        if (table.informative)
        {
            minA = Math.max(0, table.m1 - table.n0);
            maxA = Math.min(table.m1, table.n1);
            degDi = (int)Math.round((double)(maxA - minA));
            polyDi = new double[degDi + 1];
            polyDi[0] = 1.0;
            aa = minA;
            bb = table.m1 - minA + 1;
            cc = table.n1 - minA + 1;
            dd = table.n0 - table.m1 + minA;
            for (i = 1; i <= degDi; i++)
            {
                polyDi[i] = polyDi[i - 1] * ((bb - i) / (double)(aa + i)) * ((cc - i) / (double)(dd + i));
            }
        }
        refTable.set(table);
        refPolyDi.set(polyDi);
        return degDi;
    }

    public double Comb(double y, double x)
    {
        int i;
        double f;

        f = 1.0;
        for (i = 1; i <= Math.round(Math.min(x, y - x)); i++)
        {
            f = f * y / (double)i;
            y = y - 1.0;
        }
        return f;
    }

    public int PolyMatchCC(Ref<Table> refTable, Ref<double[]> refPolyEi)
    {
    	//(ref Table table, ref double[] polyEi)
    	Table table = refTable.get();
    	double[] polyEi = refPolyEi.get();
    	
        double c0, c1;
        int degEi = 0;
        polyEi[0] = 1.0;
        if (table.informative)
        {

            c0 = (Comb(table.n1, 0) * Comb(table.n0, table.m1));
            c1 = (Comb(table.n1, 1) * Comb(table.n0, table.m1 - 1));
            //BinomialExpansion(c0, c1, table.freq, ref polyEi, degEi);
            Ref<double[]> refPolyEi2 = new Ref<double[]>(polyEi);
            
            BinomialExpansion(c0, c1, table.freq, refPolyEi2, degEi);
            
            polyEi = refPolyEi2.get();
        }
        
        refTable.set(table);
        refPolyEi.set(polyEi);
        return polyEi.length - 1;
    }

    public int PolyStratPT(Ref<Table> refTable, Ref<double[]> refPolyDi)
    {
    	//ref Table table, ref double[] polyDi
    	Table table = refTable.get();
    	double[] polyDi = refPolyDi.get();
    	
        int degDi = 0;
        polyDi[0] = 1.0;
        if (table.informative)
        {
        	//BinomialExpansion((table.n0 / table.n1), 1.0, (int)Math.round((double)table.m1), ref polyDi, degDi);
        	Ref<double[]> refPolyDi2 = new Ref<double[]>(polyDi);
        	
            BinomialExpansion((table.n0 / table.n1), 1.0, (int)Math.round((double)table.m1), refPolyDi2, degDi);
            
            polyDi = refPolyDi2.get();
        }
        
        refTable.set(table);
        refPolyDi.set(polyDi);
        return polyDi.length - 1;
    }

    public int MultPoly(Ref<double[]> refP1, Ref<double[]> refP2, int deg1, int deg2, Ref<double[]> refP3)
    {
    	//(ref double[] p1, ref double[] p2, int deg1, int deg2, ref double[] p3)
    	double[] p1 = refP1.get();
    	double[] p2 = refP2.get();
    	double[] p3 = refP3.get();
    	
        int i, j;
        int deg3 = deg1 + deg2;
        for (i = 0; i <= deg3; i++)
        {
            p3[i] = 0.0;
        }
        for (i = 0; i <= deg1; i++)
        {
            for (j = 0; j <= deg2; j++)
            {
                p3[i + j] = p1[i] * p2[j] + p3[i + j];
            }
        }
        
        refP1.set(p1);
        refP2.set(p2);
        refP3.set(p3);
        return deg3;
    }

    public void BinomialExpansion(double c0, double c1, int f, Ref<double[]> refP, int degp)
    {
    	//(double c0, double c1, int f, ref double[] p, int degp)
    	double[] p = refP.get();
    	
        int i;
        degp = f;
        p[degp] = Math.pow(c1, degp);
        for (i = degp - 1; i >= 0; i--)
        {
            p[i] = p[i + 1] * c0 * (i + 1) / (c1 * (degp - i));
        }
        
        refP.set(p);
    }

    public void CalcPoly(int DataType)
    {
        double[] poly1;
        double[] poly2;
        int i, j, deg1, deg2;
        Table CurTable;

        CurTable = this.Tables[0];
        if (DataType == 1)
        {
        	//this.degD = PolyStratCC(ref CurTable, ref this.polyD);
        	Ref<Table> refCurTable = new Ref<Table>(CurTable);
        	Ref<double[]> refPolyD = new Ref<double[]>(this.polyD);
        	        	
            this.degD = PolyStratCC(refCurTable, refPolyD);
            
            CurTable = refCurTable.get();
            this.polyD = refPolyD.get();
        }
        else if (DataType == 2)
        {
        	//this.degD = PolyMatchCC(ref CurTable, ref this.polyD);
        	Ref<Table> refCurTable = new Ref<Table>(CurTable);
        	Ref<double[]> refPolyD = new Ref<double[]>(this.polyD);
        	
            this.degD = PolyMatchCC(refCurTable, refPolyD);
            
            CurTable = refCurTable.get();
            this.polyD = refPolyD.get();
        }
        else if (DataType == 3)
        {
        	//this.degD = PolyStratPT(ref CurTable, ref this.polyD);
        	Ref<Table> refCurTable = new Ref<Table>(CurTable);
        	Ref<double[]> refPolyD = new Ref<double[]>(this.polyD);
        	
            this.degD = PolyStratPT(refCurTable, refPolyD);
            
            CurTable = refCurTable.get();
            this.polyD = refPolyD.get();
        }
        else
        {
            return;
        }
        for (i = 1; i < this.Tables.length; i++)
        {
            CurTable = this.Tables[i];
            if (CurTable.informative)
            {
                deg1 = this.degD;
                poly1 = new double[this.polyD.length];
                poly2 = new double[this.polyD.length];
                for (j = 0; j <= deg1; j++)
                {
                    poly1[j] = this.polyD[j];
                }
                if (DataType == 1)
                {
                	//deg2 = PolyStratCC(ref CurTable, ref poly2);
                	Ref<Table> refCurTable = new Ref<Table>(CurTable);
                	Ref<double[]> refPoly2 = new Ref<double[]>(poly2);
                	
                    deg2 = PolyStratCC(refCurTable, refPoly2);
                    
                    CurTable = refCurTable.get();
                    poly2 = refPoly2.get();
                }
                else if (DataType == 2)
                {
                	//deg2 = PolyMatchCC(ref CurTable, ref poly2);
                	Ref<Table> refCurTable = new Ref<Table>(CurTable);
                	Ref<double[]> refPoly2 = new Ref<double[]>(poly2);
                	
                    deg2 = PolyMatchCC(refCurTable, refPoly2);
                    
                    CurTable = refCurTable.get();
                    poly2 = refPoly2.get();
                }
                else if (DataType == 3)
                {
                	//deg2 = PolyStratPT(ref CurTable, ref poly2);
                	Ref<Table> refCurTable = new Ref<Table>(CurTable);
                	Ref<double[]> refPoly2 = new Ref<double[]>(poly2);
                	
                    deg2 = PolyStratPT(refCurTable, refPoly2);
                    
                    CurTable = refCurTable.get();
                    poly2 = refPoly2.get();
                }
                else
                {
                    return;
                }
                //this.degD = MultPoly(ref poly1, ref poly2, deg1, deg2, ref this.polyD);
                Ref<double[]> refPoly1 = new Ref<double[]>(poly1);
                Ref<double[]> refPoly2 = new Ref<double[]>(poly2);
                Ref<double[]> refPolyD = new Ref<double[]>(this.polyD);
                
                this.degD = MultPoly(refPoly1, refPoly2, deg1, deg2, refPolyD);
                
                poly1 = refPoly1.get();
                poly2 = refPoly2.get();
                this.polyD = refPolyD.get();
            }
        }
    }

    public Nums BracketRoot(double approx)
    {
        int iter;
        Nums nums = new Nums();

        iter = 0;
        double x1 = Math.max(0.5, approx);
        double x0 = 0;
        double f0 = Func(x0);
        double f1 = Func(x1);
        while ((f1 * f0) > 0.0 && (iter < 10000))
        {
            iter = iter + 1;
            x0 = x1;
            f0 = f1;
            x1 = x1 * 1.5 * iter;
            f1 = Func(x1);
        }
        nums.x1 = x1;
        nums.x0 = x0;
        nums.f1 = f1;
        nums.f0 = f0;
        return nums;
    }

    public double EvalPoly(double[] c, int degC, double r)
    {
        double y=0;
        if (r == 0)
        {
            y = c[0];
        }
        else if (r <= 1)
        {
            y = c[degC];
            if (r < 1)
            {
                for (int i = (degC - 1); i >= 0; i -= 1)
                {
                    y = y * (r) + c[i];
                }
            }
            else
            {
                for (int i = (degC - 1); i >= 0; i -= 1)
                {
                    y = y + c[i];
                }
            }
        }
        else if (r > 1)
        {
            y = c[0];
            r = 1 / r;
            for (int i = 1; i <= degC; i++)
            {
                y = y * (r) + c[i];
            }
        }
        return y;
    }

    public double Func(double r)
    {
        double numer;
        double denom;
        numer = EvalPoly(polyN, degN, r);
        denom = EvalPoly(polyD, degD, r);
        if (r <= 1)
        {
            return numer / denom - (value);
        }
        else
        {
            return (numer / Math.pow(r, (degD - degN)) / denom) - value;
        }
    }

    private int CheckData(int DataType)
    {
        int i;
        Table curTbl;
        int error = 0;
        this.sumA = 0;
        this.minSumA = 0;
        this.maxSumA = 0;
        for (i = 0; i < this.Tables.length; i++)
        {
            curTbl = this.Tables[i];

            if (curTbl.informative)
            {
                this.sumA += (int)Math.round((double)(curTbl.a * curTbl.freq));
                if ((DataType == 1) || (DataType == 2))
                {
                    this.minSumA += (int)Math.round((double)Math.max(0, curTbl.m1 - curTbl.n0) * curTbl.freq);
                    this.maxSumA += (int)Math.round((double)Math.min(curTbl.m1, curTbl.n1) * curTbl.freq);
                }
                else if (DataType == 3)
                {
                    this.minSumA = 0;
                    this.maxSumA += (int)Math.round((double)curTbl.m1 * curTbl.freq);
                }
            }
            if ((this.maxSumA - this.minSumA > 100000))
            {
                error = 1;
            }
            else if (this.minSumA == this.maxSumA)
            {
                error = 2;
            }
            else if ((DataType == 2) && (curTbl.a > 1))
            {
                error = 3;
            }
        }

        return error;
    }

    public double GetExactLim(int yy, int yn, int ny, int nn, boolean pbLower, boolean pbFisher, double pvApprox, double pnConfLevel)
    {
        Tables = new Table[1];
        Table table = new Table();
        table.a = yy;//7
        table.b = ny;//9
        table.c = yn;//8
        table.d = nn;//11
        table.freq = 1;
        table.m1 = yy+ny;//16;
        table.n1 = yy+yn;//15;
        table.n0 = ny+nn;//20;
        table.informative = true;
        Tables[0] = table;
        CheckData(1);
        CalcPoly(1);

        double pvLimit;

        if (pbLower)
        {
            value = 0.5 * (1 + pnConfLevel);
        }
        else
        {
            value = 0.5 * (1 - pnConfLevel);
        }
        if (pbLower && pbFisher)
        {
            degN = sumA - minSumA - 1;
        }
        else
        {
            degN = sumA - minSumA;
        }
        polyN = new double[degN + 1];
        for (int i = 0; i <= degN; i++)
        {
            polyN[i] = polyD[i];
        }
        if (!pbFisher)
        {
            polyN[degN] = (0.5) * polyD[degN];
        }
        pvLimit = Converge(pvApprox);
        return pvLimit;
    }

    public double Converge(double approx)
    {
        Nums nums;
        double rootc;
        boolean error = false;
        nums = BracketRoot(approx);
        rootc = Zero(nums);
        if (!error)
        {
            return rootc;
        }
        else
        {
        	return Double.NaN;
        }
    }

    public double Zero(Nums nums)
    {
        double root;
        boolean found = false;
        double x2;
        double f2;
        double swap;
        double iter;
        double x0 = nums.x0;
        double x1 = nums.x1;
        double f0 = nums.f0;
        double f1 = nums.f1;
        int error = 0;
        iter = 0;
        if (Math.abs(f0) < Math.abs(f1))
        {
            swap = x0;
            x0 = x1;
            x1 = swap;
            swap = f0;
            f0 = f1;
            f1 = swap;
        }
        found = (f1 == 0);
        if (!found && ((f0 * f1) > 0))
        {
            error = 1;
        }
        while (!(found) && (iter < 10000) && (error == 0))
        {
            iter = iter + 1;
            x2 = x1 - f1 * (x1 - x0) / (f1 - f0);
            f2 = Func(x2);
            if (f1 * f2 < 0)
            {
                x0 = x1;
                f0 = f1;
            }
            else
            {
                f0 = f0 * f1 / (f1 + f2);
            }
            x1 = x2;
            f1 = f2;
            found = ((Math.abs(x1 - x0) < (Math.abs(x1) * 0.0000001)) || (f1 == 0));
        }
        root = x1;
        if (!(found) && (iter >= 10000) && (error == 0))
        {
            error = 2;
        }
        nums.error = error;
        return root;
    }

    public double CalcCmle(double approx)
    {
        double cmle = 0;
        if ((this.minSumA < this.sumA) && (this.sumA < this.maxSumA))
        {
            cmle = GetCmle(approx);
        }
        else if ((this.sumA == this.minSumA))
        {
            cmle = 0;
        }
        else if ((this.sumA == this.maxSumA))
        {
            cmle = Double.POSITIVE_INFINITY;
        }
        return cmle;
    }

    public double GetCmle(double approx)
    {
        int i;
        double cmle;
        this.value = this.sumA;
        
        this.degN = this.degD;
        this.polyN = new double[this.degD + 1];
        
        for (i = 0; i <= this.degN; i++)
        {
            this.polyN[i] = (this.minSumA + i) * this.polyD[i];
        }

        cmle = Converge(approx);
        return cmle;
    }

    public TableResults AnalyzeSingleTable(double yy, double ny, double yn, double nn)
    {
        double r1 = yy + ny;
        double r2 = yn + nn;
        double c1 = yy + yn;
        double c2 = ny + nn;
        double t = yy + ny + yn + nn;

        double cs = (t * Math.pow((yy * nn) - (ny * yn), 2.0)) / (c1 * c2 * r1 * r2);
        double csc = (t * Math.pow(Math.max(0.0, Math.abs((yy * nn) - (ny * yn)) - t / 2.0), 2.0)) / (c1 * c2 * r1 * r2);
        double mhcs = ((t - 1.0) * Math.pow((yy * nn) - (ny * yn), 2)) / (c1 * c2 * r1 * r2);

        double pcs = pchisq(cs, 1, true); //Chi square, 1 degree of freedom, number only
        double pcsc = pchisq(csc, 1, true);
        double pmhcs = pchisq(mhcs, 1, true);

        /*double od;
        if (ny * yn == 0)
        {
            od = Double.NEGATIVE_INFINITY;
        }
        else
        {
            od = (yy * nn) / ((ny * yn) * 1.0);
        }*/

        //cscrit['95%']=3.841

        //double od_lo = od * Math.exp(-Math.sqrt(3.841) * Math.sqrt(1.0 / a + 1.0 / b + 1.0 / c + 1.0 / (d * 1.0)));
        //double od_hi = od * Math.exp(Math.sqrt(3.841) * Math.sqrt(1.0 / a + 1.0 / b + 1.0 / c + 1.0 / (d * 1.0)));

        //double midPmin = GetExactLim((int)yy,(int)yn,(int)ny,(int)nn,true, false, 1, 0.95);
        //double midPmax = GetExactLim((int)yy,(int)yn,(int)ny,(int)nn,false, false, 1, 0.95);
        double fisherMin = 0;
        double fisherMax = 0;
        double odds = 0;
        
        	try
        	{
        		fisherMin = GetExactLim((int)yy,(int)yn,(int)ny,(int)nn,true, true, 1, 0.95);
        		fisherMax = GetExactLim((int)yy,(int)yn,(int)ny,(int)nn,false, true, 1, 0.95);
        		odds = CalcCmle(1);
        	}
        	catch (Exception ex)
        	{
        		String exception = ex.toString();
        		exception += "";
        	}
        
        double rr = (yy / (c1 * 1.0)) / (ny / (c2 * 1.0));
        double rr_lo = rr * Math.exp(-Math.sqrt(3.841) * Math.sqrt(((1 - yy / c1) / (c1 * yy / c1)) + ((1 - ny / c2) / (c2 * ny / c2))));
        double rr_hi = rr * Math.exp(Math.sqrt(3.841) * Math.sqrt(((1 - yy / c1) / (c1 * yy / c1)) + ((1 - ny / c2) / (c2 * ny / c2))));

        TableResults results = new TableResults();
        results.SetUncorrectedChi(cs);
        results.SetUncorrectedP(pcs);
        results.SetMantelChi(mhcs);
        results.SetMantelP(pmhcs);
        results.SetYatesChi(csc);
        results.SetYatesP(pcsc);
        results.SetRisk(rr);
        results.SetRiskHi(rr_hi);
        results.SetRiskLo(rr_lo);
        results.SetOdds(odds);
        results.SetOddsHi(fisherMax);
        results.SetOddsLo(fisherMin);
        
        return results;
    }
}

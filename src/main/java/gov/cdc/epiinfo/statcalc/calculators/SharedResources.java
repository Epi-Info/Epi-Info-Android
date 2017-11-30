package gov.cdc.epiinfo.statcalc.calculators;


public class SharedResources
{
    public static Double PValFromChiSq(double _x, double _df)
    {
        Double _j;
        Double _k;
        Double _l;
        Double _m;
        Double _pi = 3.1416;

        if (_x < 0.000000001 || _df < 1.0)
            return 1.0;

        Double _rr = 1.0;
        int _ii = (int) (_df * 1);

        while (_ii >= 2)
        {
            _rr = _rr * (Double) (_ii * 1.0);
            _ii = _ii - 2;
        }

        _k = Math.exp(Math.floor((_df + 1.0) * 0.5) * Math.log(Math.abs(_x)) - _x * 0.5) / _rr;

        if (_k < 0.00001)
            return 0.0;

        if (Math.floor(_df * 0.5) == _df * 0.5)
            _j = 1.0;
        else
            _j = Math.sqrt(2.0 / _x / _pi);

        _l = 1.0;
        _m = 1.0;

        if (!Double.isNaN(_x) && !Double.isInfinite(_x))
        {
            while (_m >= 0.00000001)
            {
                _df = _df + 2.0;
                _m = _m * _x / _df;
                _l = _l + _m;
            }
        }
        Double PfX2 = 1 - _j * _k * _l;
        return PfX2;
    }

    public static Double PFromZ(Double _z)
    {
        Double PFZ = Double.NaN;
        int LTONE = 7;
        int UTZERO = 12;
        Double CON = 1.28;

        Double _x = Math.abs(_z);
        if (_x > UTZERO)
        {
            if (_z < 0)
                PFZ = 1.0;
            else
                PFZ = 0.0;
            return PFZ;
        }
        Double _y = Math.pow(_z, 2.0) / 2;
        if (_x > CON)
        {
            PFZ = _x - 0.151679116635 + 5.29330324926 / (_x + 4.8385912808 - 15.1508972451 / (_x + 0.742380924027 + 30.789933034 / (_x + 3.99019417011)));
            PFZ = _x + 0.000398064794 + 1.986158381364 / PFZ;
            PFZ = _x - 0.000000038052 + 1.00000615302 / PFZ;
            PFZ = 0.398942280385 * Math.exp(-_y) / PFZ;
        }
        else
        {
            PFZ = _y / (_y + 5.75885480458 - 29.8213557808 / (_y + 2.624331121679 + 48.6959930692 / (_y + 5.92885724438)));
            PFZ = 0.398942280444 - 0.399903438504 * PFZ;
            PFZ = 0.5 - _x * PFZ;
        }
        if (_z < 0)
            PFZ = 1 - PFZ;
        return PFZ;
    }

    public static Double PFromT(Double _t, int _df)
    {
        Double PFT = Double.NaN;
        Double g1 = 0.3183098862;
        int MaxInt = 1000;
        int ddf = 0;
        int F = 0;
        int i = 0;
        Double _a = Double.NaN;
        Double _b = Double.NaN;
        Double _c = Double.NaN;
        Double _s = Double.NaN;
        Double _p = Double.NaN;

        _t = Math.abs(_t);
        if (_df < MaxInt)
        {
            ddf = _df;
            _a = _t / Math.sqrt(ddf);
            _b = ddf / (ddf + Math.pow(_t, 2.0));
            i = ddf % 2;
            _s = 1.0;
            _c = 1.0;
            F = 2 + i;
            while (F <= ddf - 2)
            {
                _c = _c * _b * (F - 1) / F;
                _s = _s + _c;
                F = F + 2;
            }
            if (i <= 0)
            {
                _p = 0.5 - _a * Math.sqrt(_b) * _s / 2;
            }
            else
            {
                _p = 0.5 - (_a * _b * _s + Math.atan(_a)) * g1;
            }
            if (_p < 0.0)
                _p = 0.0;
            if (_p > 1.0)
                _p = 1.0;
            PFT = _p;
        }
        else
            PFT = PFromZ(_t);
        return PFT;
    }

    public static Double PFromF(double F, double df1, double df2)
    {
        double PFF = 0.0;
        double ACU = 0.000000001;
        double xx = 0.0;
        double pp = 0.0;
        double qq = 0.0;
        Boolean index;
        
        if (F == 0.0)
            return 0.0;
        if (F < 0 || df1 < 1 || df2 < 1)
            return 0.0;
        if (df1 == 1)
        {
            PFF = PFromT(Math.sqrt(F), (int)df2) * 2;
            return PFF;
        }

        double x = df1 * F / (df2 + df1 * F);
        double P = df1 / 2;
        double q = df2 / 2;
        double psq = P + q;
        double cx = 1 - x;
        
        if (P >= x * psq)
        {
            xx = x;
            pp = P;
            qq = q;
            index = false;
        }
        else
        {
            xx = cx;
            cx = x;
            pp = q;
            qq = P;
            index = true;
        }

        double term = 1.0;
        double ai = 1.0;
        double b = 1.0;
        double ns = pp + cx * psq;
        double rx = xx / cx;
        double term1 = 1.0;
        
        double temp = qq - ai;
        
        if (ns == 0.0)
            rx = xx;

        term = term / (pp + ai) * temp * rx;
        
        while (Math.abs(term) <= term1)
        {
        	b = b + term;
        	temp = Math.abs(term);
        	term1 = temp;
        	if (temp > ACU || temp > ACU * b)
        	{
        		ai = ai + 1;
        		ns = ns - 1;
        		if (ns >= 0)
        		{
        			temp = qq - ai;
        			if (ns == 0)
        				rx = xx;
        			term = term / (pp + ai) * temp * rx;
        		}
        		else
        		{
        			temp = psq;
            		psq = psq + 1;
            		term = term / (pp + ai) * temp * rx;
        		}
        	}
        	else
        		break;
        }
        
        double beta = algama(P) + algama(q) - algama(P + q);
        temp = (pp * Math.log(xx) + (qq - 1) * Math.log(cx) - beta) - Math.log(pp);

        if (temp > -70)
            b = b * Math.exp(temp);
        else
            b = 0.0;

        if (index)
            b = 1 - b;

        PFF = 1 - b;
        
        return PFF;
    }

    public static Double ZFromP(Double P)
    {
        Double P0 = -0.322232431088;
        Double P2 = -0.342242088547;
        Double P3 = -0.0204231210245;
        Double P4 = -4.53642210148E-05;
        Double Q0 = 0.099348462606;
        Double Q1 = 0.588581570495;
        Double Q2 = 0.531103462366;
        Double Q3 = 0.10353775285;
        Double Q4 = 0.0038560700634;
        Double ZFromP = 0.0;

        Double F = P;
        if (F >= 1)
            return Double.NaN;
        if (F == 0.5)
            return 0.0;
        if (F > 0.5)
            F = 1 - F;

        Double T = Math.sqrt(Math.log(1 / Math.pow(F, 2)));
        T = T + ((((T * P4 + P3) * T + P2) * T - 1) * T + P0) / ((((T * Q4 + Q3) * T + Q2) * T + Q1) * T + Q0);

        if (P > 0.5)
            ZFromP = -T;
        else
            ZFromP = T;

        return ZFromP;
    }

    public static double algama(double s)
    {
        double ag = 0.0;
        double Z = 0.0;
        double F = 0.0;
        double x = s;

        if (x < 0)
            return ag;

        if (x < 7)
        {
            F = 1.0;
            Z = x - 1;
            while (Z < 7)
            {
                Z = Z + 1;
                if (Z < 7)
                {
                    x = Z;
                    F = F * Z;
                }
            }
            x = x + 1;
            F = -Math.log(F);
        }

        Z = 1/ Math.pow(x, 2.0);

        ag = F + (x - 0.5) * Math.log(x) - x + 0.918938533204673 + (((-1.0 / 1680.0 * Z + 1.0 / 1260.0) * Z - 1.0 / 360.0) * Z + 1.0 / 12.0) / x;
        return ag;
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
	
	public static double chooseyForLep(double chooa, double choob, double ppp)
	{
		double ccccc = chooa - choob;
		double chooseyforlep = 1.0;
		double oldchoob = choob;
		if (choob < chooa / 2)
			choob = ccccc;
		for (int i = (int) choob + 1; i <= (int) chooa; i++)
			chooseyforlep = (chooseyforlep * i) / (chooa - (i - 1)) * ppp;
		chooseyforlep *= Math.pow(1 - ppp, chooa - oldchoob);
		if (oldchoob == choob)
			chooseyforlep *= Math.pow(ppp, choob - (chooa - choob));
		
		return chooseyforlep;
	}
	
	public static double ribetafunction(double p, int alpha, int beta, boolean yn)
	{
		double functionvalue = 0.0;
		
		for (int j = alpha; j < alpha + beta; j++)
			functionvalue += choosey(alpha + beta - 1, j) * Math.pow(p, j) * Math.pow(1 - p, alpha + beta - 1 - j);
		
		return functionvalue;
	}
	
	public static double ribetafunction(double p, int alpha, int beta)
	{
		double functionvalue = 0.0;
		double aa = 1.0;
		double bb = 1.0;
		double cc = 1.0;
		
		for (int i = 0; i < alpha + beta - 1; i++)
		{
			aa *= (double)((alpha + beta - 1) - i);
		}
		for (int j = alpha; j < alpha + beta; j++)
		{
			bb = 1.0;
			cc = 1.0;
			for (int i = 0; i < j; i++)
			{
				bb *= (double)(j - i);
			}
			for (int i = 0; i < alpha + beta - 1 - j; i++)
			{
				cc *= (double)((alpha + beta - 1 - j) - i);
			}
			functionvalue += (aa / (bb * cc)) * Math.pow(p, (double)j) * Math.pow((1 - p), (double)(alpha + beta - 1 - j));
		}
		
		return functionvalue;
	}
}

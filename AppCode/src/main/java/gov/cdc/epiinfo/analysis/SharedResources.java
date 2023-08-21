package gov.cdc.epiinfo.analysis;

public class SharedResources
{
    public static double PValFromChiSq(double _x, double _df)
    {
    	double _j;
    	double _k;
    	double _l;
    	double _m;
    	double _pi = 3.1416;

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

    public static double PFromT(double _t, int _df)
    {
    	double PFT = Double.NaN;
    	double g1 = 0.3183098862;
        int MaxInt = 1000;
        int ddf = 0;
        int F = 0;
        int i = 0;
        double _a = Double.NaN;
        double _b = Double.NaN;
        double _c = Double.NaN;
        double _s = Double.NaN;
        double _p = Double.NaN;

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

    public static double PFromF(double F, double df1, double df2)
    {
    	double PFF = 0.0;
        return PFF;
    }

    public static double ZFromP(double P)
    {
    	double P0 = -0.322232431088;
    	double P2 = -0.342242088547;
    	double P3 = -0.0204231210245;
    	double P4 = -4.53642210148E-05;
    	double Q0 = 0.099348462606;
    	double Q1 = 0.588581570495;
    	double Q2 = 0.531103462366;
    	double Q3 = 0.10353775285;
    	double Q4 = 0.0038560700634;
    	double ZFromP = 0.0;

    	double F = P;
        if (F >= 1)
            return Double.NaN;
        if (F == 0.5)
            return 0.0;
        if (F > 0.5)
            F = 1 - F;

        double T = Math.sqrt(Math.log(1 / Math.pow(F, 2)));
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

        ag = F + (x - 0.5) * Math.log(x) - x + 0.918938533204673 + (((-1 / 1680 * Z + 1 / 1260) * Z - 1 / 360) * Z + 1 / 12) / x;
        return ag;
    }
}

package npylm;

import java.util.*;
import npylm.Restaurant_Franchise;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.BetaDistribution;


public class Slice_Sampler {

    private BetaDistribution beta_dist;
    private GammaDistribution gamma_dist;
    private Random rand_gen;

    private double width = 0.02;

    private double max_discount = 0.999;
    private double min_discount = 0.001;
    private double max_strength = Double.MAX_VALUE;
    private double min_strength = 0.001;

    private double xl = 0.0;
    private double xr = 0.0;

    public Slice_Sampler() {
	this.beta_dist  = new BetaDistribution(1.0, 1.0);
	this.gamma_dist = new GammaDistribution(1.0, 1.0);
	this.rand_gen = new Random(0);
    }

    // i = 2 for bi, 1 for uni, 0 for zero
    public double next_discount(int i, double x0, Restaurant_Franchise rf) {
	double log_y = rf.log_like() + Math.log(this.beta_dist.density(x0)) + Math.log(rand_gen.nextDouble() + 1e-100);
	this.find_range_for_discount(i, x0, log_y, rf);

	double x = this.shrink_for_discount(i, x0, log_y, rf);
	if (x > this.max_discount) {
	    return this.max_discount;
	} else if (x < this.min_discount) {
	    return this.min_discount;
	} else {
	    return x;
	}
    }

    private void find_range_for_discount(int i, double x0, double log_y, Restaurant_Franchise rf) {
	double l = Math.max(x0 - this.width * rand_gen.nextDouble(), this.min_discount);
	double r = Math.min(l + this.width, this.max_discount);
	double w;

	w = this.width;
	while (this.log_f_for_discount(i, l, rf) > log_y) {
	    l -= w;
	    if (l < this.min_discount) {
		l = this.min_discount;
		break;
	    }
	    w *= 2.0;
	}
	w = this.width;
	while (this.log_f_for_discount(i, r, rf) > log_y) {
	    r += w;
	    if (r > this.max_discount) {
		r = this.max_discount;
		break;
	    }
	    w *= 2.0;
	}

	this.xl = l;
	this.xr = r;
    }

    private double shrink_for_discount(int i, double x0, double log_y, Restaurant_Franchise rf) {
	double x;
	while (true) {
	    x = this.xl + this.rand_gen.nextDouble() * (this.xr - this.xl);
	    if (this.log_f_for_discount(i, x, rf) > log_y && this.check_for_discount(i, x, x0, log_y, rf)) {
		break;
	    }
	    if (x < x0) {
		this.xl = x;
	    } else {
		this.xr = x;
	    }
	}
	return x;
    }

    private boolean check_for_discount(int i, double x, double x0, double log_y,
				       Restaurant_Franchise rf) {
	double l = this.xl;
	double r = this.xr;
	double m;
	boolean d;

	while ((r - l) > 1.1 * this.width) {
	    m = (l + r) * 0.5;
	    d = (x0 < m && x >= m) || (x0 >= m && x < m);
	    if (x < m) {
		r = m;
	    } else {
		l = m;
	    }
	    if (d && log_y >= this.log_f_for_discount(i, l, rf) &&
		log_y >= this.log_f_for_discount(i, r, rf)) {
		return false;
	    }
	}
	return true;
    }

    private double log_f_for_discount(int i, double x, Restaurant_Franchise rf) {
	double rf_log_like;
	if (i == 2) {
	    rf_log_like = rf.log_like(x, rf.uni_discount, rf.zero_discount,
				      rf.bi_strength, rf.uni_strength, rf.zero_strength);
	} else if (i == 1) {
	    rf_log_like = rf.log_like(rf.bi_discount, x, rf.zero_discount,
				      rf.bi_strength, rf.uni_strength, rf.zero_strength);
	} else {
	    rf_log_like = rf.log_like(rf.bi_discount, rf.uni_discount, x,
				      rf.bi_strength, rf.uni_strength, rf.zero_strength);
	}

	return rf_log_like + Math.log(this.beta_dist.density(x));
    }	

	
    // i = 2 for bi, 1 for uni, 0 for zero
    public double next_strength(int i, double x0, Restaurant_Franchise rf) {
	double log_y = rf.log_like() + Math.log(this.gamma_dist.density(x0)) + Math.log(rand_gen.nextDouble() + 1e-100);
	this.find_range_for_strength(i, x0, log_y, rf);

	double x = this.shrink_for_strength(i, x0, log_y, rf);
	if (x > this.max_strength) {
	    return this.max_strength;
	} else if (x < this.min_strength) {
	    return this.min_strength;
	} else {
	    return x;
	}
    }

    private void find_range_for_strength(int i, double x0, double log_y, Restaurant_Franchise rf) {
	double l = Math.max(x0 - this.width * rand_gen.nextDouble(), this.min_strength);
	double r = Math.min(l + this.width, this.max_strength);
	double w;

	w = this.width;
	while (this.log_f_for_strength(i, l, rf) > log_y) {
	    l -= w;
	    if (l < this.min_strength) {
		l = this.min_strength;
		break;
	    }
	    w *= 2.0;
	}
	w = this.width;
	while (this.log_f_for_strength(i, r, rf) > log_y) {
	    r += w;
	    if (r > this.max_strength) {
		r = this.max_strength;
		break;
	    }
	    w *= 2.0;
	}

	this.xl = l;
	this.xr = r;
    }

    private double shrink_for_strength(int i, double x0, double log_y, Restaurant_Franchise rf) {
	double x;
	while (true) {
	    x = this.xl + this.rand_gen.nextDouble() * (this.xr - this.xl);
	    if (this.log_f_for_strength(i, x, rf) > log_y && this.check_for_strength(i, x, x0, log_y, rf)) {
		break;
	    }
	    if (x < x0) {
		this.xl = x;
	    } else {
		this.xr = x;
	    }
	}
	return x;
    }

    private boolean check_for_strength(int i, double x, double x0, double log_y,
				       Restaurant_Franchise rf) {
	double l = this.xl;
	double r = this.xr;
	double m;
	boolean d;

	while ((r - l) > 1.1 * this.width) {
	    m = (l + r) * 0.5;
	    d = (x0 < m && x >= m) || (x0 >= m && x < m);
	    if (x < m) {
		r = m;
	    } else {
		l = m;
	    }
	    if (d && log_y >= this.log_f_for_strength(i, l, rf) &&
		log_y >= this.log_f_for_strength(i, r, rf)) {
		return false;
	    }
	}
	return true;
    }

    private double log_f_for_strength(int i, double x, Restaurant_Franchise rf) {
	double rf_log_like;
	if (i == 2) {
	    rf_log_like = rf.log_like(rf.bi_discount, rf.uni_discount, rf.zero_discount, 
				      x, rf.uni_strength, rf.zero_strength);
	} else if (i == 1) {
	    rf_log_like = rf.log_like(rf.bi_discount, rf.uni_discount, rf.zero_discount,
				      rf.bi_strength, x, rf.zero_strength);
	} else {
	    rf_log_like = rf.log_like(rf.bi_discount, rf.uni_discount, rf.zero_discount,
				      rf.bi_strength, rf.uni_strength, x);
	}

	return rf_log_like + Math.log(this.gamma_dist.density(x));
    }
}

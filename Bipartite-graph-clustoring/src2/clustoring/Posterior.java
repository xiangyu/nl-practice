package clustoring;

import java.util.*;


public class Posterior {

    // marginaling with k,l
    public List<List<Double>> semi_marginals1;
    // marginaling with m,n
    public List<List<Double>> semi_margibals2;

    pulic Posterior(Model model) {
    }

    public void updata_model(Model model) {
	this.prepare_marginals(model);
	this._update_model(model);
    }

    private void prepare_marginals(Mode model) {
    }

    private void _update_model(Model model) {
    }
}

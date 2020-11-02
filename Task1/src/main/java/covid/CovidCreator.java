package covid;

import org.opt4j.core.genotype.SelectMapGenotype;
import org.opt4j.core.problem.Creator;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Random;

public class CovidCreator
        implements Creator<SelectMapGenotype<Region, Lab>>
{
    private CovidProblem problem;

    @Inject
    public CovidCreator(CovidProblem problem)
    {
        this.problem = problem;
    }

    @Override
    public SelectMapGenotype<Region, Lab> create()
    {
        var labs = problem.getLabs();
        var regions = problem.getRegions();
        var genotype = new SelectMapGenotype<>(regions, labs);

        genotype.init(new Random());

        return genotype;
    }
}

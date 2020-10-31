package covid;

import java.util.Collections;
import javax.inject.Inject;
import org.opt4j.core.genotype.PermutationGenotype;
import org.opt4j.core.problem.Creator;

public class CovidCreator
        implements Creator<PermutationGenotype<Region>>
{
    private CovidProblem problem;

    @Inject
    public CovidCreator(CovidProblem problem)
    {
        this.problem = problem;
    }

    @Override
    public PermutationGenotype<Region> create()
    {
        var genotype = new PermutationGenotype<Region>();
        genotype.addAll(problem.getRegions());
        Collections.shuffle(genotype);
        return genotype;
    }
}

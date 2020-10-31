package covid;

import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;
import org.opt4j.core.problem.Evaluator;

public class CovidEvaluator
        implements Evaluator<ProbesDistribution>
{
    @Override
    public Objectives evaluate(ProbesDistribution phenotype)
    {

        Objectives objectives = new Objectives();
        objectives.add("distance", Sign.MIN, phenotype.size());
        return objectives;
    }

}

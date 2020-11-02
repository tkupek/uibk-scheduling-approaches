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

        double dist = 0;
        for ( var pd : phenotype )
        {

            var key = pd.getLab();
            var region = pd.getRegion();

            dist += region.getLabs()
                    .getOrDefault(key, 0);
        }

        Objectives objectives = new Objectives();
        objectives.add("distance", Sign.MIN, dist);
        return objectives;
    }

}

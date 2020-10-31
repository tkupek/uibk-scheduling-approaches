package covid;

import org.opt4j.core.problem.ProblemModule;

public class CovidProblemModule
        extends ProblemModule
{
    @Override
    protected void config()
    {
        bindProblem(CovidCreator.class, CovidDecoder.class, CovidEvaluator.class);
    }
}

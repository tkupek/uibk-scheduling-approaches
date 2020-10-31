package covid;

import java.util.Random;
import org.opt4j.core.genotype.PermutationGenotype;
import org.opt4j.core.problem.Decoder;

public class CovidDecoder
        implements Decoder<PermutationGenotype<Region>, ProbesDistribution>
{
    @Override
    public ProbesDistribution decode(PermutationGenotype<Region> genotype)
    {
        var probesDistribution = new ProbesDistribution();

        var labs = new CovidProblem().getLabs();

        Random rand = new Random();

        for ( Region region : genotype )
        {

            //select a random lab
            var randomLab = labs.get(rand.nextInt(labs.size()));

            //assign all probes to the random lab, maybe change to assign each probe to a random lab
            probesDistribution.put(randomLab, region.getProbes());

        }

        return probesDistribution;
    }
}

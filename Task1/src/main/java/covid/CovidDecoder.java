package covid;

import org.opt4j.core.genotype.SelectMapGenotype;
import org.opt4j.core.problem.Decoder;

/**
 * Opt4J decoder class to transform Covid genotype to ProbesDistribution
 */
public class CovidDecoder
        implements Decoder<SelectMapGenotype<Region, Lab>, ProbesDistribution> {
    @Override
    public ProbesDistribution decode(SelectMapGenotype<Region, Lab> genotype) {
        var probesDistribution = new ProbesDistribution();
        for (var key : genotype.getKeys()) {
            probesDistribution.add(new ProbeDistribution(genotype.getValue(key), key));
        }

        return probesDistribution;
    }
}

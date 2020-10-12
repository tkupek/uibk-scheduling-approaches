package at.uibk.dps.dsB.ex0.decoders;

import org.opt4j.core.genotype.PermutationGenotype;
import org.opt4j.core.problem.Decoder;
import org.opt4j.tutorial.salesman.SalesmanProblem.City;
import org.opt4j.tutorial.salesman.SalesmanRoute;

/**
 * The {@link Decoder} class which will be used to decode the genotypes, i.e., transform them into a representation
 * which can be processed by the evaluator.
 *
 * @author Fedor Smirnov
 */
public class MyFirstDecoder
		implements Decoder<PermutationGenotype<City>, SalesmanRoute>
{
	public SalesmanRoute decode(PermutationGenotype<City> genotype)
	{
		SalesmanRoute salesmanRoute = new SalesmanRoute();
		for ( City city : genotype )
		{
			salesmanRoute.add(city);
		}
		return salesmanRoute;
	}
}

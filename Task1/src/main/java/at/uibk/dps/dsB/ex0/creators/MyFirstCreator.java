package at.uibk.dps.dsB.ex0.creators;

import java.util.Collections;
import javax.inject.Inject;
import org.opt4j.core.genotype.PermutationGenotype;
import org.opt4j.core.problem.Creator;
import org.opt4j.tutorial.salesman.SalesmanProblem;
import org.opt4j.tutorial.salesman.SalesmanProblem.City;

/**
 * The {@link Creator} class which will be used to initialize the genotypes encoding individual problem solutions.
 *
 * @author Fedor Smirnov
 */
public class MyFirstCreator
		implements Creator<PermutationGenotype<City>>
{
	protected final SalesmanProblem problem;

	@Inject
	public MyFirstCreator(SalesmanProblem problem)
	{
		this.problem = problem;
	}

	public PermutationGenotype<City> create()
	{
		PermutationGenotype<City> genotype = new PermutationGenotype<City>();
		for ( City city : problem.getCities() )
		{
			genotype.add(city);
		}
		Collections.shuffle(genotype);
		return genotype;
	}
}

package at.uibk.dps.dsB.ex0.evaluators;

import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;
import org.opt4j.core.problem.Decoder;
import org.opt4j.core.problem.Evaluator;
import org.opt4j.tutorial.salesman.SalesmanProblem.City;
import org.opt4j.tutorial.salesman.SalesmanRoute;

/**
 * The {@link Evaluator} class which will be used to evaluate the phenotypes returned by the {@link Decoder}.
 *
 * @author Fedor Smirnov
 */
public class MyFirstEvaluator
		implements Evaluator<SalesmanRoute>
{
	public Objectives evaluate(SalesmanRoute salesmanRoute)
	{
		double dist = 0;
		for ( int i = 0; i < salesmanRoute.size(); i++ )
		{
			City one = salesmanRoute.get(i);
			City two = salesmanRoute.get((i + 1) % salesmanRoute.size());
			dist += getEuclideanDistance(one, two);
		}
		Objectives objectives = new Objectives();
		objectives.add("distance", Sign.MIN, dist);
		return objectives;
	}

	private double getEuclideanDistance(City one, City two)
	{
		final double x = one.getX() - two.getX();
		final double y = one.getY() - two.getY();
		return Math.sqrt(x * x + y * y);
	}
}

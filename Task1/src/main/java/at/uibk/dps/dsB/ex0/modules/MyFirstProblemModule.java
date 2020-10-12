package at.uibk.dps.dsB.ex0.modules;

import org.opt4j.core.problem.ProblemModule;
import org.opt4j.core.start.Constant;
import org.opt4j.tutorial.salesman.SalesmanCreator;
import org.opt4j.tutorial.salesman.SalesmanDecoder;
import org.opt4j.tutorial.salesman.SalesmanEvaluator;

/**
 * In general, Opt4J's modules are use to configure the binding of interfaces to concrete classes. In this case, we are
 * binding the Creator, Decoder, and Evaluator interfaces to the classes defined throughout the Exercise0 project.
 * You don't have to alter anything within this class.
 *
 * @author Fedor Smirnov
 */
public class MyFirstProblemModule
		extends ProblemModule
{
	@Constant(value = "size")
	protected int size = 100;

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public void config()
	{
		bindProblem(SalesmanCreator.class, SalesmanDecoder.class, SalesmanEvaluator.class);
	}
}

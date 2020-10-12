package at.uibk.dps.dsB.ex0.creators;

import org.opt4j.core.Genotype;
import org.opt4j.core.problem.Creator;

/**
 * The {@link Creator} class which will be used to initialize the genotypes
 * encoding individual problem solutions.
 * 
 * @author Fedor Smirnov
 *
 */
public class MyFirstCreator implements Creator<Genotype> {

	@Override
	public Genotype create() {
		// TODO Implement a method initializing the genotypes.
		throw new IllegalArgumentException("Creation of genotypes not yet implemented.");
	}

}

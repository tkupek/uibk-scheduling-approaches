package at.uibk.dps.dsB.ex0.decoders;

import org.opt4j.core.Genotype;
import org.opt4j.core.problem.Decoder;

/**
 * The {@link Decoder} class which will be used to decode the genotypes, i.e.,
 * transform them into a representation which can be processed by the evaluator.
 * 
 * @author Fedor Smirnov
 *
 */
public class MyFirstDecoder implements Decoder<Genotype, Object> {

	@Override
	public Object decode(Genotype genotype) {
		// TODO Implement a method which transforms a given genotype into the phenotype,
		// i.e., a representation which can be processed by the evalutor.
		throw new IllegalArgumentException("Decoding not yet implemented.");
	}
}

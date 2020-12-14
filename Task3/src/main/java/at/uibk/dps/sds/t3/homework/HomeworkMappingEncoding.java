package at.uibk.dps.sds.t3.homework;

import java.util.Set;

import org.opt4j.satdecoding.Constraint;

import net.sf.opendse.encoding.mapping.MappingConstraintGenerator;
import net.sf.opendse.encoding.variables.T;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

/**
 * 
 * Class for the implementation of the homework.
 * 
 * @author Fedor Smirnov
 */
public class HomeworkMappingEncoding implements MappingConstraintGenerator {

	@Override
	public Set<Constraint> toConstraints(Set<T> processVariables, Mappings<Task, Resource> mappings) {
		throw new IllegalStateException("Mapping encoding not yet implemented.");
	}
}

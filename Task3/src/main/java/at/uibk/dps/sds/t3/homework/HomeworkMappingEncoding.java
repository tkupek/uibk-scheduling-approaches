package at.uibk.dps.sds.t3.homework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import at.uibk.dps.sds.t3.example.ExampleMappingEncoding;
import net.sf.opendse.encoding.mapping.MappingConstraintGenerator;
import net.sf.opendse.encoding.variables.M;
import net.sf.opendse.encoding.variables.T;
import net.sf.opendse.encoding.variables.Variable;
import net.sf.opendse.encoding.variables.Variables;
import net.sf.opendse.model.*;
import net.sf.opendse.model.properties.TaskPropertyService;
import org.opt4j.satdecoding.Constraint;
import org.opt4j.satdecoding.Constraint.Operator;
import net.sf.opendse.optimization.SpecificationWrapper;

/**
 * Class for the implementation of the homework.
 *
 * @author Fedor Smirnov
 */
public class HomeworkMappingEncoding
        implements MappingConstraintGenerator {

    protected final Specification spec;

    public HomeworkMappingEncoding(SpecificationWrapper specWrapper) {
        this.spec = specWrapper.getSpecification();
    }

    @Override
    public Set<Constraint> toConstraints(Set<T> processVariables, Mappings<Task, Resource> mappings) {

        var result = new HashSet<Constraint>();
        result.addAll(addSecretTaskNotOnCloudResourceConstraints(mappings));
        result.addAll(addCapacityConstraints(mappings));
        result.addAll(addRegionConstraints(mappings));

        return result;
    }

    /**
     * Communicating secret tasks must share the same region
     *
     * @param mappings
     * @return
     */
    private Set<Constraint> addRegionConstraints(Mappings<Task, Resource> mappings) {
        var constraints = new HashSet<Constraint>();

        for (Mapping<Task, Resource> mapping : mappings) {
            var task1 = mapping.getSource();
            if (!PropertyService.isSecret(task1)) {
                continue;
            }

            for (Dependency outEdge : spec.getApplication().getOutEdges(task1)) {
                var childTask = spec.getApplication().getFunction(outEdge).getDest(outEdge);
                if(!TaskPropertyService.isCommunication(childTask)) {
                    continue;
                }

                for (Dependency outEdgeChild : spec.getApplication().getOutEdges(childTask)) {
                    var task2 = spec.getApplication().getFunction(outEdgeChild).getDest(outEdgeChild);
                    if (!PropertyService.isSecret(task2)) {
                        continue;
                    }

                    for (Mapping<Task, Resource> mapping2 : mappings.get(task2)) {
                        constraints.add(addRegionConstraint(mapping, mapping2));
                    }
                }
            }
        }

        return constraints;
    }

    private Constraint addRegionConstraint(Mapping<Task, Resource> mapping1, Mapping<Task, Resource> mapping2) {
        var constraint = new Constraint(Operator.EQ, 0);

        if (!PropertyService.getRegion(mapping1.getTarget()).equals(PropertyService.getRegion(mapping2.getTarget()))) {
            var mVar1 = Variables.varM(mapping1);
            var mVar2 = Variables.varM(mapping2);
            var andVariable = Variables.varAndVariable(mVar1, mVar2);
            constraint.add(Variables.p(andVariable));
        }

        return constraint;
    }

    /**
     * Edge resources can run a maximum of two tasks
     *
     * @param mappings
     * @return
     */
    private Set<Constraint> addCapacityConstraints(Mappings<Task, Resource> mappings) {
        var constraints = new HashSet<Constraint>();

        Set<Resource> edgeResources = new HashSet<>();
        for (Mapping<Task, Resource> mapping : mappings) {
            if (PropertyService.isEdge(mapping.getTarget())) {
                edgeResources.add(mapping.getTarget());
            }
        }
        for (Resource res : edgeResources) {
            constraints.add(addCapacityConstraint(mappings.get(res)));
        }

        return constraints;
    }

    private Constraint addCapacityConstraint(Set<Mapping<Task, Resource>> resMappings) {
        var constraint = new Constraint(Operator.LE, 2);
        for (Mapping<Task, Resource> mapping : resMappings) {
            M mVar = Variables.varM(mapping);
            constraint.add(Variables.p(mVar));
        }
        return constraint;
    }

    /**
     * Secret tasks can only run on edge resources
     *
     * @param mappings
     * @return
     */
    private Set<Constraint> addSecretTaskNotOnCloudResourceConstraints(Mappings<Task, Resource> mappings) {
        var constraints = new HashSet<Constraint>();
        for (var mapping : mappings) {
            if (PropertyService.isSecret(mapping.getSource())) {
                constraints.add(addTaskNotOnCloudResourceConstraint(mapping));
            }
        }

        return constraints;
    }

    private Constraint addTaskNotOnCloudResourceConstraint(Mapping<Task, Resource> mapping) {
        var constraint = new Constraint(Operator.EQ, 0);

        if (PropertyService.isCloud(mapping.getTarget())) {
            var mVar = Variables.varM(mapping);
            constraint.add(Variables.p(mVar));
        }

        return constraint;
    }
}

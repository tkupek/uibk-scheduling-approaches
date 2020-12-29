package at.uibk.dps.sds.t3.homework;

import java.util.*;

import net.sf.opendse.encoding.mapping.MappingConstraintGenerator;
import net.sf.opendse.encoding.variables.M;
import net.sf.opendse.encoding.variables.T;
import net.sf.opendse.encoding.variables.Variables;
import net.sf.opendse.model.*;
import net.sf.opendse.model.properties.TaskPropertyService;
import org.opt4j.satdecoding.Constraint;
import org.opt4j.satdecoding.Constraint.Operator;
import net.sf.opendse.optimization.SpecificationWrapper;
import org.opt4j.satdecoding.Term;

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
        result.addAll(encodeTaskMappingNecessityConstraints(processVariables, mappings));

        return result;
    }

    /**
     * Encodes that each task is mapped at least once.
     *
     * @param processVariables the variables encoding the activation of processes
     * @param mappings         the mappings
     * @return constraint set encoding that each task is mapped at least once
     */
    protected Set<Constraint> encodeTaskMappingNecessityConstraints(Set<T> processVariables,
                                                                    Mappings<Task, Resource> mappings) {
        Set<Constraint> result = new HashSet<>();
        for (T tVar : processVariables) {
            Set<Mapping<Task, Resource>> taskMappings = mappings.get(tVar.getTask());
            result.add(encodeTaskMappingNecessityConstraint(tVar, taskMappings));
        }
        return result;
    }

    /**
     * Encodes the constraint stating that the task encoded by the given variable is
     * mapped on at least one resource.
     * <p>
     * - T + sum (M) >= 0
     *
     * @param tVar         The encoding variable of the task
     * @param taskMappings a set of the task mappings
     * @return the constraint stating that the task encoded by the given variable is
     * mapped on at least one resource
     */
    protected Constraint encodeTaskMappingNecessityConstraint(T tVar, Set<Mapping<Task, Resource>> taskMappings) {
        Constraint result = new Constraint(Operator.GE, 0);
        result.add(new Term(-1, Variables.p(tVar))); // Here you have to pay attention to use the Variables from the encoding
        // project, not from the optimization project
        for (Mapping<Task, Resource> mapping : taskMappings) {
            M mVar = Variables.varM(mapping);
            result.add(Variables.p(mVar));
        }
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

        for (Task task : spec.getApplication().getVertices()) {
            if (!TaskPropertyService.isCommunication(task)) {
                continue;
            }

            List<Task> commTasks = new ArrayList<>();
            for (Dependency dependency : spec.getApplication().getInEdges(task)) {
                Task t = spec.getApplication().getSource(dependency);

                if (PropertyService.isSecret(t)) {
                    commTasks.add(t);
                }
            }

            for (Dependency dependency : spec.getApplication().getOutEdges(task)) {
                Task t = spec.getApplication().getDest(dependency);
                if (PropertyService.isSecret(t)) {
                    commTasks.add(t);
                }
            }

            if(commTasks.size() == 2) {
                constraints.addAll(addRegionConstraint(commTasks.get(0), commTasks.get(1), mappings));
            } else {
                throw new UnsupportedOperationException("Communications between != 2 tasks not supported in region constraint");
            }
        }

        return constraints;
    }

    private Collection<Constraint> addRegionConstraint(Task task1, Task task2, Mappings<Task, Resource> mappings) {
        List<Constraint> constraints = new ArrayList<>();

        for (Mapping<Task, Resource> mapping1 : mappings.get(task1)) {
            for (Mapping<Task, Resource> mapping2 : mappings.get(task2)) {
                if(!PropertyService.getRegion(mapping1.getTarget()).equals(PropertyService.getRegion(mapping2.getTarget()))) {
                    var constraint = new Constraint(Operator.LE, 1);
                    constraint.add(Variables.p(Variables.varM(mapping1)));
                    constraint.add(Variables.p(Variables.varM(mapping2)));
                    constraints.add(constraint);
                }
            }
        }
        return constraints;
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
        constraints.add(addSecretTaskNotOnCloudResourceConstraint(mappings));
        return constraints;
    }

    private Constraint addSecretTaskNotOnCloudResourceConstraint(Mappings<Task, Resource> mappings) {
        var constraint = new Constraint(Operator.EQ, 0);
        for (var mapping : mappings) {
            if (PropertyService.isCloud(mapping.getTarget()) && PropertyService.isSecret(mapping.getSource())) {
                var mVar = Variables.varM(mapping);
                constraint.add(Variables.p(mVar));
            }
        }
        return constraint;
    }
}

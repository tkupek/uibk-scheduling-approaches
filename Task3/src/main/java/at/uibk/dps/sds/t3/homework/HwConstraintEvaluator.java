package at.uibk.dps.sds.t3.homework;

import java.util.HashSet;
import java.util.Set;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;
import net.sf.opendse.optimization.ImplementationEvaluator;
import org.opt4j.core.Objective;
import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;

/**
 * The evaluator used to enforce the security constraints by means of additional objectives.
 *
 * @author Fedor Smirnov
 */
public class HwConstraintEvaluator
        implements ImplementationEvaluator
{

    protected final Objective numConstraintViolations = new Objective("Num Constraint Violations", Sign.MIN);

    @Override
    public Specification evaluate(Specification implementation, Objectives objectives)
    {
        objectives.add(numConstraintViolations, countConstraintViolations(implementation));
        return null;
    }

    /**
     * Counts the number of constraint violations in the given implementation
     *
     * @param implementation the given implementation
     * @return the number of constraint violations
     */
    private int countConstraintViolations(Specification implementation)
    {
        var secretTaskOnCloudResource = countSecretTasksOnCloudResource(implementation);
        var resourcesWithMoreThanTwoTasks = countResourcesWithMoreThanTwoTasks(implementation);
        var regionConstraintViolations = countRegionConstraintViolation(implementation);
        return secretTaskOnCloudResource + resourcesWithMoreThanTwoTasks + regionConstraintViolations;
    }

    private int countResourcesWithMoreThanTwoTasks(Specification implementation)
    {
        var constraintViolations = 0;
        var mappings = implementation.getMappings();
        Set<Resource> edgeResources = new HashSet<>();
        for ( Mapping<Task, Resource> mapping : mappings )
        {
            if ( PropertyService.isEdge(mapping.getTarget()) )
            {
                edgeResources.add(mapping.getTarget());
            }
        }
        for ( Resource res : edgeResources )
        {

            constraintViolations += mappings.get(res)
                    .size();
        }

        return constraintViolations;
    }

    private int countSecretTasksOnCloudResource(Specification specification)
    {
        var mappings = specification.getMappings();
        int violations = 0;
        for ( var mapping : mappings )
        {
            if ( PropertyService.isCloud(mapping.getTarget()) && PropertyService.isSecret(mapping.getSource()) )
            {
                violations++;
            }
        }
        return violations;
    }

    private int countRegionConstraintViolation(Specification specification)
    {

        var violations = 0;
        for ( Task task : specification.getApplication()
                .getVertices() )
        {
            if ( !TaskPropertyService.isCommunication(task) )
            {
                continue;
            }

            HashSet<Task> commTasks = new HashSet<>();
            for ( Dependency dependency : specification.getApplication()
                    .getInEdges(task) )
            {
                Task t = specification.getApplication()
                        .getSource(dependency);

                if ( PropertyService.isSecret(t) )
                {
                    commTasks.add(t);
                }
            }

            for ( Dependency dependency : specification.getApplication()
                    .getOutEdges(task) )
            {
                Task t = specification.getApplication()
                        .getDest(dependency);
                if ( PropertyService.isSecret(t) )
                {
                    commTasks.add(t);
                }
            }

            if ( !commTasks.isEmpty() )
            {
                violations += evaluateRegionConstraintViolations(commTasks, specification.getMappings());
            }
        }

        return violations;
    }

    private int evaluateRegionConstraintViolations(HashSet<Task> commTasks, Mappings<Task, Resource> mappings)
    {

        var violations = 0;
        String enforcedRegion = null;
        for ( Task task : commTasks )
        {
            for ( Mapping<Task, Resource> mapping : mappings.get(task) )
            {

                var region = PropertyService.getRegion(mapping.getTarget());
                if ( enforcedRegion == null )
                {
                    enforcedRegion = region;
                }

                if ( region.equals(enforcedRegion) )
                {
                    violations++;
                }
            }
        }

        return violations;
    }

    @Override
    public int getPriority()
    {
        // independent of other stuff
        return 0;
    }
}

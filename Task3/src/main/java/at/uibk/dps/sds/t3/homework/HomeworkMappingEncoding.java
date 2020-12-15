package at.uibk.dps.sds.t3.homework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.opendse.encoding.mapping.MappingConstraintGenerator;
import net.sf.opendse.encoding.variables.T;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import org.opt4j.satdecoding.Constraint;
import org.opt4j.satdecoding.Constraint.Operator;

/**
 * Class for the implementation of the homework.
 *
 * @author Fedor Smirnov
 */
public class HomeworkMappingEncoding
        implements MappingConstraintGenerator
{

    @Override
    public Set<Constraint> toConstraints(Set<T> processVariables, Mappings<Task, Resource> mappings)
    {

        var result = new HashSet<Constraint>();
        result.addAll(addSecretTaskNotOnCloudResourceConstraints(mappings));
        result.addAll(addCapacityConstraints(mappings));
        result.addAll(addRegionConstraints(mappings));
        return result;
    }

    private Set<Constraint> addRegionConstraints(Mappings<Task, Resource> mappings)
    {
        var constraints = new HashSet<Constraint>();
        var secretAndCommunicationTasks = new HashSet<Task>();
        for ( Mapping<Task, Resource> mapping : mappings )
        {
            var t = mapping.getSource();
            if ( PropertyService.isSecret(t) )
            {

            }

        }

        return constraints;
    }

    private Constraint addRegionConstraint()
    {
        return new Constraint(Operator.EQ, 1);//one region
    }

    private Set<Constraint> addCapacityConstraints(Mappings<Task, Resource> mappings)
    {
        var constraints = new HashSet<Constraint>();
        var edgeResourcesToTasks = new HashMap<Resource, List<Task>>();
        for ( Mapping<Task, Resource> mapping : mappings )
        {
            if ( PropertyService.isEdge(mapping.getTarget()) )
            {
                var tasks = edgeResourcesToTasks.getOrDefault(mapping.getTarget(), new ArrayList<>());
                tasks.add(mapping.getSource());
                edgeResourcesToTasks.put(mapping.getTarget(), tasks);
            }
        }

        for ( var resourceListEntry : edgeResourcesToTasks.entrySet() )
        {
            constraints.add(addCapacityConstraint(resourceListEntry.getKey(), resourceListEntry.getValue()));
        }

        return constraints;
    }

    private Constraint addCapacityConstraint(Resource edgeResource, List<Task> tasks)
    {
        var constraint = new Constraint(Operator.LE, 2); //max two resources

        return constraint;
    }

    //find out which task is secret and if it's ressource is a cloud resozrce
    private Set<Constraint> addSecretTaskNotOnCloudResourceConstraints(Mappings<Task, Resource> mappings)
    {
        var constraints = new HashSet<Constraint>();
        var cloudResources = new HashSet<Resource>();
        var secretTasks = new HashSet<Task>();
        for ( var m : mappings )
        {
            if ( PropertyService.isSecret(m.getSource()) )
            {
                secretTasks.add(m.getSource());
            }
            if ( PropertyService.isCloud(m.getTarget()) )
            {
                cloudResources.add(m.getTarget());
            }
        }

        for ( var secretTask : secretTasks )
        {
            constraints.add(addSecretTaskNotOnCloudResourceConstraint(secretTask, cloudResources));
        }

        return constraints;
    }

    private Constraint addSecretTaskNotOnCloudResourceConstraint(Task secretTask, Set<Resource> cloudResources)
    {
        var constraint = new Constraint(Operator.EQ, 0);//not on cloud resources

        return constraint;
    }

}

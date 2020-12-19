package at.uibk.dps.sds.t3.homework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.opendse.encoding.mapping.MappingConstraintGenerator;
import net.sf.opendse.encoding.variables.T;
import net.sf.opendse.encoding.variables.Variable;
import net.sf.opendse.encoding.variables.Variables;
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
        //result.addAll(addRegionConstraints(mappings));
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

        var rVar = Variables.varR(edgeResource);
        ArrayList<Variable> vars = new ArrayList<>();
        vars.add(rVar);
        for ( Task task : tasks )
        {
            var tVar = Variables.varT(task);
            vars.add(tVar);
        }

        var andVariable = Variables.varAndVariable(vars.toArray(Variable[]::new));

        constraint.add(Variables.p(andVariable));
        return constraint;
    }

    //find out which task is secret and if it's ressource is a cloud resozrce
    private Set<Constraint> addSecretTaskNotOnCloudResourceConstraints(Mappings<Task, Resource> mappings)
    {
        var constraints = new HashSet<Constraint>();
        for ( var m : mappings )
        {
            if ( PropertyService.isSecret(m.getSource()) && PropertyService.isCloud(m.getTarget()) )
            {
                constraints.add(addSecretTaskNotOnCloudResourceConstraint(m.getSource(), m.getTarget()));
            }

        }

        return constraints;
    }

    private Constraint addSecretTaskNotOnCloudResourceConstraint(Task secretTask, Resource cloudResources)
    {
        var constraint = new Constraint(Operator.EQ, 0);//not on cloud resources
        var rVar = Variables.varR(cloudResources);
        var tVar = Variables.varT(secretTask);
        var andVariable = Variables.varAndVariable(rVar, tVar);
        constraint.add(Variables.p(andVariable));
        System.out.println(constraint);
        return constraint;
    }

}

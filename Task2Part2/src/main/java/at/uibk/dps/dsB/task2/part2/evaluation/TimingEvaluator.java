package at.uibk.dps.dsB.task2.part2.evaluation;

import at.uibk.dps.dsB.task2.part2.properties.PropertyProvider;
import at.uibk.dps.dsB.task2.part2.properties.PropertyProviderStatic;
import at.uibk.dps.dsB.task2.part2.properties.PropertyService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
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
 * Evaluator for the makespan of the Piw3000
 *
 * @author Fedor Smirnov
 */
public class TimingEvaluator
        implements ImplementationEvaluator
{

    private final PropertyProvider propertyProvider = new PropertyProviderStatic();

    static final int priority = 0;

    private final Objective makeSpanObjective = new Objective("Makespan [TU]", Sign.MIN);

    protected final String endTimeAttribute = "End Time";
    private static final String accumulatedUsageAttribute = "Accumulated Usage";

    @Override
    public Specification evaluate(Specification implementation, Objectives objectives)
    {
        objectives.add(makeSpanObjective, calculateMakespan(implementation));
        // Implementation annotated => return the impl
        return implementation;
    }

    /**
     * Does the actual makespan calculation.
     *
     * @param implementation the orchestration under evaluation
     * @return the makespan of the orchestration
     */
    private double calculateMakespan(Specification implementation)
    {
        Application<Task, Dependency> appl = implementation.getApplication();
        var toDo = new HashSet<>(appl.getVertices());
        var startTimes = new HashMap<Task, Double>();
        var endTimes = new HashMap<Task, Double>();

        Task root = null;

        for ( Task t : appl )
        {
            if ( appl.getPredecessorCount(t) == 0 )
            {
                root = t;
                break;
            }
        }

        var schedulable = new HashSet<Task>();
        schedulable.add(root);
        startTimes.put(root, 0.0D);
        double result = 0.0D;

        label55:
        while ( !toDo.isEmpty() )
        {
            var var10 = schedulable.iterator();

            while ( true )
            {
                Task t;
                do
                {
                    if ( !var10.hasNext() )
                    {
                        toDo.removeAll(endTimes.keySet());
                        var10 = toDo.iterator();

                        while ( var10.hasNext() )
                        {
                            t = var10.next();
                            if ( this.isSchedulable(t, endTimes, appl) )
                            {
                                schedulable.add(t);
                            }
                        }
                        continue label55; //TODO remove label55
                    }

                    t = var10.next();
                } while ( !toDo.contains(t) );

                double execTime = TaskPropertyService.isProcess(t)
                        ? this.getExecTimeTask(t,
                                               implementation.getMappings())
                        : this.getExecTimeComm((Communication) t,
                                               implementation.getRoutings()
                                                       .get(t));
                double start = startTimes.get(t);
                double endTime = start + execTime;
                result = Math.max(result, endTime);
                endTimes.put(t, endTime);

                for ( Task succ : appl.getSuccessors(t) )
                {
                    this.updateSuccessorStartTime(endTime, startTimes, succ);
                }

                t.setAttribute(endTimeAttribute, endTime);
            }
        }

        return result;
    }

    private void updateSuccessorStartTime(double endTimePred, Map<Task, Double> startTimes, Task successor)
    {
        if ( !startTimes.containsKey(successor) || startTimes.get(successor) < endTimePred )
        {
            startTimes.put(successor, endTimePred);
        }

    }

    private double getExecTimeTask(Task t, Mappings<Task, Resource> mappings)
    {
        Set<Mapping<Task, Resource>> ms = mappings.get(t);
        if ( ms.size() != 1 )
        {
            throw new IllegalArgumentException("More than one mapping for task.");
        }
        else
        {
            Mapping<Task, Resource> m = ms.iterator()
                    .next();
            double processingOneTask = this.propertyProvider.getExecutionTime(m);
            int execNumber = this.getInstanceNumber(t);
            Resource bindingTarget = m.getTarget();
            this.updateUsage(bindingTarget, (double) execNumber * processingOneTask);
            if ( PropertyService.isSingleExecTask(t) )
            {
                return processingOneTask;
            }
            else
            {
                int availableRes = PropertyService.isCloudResource(bindingTarget)
                        ? this.propertyProvider.getNumberOfAvailableInstances(bindingTarget)
                        : 1;
                int runs = execNumber / availableRes + (execNumber % availableRes > 0 ? 1 : 0);
                return (double) runs * processingOneTask;
            }
        }
    }

    private void updateUsage(Resource res, double usage)
    {
        double toSet = usage;
        if ( res.getAttribute(accumulatedUsageAttribute) != null )
        {
            toSet = usage + (Double) res.getAttribute(accumulatedUsageAttribute);
        }

        res.setAttribute(accumulatedUsageAttribute, toSet);
    }

    private double getExecTimeComm(Communication comm, Architecture<Resource, Link> routing)
    {
        double transmissionTimeOneMessage = 0.0D;

        Link l;
        for ( var var5 = routing.getEdges()
                .iterator(); var5.hasNext(); transmissionTimeOneMessage += this.propertyProvider.getTransmissionTime(
                comm,
                l) )
        {
            l = var5.next();
        }

        return transmissionTimeOneMessage * (double) this.getInstanceNumber(comm);
    }

    private int getInstanceNumber(Task t)
    {
        if ( PropertyService.isSingleExecTask(t) )
        {
            return 1;
        }
        else
        {
            return PropertyService.isIterativeCars(t)
                    ? this.propertyProvider.getCarNumber()
                    : this.propertyProvider.getNumberOfPeople();
        }
    }

    private boolean isSchedulable(Task t, Map<Task, Double> endTimes, Application<Task, Dependency> appl)
    {
        var var4 = appl.getPredecessors(t)
                .iterator();

        Task pred;
        do
        {
            if ( !var4.hasNext() )
            {
                return true;
            }

            pred = var4.next();
        } while ( endTimes.containsKey(pred) );

        return false;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }
}

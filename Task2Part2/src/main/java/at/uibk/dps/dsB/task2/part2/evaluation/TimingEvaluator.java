package at.uibk.dps.dsB.task2.part2.evaluation;

import at.uibk.dps.dsB.task2.part2.properties.PropertyProvider;
import at.uibk.dps.dsB.task2.part2.properties.PropertyProviderStatic;
import at.uibk.dps.dsB.task2.part2.properties.PropertyService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

    static final int PRIORITY = 0;

    private final Objective makeSpanObjective = new Objective("Makespan [TU]", Sign.MIN);

    private static final String END_TIME_ATTRIBUTE = "End Time";
    private static final String ACCUMULATED_USAGE_ATTRIBUTE = "Accumulated Usage";

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
        var startTimes = new HashMap<Task, Double>();
        var endTimes = new HashMap<Task, Double>();

        var root = getRoot(appl);
        if ( root != null ) //to please sonar
        {
            startTimes.put(root, 0.0D);
            return recursive(root, implementation, startTimes, endTimes, appl, 0.);
        }
        return 0.;

    }

    protected double calculateMakespan_Fedor(Specification implementation)
    {
        Application<Task, Dependency> appl = implementation.getApplication();
        Set<Task> toDo = new HashSet(appl.getVertices());
        Map<Task, Double> startTimes = new HashMap();
        Map<Task, Double> endTimes = new HashMap();
        Task root = null;
        Iterator var7 = appl.iterator();

        while ( var7.hasNext() )
        {
            Task t = (Task) var7.next();
            if ( appl.getPredecessorCount(t) == 0 )
            {
                root = t;
                break;
            }
        }

        Set<Task> schedulable = new HashSet();
        schedulable.add(root);
        startTimes.put(root, 0.0D);
        double result = 0.0D;

        label55:
        while ( !toDo.isEmpty() )
        {
            Iterator var10 = schedulable.iterator();

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
                            t = (Task) var10.next();
                            if ( this.isSchedulable(t, endTimes, appl) )
                            {
                                schedulable.add(t);
                            }
                        }
                        continue label55;
                    }

                    t = (Task) var10.next();
                } while ( !toDo.contains(t) );

                double execTime = TaskPropertyService.isProcess(t)
                        ? this.getExecTimeTask(t,
                                               implementation.getMappings())
                        : this.getExecTimeComm((Communication) t,
                                               implementation.getRoutings()
                                                       .get(t));
                double start = (Double) startTimes.get(t);
                double endTime = start + execTime;
                result = Math.max(result, endTime);
                endTimes.put(t, endTime);
                Iterator var18 = appl.getSuccessors(t)
                        .iterator();

                while ( var18.hasNext() )
                {
                    Task succ = (Task) var18.next();
                    this.updateSuccessorStartTime(endTime, startTimes, succ);
                }

                t.setAttribute("End Time", endTime);
            }
        }

        return result;
    }

    //idea: recursive depth first graph traversal, calculate maximum result for all successors and from this the maximum result for the root
    private double recursive(Task t,
                             Specification implementation,
                             HashMap<Task, Double> startTimes,
                             HashMap<Task, Double> endTimes,
                             Application<Task, Dependency> appl,
                             double oldResult)
    {
        //calculate execution time based on type
        double execTime = TaskPropertyService.isProcess(t)
                ? this.getExecTimeTask(t, implementation.getMappings())
                : this.getExecTimeComm((Communication) t,
                                       implementation.getRoutings()
                                               .get(t));
        double start = startTimes.get(t);
        double endTime = start + execTime;

        endTimes.put(t, endTime);
        t.setAttribute(END_TIME_ATTRIBUTE, endTime);

        double result = 0.;
        //depth first traversal of successor, find the maximum time
        for ( Task succ : appl.getSuccessors(t) )
        {
            updateSuccessorStartTime(endTime, startTimes, succ);
            var tmp = recursive(succ, implementation, startTimes, endTimes, appl, result);
            result = Math.max(tmp, endTime);
        }

        result = Math.max(result, oldResult);

        return result;
    }

    private Task getRoot(Application<Task, Dependency> appl)
    {
        for ( Task t : appl )
        {
            if ( appl.getPredecessorCount(t) == 0 )
            {
                return t;
            }
        }
        return null;
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
            /* From readme
            You can assume that exactly one resource type is chosen for every task
             */
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
                /*
                From readme
                Due to Distopistan's backwardness, their cloud technology is not yet mature,
                so that only a limited number of instances is available for each resource type
                (it can be obtained via the PropertyProvider), restricting the potential for task parallelization.
                 */

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
        if ( res.getAttribute(ACCUMULATED_USAGE_ATTRIBUTE) != null )
        {
            toSet = usage + (Double) res.getAttribute(ACCUMULATED_USAGE_ATTRIBUTE);
        }

        res.setAttribute(ACCUMULATED_USAGE_ATTRIBUTE, toSet);
    }

    //calculate total execution time, based on the transmission time of all edges multiplied by the number of instances
    private double getExecTimeComm(Communication comm, Architecture<Resource, Link> routing)
    {
        double transmissionTimeOneMessage =

                routing.getEdges()
                        .stream()
                        .mapToDouble(e -> this.propertyProvider.getTransmissionTime(comm, e))
                        .sum();

        return transmissionTimeOneMessage * (double) this.getInstanceNumber(comm);
    }

    /* From readme
    The tasks annotated with ITERATIVE_PEOPLE or ITERATIVE_CARS
    have to be executed once for each instance of the respective
    object in the processed frame (this number can also be obtained from the PropertyProvider)
     */
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

    protected boolean isSchedulable(Task t, Map<Task, Double> endTimes, Application<Task, Dependency> appl)
    {
        Iterator var4 = appl.getPredecessors(t)
                .iterator();

        Task pred;
        do
        {
            if ( !var4.hasNext() )
            {
                return true;
            }

            pred = (Task) var4.next();
        } while ( endTimes.containsKey(pred) );

        return false;
    }

    @Override
    public int getPriority()
    {
        return PRIORITY;
    }
}

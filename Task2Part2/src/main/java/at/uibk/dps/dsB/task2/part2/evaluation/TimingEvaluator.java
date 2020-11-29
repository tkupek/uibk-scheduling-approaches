package at.uibk.dps.dsB.task2.part2.evaluation;

import at.uibk.dps.dsB.task2.part2.properties.PropertyProvider;
import at.uibk.dps.dsB.task2.part2.properties.PropertyProviderStatic;
import at.uibk.dps.dsB.task2.part2.properties.PropertyService;

import java.util.HashMap;
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
public class TimingEvaluator implements ImplementationEvaluator {

    private final PropertyProvider propertyProvider = new PropertyProviderStatic();

    static final int PRIORITY = 0;

    private final Objective makeSpanObjective = new Objective("Makespan [TU]", Sign.MIN);

    private static final String END_TIME_ATTRIBUTE = "End Time";
    private static final String ACCUMULATED_USAGE_ATTRIBUTE = "Accumulated Usage";

    @Override
    public Specification evaluate(Specification implementation, Objectives objectives) {
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
    private double calculateMakespan(Specification implementation) {
        Application<Task, Dependency> application = implementation.getApplication();
        var startTimes = new HashMap<Task, Double>();
        var endTimes = new HashMap<Task, Double>();

        var root = getApplicationRoot(application);
        if(root == null) {
            return 0D;
        }

        startTimes.put(root, 0D);
        return getExecutionTime(root, implementation, startTimes, endTimes, application, 0D);
    }

    // Recursive depth first graph traversal
    // calculate maximum result for all successors
    private double getExecutionTime(Task task, Specification implementation,
                                    HashMap<Task, Double> startTimes, HashMap<Task, Double> endTimes,
                                    Application<Task, Dependency> application, double result) {

        // TODO Tobias, finish refactoring

        //calculate execution time based on type
        double execTime = TaskPropertyService.isProcess(task)
                ? this.getTaskTime(task, implementation.getMappings())
                : this.getTransmissionTime((Communication) task,
                implementation.getRoutings()
                        .get(task));
        double start = startTimes.get(task);
        double endTime = start + execTime;

        endTimes.put(task, endTime);
        task.setAttribute(END_TIME_ATTRIBUTE, endTime);

        double newResult = 0.;
        //depth first traversal of successor, find the maximum time
        for (Task succ : application.getSuccessors(task)) {
            updateSuccessorStartTime(endTime, startTimes, succ);
            var tmp = getExecutionTime(succ, implementation, startTimes, endTimes, application, newResult);
            newResult = Math.max(tmp, endTime);
        }

        return Math.max(newResult, result);
    }

    private Task getApplicationRoot(Application<Task, Dependency> application) {
        for (Task task : application) {
            if (application.getPredecessorCount(task) == 0) {
                return task;
            }
        }
        return null;
    }

    private void updateSuccessorStartTime(double endTimePred, Map<Task, Double> startTimes, Task successor) {
        if (!startTimes.containsKey(successor) || startTimes.get(successor) < endTimePred) {
            startTimes.put(successor, endTimePred);
        }
    }

    private void updateUsage(Resource resource, double usage) {
        if (resource.getAttribute(ACCUMULATED_USAGE_ATTRIBUTE) != null) {
            usage = usage + (double) resource.getAttribute(ACCUMULATED_USAGE_ATTRIBUTE);
            resource.setAttribute(ACCUMULATED_USAGE_ATTRIBUTE, usage);
        }
    }

    private double getTaskTime(Task task, Mappings<Task, Resource> resourceMappings) {
        Set<Mapping<Task, Resource>> taskResources = resourceMappings.get(task);
        if (taskResources.size() != 1) {
            // You can assume that exactly one resource type is chosen for every task
            throw new IllegalArgumentException(String.format("More than one ressource mapping for task %s", task.getId()));
        }

        Mapping<Task, Resource> taskResource = taskResources.iterator().next();
        double executionTime = this.propertyProvider.getExecutionTime(taskResource);

        int numberOfInstances = this.getNumberOfInstances(task);
        Resource resource = taskResource.getTarget();
        this.updateUsage(resource, executionTime * numberOfInstances);

        if (PropertyService.isSingleExecTask(task)) {
            return executionTime;
        }

        // Only a limited number of instances is available for each resource type // TODO why do we need this here? should this limitation not already be included in the resourceMappings?
        var availableResources = 1;
        if(PropertyService.isCloudResource(resource)) {
            availableResources = this.propertyProvider.getNumberOfAvailableInstances(resource);
        }
        int runs = numberOfInstances / availableResources + (numberOfInstances % availableResources > 0 ? 1 : 0); // TODO this it not clear to me, please explain
        return executionTime * runs;
    }

    // Calculate transmission time based on routing and number of instances
    private double getTransmissionTime(Communication comm, Architecture<Resource, Link> routing) {
        double transmissionTimeOneMessage = routing
                .getEdges()
                .stream()
                .mapToDouble(e -> this.propertyProvider.getTransmissionTime(comm, e))
                .sum();

        return transmissionTimeOneMessage * this.getNumberOfInstances(comm);
    }

    // Tasks annotated with ITERATIVE_PEOPLE or ITERATIVE_CARS
    // have to be executed once for each instance of the respective object in the processed frame
    private int getNumberOfInstances(Task t) {
        if (PropertyService.isIterativeCars(t)) {
            return this.propertyProvider.getCarNumber();
        }
        if (PropertyService.isIterativePeople(t)) {
            return this.propertyProvider.getNumberOfPeople();
        }

        return 1;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}

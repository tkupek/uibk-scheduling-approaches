package at.uibk.dps.dsB.task4.evaluation;

import at.uibk.dps.dsB.task4.properties.PropertyProviderDynamic;
import at.uibk.dps.dsB.task4.properties.PropertyService;
import java.util.HashMap;
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

	protected final PropertyProviderDynamic propertyProvider = new PropertyProviderDynamic();

	protected static final int priority = 0;

	protected final Objective makeSpanObjective = new Objective("Makespan [TU]", Sign.MIN);
	private static final String END_TIME_ATTRIBUTE = "End Time";
	static final String ACCUMULATED_USAGE_ATTRIBUTE = "Accumulated Usage";

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
		Application<Task, Dependency> application = implementation.getApplication();
		var startTimes = new HashMap<Task, Double>();
		var endTimes = new HashMap<Task, Double>();

		var root = getApplicationRoot(application);
		if ( root == null )
		{
			return 0D;
		}

		startTimes.put(root, 0D);
		endTimes = getEndTimes(application, root, implementation, startTimes, endTimes);
		return endTimes.values()
				.stream()
				.mapToDouble(e -> e)
				.max()
				.orElse(0D);
	}

	/**
	 * Recursive depth first graph traversal calculate end times for all nodes in the graph
	 */
	private HashMap<Task, Double> getEndTimes(Application<Task, Dependency> application,
											  Task task,
											  Specification implementation,
											  HashMap<Task, Double> startTimes,
											  HashMap<Task, Double> endTimes)
	{
		double executionTime = 0D;
		if ( TaskPropertyService.isProcess(task) )
		{
			executionTime = this.getTaskTime(task, implementation.getMappings());
		}
		if ( TaskPropertyService.isCommunication(task) )
		{
			executionTime = this.getTransmissionTime((Communication) task,
													 implementation.getRoutings()
															 .get(task));
		}
		double endTime = startTimes.get(task) + executionTime;
		endTimes.put(task, endTime);
		task.setAttribute(END_TIME_ATTRIBUTE, endTime);

		// Go through all successors and add calculate end times
		for ( Task successor : application.getSuccessors(task) )
		{
			startTimes.put(successor, endTime);
			endTimes = getEndTimes(application, successor, implementation, startTimes, endTimes);
		}

		return endTimes;
	}

	private Task getApplicationRoot(Application<Task, Dependency> application)
	{
		for ( Task task : application )
		{
			if ( application.getPredecessorCount(task) == 0 )
			{
				return task;
			}
		}
		return null;
	}

	/**
	 * Update Accumulated Usage Attribute, used by the costs evaluator
	 */
	private void updateUsage(Resource resource, double usage)
	{
		if ( resource.getAttribute(ACCUMULATED_USAGE_ATTRIBUTE) != null )
		{
			usage = usage + (double) resource.getAttribute(ACCUMULATED_USAGE_ATTRIBUTE);

		}
		resource.setAttribute(ACCUMULATED_USAGE_ATTRIBUTE, usage);
	}

	private double getTaskTime(Task task, Mappings<Task, Resource> resourceMappings)
	{
		Set<Mapping<Task, Resource>> taskResources = resourceMappings.get(task);
		if ( taskResources.size() != 1 )
		{
			// You can assume that exactly one resource type is chosen for every task
			throw new IllegalArgumentException("More than one ressource mapping for task " + task.getId());
		}

		Mapping<Task, Resource> taskResource = taskResources.iterator()
				.next();
		double executionTime = this.propertyProvider.getExecutionTime(taskResource);

		int numberOfInstances = this.getNumberOfInstances(task);
		Resource resource = taskResource.getTarget();
		updateUsage(resource, executionTime * numberOfInstances);

		if ( PropertyService.isSingleExecTask(task) )
		{
			return executionTime;
		}

		// There might be multiple instances available for this task
		var availableResources = 1;
		if ( PropertyService.isCloudResource(resource) )
		{
			availableResources = this.propertyProvider.getNumberOfAvailableInstances(resource);
		}
		long runs = (long) Math.ceil((double) numberOfInstances / (double) availableResources);
		return executionTime * runs;
	}

	/**
	 * Calculate transmission time based on routing and number of instances
	 */
	private double getTransmissionTime(Communication comm, Architecture<Resource, Link> routing)
	{
		double transmissionTimeOneMessage = routing.getEdges()
				.stream()
				.mapToDouble(e -> this.propertyProvider.getTransmissionTime(comm, e))
				.sum();

		return transmissionTimeOneMessage * this.getNumberOfInstances(comm);
	}

	/**
	 * Tasks annotated with ITERATIVE_PEOPLE or ITERATIVE_CARS have to be executed once for each instance of the
	 * respective object in the processed frame
	 */
	private int getNumberOfInstances(Task t)
	{
		if ( PropertyService.isIterativeCars(t) )
		{
			return this.propertyProvider.getCarNumber();
		}
		if ( PropertyService.isIterativePeople(t) )
		{
			return this.propertyProvider.getNumberOfPeople();
		}

		return 1;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}
}

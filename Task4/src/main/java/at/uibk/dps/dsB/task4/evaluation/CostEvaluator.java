package at.uibk.dps.dsB.task4.evaluation;

import at.uibk.dps.dsB.task4.properties.PropertyService;

import java.util.concurrent.atomic.AtomicReference;

import net.sf.opendse.model.Specification;
import net.sf.opendse.optimization.ImplementationEvaluator;
import org.opt4j.core.Objective;
import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;

/**
 * The {@link CostEvaluator} is used to calculate the costs of different
 * orchestrations of the PIW3000.
 *
 * @author Fedor Smirnov
 */
public class CostEvaluator implements ImplementationEvaluator {

    protected final Objective costObjective = new Objective("Costs [FreeRep Libra]", Sign.MIN);

    @Override
    public Specification evaluate(Specification implementation, Objectives objectives) {
        double costs = calculateImplementationCost(implementation);
        objectives.add(costObjective, costs);
        // No changes to the implementation => return null
        return null;
    }

    /**
     * Does the actual cost calculation
     *
     * @param implementation the solution which is being evaluated
     * @return the cost of the implementation
     */
    protected double calculateImplementationCost(Specification implementation) {
        // calculate resource costs
        AtomicReference<Double> resCosts = new AtomicReference<>(0.0D);
        implementation.getArchitecture()
                .forEach(res -> {
                    if (PropertyService.isCloudResource(res)) {
                        double costRate = PropertyService.getResourceCosts(res);
                        double accumulatedTime = res.getAttribute(TimingEvaluator.ACCUMULATED_USAGE_ATTRIBUTE);
                        var costsByTime = costRate * accumulatedTime;
                        resCosts.updateAndGet(v -> (v + costsByTime));

                    } else {
                        resCosts.updateAndGet(v -> (v + PropertyService.getResourceCosts(res)));
                    }
                });

        // calculate link costs
        double linkCosts = implementation.getArchitecture()
                .getEdges()
                .stream()
                .mapToDouble(PropertyService::getLinkCost)
                .sum();

        return resCosts.get() + linkCosts;
    }

    @Override
    public int getPriority() {
        // To be executed after the timing evaluator
        return TimingEvaluator.priority + 1;
    }
}

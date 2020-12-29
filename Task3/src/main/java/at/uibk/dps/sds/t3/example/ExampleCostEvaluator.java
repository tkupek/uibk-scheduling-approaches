package at.uibk.dps.sds.t3.example;

import net.sf.opendse.model.*;
import net.sf.opendse.optimization.ImplementationEvaluator;
import org.opt4j.core.Objective;
import org.opt4j.core.Objective.Sign;
import org.opt4j.core.Objectives;

/**
 * Primitive cost evaluator for the example.
 *
 * @author Fedor Smirnov
 */
public class ExampleCostEvaluator implements ImplementationEvaluator {

    protected final Objective costObjective = new Objective("Costs [$]", Sign.MIN);

    @Override
    public Specification evaluate(Specification implementation, Objectives objectives) {
        int costs = getMappingCosts(implementation.getMappings());
        objectives.add(costObjective, costs);
        // no change to the implementation
        return null;
    }

    /**
     * Returns the costs of the given implementation mappings.
     *
     * @param implMappings the implementation mappings
     * @return the costs of the given implementation mappings
     */
    protected int getMappingCosts(Mappings<Task, Resource> implMappings) {
        int result = 0;
        for (Mapping<Task, Resource> mapping : implMappings) {
            result += (int) mapping.getAttribute(ExampleSpecWrapper.costAttrName);
        }
        return result;
    }

    @Override
    public int getPriority() {
        // no relation to anything else
        return 0;
    }
}

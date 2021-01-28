package at.uibk.dps.dsB.task4.evaluation;

import org.opt4j.core.config.annotations.Required;
import org.opt4j.core.start.Constant;

import at.uibk.dps.dsB.task4.properties.PropertyProviderDynamic;
import net.sf.opendse.optimization.evaluator.EvaluatorModule;

/**
 * The {@link Liw4000EvaluatorModule} is used to include the
 * {@link CostEvaluator} and the {@link TimingEvaluator} into the optimization.
 * 
 * (no need to alter anything here)
 * 
 * @author Fedor Smirnov
 *
 */
public class Liw4000EvaluatorModule extends EvaluatorModule {

	public boolean useEstimator = true;

	@Required(property = "useEstimator", value = true)
	@Constant(value = "sampleNum", namespace = PropertyEstimator.class)
	public int sampleNumber = 100;

	public int getSampleNumber() {
		return sampleNumber;
	}

	public void setSampleNumber(int sampleNumber) {
		this.sampleNumber = sampleNumber;
	}

	public boolean isUseEstimator() {
		return useEstimator;
	}

	public void setUseEstimator(boolean useEstimator) {
		this.useEstimator = useEstimator;
	}

	@Override
	protected void config() {

		if (useEstimator) {
			bind(PropertyProviderDynamic.class).to(PropertyEstimator.class);
		}

		bindEvaluator(CostEvaluator.class);
		bindEvaluator(TimingEvaluator.class);
	}
}

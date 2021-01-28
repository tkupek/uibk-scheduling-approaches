package at.uibk.dps.dsB.task4.evaluation;

import at.uibk.dps.dsB.task4.properties.PropertyProviderDynamic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import org.apache.commons.math3.util.Pair;
import org.opt4j.core.start.Constant;

import com.google.inject.Inject;

class PropertyEstimator extends PropertyProviderDynamic {
	protected final int SAMPLES;

	private final PropertyProviderDynamic propertyProvider = new PropertyProviderDynamic();

	private Integer carNumber;
	private Integer numberOfPeople;
	private final Map<String, Integer> nrOfAvailableInstancesPerResource = new HashMap<>();
	private final Map<String, Double> executionTimesForMappings = new HashMap<>();
	private final Map<Pair<String, String>, Double> transmissionTimesForCommunicationAndLinks = new HashMap<>();

	@Inject
	public PropertyEstimator(@Constant(value = "sampleNum", namespace = PropertyEstimator.class) int sampleNumber) {
		this.SAMPLES = sampleNumber;
	}

	public double getExecutionTime(Mapping<Task, Resource> mapping) {
		var executionTime = executionTimesForMappings.get(mapping.getId());
		if (executionTime == null) {

			var executionTimeSamples = new ArrayList<Double>(SAMPLES);
			for (int i = 0; i < SAMPLES; i++) {
				executionTimeSamples.add(propertyProvider.getExecutionTime(mapping));
			}

			executionTime = executionTimeSamples.stream().mapToDouble(cn -> cn).average().orElse(0);
			executionTimesForMappings.put(mapping.getId(), executionTime);
		}

		return executionTime;
	}

	public double getTransmissionTime(Communication comm, Link l) {
		var key = new Pair<>(comm.getId(), l.getId());
		var transmissionTime = transmissionTimesForCommunicationAndLinks.get(key);
		if (transmissionTime == null) {

			var executionTimeSamples = new ArrayList<Double>(SAMPLES);
			for (int i = 0; i < SAMPLES; i++) {
				executionTimeSamples.add(propertyProvider.getTransmissionTime(comm, l));
			}

			transmissionTime = executionTimeSamples.stream().mapToDouble(cn -> cn).average().orElse(0);
			transmissionTimesForCommunicationAndLinks.put(key, transmissionTime);
		}

		return transmissionTime;
	}

	public int getCarNumber() {
		if (carNumber == null) {

			var carNumbers = new ArrayList<Integer>(SAMPLES);
			for (int i = 0; i < SAMPLES; i++) {
				carNumbers.add(propertyProvider.getCarNumber());
			}

			carNumber = Math.toIntExact(Math.round(carNumbers.stream().mapToInt(cn -> cn).average().orElse(0)));
		}
		return carNumber;
	}

	public int getNumberOfPeople() {
		if (numberOfPeople == null) {

			var numberOfPeoples = new ArrayList<Integer>(SAMPLES);
			for (int i = 0; i < SAMPLES; i++) {
				numberOfPeoples.add(propertyProvider.getNumberOfPeople());
			}

			numberOfPeople = Math
					.toIntExact(Math.round(numberOfPeoples.stream().mapToInt(cn -> cn).average().orElse(0)));

		}
		return numberOfPeople;
	}

	public int getNumberOfAvailableInstances(Resource cloudResource) {
		var instances = nrOfAvailableInstancesPerResource.get(cloudResource.getId());
		if (instances == null) {

			var instancesSamples = new ArrayList<Integer>(SAMPLES);
			for (int i = 0; i < SAMPLES; i++) {
				instancesSamples.add(propertyProvider.getNumberOfAvailableInstances(cloudResource));
			}

			instances = Math.toIntExact(Math.round(instancesSamples.stream().mapToInt(cn -> cn).average().orElse(0)));
			nrOfAvailableInstancesPerResource.put(cloudResource.getId(), instances);

		}

		return instances;
	}

}

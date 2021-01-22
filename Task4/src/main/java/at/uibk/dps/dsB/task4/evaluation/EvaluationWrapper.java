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

class EvaluationWrapper
{
    private static EvaluationWrapper wrapper;
    private static final int SAMPLES = 100;

    private final PropertyProviderDynamic propertyProvider = new PropertyProviderDynamic();

    private Integer carNumber;
    private Integer numberOfPeople;
    private final Map<String, Integer> nrOfAvailableInstancesPerResource = new HashMap<>();
    private final Map<String, Double> executionTimesForMappings = new HashMap<>();
    private final Map<Pair<String, String>, Double> transmissionTimesForCommunicationAndLinks = new HashMap<>();

    static EvaluationWrapper getInstance()
    {
        if ( wrapper == null )
        {
            wrapper = new EvaluationWrapper();
        }

        return wrapper;
    }

    double getExecutionTime(Mapping<Task, Resource> mapping)
    {
        var executionTime = executionTimesForMappings.get(mapping.getId());
        if ( executionTime == null )
        {

            var executionTimeSamples = new ArrayList<Double>(SAMPLES);

            for ( int i = 0; i < SAMPLES; i++ )
            {
                executionTimeSamples.add(propertyProvider.getExecutionTime(mapping));
            }

            executionTime = executionTimeSamples.stream()
                    .mapToDouble(cn -> cn)
                    .average()
                    .orElse(0);
            executionTimesForMappings.put(mapping.getId(), executionTime);
        }

        return executionTime;
    }

    double getTransmissionTime(Communication comm, Link l)
    {
        var key = new Pair<>(comm.getId(), l.getId());
        var transmissionTime = transmissionTimesForCommunicationAndLinks.get(key);
        if ( transmissionTime == null )
        {

            var executionTimeSamples = new ArrayList<Double>(SAMPLES);

            for ( int i = 0; i < SAMPLES; i++ )
            {
                executionTimeSamples.add(propertyProvider.getTransmissionTime(comm, l));
            }

            transmissionTime = executionTimeSamples.stream()
                    .mapToDouble(cn -> cn)
                    .average()
                    .orElse(0);
            transmissionTimesForCommunicationAndLinks.put(key, transmissionTime);
        }

        return transmissionTime;
    }

    int getCarNumber()
    {
        if ( carNumber == null )
        {

            var carNumbers = new ArrayList<Integer>(SAMPLES);

            for ( int i = 0; i < SAMPLES; i++ )
            {
                carNumbers.add(propertyProvider.getCarNumber());
            }

            carNumber = Math.toIntExact(Math.round(carNumbers.stream()
                                                           .mapToInt(cn -> cn)
                                                           .average()
                                                           .orElse(0)));
        }
        return carNumber;
    }

    int getNumberOfPeople()
    {
        if ( numberOfPeople == null )
        {

            var numberOfPeoples = new ArrayList<Integer>(SAMPLES);

            for ( int i = 0; i < SAMPLES; i++ )
            {
                numberOfPeoples.add(propertyProvider.getNumberOfPeople());
            }

            numberOfPeople = Math.toIntExact(Math.round(numberOfPeoples.stream()
                                                                .mapToInt(cn -> cn)
                                                                .average()
                                                                .orElse(0)));

        }
        return numberOfPeople;
    }

    int getNumberOfAvailableInstances(Resource cloudResource)
    {
        var instances = nrOfAvailableInstancesPerResource.get(cloudResource.getId());
        if ( instances == null )
        {

            var instancesSamples = new ArrayList<Integer>(SAMPLES);

            for ( int i = 0; i < SAMPLES; i++ )
            {
                instancesSamples.add(propertyProvider.getNumberOfAvailableInstances(cloudResource));
            }

            instances = Math.toIntExact(Math.round(instancesSamples.stream()
                                                           .mapToInt(cn -> cn)
                                                           .average()
                                                           .orElse(0)));
            nrOfAvailableInstancesPerResource.put(cloudResource.getId(), instances);

        }

        return instances;
    }

}

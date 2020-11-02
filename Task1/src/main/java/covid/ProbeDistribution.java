package covid;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Model class for a distribution of probes, maps a region to a laboratory
 */
@AllArgsConstructor
@Getter
public class ProbeDistribution {
    private Lab lab;
    private Region region;

    @Override
    public String toString() {
        return region.getName() + ":" + lab.getName() + ":" + region.getProbes();
    }
}

package covid;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProbeDistribution
{
    private Lab lab;
    private Region region;

    @Override
    public String toString()
    {
        return region.getName() + ":" + lab.getName() + ":" + region.getProbes();
    }
}

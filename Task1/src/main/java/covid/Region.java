package covid;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * Model class for a federal state
 */
@Data
@Builder
public class Region {
    private String name;
    private int probes;

    @Singular("lab")
    private Map<Lab, Integer> labs;
}

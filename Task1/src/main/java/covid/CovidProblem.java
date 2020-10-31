package covid;

import java.util.List;
import lombok.Getter;

public class CovidProblem
{
    @Getter
    private final List<Region> regions;

    @Getter
    private final List<Lab> labs;

    public CovidProblem()
    {

        var viennaLab = Lab.builder()
                .name("Wien")
                .build();
        var innsbruckLab = Lab.builder()
                .name("Innsbruck")
                .build();
        var salzburgLab = Lab.builder()
                .name("Salzburg")
                .build();

        labs = List.of(viennaLab, innsbruckLab, salzburgLab);

        var viennaRegion = Region.builder()
                .name("Wien")
                .lab(viennaLab, 0)
                .lab(innsbruckLab, 4)
                .lab(salzburgLab, 3)
                .probes(31602)
                .build();

        var upperAustria = Region.builder()
                .name("Oberösterreich")
                .lab(viennaLab, 2)
                .lab(innsbruckLab, 2)
                .lab(salzburgLab, 1)
                .probes(12526)
                .build();

        var lowerAustria = Region.builder()
                .name("Niederösterreich")
                .lab(viennaLab, 1)
                .lab(innsbruckLab, 3)
                .lab(salzburgLab, 2)
                .probes(12194)
                .build();

        var tyrol = Region.builder()
                .name("Tirol")
                .lab(viennaLab, 4)
                .lab(innsbruckLab, 0)
                .lab(salzburgLab, 1)
                .probes(10684)
                .build();

        var styria = Region.builder()
                .name("Steiermark")
                .lab(viennaLab, 2)
                .lab(innsbruckLab, 2)
                .lab(salzburgLab, 1)
                .probes(6440)
                .build();

        var salzburg = Region.builder()
                .name("Salzburg")
                .lab(viennaLab, 3)
                .lab(innsbruckLab, 1)
                .lab(salzburgLab, 0)
                .probes(4876)
                .build();

        var vorarlberg = Region.builder()
                .name("Vorarlberg")
                .lab(viennaLab, 5)
                .lab(innsbruckLab, 1)
                .lab(salzburgLab, 1)
                .probes(3409)
                .build();

        var carinthia = Region.builder()
                .name("Kärnten")
                .lab(viennaLab, 3)
                .lab(innsbruckLab, 2)
                .lab(salzburgLab, 1)
                .probes(1877)
                .build();

        var burgenland = Region.builder()
                .name("Burgenland")
                .lab(viennaLab, 2)
                .lab(innsbruckLab, 3)
                .lab(salzburgLab, 2)
                .probes(1625)
                .build();

        regions = List.of(viennaRegion,
                          upperAustria,
                          lowerAustria,
                          tyrol,
                          styria,
                          salzburg,
                          vorarlberg,
                          carinthia,
                          burgenland);
    }
}

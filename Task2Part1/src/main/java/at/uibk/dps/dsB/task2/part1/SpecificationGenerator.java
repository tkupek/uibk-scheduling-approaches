package at.uibk.dps.dsB.task2.part1;

import net.sf.opendse.model.*;

/**
 * The {@link SpecificationGenerator} generates the {@link Specification}
 * modeling the orchestration of the customer modeling application discussed in
 * Lecture 1.
 *
 * @author Fedor Smirnov
 */
public final class SpecificationGenerator {

    private SpecificationGenerator() {
    }

    /**
     * Generates the specification modeling the orchestration of the customer
     * monitoring application.
     *
     * @return the specification modeling the orchestration of the customer
     * monitoring application
     */
    public static Specification generate() {
        Application<Task, Dependency> appl = generateApplication();
        Architecture<Resource, Link> arch = generateArchitecture();
        Mappings<Task, Resource> mappings = generateMappings(arch, appl);
        return new Specification(appl, arch, mappings);
    }

    /**
     * Generates the application graph
     *
     * @return the application graph
     */
    private static Application<Task, Dependency> generateApplication() {
        Application<Task, Dependency> application = new Application<>();
        Task t0 = new Task("t0");
        Task t1 = new Task("t1");
        Task t2 = new Task("t2");
        Task t3 = new Task("t3");
        Task t4 = new Task("t4");
        Task t5 = new Task("t5");

        Communication c0 = new Communication("c0");
        Communication c1 = new Communication("c1");
        Communication c2 = new Communication("c2");
        Communication c3 = new Communication("c3");
        Communication c4 = new Communication("c4");

        application.addEdge(new Dependency("d0"), t0, c0);
        application.addEdge(new Dependency("d1"), c0, t1);
        application.addEdge(new Dependency("d2"), t1, c1);
        application.addEdge(new Dependency("d3"), t1, c2);
        application.addEdge(new Dependency("d4"), c1, t2);
        application.addEdge(new Dependency("d5"), c2, t3);
        application.addEdge(new Dependency("d6"), t2, c3);
        application.addEdge(new Dependency("d7"), t3, c4);
        application.addEdge(new Dependency("d8"), c3, t4);
        application.addEdge(new Dependency("d9"), c3, t5);
        application.addEdge(new Dependency("d10"), c4, t4);
        application.addEdge(new Dependency("d11"), c4, t5);

        return application;
    }

    /**
     * Generates the architecture graph
     *
     * @return the architecture graph
     */
    private static Architecture<Resource, Link> generateArchitecture() {
        Architecture<Resource, Link> architecture = new Architecture<>();
        Resource r0 = new Resource("r0");
        Resource r1 = new Resource("r1");
        Resource r2 = new Resource("r2");
        Resource r3 = new Resource("r3");
        Resource r4 = new Resource("r4");
        Resource r5 = new Resource("r5");

        Resource bus0 = new Resource("bus0");
        Resource bus1 = new Resource("bus1");
        Resource bus2 = new Resource("bus2");
        Resource bus3 = new Resource("bus3");
        Resource bus4 = new Resource("bus4");

        r0.setAttribute("costs", 1);
        r1.setAttribute("costs", 1);
        r2.setAttribute("costs", 1);
        r3.setAttribute("costs", 1);
        r4.setAttribute("costs", 1);
        r5.setAttribute("costs", 1);
        bus0.setAttribute("costs", 1);
        bus1.setAttribute("costs", 1);
        bus2.setAttribute("costs", 1);
        bus3.setAttribute("costs", 1);
        bus4.setAttribute("costs", 1);

        architecture.addEdge(new Link("l0"), r0, bus0);
        architecture.addEdge(new Link("l1"), r1, bus0);
        architecture.addEdge(new Link("l2"), r1, bus1);
        architecture.addEdge(new Link("l3"), r1, bus2);
        architecture.addEdge(new Link("l4"), r2, bus1);
        architecture.addEdge(new Link("l5"), r2, bus3);
        architecture.addEdge(new Link("l6"), r3, bus2);
        architecture.addEdge(new Link("l7"), r3, bus4);
        architecture.addEdge(new Link("l8"), r4, bus3);
        architecture.addEdge(new Link("l9"), r4, bus4);
        architecture.addEdge(new Link("l10"), r5, bus3);
        architecture.addEdge(new Link("l11"), r5, bus4);
        return architecture;
    }

    /**
     * Generates the mapping edges
     *
     * @param arch the architecture graph
     * @param appl the application graph
     * @return the mapping edges
     */
    private static Mappings<Task, Resource> generateMappings(Architecture<Resource, Link> arch,
                                                             Application<Task, Dependency> appl) {
        Mappings<Task, Resource> mappings = new Mappings<>();
        Mapping<Task, Resource> m0 = new Mapping<>("m0", appl.getVertex("t0"), arch.getVertex("r0"));
        Mapping<Task, Resource> m1 = new Mapping<>("m1", appl.getVertex("t1"), arch.getVertex("r1"));
        Mapping<Task, Resource> m2 = new Mapping<>("m2", appl.getVertex("t2"), arch.getVertex("r2"));
        Mapping<Task, Resource> m3 = new Mapping<>("m3", appl.getVertex("t3"), arch.getVertex("r3"));
        Mapping<Task, Resource> m4 = new Mapping<>("m4", appl.getVertex("t4"), arch.getVertex("r4"));
        Mapping<Task, Resource> m5 = new Mapping<>("m5", appl.getVertex("t5"), arch.getVertex("r5"));
        mappings.add(m0);
        mappings.add(m1);
        mappings.add(m2);
        mappings.add(m3);
        mappings.add(m4);
        mappings.add(m5);
        return mappings;
    }

}

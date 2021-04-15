package org.openjdk.jcstress.os;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openjdk.jcstress.os.topology.PresetTopology;
import org.openjdk.jcstress.os.topology.Topology;
import org.openjdk.jcstress.os.topology.TopologyParseException;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

@RunWith(Parameterized.class)
public class SchedulerLocalAffinityTest {

    @Parameterized.Parameters(name = "p={0} c={1} t={2} limited={3}")
    public static Iterable<Object[]> data() {
        List<Object[]> r = new ArrayList<>();
        for (int p = 1; p <= 4; p++) {
            for (int c : new int[] { 1, 2, 4, 5, 6, 8 }) {
                for (int t : new int[] { 1, 2, 8 }) {
                    r.add(new Object[] { p, c, t, false });
                    r.add(new Object[] { p, c, t, true });
                }
            }
        }
        return r;
    }

    @Parameterized.Parameter(0)
    public int p;

    @Parameterized.Parameter(1)
    public int c;

    @Parameterized.Parameter(2)
    public int t;

    @Parameterized.Parameter(3)
    public boolean limited;

    @Test
    public void test() throws TopologyParseException {
        Topology topo = new PresetTopology(p, c, t);
        int maxThreads = limited ? Math.min(4, topo.totalCores()) : topo.totalCores();
        Scheduler s = new Scheduler(topo, maxThreads);
        s.enableDebug();

        Queue<CPUMap> takenMaps = new LinkedBlockingQueue<>();

        List<SchedulingClass> cases = new ArrayList<>();
        for (int a = 1; a <= 4; a++) {
            List<SchedulingClass> skel = s.localAffinityFor(a, maxThreads);
            for (int c = 0; c < 1000; c++) {
                cases.addAll(skel);
            }
        }

        Collections.shuffle(cases, new Random(12345));

        for (SchedulingClass scl : cases) {
            CPUMap cpuMap = s.tryAcquire(scl);
            while (cpuMap == null) {
                CPUMap old = takenMaps.poll();
                Assert.assertNotNull("Cannot schedule on empty system", old);
                s.release(old);
                cpuMap = s.tryAcquire(scl);
            }

            takenMaps.offer(cpuMap);
            int[] actorMap = cpuMap.actorMap();

            for (int a1 = 0; a1 < scl.numActors(); a1++) {
                for (int a2 = 0; a2 < scl.numActors(); a2++) {
                    if (scl.packages[a1] == scl.packages[a2]) {
                        Assert.assertEquals("Should be scheduled at the same package",
                                topo.threadToPackage(actorMap[a1]), topo.threadToPackage(actorMap[a2]));
                    } else {
                        Assert.assertNotEquals("Should be scheduled at the different packages",
                                topo.threadToPackage(actorMap[a1]), topo.threadToPackage(actorMap[a2]));
                    }
                    if (scl.cores[a1] == scl.cores[a2]) {
                        Assert.assertEquals("Should be scheduled at the same core: " + scl,
                                topo.threadToCore(actorMap[a1]), topo.threadToCore(actorMap[a2]));
                    } else {
                        Assert.assertNotEquals("Should be scheduled at the different core: " + scl,
                                topo.threadToCore(actorMap[a1]), topo.threadToCore(actorMap[a2]));
                    }
                }
            }
        }
    }

}

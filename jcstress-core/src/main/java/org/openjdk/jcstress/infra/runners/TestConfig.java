/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.jcstress.infra.runners;

import org.openjdk.jcstress.os.SchedulingClass;
import org.openjdk.jcstress.Options;
import org.openjdk.jcstress.infra.TestInfo;
import org.openjdk.jcstress.os.CPUMap;
import org.openjdk.jcstress.vm.AllocProfileSupport;
import org.openjdk.jcstress.vm.DeoptMode;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TestConfig implements Serializable {
    public final SpinLoopStyle spinLoopStyle;
    public final int time;
    public final int iters;
    public final DeoptMode deoptMode;
    public final int threads;
    public final String name;
    public final String generatedRunnerName;
    public final List<String> jvmArgs;
    public final int forkId;
    public final int maxFootprintMB;
    public final List<String> actorNames;
    public final int compileMode;
    public final SchedulingClass shClass;
    public int minStride;
    public int maxStride;
    public StrideCap strideCap;
    public CPUMap cpuMap;

    public void setCPUMap(CPUMap cpuMap) {
        this.cpuMap = cpuMap;
    }

    public enum StrideCap {
        NONE,
        FOOTPRINT,
        TIME,
    }

    public TestConfig(Options opts, TestInfo info, int forkId, List<String> jvmArgs, int compileMode, SchedulingClass scl) {
        this.forkId = forkId;
        this.jvmArgs = jvmArgs;
        time = opts.getTime();
        minStride = opts.getMinStride();
        maxStride = opts.getMaxStride();
        iters = opts.getIterations();
        spinLoopStyle = opts.getSpinStyle();
        deoptMode = opts.deoptMode();
        maxFootprintMB = opts.getMaxFootprintMb();
        threads = info.threads();
        name = info.name();
        generatedRunnerName = info.generatedRunner();
        actorNames = info.actorNames();
        this.compileMode = compileMode;
        shClass = scl;
        strideCap = StrideCap.NONE;
    }

    public void adjustStrides(FootprintEstimator estimator) {
        int count = 1;
        int succCount = count;
        while (true) {
            StrideCap cap = tryWith(estimator, count);
            if (cap != StrideCap.NONE) {
                strideCap = cap;
                break;
            }

            // success!
            succCount = count;

            // do not go over the maxStride
            if (succCount >= maxStride) {
                succCount = maxStride;
                break;
            }

            count *= 2;
        }

        maxStride = Math.min(maxStride, succCount);
        minStride = Math.min(minStride, succCount);
    }

    public interface FootprintEstimator {
        void runWith(int size, long[] counters);
    }

    private StrideCap tryWith(FootprintEstimator estimator, int count) {
        try {
            long[] cnts = new long[2];
            estimator.runWith(count, cnts);
            long footprint = cnts[0];
            long usedTime = cnts[1];

            if (footprint > maxFootprintMB * 1024 * 1024) {
                // blown the footprint estimate
                return StrideCap.FOOTPRINT;
            }

            if (TimeUnit.NANOSECONDS.toMillis(usedTime) > time) {
                // blown the time estimate
                return StrideCap.TIME;
            }

        } catch (OutOfMemoryError err) {
            // blown the heap size
            return StrideCap.FOOTPRINT;
        }
        return StrideCap.NONE;
    }

    public int getCompileMode() {
        return compileMode;
    }

    public SchedulingClass getSchedulingClass() {
        return shClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestConfig that = (TestConfig) o;

        if (!name.equals(that.name)) return false;
        if (spinLoopStyle != that.spinLoopStyle) return false;
        if (minStride != that.minStride) return false;
        if (maxStride != that.maxStride) return false;
        if (time != that.time) return false;
        if (iters != that.iters) return false;
        if (deoptMode != that.deoptMode) return false;
        if (threads != that.threads) return false;
        if (compileMode != that.compileMode) return false;
        if (!jvmArgs.equals(that.jvmArgs)) return false;
        if (!shClass.equals(that.shClass)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "JVM options: " + jvmArgs +"; Compile mode: " + getCompileMode();
    }
}

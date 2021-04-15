/*
 * Copyright (c) 2021, Red Hat, Inc. All rights reserved.
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
package org.openjdk.jcstress.os;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SchedulingClass implements Serializable {
    final AffinityMode mode;
    final int actors;
    final int[] packages;
    final int[] cores;

    public SchedulingClass(AffinityMode mode, int actors) {
        this.mode = mode;
        this.packages = new int[actors];
        this.cores = new int[actors];
        this.actors = actors;
        Arrays.fill(packages, -1);
        Arrays.fill(cores, -1);
    }

    public SchedulingClass(SchedulingClass copy) {
        this.actors = copy.actors;
        this.mode = copy.mode;
        this.packages = Arrays.copyOf(copy.packages, copy.packages.length);
        this.cores = Arrays.copyOf(copy.cores, copy.cores.length);
    }

    public AffinityMode mode() {
        return mode;
    }

    public int numActors() {
        return actors;
    }

    public int numPackages() {
        int m = -1;
        for (int p : packages) {
            m = Math.max(m, p);
        }
        return m + 1;
    }

    public int numCores() {
        int m = -1;
        for (int c : cores) {
            m = Math.max(m, c);
        }
        return m + 1;
    }

    public int[] packageActors() {
        int[] r = new int[numPackages()];
        for (int p : packages) {
            if (p != -1) r[p]++;
        }
        return r;
    }

    public int[] coreActors() {
        int[] r = new int[numCores()];
        for (int c : cores) {
            if (c != -1) r[c]++;
        }
        return r;
    }

    public void setPackage(int a, int p) {
        packages[a] = p;
    }

    public int getPackage(int a) {
        return packages[a];
    }

    public void setCore(int a, int c) {
        cores[a] = c;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchedulingClass scl = (SchedulingClass) o;
        return Arrays.equals(packages, scl.packages) &&
                Arrays.equals(cores, scl.cores);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(packages);
        result = 31 * result + Arrays.hashCode(cores);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int a = 0; a < actors; a++) {
            if (a != 0) {
                sb.append(", ");
            }
            sb.append("(PG ");
            int p = packages[a];
            if (p != -1) {
                sb.append(p);
            } else {
                sb.append("free");
            }
            sb.append(", ");
            sb.append("CG ");
            int c = cores[a];
            if (c != -1) {
                sb.append(c);
            } else {
                sb.append("free");
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public static String description(SchedulingClass scl, List<String> actorNames) {
        StringBuilder sb = new StringBuilder();
        for (int a = 0; a < actorNames.size(); a++) {
            String an = actorNames.get(a);
            if (an != null) {
                sb.append("    ");
                sb.append(an);
                sb.append(": ");
            }
            sb.append("package group ");
            int p = scl.packages[a];
            if (p != -1) {
                sb.append(p);
            } else {
                sb.append("free");
            }
            sb.append(", ");
            sb.append("core group ");
            int c = scl.cores[a];
            if (c != -1) {
                sb.append(c);
            } else {
                sb.append("free");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

}

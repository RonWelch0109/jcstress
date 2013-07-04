/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.jcstress.tests.causality.lazyinit.plain;

import org.openjdk.jcstress.infra.results.DoubleResult2;
import org.openjdk.jcstress.tests.Actor2_Test;

public class DoubleLazyTest implements Actor2_Test<DoubleLazyTest.State, DoubleResult2> {

    @Override
    public void actor1(State s, DoubleResult2 r) {
        if (s.f == 0D) {
            s.f = 1D;
        }
        r.r1 = s.f;
    }

    @Override
    public void actor2(State s, DoubleResult2 r) {
        if (s.f == 0D) {
            s.f = 1D;
        }
        r.r2 = s.f;
    }

    @Override
    public State newState() {
        return new State();
    }

    @Override
    public DoubleResult2 newResult() {
        return new DoubleResult2();
    }

    public static class State {
        double f;
    }
}

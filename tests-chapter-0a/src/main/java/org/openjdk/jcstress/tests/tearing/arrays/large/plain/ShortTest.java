/*
 * Copyright (c) 2016, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.jcstress.tests.tearing.arrays.large.plain;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.*;
import java.util.concurrent.*;

// -- This file was mechanically generated: Do not edit! -- //

/**
 * Tests if fields writes are not word-teared.
 */
@JCStressTest
@Outcome(id = "[-1, -1]", expect = Expect.ACCEPTABLE, desc = "Seeing the set value.")
@Outcome(expect = Expect.FORBIDDEN, desc = "Should see the set values only")
@State
public class ShortTest {

    short[] arr =  new short[2 * 1024 * 1024];
    int idx1 = ThreadLocalRandom.current().nextInt(2 * 1024 * 1024 - 1);
    int idx2 = idx1 + 1;

    @Actor
    public void actor1() {
        arr[idx1] = (short) -1;
    }

    @Actor
    public void actor2() {
        arr[idx2] = (short) -1;
    }

    @Arbiter
    public void arbiter(ShortResult2 r) {
        r.r1 = arr[idx1];
        r.r2 = arr[idx2];
    }

}


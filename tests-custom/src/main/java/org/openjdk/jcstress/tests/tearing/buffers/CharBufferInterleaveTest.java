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
package org.openjdk.jcstress.tests.tearing.buffers;

import org.openjdk.jcstress.infra.results.IntResult3;
import org.openjdk.jcstress.tests.Actor2_Arbiter1_Test;

import java.nio.CharBuffer;

public class CharBufferInterleaveTest implements Actor2_Arbiter1_Test<CharBuffer, IntResult3> {

    /** Array size: 256 bytes inevitably crosses the cache line on most implementations */
    public static final int SIZE = 256;

    @Override
    public CharBuffer newState() {
        return CharBuffer.allocate(SIZE);
    }

    @Override
    public void actor1(CharBuffer s, IntResult3 r) {
        for (int i = 0; i < SIZE; i += 2) {
            s.put(i, 'a');
        }
    }

    @Override
    public void actor2(CharBuffer s, IntResult3 r) {
        for (int i = 1; i < SIZE; i += 2) {
            s.put(i, 'b');
        }
    }

    @Override
    public void arbiter1(CharBuffer s, IntResult3 r) {
        r.r1 = r.r2 = r.r3 = 0;
        for (int i = 0; i < SIZE; i++) {
            switch (s.get(i)) {
                case 'a': r.r2++; break;
                case 'b': r.r3++; break;
                default: r.r1++; break;
            }
        }
    }

    @Override
    public IntResult3 newResult() {
        return new IntResult3();
    }

}

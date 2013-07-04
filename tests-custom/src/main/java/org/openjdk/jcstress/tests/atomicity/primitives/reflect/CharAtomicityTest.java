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
package org.openjdk.jcstress.tests.atomicity.primitives.reflect;

import org.openjdk.jcstress.infra.results.CharResult1;
import org.openjdk.jcstress.tests.Actor2_Test;
import org.openjdk.jcstress.tests.atomicity.primitives.Constants;

import java.lang.reflect.Field;

/**
 * Tests if volatile primitive doubles experience non-atomic updates.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public class CharAtomicityTest implements Actor2_Test<CharAtomicityTest.State, CharResult1> {

    public static class State {
        private static Field FIELD;

        static {
            try {
                FIELD = State.class.getDeclaredField("x");
                FIELD.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException(e);
            }

        }

        char x;
    }

    @Override
    public State newState() {
        return new State();
    }

    @Override
    public void actor1(State s, CharResult1 r) {
        try {
            State.FIELD.setChar(s, Constants.CHAR_SAMPLE);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void actor2(State s, CharResult1 r) {
        try {
            r.r1 = State.FIELD.getChar(s) == 0 ? 'N' : 'A';
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public CharResult1 newResult() {
        return new CharResult1();
    }

}

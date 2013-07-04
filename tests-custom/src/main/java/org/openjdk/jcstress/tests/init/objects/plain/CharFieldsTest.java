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
package org.openjdk.jcstress.tests.init.objects.plain;

import org.openjdk.jcstress.infra.results.CharResult4;
import org.openjdk.jcstress.tests.Actor2_Test;

public class CharFieldsTest implements Actor2_Test<CharFieldsTest.State, CharResult4> {

    public static class State {
        Data data;
    }

    public static class Data {
        char v0, v1, v2, v3;
    }

    @Override
    public State newState() {
        return new State();
    }

    @Override
    public void actor1(State s, CharResult4 r) {
        s.data = new Data();
    }

    @Override
    public void actor2(State s, CharResult4 r) {
        Data d = s.data;
        if (d == null) {
            r.r1 = r.r2 = r.r3 = r.r4 = 'N';
        } else {
            r.r1 = (char)(d.v0 + 'A');
            r.r2 = (char)(d.v1 + 'A');
            r.r3 = (char)(d.v2 + 'A');
            r.r4 = (char)(d.v3 + 'A');
        }
    }

    @Override
    public CharResult4 newResult() {
        return new CharResult4();
    }

}

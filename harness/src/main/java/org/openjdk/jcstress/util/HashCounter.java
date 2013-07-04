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
package org.openjdk.jcstress.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashCounter<T> implements Counter<T> {

    private Map<T, Holder> ms = new HashMap<T, Holder>();

    @Override
    public void record(T result) {
        record(result, 1);
    }

    @Override
    public void record(T result, long count) {
        Holder holder = ms.get(result);
        if (holder == null) {
            holder = new Holder();
            ms.put(result, holder);
        }
        holder.value += count;
    }

    @Override
    public long count(T result) {
        Holder holder = ms.get(result);
        return holder == null ? 0 : holder.value;
    }

    @Override
    public Set<T> elementSet() {
        return ms.keySet();
    }

    @Override
    public Counter<T> merge(Counter<T> other) {
        HashCounter<T> r = new HashCounter<T>();
        for (T t : other.elementSet()) {
            r.record(t, other.count(t));
        }
        for (T t : this.elementSet()) {
            r.record(t, this.count(t));
        }
        return r;
    }

    private static class Holder {
        public long value;
    }

}

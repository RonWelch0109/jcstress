/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
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

import org.openjdk.jcstress.infra.Status;
import org.openjdk.jcstress.infra.collectors.TestResult;
import org.openjdk.jcstress.util.Counter;
import org.openjdk.jcstress.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Basic runner for concurrency tests.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */
public abstract class Runner<R> {
    protected static final int MIN_TIMEOUT_MS = 30*1000;

    protected final Control control;
    protected final ExecutorService pool;
    protected final String testName;
    protected final ForkedTestConfig config;
    protected final List<String> messages;

    public Runner(ForkedTestConfig config, ExecutorService pool, String testName) {
        this.pool = pool;
        this.testName = testName;
        this.control = new Control();
        this.config = config;
        this.messages = new ArrayList<>();
    }

    /**
     * Run the test.
     * This method blocks until test is complete
     */
    public TestResult run() {
        Counter<R> result = new Counter<>();

        try {
            Counter<R> cnt = sanityCheck();
            result.merge(cnt);
        } catch (ClassFormatError | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            return dumpFailure(Status.API_MISMATCH, "Test sanity check failed, skipping", e);
        } catch (Throwable e) {
            return dumpFailure(Status.CHECK_TEST_ERROR, "Check test failed", e);
        }

        for (int c = 0; c < config.iters; c++) {
            Collection<Future<Counter<R>>> futures = internalRun();

            long startTime = System.nanoTime();
            boolean allStopped = false;
            while (!allStopped) {
                allStopped = true;
                for (Future<Counter<R>> t : futures) {
                    try {
                        t.get(1, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        allStopped = false;
                    } catch (ExecutionException | InterruptedException e) {
                        return dumpFailure(Status.TEST_ERROR, "Unrecoverable error while running", e.getCause());
                    }
                }

                long timeSpent = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                if (timeSpent > Math.max(10*config.time, MIN_TIMEOUT_MS)) {
                    return dumpFailure(Status.TIMEOUT_ERROR, "Timeout waiting for tasks to complete: " + timeSpent + " ms");
                }
            }

            for (Future<Counter<R>> t : futures) {
                try {
                    result.merge(t.get());
                } catch (InterruptedException | ExecutionException e) {
                    // Cannot happen anymore.
                }
            }
        }

        return dump(result);
    }

    private TestResult prepareResult(Status status) {
        TestResult result = new TestResult(status);
        for (String msg : messages) {
            result.addMessage(msg);
        }
        messages.clear();
        return result;
    }

    protected TestResult dumpFailure(Status status, String message) {
        messages.add(message);
        return prepareResult(status);
    }

    protected TestResult dumpFailure(Status status, String message, Throwable aux) {
        messages.add(message);
        TestResult result = prepareResult(status);
        result.addMessage(StringUtils.getStacktrace(aux));
        return result;
    }

    protected TestResult dump(Counter<R> cnt) {
        TestResult result = prepareResult(Status.NORMAL);
        for (R e : cnt.elementSet()) {
             result.addState(String.valueOf(e), cnt.count(e));
        }
        return result;
    }

    public abstract Counter<R> sanityCheck() throws Throwable;

    public abstract Collection<Future<Counter<R>>> internalRun();

}

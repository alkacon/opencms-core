/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.test;

import org.opencms.file.CmsObject;

import java.lang.reflect.Method;

/**
 * Allows to run a specific method of an {@link org.opencms.test.OpenCmsTestCase} case concurrently.<p>
 *
 * @since 6.5.0
 */
public class OpenCmsThreadedTestCaseSuite {

    /** A not-initialized OpenCms user context. */
    public static final CmsObject PARAM_CMSOBJECT = new CmsObject(null, null);

    /** A not-initialized OpenCms user context. */
    public static final Object PARAM_COUNTER = new Object();

    /** The maximum allowed runtime for this test suite. */
    private long m_allowedRuntime;

    /** The number of concurrent test cases to execute. */
    private int m_count;

    /** The time the method executed. */
    private long m_runtime;

    /** The initialized threaded test cases. */
    private OpenCmsThreadedTestCase[] m_threads;

    /** Error that occurred when running the test suite. */
    private Throwable m_throwable;

    /**
     * Generates a new threaded test case suite with the selected test case method.<p>
     *
     * The <code>parameters</code> array may contain the following placeholders:<ul>
     * <li>{@link #PARAM_CMSOBJECT}: This will be replaced by a new {@link CmsObject} instance.
     * <li>{@link #PARAM_COUNTER}: This will be replaced by an integer counter with the number of the generated thread
     * </ul>
     *
     * @param count the number of concurrent test cases to execute
     * @param testCase the test case class to use
     * @param method the method to execute on the test case
     * @param parameters the parameter values for the method
     */
    public OpenCmsThreadedTestCaseSuite(int count, OpenCmsTestCase testCase, String method, Object[] parameters) {

        m_count = count;
        m_runtime = -1;
        m_allowedRuntime = m_count * 100;

        try {
            // generate class array - required for reflection
            Class[] clazz = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == PARAM_COUNTER) {
                    clazz[i] = Integer.class;
                } else {
                    clazz[i] = parameters[i].getClass();
                }
            }
            // get the method we want to use from the test case
            Method m = testCase.getClass().getMethod(method, clazz);
            // now initialize the threads
            m_threads = initThreads(m_count, testCase, m, parameters);
        } catch (Exception e) {
            // exception may be becasue of bad method, or during initialization of threads
            m_throwable = e;
        }
    }

    /**
     * Returns a list of {@link OpenCmsThreadedTestCase} instances that will be executed concurrently by this suite.<p>
     *
     * @param count the number of concurrent executions
     * @param testCase the base test case instance to execute a method from
     * @param method the method name from the test case to execute
     * @param parameters the parameters of the test case
     *
     * @return a list of {@link OpenCmsThreadedTestCase} instances that will be executed concurrently by this suite
     *
     * @throws Exception if something goes wrong
     */
    private static OpenCmsThreadedTestCase[] initThreads(
        int count,
        OpenCmsTestCase testCase,
        Method method,
        Object[] parameters) throws Exception {

        OpenCmsThreadedTestCase[] result = new OpenCmsThreadedTestCase[count];
        for (int i = 0; i < count; i++) {
            // create a copy of the parameters, initialize any placeholders
            Object[] params = new Object[parameters.length];
            for (int j = 0; j < parameters.length; j++) {
                if (parameters[j] == PARAM_CMSOBJECT) {
                    // initialize a new CmsObject instance
                    params[j] = testCase.getCmsObject();
                } else if (parameters[j] == PARAM_COUNTER) {
                    // add a new counter parameter
                    params[j] = Integer.valueOf(i);
                } else {
                    // just use the given parameter
                    params[j] = parameters[j];
                }
            }
            result[i] = new OpenCmsThreadedTestCase(testCase, method, params);
        }

        return result;
    }

    /**
     * Returns the maximum allowed runtime of this test suite.<p>
     *
     * If the given limit is exceeded, then this suite will stop waiting for
     * the running test cases to finish and generate an internal exception.<p>
     *
     * The default is the number of threads multiplied by 100 milliseconds.<p>
     *
     * @return the maximum allowed runtime of this test suite
     */
    public long getAllowedRuntime() {

        return m_allowedRuntime;
    }

    /**
     * Returns the runtime of this test suite.<p>
     *
     * @return the runtime of this test suite
     */
    public long getRuntime() {

        return m_runtime;
    }

    /**
     * Returns the error that that may have been caused while initializing or running this suite,
     * or <code>null</code> in case no error was thrown.<p>
     *
     * @return the error that that may have been caused while initializing or running this suite,
     * or <code>null</code> in case no error was thrown
     */
    public Throwable getThrowable() {

        return m_throwable;
    }

    /**
     * Runs the initialized test concurrent test cases.<p>
     *
     * Returns the array of generated {@link OpenCmsThreadedTestCase} instances, which contain information
     * about the individual test results.<p>*
     *
     * @return the array of generated {@link OpenCmsThreadedTestCase} instances
     */
    public OpenCmsThreadedTestCase[] run() {

        long startTime = System.currentTimeMillis();

        if (m_throwable == null) {
            // there may have been an exception acessing the selected method, so the test cases can not run at all

            try {
                for (int i = 0; i < m_count; i++) {
                    // start all the test cases
                    m_threads[i].start();
                }

                // give the tests some time to finish
                long runTime;
                int runCount;
                long loopTime = System.currentTimeMillis();
                do {
                    runCount = 0;
                    for (int i = 0; i < m_count; i++) {
                        // check each test if it is still alive
                        if (m_threads[i].isAlive()) {
                            // this thread is still running
                            runCount++;
                        }
                    }
                    if (runCount > 0) {
                        // give the running threads some time to finish
                        Thread.sleep(50);
                    }
                    // calculate the runtime
                    runTime = System.currentTimeMillis() - loopTime;
                    // limit the run time to avoid total hangup of test
                } while ((runCount > 0) && (runTime < m_allowedRuntime));

                if ((runCount > 0) && (runTime >= m_allowedRuntime)) {
                    // allowed runtime was exceeded, generate exception that is stored be the member later
                    throw new RuntimeException(
                        "There where "
                            + runCount
                            + " threads still running after "
                            + (m_allowedRuntime / 1000.0)
                            + " seconds");
                }

            } catch (Throwable e) {
                // store the exception
                m_throwable = e;
            }
        }

        // calculate the final runtime
        m_runtime = System.currentTimeMillis() - startTime;
        return m_threads;
    }

    /**
     * Sets the maximum allowed runtime of this test suite.<p>
     *
     * @param allowedRuntime the maximum allowed runtime of this test suite to set
     */
    public void setAllowedRuntime(long allowedRuntime) {

        m_allowedRuntime = allowedRuntime;
    }
}
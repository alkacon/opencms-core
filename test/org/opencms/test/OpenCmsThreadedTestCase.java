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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Allows to run an {@link org.opencms.test.OpenCmsTestCase} in a separate Thread,
 * for concurrent execution.<p>
 *
 * Usually this class is created by a {@link org.opencms.test.OpenCmsThreadedTestCaseSuite}.<p>
 *
 * @since 6.5.0
 */
public class OpenCmsThreadedTestCase extends Thread {

    /** The method to execute on the test class. */
    private Method m_method;

    /** The parameters to use with the method. */
    private Object[] m_parameters;

    /** The result of the method execution. */
    private Object m_result;

    /** The time the method executed. */
    private long m_runtime;

    /** The base test case class to run the method from. */
    private OpenCmsTestCase m_testCase;

    /** Error that occurred when running the test method. */
    private Throwable m_throwable;

    /**
     * Generates a new threaded test case.<p>
     *
     * @param testCase the test case class to use
     * @param method the method to execute on the test case
     * @param parameters the parameter values for the method
     */
    public OpenCmsThreadedTestCase(OpenCmsTestCase testCase, Method method, Object[] parameters) {

        m_throwable = null;
        m_result = null;
        m_runtime = -1;
        m_testCase = testCase;
        m_parameters = parameters;
        m_method = method;
    }

    /**
     * Returns the (optional) result object of the selected test method.<p>
     *
     * @return the (optional) result object of the selected test method
     */
    public Object getResult() {

        return m_result;
    }

    /**
     * Returns the total runtime of this thread, or <code>-1</code> if the thread was not run at all.<p>
     *
     * @return the total runtime of this thread, or <code>-1</code> if the thread was not run at all
     */
    public long getRuntime() {

        return m_runtime;
    }

    /**
     * Returns the stored Throwable that was thrown when executing the selected test method, or <code>null</code>
     * if the test method did not cause an exception.<p>
     *
     * @return the stored Throwable that was thrown when executing the selected test method, or <code>null</code>
     *      if the test method did not cause an exception
     */
    public Throwable getThrowable() {

        return m_throwable;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        long starttime = System.currentTimeMillis();
        if (m_throwable == null) {
            try {
                m_result = m_method.invoke(m_testCase, m_parameters);
            } catch (InvocationTargetException e) {
                // store the target exception
                m_throwable = e.getTargetException();
            } catch (Throwable e) {
                // store the exception
                m_throwable = e;
            }
        }
        m_runtime = System.currentTimeMillis() - starttime;
    }
}
/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/flex/TestCmsFlexResponse.java,v $
 * Date   : $Date: 2005/09/11 13:27:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.flex;

import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.test.OpenCmsTestCase;
import org.opencms.test.OpenCmsTestProperties;
import org.opencms.util.CmsRequestUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

/** 
 * Unit tests for the {@link CmsFlexResponse}.<p> 
 * 
 * This test suite performs way more set-up than is required for the amount of testing that is done.
 * However, there is probably value in demonstrating how to set up a test case to access flex cache resources
 * so that more robust unit tests can be developed here.<p>
 * 
 * @author Jason Trump
 *  
 * @version $Revision: 1.1 $
 * 
 * @since 6.0.1
 */
public class TestCmsFlexResponse extends OpenCmsTestCase {

    /**
     * An InvocationHandler which simply records the arguments for each method that was called.<p>
     * 
     * If a 'stub' object was passed in the contructor, and the stub object has a method of the
     * same signature as the one that is being called, that method will be invoked.<p>
     */
    public static class RecordingMock implements InvocationHandler {

        /** Maps {@link Method} to a {@link List} of arguments passed to each invocation of that method handled by this object. */
        HashMap m_invocations = new HashMap();
        
        /** If non-null, delegate method invocations to this object when possible. */
        Object m_stub;

        /**
         * Default empty construtor.<p>
         */
        public RecordingMock() {
            // noop
        }

        /**
         * Construtor with a 'stub' Object.<p>
         * 
         * @param stub the stub Object to use
         */
        public RecordingMock(Object stub) {

            m_stub = stub;
        }

        /** 
         * Returns a list of all recorded calls to the given method.<p>
         * 
         * @param method the method to get the recorded calls for
         * 
         * @return a list of all recorded calls to the given method
         */
        public List getCalls(Method method) {

            ArrayList calls = (ArrayList)m_invocations.get(method);
            if (calls == null) {
                calls = new ArrayList();
                m_invocations.put(method, calls);
            }
            return calls;
        }


        /**
         * Notice that the given method has been invoked.<p>
         * 
         * Two actions are taken:
         * <ol>
         * <li>The invocation is recorded in <code>{@link #getCalls getCalls(method)}</code>.</li>
         * <li>If {@link #m_stub} is not null, the requested method is invoked on it</li>
         * </ol>
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            // record the invocation
            getCalls(method).add(args);
            
            // check to see if our stub object supports this method.
            if (m_stub != null) {
                Method stubMethod = null;
                try {
                    stubMethod = m_stub.getClass().getMethod(method.getName(), method.getParameterTypes());
                    if (stubMethod != null) {
                        if (stubMethod.getReturnType() == null) {
                            if (method.getReturnType() != null) {
                                stubMethod = null;
                            }
                        } else if (!stubMethod.getReturnType().equals(method.getReturnType())) {
                            stubMethod = null;
                        }
                    }
                } catch (NoSuchMethodException e) {
                    // ignore
                } catch (SecurityException e) {
                    // ignore
                }

                // if stubMethod is not null, then stub object has a public method with the requested signature
                if (stubMethod != null) {
                    return stubMethod.invoke(m_stub, args);
                }
            }

            return null;
        }
    }
    
    /** 
     * A partial implementation of {@link HttpServletRequest} which allows for the setting and getting of request attributes.<p>
     */
    public static class RequestStub {

        /** Attribute map. */
        HashMap m_attributes = new HashMap();

        /**
         * Returns the named attribute value.<p>
         * 
         * @param name the name of the attribute to return
         * @return the value of the attribute
         */
        public Object getAttribute(String name) {

            return m_attributes.get(name);
        }

        /**
         * Removes the named attribute.<p>
         * 
         * @param name the name of the attribute to remove
         */
        public void removeAttribute(String name) {

            m_attributes.remove(name);
        }

        /**
         * Sets the named attribute to the given value.<p>
         * 
         * @param name the name of the attribute to set
         * @param value the value to set
         */
        public void setAttribute(String name, Object value) {

            m_attributes.put(name, value);
        }
    }

    /** Method for setContentType(String) from the HttpServletResponse class. */
    public static Method SET_CONTENT_TYPE = null;
    
    /** Flex controller to be used by the tests. */
    private CmsFlexController m_controller;
    
    /** Request mockup object. */
    private RecordingMock m_reqMock;
    
    /** Response mockup object. */
    private RecordingMock m_resMock;
    
    /** Servlet request to use with the tests. */
    private HttpServletRequest m_request;

    /** Servlet response to use with the tests. */
    private HttpServletResponse m_response;

    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */
    public TestCmsFlexResponse(String arg0) {

        super(arg0);
    }

    /**
     * Static initializer for this test case.<p>
     */
    static {
        try {
            SET_CONTENT_TYPE = HttpServletResponse.class.getMethod("setContentType", new Class[] {String.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("HttpServletResponse linkage error", e);
        }
    }

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */    
    public static TestSetup suite() {

        OpenCmsTestProperties.initialize(org.opencms.test.AllTests.TEST_PROPERTIES_PATH);

        TestSuite suite = new TestSuite();
        suite.setName(TestCmsFlexResponse.class.getName());

        suite.addTest(new TestCmsFlexResponse("testContentTypeRules"));

        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {

                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {

                removeOpenCms();
            }
        };

        return wrapper;
    }

    /** 
     * Convenience method to create a mock HttpServletRequest backed by the given invocation handler.<p>
     */
    private static HttpServletRequest createMockRequest(RecordingMock recorder) {

        return (HttpServletRequest)createProxy(HttpServletRequest.class, recorder);
    }

    /** 
     * Convenience method to create a mock HttpServletResponse backed by the given invocation handler.<p>
     */
    private static HttpServletResponse createMockResponse(RecordingMock recorder) {

        return (HttpServletResponse)createProxy(HttpServletResponse.class, recorder);
    }

    private static Object createProxy(Class interfaceClass, InvocationHandler handler) {

        return Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] {interfaceClass},
            handler);
    }

    /**
     * Test semantics for Content-Type header on
     * {@link CmsFlexResponse#setContentType(String)} and {@link CmsFlexResponse#setHeader(String, String)}.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testContentTypeRules() throws Exception {

        // test that non-top elements won't try to set the content type.
        CmsFlexResponse f_res = new CmsFlexResponse(m_response, m_controller, false, false);
        f_res.setHeader(CmsRequestUtil.HEADER_CONTENT_TYPE, "application/borked");
        assertTrue("non-top request does not set content type header", m_resMock.m_invocations.isEmpty());

        f_res.setContentType("application/stillborked");
        assertTrue("non-top request does not set content type header", m_resMock.m_invocations.isEmpty());

        // test that top elements only set content type once
        // first, try with a call to setContentType()
        f_res = new CmsFlexResponse(m_response, m_controller, false, true);
        f_res.setContentType("text/foo");

        assertEquals("one method has been invoked on the actual response", 1, m_resMock.m_invocations.size());
        List setCalls = m_resMock.getCalls(SET_CONTENT_TYPE);
        assertEquals("top element has called setContentType() on the actual servlet response", 1, setCalls.size());
        assertEquals("correct content type value passed", "text/foo", ((Object[])setCalls.get(0))[0]);

        // subsequent attempts to set the content type on the same response should have no affect
        f_res.setContentType("text/bar");
        assertEquals("top element did NOT call content type method again", 1, setCalls.size());

        f_res.setHeader(CmsRequestUtil.HEADER_CONTENT_TYPE, "text/baz");
        assertEquals("still no more calls to setContentType() method", 1, setCalls.size());
        assertEquals("no other methods called on request", 1, m_resMock.m_invocations.size());

        // now, try with a call to setHeader on a new top response
        f_res = new CmsFlexResponse(m_response, m_controller, false, true);
        f_res.setHeader(CmsRequestUtil.HEADER_CONTENT_TYPE, "text/qux");
        assertEquals("setContentType() was called from setHeader", 2, setCalls.size());
        assertEquals("correct content type value passed", "text/qux", ((Object[])setCalls.get(1))[0]);

        // subsequent attempts to set the content type on the same response should have no affect
        f_res.setContentType("text/quux");
        assertEquals("no further calls to setContentType", 2, setCalls.size());
        assertEquals("no other methods called", 1, m_resMock.m_invocations.size());

        f_res.setHeader(CmsRequestUtil.HEADER_CONTENT_TYPE, "text/arg");
        assertEquals("no further calls to setContentType", 2, setCalls.size());
        assertEquals("no other methods called", 1, m_resMock.m_invocations.size());
    }

    /**
     * Initializes a flex cache controller and mock servlet request and response objects to be
     * used by this unit tests.<p>
     * 
     * @throws Exception if the setup fails
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {

        super.setUp();
        CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
        if (!cms.getRequestContext().currentUser().getName().equals(OpenCms.getDefaultUsers().getUserGuest())) {
            fail("'Guest' user could not be properly initialized!");
        }

        m_reqMock = new RecordingMock(new RequestStub());
        m_request = createMockRequest(m_reqMock);
        m_resMock = new RecordingMock();
        m_response = createMockResponse(m_resMock);

        m_controller = new CmsFlexController(cms, null, CmsFlexDummyLoader.m_flexCache, m_request, m_response, false, true);
        CmsFlexController.setController(m_request, m_controller);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {

        super.tearDown();
        m_reqMock = null;
        m_resMock = null;
        m_request = null;
        m_response = null;
        m_controller = null;
    }
}
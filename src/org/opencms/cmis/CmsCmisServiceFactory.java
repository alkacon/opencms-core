/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.cmis;

import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
import org.apache.commons.logging.Log;

/**
 * The factory class for creating the OpenCms CMIS service instances.<p>
 */
public class CmsCmisServiceFactory extends AbstractServiceFactory {

    /**
     * An invocation handler which wraps a service and is used for debugging/logging CMIS service calls.<p>
     */
    static class LoggingServiceProxy implements InvocationHandler {

        /** The CMIS service interfaces. */
        private static Set<Class<?>> m_serviceInterfaces = new HashSet<Class<?>>();

        /** The wrapped service. */
        private CmisService m_service;

        /**
         * Creates a instance.<p>
         *
         * @param service the service to wrap
         */
        public LoggingServiceProxy(CmisService service) {

            m_service = service;
        }

        static {
            for (Class<?> svcInterface : CmisService.class.getInterfaces()) {
                m_serviceInterfaces.add(svcInterface);
            }
        }

        /**
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            try {
                // CmisService defines some methods in addition to its base interfaces which don't correspond to CMIS service calls

                boolean isServiceCall = m_serviceInterfaces.contains(method.getDeclaringClass());
                if (isServiceCall) {
                    LOG.info("CMIS service call: " + getCallString(method, args));
                }
                Object result = method.invoke(m_service, args);
                if (isServiceCall && LOG.isDebugEnabled()) {
                    // This can generate a *VERY LARGE AMOUNT* of data in the log file, don't activate the debug channel
                    // unless you really need to
                    LOG.debug("Returned '" + result + "'");
                }
                return result;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                LOG.info(cause.getLocalizedMessage(), cause);
                throw cause;
            }
        }

        /**
         * Creates a string representation of a given method call, which is used for logging.<p>
         *
         * @param method the method
         * @param args the method call arguments
         *
         * @return a string representation of the method call
         */
        private String getCallString(Method method, Object[] args) {

            List<String> tokens = new ArrayList<String>();
            tokens.add(method.getName());
            if ((args != null) && (args.length > 0)) {
                tokens.add("=>");
                for (Object arg : args) {
                    tokens.add("'" + arg + "'");
                }
            }
            return CmsStringUtil.listAsString(tokens, " ");
        }
    }

    /** The logger for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsCmisServiceFactory.class);

    /** Default value for maximum depth of objects to return. */
    private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(100);

    /** Default value for maximum depth of types to return. */
    private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);

    /** Default value for maximum number of objects to return. */
    private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);

    /** Default value for maximum number of types to return. */
    private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory#destroy()
     */
    @Override
    public void destroy() {

        // do nothing for now
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory#getService(org.apache.chemistry.opencmis.commons.server.CallContext)
     */
    @Override
    public CmisService getService(CallContext context) {

        CmsCmisService service = new CmsCmisService(context);
        CmisService proxyService = (CmisService)Proxy.newProxyInstance(
            this.getClass().getClassLoader(),
            new Class[] {CmisService.class},
            new LoggingServiceProxy(service));
        ConformanceCmisServiceWrapper wrapperService = new ConformanceCmisServiceWrapper(
            proxyService,
            DEFAULT_MAX_ITEMS_TYPES,
            DEFAULT_DEPTH_TYPES,
            DEFAULT_MAX_ITEMS_OBJECTS,
            DEFAULT_DEPTH_OBJECTS);
        return wrapperService;
    }

    /**
     * @see org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory#init(java.util.Map)
     */
    @Override
    public void init(Map<String, String> parameters) {

        // do nothing for now
    }
}

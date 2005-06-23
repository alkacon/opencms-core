/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsSetNextRule.java,v $
 * Date   : $Date: 2005/06/23 10:47:19 $
 * Version: $Revision: 1.4 $
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * 
 * This file is based upon: 
 * org.apache.commons.digester.CallMethodRule.
 *
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencms.configuration;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.logging.Log;

import org.xml.sax.Attributes;

/**
 * Rule implementation that invokes a method on the (top-1) (parent) object, 
 * passing as implicit first argument of type <code>{@link org.opencms.file.CmsObject}</code>
 * and as a further argument the top stack instance. <p>
 * 
 * If no subsequent <code>CallParamRule</code> are matched for <code>CmsObject</code> 
 * which is the case in the OpenCms usage the first argument <code>CmsObject</code> 
 * will be null at method invocation time. <p>

 * This is an alternative for <code>{@link org.apache.commons.digester.SetNextRule}</code>
 * if a parent to child-property configuration has been done but the setter for that 
 * property requires additional arguments that are only available at real runtime 
 * of the application.<p>
 * 
 * The top stack element (child) that has to be set is matched against the constructor 
 * given <code>{@link java.lang.Class}[]</code>: It is used as argument on the position 
 * where the <code>Class[]</code> has an instance of the same type as it's own <code>Class</code>.<p>
 * 
 * @see org.apache.commons.digester.CallMethodRule
 * @see org.apache.commons.digester.SetNextRule
 * 
 * @author Craig McClanahan 
 * @author Achim Westermann 
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 6.0.0
 */

public class CmsSetNextRule extends Rule {

    /** The log object of this class. */
    private static Log LOG = CmsLog.getLog(CmsSetNextRule.class);
    /**
     * The body text collected from this element.
     */
    protected String m_bodyText = null;

    /**
     * The method name to call on the parent object.
     */
    protected String m_methodName = null;

    /**
     * The number of parameters to collect from <code>MethodParam</code> rules.
     * If this value is zero, a single parameter will be collected from the
     * body of this element.
     */
    protected int m_paramCount = 0;

    /**
     * The parameter types of the parameters to be collected.
     */
    protected Class[] m_paramTypes = null;

    /**
     * Should <code>MethodUtils.invokeExactMethod</code> be used for reflection.
     */
    protected boolean m_useExactMatch = false;

    /**
     * The names of the classes of the parameters to be collected.
     * This attribute allows creation of the classes to be postponed until the digester is set.
     */
    private String[] m_paramClassNames = null;

    /** 
     * location of the target object for the call, relative to the
     * top of the digester object stack. The default value of zero
     * means the target object is the one on top of the stack.
     */
    private int m_targetOffset = 0;

    /**
     * Construct a "call method" rule with the specified method name.<p>
     * 
     * 
     * The 1<sup>st</sup> argument of the method will be of type <code>{@link CmsObject}</code>.
     * It's value will remain null (except subsequent 
     * <code>{@link org.apache.commons.digester.CallParamRule}</code> would put a value 
     * which currently is impossible at initialization time within OpenCms).<p> 
     * 
     * The 2<sup>nd</sup> argument will be the top-stack element at digestion time. 
     * That instance has to be of the same type as the <code>clazz</code> argument to succeed.<p>
     *  
     *
     * @param methodName Method name of the parent method to call
     * @param clazz The class of the top-stack element (child) that will be present at digestion-time
     */
    public CmsSetNextRule(String methodName, Class clazz) {

        this(methodName, new Class[] {clazz});
    }

    /**
     * Construct a "call method" rule with the specified method name 
     * and additional parameters.<p>
     * 
     * 
     * The 1<sup>st</sup> argument of the method will be of type <code>{@link CmsObject}</code>.
     * It's value will remain null (except subsequent 
     * <code>{@link org.apache.commons.digester.CallParamRule}</code> would put a value 
     * which currently is impossible at initialization time within OpenCms).<p> 
     * 
     * The further arguments will be filled by the subsequent <code>{@link org.apache.commons.digester.CallParamRule}</code>  
     * matches. If the first <code>Class</code> in the given array matches the top stack element 
     * (child) that value will be used. If at digestion time no parameters are found for the given 
     * types their values for invocation of the method remain null.<p>
     *  
     *
     * @param methodName Method name of the parent method to call
     * @param clazzes an array with all parameter types for the method to invoke at digestion time 
     */
    public CmsSetNextRule(String methodName, Class[] clazzes) {

        m_targetOffset = 0;
        m_methodName = methodName;
        m_paramCount = clazzes.length + 1;
        m_paramTypes = new Class[m_paramCount];
        m_paramTypes[0] = CmsObject.class;
        System.arraycopy(clazzes, 0, m_paramTypes, 1, clazzes.length);
    }

    /**
     * Process the start of this element.
     *
     * @param attributes The attribute list for this element 
     * @param namespace  the namespace URI of the matching element, or an empty string if the parser is not namespace 
     *        aware or the element has no namespace
     * @param name the local name if the parser is namespace aware, or just the element name otherwise
     * @throws Exception if something goes wrong
     */
    public void begin(java.lang.String namespace, java.lang.String name, Attributes attributes) throws Exception {

        // not now: 6.0 RC 2
        //digester.setLogger(CmsLog.getLog(digester.getClass()));

        // Push an array to capture the parameter values if necessary
        if (m_paramCount > 0) {
            Object[] parameters = new Object[m_paramCount];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = null;
            }
            digester.pushParams(parameters);
        }
    }

    /**
     * Process the body text of this element.<p>
     *
     * @param bodyText The body text of this element
     * @param namespace the namespace URI of the matching element, or an empty string if the parser is not namespace 
     *                  aware or the element has no namespace
     * @param name the local name if the parser is namespace aware, or just the element name otherwise
     * @throws Exception if something goes wrong 
     */
    public void body(java.lang.String namespace, java.lang.String name, String bodyText) throws Exception {

        if (m_paramCount == 0) {
            m_bodyText = bodyText.trim();
        }
    }

    /**
     * Process the end of this element.<p>
     * 
     * @param namespace the namespace URI of the matching element, or an empty string if the parser is not namespace 
     *                  aware or the element has no namespace
     * @param name the local name if the parser is namespace aware, or just the element name otherwise
     * @throws Exception if something goes wrong
     */
    public void end(java.lang.String namespace, java.lang.String name) throws Exception {

        // Determine the target object for the method call: the parent object
        Object parent = digester.peek(1);
        Object child = digester.peek(0);

        // Retrieve or construct the parameter values array
        Object[] parameters = null;
        if (m_paramCount > 0) {
            parameters = (Object[])digester.popParams();
            if (LOG.isTraceEnabled()) {
                for (int i = 0, size = parameters.length; i < size; i++) {
                    LOG.trace("[SetNextRuleWithParams](" + i + ")" + parameters[i]);
                }
            }

            // In the case where the target method takes a single parameter
            // and that parameter does not exist (the CallParamRule never
            // executed or the CallParamRule was intended to set the parameter
            // from an attribute but the attribute wasn't present etc) then
            // skip the method call.
            //
            // This is useful when a class has a "default" value that should
            // only be overridden if data is present in the XML. I don't
            // know why this should only apply to methods taking *one*
            // parameter, but it always has been so we can't change it now.
            if (m_paramCount == 1 && parameters[0] == null) {
                return;
            }

        } else if (m_paramTypes != null && m_paramTypes.length != 0) {
            // Having paramCount == 0 and paramTypes.length == 1 indicates
            // that we have the special case where the target method has one
            // parameter being the body text of the current element.

            // There is no body text included in the source XML file,
            // so skip the method call
            if (m_bodyText == null) {
                return;
            }

            parameters = new Object[1];
            parameters[0] = m_bodyText;
            if (m_paramTypes.length == 0) {
                m_paramTypes = new Class[1];
                m_paramTypes[0] = String.class;
            }

        } else {
            // When paramCount is zero and paramTypes.length is zero it
            // means that we truly are calling a method with no parameters.
            // Nothing special needs to be done here.
        }

        // Construct the parameter values array we will need
        // We only do the conversion if the param value is a String and
        // the specified paramType is not String. 
        Object[] paramValues = new Object[m_paramTypes.length];

        Class propertyClass = child.getClass();
        for (int i = 0; i < m_paramTypes.length; i++) {
            if (m_paramTypes[i] == propertyClass) {
                // implant the original child to set if Class matches: 
                paramValues[i] = child;
            } else if (parameters[i] == null
                || (parameters[i] instanceof String && !String.class.isAssignableFrom(m_paramTypes[i]))) {
                // convert nulls and convert stringy parameters 
                // for non-stringy param types
                paramValues[i] = ConvertUtils.convert((String)parameters[i], m_paramTypes[i]);
            } else {
                paramValues[i] = parameters[i];
            }
        }

        if (parent == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("[SetNextRuleWithParams]{");
            sb.append(digester.getMatch());
            sb.append("} Call target is null (");
            sb.append("targetOffset=");
            sb.append(m_targetOffset);
            sb.append(",stackdepth=");
            sb.append(digester.getCount());
            sb.append(")");
            throw new org.xml.sax.SAXException(sb.toString());
        }

        // Invoke the required method on the top object
        if (LOG.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("[SetNextRuleWithParams]{");
            sb.append(digester.getMatch());
            sb.append("} Call ");
            sb.append(parent.getClass().getName());
            sb.append(".");
            sb.append(m_methodName);
            sb.append("(");
            for (int i = 0; i < paramValues.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                if (paramValues[i] == null) {
                    sb.append("null");
                } else {
                    sb.append(paramValues[i].toString());
                }
                sb.append("/");
                if (m_paramTypes[i] == null) {
                    sb.append("null");
                } else {
                    sb.append(m_paramTypes[i].getName());
                }
            }
            sb.append(")");
            LOG.debug(sb.toString());
        }

        Object result = null;
        if (m_useExactMatch) {
            // invoke using exact match
            result = MethodUtils.invokeExactMethod(parent, m_methodName, paramValues, m_paramTypes);

        } else {
            // invoke using fuzzier match
            result = MethodUtils.invokeMethod(parent, m_methodName, paramValues, m_paramTypes);
        }

        processMethodCallResult(result);
    }

    /**
     * Clean up after parsing is complete.<p>
     * 
     * @param namespace the namespace URI of the matching element, or an empty string if the parser is not namespace 
     *                  aware or the element has no namespace
     * @param name the local name if the parser is namespace aware, or just the element name otherwise
     * @throws Exception if something goes wrong
     */
    public void finish(String namespace, String name) throws Exception {

        String dummy = name;
        dummy = namespace;
        dummy = null;
        m_bodyText = dummy;
    }

    /**
     * Returns true if <code>MethodUtils.invokeExactMethod</code>
     * shall be used for the reflection.<p>
     * 
     * @return true if <code>MethodUtils.invokeExactMethod</code>
     *                 shall be used for the reflection.
     */
    public boolean getUseExactMatch() {

        return m_useExactMatch;
    }

    /**
     * Set the associated digester.<p>
     * 
     * The digester gets assigned to use the OpenCms conform logging
     * 
     * If needed, this class loads the parameter classes from their names.<p>
     * 
     * @param aDigester the associated digester to set
     */
    public void setDigester(Digester aDigester) {

        aDigester.setLogger(CmsLog.getLog(aDigester.getClass()));
        // call superclass
        super.setDigester(aDigester);
        // if necessary, load parameter classes
        if (m_paramClassNames != null) {
            m_paramTypes = new Class[m_paramClassNames.length];
            for (int i = 0; i < m_paramClassNames.length; i++) {
                try {
                    m_paramTypes[i] = aDigester.getClassLoader().loadClass(m_paramClassNames[i]);
                } catch (ClassNotFoundException e) {
                    // use the digester log
                    LOG.error(Messages.get().key(Messages.ERR_LOAD_CLASS_1, m_paramClassNames[i]), e);
                    m_paramTypes[i] = null; // Will cause NPE later
                }
            }
        }
    }

    /**
     * Set the value to use for <code>MethodUtils.invokeExactMethod</code>
     * to use.<p>
     * 
     * @param useExactMatch the value to use for <code>MethodUtils.invokeExactMethod</code>
     *                      to use
     */
    public void setUseExactMatch(boolean useExactMatch) {

        m_useExactMatch = useExactMatch;
    }

    /**
     * Returns a printable version of this Rule.<p>
     * 
     * @return a printable version of this Rule
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("CallMethodRule[");
        sb.append("methodName=");
        sb.append(m_methodName);
        sb.append(", paramCount=");
        sb.append(m_paramCount);
        sb.append(", paramTypes={");
        if (m_paramTypes != null) {
            for (int i = 0; i < m_paramTypes.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(m_paramTypes[i].getName());
            }
        }
        sb.append("}");
        sb.append("]");
        return (sb.toString());

    }

    /**
     * Subclasses may override this method to perform additional processing of the 
     * invoked method's result.
     *
     * @param result the Object returned by the method invoked, possibly null
     */
    protected void processMethodCallResult(Object result) {

        // do nothing but to fool checkstyle
        if (result != null) {
            // nop
        }
    }
}
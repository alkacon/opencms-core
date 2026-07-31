/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.jsp.util;

import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.ImportHandler;
import jakarta.servlet.jsp.JspContext;

/**
 * This EL resolver is a workaround for performance problems in Jetty when using EE 10.
 * <p>
 * When using EE 10, a new ImportELResolver is present which tries to interpret EL variables as classes. This fires for all otherwise undefined variables,
 * of which there are many in the Mercury template at runtime, causing a lot of class loading. This leads to a huge slowdown in Jetty, but not in Tomcat.
 * Jetty and Tomcat (currently) use the same JSP engine, which marks standalone top-level identifiers, but only Tomcat's JSP API implementation makes
 * use of that information to optimize the lookup.
 *
 * <p>This resolver tries to optimize the case where it finds such a marker, effectively disabling the class lookup for standalone identifiers,
 *  while preserving it for more complex expressions like ${Foo.bar}.
 */
public class CmsJspElResolver extends ELResolver {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspElResolver.class);

    /**
     * The EL context key under which the EL parser flags a stand-alone identifier,
     * or <code>null</code> if the EL implementation in use does not provide the flag.
     */
    private static final Class<?> AST_IDENTIFIER_KEY;

    /** The class for the NotFoundELResolver - we can't just use a class constant because it's not available in EE9 and older. */
    private static final Class<?> NOT_FOUND_EL_RESOLVER;

    static {
        Class<?> key = null;
        try {
            key = Class.forName("org.apache.el.parser.AstIdentifier");
        } catch (ClassNotFoundException e) {
            LOG.warn(
                "EL implementation does not flag stand-alone identifiers, JSP EL performance short-cut is disabled.",
                e);
        }
        AST_IDENTIFIER_KEY = key;
        Class<?> resolverClass = null;
        try {
            resolverClass = Class.forName("jakarta.servlet.jsp.el.NotFoundELResolver");
        } catch (ClassNotFoundException e) {
            LOG.info("Did not find NotFoundELResolver - probably Jakarta EE 9 or older", e);
        }
        NOT_FOUND_EL_RESOLVER = resolverClass;
    }

    /**
     * @see jakarta.el.ELResolver#getCommonPropertyType(jakarta.el.ELContext, java.lang.Object)
     */
    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {

        return base == null ? String.class : null;
    }

    /**
     * @see jakarta.el.ELResolver#getType(jakarta.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {

        return null;
    }

    /**
     * Resolves a stand-alone identifier from the scoped JSP attributes and ends the resolution,
     * so that the identifier is not looked up as a class name.<p>
     *
     * All other cases are left to the standard resolver chain.<p>
     *
     * @see jakarta.el.ELResolver#getValue(jakarta.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {

        if ((AST_IDENTIFIER_KEY == null) || (NOT_FOUND_EL_RESOLVER == null) || (base != null) || (property == null)) {
            return null;
        }
        if (!Boolean.TRUE.equals(context.getContext(AST_IDENTIFIER_KEY))) {
            // not a stand-alone identifier, the identifier may well be an imported class name
            return null;
        }

        if (Boolean.TRUE.equals(context.getContext(NOT_FOUND_EL_RESOLVER))) {
            // the page uses errorOnELNotFound, the chain must reach the NotFoundELResolver
            return null;
        }
        JspContext jspContext = (JspContext)context.getContext(JspContext.class);
        if (jspContext == null) {
            return null;
        }
        String name = property.toString();
        Object result = jspContext.findAttribute(name);
        if (result == null) {
            // a stand-alone identifier may still be an imported static member, this is a plain map lookup
            result = getImportedStaticValue(context, name);
        }
        context.setPropertyResolved(true);
        return result;
    }

    /**
     * @see jakarta.el.ELResolver#isReadOnly(jakarta.el.ELContext, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {

        return false;
    }

    /**
     * @see jakarta.el.ELResolver#setValue(jakarta.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {

        // not resolved here, the standard scoped attribute resolver handles writing
    }

    /**
     * Returns the value of an imported static member with the given name, or <code>null</code> if there is none.<p>
     *
     * @param context the EL context
     * @param name the name of the static member
     *
     * @return the value of the imported static member, or <code>null</code>
     */
    private Object getImportedStaticValue(ELContext context, String name) {

        ImportHandler importHandler = context.getImportHandler();
        if (importHandler == null) {
            return null;
        }
        Class<?> clazz = importHandler.resolveStatic(name);
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getField(name).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            // this is checked when the static import is declared, so it should not happen here
            LOG.debug("Unable to read imported static member \"" + name + "\".", e);
            return null;
        }
    }
}
/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.i18n.CmsEncoder;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.util.CmsStringUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Locale;

/**
 * Master exception type for all exceptions caused in OpenCms.<p>
 * 
 * @since 6.0.0 
 */
public class CmsException extends Exception implements I_CmsThrowable {

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -1372556209321406104L;

    /** The container for the localized message.  */
    protected CmsMessageContainer m_message;

    /**
     * Creates a new localized Exception.<p>
     * 
     * @param message the localized message container to use
     */
    public CmsException(CmsMessageContainer message) {

        super(message.getKey());
        m_message = message;
    }

    /**
     * Creates a new localized Exception that also containers a root cause.<p>
     * 
     * @param message the localized message container to use
     * @param cause the Exception root cause
     */
    public CmsException(CmsMessageContainer message, Throwable cause) {

        super(message.getKey(), cause);
        m_message = message;
    }

    /**
     * Returns the HTML formatted error stack of a Throwable.<p>
     * 
     * The error stack is used by the common error screen 
     * that is displayed if an error occurs.<p>
     * 
     * @param t the throwable to get the errorstack from
     * @return the formatted value of the errorstack parameter
     */
    public static String getFormattedErrorstack(Throwable t) {

        String stacktrace = CmsException.getStackTraceAsString(t);
        if (CmsStringUtil.isEmpty(stacktrace)) {
            return "";
        } else {
            stacktrace = CmsStringUtil.escapeJavaScript(stacktrace);
            stacktrace = CmsEncoder.escapeXml(stacktrace);
            StringBuffer result = new StringBuffer(256);
            result.append("<html><body style='background-color: /*begin-color Window*/#ffffff/*end-color*/; overflow: scroll;'><pre>");
            result.append(stacktrace);
            result.append("</pre></body></html>");
            return result.toString();
        }
    }

    /**
     * Returns the stack trace (including the message) of an exception as a String.<p>
     * 
     * If the exception is a CmsException, 
     * also writes the root cause to the String.<p>
     * 
     * @param e the exception to get the stack trace from
     * @return the stack trace of an exception as a String
     */
    public static String getStackTraceAsString(Throwable e) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        if (e instanceof CmsMultiException) {
            CmsMultiException me = (CmsMultiException)e;
            Iterator<CmsException> it = me.getExceptions().iterator();
            while (it.hasNext()) {
                Throwable t = it.next();
                t.printStackTrace(printWriter);
            }
        }
        return stringWriter.toString();
    }

    /**
     * Creates a copied instance of this localized exception.<p>
     * 
     * @param container the message container
     * @param cause the root cause
     * 
     * @return a copied instance of this localized exception
     */
    public CmsException createException(CmsMessageContainer container, Throwable cause) {

        return new CmsException(container, cause);
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage()
     */
    @Override
    public String getLocalizedMessage() {

        if (m_message == null) {
            return super.getLocalizedMessage();
        }
        return m_message.key();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getLocalizedMessage(Locale)
     */
    public String getLocalizedMessage(Locale locale) {

        return m_message.key(locale);
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {

        return getLocalizedMessage();
    }

    /**
     * @see org.opencms.main.I_CmsThrowable#getMessageContainer()
     */
    public CmsMessageContainer getMessageContainer() {

        return m_message;
    }
}

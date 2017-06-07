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

package org.opencms.gwt;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * GWT RPC Exception. Wrapper for exceptions thrown while processing a RPC request.<p>
 *
 * As of the current state of exception serialization within GWT,
 * details of the original throwable are kept to be available on the client.<p>
 *
 * @since 8.0.0
 */
public class CmsRpcException extends Exception implements IsSerializable {

    /** Serialization uid. */
    private static final long serialVersionUID = 7582056307629544840L;

    /** The original cause message. */
    private String m_originalCauseMessage;

    /** The original class name. */
    private String m_originalClassName;

    /** The original message. */
    private String m_originalMessage;

    /** The original stack trace. */
    private StackTraceElement[] m_originalStackTrace;

    /**
     * Default constructor.<p>
     */
    public CmsRpcException() {

        // empty
    }

    /**
     * Default constructor.<p>
     *
     * @param t the cause
     */
    public CmsRpcException(Throwable t) {

        super(t);
        setOriginalStackTrace(t.getStackTrace());
        setOriginalMessage(t.getLocalizedMessage());
        setOriginalClassName(t.getClass().getName());
        if (t.getCause() != null) {
            setOriginalCauseMessage(t.getCause().getLocalizedMessage());
        }
    }

    /**
     * Returns the cause message.<p>
     *
     * @return the cause message
     */
    public String getOriginalCauseMessage() {

        return m_originalCauseMessage;
    }

    /**
     * Returns the original class name.<p>
     *
     * @return the original class name
     */
    public String getOriginalClassName() {

        return m_originalClassName;
    }

    /**
     * Returns the original message.<p>
     *
     * @return the original message
     */
    public String getOriginalMessage() {

        return m_originalMessage;
    }

    /**
     * Returns the original stack trace.<p>
     *
     * @return the original stack trace
     */
    public StackTraceElement[] getOriginalStackTrace() {

        return m_originalStackTrace;
    }

    /**
     * Sets the original class name.<p>
     *
     * @param originalClassName the original class name to set
     */
    public void setOriginalClassName(String originalClassName) {

        m_originalClassName = originalClassName;
    }

    /**
     * Sets the original message.<p>
     *
     * @param originalMessage the original message to set
     */
    public void setOriginalMessage(String originalMessage) {

        m_originalMessage = originalMessage;
    }

    /**
     * Sets the original cause message.<p>
     *
     * @param originalCauseMessage  the original cause message
     */
    protected void setOriginalCauseMessage(String originalCauseMessage) {

        m_originalCauseMessage = originalCauseMessage;
    }

    /**
     * Sets the original stack trace.<p>
     *
     * @param trace the original stack trace
     */
    protected void setOriginalStackTrace(StackTraceElement[] trace) {

        m_originalStackTrace = trace;
    }
}

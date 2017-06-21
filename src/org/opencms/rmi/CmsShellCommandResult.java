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

package org.opencms.rmi;

import java.io.Serializable;

/**
 * An object containing the output of a remote shell command and also the updated values for some of the
 * internal state fields of the client application.<p>
 */
public class CmsShellCommandResult implements Serializable {

    /** Serial version id. */
    private static final long serialVersionUID = 8485596393085683802L;

    /** The command output. */
    private String m_output;

    /** The error code. */
    private int m_errorCode;

    /** The prompt. */
    private String m_prompt;

    /** The echo mode. */
    private boolean m_echo;

    /** True if exit was called. */
    private boolean m_exitCalled;

    /** True if an error occurred during command execution. */
    private boolean m_hasError;

    /**
     * Gets the error code.<p>
     *
     * @return the error code
     */
    public int getErrorCode() {

        return m_errorCode;
    }

    /**
     * Gets the command output.<p>
     *
     * @return the command output
     */
    public String getOutput() {

        return m_output;
    }

    /**
     * Gets the prompt.<p>
     *
     * @return the prompt
     */
    public String getPrompt() {

        return m_prompt;
    }

    /**
     * Returns the echo mode.<p>
     *
     * @return the echo mode
     */
    public boolean hasEcho() {

        return m_echo;
    }

    /**
     * Returns true if an error has occurred.<p>
     *
     * @return true if an error has occurred
     */
    public boolean hasError() {

        return m_hasError;
    }

    /**
     * Returns true if exit was called.<p>
     *
     * @return true if exit was called
     */
    public boolean isExitCalled() {

        return m_exitCalled;
    }

    /**
     * Sets the echo mode.<p>
     *
     * @param echo the echo mode
     */
    public void setEcho(boolean echo) {

        m_echo = echo;
    }

    /**
     * Sets the error code.<p>
     *
     * @param errorCode the error code
     */
    public void setErrorCode(int errorCode) {

        m_errorCode = errorCode;
    }

    /**
     * Sets the 'exitCalled' flag.<p>
     *
     * @param exitCalled the new value
     */
    public void setExitCalled(boolean exitCalled) {

        m_exitCalled = exitCalled;
    }

    /**
     * Sets the error mode.<p>
     *
     * @param hasError the error mode
     */
    public void setHasError(boolean hasError) {

        m_hasError = hasError;
    }

    /**
     * Sets the command output.<p>
     *
     * @param outputString the command output
     */
    public void setOutput(String outputString) {

        m_output = outputString;
    }

    /**
     * Sets the prompt.<p>
     *
     * @param prompt the prompt
     */
    public void setPrompt(String prompt) {

        m_prompt = prompt;
    }

}

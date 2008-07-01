/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsServletContainerSettings.java,v $
 * Date   : $Date: 2008/07/01 13:17:16 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.util.A_CmsModeStringEnumeration;

/**
 * Stores specific servlet container options, that might influence OpenCms behavior.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 7.0.5 
 */
public class CmsServletContainerSettings {

    /**
     *  Enumeration class for the configuration mode.<p>
     */
    public static final class CmsServletContainerCfgMode extends A_CmsModeStringEnumeration {

        /** Auto configuration mode. */
        protected static final CmsServletContainerCfgMode MODE_AUTO = new CmsServletContainerCfgMode("auto");

        /** Manual configuration mode. */
        protected static final CmsServletContainerCfgMode MODE_MANUAL = new CmsServletContainerCfgMode("manual");

        /** No set configuration mode. */
        protected static final CmsServletContainerCfgMode MODE_NONE = new CmsServletContainerCfgMode("none");

        /** Version id required for safe serialization. */
        private static final long serialVersionUID = -8191582624108081577L;

        /**
         * Private constructor.<p>
         * 
         * @param mode the remote command execution return type integer representation
         */
        private CmsServletContainerCfgMode(String mode) {

            super(mode);
        }

        /**
         * Returns the parsed mode object if the string representation matches, or <code>null</code> if not.<p>
         * 
         * @param mode the string representation to parse
         * 
         * @return the parsed mode object
         */
        public static CmsServletContainerCfgMode valueOf(String mode) {

            if (mode == null) {
                return null;
            }
            if (mode.equalsIgnoreCase(MODE_NONE.getMode())) {
                return MODE_NONE;
            }
            if (mode.equalsIgnoreCase(MODE_MANUAL.getMode())) {
                return MODE_MANUAL;
            }
            if (mode.equalsIgnoreCase(MODE_AUTO.getMode())) {
                return MODE_AUTO;
            }
            return null;
        }

        /**
         * Checks if this is the none mode.<p>
         * 
         * @return <code>true</code> if this is the none mode
         */
        public boolean isNone() {

            return this == MODE_NONE;
        }

        /**
         * Checks if this is the auto mode.<p>
         * 
         * @return <code>true</code> if this is the auto mode
         */
        public boolean isAuto() {

            return this == MODE_AUTO;
        }

        /**
         * Checks if this is the manual mode.<p>
         * 
         * @return <code>true</code> if this is the manual mode
         */
        public boolean isManual() {

            return this == MODE_MANUAL;
        }
    }

    /** String remote command execution return type. */
    public static final CmsServletContainerCfgMode CFG_MODE_AUTO = CmsServletContainerCfgMode.MODE_AUTO;

    /** Map remote command execution return type. */
    public static final CmsServletContainerCfgMode CFG_MODE_MANUAL = CmsServletContainerCfgMode.MODE_MANUAL;

    /** List remote command execution return type. */
    public static final CmsServletContainerCfgMode CFG_MODE_NONE = CmsServletContainerCfgMode.MODE_NONE;

    /** 
     * The replacement request attribute for the {@link javax.servlet.http.HttpServletRequest#getPathInfo()} method, 
     * which is needed because this method is not properly implemented in BEA WLS 9.x.<p>
     */
    private static final String REQUEST_ERROR_PAGE_ATTRIBUTE_WEBLOGIC = "weblogic.servlet.errorPage";

    /** Constant name to identify Resin servers. */
    private static final String SERVLET_CONTAINER_RESIN = "Resin";

    /** Constant name to identify BEA WebLogic servers. */
    private static final String SERVLET_CONTAINER_WEBLOGIC = "WebLogic Server";

    /** Constant name to identify IBM Websphere servers. */
    private static final String SERVLET_CONTAINER_WEBSPHERE = "IBM WebSphere Application Server";

    /** The configuration mode. */
    private CmsServletContainerCfgMode m_mode = CFG_MODE_NONE;

    /** If the flex response has to prevent buffer flushing, for instance, Websphere does not allow to set headers afterwards, so we have to prevent it. */
    private boolean m_preventResponseFlush;

    /** If the tags need to be released after ending, this has to be prevented when running with Resin, for example. */
    private boolean m_releaseTagsAfterEnd = true;

    /** 
     * The request error page attribute to use if {@link javax.servlet.http.HttpServletRequest#getPathInfo()}
     * is not working properly, like in BEA WLS 9.x. 
     */
    private String m_requestErrorPageAttribute;

    /** The name of the servlet container running OpenCms. */
    private String m_servletContainerName;

    /** If the servlet can throw an exception if initialization fails, for instance, Weblogic and Resin have problems with the exception. */
    private boolean m_servletThrowsException = true;

    /**
     * Creates a new object.<p>
     * 
     * @param servletContainerName the name of the current running servlet container
     */
    public CmsServletContainerSettings(String servletContainerName) {

        if (servletContainerName == null) {
            m_servletContainerName = "";
            // use defaults
            return;
        }
        m_servletContainerName = servletContainerName;

        // the tags behavior
        m_releaseTagsAfterEnd = !(m_servletContainerName.indexOf(SERVLET_CONTAINER_RESIN) > -1);

        // the request error page attribute
        m_requestErrorPageAttribute = null;
        if (m_servletContainerName.indexOf(SERVLET_CONTAINER_WEBLOGIC) > -1) {
            m_requestErrorPageAttribute = REQUEST_ERROR_PAGE_ATTRIBUTE_WEBLOGIC;
        }

        // the failed initialization behavior
        m_servletThrowsException = true;
        m_servletThrowsException &= (m_servletContainerName.indexOf(SERVLET_CONTAINER_RESIN) < 0);
        m_servletThrowsException &= (m_servletContainerName.indexOf(SERVLET_CONTAINER_WEBLOGIC) < 0);

        // the flush flex response behavior
        m_preventResponseFlush = false;
        m_preventResponseFlush |= (m_servletContainerName.indexOf(SERVLET_CONTAINER_WEBSPHERE) > -1);
        m_preventResponseFlush |= (m_servletContainerName.indexOf(SERVLET_CONTAINER_RESIN) > -1);
    }

    /**
     * Returns the mode.<p>
     *
     * @return the mode
     */
    public CmsServletContainerCfgMode getMode() {

        return m_mode;
    }

    /**
     * Returns the request error page attribute.<p>
     *
     * @return the request error page attribute
     */
    public String getRequestErrorPageAttribute() {

        return m_requestErrorPageAttribute;
    }

    /**
     * Returns the name of the servlet container running OpenCms.<p>
     *
     * @return the name of the servlet container running OpenCms
     */
    public String getServletContainerName() {

        return m_servletContainerName;
    }

    /**
     * Checks if the flex response has to prevent buffer flushing.<p>
     *
     * @return <code>true</code> if the flex response has to prevent buffer flushing
     */
    public boolean isPreventResponseFlush() {

        return m_preventResponseFlush;
    }

    /**
     * Checks if the tags need to be released after ending.<p>
     *
     * @return <code>true</code> if the tags need to be released after ending
     */
    public boolean isReleaseTagsAfterEnd() {

        return m_releaseTagsAfterEnd;
    }

    /**
     * Checks if the servlet can throw an exception if initialization fails.<p>
     *
     * @return <code>true</code> if the servlet can throw an exception if initialization fails
     */
    public boolean isServletThrowsException() {

        return m_servletThrowsException;
    }

    /**
     * Sets the mode from the configuration.<p>
     * 
     * @param configValue the mode to set
     */
    public void setMode(String configValue) {

        m_mode = CmsServletContainerCfgMode.valueOf(configValue);
    }

    /**
     * Sets if the flex response has to prevent buffer flushing.<p>
     *
     * @param preventResponseFlush the flag to set
     */
    public void setPreventResponseFlush(boolean preventResponseFlush) {

        m_preventResponseFlush = preventResponseFlush;
    }

    /**
     * Sets if the tags need to be released after ending.<p>
     *
     * @param releaseTagsAfterEnd the flag to set
     */
    public void setReleaseTagsAfterEnd(boolean releaseTagsAfterEnd) {

        m_releaseTagsAfterEnd = releaseTagsAfterEnd;
    }

    /**
     * Sets the request error page attribute.<p>
     *
     * @param requestErrorPageAttribute the request error page attribute to set
     */
    public void setRequestErrorPageAttribute(String requestErrorPageAttribute) {

        m_requestErrorPageAttribute = requestErrorPageAttribute;
    }

    /**
     * Sets if the servlet can throw an exception if initialization fails.<p>
     *
     * @param servletThrowsException the flag to set
     */
    public void setServletThrowsException(boolean servletThrowsException) {

        m_servletThrowsException = servletThrowsException;
    }
}

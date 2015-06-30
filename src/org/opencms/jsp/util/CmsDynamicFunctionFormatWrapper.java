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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.containerpage.CmsDynamicFunctionBean;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Wrapper class for dynamic function formats which can be used from JSP EL.<p>
 */
public class CmsDynamicFunctionFormatWrapper {

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDynamicFunctionFormatWrapper.class);

    /** The current cms context. */
    private CmsObject m_cms;

    /** The dynamic function format. */
    private CmsDynamicFunctionBean.Format m_format;

    /** The jsp resource of the dynamic function format. */
    private CmsResource m_jspResource;

    /**
     * Creates a new wrapper instance for a given format.<p>
     *
     * The format parameter may be null.
     *
     * @param cms the current CMS context
     * @param format the dynamic function format
     */
    public CmsDynamicFunctionFormatWrapper(CmsObject cms, CmsDynamicFunctionBean.Format format) {

        m_cms = cms;
        m_format = format;
        if (format != null) {
            try {
                m_jspResource = cms.readResource(format.getJspStructureId());
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Check if this format is actually valid, i.e. was not created with a null format argument.<p>
     *
     * @return true if this format is valid
     */
    public boolean getExists() {

        return m_jspResource != null;
    }

    /**
     * Gets the jsp site path for this dynamic function format.<p>
     *
     * @return the jsp site path for this dynamic function format
     */
    public String getJsp() {

        if (m_jspResource != null) {
            return m_cms.getSitePath(m_jspResource);
        }
        return "";
    }

    /**
     * Gets the parameters for this dynamic function format.<p>
     *
     * @return the map of parameters for the dynamic function
     */
    public Map<String, String> getParam() {

        return getParameters();
    }

    /**
     * Gets the parameters for this dynamic function format.<p>
     *
     * @return the map of parameters for the dynamic function
     */
    public Map<String, String> getParameters() {

        if (m_format != null) {
            return m_format.getParameters();
        }
        return Collections.emptyMap();
    }

}

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

package org.opencms.ade.publish.shared;

import org.opencms.util.CmsUUID;

import java.io.Serializable;
import java.util.Map;

/**
 * Bean encapsulating all ADE publish options.<p>
 *
 * @since 7.6
 */
public class CmsPublishOptions implements Serializable {

    /** Parameter name for the collector items. */
    public static final String PARAM_COLLECTOR_ITEMS = "collectorItems";

    /** The collector information. */
    public static final String PARAM_COLLECTOR_INFO = "collectorInfo";

    /** Parameter name for the container page structure id. */
    public static final String PARAM_CONTAINERPAGE = "containerpage";

    /** Parameter name for the content structure id. */
    public static final String PARAM_CONTENT = "content";

    /** Parameter name for the detail content structure id. */
    public static final String PARAM_DETAIL = "detail";

    /** Parameter for enabling the 'add contents' check box. */
    public static final String PARAM_ENABLE_INCLUDE_CONTENTS = "enable_include_contents";

    /** The name of the parameter used for passing in the list of resources. */
    public static final String PARAM_FILES = "files";

    /** The name of the parameter which controls whether to add sub-resources of folders. */
    public static final String PARAM_INCLUDE_CONTENTS = "include_contents";

    /** Parameter for indicating that the initial project should be the 'current page' virtual project. */
    public static final String PARAM_START_WITH_CURRENT_PAGE = "startWithCurrentPage";

    /** The serial version id. */
    private static final long serialVersionUID = 1L;

    /** Flag to include related resources. */
    private boolean m_includeRelated;

    /** Flag to include siblings. */
    private boolean m_includeSiblings;

    /** The additional publish parameters. */
    private Map<String, String> m_params;

    /** The id of the project to publish. */
    private CmsUUID m_projectId;

    /**
     * Creates a new publish options bean.<p>
     **/
    public CmsPublishOptions() {

        m_includeRelated = true;
    }

    /**
     * Creates a new publish options bean.<p>
     *
     * @param includeRelated Flag to include related resources
     * @param includeSiblings Flag to include siblings
     * @param projectId The id of the project to publish
     */
    public CmsPublishOptions(boolean includeRelated, boolean includeSiblings, CmsUUID projectId) {

        m_includeRelated = includeRelated;
        m_includeSiblings = includeSiblings;
        m_projectId = projectId;
    }

    /**
     * Creates a new instance.<p>
     *
     * @param params the additional publish parameters
     */
    public CmsPublishOptions(Map<String, String> params) {

        this();
        m_params = params;
    }

    /**
     * Gets the additional publish parameters.<p>
     *
     * @return the additional publish parameters
     */
    public Map<String, String> getParameters() {

        return m_params;
    }

    /**
     * Returns the project id.<p>
     *
     * @return the project id
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * Checks if to include related resources.<p>
     *
     * @return <code>true</code> if to include related resources
     */
    public boolean isIncludeRelated() {

        return m_includeRelated;
    }

    /**
     * Checks if to include siblings.<p>
     *
     * @return <code>true</code> if to include siblings
     */
    public boolean isIncludeSiblings() {

        return m_includeSiblings;
    }

    /**
     * Sets the flag to include related resources.<p>
     *
     * @param includeRelated the flag to set
     */
    public void setIncludeRelated(boolean includeRelated) {

        m_includeRelated = includeRelated;
    }

    /**
     * Sets the flag to include siblings.<p>
     *
     * @param includeSiblings the flag to set
     */
    public void setIncludeSiblings(boolean includeSiblings) {

        m_includeSiblings = includeSiblings;
    }

    /**
     * Sets the additional publish parameters.<p>
     *
     * @param params the additional parameters to set
     */
    public void setParameters(Map<String, String> params) {

        m_params = params;
    }

    /**
     * Sets the id of the project to publish.<p>
     *
     * @param projectId the id to set
     */
    public void setProjectId(CmsUUID projectId) {

        m_projectId = projectId;
    }
}

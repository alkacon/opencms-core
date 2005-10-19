/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheckResult.java,v $
 * Date   : $Date: 2005/10/19 08:33:28 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds the results of the content tests and provides methods to access the collected
 * errors and warnings.<p>
 * 
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.0 
 */
public class CmsContentCheckResult {

    /** List of all CmsContentCheckResource with collected warnings or errors. */
    private List m_allCheckResources;

    /** List of all resources with collected warnings or errors. */
    private List m_allResources;

    /** List of all CmsContentCheckResource with collected errors. */
    private List m_errorCheckResources;

    /** List containing all recouces that collected errors. */
    private List m_errorResources;

    /** 
     * Map containing all all collected errors. Resourcenames are used as keys,
     * lists are used as values 
     */
    private Map m_errors;

    /** List of all CmsContentCheckResource with collected warnings. */
    private List m_warningCheckResources;

    /** List containing all recouces that collected warnings. */
    private List m_warningResources;

    /** 
     * Map containing all all collected warnings. Resourcenames are used as keys,
     * lists are used as values 
     */
    private Map m_warnings;

    /**
     * Constructor, creates an empty CmsContentCheckResult.<p>     *
     */
    public CmsContentCheckResult() {

        m_errors = new HashMap();
        m_warnings = new HashMap();
        m_errorResources = new ArrayList();
        m_warningResources = new ArrayList();
        m_allResources = new ArrayList();
        m_errorCheckResources = new ArrayList();
        m_warningCheckResources = new ArrayList();
        m_allCheckResources = new ArrayList();
    }

    /**
     * Adds the testing results of a CmsContentCheckResource to the result lists.<p>
     * @param testResource the CmsContentCheckResource to add the results from
     */
    public void addResult(CmsContentCheckResource testResource) {

        List warnings = testResource.getWarnings();
        List errors = testResource.getErrors();
        // add the warnings if there were any
        if (warnings != null && warnings.size() > 0) {
            m_warnings.put(testResource.getResourceName(), warnings);
            m_warningResources.add(testResource.getResource());
            m_warningCheckResources.add(testResource);
        }
        // add the errors if there were any
        if (errors != null && errors.size() > 0) {
            m_errors.put(testResource.getResourceName(), errors);
            m_errorResources.add(testResource.getResource());
            m_errorCheckResources.add(testResource);
        }
        m_allResources.add(testResource.getResource());
        m_allCheckResources.add(testResource);
    }

    /**
     * Gets a list of all CmsContentCheckResource that colleced an error or a warning during the content check.<p>
     * @return List of CmsContentCheckResource which collected an error or a warning.
     */
    public List getAllCheckResources() {

        return m_allResources;
    }

    /**
     * Gets a list of all resources that colleced an error or a warning during the content check.<p>
     * @return List of CmsResources which collected an error or a warning.
     */
    public List getAllResources() {

        return m_allResources;
    }

    /**
     * Gets a list of all CmsContentCheckResource that colleced an error during the content check.<p>
     * @return List of CmsContentCheckResource which collected an error.
     */
    public List getErrorCheckResources() {

        return m_errorCheckResources;
    }

    /**
     * Gets a list of all resources that colleced an error during the content check.<p>
     * @return List of CmsResources which collected an error.
     */
    public List getErrorResources() {

        return m_errorResources;
    }

    /**
     * Gets a map of all error collected during the content check. <p>
     * 
     * The map contains the complete resource root path as keys and a list of errors
     * as values.
     * @return map of collected warnings
     */
    public Map getErrors() {

        return m_errors;
    }

    /** 
     * Gets a list of errors collected during the content check for a given
     * resource.<p>
     * 
     * @param resourceName the complete root path of the resource to get the list from
     * @return list of error messages or null if no warnings are found
     */
    public List getErrors(String resourceName) {

        return (List)m_errors.get(resourceName);
    }

    /**
     * Gets a list of all CmsContentCheckResource that colleced a warning during the content check.<p>
     * @return List of CmsContentCheckResource which collected a warning.
     */
    public List getWarningCheckResources() {

        return m_warningCheckResources;
    }

    /**
     * Gets a list of all resources that colleced a warning during the content check.<p>
     * @return List of CmsResources which collected a warning.
     */
    public List getWarningResources() {

        return m_warningResources;
    }

    /**
     * Gets a map of all warnings collected during the content check. <p>
     * 
     * The map contains the complete resource root path as keys and a list of warnings
     * as values.
     * @return map of collected warnings
     */
    public Map getWarnings() {

        return m_warnings;
    }

    /** 
     * Gets a list of warnings collected during the content check for a given
     * resource.<p>
     * 
     * @param resourceName the complete root path of the resource to get the list from
     * @return list of warning messages or null if no warnings are found
     */
    public List getWarnings(String resourceName) {

        return (List)m_warnings.get(resourceName);
    }

}

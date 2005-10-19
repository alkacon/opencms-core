/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsProjectFilesCollector.java,v $
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

package org.opencms.workplace.tools.projects;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Collector for receiving CmsResources from a CmsContentCheckResult.<p>
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.1.0 
 */
public class CmsProjectFilesCollector implements I_CmsResourceCollector {

    /** Parameter of the default collector name. */
    public static final String COLLECTOR_NAME = "projectresources";

    /** Parameter to get all changed resources. */
    public static final String PARAM_CHANGED = "changed";

    /** Parameter to get all deleted resources. */
    public static final String PARAM_DELETED = "deleted";

    /** Parameter to get all modified resources. */
    public static final String PARAM_MODIFIED = "modiefied";

    /** Parameter to get all new resources. */
    public static final String PARAM_NEW = "new";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProjectFilesCollector.class);

    /** The collector name. */
    private String m_collectorName;

    /** The colelctor parameter. */
    private String m_collectorParameter;

    /** Sort order. Not used yet. */
    private int m_order;

    /**
     * Constructor, creates a new CmsProjectFilesCollector.<p>
     */
    public CmsProjectFilesCollector() {

        m_collectorName = COLLECTOR_NAME;
        m_collectorParameter = PARAM_MODIFIED + "|0";
        m_order = 0;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {

        // TODO: Auto-generated method stub
        return 0;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCollectorNames()
     */
    public List getCollectorNames() {

        List names = new ArrayList();
        names.add(COLLECTOR_NAME);
        return names;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject)
     */
    public String getCreateLink(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateLink(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateLink(CmsObject cms, String collectorName, String param) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject)
     */
    public String getCreateParam(CmsObject cms) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getCreateParam(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    public String getCreateParam(CmsObject cms, String collectorName, String param) {

        return null;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorName()
     */
    public String getDefaultCollectorName() {

        return m_collectorName;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getDefaultCollectorParam()
     */
    public String getDefaultCollectorParam() {

        return m_collectorParameter;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getOrder()
     */
    public int getOrder() {

        return m_order;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject)
     */
    public List getResults(CmsObject cms) {

        return getResults(cms, COLLECTOR_NAME, m_collectorParameter);
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#getResults(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     * The parameter must follow the syntax "mode|projectId" where mode is either "new", "changed", "deleted" 
     * or "modified" and projectId is the id of the project to be displayed.
     */
    public List getResults(CmsObject cms, String collectorName, String parameter) {

        if (parameter == null) {
            parameter = m_collectorParameter;
        }

        List params = CmsStringUtil.splitAsList(parameter, "|");
        String param = (String)params.get(0);
        String projectId = (String)params.get(1);

        int state;
        if (param.equals(PARAM_NEW)) {
            state = CmsResource.STATE_NEW;
        } else if (param.equals(PARAM_CHANGED)) {
            state = CmsResource.STATE_CHANGED;
        } else if (param.equals(PARAM_DELETED)) {
            state = CmsResource.STATE_DELETED;
        } else {
            state = CmsResource.STATE_KEEP;
        }

        // show files in the selected project with the selected status
        try {
            return cms.readProjectView(new Integer(projectId).intValue(), state);
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorName(java.lang.String)
     */
    public void setDefaultCollectorName(String collectorName) {

        m_collectorName = collectorName;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setDefaultCollectorParam(java.lang.String)
     * The parameter must follow the syntax "mode|projectId" where mode is either "new", "changed", "deleted" 
     * or "modified" and projectId is the id of the project to be displayed.
     */
    public void setDefaultCollectorParam(String param) {

        m_collectorParameter = param;
    }

    /**
     * @see org.opencms.file.collectors.I_CmsResourceCollector#setOrder(int)
     */
    public void setOrder(int order) {

        m_order = order;
    }

}

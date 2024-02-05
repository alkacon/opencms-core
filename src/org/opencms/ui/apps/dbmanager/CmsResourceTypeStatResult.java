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

package org.opencms.ui.apps.dbmanager;

import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

/**
 * Class for the result of a database statistic set.<p>
 */
public class CmsResourceTypeStatResult {

    /**Count of resources.*/
    private int m_count;

    /**Site root.*/
    private String m_siteRoot;

    /**Time when the statistic was generated.*/
    private long m_timestamp;

    /**Type of resource.*/
    private I_CmsResourceType m_type;

    /**
     * Public constructor.<p>
     *
     * @param type of resources
     * @param siteRoot of site
     * @param count of found resources
     */
    public CmsResourceTypeStatResult(I_CmsResourceType type, String siteRoot, int count) {
        m_type = type;
        m_count = count;
        m_siteRoot = siteRoot;
        m_timestamp = System.currentTimeMillis();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {

        if (!(o instanceof CmsResourceTypeStatResult)) {
            return false;
        }
        CmsResourceTypeStatResult result = (CmsResourceTypeStatResult)o;

        if (m_type != null) {
            return m_type.equals(result.getType()) & m_siteRoot.equals(result.getSiteRoot());
        }
        return (result.getType() == null) & m_siteRoot.equals(result.getSiteRoot());
    }

    /**
     * Gets the localized result message.<p>
     *
     * @return the result as string
     */
    public String getResult() {

        String res;
        CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(m_siteRoot);
        if (site == null) {
            res = CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATS_RESULTS_ROOT_1, Integer.valueOf(m_count));
        } else {
            res = CmsVaadinUtils.getMessageText(
                Messages.GUI_DATABASEAPP_STATS_RESULTS_2,
                site.getTitle(),
                Integer.valueOf(m_count));
        }
        return res;
    }

    /**
     * Getter for site root.<p>
     *
     * @return site roor
     */
    public String getSiteRoot() {

        return m_siteRoot;
    }

    /**
     * Getter for timestamp.<p>
     *
     * @return the timestamp
     */
    public long getTimestamp() {

        return m_timestamp;
    }

    /**
     * Getter for resource type.<p>
     *
     * @return the resource type
     */
    public I_CmsResourceType getType() {

        return m_type;
    }

    /**
     * Gets the title of the resource type.<p>
     *
     * @return title of resource type
     */
    public String getTypeTitle() {

        if (m_type == null) {
            return CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_STATS_ALL_RESOURCES_0);
        }

        String res = "";
        CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
            m_type.getTypeName());
        res = CmsVaadinUtils.getMessageText(typeSetting.getKey());
        return res;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return super.hashCode();
    }
}

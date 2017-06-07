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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Util class to find broken links in a bundle of resources to be published.<p>
 *
 * @since 6.5.5
 */
public class CmsRelationPublishValidator {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRelationPublishValidator.class);

    /** The internal computed broken relations map. */
    protected Map<String, List<CmsRelation>> m_brokenRelations;

    /** the cms context object. */
    private CmsObject m_cms;

    /**
     * Creates a new helper object.<p>
     *
     * @param cms the cms object
     * @param publishList a publish list to validate
     */
    public CmsRelationPublishValidator(CmsObject cms, CmsPublishList publishList) {

        try {
            m_cms = OpenCms.initCmsObject(cms);
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
            m_cms = cms;
        }
        try {
            m_brokenRelations = OpenCms.getPublishManager().validateRelations(m_cms, publishList, null);
        } catch (Exception e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Returns the information bean for the given entry.<p>
     *
     * @param resourceName the entry name
     *
     * @return the information bean for the given entry
     */
    public CmsRelationValidatorInfoEntry getInfoEntry(String resourceName) {

        String resName = resourceName;
        String siteRoot = m_cms.getRequestContext().getSiteRoot();
        String siteName = null;
        if (resName.startsWith(m_cms.getRequestContext().getSiteRoot())) {
            resName = m_cms.getRequestContext().removeSiteRoot(resName);
        } else {
            siteRoot = OpenCms.getSiteManager().getSiteRoot(resName);
            siteName = siteRoot;
            if (siteRoot != null) {
                String oldSite = m_cms.getRequestContext().getSiteRoot();
                try {
                    m_cms.getRequestContext().setSiteRoot("/");
                    siteName = m_cms.readPropertyObject(siteRoot, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue(
                        siteRoot);
                } catch (CmsException e) {
                    siteName = siteRoot;
                } finally {
                    m_cms.getRequestContext().setSiteRoot(oldSite);
                }
                resName = resName.substring(siteRoot.length());
            } else {
                siteName = "/";
            }
        }
        return new CmsRelationValidatorInfoEntry(
            resourceName,
            resName,
            siteName,
            siteRoot,
            Collections.unmodifiableList(m_brokenRelations.get(resourceName)));
    }

    /**
     * If no relation would be broken deleting the given resources.<p>
     *
     * @return <code>true</code> if no relation would be broken deleting the given resources
     */
    public boolean isEmpty() {

        return m_brokenRelations.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     *
     * @return the broken relations key set
     */
    public Set<String> keySet() {

        return m_brokenRelations.keySet();
    }

    /**
     * @see java.util.Map#values()
     *
     * @return the broken relations value set
     */
    public Collection<List<CmsRelation>> values() {

        return m_brokenRelations.values();
    }
}

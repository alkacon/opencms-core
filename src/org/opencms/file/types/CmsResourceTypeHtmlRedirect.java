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

package org.opencms.file.types;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.loader.CmsRedirectLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * The HTML redirect resource type.<p>
 */
public class CmsResourceTypeHtmlRedirect extends CmsResourceTypeXmlAdeConfiguration {

    private static final Log LOG = CmsLog.getLog(CmsResourceTypeHtmlRedirect.class);

    /** The serial version id. */
    private static final long serialVersionUID = 2757710991033290640L;

    /** Type name constant. */
    public static final String TYPE_NAME = "htmlredirect";

    /**
     * Checks if the htmlredirect should be excluded from the XML sitemap.
     *
     * @param cms the CMS context
     * @param resource the resource
     * @return true if the htmlredirect should be excluded
     */
    public static boolean checkExcludeFromSitemap(CmsObject cms, CmsResource resource) {

        try {
            CmsFile file = cms.readFile(resource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
            content.getValue(CmsADEManager.N_LINK, Locale.ENGLISH);
            String type = content.getValue(CmsADEManager.N_TYPE, Locale.ENGLISH).getStringValue(cms);
            if (type.equals("sublevel")) {
                return false;
            }
            I_CmsXmlContentValue linkValue = content.getValue(CmsADEManager.N_LINK, Locale.ENGLISH);

            if (linkValue == null) {
                return true;
            }
            String linkString = linkValue.getStringValue(cms);
            try {
                URI uri = new URI(linkString);
            } catch (URISyntaxException e) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return true;
        }
    }

    /**
     * Checks if the given resource is a htmlredirect.
     *
     * @param resource the resource to check
     * @return true if it is an htmlredirect
     */
    public static boolean isRedirect(CmsResource resource) {

        return OpenCms.getResourceManager().matchResourceType(TYPE_NAME, resource.getTypeId());
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsRedirectLoader.LOADER_ID;
    }
}

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

package org.opencms.ade.containerpage.inherited;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for inheritance groups which don't fit anywhere else.<p>
 */
public final class CmsInheritanceGroupUtils {

    /**
     * Private constructor to prevent instantiation.<p>
     */
    private CmsInheritanceGroupUtils() {

        // do nothing
    }

    /**
     * Finds the inheritance group content with a given internal name.<p>
     *
     * Currently this is implemented as a property search, which may be potentially slow.<p>
     *
     * @param cms the current CMS context
     * @param name the name to search
     *
     * @return the inheritance group resource
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsResource getInheritanceGroupContentByName(CmsObject cms, String name) throws CmsException {

        String oldSiteRoot = cms.getRequestContext().getSiteRoot();
        try {
            cms.getRequestContext().setSiteRoot("");
            List<CmsResource> resources = cms.readResourcesWithProperty(
                "/",
                CmsPropertyDefinition.PROPERTY_KEYWORDS,
                name);
            Iterator<CmsResource> resourceIter = resources.iterator();
            while (resourceIter.hasNext()) {
                CmsResource currentRes = resourceIter.next();
                if (!OpenCms.getResourceManager().getResourceType(currentRes).getTypeName().equals(
                    "inheritance_group")) {
                    resourceIter.remove();
                }
            }
            if (resources.isEmpty()) {
                throw new CmsVfsResourceNotFoundException(
                    org.opencms.gwt.Messages.get().container(
                        org.opencms.gwt.Messages.ERR_INHERITANCE_GROUP_NOT_FOUND_1,
                        name));
            }
            return resources.get(0);
        } finally {
            cms.getRequestContext().setSiteRoot(oldSiteRoot);
        }
    }

    /**
     * Parses an inheritance group configuration C and returns the names of inheritance groups in C in which a given resource
     * is defined as a new element.<p>
     *
     * @param cms the current CMS context
     * @param inheritanceConfig the inheritance configuration resource
     * @param target the resource to search in the inheritance configuration
     *
     * @return the names of the inheritance groups in which the target resource is defined as  a new element
     *
     * @throws CmsException if something goes wrong
     */
    public static Set<String> getNamesOfGroupsContainingResource(
        CmsObject cms,
        CmsResource inheritanceConfig,
        CmsResource target) throws CmsException {

        Set<String> names = new HashSet<String>();
        CmsContainerConfigurationParser parser = new CmsContainerConfigurationParser(cms);
        parser.parse(inheritanceConfig);
        Map<Locale, Map<String, CmsContainerConfiguration>> contents = parser.getParsedResults();
        for (Map<String, CmsContainerConfiguration> mapForLocale : contents.values()) {
            for (Map.Entry<String, CmsContainerConfiguration> entry : mapForLocale.entrySet()) {
                String key = entry.getKey();
                CmsContainerConfiguration config = entry.getValue();
                for (CmsContainerElementBean element : config.getNewElements().values()) {
                    if (element.getId().equals(target.getStructureId())) {
                        names.add(key);
                    }
                }
            }
        }
        return names;
    }

}

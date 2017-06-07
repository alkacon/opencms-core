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

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReference;
import org.opencms.ade.containerpage.inherited.CmsInheritanceReferenceParser;
import org.opencms.ade.containerpage.inherited.CmsInheritedContainerState;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * The XML content handler class for inheritance groups.
 */
public class CmsXmlInheritGroupContainerHandler extends CmsDefaultXmlContentHandler {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlInheritGroupContainerHandler.class);

    /**
     * Constructor.<p>
     */
    public CmsXmlInheritGroupContainerHandler() {

        super();
    }

    /**
     * Returns the elements of the given inheritance group for the request context URI.<p>
     *
     * @param cms the current cms context
     * @param resource the inheritance group resource
     *
     * @return the elements
     */
    public static List<CmsContainerElementBean> loadInheritContainerElements(CmsObject cms, CmsResource resource) {

        CmsInheritanceReferenceParser parser = new CmsInheritanceReferenceParser(cms);
        try {
            parser.parse(resource);
            CmsInheritanceReference ref = parser.getReference(cms.getRequestContext().getLocale());
            if (ref != null) {
                String name = ref.getName();
                CmsADEManager adeManager = OpenCms.getADEManager();
                CmsInheritedContainerState result = adeManager.getInheritedContainerState(
                    cms,
                    cms.getRequestContext().getRootUri(),
                    name);
                return result.getElements(false);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#getCSSHeadIncludes(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public Set<String> getCSSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsException {

        Set<String> result = new LinkedHashSet<String>();

        List<CmsContainerElementBean> containerElements = loadInheritContainerElements(cms, resource);
        for (CmsContainerElementBean elementBean : containerElements) {
            if (elementBean.isGroupContainer(cms) || elementBean.isInheritedContainer(cms)) {
                throw new CmsException(
                    Messages.get().container(
                        Messages.ERR_ELEMENT_GROUP_REFERENCES_ANOTHER_GROUP_2,
                        resource.getRootPath(),
                        elementBean.getResource().getRootPath()));
            }
            CmsResource elementResource = elementBean.getResource();
            Set<String> elementIncludes = CmsXmlContentDefinition.getContentHandlerForResource(
                cms,
                elementResource).getCSSHeadIncludes(cms, elementResource);
            result.addAll(elementIncludes);
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#getJSHeadIncludes(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public Set<String> getJSHeadIncludes(CmsObject cms, CmsResource resource) throws CmsException {

        Set<String> result = new LinkedHashSet<String>();
        List<CmsContainerElementBean> containerElements = loadInheritContainerElements(cms, resource);
        for (CmsContainerElementBean elementBean : containerElements) {
            if (elementBean.isGroupContainer(cms) || elementBean.isInheritedContainer(cms)) {
                throw new CmsException(
                    Messages.get().container(
                        Messages.ERR_ELEMENT_GROUP_REFERENCES_ANOTHER_GROUP_2,
                        resource.getRootPath(),
                        elementBean.getResource().getRootPath()));
            }
            CmsResource elementResource = elementBean.getResource();
            Set<String> elementIncludes = CmsXmlContentDefinition.getContentHandlerForResource(
                cms,
                elementResource).getJSHeadIncludes(cms, elementResource);
            result.addAll(elementIncludes);
        }
        return result;
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#hasModifiableFormatters()
     */
    @Override
    public boolean hasModifiableFormatters() {

        return false;
    }

}

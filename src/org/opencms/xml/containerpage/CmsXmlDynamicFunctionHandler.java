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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is the XML content handler class for the "dynamic functionality" resource type.<p>
 *
 * This resource type needs special handling of formatters and element settings: They are
 * read from each content of this type rather than from the XSD.<p>
 */
public class CmsXmlDynamicFunctionHandler extends CmsDefaultXmlContentHandler {

    /** The node name for the formatter settings. */
    public static final String N_CONTAINER_SETTINGS = "ContainerSettings";

    /** The resource type for dynamic functions. */
    public static final String TYPE_FUNCTION = "function";

    /**
     * Default constructor.<p>
     */
    public CmsXmlDynamicFunctionHandler() {

        super();
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#getFormatterConfiguration(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public CmsFormatterConfiguration getFormatterConfiguration(CmsObject cms, CmsResource resource) {

        try {
            CmsDynamicFunctionParser parser = new CmsDynamicFunctionParser();
            CmsDynamicFunctionBean functionBean = parser.parseFunctionBean(cms, resource);
            List<CmsFormatterBean> formatters = functionBean.getFormatters();
            List<I_CmsFormatterBean> wrappers = new ArrayList<I_CmsFormatterBean>();
            for (CmsFormatterBean formatter : formatters) {
                wrappers.add(new CmsSchemaFormatterBeanWrapper(cms, formatter, this, resource));
            }
            return CmsFormatterConfiguration.create(cms, wrappers);
        } catch (CmsException e) {
            return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
        }
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#getSettings(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    @Override
    public Map<String, CmsXmlContentProperty> getSettings(CmsObject cms, CmsResource res) {

        try {
            CmsDynamicFunctionParser parser = new CmsDynamicFunctionParser();
            CmsDynamicFunctionBean functionBean = parser.parseFunctionBean(cms, res);
            return functionBean.getSettings();
        } catch (CmsException e) {
            return Collections.<String, CmsXmlContentProperty> emptyMap();
        }
    }

    /**
     * @see org.opencms.xml.content.CmsDefaultXmlContentHandler#hasModifiableFormatters()
     */
    @Override
    public boolean hasModifiableFormatters() {

        return false;
    }

}

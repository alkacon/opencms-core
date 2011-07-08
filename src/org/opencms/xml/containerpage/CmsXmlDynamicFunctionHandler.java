/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsDefaultXmlContentHandler;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.Collections;
import java.util.Locale;

/**
 * This is the XML content handler class for the "dynamic functionality" resource type.<p>
 * 
 * This resource type needs special handling of formatters and element settings: They are 
 * read from each content of this type rather than from the XSD.<p>
 */
public class CmsXmlDynamicFunctionHandler extends CmsDefaultXmlContentHandler {

    /** The path of the formatter which calls the JSP. */
    public static final String FORMATTER_PATH = "/system/modules/org.opencms.ade.containerpage/elements/function.jsp";

    /** The node name for the formatter settings. */
    public static final String N_CONTAINER_SETTINGS = "ContainerSettings";

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
            CmsFile file = cms.readFile(resource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, file);
            Locale locale = new Locale("en");
            I_CmsXmlContentValue value = content.getValue("ContainerSettings", new Locale("en"));
            CmsFormatterBean formatterBean;
            CmsResource jspResource = cms.readResource(FORMATTER_PATH);
            if (value == null) {
                formatterBean = new CmsFormatterBean(
                    "*",
                    jspResource.getRootPath(),
                    jspResource.getStructureId(),
                    -1,
                    Integer.MAX_VALUE,
                    false,
                    false,
                    resource.getRootPath());
            } else {
                String type = "";
                String minWidth = "";
                String maxWidth = "";
                I_CmsXmlContentValue typeVal = content.getValue(N_CONTAINER_SETTINGS + "/Type", locale);
                if (typeVal != null) {
                    type = typeVal.getStringValue(cms);
                }
                I_CmsXmlContentValue minWidthVal = content.getValue(N_CONTAINER_SETTINGS + "/MinWidth", locale);
                if (minWidthVal != null) {
                    minWidth = minWidthVal.getStringValue(cms);
                }
                I_CmsXmlContentValue maxWidthVal = content.getValue(N_CONTAINER_SETTINGS + "/MaxWidth", locale);
                if (maxWidthVal != null) {
                    maxWidth = maxWidthVal.getStringValue(cms);
                }
                formatterBean = new CmsFormatterBean(
                    type,
                    FORMATTER_PATH,
                    minWidth,
                    maxWidth,
                    "false",
                    "false",
                    resource.getRootPath());
                formatterBean.setJspStructureId(jspResource.getStructureId());
            }
            return CmsFormatterConfiguration.create(cms, Collections.singletonList(formatterBean));
        } catch (CmsException e) {
            return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
        }
    }

}

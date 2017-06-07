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

package org.opencms.setup.xml;

import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;

import org.dom4j.Document;

/**
 * Skeleton for handling opencms-vfs.xml.<p>
 *
 * @since 8.0.0
 */
public abstract class A_CmsXmlVfs extends A_CmsSetupXmlUpdate {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getXmlFilename()
     */
    public String getXmlFilename() {

        return CmsVfsConfiguration.DEFAULT_XML_FILE_NAME;
    }

    /**
     * Creates a resource type node.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the resource type (ie. <code>/opencms/vfs/resources/resourcetypes/type[@name='...']</code>)
     * @param name the name attribute value
     * @param clazz the class attribute value
     * @param id the id attribute value
     */
    protected void createResourceType(Document document, String xpath, String name, Class<?> clazz, int id) {

        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_CLASS, clazz.getName());
        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_NAME, name);
        CmsSetupXmlHelper.setValue(document, xpath + "/@" + I_CmsXmlConfiguration.A_ID, String.valueOf(id));
    }

    /**
     * Creates a resource type parameter.<p>
     *
     * @param document the document to modify
     * @param xpath the xpath to the resource type (ie. <code>/opencms/vfs/resources/resourcetypes/type[@name='...']</code>)
     * @param name the parameter name
     * @param value the parameter value
     */
    protected void createRtParameter(Document document, String xpath, String name, String value) {

        String xp = xpath
            + "/"
            + I_CmsXmlConfiguration.N_PARAM
            + "[@"
            + I_CmsXmlConfiguration.A_NAME
            + "='"
            + name
            + "']";
        CmsSetupXmlHelper.setValue(document, xp + "/@" + I_CmsXmlConfiguration.A_NAME, name);
        CmsSetupXmlHelper.setValue(document, xp, value);
    }
}
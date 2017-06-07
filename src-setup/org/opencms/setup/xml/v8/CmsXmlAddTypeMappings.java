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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;

/**
 * Adds some binary type mappings.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlAddTypeMappings extends A_CmsXmlVfs {

    /**
     * The new widget definition data.<p>
     */
    private String[] m_suffixes = {".flv", ".swf", ".docx", ".xlsx", ".pptx"};

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new resource type mappings";
    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#validate(org.opencms.setup.CmsSetupBean)
     */
    @Override
    public boolean validate(CmsSetupBean setupBean) throws Exception {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(getCodeToChange(setupBean));
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        boolean result = false;
        for (int i = 0; i < m_suffixes.length; i++) {
            if (xpath.equals(getXPathsToUpdate().get(i))) {
                if (document.selectSingleNode(xpath) == null) {
                    CmsSetupXmlHelper.setValue(document, xpath, "");
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        return xpathForType("binary");
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            m_xpaths = new ArrayList<String>();
            for (int i = 0; i < m_suffixes.length; i++) {
                m_xpaths.add(xpathForTypeMapping("binary", m_suffixes[i]));
            }
        }
        return m_xpaths;
    }

    /**
     * Returns the xpath for the type node with a given type name.<p>
     *
     * @param type the resource type name
     *
     * @return the xpath for the type node
     */
    protected String xpathForType(String type) {

        return xpathForTypes()
            + "/"
            + CmsVfsConfiguration.N_TYPE
            + "[@"
            + I_CmsXmlConfiguration.A_NAME
            + "='"
            + type
            + "']";
    }

    /**
     * Returns the xpath for a type mapping with a given suffix.<p>
     *
     * @param type the type name
     * @param suffix the suffix
     * @return the xpath of the type mapping
     */
    protected String xpathForTypeMapping(String type, String suffix) {

        return xpathForType(type)
            + "/"
            + CmsVfsConfiguration.N_MAPPINGS
            + "/"
            + CmsVfsConfiguration.N_MAPPING
            + "[@"
            + I_CmsXmlConfiguration.A_SUFFIX
            + "='"
            + suffix
            + "']";
    }

    /**
     * Returns the xpath for a the resourcetypes node.<p>
     *
     * @return the xpath for the resourcetypes node
     */
    protected String xpathForTypes() {

        return "/"
            + CmsConfigurationManager.N_ROOT
            + "/"
            + CmsVfsConfiguration.N_VFS
            + "/"
            + CmsVfsConfiguration.N_RESOURCES
            + "/"
            + CmsVfsConfiguration.N_RESOURCETYPES;
    }

}

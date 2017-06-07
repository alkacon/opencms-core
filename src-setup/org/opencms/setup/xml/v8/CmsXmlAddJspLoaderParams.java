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

package org.opencms.setup.xml.v8;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.configuration.I_CmsXmlConfiguration;
import org.opencms.loader.CmsJspLoader;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlVfs;
import org.opencms.setup.xml.CmsSetupXmlHelper;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

/**
 * Adds the new loader parameters.<p>
 *
 *
 * @since 8.0.0
 */
public class CmsXmlAddJspLoaderParams extends A_CmsXmlVfs {

    /** List of xpaths to update. */
    private List<String> m_xpaths;

    /** The JSP loader parameter names. */
    private String[] m_names = {"taglib.cms", "taglib.c", "taglib.fn", "taglib.fmt", "taglib.x", "taglib.sql"};

    /** The JSP loader parameter values. */
    private String[] m_uris = {
        "http://www.opencms.org/taglib/cms",
        "http://java.sun.com/jsp/jstl/core",
        "http://java.sun.com/jsp/jstl/functions",
        "http://java.sun.com/jsp/jstl/fmt",
        "http://java.sun.com/jsp/jstl/xml",
        "http://java.sun.com/jsp/jstl/sql"};

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Add new loader parameters to JSP loader";
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

        Node node = document.selectSingleNode(xpath);
        if (node == null) {
            for (int i = 0; i < m_names.length; i++) {
                if (xpath.equals(getXPathsToUpdate().get(i))) {
                    CmsSetupXmlHelper.setValue(document, xpath, m_uris[i]);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getCommonPath()
     */
    @Override
    protected String getCommonPath() {

        // /opencms/vfs/resources/resourceloaders
        return new StringBuffer("/").append(CmsConfigurationManager.N_ROOT).append("/").append(
            CmsVfsConfiguration.N_VFS).append("/").append(CmsVfsConfiguration.N_RESOURCES).append("/").append(
                CmsVfsConfiguration.N_RESOURCELOADERS).toString();
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        if (m_xpaths == null) {
            m_xpaths = new ArrayList<String>();
            for (String name : m_names) {
                m_xpaths.add(xpathForParam(name));
            }
        }
        return m_xpaths;
    }

    /**
     * Returns the xpath for a given JSP loader parameter.<p>
     *
     * @param name the name of the loader parameters
     *
     * @return the xpath for that loader parameter
     */
    protected String xpathForParam(String name) {

        String template = "/%1$s/%2$s/%3$s/%4$s/%5$s[@%6$s='%7$s']/%8$s[@%9$s='%10$s']";
        return String.format(
            template,
            CmsConfigurationManager.N_ROOT,
            CmsVfsConfiguration.N_VFS,
            CmsVfsConfiguration.N_RESOURCES,
            CmsVfsConfiguration.N_RESOURCELOADERS,
            CmsVfsConfiguration.N_LOADER,
            I_CmsXmlConfiguration.A_CLASS,
            CmsJspLoader.class.getName(),
            I_CmsXmlConfiguration.N_PARAM,
            I_CmsXmlConfiguration.A_NAME,
            name);

    }

}
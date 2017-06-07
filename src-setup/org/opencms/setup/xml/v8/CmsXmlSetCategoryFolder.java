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
import org.opencms.configuration.CmsWorkplaceConfiguration;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.xml.A_CmsXmlWorkplace;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Adds the categoryfolder option to the old default setting.
 *
 */
public class CmsXmlSetCategoryFolder extends A_CmsXmlWorkplace {

    /** Xpath . */
    public static final String CATEGORY_FOLDER_PATH = CmsConfigurationManager.N_ROOT
        + "/"
        + CmsWorkplaceConfiguration.N_WORKPLACE
        + "/"
        + CmsWorkplaceConfiguration.N_CATEGORYFOLDER;

    /** Xpath. */
    public static final String PREVIOUS_SIBLING_PATH = CmsConfigurationManager.N_ROOT
        + "/"
        + CmsWorkplaceConfiguration.N_WORKPLACE
        + "/"
        + CmsWorkplaceConfiguration.N_ENABLEADVANCEDPROPERTYTABS;

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Sets the category folder to the old value.";

    }

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#validate(org.opencms.setup.CmsSetupBean)
     */
    @Override
    public boolean validate(CmsSetupBean setupBean) throws Exception {

        return true;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        if (!document.selectNodes("/" + CATEGORY_FOLDER_PATH).isEmpty()) {
            // entry was found, no update necessary
            return false;
        }
        if (!forReal) {
            return true;
        }
        Element prevSibling = (Element)document.selectSingleNode("/" + PREVIOUS_SIBLING_PATH);
        Element parent = prevSibling.getParent();

        List<?> siblings = prevSibling.getParent().elements();
        int prevSiblingIndex = siblings.indexOf(prevSibling);

        Element categoryFolderElement = parent.addElement(CmsWorkplaceConfiguration.N_CATEGORYFOLDER);
        categoryFolderElement.setText("/_categories/");
        categoryFolderElement.detach();

        int insertIndex = prevSiblingIndex + 1;
        parent.elements().add(insertIndex, categoryFolderElement);
        return true;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList(CATEGORY_FOLDER_PATH);
    }

}

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
import org.opencms.setup.xml.A_CmsXmlWorkplace;
import org.opencms.setup.xml.CmsSetupXmlHelper;

import java.util.Collections;
import java.util.List;

import org.dom4j.Document;

/**
 * Changes the default upload applet to the GWT based upload widget.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlChangeDefaultUpload extends A_CmsXmlWorkplace {

    /**
     * @see org.opencms.setup.xml.I_CmsSetupXmlUpdate#getName()
     */
    public String getName() {

        return "Updates the default upload widget.";
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#executeUpdate(org.dom4j.Document, java.lang.String, boolean)
     */
    @Override
    protected boolean executeUpdate(Document document, String xpath, boolean forReal) {

        if (document.selectSingleNode(
            "/"
                + CmsConfigurationManager.N_ROOT
                + "/"
                + CmsWorkplaceConfiguration.N_WORKPLACE
                + "/"
                + CmsWorkplaceConfiguration.N_DEFAULTPREFERENCES
                + "/"
                + CmsWorkplaceConfiguration.N_WORKPLACEPREFERENCES
                + "/"
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS) != null) {
            CmsSetupXmlHelper.setValue(document, xpath, "gwt");
            return true;
        }
        return false;
    }

    /**
     * @see org.opencms.setup.xml.A_CmsSetupXmlUpdate#getXPathsToUpdate()
     */
    @Override
    protected List<String> getXPathsToUpdate() {

        return Collections.singletonList(
            CmsConfigurationManager.N_ROOT
                + "/"
                + CmsWorkplaceConfiguration.N_WORKPLACE
                + "/"
                + CmsWorkplaceConfiguration.N_DEFAULTPREFERENCES
                + "/"
                + CmsWorkplaceConfiguration.N_WORKPLACEPREFERENCES
                + "/"
                + CmsWorkplaceConfiguration.N_WORKPLACEGENERALOPTIONS
                + "/"
                + CmsWorkplaceConfiguration.N_UPLOADAPPLET);
    }

}

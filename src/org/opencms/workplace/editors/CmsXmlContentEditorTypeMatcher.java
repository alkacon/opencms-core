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

package org.opencms.workplace.editors;

import org.opencms.main.OpenCms;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of an additionional editor resource type matcher for xmlcontent resources.<p>
 *
 * All resourcetypes refering to xmlcontent will be found by this class.<p>
 *
 * @since 6.0.0
 */
public class CmsXmlContentEditorTypeMatcher implements I_CmsEditorTypeMatcher {

    /** The name of the xmlcontent resource type. */
    protected static final String TYPE_XMLCONTENT = "xmlcontent";

    /**
     * @see org.opencms.workplace.editors.I_CmsEditorTypeMatcher#getAdditionalResourceTypes()
     */
    public List<String> getAdditionalResourceTypes() {

        ArrayList<String> additionalTypes = new ArrayList<String>();
        // get all explorerTypes
        List<CmsExplorerTypeSettings> explorerTypes = OpenCms.getWorkplaceManager().getExplorerTypeSettings();
        Iterator<CmsExplorerTypeSettings> i = explorerTypes.iterator();
        // loop through all types and select those with reference to the type xmlcontent
        while (i.hasNext()) {
            CmsExplorerTypeSettings type = i.next();
            if ((type.getName().equalsIgnoreCase(TYPE_XMLCONTENT))
                || ((type.getReference() != null) && type.getReference().equalsIgnoreCase(TYPE_XMLCONTENT))) {
                additionalTypes.add(type.getName());
            }
        }
        return additionalTypes;
    }

}

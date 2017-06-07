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

package org.opencms.setup.xml;

import org.dom4j.Document;

/**
 * Helper class which represents a single XML update action.<p>
 *
 * This class was written to avoid writing huge switch-case blocks in the XML configuration
 * updater classes. Instead, a map from xpaths to instances of subclasses of this class can
 * be used.<p>
 *
 * @since 8.0.0
 */
public class CmsXmlUpdateAction {

    /**
     * @see A_CmsSetupXmlUpdate#executeUpdate(Document, String, boolean)
     *
     * @param doc the document to be updated
     * @param xpath the path at which the update should take place
     * @param  forReal true if the update is for real
     *
     * @return true if the document was updated
     */
    public boolean executeUpdate(Document doc, String xpath, boolean forReal) {

        return false;
    }

}

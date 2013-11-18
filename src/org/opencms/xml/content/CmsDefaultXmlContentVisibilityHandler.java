/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.content;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * The default visibility handler.<p>
 * 
 * Only users that are member in one of the specified groups will be allowed to view and edit the given content field.<p>
 * The parameter should contain a '|' separated list of group names.<p>
 */
public class CmsDefaultXmlContentVisibilityHandler implements I_CmsXmlContentVisibilityHandler {

    /** The logger for this class. */
    private static Log LOG = CmsLog.getLog(CmsDefaultXmlContentVisibilityHandler.class);

    /**
     * @see org.opencms.xml.content.I_CmsXmlContentVisibilityHandler#isValueVisible(org.opencms.file.CmsObject, org.opencms.xml.types.I_CmsXmlSchemaType, java.lang.String, java.lang.String)
     */
    public boolean isValueVisible(CmsObject cms, I_CmsXmlSchemaType value, String elementName, String params) {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        boolean result = false;

        try {
            List<CmsGroup> groups = cms.getGroupsOfUser(user.getName(), false);
            String[] allowedGroups = params.split("|");
            List<String> groupNames = new ArrayList<String>();
            for (CmsGroup group : groups) {
                groupNames.add(group.getName());
            }
            for (int i = 0; i < allowedGroups.length; i++) {
                if (groupNames.contains(allowedGroups[i])) {
                    result = true;
                    break;
                }
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }

        return result;
    }
}

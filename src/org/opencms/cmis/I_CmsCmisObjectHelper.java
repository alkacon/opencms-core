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

package org.opencms.cmis;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

/**
 * Interface containing the basic CRUD operations for CMIS objects.<p>
 */
public interface I_CmsCmisObjectHelper {

    /**
     * Deletes a CMIS object.<p>
     *
     * @param context the call context
     * @param objectId the id of the object to delete
     * @param allVersions flag to delete all version
     */
    void deleteObject(CmsCmisCallContext context, String objectId, boolean allVersions);

    /**
     * Gets the ACL for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @param onlyBasicPermissions flag to only get basic permissions
     *
     * @return the ACL for the object
     */
    Acl getAcl(CmsCmisCallContext context, String objectId, boolean onlyBasicPermissions);

    /**
     * Gets the allowable actions for an object.<p>
     *
     * @param context the call context
     * @param objectId the object id
     * @return the allowable actions
     */
    AllowableActions getAllowableActions(CmsCmisCallContext context, String objectId);

    /**
     * Gets the data for a CMIS object.<p>
     *
     * @param context the CMIS call context
     * @param objectId the id of the object
     * @param filter the property filter
     * @param includeAllowableActions flag to include allowable actions
     * @param includeRelationships flag to include relationships
     * @param renditionFilter the rendition filter string
     * @param includePolicyIds flag to include policy ids
     * @param includeAcl flag to include ACLs
     *
     * @return the CMIS object data
     */
    ObjectData getObject(
        CmsCmisCallContext context,
        String objectId,
        String filter,
        boolean includeAllowableActions,
        IncludeRelationships includeRelationships,
        String renditionFilter,
        boolean includePolicyIds,
        boolean includeAcl);
}

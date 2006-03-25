/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerTypeAccess.java,v $
 * Date   : $Date: 2006/03/25 22:42:44 $
 * Version: $Revision: 1.11.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.explorer;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Explorer type access object, encapsulates access control entires and lists of a explorer type.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.11.2.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerTypeAccess {

    /** Principal key name for the default permission settings. */
    public static final String PRINCIPAL_DEFAULT = "DEFAULT";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExplorerTypeAccess.class);

    private Map m_accessControl;
    private CmsAccessControlList m_accessControlList;

    /**
     * Constructor, creates an empty, CmsExplorerTypeAccess object.<p>
     */
    public CmsExplorerTypeAccess() {

        m_accessControl = new HashMap();
        m_accessControlList = new CmsAccessControlList();
    }

    /** 
     * Adds a single access entry to the map of access entries of the explorer type setting.<p>
     * 
     * This stores the configuration data in a map which is used in the initialize process 
     * to create the access control list.<p> 
     * 
     * @param key the principal of the ace
     * @param value the permissions for the principal
     */
    public void addAccessEntry(String key, String value) {

        m_accessControl.put(key, value);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_ADD_ACCESS_ENTRY_2, key, value));
        }
    }

    /** 
     * Creates the access control list from the temporary map.<p> 
     * 
     * @throws CmsException if reading a group or user fails
     */
    public void createAccessControlList() throws CmsException {

        if (OpenCms.getRunLevel() < OpenCms.RUNLEVEL_2_INITIALIZING) {
            // we don't need this for simple test cases
            return;
        }

        m_accessControlList = new CmsAccessControlList();
        Iterator i = m_accessControl.keySet().iterator();
        while (i.hasNext()) {
            String key = (String)i.next();
            if (!PRINCIPAL_DEFAULT.equals(key)) {
                String value = (String)m_accessControl.get(key);
                CmsUUID principalId = new CmsUUID();
                // get the principal name from the principal String
                String principal = key.substring(key.indexOf('.') + 1, key.length());

                // create an OpenCms user context with "Guest" permissions
                CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());

                if (key.startsWith(I_CmsPrincipal.PRINCIPAL_GROUP)) {
                    // read the group
                    principal = OpenCms.getImportExportManager().translateGroup(principal);
                    principalId = cms.readGroup(principal).getId();
                } else {
                    // read the user
                    principal = OpenCms.getImportExportManager().translateUser(principal);
                    principalId = cms.readUser(principal).getId();
                }
                // create a new entry for the principal
                CmsAccessControlEntry entry = new CmsAccessControlEntry(null, principalId, value);
                m_accessControlList.add(entry);
            }
        }
    }

    /**
     * Returns the map of access entries of the explorer type setting.<p>
     * 
     * @return the map of access entries of the explorer type setting
     */
    public Map getAccessEntries() {

        return m_accessControl;
    }

    /**
     * Calculates the permissions for this explorer type settings 
     * for the user in the given OpenCms user context.<p>  
     *  
     * @param cms the OpenCms user context to calculate the permissions for
     * 
     * @return the permissions for this explorer type settings for the user in the given OpenCms user context 
     */
    public CmsPermissionSet getPermissions(CmsObject cms) {

        CmsAccessControlList acl = (CmsAccessControlList)m_accessControlList.clone();

        CmsUser user = cms.getRequestContext().currentUser();
        List groups = null;
        try {
            groups = cms.getGroupsOfUser(user.getName());
        } catch (CmsException e) {
            // error reading the groups of the current user
            LOG.error(Messages.get().getBundle().key(Messages.LOG_READ_GROUPS_OF_USER_FAILED_1, user.getName()));
        }
        String defaultPermissions = (String)m_accessControl.get(PRINCIPAL_DEFAULT);
        // add the default permissions to the acl
        if ((defaultPermissions != null) && !user.isGuestUser()) {
            boolean found = false;
            if (acl.getPermissions(user) != null) {
                // acl already contains the user, no need for default
                found = true;
            }
            if (!found && (groups != null)) {
                // look up all groups to see if we need the default
                Iterator i = groups.iterator();
                while (i.hasNext()) {
                    I_CmsPrincipal principal = (I_CmsPrincipal)i.next();
                    if (acl.getPermissions(principal) != null) {
                        // acl already contains the group, no need for default
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                // add default access control settings for current user
                CmsAccessControlEntry entry = new CmsAccessControlEntry(null, user.getId(), defaultPermissions);
                acl.add(entry);
            }
        }

        // get permissions of the current user
        return acl.getPermissions(user, groups);
    }

    /**
     * Tests if there are any access information stored.<p>
     * @return true or false
     */
    public boolean isEmpty() {

        return m_accessControl.isEmpty();
    }
}
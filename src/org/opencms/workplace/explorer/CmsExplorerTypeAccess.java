/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerTypeAccess.java,v $
 * Date   : $Date: 2005/06/23 07:58:47 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsAccessControlList;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Explorer type access object, encapsulates access control entires and lists of a explorer type.<p>
 * 
 * @author Michael Emmerich 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 */
public class CmsExplorerTypeAccess {

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
            LOG.debug(Messages.get().key(Messages.LOG_ADD_ACCESS_ENTRY_2, key, value));
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
            String value = (String)m_accessControl.get(key);
            CmsUUID principalId = new CmsUUID();
            // get the principal name from the principal String
            String principal = key.substring(key.indexOf('.') + 1, key.length());

            // create an OpenCms user context with "Guest" permissions
            CmsObject cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());

            if (key.startsWith(I_CmsPrincipal.C_PRINCIPAL_GROUP)) {
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

    /**
     * Returns the list of access control entries of the explorer type setting.<p>
     * 
     * @return the list of access control entries of the explorer type setting
     */
    public CmsAccessControlList getAccessControlList() {

        return m_accessControlList;
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
     * Tests if there are any access information stored.<p>
     * @return true or false
     */
    public boolean isEmpty() {

        boolean isEmpty = false;
        if (m_accessControl.size() == 0) {
            isEmpty = true;
        }
        return isEmpty;
    }
}

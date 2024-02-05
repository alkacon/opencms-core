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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ou Handler.
 */
public class CmsOUHandler {

    /**List of all managable OUs for current user. */
    private List<String> m_managableOU;

    /**CmsObject. */
    private CmsObject m_cms;

    /**Is Root account manager (or higher). */
    private boolean m_isRootAccountManager;

    /**Maps ou names to boolean if they are parents of managable ou. */
    private Map<String, Boolean> m_isParentOfOU = new HashMap<String, Boolean>();

    /**
     * Public constructor.<p>
     *
     * @param cms CmsObject
     */
    public CmsOUHandler(CmsObject cms) {

        m_cms = cms;
        m_isRootAccountManager = OpenCms.getRoleManager().hasRole(m_cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(""));
        if (!m_isRootAccountManager) {
            m_managableOU = getManagableOUs(cms);
        }
    }

    /**
     * Gets List of managable OU names for the current user.<p>
     *
     * @param cms CmsObject
     * @return List of String
     */
    public static List<String> getManagableOUs(CmsObject cms) {

        List<String> ous = new ArrayList<String>();
        try {
            for (CmsRole role : OpenCms.getRoleManager().getRolesOfUser(
                cms,
                cms.getRequestContext().getCurrentUser().getName(),
                "",
                true,
                false,
                true)) {
                if (role.getRoleName().equals(CmsRole.ACCOUNT_MANAGER.getRoleName())) {
                    if (role.getOuFqn().equals("")) {
                        ous.add(0, role.getOuFqn());
                    } else {
                        ous.add(role.getOuFqn());
                    }
                }
            }
        } catch (CmsException e) {
            //
        }
        return ous;
    }

    /**
     * Get base ou for given manageable ous.<p>
     *
     * @return Base ou (may be outside of given ou)
     */
    public String getBaseOU() {

        if (m_isRootAccountManager) {
            return "";
        }

        if (m_managableOU.contains("")) {
            return "";
        }
        String base = m_managableOU.get(0);
        for (String ou : m_managableOU) {
            while (!ou.startsWith(base)) {
                base = base.substring(0, base.length() - 1);
                if (base.lastIndexOf("/") > -1) {
                    base = base.substring(0, base.lastIndexOf("/"));
                } else {
                    return "";
                }
            }
        }
        return base;
    }

    /**
     * Checks if given ou is manageable.<p>
     *
     * @param ou to check
     * @return true is it is manageable
     */
    public boolean isOUManagable(String ou) {

        if (m_isRootAccountManager) {
            return true;
        }

        if (OpenCms.getRoleManager().hasRole(m_cms, CmsRole.ACCOUNT_MANAGER.forOrgUnit(""))) {
            return true;
        }

        return m_managableOU.contains(assureOUString(ou));
    }

    /**
     * Checks if given ou is parent of a managable ou.<p>
     *
     * @param name to check
     * @return boolean
     */
    public boolean isParentOfManagableOU(String name) {

        if (m_isRootAccountManager) {
            return true;
        }

        if (m_isParentOfOU.containsKey(name)) {
            return m_isParentOfOU.get(name).booleanValue();
        }
        for (String ou : m_managableOU) {
            if (ou.startsWith(name)) {
                m_isParentOfOU.put(name, Boolean.valueOf(true));
                return true;
            }
        }
        m_isParentOfOU.put(name, Boolean.valueOf(false));
        return false;
    }

    /**
     *
     * Returns valid ou name.<p>
     *
     * @param ou name
     * @return valid ou name with "/" at the end
     */
    private String assureOUString(String ou) {

        if (ou.equals("") | ou.endsWith("/")) {
            return ou;
        }
        return ou + "/";
    }

}

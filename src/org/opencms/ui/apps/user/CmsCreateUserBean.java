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

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

public class CmsCreateUserBean extends CmsJspActionElement {

    public CmsCreateUserBean() {
        //
    }

    public CmsCreateUserBean(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    public void createOU(String name, int flag) {

        try {
            OpenCms.getOrgUnitManager().createOrganizationalUnit(getCmsObject(), name, "dfgdhdfhdfh", flag, "/");
        } catch (CmsException e) {
            //
        }
    }

    public void createSingleUser(String name, boolean setRole) {

        try {
            CmsUser user = getCmsObject().createUser(name, "123123", "", null);
            if (setRole) {
                OpenCms.getRoleManager().addUserToRole(
                    getCmsObject(),
                    CmsRole.ELEMENT_AUTHOR.forOrgUnit(user.getOuFqn()),
                    name);
            }
        } catch (CmsException e) {
            //
        }
    }

    public void createUser() {

        for (int i = 0; i < 1000; i++) {
            createSingleUser("test" + i, true);
        }
        createOU("BigOU", CmsOrganizationalUnit.FLAG_WEBUSERS);
        for (int i = 0; i < 5000; i++) {
            createSingleUser("BigOU/webOUUser" + i, false);
        }
        for (int i = 0; i < 100; i++) {
            createOU("TestOU" + i, 0);
            for (int j = 0; j < 100; j++) {
                createSingleUser("TestOU" + i + "/testUser" + j, true);
            }
        }
    }

    public void deleteOU(String name) {

        try {
            OpenCms.getOrgUnitManager().deleteOrganizationalUnit(getCmsObject(), name);
        } catch (CmsException e1) {
            //
        }

    }

    public void deleteSingleUser(String name) {

        try {
            getCmsObject().deleteUser(name);
        } catch (CmsException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();

        }
    }

    public void deleteUser() {

        for (int i = 0; i < 1000; i++) {
            deleteSingleUser("testUser" + i);
        }
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                deleteSingleUser("TestOU" + i + "/testUser" + j);
            }
            deleteOU("TestOU" + i);
        }
    }

}

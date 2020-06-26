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

package org.opencms.file.wrapper;

import org.opencms.db.Messages;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.security.CmsPermissionViolationException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Resource wrapper class which is used to prevent resources with names matching a given regex from being created.<p>
 */
public class CmsResourceWrapperPreventCreateNameRegex extends A_CmsResourceWrapper {

    /** The pattern for file names which should be prevented from being created. */
    private Pattern m_pattern;

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#configure(java.lang.String)
     */
    @Override
    public void configure(String configString) {

        m_pattern = Pattern.compile(configString);
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        String resourcepath,
        int type,
        byte[] content,
        List<CmsProperty> properties)
    throws CmsIllegalArgumentException {

        String name = CmsResource.getName(resourcepath);
        if (m_pattern.matcher(name).matches()) {
            throw new CmsSilentWrapperException(
                new CmsPermissionViolationException(
                    Messages.get().container(Messages.ERR_PERM_DENIED_2, resourcepath, "+c")));
        } else {
            return null;
        }
    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        return false;
    }

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#moveResource(org.opencms.file.CmsObject, java.lang.String, java.lang.String)
     */
    @Override
    public boolean moveResource(CmsObject cms, String source, String destination)
    throws CmsException, CmsIllegalArgumentException {

        String name = CmsResource.getName(destination);

        if (m_pattern.matcher(name).matches()) {
            throw new CmsPermissionViolationException(
                Messages.get().container(Messages.ERR_PERM_DENIED_2, destination, "+c"));
        } else {
            return false;
        }
    }

}

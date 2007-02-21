/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/cms/Attic/CmsRepository.java,v $
 * Date   : $Date: 2007/02/21 14:45:01 $
 * Version: $Revision: 1.1.4.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.repository.cms;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.repository.A_CmsRepository;
import org.opencms.repository.CmsRepositoryAuthorizationException;
import org.opencms.repository.I_CmsRepositorySession;

import javax.servlet.ServletConfig;

/**
 * Creates a repository session to access OpenCms.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.3 $
 * 
 * @since 6.5.6
 */
public class CmsRepository extends A_CmsRepository {

    /**
     * Empty default constructor.<p>
     */
    public CmsRepository() {

        // noop
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig servletConfig) {

        // noop
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#login(java.lang.String, java.lang.String)
     */
    public I_CmsRepositorySession login(String userName, String password)
    throws CmsRepositoryAuthorizationException {

        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            cms.loginUser(userName, password);
            
            CmsUser user = cms.readUser(userName);
            CmsUserSettings settings = new CmsUserSettings(user);
            
            cms.getRequestContext().setSiteRoot(settings.getStartSite());
            cms.getRequestContext().setCurrentProject(cms.readProject(settings.getStartProject()));
        } catch (CmsException e) {
            throw new CmsRepositoryAuthorizationException(e.getLocalizedMessage());
        }
        return new CmsRepositorySession(cms);
    }
}

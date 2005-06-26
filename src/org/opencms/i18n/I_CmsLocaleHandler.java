/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/I_CmsLocaleHandler.java,v $
 * Date   : $Date: 2005/06/26 12:23:30 $
 * Version: $Revision: 1.15 $
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

package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;

import javax.servlet.http.HttpServletRequest;

/**
 * A locale handler returns the locale name to use for the given request context.<p>
 * 
 * By implementing this interface, and configuring OpenCms
 * accordingly, the behaviour for the m_locale selection can be fine-tuned
 * to the exact need of the OpenCms installation.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.15 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsLocaleHandler {

    /**
     * Returns the i18n information to use in the request context.<p>
     * 
     * @param req the current http request
     * @param user the current user
     * @param project the current project
     * @param resource the URI of the requested resource (with full site root added)
     * 
     * @return the i18n information to use for the given request context
     */
    CmsI18nInfo getI18nInfo(HttpServletRequest req, CmsUser user, CmsProject project, String resource);

    /**
     * Will be called during system startup.<p>
     * 
     * @param cms an initialized cms permission context for VFS access
     */
    void initHandler(CmsObject cms);
}

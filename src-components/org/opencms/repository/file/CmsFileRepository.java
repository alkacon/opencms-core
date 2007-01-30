/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/repository/file/Attic/CmsFileRepository.java,v $
 * Date   : $Date: 2007/01/30 11:32:16 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.repository.file;

import org.opencms.repository.I_CmsRepository;
import org.opencms.repository.I_CmsRepositorySession;
import org.opencms.webdav.Messages;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * File repository which gives access to the file session.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.2.3 $
 * 
 * @since 6.5.6
 */
public class CmsFileRepository implements I_CmsRepository {

    /** The root path to use for the repository. */
    private String m_root;

    /** The name of the init parameter in the web.xml to specify the root path. */
    private static final String INIT_PARAM_ROOT = "root";

    /**
     * @see org.opencms.repository.I_CmsRepository#login(java.lang.String, java.lang.String)
     */
    public I_CmsRepositorySession login(String userName, String password) {

        // username and password doesnt matter in this case
        return new CmsFileRepositorySession(m_root);
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig servletConfig) throws ServletException {

        String value = servletConfig.getInitParameter(INIT_PARAM_ROOT);
        if (value != null) {
            m_root = value;
        } else {
            throw new ServletException(Messages.get().getBundle().key(
                Messages.ERR_INIT_PARAM_MISSING_1,
                INIT_PARAM_ROOT));
        }

    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/I_CmsLocaleHandler.java,v $
 * Date   : $Date: 2004/02/21 17:11:43 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.i18n;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * A locale handler returns the locale name to use for the given request context.<p>
 * 
 * By implementing this interface, and configuring <code>registry.xml</code>
 * accordingly, the behaviour for the m_locale selection can be fine-tuned
 * to the exact need of the OpenCms installation.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.6 $ 
 */
public interface I_CmsLocaleHandler {

    /**
     * Returns the locale name to use for the given request context.<p>
     * 
     * Note: the request context is not initialized completely, but it already 
     * has the requested resource URI set.<p> 
     * 
     * @param context the request context
     * @param req the current http request (can be null)
     * @return the locale name to use for the given request context
     */
    Locale getLocale(CmsRequestContext context, HttpServletRequest req);
    
    /**
     * Will be called during system startup.<p>
     * 
     * @param cms an initialized cms permission context for VFS access
     */
    void initHandler(CmsObject cms);
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsDefaultLocaleHandler.java,v $
 * Date   : $Date: 2004/02/13 13:41:45 $
 * Version: $Revision: 1.5 $
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

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;

import java.util.List;
import java.util.Locale;

/**
 * Default implementation of the locale handler.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com) 
 * @version $Revision: 1.5 $ 
 */
public class CmsDefaultLocaleHandler implements I_CmsLocaleHandler {

    /** A cms obbject that has been initialized with Admin permissions */
    private CmsObject m_adminCmsObject;
    
    /**
     * Constructor, no action is required.<p>
     */
    public CmsDefaultLocaleHandler() {
        // noop
    }
    
    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#initHandler(org.opencms.file.CmsObject)
     */
    public void initHandler(CmsObject cms) {
        m_adminCmsObject = cms;
    }
    
    /**
     * @see org.opencms.i18n.I_CmsLocaleHandler#getLocale(org.opencms.file.CmsRequestContext)
     */
    public Locale getLocale(CmsRequestContext context) {        
        // get resource name (internal "Admin" cms object is in "/" site so adjust name to full path)
        String resourceName = context.addSiteRoot(context.getUri());

        CmsLocaleManager localeManager = OpenCms.getLocaleManager();
        
        List defaultLocales = null;
        synchronized (m_adminCmsObject) {
            // must switch project id in stored Admin context to match current project
            try {
                m_adminCmsObject.getRequestContext().setCurrentProject(context.currentProject().getId());            
                // now get default m_locale names
                defaultLocales = localeManager.getDefaultLocales(m_adminCmsObject, resourceName);
            } catch (CmsException e) {
                OpenCms.getLog(this).error("Error switching admin context project", e);
            }
        }
        
        // return the first default name 
        if ((defaultLocales != null) && (defaultLocales.size() > 0)) {
            return (Locale)defaultLocales.get(0);
        } else {
            return localeManager.getDefaultLocale();
        }
    }        
}

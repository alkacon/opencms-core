/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/i18n/CmsDefaultLocaleHandler.java,v $
 * Date   : $Date: 2004/04/10 12:56:15 $
 * Version: $Revision: 1.9 $
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
import org.opencms.file.CmsProject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of the locale handler.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @author Alexander Kandzior (a.kandzior@alkacon.com) 
 * @version $Revision: 1.9 $ 
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
     * @see org.opencms.i18n.I_CmsLocaleHandler#getI18nInfo(javax.servlet.http.HttpServletRequest, org.opencms.file.CmsUser, org.opencms.file.CmsProject, java.lang.String)
     */
    public CmsI18nInfo getI18nInfo(HttpServletRequest req, CmsUser user, CmsProject project, String resourceName) {
        
        CmsLocaleManager localeManager = OpenCms.getLocaleManager();
        
        String encoding;
        List defaultLocales;
        synchronized (m_adminCmsObject) {
            // must switch project id in stored Admin context to match current project
            m_adminCmsObject.getRequestContext().setCurrentProject(project);            
            // now get default m_locale names
            defaultLocales = localeManager.getDefaultLocales(m_adminCmsObject, resourceName);
            // get the encoding
            try {
                encoding = m_adminCmsObject.readPropertyObject(resourceName, I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());
            } catch (CmsException e) {
                if (OpenCms.getLog(this).isInfoEnabled()) {
                    OpenCms.getLog(this).info("Could not read encoding property for resource " + resourceName, e);
                } 
                encoding = OpenCms.getSystemInfo().getDefaultEncoding();
            }
        }
        
        Locale locale;
        // return the first default locale name 
        if ((defaultLocales != null) && (defaultLocales.size() > 0)) {
            locale = (Locale)defaultLocales.get(0);
        } else {
            locale = localeManager.getDefaultLocale();
        }
        
        return new CmsI18nInfo(locale, encoding);        
    }     
}

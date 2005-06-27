/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/legacy/Attic/CmsXmlPageTemplate.java,v $
 * Date   : $Date: 2005/06/27 23:22:15 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  Alkacon Software (http://www.alkacon.com)
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
 
package com.opencms.legacy;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.loader.CmsXmlPageLoader;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.page.CmsXmlPageFactory;

import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsDumpTemplate;

import java.util.Hashtable;
import java.util.Locale;

/**
 * A simple dump class which enables
 * the use of XmlPage as sub-elements in the legacy OpenCms XMLTemplate 
 * mechanism.<p>
 *
 * @author  Carsten Weinholz
 * 
 * @version $Revision: 1.8 $
 * @since 5.1
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsXmlPageTemplate extends CmsDumpTemplate {
    
    /**
     * The constructor of the class is empty and does nothing.<p>
     */
    public CmsXmlPageTemplate() {
        // NOOP
    }

    /**
     * Gets the content of the given XmlPage file to include them
     * in the XMLTemplate.<p>
     *
     * @param cms for accessing system resources
     * @param filename name of the xml page file in the VFS
     * @param elementName <em>not used</em>
     * @param parameters <em>not used</em>
     * 
     * @return Content of the requested JSP page.
     * 
     * @throws CmsException in case something goes wrong
     */
    public byte[] getContent(CmsObject cms, String filename, String elementName, Hashtable parameters) throws CmsException {
        if (CmsLog.getLog(this).isDebugEnabled()) {
            CmsLog.getLog(this).debug("Loading contents of file " + filename);
        }

        byte[] s = null;
        try {
            CmsFile file = cms.readFile(filename);
            CmsXmlPageLoader loader = (CmsXmlPageLoader)OpenCms.getResourceManager().getLoader(CmsXmlPageLoader.RESOURCE_LOADER_ID);
            // check the current locales
            CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
            String absolutePath = cms.getSitePath(file);
            Locale locale = OpenCms.getLocaleManager().getBestMatchingLocale(cms.getRequestContext().getLocale(), OpenCms.getLocaleManager().getDefaultLocales(cms, absolutePath), page.getLocales());            
            s = loader.dump(cms, file, elementName, locale, null, null);
        } catch (java.lang.ClassCastException e) {
            s = null;
            throw new CmsLegacyException("[CmsXmlPageTemplate] " + filename + " is not a xml page");
        } catch (org.opencms.main.CmsException e) {
            s = null;
            // File might not exist or no read permissions
            throw new CmsLegacyException("[CmsXmlPageTemplate] Error while reading xml page " + filename + "\n" + e, e);
        } catch (Exception e) {
            s = null;
            String errorMessage = "[CmsXmlPageTemplate] Error while loading xml page file " + filename + ": " + e;
            if (CmsLog.getLog(this).isErrorEnabled()) {
                CmsLog.getLog(this).error(errorMessage, e);
            }
            if (e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                throw new CmsLegacyException(errorMessage, CmsLegacyException.C_UNKNOWN_EXCEPTION);
            }
        }
        return s;
    }
    
    /**
     * Cache method required by the ElementCache to indicate if the
     * results of the page should be cached in the ElementCache.<p>
     * 
     * @return <code>false</code>
     */
    public boolean isTemplateCacheSet() {
        return false;
    }    
    
    /**
     * Method used by the ElementCache to check if the page 
     * should reload or not.<p>
     * 
     * @param cms default argument for element cache method
     * @param templateFile default argument for element cache method
     * @param elementName default argument for element cache method
     * @param parameters default argument for element cache method
     * @param templateSelector default argument for element cache method
     * @return <code>true</code>
     */
    public boolean shouldReload(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    } 
    
    /**
     * Returns the caching information from the current template class for the
     * ElementCache.<p>
     * 
     * JSPs will be cached in the FlexCache and so 
     * we always return directives that prevent caching here,
     * i.e. <code>new CmsCacheDirectives(false)</code>
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile filename of the template file
     * @param elementName element name of this template in our parent template
     * @param parameters Hashtable with all template class parameters
     * @param templateSelector template section that should be processed
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        // Just set caching to false
        return new CmsCacheDirectives(false);
    }    
}

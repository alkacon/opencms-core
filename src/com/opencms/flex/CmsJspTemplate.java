/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/flex/Attic/CmsJspTemplate.java,v $
 * Date   : $Date: 2002/08/21 11:29:32 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002  The OpenCms Group
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * First created on 24. Mai 2002, 09:53
 */


package com.opencms.flex;

import java.util.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.template.cache.*;

/**
 * A simple dump class for JSPs.
 * This class enables using JSP as elements in the XMLTemplate 
 * mechanism.
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.2 $
 */
public class CmsJspTemplate extends com.opencms.template.CmsDumpTemplate {
    
    /** 
     * Constructor for class CmsJspTemplate.
     * Not used.
     */
    public CmsJspTemplate() {       
    }

    /**
     * Gets the content of a given template file.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters) throws CmsException {
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_FLEX_LOADER, "[CmsJspTemplate] Now loading contents of file " + templateFile);
        }
        byte[] s = null;
        try {
            CmsFile file = cms.readFile(templateFile);
            int type = file.getLauncherType();
            com.opencms.flex.CmsJspLoader loader = (com.opencms.flex.CmsJspLoader)cms.getLauncherManager().getLauncher(type);
            s = loader.loadTemplate(cms, file);
        } catch (java.lang.ClassCastException e) {
            throw new CmsException("JspTemplate: " + templateFile + " is not a JSP");
        } catch (com.opencms.core.CmsException e) {
            // File might not exist or no read permissions
            throw new CmsException("JspTemplate: Error while reading JSP " + templateFile + "\n" + e, e);
        }        

        catch(Exception e) {
            String errorMessage = "Error while loading jsp file " + templateFile + ": " + e;
            if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsJspTemplate] " + errorMessage);
            }
            if(e instanceof CmsException) {
                throw (CmsException)e;
            }
            else {
                throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
        return s;
    }
    
    /**
     * Don't cache JSPs in the ElementCache. 
     * So we can always return <code>false</code> here.
     * @return <code>false</code>
     */
    public boolean isTemplateCacheSet() {
        // return true;
        return false;
    }    
    
    /**
     * JSPs are usually very dynamic.
     * So we always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean shouldReload(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        // return false;
        return true;
    } 
    
    /**
     * Returns the caching information from the current template class.
     * JSPs are never cached in the ElementCache.
     * Use the FlexCache to cache results of a JSP.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        // Just set caching to false.
        // Set cacheing for export requests
        CmsCacheDirectives result = new CmsCacheDirectives(false);
        // CmsCacheDirectives result = new CmsCacheDirectives(true);
        // result.setCacheUri(true);
        return result;
    }    
}

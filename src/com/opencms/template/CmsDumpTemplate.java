
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/Attic/CmsDumpTemplate.java,v $
* Date   : $Date: 2001/05/07 14:46:58 $
* Version: $Revision: 1.20 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.template;

import java.util.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.cache.*;

/**
 * Template class for dumping files to the output without further
 * interpreting or processing.
 * This can be used for plain text files or files containing graphics.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.20 $ $Date: 2001/05/07 14:46:58 $
 */
public class CmsDumpTemplate extends A_CmsTemplate implements I_CmsDumpTemplate {

    /**
     * Template cache is not used here since we don't include
     * any subtemplates.
     */
    private static I_CmsTemplateCache m_cache = null;

    /** Boolean for additional debug output control */
    private static final boolean C_DEBUG = false;
    public CmsDumpTemplate() {

    }

    /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {

        // First build our own cache directives.
        CmsCacheDirectives result = new CmsCacheDirectives(true);
        result.setCacheUri(true);
        return result;

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
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsDumpTemplate] Now dumping contents of file " + templateFile);
        }
        byte[] s = null;
        try {
            s = cms.readFile(templateFile).getContents();
        }
        catch(Exception e) {
            String errorMessage = "Error while reading file " + templateFile + ": " + e;
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsDumpTemplate] " + errorMessage);
                e.printStackTrace();
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
     * Gets the content of a given template file.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @param templateSelector <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {

        // ignore the templateSelector since we only dump the template
        return getContent(cms, templateFile, elementName, parameters);
    }

    /**
     * Gets the key that should be used to cache the results of
     * this template class.
     * <P>
     * Since this class is quite simple it's okay to return
     * just the name of the template file here.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return key that can be used for caching
     */
    public Object getKey(CmsObject cms, String templateFile, Hashtable parameter, String templateSelector) {

        //return templateFile.getAbsolutePath();

        //Vector v = new Vector();
        CmsRequestContext reqContext = cms.getRequestContext();

        //v.addElement(reqContext.currentProject().getName());

        //v.addElement(templateFile);

        //return v;
        return "" + reqContext.currentProject().getId() + ":" + templateFile;
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    /*public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }*/

    /**
     * Any results of this class are cacheable since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean isTemplateCacheSet() {
        return true;
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates <em>(not implemented)</em>.
     */
    public void setTemplateCache(I_CmsTemplateCache c) {


    // do nothing.
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates. So we can always return <code>false</code> here.
     * @return <code>false</code>
     */
    public boolean shouldReload(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }

    public A_CmsElement createElement(CmsObject cms, String templateFile, Hashtable parameters) {
        return new CmsElementDump(getClass().getName(), templateFile, collectCacheDirectives(cms, templateFile, null, parameters, null));
    }
}

/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementXml.java,v $
* Date   : $Date: 2001/05/07 16:22:56 $
* Version: $Revision: 1.3 $
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
package com.opencms.template.cache;

import java.util.*;
import java.io.*;
import com.opencms.boot.*;
import com.opencms.core.*;
import com.opencms.file.*;
import com.opencms.template.*;

/**
 * An instance of CmsElement represents an requestable Element in the OpenCms
 * staging-area. It contains all informations to generate the content of this
 * element. It also stores the variants of once generated content to speed up
 * performance.
 *
 * It points to other depending elements. Theses elements are called to generate
 * their content on generation-time.
 *
 * @author Andreas Schouten
 * @author Alexander Lucas
 */
public class CmsElementXml extends A_CmsElement implements com.opencms.boot.I_CmsLogChannels {

    /**
     * Constructor for an element with the given class and template name.
     */
    public CmsElementXml(String className, String templateName, CmsCacheDirectives cd) {
        init(className, templateName, cd);
    }

    /**
     * A construcor which creates an element with the given element
     * definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param cd Cache directives for this element
     * @param defs Vector with ElementDefinitions for this element.
     */
    public CmsElementXml(String className, String templateName, CmsCacheDirectives cd, Vector defs) {
        init(className, templateName, cd, defs);
    }

    public byte[] getContent(CmsStaging staging, CmsObject cms, Hashtable parameters) throws CmsException  {
        byte[] result = null;

        // Get template class.
        // In classic mode, this is donw by the launcher.
        I_CmsTemplate templateClass = null;
        try {
            templateClass = getTemplateClass(cms, m_className);
        } catch(Throwable e) {
            if(CmsBase.isLogging()) {
                CmsBase.log(C_OPENCMS_CRITICAL, toString() + " Could not load my template class \"" + m_className + "\". ");
                CmsBase.log(C_OPENCMS_CRITICAL, e.toString());
                return e.getMessage().getBytes();
            }
        }

        // Get out own cache directives
        CmsCacheDirectives cd = getCacheDirectives();

        // We really don't want to stream here
        /*boolean streamable = cms.getRequestContext().isStreaming() && cd.isStreamable();
        cms.getRequestContext().setStreaming(streamable);*/
        boolean streamable = false;

        CmsElementVariant variant = null;

        // In classic mode, now the cache-control headers of the response
        // are setted. What shall we do here???

        // Now check, if there is a variant of this element in the cache.
        //if(cacheable && !templateClass.shouldReload(cms, m_templateName, m_elementName, parameters, null)) {
        if(cd.isInternalCacheable()) {
            //variant = getVariant(templateClass.getKey(cms, m_templateName, parameters, null));
            variant = getVariant(cd.getCacheKey(cms, parameters));
            if(variant != null) {
                result = resolveVariant(cms, variant, staging, parameters);
            }
        }
        if(variant == null) {
            // This element was not found in the variant cache.
            // We have to generate it.
            try {
                if(cd.isInternalCacheable()) {
                    System.err.println(toString() + " ### Variant not in cache. Must be generated.");
                } else {
                    System.err.println(toString() + " ### Element not cacheable. Generating variant temporarily.");
                }
                result = templateClass.getContent(cms, m_templateName, m_elementName, parameters);
                if(result == null) {
                    System.err.println(toString() + " ########## WARNING! RESULT IST NULL!");
                }
            }
            catch(CmsException e) {
                // Clear cache and do logging here
                throw e;
            }
            if(streamable) {
                result = null;
            }
        }
        return result;
    }


}
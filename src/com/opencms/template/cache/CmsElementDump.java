/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDump.java,v $
* Date   : $Date: 2003/01/20 17:57:48 $
* Version: $Revision: 1.14 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
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
*/

package com.opencms.template.cache;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.template.A_CmsCacheDirectives;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.I_CmsTemplate;

import java.util.Hashtable;

/**
 * An instance of CmsElementDump represents an requestable dump element in the OpenCms
 * element cache area. It contains all informations to generate the content of this
 * element. It also stores the variants of once generated content to speed up
 * performance.
 *
 * This special case of an element doesn't point to other depending elements.
 * It may only be used for dumping plain text or binary data.
 *
 * @author Alexander Lucas
 */
public class CmsElementDump extends A_CmsElement {

    /**
     * Constructor for an element with the given class and template name.
     */
    public CmsElementDump(String className, String templateName, String readAccessGroup, CmsCacheDirectives cd, int variantCachesize) {
        init(className, templateName, readAccessGroup, cd, variantCachesize);
    }

    /**
     * A construcor which creates an element with the given element
     * definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param cd Cache directives for this element
     * @param defs CmsElementDefinitionCollection for this element.
     * @param variantCachesize The size of the variant cache.
     */
    public CmsElementDump(String className, String templateName, String readAccessGroup, CmsCacheDirectives cd, CmsElementDefinitionCollection defs, int variantCachesize) {
        init(className, templateName, readAccessGroup, cd, defs, variantCachesize);
    }

    /**
     * Get the content of this element.
     * @param elementCache Entry point for the element cache
     * @param cms CmsObject for accessing system resources
     * @param elDefs Definitions of this element's subelements
     * @param parameters All parameters of this request
     * @param methodParameter not used here.
     * @return Byte array with the processed content of this element.
     * @exception CmsException
     */
    public byte[] getContent(CmsElementCache elementCache, CmsObject cms, CmsElementDefinitionCollection elDefs, String elementName, Hashtable parameters, String methodParameter) throws CmsException  {
        byte[] result = null;

        // Get template class.
        // In classic mode, this is donw by the launcher.
        I_CmsTemplate templateClass = getTemplateClass(cms, m_className);

        // Collect cache directives from subtemplates
        A_CmsCacheDirectives cd = getCacheDirectives();

        // We really don't want to stream here
        /*boolean streamable = cms.getRequestContext().isStreaming() && cd.isStreamable();
        cms.getRequestContext().setStreaming(streamable);*/
        //boolean streamable = false;
        boolean streamable = cms.getRequestContext().isStreaming();

        CmsElementVariant variant = null;

//        Object cacheKey = templateClass.getKey(cms, m_templateName, parameters, null);
        Object cacheKey = cd.getCacheKey(cms, parameters);

        // In classic mode, now the cache-control headers of the response
        // are setted. What shall we do here???
        // Now check, if there is a variant of this element in the cache.

        if(cd.isInternalCacheable()) {
            checkReadAccess(cms);
            variant = getVariant(cacheKey);
        }

        if(variant != null) {
            result = resolveVariant(cms, variant, elementCache, elDefs, elementName, parameters);
        } else {
            // This element was not found in the variant cache.
            // We have to generate it.
            try {
                result = templateClass.getContent(cms, m_templateName, elementName, parameters);
                if(cd.isInternalCacheable()) {
                    variant = new CmsElementVariant();
                    //Gridnine AB Aug 5, 2002
                    variant.add(result);
                    addVariant(cacheKey, variant);
                }

            }
            catch(CmsException e) {
                // Clear cache and do logging here
                throw e;
            }
        }
        if(streamable) {
            try {
                cms.getRequestContext().getResponse().getOutputStream().write(result);
            }catch(Exception e) {
                if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, this.toString() + " Error while streaming!");
                }
            }
            result = null;
        }
        return result;
    }
}
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDump.java,v $
* Date   : $Date: 2001/05/08 13:04:00 $
* Version: $Revision: 1.4 $
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
public class CmsElementDump extends A_CmsElement {

    /**
     * Constructor for an element with the given class and template name.
     */
    public CmsElementDump(String className, String templateName, CmsCacheDirectives cd) {
        init(className, templateName, cd);
    }

    /**
     * A construcor which creates an element with the given element
     * definitions.
     * @param name the name of this element-definition.
     * @param className the classname of this element-definition.
     * @param cd Cache directives for this element     *
     * @param defs CmsElementDefinitionCollection for this element.
     */
    public CmsElementDump(String className, String templateName, CmsCacheDirectives cd, CmsElementDefinitionCollection defs) {
        init(className, templateName, cd, defs);
    }


    public byte[] getContent(CmsStaging staging, CmsObject cms, CmsElementDefinitionCollection elDefs, Hashtable parameters) throws CmsException  {
        byte[] result = null;

        // Get template class.
        // In classic mode, this is donw by the launcher.
        I_CmsTemplate templateClass = getTemplateClass(cms, m_className);

        // Collect cache directives from subtemplates
        CmsCacheDirectives cd = getCacheDirectives();

        // We really don't want to stream here
        /*boolean streamable = cms.getRequestContext().isStreaming() && cd.isStreamable();
        cms.getRequestContext().setStreaming(streamable);*/
        boolean streamable = false;

        CmsElementVariant variant = null;

        Object cacheKey = templateClass.getKey(cms, m_templateName, parameters, null);

        // In classic mode, now the cache-control headers of the response
        // are setted. What shall we do here???
        // Now check, if there is a variant of this element in the cache.

        if(cd.isInternalCacheable()) {
            variant = getVariant(cacheKey);
        }

        if(variant != null) {
            result = resolveVariant(cms, variant, staging, elDefs, parameters);
        } else {
            // This element was not found in the variant cache.
            // We have to generate it.
            try {
                System.err.println(toString() + " : Variante muss generiert werden.");
                result = templateClass.getContent(cms, m_templateName, m_elementName, parameters);
                if(cd.isInternalCacheable()) {
                    variant = new CmsElementVariant();
                    variant.add(result);
                    addVariant(cacheKey, variant);
                }
                System.err.println(toString() + " : New variant is: " + variant);

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
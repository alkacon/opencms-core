/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/template/cache/Attic/CmsElementDump.java,v $
* Date   : $Date: 2001/05/07 08:57:24 $
* Version: $Revision: 1.2 $
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
     * @param defs Vector with ElementDefinitions for this element.
     */
    public CmsElementDump(String className, String templateName, CmsCacheDirectives cd, Vector defs) {
        init(className, templateName, cd, defs);
    }


    public byte[] getContent(CmsStaging staging, CmsObject cms, Hashtable parameters) throws CmsException  {
        byte[] result = null;

        // Get template class.
        // In classic mode, this is donw by the launcher.
        // TODO: Do we really have to load this here?
        I_CmsTemplate templateClass = this.getTemplateClass(cms, m_className);

        // Collect cache directives from subtemplates
        // TODO: Replace root template name here
        CmsCacheDirectives cd = templateClass.collectCacheDirectives(cms, m_templateName, m_elementName, parameters, null);

        // We really don't want to stream here
        /*boolean streamable = cms.getRequestContext().isStreaming() && cd.isStreamable();
        cms.getRequestContext().setStreaming(streamable);*/
        boolean streamable = false;

        CmsElementVariant variant = null;

        Object cacheKey = templateClass.getKey(cms, m_templateName, parameters, null);
        boolean cacheable = cd.isInternalCacheable();

        // In classic mode, now the cache-control headers of the response
        // are setted. What shall we do here???

        // Now check, if there is a variant of this element in the cache.

        if(cacheable && !templateClass.shouldReload(cms, m_templateName, m_elementName, parameters, null)) {
            variant = getVariant(cacheKey);
        }

        if(variant == null) {
            // This element was not found in the variant cache.
            // We have to generate it.
            try {
                System.err.println(toString() + " : Variante muss generiert werden.");
                result = templateClass.getContent(cms, m_templateName, m_elementName, parameters);
                variant = new CmsElementVariant();
                variant.add(result);
                addVariant(cacheKey, variant);
                System.err.println(toString() + " : New variant is: " + variant);

            }
            catch(CmsException e) {
                // Clear cache and do logging here
                throw e;
            }
            if(cacheable) {
                //cache.put(cacheKey, result);
                // We do not care about storing this variant in the cache here.
                // This has been done bye startProcessing() of the template engine.
            }
            if(streamable) {
                result = null;
            }
        }

        // Now the variant really should exist. Try to resolve it.
        try {
            int len = variant.size();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for(int i=0; i<len; i++) {
                Object o = variant.get(i);
                if(o instanceof String) {
                    baos.write(((String)o).getBytes());
                } else if(o instanceof byte[]) {
                    baos.write((byte[])o);
                } else if(o instanceof CmsElementLink) {
                    // we have to resolve the element link right NOW!
                    String lookupName = ((CmsElementLink)o).getElementName();
                    System.err.println("= Trying to resolve link \"" + lookupName +"\".");
                    CmsElementDefinition elDef = getElementDefinition(lookupName);
                    if(elDef != null) {
                        A_CmsElement subEl = staging.getElementLocator().get(cms, elDef.getDescriptor(), parameters);
                        System.err.println("= Element defintion for \"" + lookupName +"\" says: ");
                        System.err.println("= -> Class    : " + elDef.getClassName());
                        System.err.println("= -> Template : " + elDef.getTemplateName());
                        if(subEl != null) {
                            System.err.println("= Element object found for \"" + lookupName +"\". Calling getContent on this object. ");
                            baos.write(subEl.getContent(staging, cms, parameters));
                        } else {
                            System.err.println("= Cannot find Element object for \"" + lookupName +"\". Ignoring this link. ");
                        }
                    } else {
                        System.err.println("= No element definition found for \"" + lookupName +"\". Ignoring this link. ");
                    }
                }
            }
            result = baos.toByteArray();
        } catch(Exception e) {
            System.err.println("Error while resolving element variant");
            e.printStackTrace();
        }


        return result;
    }
}
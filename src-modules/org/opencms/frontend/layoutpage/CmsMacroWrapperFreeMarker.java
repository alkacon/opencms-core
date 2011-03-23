/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/layoutpage/CmsMacroWrapperFreeMarker.java,v $
 * Date   : $Date: 2011/03/23 14:50:01 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.frontend.layoutpage;

import freemarker.core.Macro;
import freemarker.template.Template;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Wrapper for the FreeMarker template engine containing macros that are used to generate HTML output.<p>
 * 
 * Use this class with caution! It might be moved to the OpenCms core packages in the future.<p>
 * 
 * @author Andreas Zahner
 * 
 * @since 6.2.0
 */
public class CmsMacroWrapperFreeMarker implements I_CmsMacroWrapper {

    /** File suffix for template files. */
    public static final String FILE_SUFFIX = "ftl";

    /** Variable name for the macro to execute. */
    protected static final String MACRO_NAME = "ocmsmacro";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMacroWrapperFreeMarker.class);

    /** The FreeMarker root map. */
    private Map m_rootMap;

    /** The FreeMarker template instance. */
    private Template m_template;

    /**
     * Constructor, with parameters.<p>
     * 
     * @param cms the OpenCms user context to use
     * @param macroFile the OpenCms VFS path of the macro template file to use
     * 
     * @throws Exception if the initialization of the macro template engine fails
     */
    public CmsMacroWrapperFreeMarker(CmsObject cms, String macroFile)
    throws Exception {

        // initialize member variables
        init(cms, macroFile);
    }

    /**
     * @see org.opencms.frontend.layoutpage.I_CmsMacroWrapper#getFileSuffix()
     */
    public String getFileSuffix() {

        return FILE_SUFFIX;
    }

    /**
     * @see org.opencms.frontend.layoutpage.I_CmsMacroWrapper#getResult(java.lang.String)
     */
    public String getResult(String macroName) {

        return getResult(macroName, null);
    }

    /**
     * @see org.opencms.frontend.layoutpage.I_CmsMacroWrapper#getResult(java.lang.String, java.lang.String[])
     */
    public String getResult(String macroName, String[] args) {

        Writer out = new StringWriter();
        boolean error = false;
        try {
            // get the macro object to process
            Macro macro = (Macro)m_template.getMacros().get(macroName);
            if (macro != null) {
                // found macro, put it context
                putContextVariable(MACRO_NAME, macro);
                // process the template
                m_template.process(getContext(), out);
            } else {
                // did not find macro
                error = true;
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            error = true;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
                // ignore exception when closing writer
            }
        }
        if (error) {
            return "";
        }
        return out.toString();
    }

    /**
     * @see org.opencms.frontend.layoutpage.I_CmsMacroWrapper#init(org.opencms.file.CmsObject, java.lang.String)
     */
    public void init(CmsObject cms, String macroFile) throws Exception {

        // create the root map context that can be used
        m_rootMap = new HashMap(16);

        // get the template cache instance
        CmsFreeMarkerTemplateCache cache = CmsFreeMarkerTemplateCache.getInstance();
        // get the template from the cache
        m_template = cache.getTemplate(cms, macroFile);
    }

    /**
     * @see org.opencms.frontend.layoutpage.I_CmsMacroWrapper#putContextVariable(java.lang.String, java.lang.Object)
     */
    public Object putContextVariable(String key, Object value) {

        return getContext().put(key, value);
    }

    /**
     * @see org.opencms.frontend.layoutpage.I_CmsMacroWrapper#removeContextVariable(java.lang.String)
     */
    public Object removeContextVariable(String key) {

        return getContext().remove(key);
    }

    /**
     * Returns an initialized context root map.<p>
     * 
     * @return an initialized context root map
     */
    private Map getContext() {

        return m_rootMap;
    }

}

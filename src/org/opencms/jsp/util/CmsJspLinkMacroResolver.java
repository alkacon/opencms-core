/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/util/CmsJspLinkMacroResolver.java,v $
 * Date   : $Date: 2006/10/12 10:07:35 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.jsp.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.staticexport.CmsLinkManager;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsUUID;
import org.opencms.util.I_CmsMacroResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves link macros for jsp pages.<p>
 * 
 * The only supported macro is the 'link' macro.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.4 
 */
public class CmsJspLinkMacroResolver implements I_CmsMacroResolver {

    /** Identifier for the link macro name. */
    public static final String KEY_LINK = "link:";

    /** Identifier for the link macro separator. */
    public static final char KEY_SEPARATOR = ':';

    /** The cms context. */
    private CmsObject m_cms;

    /** 
     * If <code>true</code> the macros get really resolved to valid vfs paths, 
     * otherwise only the path/id in the macros and the file relations are updated. 
     */
    private boolean m_forRfs;

    /** The list of links. */
    private List m_links = new ArrayList();

    /** If <code>true</code> the links are displayed site aware, if not root paths are used. */
    private boolean m_forEditor;

    /** The jsp root path. */
    private String m_jspRootPath;

    /**
     * Default constructor.<p>
     * 
     * @param cms the cms context
     * @param jspRootPath the (optional) jsp root path, needed for saving from the editor to resolve relative links 
     * @param forRfs Only if <code>true</code> the macros get really resolved to valid vfs paths
     * @param forEditor If <code>true</code> the links are displayed site aware
     */
    public CmsJspLinkMacroResolver(CmsObject cms, String jspRootPath, boolean forRfs, boolean forEditor) {

        m_cms = cms;
        m_forRfs = forRfs;
        m_forEditor = forEditor;
        m_jspRootPath = jspRootPath;
    }

    /**
     * Returns the links.<p>
     *
     * @return the links
     */
    public List getLinks() {

        return m_links;
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#getMacroValue(java.lang.String)
     */
    public String getMacroValue(String macro) {

        String path = null;
        String id = null;
        
        // validate macro command
        String cmd = KEY_LINK;
        if (macro.startsWith(cmd)) {
            path = macro.substring(cmd.length());
            int pos = path.indexOf(KEY_SEPARATOR);
            if (pos > 0 && path.length() > pos + 1) {
                id = path.substring(pos + 1);
            }
            if (pos > 0) {
                path = path.substring(0, pos);
            }
            if ((path.charAt(0) == '/') && !path.startsWith(m_cms.getRequestContext().getSiteRoot())) {
                path = m_cms.getRequestContext().addSiteRoot(path);
            } else if (m_jspRootPath != null) {
                path = CmsLinkManager.getAbsoluteUri(path, CmsResource.getParentFolder(m_jspRootPath));
            }
        } else {
            // this is an unknown macro, ignore it
            return null;
        }
        
        // we do have a valid link macro now
        CmsUUID uuid = null;
        if (id != null) {
            try {
                uuid = new CmsUUID(id);
            } catch (Exception e) {
                // ignore
            }
        }
        CmsLink link = new CmsLink("link0", CmsRelationType.JSP, uuid, path, true);
        link.checkConsistency(m_cms); // update id/path
        m_links.add(link);
        if (m_forEditor) {
            return "" + I_CmsMacroResolver.MACRO_DELIMITER_NEW
                + I_CmsMacroResolver.MACRO_START_NEW
                + KEY_LINK
                + m_cms.getRequestContext().removeSiteRoot(link.getTarget())
                + I_CmsMacroResolver.MACRO_END_NEW;
        } else if (m_forRfs) {
            return m_cms.getRequestContext().removeSiteRoot(link.getTarget());
        } else {
            StringBuffer newMacro = new StringBuffer(128);
            newMacro.append(I_CmsMacroResolver.MACRO_DELIMITER_NEW);
            newMacro.append(I_CmsMacroResolver.MACRO_START_NEW);
            newMacro.append(KEY_LINK);
            newMacro.append(link.getVfsUri());
            if (link.getStructureId() != null && !link.getStructureId().isNullUUID()) {
                newMacro.append(KEY_SEPARATOR).append(link.getStructureId());
            }
            newMacro.append(I_CmsMacroResolver.MACRO_END_NEW);
            return newMacro.toString();
        }
    }

    /**
     * @see org.opencms.util.I_CmsMacroResolver#isKeepEmptyMacros()
     */
    public boolean isKeepEmptyMacros() {

        return true;
    }

    /**
     * Resolves the JSP link management macros in the given input.<p>
     * 
     * Calls <code>{@link #resolveMacros(String)}</code> once for each macro in the input.
     * This means "nested" macros are not supported in this implementation, which is fine since
     * it can't happen in JSP link management anyway.<p> 
     * 
     * @see org.opencms.util.I_CmsMacroResolver#resolveMacros(java.lang.String)
     */
    public String resolveMacros(String input) {

        // clear the list of links
        m_links.clear();

        // parse the input string
        String result;
        if (input != null) {
            // resolve the macros
            result = CmsMacroResolver.resolveMacros(input, this);
        } else {
            // nothing to resolve
            result = null;
        }
        // return the result
        return result;
    }
}
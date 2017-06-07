/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ade.contenteditor;

import org.opencms.ade.contenteditor.shared.CmsContentDefinition;
import org.opencms.ade.contenteditor.shared.CmsExternalWidgetConfiguration;
import org.opencms.ade.contenteditor.shared.rpc.I_CmsContentService;
import org.opencms.gwt.CmsGwtActionElement;
import org.opencms.gwt.shared.CmsCoreData;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.CmsEditor;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The content editor action element.<p>
 */
public class CmsContentEditorActionElement extends CmsGwtActionElement {

    /** The OpenCms module name. */
    public static final String CMS_MODULE_NAME = "org.opencms.ade.contenteditor";

    /** The GWT module name. */
    public static final String GWT_MODULE_NAME = CmsCoreData.ModuleKey.contenteditor.name();

    /**
     * Constructor.<p>
     *
     * @param context the page context
     * @param req the servlet request
     * @param res the servlet response
     */
    public CmsContentEditorActionElement(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#export()
     */
    @Override
    public String export() throws Exception {

        return "";
    }

    /**
     * @see org.opencms.gwt.CmsGwtActionElement#exportAll()
     */
    @Override
    public String exportAll() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append(super.export());
        sb.append(exportModuleScriptTag(GWT_MODULE_NAME));
        sb.append(getPrefetch());
        return sb.toString();
    }

    /**
     * Adds link and script tags to the buffer if required for external widgets.<p>
     *
     * @param sb the string buffer to append the tags to
     * @param definition the content definition
     */
    private void addExternalResourceTags(StringBuffer sb, CmsContentDefinition definition) {

        Set<String> includedScripts = new HashSet<String>();
        Set<String> includedStyles = new HashSet<String>();
        for (CmsExternalWidgetConfiguration configuration : definition.getExternalWidgetConfigurations()) {
            for (String css : configuration.getCssResourceLinks()) {
                // avoid including the same resource twice
                if (!includedStyles.contains(css)) {
                    sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"").append(css).append("\"></link>");
                    includedStyles.add(css);
                }
            }
            for (String script : configuration.getJavaScriptResourceLinks()) {
                // avoid including the same resource twice
                if (!includedScripts.contains(script)) {
                    sb.append("<script type=\"text/javascript\" src=\"").append(script).append("\"></script>");
                    includedScripts.add(script);
                }
            }
        }
    }

    /**
     * Returns the prefetch data include.<p>
     *
     * @return the prefetch data include
     *
     * @throws Exception if something goes wrong
     */
    private String getPrefetch() throws Exception {

        long timer = 0;
        if (CmsContentService.LOG.isDebugEnabled()) {
            timer = System.currentTimeMillis();
        }
        CmsContentDefinition definition = CmsContentService.prefetch(getRequest());
        StringBuffer sb = new StringBuffer();
        String backlink = getRequest().getParameter(CmsEditor.PARAM_BACKLINK);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(backlink)) {
            backlink = link(CmsWorkplace.JSP_WORKPLACE_URI);
        } else {
            backlink = link(backlink);
        }
        sb.append(wrapScript(I_CmsContentService.PARAM_BACKLINK, "='", backlink, "';\n"));
        String prefetchedData = exportDictionary(
            I_CmsContentService.DICT_CONTENT_DEFINITION,
            I_CmsContentService.class.getMethod("prefetch"),
            definition);
        sb.append(prefetchedData);
        addExternalResourceTags(sb, definition);
        if (CmsContentService.LOG.isDebugEnabled()) {
            CmsContentService.LOG.debug(
                Messages.get().getBundle().key(
                    Messages.LOG_TAKE_PREFETCHING_TIME_FOR_RESOURCE_2,
                    definition.getSitePath(),
                    "" + (System.currentTimeMillis() - timer)));
        }
        return sb.toString();
    }
}

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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors.directedit;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.Messages;

/**
 * Provider for the OpenCms graphical "direct edit" buttons. <p>
 *
 * Uses the JQuery library to create the buttons and place it to the correct position.<p>
 *
 * This provider DOES NOT support {@link CmsDirectEditMode#MANUAL} mode.<p>
 *
 * @since 7.0.3
 */
public class CmsDirectEditJQueryProvider extends CmsDirectEditDefaultProvider {

    /** Default direct edit include file URI for the jQuery direct edit provider. */
    protected static final String INCLUDE_FILE_JQUERY = "/system/workplace/editors/jquery_direct_edit_include.txt";

    /** Contains the close link. */
    private String m_closeLink;

    /**
     *
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#getDirectEditIncludes(org.opencms.workplace.editors.directedit.CmsDirectEditParams)
     */
    @Override
    public String getDirectEditIncludes(CmsDirectEditParams params) {

        m_closeLink = getLink(params.getLinkForClose());
        return super.getDirectEditIncludes(params);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#init(org.opencms.file.CmsObject, org.opencms.workplace.editors.directedit.CmsDirectEditMode, java.lang.String)
     */
    @Override
    public void init(CmsObject cms, CmsDirectEditMode mode, String fileName) {

        if (CmsStringUtil.isEmpty(fileName)) {
            fileName = INCLUDE_FILE_JQUERY;
        }
        super.init(cms, mode, fileName);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#newInstance()
     */
    @Override
    public I_CmsDirectEditProvider newInstance() {

        CmsDirectEditJQueryProvider result = new CmsDirectEditJQueryProvider();
        result.m_configurationParameters = m_configurationParameters;
        return result;
    }

    /**
     *
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#startDirectEditDisabled(org.opencms.workplace.editors.directedit.CmsDirectEditParams, org.opencms.workplace.editors.directedit.CmsDirectEditResourceInfo)
     */
    @Override
    public String startDirectEditDisabled(CmsDirectEditParams params, CmsDirectEditResourceInfo resourceInfo) {

        return appendDirectEditData(params, false);
    }

    /**
     *
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#startDirectEditEnabled(org.opencms.workplace.editors.directedit.CmsDirectEditParams, org.opencms.workplace.editors.directedit.CmsDirectEditResourceInfo)
     */
    @Override
    public String startDirectEditEnabled(CmsDirectEditParams params, CmsDirectEditResourceInfo resourceInfo) {

        return appendDirectEditData(params, false);
    }

    /**
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#prepareMacroResolverForIncludes(org.opencms.workplace.editors.directedit.CmsDirectEditParams)
     */
    @Override
    protected CmsMacroResolver prepareMacroResolverForIncludes(CmsDirectEditParams params) {

        CmsMacroResolver resolver = super.prepareMacroResolverForIncludes(params);
        resolver.addMacro(
            "jquery_flydom",
            readFile("/system/modules/org.opencms.jquery/resources/packed/jquery.flydom.js"));
        resolver.addMacro(
            "jquery_dimensions",
            readFile("/system/modules/org.opencms.jquery/resources/packed/jquery.dimensions.js"));
        resolver.addMacro("jquery", readFile("/system/modules/org.opencms.jquery/resources/packed/jquery.js"));
        return resolver;
    }

    /**
     * Appends the data for the direct edit buttons, which are dynamically created with jQuery.<p>
     *
     * Generates the following code:<p>
     * <pre>
     *  &#60;script type="text/javascript" &#62;
     *      ocms_de_data['key']= {
     *          id: key,
     *          resource: res,
     *          ...
     *      };
     *  &#60;/script &#62;
     *  </pre>
     *
     * @param params the direct edit parameters
     * @param disabled if the buttons are disabled or not
     *
     * @return the data needed for the direct edit buttons
     */
    private String appendDirectEditData(CmsDirectEditParams params, boolean disabled) {

        StringBuffer result = new StringBuffer(512);
        String editId = getNextDirectEditId();

        result.append("\n<script type=\"text/javascript\">\n");

        result.append("ocms_de_data['").append(editId).append("']= {\n");
        result.append("\t").append("id: '").append(editId).append("',\n");
        result.append("\t").append("deDisabled: ").append(disabled).append(",\n");
        result.append("\t").append("hasEdit: ").append(params.getButtonSelection().isShowEdit()).append(",\n");
        result.append("\t").append("hasDelete: ").append(params.getButtonSelection().isShowDelete()).append(",\n");
        result.append("\t").append("hasNew: ").append(params.getButtonSelection().isShowNew()).append(",\n");
        result.append("\t").append("resource: '").append(params.getResourceName()).append("',\n");
        result.append("\t").append("editLink: '").append(getLink(params.getLinkForEdit())).append("',\n");
        result.append("\t").append("language: '").append(m_cms.getRequestContext().getLocale().toString());
        result.append("',\n");
        result.append("\t").append("element: '").append(params.getElement()).append("',\n");
        result.append("\t").append("backlink: '").append(m_cms.getRequestContext().getUri()).append("',\n");
        result.append("\t").append("newlink: '").append(CmsEncoder.encode(params.getLinkForNew())).append("',\n");
        result.append("\t").append("closelink: '").append(m_closeLink).append("',\n");
        result.append("\t").append("deletelink: '").append(getLink(params.getLinkForDelete())).append("',\n");
        if (!disabled) {
            result.append("\t").append("button_edit: '");
            result.append(m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_EDIT_0)).append("',\n");
            result.append("\t").append("button_delete: '");
            result.append(m_messages.key(Messages.GUI_BUTTON_DELETE_0)).append("',\n");
            result.append("\t").append("button_new: '");
            result.append(m_messages.key(Messages.GUI_BUTTON_NEW_0)).append("',\n");
        } else {
            result.append("\t").append("button_edit: '");
            result.append(m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_LOCKED_0)).append("',\n");
            result.append("\t").append("button_delete: '");
            result.append(m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_LOCKED_0)).append("',\n");
            result.append("\t").append("button_new: '");
            result.append(m_messages.key(Messages.GUI_EDITOR_FRONTEND_BUTTON_LOCKED_0)).append("',\n");
        }
        result.append("\t").append("editortitle: '").append(m_messages.key(Messages.GUI_EDITOR_TITLE_NEW_0));

        result.append("'\n");
        result.append("};\n");
        result.append("</script>\n");

        result.append("<div id=\"").append(editId).append("\" class=\"ocms_de_norm\">");
        return result.toString();
    }

    /**
     * Helper method to read the content of an included Javascript file.<p>
     *
     * @param path the root path of a Javascript file
     *
     * @return the content of the Javascript file
     */
    private String readFile(String path) {

        String result = (String)CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().getCachedObject(m_cms, path);
        if (result == null) {
            try {
                CmsFile file = m_cms.readFile(path);
                result = getContentAsString(file);
            } catch (CmsException e) {
                throw new RuntimeException(e);
            }
        }
        if (result != null) {
            CmsVfsMemoryObjectCache.getVfsMemoryObjectCache().putCachedObject(m_cms, path, result);
        }
        return result;

    }
}

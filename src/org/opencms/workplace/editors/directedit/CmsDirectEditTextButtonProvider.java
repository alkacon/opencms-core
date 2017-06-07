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

package org.opencms.workplace.editors.directedit;

import org.opencms.i18n.CmsEncoder;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.editors.Messages;

/**
 * Creates HTML for simple text based direct edit buttons.<p>
 *
 * This provider support {@link CmsDirectEditMode#MANUAL} mode.<p>
 *
 * @since 6.2.3
 */
public class CmsDirectEditTextButtonProvider extends CmsDirectEditDefaultProvider {

    /** The HTML for the direct edit end (only enabled). */
    private String m_endHtml;

    /**
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#endDirectEditDisabled()
     */
    @Override
    public String endDirectEditDisabled() {

        return "";
    }

    /**
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#endDirectEditEnabled()
     */
    @Override
    public String endDirectEditEnabled() {

        String result = "";
        if (CmsStringUtil.isNotEmpty(m_endHtml)) {
            result = m_endHtml;
            m_endHtml = null;
        }
        return result;
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#isManual(org.opencms.workplace.editors.directedit.CmsDirectEditMode)
     */
    @Override
    public boolean isManual(CmsDirectEditMode mode) {

        return (mode == CmsDirectEditMode.MANUAL)
            || ((m_mode == CmsDirectEditMode.MANUAL) && (mode == CmsDirectEditMode.TRUE));
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#newInstance()
     */
    @Override
    public I_CmsDirectEditProvider newInstance() {

        CmsDirectEditTextButtonProvider result = new CmsDirectEditTextButtonProvider();
        result.m_configurationParameters = m_configurationParameters;
        return result;
    }

    /**
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#startDirectEditDisabled(org.opencms.workplace.editors.directedit.CmsDirectEditParams, org.opencms.workplace.editors.directedit.CmsDirectEditResourceInfo)
     */
    @Override
    public String startDirectEditDisabled(CmsDirectEditParams params, CmsDirectEditResourceInfo resourceInfo) {

        StringBuffer result = new StringBuffer(256);

        result.append("<span class=\"ocms_txt_dis\">");
        result.append(m_messages.key(Messages.GUI_DIRECTEDIT_TEXT_EDIT_0));
        result.append("</span>");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider#startDirectEditEnabled(org.opencms.workplace.editors.directedit.CmsDirectEditParams, org.opencms.workplace.editors.directedit.CmsDirectEditResourceInfo)
     */
    @Override
    public String startDirectEditEnabled(CmsDirectEditParams params, CmsDirectEditResourceInfo resourceInfo) {

        String editId = getNextDirectEditId();
        StringBuffer result = new StringBuffer(256);
        String linkForNew = CmsEncoder.encode(params.getLinkForNew());

        result.append("<span class=\"ocms_txt_en\">");
        if (params.getButtonSelection().isShowEdit()) {
            result.append("[<a href=\"#\" class=\"ocms_txt\" onclick=\"javascript:submitOcms(\'").append(editId).append(
                "\', \'").append(CmsDirectEditButtonSelection.VALUE_EDIT).append("\');\">");
            result.append(m_messages.key(Messages.GUI_DIRECTEDIT_TEXT_EDIT_0));
            result.append("</a>]");
        }
        if (params.getButtonSelection().isShowDelete()) {
            result.append("\n[<a href=\"#\" class=\"ocms_txt\" onclick=\"javascript:submitOcms(\'").append(
                editId).append("\', \'").append(CmsDirectEditButtonSelection.VALUE_DELETE).append("\');\">");
            result.append(m_messages.key(Messages.GUI_DIRECTEDIT_TEXT_DELETE_0));
            result.append("</a>]");
        }
        if (params.getButtonSelection().isShowNew()) {
            result.append("\n[<a href=\"#\" class=\"ocms_txt\" onclick=\"javascript:submitOcms(\'").append(
                editId).append("\', \'").append(CmsDirectEditButtonSelection.VALUE_NEW).append("\', \'").append(
                    linkForNew).append("\');\">");
            result.append(m_messages.key(Messages.GUI_DIRECTEDIT_TEXT_NEW_0));
            result.append("</a>]");
        }
        result.append("</span>");

        StringBuffer endHtml = new StringBuffer(256);
        String editLocale = m_cms.getRequestContext().getLocale().toString();
        String uri = m_cms.getRequestContext().getUri();
        String linkForEdit = getLink(params.getLinkForEdit());
        endHtml.append("<form class=\"ocms_nomargin\" name=\"form_").append(editId).append("\" id=\"form_").append(
            editId).append("\" method=\"post\" action=\"").append(linkForEdit).append("\" target=\"_top\">\n");
        endHtml.append("<input type=\"hidden\" name=\"resource\" value=\"").append(params.getResourceName()).append(
            "\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"directedit\" value=\"true\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"elementlanguage\" value=\"").append(editLocale).append("\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"elementname\" value=\"").append(params.getElement()).append(
            "\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"backlink\" value=\"").append(uri).append("\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"newlink\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"closelink\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"redirect\" value=\"true\"/>\n");
        endHtml.append("<input type=\"hidden\" name=\"editortitle\"/>\n");
        endHtml.append("</form>");
        m_endHtml = endHtml.toString();

        return result.toString();
    }
}
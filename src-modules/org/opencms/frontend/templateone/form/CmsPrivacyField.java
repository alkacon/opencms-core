/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsPrivacyField.java,v $
 * Date   : $Date: 2011/03/23 14:50:48 $
 * Version: $Revision: 1.10 $
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

package org.opencms.frontend.templateone.form;

import org.opencms.i18n.CmsMessages;
import org.opencms.util.CmsStringUtil;

/**
 * Represents a check box with a link.<p>
 * 
 */
public class CmsPrivacyField extends CmsCheckboxField {

    /** HTML field type: checkbox. */
    private static final String TYPE = "privacy";

    /**
     * Returns the type of the input field, e.g. "text" or "select".<p>
     * 
     * @return the type of the input field
     */
    public static String getStaticType() {

        return TYPE;
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#buildHtml(CmsFormHandler, org.opencms.i18n.CmsMessages, String)
     */
    @Override
    public String buildHtml(CmsFormHandler formHandler, CmsMessages messages, String errorKey) {

        StringBuffer buf = new StringBuffer();
        String fieldLabel = getLabel();
        String errorMessage = "";
        String mandatory = "";
        boolean showMandatory = false;

        if (isMandatory()) {
            mandatory = messages.key("form.html.mandatory");
        }
        // show the text with the mandatory, if exits
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(fieldLabel)) {
            fieldLabel = fieldLabel + mandatory;
            showMandatory = true;
        } else {
            fieldLabel = "&nbsp;";
        }

        if (CmsStringUtil.isNotEmpty(errorKey)) {

            if (CmsFormHandler.ERROR_MANDATORY.equals(errorKey)) {
                errorMessage = messages.key("form.error.mandatory");
            } else if (CmsStringUtil.isNotEmpty(getErrorMessage())) {
                errorMessage = getErrorMessage();
            } else {
                errorMessage = messages.key("form.error.validation");
            }

            errorMessage = messages.key("form.html.error.start") + errorMessage + messages.key("form.html.error.end");
            fieldLabel = messages.key("form.html.label.error.start")
                + fieldLabel
                + messages.key("form.html.label.error.end");
        }

        // line #1
        if (showRowStart(messages.key("form.html.col.two"))) {
            buf.append(messages.key("form.html.row.start")).append("\n");
        }

        // add the item
        if (getItems().size() > 0) {

            // line #2
            buf.append(messages.key("form.html.label.start")).append(fieldLabel).append(
                messages.key("form.html.label.end")).append("\n");

            // line #3
            buf.append(messages.key("form.html.field.start")).append("\n");

            CmsFieldItem curOption = (CmsFieldItem)getItems().get(0);
            String checked = "";
            if (curOption.isSelected()) {
                checked = " checked=\"checked\"";
            }
            //checks if intern link
            String link = curOption.getLabel();
            if (link.startsWith("/")) {
                link = formHandler.link(link);
            }

            buf.append("<input type=\"checkbox\" name=\"").append(getName()).append("\" value=\"").append(
                curOption.getValue()).append("\"").append(checked).append("/>");
            //insert a link
            buf.append("<a href=\"").append(link).append("\" rel=\"_blank\">").append(curOption.getValue()).append(
                showMandatory ? "" : mandatory).append("</a>");

            buf.append("\n");
        }

        buf.append(errorMessage).append("\n");

        buf.append(messages.key("form.html.field.end")).append("\n");

        if (showRowEnd(messages.key("form.html.col.two"))) {
            buf.append(messages.key("form.html.row.end")).append("\n");
        }

        return buf.toString();
    }

    /**
     * @see org.opencms.frontend.templateone.form.I_CmsField#getType()
     */
    @Override
    public String getType() {

        return TYPE;
    }

}

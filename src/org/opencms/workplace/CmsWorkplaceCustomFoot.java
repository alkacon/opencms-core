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

package org.opencms.workplace;

import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;

/**
 * A custom foot configuration to create a specially designed foot for the OpenCms workplace.<p>
 *
 * @since 6.9.2
 */
public class CmsWorkplaceCustomFoot {

    /** The default foot frame font color. */
    public static final String DEFAUL_COLOR = "/*begin-color WindowText*/#000000/*end-color*/";

    /** The default foot frame background color. */
    public static final String DEFAULT_BACKGROUNDCOLOR = "/*begin-color ThreeDFace*/#f0f0f0/*end-color*/";

    /** The default workplace foot text to display. */
    public static final String DEFAULT_TEXT = "%("
        + CmsMacroResolver.KEY_LOCALIZED_PREFIX
        + Messages.GUI_LABEL_USER_0
        + ") %("
        + CmsMacroResolver.KEY_CURRENT_USER_FULLNAME
        + ") %("
        + CmsMacroResolver.KEY_LOCALIZED_PREFIX
        + Messages.GUI_LABEL_LOGINTIME_0
        + ") %("
        + CmsMacroResolver.KEY_CURRENT_USER_LASTLOGIN
        + ") %("
        + CmsMacroResolver.KEY_LOCALIZED_PREFIX
        + Messages.GUI_LABEL_LOGINADDRESS_0
        + ") %("
        + CmsMacroResolver.KEY_OPENCMS
        + "remoteaddress) ";

    /** The default workplace foot text to display. */
    public static final String DEFAULT_TEXT_WITH_OU = DEFAULT_TEXT
        + "[%("
        + CmsMacroResolver.KEY_LOCALIZED_PREFIX
        + Messages.GUI_LABEL_OU_0
        + ") %("
        + CmsMacroResolver.KEY_CURRENT_ORGUNIT_DESCRIPTION
        + ")] ";

    /** The background color of the foot frame. */
    private String m_backgroundColor;

    /** The font color of the foot frame. */
    private String m_color;

    /** Indicates if the default text should be replaced or kept. */
    private boolean m_replaceDefault;

    /** The additional text of the foot frame. */
    private String m_text;

    /** The text shown in the foot frame (can contain macros). */
    private String m_textShown;

    /**
     * Empty constructor.<p>
     */
    public CmsWorkplaceCustomFoot() {

        // initialize members with default or empty values
        m_backgroundColor = DEFAULT_BACKGROUNDCOLOR;
        m_color = DEFAUL_COLOR;
        m_text = "";
    }

    /**
     * Returns the background color of the foot frame.<p>
     *
     * @return the background color of the foot frame
     */
    public String getBackgroundColor() {

        return m_backgroundColor;
    }

    /**
     * Returns the font color of the foot frame.<p>
     *
     * @return the font color of the foot frame
     */
    public String getColor() {

        return m_color;
    }

    /**
     * Returns the additional text of the foot frame.<p>
     *
     * @return the additional text of the foot frame
     */
    public String getText() {

        return m_text;
    }

    /**
     * Returns the text of the foot frame with resolved macros.<p>
     *
     * @param wp the initialized workplace dialog
     * @return the text of the foot frame with resolved macros
     */
    public String getTextResolved(CmsWorkplace wp) {

        CmsMacroResolver resolver = CmsMacroResolver.newInstance();
        resolver.setCmsObject(wp.getCms());
        resolver.setJspPageContext(wp.getJsp().getJspContext());
        resolver.setMessages(wp.getMessages());
        if (m_textShown == null) {
            // create the shown text containing macros
            StringBuffer text = new StringBuffer(512);
            if (!isReplaceDefault()) {
                // the default text should be shown
                try {
                    if (OpenCms.getOrgUnitManager().getOrganizationalUnits(wp.getCms(), "", true).isEmpty()) {
                        text.append(DEFAULT_TEXT).append(" ");
                    } else {
                        text.append(DEFAULT_TEXT_WITH_OU).append(" ");
                    }
                } catch (CmsException e) {
                    text.append(DEFAULT_TEXT).append(" ");
                }
            }
            text.append(m_text);
            m_textShown = text.toString();
        }
        // return the resolved text
        return resolver.resolveMacros(m_textShown);
    }

    /**
     * Returns if the default text should be replaced or kept.<p>
     *
     * @return true if the default text should be replaced, otherwise false
     */
    public boolean isReplaceDefault() {

        return m_replaceDefault;
    }

    /**
     * Sets the background color of the foot frame.<p>
     *
     * @param backgroundColor the background color of the foot frame
     */
    public void setBackgroundColor(String backgroundColor) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(backgroundColor)) {
            m_backgroundColor = backgroundColor;
        }
    }

    /**
     * Sets the font color of the foot frame.<p>
     *
     * @param color the font color of the foot frame
     */
    public void setColor(String color) {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(color)) {
            m_color = color;
        }
    }

    /**
     * Sets the additional text of the foot frame.<p>
     *
     * @param text the additional text of the foot frame
     * @param replaceDefault flag indicating if the default text should be replaced or kept
     */
    public void setText(String text, String replaceDefault) {

        m_replaceDefault = Boolean.valueOf(replaceDefault).booleanValue();
        m_text = text;
    }

}

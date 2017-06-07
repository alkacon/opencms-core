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

package org.opencms.gwt.shared.property;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap initialization data.<p>
 *
 * @since 8.0
 */
public class CmsClientTemplateBean implements IsSerializable {

    /** The description. */
    private String m_description;

    /** The image path. */
    private String m_imgPath;

    /** True if the template should be displayed with weak text. */
    private boolean m_showWeakText;

    /** The site path. */
    private String m_sitePath;

    /** The title. */
    private String m_title;

    /**
     * Constructor.<p>
     */
    public CmsClientTemplateBean() {

        // empty
    }

    /**
     * Constructor.<p>
     *
     * @param title the title
     * @param description the description
     * @param sitePath the site path
     * @param imgPath the image path
     */
    public CmsClientTemplateBean(String title, String description, String sitePath, String imgPath) {

        m_title = title;
        m_description = description;
        m_sitePath = sitePath;
        m_imgPath = imgPath;
    }

    /**
     * Returns a dummy template object which represents an empty selection.<p>
     *
     * @return a dummy template object
     */
    public static CmsClientTemplateBean getNullTemplate() {

        String imagePath = "/system/workplace/resources/commons/notemplate.png";
        CmsClientTemplateBean result = new CmsClientTemplateBean("No template", "", "", imagePath);
        result.setShowWeakText(true);
        return result;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the image path.<p>
     *
     * @return the image path
     */
    public String getImgPath() {

        return m_imgPath;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns true if the template should be shown with weak text.<p>
     *
     * @return true if the template should be shown with weak text
     */
    public boolean isShowWeakText() {

        return m_showWeakText;
    }

    /**
     * Sets the display of weak text to true or false.<p>
     *
     * @param showWeakText if true, the template should be displayed with weak text
     */
    public void setShowWeakText(boolean showWeakText) {

        m_showWeakText = showWeakText;
    }

}

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

package org.opencms.gwt.shared;

import org.opencms.util.CmsStringUtil;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Info needed to preview a resource.<p>
 */
public class CmsPreviewInfo implements IsSerializable {

    /** The required height. */
    private int m_height;

    /** The preview locale. */
    private String m_locale;

    /** The available preview locales. */
    private LinkedHashMap<String, String> m_locales;

    /** Flag indicating that the preview should be opened in a new window. */
    private boolean m_newWindowRequired;

    /** The preview content. */
    private String m_previewContent;

    /** The URL to call for the preview. */
    private String m_previewUrl;

    /** The site path. */
    private String m_sitePath;

    /** The title. */
    private String m_title;

    /** The required width. */
    private int m_width;

    /**
     * Constructor.<p>
     *
     * @param content the preview content
     * @param url the preview URL
     * @param newWindowRequired flag indicating that the preview should be opened in a new window
     * @param title the title
     * @param sitePath the site path
     * @param locale the preview locale
     */
    public CmsPreviewInfo(
        String content,
        String url,
        boolean newWindowRequired,
        String title,
        String sitePath,
        String locale) {

        m_previewContent = content;
        m_previewUrl = url;
        m_newWindowRequired = newWindowRequired;
        m_title = title;
        m_sitePath = sitePath;
        m_locale = locale;
    }

    /**
     * Constructor. For serialization only.<p>
     */
    protected CmsPreviewInfo() {

        // nothing to do
    }

    /**
     * Returns the required height.<p>
     *
     * @return the required height
     */
    public int getHeight() {

        return m_height;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the locales.<p>
     *
     * @return the locales
     */
    public Map<String, String> getLocales() {

        return m_locales;
    }

    /**
     * Returns the preview content.<p>
     *
     * @return the preview content
     */
    public String getPreviewContent() {

        return m_previewContent;
    }

    /**
     * Returns the preview URL.<p>
     *
     * @return the preview URL
     */
    public String getPreviewUrl() {

        return m_previewUrl;
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
     * Returns the required width.<p>
     *
     * @return the required width
     */
    public int getWidth() {

        return m_width;
    }

    /**
     * Returns if the given preview content has additional locales.<p>
     *
     * @return <code>true</code> if the given preview content has additional locales
     */
    public boolean hasAdditionalLocales() {

        return (m_locales != null) && (m_locales.size() > 1);
    }

    /**
     * Returns if the preview requires specific dimensions.<p>
     *
     * @return <code>true</code> if the dialog requires dimensions
     */
    public boolean hasDimensions() {

        return (m_height > 0) && (m_width > 0);
    }

    /**
     * Returns if preview HTML content is available to display directly.<p>
     *
     * @return if preview HTML content is available to display directly
     */
    public boolean hasPreviewContent() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_previewContent);
    }

    /**
     * Returns if there a URL available to call for the preview.<p>
     *
     * @return if there a URL available to call for the preview
     */
    public boolean hasPreviewUrl() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_previewUrl);
    }

    /**
     * Returns if it is required to open the preview in a new window.<p>
     *
     * @return <code>true</code> if it is required to open the preview in a new window
     */
    public boolean isNewWindowRequired() {

        return m_newWindowRequired;
    }

    /**
     * Sets the required height.<p>
     *
     * @param height the required height to set
     */
    public void setHeight(int height) {

        m_height = height;
    }

    /**
     * Sets the locales.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(LinkedHashMap<String, String> locales) {

        m_locales = locales;
    }

    /**
     * Sets the required width.<p>
     *
     * @param width the required width to set
     */
    public void setWidth(int width) {

        m_width = width;
    }

}

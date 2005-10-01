/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/widgets/CmsHtmlWidgetOption.java,v $
 * Date   : $Date: 2005/10/01 20:50:06 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.widgets;

import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An option of a Html type widget.<p>
 * 
 * If options are passed from XML content schema definitions as widget configuration options,
 * the following syntax is used for defining the option values:<p>
 * 
 * <code>"height:400px,link,anchor,imagegallery,downloadgallery,formatselect,source"</code><p>
 * 
 * Available options are:
 * <ul>
 * <li><code>height:${editorheight}</code>: the editor height, where the height can be specified in px or %, e.g. <code>400px</code></li>
 * <li><code>link</code>: the link dialog button</li>
 * <li><code>anchor</code>: the anchor dialog button</li>
 * <li><code>formatselect</code>: the format selector for selecting text format like paragraph or headings</li>
 * <li><code>source</code>: shows the source code toggle button(s)</li>
 * <li><code>${gallerytype}</code>: Shows a gallery dialog button, e.g. <code>imagegallery</code></li>
 * </ul>
 * If an option key is not found in the configuration options, the corresponding button will be hidden in the editor.<p>
 * 
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.0.1
 */
public class CmsHtmlWidgetOption {

    /** The editor widget default height to use. */
    public static final String EDITOR_DEFAULTHEIGHT = "260px";

    /** Option for the "anchor" dialog. */
    private static final String OPTION_ANCHOR = "anchor";

    /** The delimiter to use in the configuration String. */
    private static final String OPTION_DELIMITER = ",";

    /** Option for the "formatselect" selector. */
    private static final String OPTION_FORMATSELECT = "formatselect";

    /** Option for the "height" configuration. */
    private static final String OPTION_HEIGHT = "height:";

    /** Option for the "link" dialog. */
    private static final String OPTION_LINK = "link";

    /** Option for the "source" code mode. */
    private static final String OPTION_SOURCE = "source";

    private List m_displayGalleries;
    private String m_editorHeight;
    private boolean m_showAnchorDialog;
    private boolean m_showFormatSelect;
    private boolean m_showLinkDialog;
    private boolean m_showSourceEditor;

    /**
     * Creates a new empty html widget object object.<p>
     */
    public CmsHtmlWidgetOption() {

        // initialize the members
        m_displayGalleries = new ArrayList();
        m_editorHeight = EDITOR_DEFAULTHEIGHT;
    }

    /**
     * Creates a new html widget object object, configured by the given configuration String.<p>
     * 
     * @param configuration configuration String to parse
     */
    public CmsHtmlWidgetOption(String configuration) {

        // initialize the widget options
        m_displayGalleries = new ArrayList();
        m_editorHeight = EDITOR_DEFAULTHEIGHT;
        parseOptions(configuration);
    }

    /**
     * Returns a html widget configuration String created from the given html widget option.<p>
     * 
     * @param option the html widget options to create the configuration String for
     * 
     * @return a select widget configuration String created from the given html widget option object
     */
    public static String createConfigurationString(CmsHtmlWidgetOption option) {

        StringBuffer result = new StringBuffer(256);
        boolean added = false;
        if (!option.getEditorHeight().equals(EDITOR_DEFAULTHEIGHT)) {
            // append the height configuration
            result.append(OPTION_HEIGHT);
            result.append(option.getEditorHeight());
            added = true;
        }
        if (option.showAnchorDialog()) {
            // append the anchor configuration
            if (added) {
                result.append(OPTION_DELIMITER);
            }
            result.append(OPTION_ANCHOR);
            added = true;
        }
        if (option.showLinkDialog()) {
            // append the link configuration
            if (added) {
                result.append(OPTION_DELIMITER);
            }
            result.append(OPTION_LINK);
            added = true;
        }
        if (option.showFormatSelect()) {
            // append the format selector configuration
            if (added) {
                result.append(OPTION_DELIMITER);
            }
            result.append(OPTION_FORMATSELECT);
            added = true;
        }
        if (option.showSourceEditor()) {
            // append the source code configuration
            if (added) {
                result.append(OPTION_DELIMITER);
            }
            result.append(OPTION_SOURCE);
            added = true;
        }

        boolean isFirst = true;
        for (int i = 0; i < option.getDisplayGalleries().size(); i++) {
            // append the galleries configuration
            String gallery = (String)option.getDisplayGalleries().get(i);
            if (added || !isFirst) {
                result.append(OPTION_DELIMITER);
            }
            result.append(gallery);
            isFirst = false;
        }

        return result.toString();
    }

    /**
     * Returns the displayed gallery names.<p>
     * 
     * @return the displayed gallery names
     */
    public List getDisplayGalleries() {

        return m_displayGalleries;
    }

    /**
     * Returns the widget editor height.<p>
     * 
     * @return the widget editor height
     */
    public String getEditorHeight() {

        return m_editorHeight;
    }

    /**
     * Sets the displayed gallery names.<p>
     * 
     * @param displayGalleries the displayed gallery names
     */
    public void setDisplayGalleries(List displayGalleries) {

        m_displayGalleries = displayGalleries;
    }

    /**
     * Sets the widget editor height.<p>
     * 
     * @param editorHeight the widget editor height
     */
    public void setEditorHeight(String editorHeight) {

        m_editorHeight = editorHeight;
    }

    /**
     * Sets if the anchor dialog button should be available.<p>
     * 
     * @param showAnchorDialog true if the anchor dialog button should be available, otherwise false
     */
    public void setShowAnchorDialog(boolean showAnchorDialog) {

        m_showAnchorDialog = showAnchorDialog;
    }

    /**
     * Sets if the format selector should be available.<p>
     * 
     * @param showFormatSelect true if the format selector should be available, otherwise false
     */
    public void setShowFormatSelect(boolean showFormatSelect) {

        m_showFormatSelect = showFormatSelect;
    }

    /**
     * Sets if the link dialog button should be available.<p>
     * 
     * @param showLinkDialog true if the link dialog button should be available, otherwise false
     */
    public void setShowLinkDialog(boolean showLinkDialog) {

        m_showLinkDialog = showLinkDialog;
    }

    /**
     * Sets if the source code button should be available.<p>
     * 
     * @param showSourceEditor true if the source code button should be available, otherwise false
     */
    public void setShowSourceEditor(boolean showSourceEditor) {

        m_showSourceEditor = showSourceEditor;
    }

    /**
     * Returns true if the anchor dialog button should be available.<p>
     * 
     * @return if the anchor dialog button should be available
     */
    public boolean showAnchorDialog() {

        return m_showAnchorDialog;
    }

    /**
     * Returns true if the format selector should be available.<p>
     * 
     * @return if the format selector should be available
     */
    public boolean showFormatSelect() {

        return m_showFormatSelect;
    }

    /**
     * Returns true if the specified gallery type dialog button is shown.<p>
     * 
     * @param galleryType the gallery type to check
     * @return true if the specified gallery type dialog button is shown, otherwise false
     */
    public boolean showGalleryDialog(String galleryType) {

        return getDisplayGalleries().contains(galleryType);
    }

    /**
     * Returns true if the link dialog button should be available.<p>
     * 
     * @return if the link dialog button should be available
     */
    public boolean showLinkDialog() {

        return m_showLinkDialog;
    }

    /**
     * Returns true if the source code button should be available.<p>
     * 
     * @return if the source code button should be available
     */
    public boolean showSourceEditor() {

        return m_showSourceEditor;
    }

    /**
     * Parses the given configuration String.<p>
     * 
     * @param configuration the configuration String to parse
     */
    protected void parseOptions(String configuration) {

        if (CmsStringUtil.isNotEmpty(configuration)) {
            List options = CmsStringUtil.splitAsList(configuration, OPTION_DELIMITER, true);
            Iterator i = options.iterator();
            while (i.hasNext()) {
                String option = ((String)i.next()).toLowerCase();
                if (OPTION_LINK.equals(option)) {
                    // show link dialog
                    setShowLinkDialog(true);
                } else if (OPTION_ANCHOR.equals(option)) {
                    // show anchor dialog
                    setShowAnchorDialog(true);
                } else if (OPTION_SOURCE.equals(option)) {
                    // show link dialog
                    setShowSourceEditor(true);
                } else if (OPTION_FORMATSELECT.equals(option)) {
                    // show link dialog
                    setShowFormatSelect(true);
                } else if (option.startsWith(OPTION_HEIGHT)) {
                    // the editor height
                    option = option.substring(OPTION_HEIGHT.length());
                    if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(option)) {
                        setEditorHeight(option.trim());
                    }
                } else {
                    // check if option describes a gallery
                    if (OpenCms.getWorkplaceManager().getGalleries().get(option) != null) {
                        // add the option to the displayed galleries
                        m_displayGalleries.add(option);
                    }

                }
            }
        }
    }

}
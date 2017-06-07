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

package org.opencms.widgets;

import org.opencms.i18n.CmsMessages;

import java.util.Locale;
import java.util.Set;

/**
 * Describes a widget enabled dialog.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsWidgetDialog {

    /**
     * Generates a button for the widget dialog.<p>
     *
     * @param href the href link for the button, if none is given the button will be disabled
     * @param target the href link target for the button, if none is given the target will be same window
     * @param image the image name for the button, skin path will be automattically added as prefix
     * @param label the label for the text of the button
     * @param type 0: image only (default), 1: image and text, 2: text only
     *
     * @return a button for the OpenCms workplace
     *
     * @see org.opencms.workplace.CmsWorkplace#button(String, String, String, String, int)
     */
    String button(String href, String target, String image, String label, int type);

    /**
     * Returns the html for a button bar.<p>
     *
     * @param segment the HTML segment (START / END)
     *
     * @return a button bar html start / end segment
     */
    String buttonBar(int segment);

    /**
     * Generates a horizontal button bar separator line with maximum width.<p>
     *
     * @return a horizontal button bar separator line
     */
    String buttonBarHorizontalLine();

    /**
     * Generates a button bar separator.<p>
     *
     * @param leftPixel the amount of pixel left to the separator
     * @param rightPixel the amount of pixel right to the separator
     *
     * @return a button bar separator
     */
    String buttonBarSeparator(int leftPixel, int rightPixel);

    /**
     * Returns the html for an invisible spacer between button bar contents like buttons, labels, etc.<p>
     *
     * @param width the width of the invisible spacer
     * @return the html for the invisible spacer
     *
     * @see org.opencms.workplace.CmsWorkplace#buttonBarSpacer(int)
     */
    String buttonBarSpacer(int width);

    /**
     * Generates a button bar starter tab.<p>
     *
     * @param leftPixel the amount of pixel left to the starter
     * @param rightPixel the amount of pixel right to the starter
     *
     * @return a button bar starter tab
     */
    String buttonBarStartTab(int leftPixel, int rightPixel);

    /**
     * Builds an invisible horiziontal spacer with the specified width.<p>
     *
     * @param width the width of the spacer in pixels
     * @return an invisible horiziontal spacer with the specified width
     *
     * @see org.opencms.workplace.CmsDialog#dialogHorizontalSpacer(int)
     */
    String dialogHorizontalSpacer(int width);

    /**
     * Returns the style setting to use when generating buttons for this widget dialog.<p>
     *
     * @return the style setting to use when generating buttons for this widget dialog
     *
     * @see org.opencms.db.CmsUserSettings#getEditorButtonStyle()
     */
    int getButtonStyle();

    /**
     * Returns a set of help messages ids that are already included on the widget dialog.<p>
     *
     * This is used to prevent the occurence of multiple html <code>div</code> id's with the same
     * value when generating the help texts. For valid html, each id can be used only once.<p>
     *
     * @return a set of help messages ids that are already included on the widget dialog
     */
    Set<String> getHelpMessageIds();

    /**
     * Returns the current users locale setting.<p>
     *
     * @return the current users locale setting
     *
     * @see org.opencms.workplace.CmsWorkplace#getLocale()
     * @see org.opencms.file.CmsRequestContext#getLocale()
     */
    Locale getLocale();

    /**
     * Returns a messages object used to render localized keys for the widget dialog.<p>
     *
     * @return a messages object used to render localized keys for the widget dialog
     */
    CmsMessages getMessages();

    /**
     * Returns the "user-agent" of the current request, or <code>null</code> in case no
     * request is available.<p>
     *
     * @return the "user-agent" of the current request
     */
    String getUserAgent();

    /**
     * Tests if we are working with the new administration dialog style.<p>
     *
     * This param is not intended for external use.<p>
     *
     * @return <code>true</code> if using the new style
     */
    boolean useNewStyle();
}
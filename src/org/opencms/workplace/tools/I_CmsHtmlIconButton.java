/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/I_CmsHtmlIconButton.java,v $
 * Date   : $Date: 2005/09/16 13:11:12 $
 * Version: $Revision: 1.5.2.1 $
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

package org.opencms.workplace.tools;

import org.opencms.i18n.CmsMessageContainer;
import org.opencms.workplace.CmsWorkplace;

/**
 * Interface for html buttons with icon.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.5.2.1 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsHtmlIconButton {

    /**
     * Returns the necessary html code.<p>
     * 
     * @param wp the jsp page to write the code to
     * 
     * @return html code
     */
    String buttonHtml(CmsWorkplace wp);

    /**
     * Returns the help text.<p>
     *
     * @return the help text
     */
    CmsMessageContainer getHelpText();

    /**
     * Returns the path to the icon.<p>
     *
     * @return the path to the icon
     */
    String getIconPath();

    /**
     * Returns the id of the html component.<p>
     * 
     * @return the id
     */
    String getId();

    /**
     * Returns the display name.<p>
     *
     * @return the display name
     */
    CmsMessageContainer getName();

    /**
     * Returns if enabled or disabled.<p>
     *
     * @return if enabled or disabled
     */
    boolean isEnabled();

    /**
     * Returns if visible or not.<p>
     *
     * @return if visible or not
     */
    boolean isVisible();

    /**
     * Sets if enabled or disabled.<p>
     *
     * @param enabled if enabled or disabled
     */
    void setEnabled(boolean enabled);

    /**
     * Sets the help Text.<p>
     *
     * @param helpText the help Text to set
     */
    void setHelpText(CmsMessageContainer helpText);

    /**
     * Sets the icon Path.<p>
     *
     * @param iconPath the icon Path to set
     */
    void setIconPath(String iconPath);

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    void setName(CmsMessageContainer name);

    /**
     * Sets if visible or not.<p>
     *
     * @param visible if visible or not
     */
    void setVisible(boolean visible);

}
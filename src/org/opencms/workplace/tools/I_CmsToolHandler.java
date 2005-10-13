/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/tools/I_CmsToolHandler.java,v $
 * Date   : $Date: 2005/10/13 11:06:32 $
 * Version: $Revision: 1.17 $
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

import org.opencms.file.CmsObject;
import org.opencms.workplace.CmsWorkplace;

import java.util.Map;

/**
 * Interface for an admin tool handler.<p>
 * 
 * These handlers are created and managed by the 
 * <code>{@link org.opencms.workplace.tools.CmsToolManager}</code>.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.17 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsToolHandler {

    /**
     * Returns the help text if disabled.<p>
     * 
     * @return the help text if disabled
     */
    String getDisabledHelpText();

    /**
     * Returns the group.<p>
     *
     * @return the group
     */
    String getGroup();

    /**
     * Returns the help text.<p>
     * 
     * @return the help text
     */
    String getHelpText();

    /**
     * Returns the path to the icon.<p>
     * 
     * @return the path to the icon
     */
    String getIconPath();

    /**
     * Returns the link to the tool.<p>
     * 
     * @return the link
     */
    String getLink();

    /**
     * Returns the displayed name.<p>
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the evaluated map of parameters.<p>
     * 
     * @param wp the workplace context 
     * 
     * @return the parameters map
     */
    Map getParameters(CmsWorkplace wp);
    
    /**
     * Returns the tool path to install the tool in.<p>
     *
     * @return the path
     */
    String getPath();

    /**
     * Returns the relative position in the group.<p>
     *
     * @return the position
     */
    float getPosition();

    /**
     * Returns the name for the menu or navbar.<p>
     *
     * @return the short name
     */
    String getShortName();

    /**
     * Returns an optional confirmation message, displayed in a js confirm dialog.<p>
     *
     * @return the confirmation message
     */
    String getConfirmationMessage();

    /**
     * Returns the path to an optional small(16x16) icon.<p>
     * 
     * @return the path to an optional small(16x16) icon
     */
    String getSmallIconPath();

    /**
     * Returns the state of the admin tool for a given cms context.<p>
     * 
     * @param cms the cms context
     * 
     * @return <code>true</code> if enabled
     */
    boolean isEnabled(CmsObject cms);

    /**
     * Returns the visibility flag for a given cms context.<p>
     * 
     * @param cms the cms context
     * 
     * @return <code>true</code> if visible
     */
    boolean isVisible(CmsObject cms);

    /**
     * Main method that somehow setups the admin tool handler.<p>
     * 
     * @param cms the admin context (at opencms-workplace (re-)initialization time) 
     * @param resourcePath the resource path of the file/folder to use as admin tool
     *
     * @return <code>false</code> if something goes wrong
     */
    boolean setup(CmsObject cms, String resourcePath);
}
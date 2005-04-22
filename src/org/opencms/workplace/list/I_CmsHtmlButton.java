/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/Attic/I_CmsHtmlButton.java,v $
 * Date   : $Date: 2005/04/22 08:38:52 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.list;

import org.opencms.util.I_CmsNamedObject;
import org.opencms.workplace.CmsWorkplace;

/**
 * Interface for html buttons.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public interface I_CmsHtmlButton extends I_CmsNamedObject {

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
    String getHelpText();

    /**
     * Returns the id of the html component.<p>
     * 
     * @return the id
     */
    String getId();

    /**
     * Returns if enabled or disabled.<p>
     *
     * @return if enabled or disabled
     */
    boolean isEnabled();

    /**
     * Sets if enabled or disabled.<p>
     *
     * @param enabled if enabled or disabled
     */
    void setEnabled(boolean enabled);

}
/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/I_CmsJspTagContentContainer.java,v $
 * Date   : $Date: 2004/10/18 13:57:54 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jsp;

import org.opencms.xml.A_CmsXmlDocument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Provides access to a XML content document that was loaded by a parent tag.<p>
 */
public interface I_CmsJspTagContentContainer {

    /** Identified for "magiac" parameter commands. */
    String[] C_MAGIC_COMMANDS = {"uri", "filename"};

    /** The "magic" commands wrapped in a List. */
    List C_MAGIC_LIST = Collections.unmodifiableList(Arrays.asList(C_MAGIC_COMMANDS));

    /** Identifier for "magic" parameter names. */
    String C_MAGIC_PREFIX = "opencms:";

    /**
     * Returns the resource name in the VFS for the currently loaded XML content document.<p>
     *
     * @return the resource name in the VFS for the currently loaded XML content document
     */
    String getResourceName();

    /**
     * Returns the currently loaded OpenCms XML content document.<p>
     *
     * @return the currently loaded OpenCms XML content document
     */
    A_CmsXmlDocument getXmlDocument();

    /**
     * Returns the currently selected element name for the current content.<p>
     * 
     * @return the currently selected element name for the current content
     */
    String getXmlDocumentElement();

    /**
     * Returns the currently selected index for the current content.<p>
     * 
     * @return the currently selected index for the current content
     */
    int getXmlDocumentIndex();

    /**
     * Returns the currently selected locale for the current content.<p>
     * 
     * @return the currently selected locale for the current content
     */
    Locale getXmlDocumentLocale();

    /**
     * Resolves the "magic" names that can be used as values for "param" and "element" attributes.<p> 
     * 
     * If the given name is not a "magic" name, it is returned unchanged.
     * Otherwise the value of the selected "magic" command is returned.<p>
     * 
     * @param name the name to resolve
     * @return the resolved "magic" name 
     */
    String resolveMagicName(String name);
}
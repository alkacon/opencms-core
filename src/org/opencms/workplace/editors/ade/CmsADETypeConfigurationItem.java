/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADETypeConfigurationItem.java,v $
 * Date   : $Date: 2009/08/26 07:58:18 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

/**
 * A single item of the ADE file type configuration.<p>
 * 
 * A configuration item describes which file should be used as a template for new 
 * content elements, and at which location in the VFS they should be created.<p>
 * 
 * It does not contain a type, since the type is given by the type of the source file.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1.2.3 $ 
 * 
 * @since 7.6 
 */
public class CmsADETypeConfigurationItem {

    /** The destination uri pattern. */
    private final String m_destination;

    /** The source file. */
    private final String m_sourceFile;

    /** 
     * Creates a new type configuration item.<p> 
     * 
     * @param sourceFile the source file uri
     * @param destination the destination uri pattern
     **/
    public CmsADETypeConfigurationItem(String sourceFile, String destination) {

        m_destination = destination;
        m_sourceFile = sourceFile;
    }

    /**
     * Gets the destination uri pattern.<p>
     * 
     * For example <code>/demo/news/news_%(number).html</code>.<p>
     * 
     * @return the destination uri pattern
     */
    public String getDestination() {

        return m_destination;
    }

    /**
     * Gets the source file uri.<p> 
     * 
     * @return the source file uri
     */
    public String getSourceFile() {

        return m_sourceFile;
    }
}

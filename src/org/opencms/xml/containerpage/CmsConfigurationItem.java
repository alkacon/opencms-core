/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsConfigurationItem.java,v $
 * Date   : $Date: 2009/10/13 11:59:41 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.xml.containerpage;

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
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6 
 */
public class CmsConfigurationItem {

    /** The destination folder uri. */
    private final String m_folder;

    /** The file pattern. */
    private final String m_pattern;

    /** The source file. */
    private final String m_sourceFile;

    /** 
     * Creates a new type configuration item.<p> 
     * 
     * @param sourceFile the source file uri
     * @param destinationFolder the destination folder uri
     * @param pattern the file pattern
     **/
    public CmsConfigurationItem(String sourceFile, String destinationFolder, String pattern) {

        m_sourceFile = sourceFile;
        m_folder = destinationFolder;
        m_pattern = pattern;
    }

    /**
     * Gets the destination uri pattern.<p>
     * 
     * For example <code>/demo/news/news_%(number).html</code>.<p>
     * 
     * @return the destination uri pattern
     */
    public String getDestination() {

        return m_folder + m_pattern;
    }

    /**
     * Returns the destination folder uri.<p>
     *
     * @return the destination folder uri
     */
    public String getFolder() {

        return m_folder;
    }

    /**
     * Returns the file pattern.<p>
     *
     * @return the file pattern
     */
    public String getPattern() {

        return m_pattern;
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

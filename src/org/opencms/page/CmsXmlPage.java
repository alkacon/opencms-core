/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsXmlPage.java,v $
 * Date   : $Date: 2003/11/21 16:41:15 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.page;

import org.opencms.util.CmsUUID;

import com.opencms.file.CmsFile;
import com.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision: 1.1 $ $Date: 2003/11/21 16:41:15 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public class CmsXmlPage extends CmsFile {
    
    /**
     * Constructor, creates a new CmsPage Object from the given CmsResource with 
     * an empty byte array as page content.<p>
     * 
     * @param resource the base resource object to create a page from
     */
    public CmsXmlPage(CmsResource resource) {
        super(resource);
    }

    /**
     * Constructor, creates a new CmsPage Object from the given CmsFile 
     * 
     * @param file the base file object to create a page from
     */
    public CmsXmlPage(CmsFile file) {
        super(file);
        setContents(file.getContents());
    }
    
    /**
     * Constructor, creates a new CmsPage object.<p>
     *
     * @param structureId the id of this resources structure record
     * @param resourceId the id of this resources resource record
     * @param parentId the id of this resources parent folder
     * @param fileId the id of this resources content record
     * @param name the filename of this resouce
     * @param type the type of this resource
     * @param flags the flags of this resource
     * @param projectId the project id this resource was last modified in
     * @param state the state of this resource
     * @param loaderId the id for the that is used to load this recource
     * @param dateCreated the creation date of this resource
     * @param userCreated the id of the user who created this resource
     * @param dateLastModified the date of the last modification of this resource
     * @param userLastModified the id of the user who did the last modification of this resource
     * @param length the size of the file content of this resource
     * @param linkCount the count of all siblings of this resource 
     * @param content the binary content data of this page
     */
    public CmsXmlPage(
        CmsUUID structureId,
        CmsUUID resourceId,
        CmsUUID parentId,
        CmsUUID fileId,
        String name,
        int type,
        int flags,
        int projectId,
        int state,
        int loaderId,
        long dateCreated,
        CmsUUID userCreated,
        long dateLastModified,
        CmsUUID userLastModified,
        int length,
        int linkCount,
        byte[] content
    ) {
        super(structureId, resourceId, parentId, fileId, name, type, flags, projectId, state, loaderId, dateCreated, userCreated, dateLastModified, userLastModified, length, linkCount, content);
    }
    
    /**
     * Adds a new empty element with the given name and language.<p>
     *  
     * @param name name of the element, must be unique
     * @param language language of the element
     */
    public void addElement(String name, String language) {
        // TODO: implement addElement
    }
    
    /**
     * Removes an existing element with the given name and language.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     */
    public void removeElement(String name, String language) {
        // TODO: implement removeElement
    }
    
    /**
     * Returns all available elements for a given language.<p>
     * 
     * @param language language
     * @return list of available elements
     */
    public List getElementNames(String language) {
        ArrayList l = new ArrayList();
        l.add("Body");
        return l;
    }
    
    /**
     * Sets the data of an already existing element.<p>
     * The data will be enclosed as CDATA within the xml page structure.
     * When setting the element data, the content of this element will be
     * re processed automatically.
     * 
     * @param name name of the element
     * @param language language of the element
     * @param data character data (CDATA) of the element
     */
    public void setElementData(String name, String language, byte[] data) {
        // TODO: implement setElementData
    }
    
    /**
     * Returns the data of an element.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     * @return the character data of the element
     */
    public byte[] getElementData(String name, String language) {
        // TODO: implement getElementData
        return ("<html><body><h2>Element Data</h2> of " + name + " " + language + "</body></html>").getBytes(); 
    }
    
    /**
     * Returns the display content (processed data) of an element.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     * @return
     */
    public byte[] getContent(String name, String language) {
      // TODO: implement getContent
      return ("<html><body><h2>Content of " + name + " " + language + "</body></html>").getBytes() ;  
    }
}

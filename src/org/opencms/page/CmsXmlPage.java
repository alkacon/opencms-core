/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/page/Attic/CmsXmlPage.java,v $
 * Date   : $Date: 2003/11/27 16:25:12 $
 * Version: $Revision: 1.3 $
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

import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;
import java.util.List;
import java.util.Set;

/**
 * @version $Revision: 1.3 $ $Date: 2003/11/27 16:25:12 $
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 */
public abstract class CmsXmlPage extends CmsFile {
    
    /**
     * Constructor, creates a new CmsPage Object from the given CmsFile 
     * 
     * @param file the base file object to create a page from
     */
    public CmsXmlPage(CmsFile file) {       
         super(file);
         this.setContents(file.getContents());
    }

    /**
     * Returns all available elements for a given language.<p>
     * 
     * @param language language
     * @return list of available elements
     */
    public abstract List getNames(String language);
    
    
    /**
     * Returns all languages with available elements.<p>
     * 
     * @return list of languages with available elements
     */
    public abstract Set getLanguages();
    
    /**
     * Returns the display content (processed data) of an element.<p>
     * 
     * @param name name of the element
     * @param language language of the element
     * @return the display content
     */
    public abstract byte[] getContent(String name, String language);
    

    /**
     * Method to unmarshal (read) the xml contents into the implementation class.<p>
     * This method must be implemented by the implementation class.
     * 
     * @param cms the cms object
     * @return the concrete PageObject instanciated with the xml data
     * @throws CmsPageException if something goes wrong
     */
    public abstract CmsXmlPage unmarshal(CmsObject cms) 
        throws CmsPageException;
    
    /**
     * Method to marshal (write) the xml contents into the underlying CmsFile.<p>
     * This method must be implemented by the implementation class.
     * 
     * @return the underlying file updated with the data from the implementation class.
     * @throws CmsPageException if something goes wrong
     */
    public abstract CmsFile marshal()
        throws CmsPageException;
    
    /**
     * Creates a new instance of the implementation class as CmsXmlPage.<p>
     * 
     * @param cms the cms object
     * @param file the xml file
     * @return a new instance of the implementation class
     */
    public static CmsXmlPage newInstance(CmsObject cms, CmsFile file) 
        throws CmsPageException {
        
        CmsXmlPage newPage = null;
        
        try {
            Class pageImplementation = Class.forName ("org.opencms.page.CmsDefaultPage");
            newPage = (CmsXmlPage)pageImplementation.getConstructor(new Class[] {com.opencms.file.CmsFile.class})
                .newInstance(new Object[] {file});
            
            return newPage.unmarshal(cms);
    
        } catch (CmsPageException exc) {
            throw exc;
        } catch (Exception exc) {
            throw new CmsPageException("Implementation class not found", exc);
        }       
    }
}

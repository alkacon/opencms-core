/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsExampleNavigationFile.java,v $ 
 * Author : $Author: w.babachan $
 * Date   : $Date: 2000/03/24 09:39:14 $
 * Version: $Revision: 1.2 $
 * Release: $Name:  $
 *
 * Copyright (c) 2000 Mindfact interaktive medien ag.   All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from Mindfact.
 * In order to use this source code, you need written permission from Mindfact.
 * Redistribution of this source code, in modified or unmodified form,
 * is not allowed.
 *
 * MINDAFCT MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. MINDFACT SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.opencms.examples;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

/**
 * Sample content definition for a navigation template.
 * <P>
 * This class is used to access special XML data tags
 * used in navigation templates for defining the layout
 * of the navigation.
 * 
 * @author $Author: w.babachan $
 * @version $Name:  $ $Revision: 1.2 $ $Date: 2000/03/24 09:39:14 $
 * @see com.opencms.template.CmsXmlTemplateFile
 */
 public class CmsExampleNavigationFile extends CmsXmlTemplateFile {

    /**
     * Default constructor.
     * @exception CmsException
     */
    public CmsExampleNavigationFile() throws CmsException {
        super();
    }
    
	
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     * @exception CmsException
     */        
    public CmsExampleNavigationFile(A_CmsObject cms, CmsFile file) throws CmsException {
        super();
        init(cms, file);
    }

	
    /**
     * Constructor for creating a new object containing the content
     * of the given filename.
     * 
     * @param cms A_CmsObject object for accessing system resources.
     * @param filename Name of the body file that shoul be read.
     * @exception CmsException
     */        
    public CmsExampleNavigationFile(A_CmsObject cms, String filename) throws CmsException {
        super();            
        init(cms, filename);
    }
		
	
	/**
     * Gets a navigation entry.
     * The given link and title will be used to display the entry.
     * This method makes use of the special XML tags
     * <code>&lt;STARTSEQ&gt;</code>, <code>&lt;MIDDLESEQ&gt;</code> and <code>&lt;ENDSEQ&gt;</code> tag
     * inside the <code>&lt;LINK&gt;</code> tag of the template file to 
     * determine the start, middle and end HTML sequence of each section entry.
     * 
     * @param link URL that should be ued for the link.
     * @param title Title for this link
     * @exception CmsException
     */
    public String getLink(String type, String link, String title) throws CmsException {
		if (type!=null) {
			if (type.equals("plain")) {
				return getDataValue("plainlink.startseq") + link + getDataValue("plainlink.middleseq") + title + getDataValue("plainlink.endseq") + "\n";
			}
		}
		return getDataValue("framelink.startseq") + link + getDataValue("framelink.middleseq") + title + getDataValue("framelink.endseq") + "\n";
    }
	
	/**
     * Gets the target of navigation.
     *
     * @exception CmsException
     */
    public String getTarget(String type) throws CmsException {
		if (type!=null) {
			if (type.equals("plain")) {
				return getDataValue("target.parameter") + "\n";
			}
		} 
        return getDataValue("target.frame") + "\n";
    }
 }
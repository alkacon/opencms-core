/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/examples/Attic/CmsPractiseStatus.java,v $
 * Date   : $Date: 2000/03/23 15:57:40 $
 * Version: $Revision: 1.1 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.examples;

import java.util.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;

import javax.servlet.http.*;

/**
 * This class demonstrates how a customized subtemplate can be 
 * included in any other template. The subtemplate contains
 * a simple navigation for the web site.
 * <P>
 * Normally, subtemplates are includued by writing a special
 * XML tag <code>&lt;ELEMENT name="subname1"/>&gt;</code>
 * at the place in the parent template, where the subtemplate
 * should be inserted. Outside the template definition
 * (i. e. outside the <code>&lt;TEMPLATE&gt;</code> tag) 
 * there must be defined, how the content of the subtemplate
 * should be generated. This can be done by adding 
 * <blockquote>
 *     <code>&lt;ELEMENTDEF name="subname1"/&gt;><BR>
 *     &lt;CLASS&gt;com.opencms.template.CmsXmlTemplate&lt;/CLASS&gt;
 *     &lt;CLASS&gt;</code>name of the subtemplate file<code>&lt;/CLASS&gt; 
 *     &lt;/ELEMENTDEF&gt; </code>
 * </blockquote>
 * 
 * This class, however, is a customized class for
 * generating dynamic output (e.g. a dynamic navigation like in this
 * example), since such a special content can not be created by the
 * standard class <code>CmsXmlTemplateClass</code>.
 * In this special case, the value of the element definition's <code>&lt;CLASS&gt;</code> 
 * tag must be replaced by the name of the customized class.
 * <P>
 * Every template may include as many subtemplates as needed.
 * Subtemplates itself can include other subtemplates using
 * the same technique, too.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/03/23 15:57:40 $
 */
public class CmsPractiseStatus extends CmsXmlTemplate implements I_CmsConstants {
    
	
    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return true;
    }
	
	
	/**
     * Prints out the Statusbar.
     *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of this subelement.
     * @exception CmsException
     */
    public Object getStatusBar(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObject) 
            throws CmsException {
		
		String lastModified="";
		CmsFile file=cms.readFile(cms.getRequestContext().getUri());
		lastModified=" File: "+cms.getRequestContext().getUri()+"    LastModified: ";
		long milis=file.getDateLastModified();
		Date date=new Date(milis);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		lastModified=lastModified+calendar.get(Calendar.DAY_OF_MONTH)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.YEAR)+"    "+calendar.get(Calendar.HOUR_OF_DAY)+"."+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND);
		return lastModified.getBytes();
		
	}
}
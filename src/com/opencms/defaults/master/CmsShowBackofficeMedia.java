/**
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsShowBackofficeMedia.java,v $
 * Author : $Author: a.schouten $
 * Date   : $Date: 2001/11/19 15:11:08 $
 * Version: $Revision: 1.1 $
 * Release: $Name:  $
 *
 * Copyright (c) 2000 Framfab Deutschland ag.   All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from Framfab.
 * In order to use this source code, you need written permission from Framfab.
 * Redistribution of this source code, in modified or unmodified form,
 * is not allowed.
 *
 * FRAMFAB MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. FRAMFAB SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.opencms.defaults.master;

import java.util.*;
import java.io.*;
import com.opencms.template.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;
import com.opencms.defaults.*;
import com.opencms.defaults.master.*;


public class CmsShowBackofficeMedia extends CmsXmlTemplate {

  	static final String C_EMPTY_PICTURE = "empty.gif";
	static byte[] emptyGIF = new byte[0];

	/**
     * Gets the content of a defined section in a given template file and its
     * subtemplates with the given parameters.
     *
	 * @see getContent(A_CmsObject cms, String templateFile, String elementName,
	 * Hashtable parameters)
	 *
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     *
     * @return It returns an array of bytes that contains the page.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		// session will be created or fetched
		CmsRequestContext req = cms.getRequestContext();
		I_CmsSession session = (I_CmsSession) req.getSession(true);
        System.err.println("**CmsShowMedia getContent**");
		byte[] picture = new byte[0];
        try{
		//selected media content definition
        CmsMasterMedia selectedmediaCD=null;
        //try to get the medias from session
        try{
                selectedmediaCD=(CmsMasterMedia)session.getValue("selectedmediaCD");
        }catch(Exception e){
                e.printStackTrace(System.err);
        }
        System.err.println("**CmsShowMedia getContent selectedmediaCD="+selectedmediaCD);
        //no CmsMasterMedia
		if(selectedmediaCD != null){
                picture = selectedmediaCD.getMedia();
                System.err.println("**CmsShowMedia getContent picture="+picture);
                String mType = selectedmediaCD.getMimetype();
                System.err.println("**CmsShowMedia getContent mType="+mType);
                if (mType == null || mType.equals("")) {
                    mType = "application/octet-stream";
                }
                // set the mimetype ...
                req.getResponse().setContentType( mType );
                //empty media
				if(picture==null){
					picture = emptyGIF;
                    // set the mimetype ...
                    req.getResponse().setContentType("images/gif");
				}
			}else{
				picture = emptyGIF;
                 // set the mimetype ...
                req.getResponse().setContentType("images/gif");
        }
        }catch(Exception exx){
                exx.printStackTrace(System.err);
        }
		return picture;
	}


   /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {

        // First build our own cache directives.
        CmsCacheDirectives result = new CmsCacheDirectives(false);
        return result;
    }
}
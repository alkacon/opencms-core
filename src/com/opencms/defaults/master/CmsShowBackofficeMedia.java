/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsShowBackofficeMedia.java,v $
* Date   : $Date: 2005/02/18 15:18:57 $
* Version: $Revision: 1.12 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.defaults.master;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.main.CmsException;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsXmlTemplate;

import java.util.Hashtable;


/**
 * Displays binary files attached to module data.
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */
public class CmsShowBackofficeMedia extends CmsXmlTemplate {

    static final String C_EMPTY_PICTURE = "empty.gif";
    static byte[] emptyGIF = new byte[0];

    /**
     * Gets the content of a defined section in a given template file and its
     * subtemplates with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
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
        I_CmsSession session = CmsXmlTemplateLoader.getSession(req, true);
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
        //no CmsMasterMedia
        if(selectedmediaCD != null){
                picture = selectedmediaCD.getMedia();
                String mType = selectedmediaCD.getMimetype();
                if (mType == null || mType.equals("")) {
                    mType = "application/octet-stream";
                }
                // set the mimetype ...
                CmsXmlTemplateLoader.getResponse(req).setContentType( mType );
                //empty media
                if(picture==null){
                    picture = emptyGIF;
                    // set the mimetype ...
                    CmsXmlTemplateLoader.getResponse(req).setContentType("images/gif");
                }
            }else{
                picture = emptyGIF;
                 // set the mimetype ...
                CmsXmlTemplateLoader.getResponse(req).setContentType("images/gif");
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
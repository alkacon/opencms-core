package com.opencms.defaults.master;

/**
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

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.CmsXmlTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * XmlTemplate class to show media objects
 */
public class CmsShowMedia extends CmsXmlTemplate {

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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
        Hashtable parameters, String templateSelector) throws CmsException {
        // session will be created or fetched
        CmsRequestContext req = cms.getRequestContext();
        byte[] picture = new byte[0];
        CmsMasterContent cd = null;
        CmsMasterMedia media = null;
        String mType = null;
        String sId = (String) parameters.get("id");
        String sPos = (String) parameters.get("pos");
        String refCD = (String) parameters.get("cd");
        // media id to fetch exactly this media object
        String smId = (String) parameters.get("mid");
        int id = -1;
        int pos =-1;  // get first media per default ...
        int mid = -1;
        try {
            id = Integer.parseInt(sId);
            if (sPos != null) {
                pos = Integer.parseInt(sPos);
            }
            if (smId != null) {
                mid = Integer.parseInt(smId);
            }
        } catch (NumberFormatException e) {
            // ?
        }

        try {
            Class c = Class.forName(refCD);
            Object o = getContentDefinition(cms, c, new Integer(id));
            cd = (CmsMasterContent)o;
        } catch (ClassNotFoundException e) {

        }

        // enable caching for this variant ...
        Vector cosDeps = new Vector();
        cosDeps.add(cd);
        registerVariantDeps(cms, templateFile, null, null, parameters, null,
                            cosDeps, null);

        // read the media object ...
        if(cd != null){
            Vector vec = cd.getMedia();
            if (mid != -1) {
                // walk through vector until media object with the mid is found
                for (int i=0; i < vec.size(); i++) {
                    media = (CmsMasterMedia)vec.get(i);
                    if (mid == media.getId()) {
                        picture = media.getMedia();
                        mType = media.getMimetype();
                        break;
                    }
                }
            }else if (pos == -1 && vec.size() > 0) {
                // got no pos info ..
                media = (CmsMasterMedia)vec.firstElement();
                picture = media.getMedia();
                mType = media.getMimetype();
            } else {
                // got pos info ...
                for (int i=0; i< vec.size(); i++) {
                    if (((CmsMasterMedia)vec.elementAt(i)).getPosition() == pos) {
                        media = (CmsMasterMedia)vec.elementAt(i);
                        picture = media.getMedia();
                        mType = media.getMimetype();
                        break;
                    }
                }
            }
            if(picture == null){
                picture = emptyGIF;
                // set the mimetype ...
                req.getResponse().setContentType("images/gif");
            } else {
                // set mime type and filename in header
                if (mType == null || mType.equals("")) {
                    mType = "application/octet-stream";
                }
                req.getResponse().setContentType( mType );
                req.getResponse().setHeader("Content-disposition","filename=" + media.getName());
            }
        } else{
            picture = emptyGIF;
            // set the mimetype ...
            req.getResponse().setContentType("images/gif");
        }

        /*if(req.isStreaming()) {
            try {
                OutputStream os = req.getResponse().getOutputStream();
                os.write(picture);
            } catch(Exception e) {
                throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
            }
        }*/

        return picture;
    }


     /**
      * Gets the content definition class method constructor
      * @return content definition object
      */
    protected Object getContentDefinition(CmsObject cms, Class cdClass, Integer id) {
        Object o = null;
        try {
            Constructor c = cdClass.getConstructor(new Class[] { CmsObject.class, Integer.class });
            o = c.newInstance(new Object[] { cms, id });
        } catch (InvocationTargetException ite) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Invocation target exception", ite);
            }
        } catch (NoSuchMethodException nsm) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Requested method was not found", nsm);
            }
        } catch (InstantiationException e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("The reflected class is abstract", e);
            }
        } catch (Exception e) {
            if (OpenCms.getLog(this).isWarnEnabled()) {
                OpenCms.getLog(this).warn("Other exception", e);
            }
    
        }
        return o;
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

        CmsCacheDirectives ret = new CmsCacheDirectives(true, false, false, true, true);
        Vector params = new Vector();
        params.addElement("mid");
        params.addElement("id");
        params.addElement("pos");
        params.addElement("cd");
        ret.setNoCacheParameters(params);
        return ret;
    }



}
package com.opencms.defaults.master;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import com.opencms.workplace.*;
import com.opencms.defaults.*;

import java.lang.reflect.*;

import java.util.*;
import java.io.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @author
 * @version 1.0
 */

public class CmsShowMedia extends CmsXmlTemplate {

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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
        Hashtable parameters, String templateSelector) throws CmsException {
        // session will be created or fetched
        CmsRequestContext req = cms.getRequestContext();
        byte[] picture = new byte[0];
        CmsMasterContent cd = null;
        CmsMasterMedia media = null;
        String sId = (String) parameters.get("id");
        String sPos = (String) parameters.get("pos");
        String refCD = (String) parameters.get("cd");
        int id = -1;
        int pos =-1;  // get first media per default ...
        try {
            id = Integer.parseInt(sId);
            pos = Integer.parseInt(sPos);
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
            if (pos == -1 && vec.size() > 0) {
                // got no pos info ..
                media = (CmsMasterMedia)vec.firstElement();
                picture = media.getMedia();
                String mType = media.getMimetype();
                if (mType == null || mType.equals("")) {
                    mType = "application/octet-stream";
                }
                req.getResponse().setContentType( mType );
                req.getResponse().setHeader("Content-disposition","filename=" + media.getName());
            } else {
                // got pos info ...
                for (int i=0; i< vec.size(); i++) {
                    if (((CmsMasterMedia)vec.elementAt(i)).getPosition() == pos) {
                        media = (CmsMasterMedia)vec.elementAt(i);
                        picture = media.getMedia();
                        String mType = media.getMimetype();
                        if (mType == null || mType.equals("")) {
                            mType = "application/octet-stream";
                        }
                        req.getResponse().setContentType( mType );
                        req.getResponse().setHeader("Content-disposition","filename=" + media.getName());
                        break;
                    }
                }
            }
            if(picture==null){
                picture = emptyGIF;
                // set the mimetype ...
                req.getResponse().setContentType("images/gif");
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
  * @returns content definition object
  */
  protected Object getContentDefinition(CmsObject cms, Class cdClass, Integer id) {
    Object o = null;
      try {
        Constructor c = cdClass.getConstructor(new Class[] {CmsObject.class, Integer.class});
        o = c.newInstance(new Object[] {cms, id});
      } catch (InvocationTargetException ite) {
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice contentDefinitionConstructor: Invocation target exception!");
        }
      } catch (NoSuchMethodException nsm) {
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
          A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice contentDefinitionConstructor: Requested method was not found!");
        }
      } catch (InstantiationException e) {
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice contentDefinitionConstructor: the reflected class is abstract!");
        }
      } catch (Exception e) {
        if (I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
            A_OpenCms.log(C_OPENCMS_INFO, getClassName() + ": Backoffice contentDefinitionConstructor: Other exception! "+e);
        }
        if(I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && A_OpenCms.isLogging() ) {
         A_OpenCms.log(C_OPENCMS_INFO, e.getMessage() );
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
        params.addElement("id");
        params.addElement("pos");
        params.addElement("cd");
        ret.setNoCacheParameters(params);
        return ret;
    }



}
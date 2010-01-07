/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/galleries/Attic/CmsImageFormatterHelper.java,v $
 * Date   : $Date: 2010/01/07 14:13:43 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.galleries;

import org.opencms.file.CmsProperty;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.loader.CmsImageScaler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Helper bean to implement gallery image formatters.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 
 * 
 */
public class CmsImageFormatterHelper extends CmsDefaultFormatterHelper {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImageFormatterHelper.class);

    /** Json property name constants. */
    private enum JsonKeys {

        /** The active image data. */
        ACTIVEIMAGE("activeimage"),

        /** The errors. */
        ERRORS("errors"),

        /** The info. */
        INFO("info"),

        /** The path. */
        PATH("path"),

        /** The root-path. */
        LINKPATH("linkpath"),

        /** The root-path. */
        SCALEPATH("scalepath"),

        /** The width of the image. */
        WIDTH("width"),

        /** The height of the image. */
        HEIGHT("height"),

        /** The title of the image. */
        TITLE("Title");

        /** Property name. */
        private String m_name;

        /** Constructor.<p> */
        private JsonKeys(String name) {

            m_name = name;
        }

        /** 
         * Returns the name.<p>
         * 
         * @return the name
         */
        public String getName() {

            return m_name;
        }
    }

    /**
     * Constructor with page context parameters. <p>
     * 
     * @param context the page context
     * @param req the request parameter
     * @param res the response parameter
     */
    public CmsImageFormatterHelper(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
    }

    /**
     * Returns a JSON object with the specific information used for image resource type.<p>
     * 
     * <ul>
     * <li><code>linkpath</code>: link path to display the image without scaling parameter.</li>
     * <li><code>scalepath</code>: scaling parameters.</li>
     * <li><code>width</code>: image width.</li>
     * <li><code>height</code>: image height.</li>  
     * </ul>
     * 
     * @return JSON object with specific information for the image resource type 
     */
    public JSONObject getJsonForActiveImage() {

        JSONObject jsonObj = new JSONObject();
        JSONObject activeImageObj = new JSONObject();

        try {
            CmsImageScaler scaler = new CmsImageScaler(getCmsObject(), getResource());
            // 1: image width
            if (scaler.isValid()) {
                activeImageObj.put(JsonKeys.WIDTH.getName(), scaler.getWidth());
            } else {
                activeImageObj.put(JsonKeys.WIDTH.getName(), -1);
            }
            // 2: image height
            if (scaler.isValid()) {
                activeImageObj.put(JsonKeys.HEIGHT.getName(), scaler.getHeight());
            } else {
                activeImageObj.put(JsonKeys.HEIGHT.getName(), -1);
            }
            // 3: the link path to the image
            activeImageObj.put(JsonKeys.LINKPATH.getName(), getLinkPath());
            jsonObj.put(JsonKeys.ACTIVEIMAGE.getName(), activeImageObj);
        } catch (JSONException e) {
            // TODO: handle exception
        } catch (CmsException e) {
            // TODO: handle exception
        }

        return jsonObj;
    }

    /**
     * Returns the element's title property.<p>
     *  
     * @return the title property
     */
    public String getPropertyTitle() {

        CmsProperty currentProperty = null;
        try {
            currentProperty = getCmsObject().readPropertyObject(getResource(), JsonKeys.TITLE.getName(), false);
        } catch (CmsException e) {
            // TODO: improve error handling
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return "";
        }
        return currentProperty.getValue("");
    }

    /**
     * Returns the element's link path.<p>
     * 
     * @return the element's link path
     * 
     * @throws CmsException if something goes wrong
     */
    public String getLinkPath() throws CmsException {

        return link(getResource().getRootPath());
    }

    /**
     * Return the size of the given resource.<p>
     * 
     * @return the size
     * @throws CmsException, if error occurred during reading resource 
     */
    public String getSize() throws CmsException {

        return String.valueOf(getResource().getLength() / 1024);
    }

    /**
     * Returns the ending of the resource.<p>
     * 
     * @return the ending
     * @throws CmsException, if error occurred during reading resource 
     */
    public String getEnding() throws CmsException {

        String ending = "";
        int dotIndex = getResource().getName().lastIndexOf('.');
        if (dotIndex != -1) {
            ending = getResource().getName().substring(dotIndex + 1).toLowerCase();
        }
        return ending;
    }

    /**
     * Returns the image format as string like 300x200.<p>
     * 
     * @return the image format
     * @throws CmsException if resource could not be read
     */
    public String getFormat() throws CmsException {

        CmsImageScaler scaler = new CmsImageScaler(getCmsObject(), getResource());
        int width = -1;
        int height = -1;
        // 1: image width
        if (scaler.isValid()) {
            width = scaler.getWidth();
        }
        // 2: image height
        if (scaler.isValid()) {
            height = scaler.getHeight();
        }
        return String.valueOf(width + "x" + height);

    }

}

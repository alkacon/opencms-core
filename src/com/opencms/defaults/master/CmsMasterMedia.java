/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/defaults/master/Attic/CmsMasterMedia.java,v $
* Date   : $Date: 2004/02/13 13:41:45 $
* Version: $Revision: 1.8 $
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

import org.opencms.util.CmsUUID;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;

/**
 * An instance of this module describes a modulemedia entry in the database.
 * It carries a set of data to read and write.
 *
 * @author A. Schouten $
 * $Revision: 1.8 $
 * $Date: 2004/02/13 13:41:45 $
 */
public class CmsMasterMedia {

    /** Primary key for the media data */
    private int m_id;

    /** Foreign key to the media master */
    private CmsUUID m_masterId;

    /** The position of this media */
    private int m_position;

    /** The width of this media */
    private int m_width;

    /** The height of this media */
    private int m_height;

    /** The size of the media */
    private int m_size;

    /** The mimetype of the media */
    private String m_mimetype;

    /** The mediatype of the media */
    private int m_type;

    /** The title of the media */
    private String m_title;

    /** The filename of the media */
    private String m_name;

    /** A description for the media */
    private String m_description;

    /** The content of the media */
    private byte[] m_media;

    /** Constant fro media type */
    public static final int C_MEDIA_TYPE_IMAGE = 0;

    /** Constant fro media type */
    public static final int C_MEDIA_TYPE_FILE = 1;

    /** The default mimetype */
    public static final String C_DEFAULT_MIMETYPE = "application/octet-stream";

    //// constructors ////
    /**
     * Constructs a new instance with some default values.
     */
    public CmsMasterMedia() {
        m_id = I_CmsConstants.C_UNKNOWN_ID;
        m_masterId = CmsUUID.getNullUUID();
        m_position = I_CmsConstants.C_UNKNOWN_ID;
        m_width = I_CmsConstants.C_UNKNOWN_ID;
        m_height = I_CmsConstants.C_UNKNOWN_ID;
        m_size = I_CmsConstants.C_UNKNOWN_ID;
        m_mimetype = C_DEFAULT_MIMETYPE;
        m_type = I_CmsConstants.C_UNKNOWN_ID;
        m_title = "";
        m_name = "";
        m_description ="";
        m_media = new byte[0];
    }

    /**
     * Constructs a new instance with some default values.
     */
    public CmsMasterMedia(int position, int width, int height, int size,
                          String mimetype, int type, String title, String name,
                          String description, byte[] media) {
        this();
        m_position = position;
        m_width = width;
        m_height = height;
        m_size = size;
        m_mimetype = mimetype;
        m_type = type;
        m_title = title;
        m_name = name;
        m_description = description;
        m_media = media;
    }

    /**
     * Constructs a new instance with some default values.
     */
    public CmsMasterMedia(int id, CmsUUID masterId, int position, int width,
                          int height, int size, String mimetype, int type,
                          String title, String name, String description,
                          byte[] media) {
        this(position, width, height, size, mimetype, type, title, name,
             description, media);
        m_id = id;
        m_masterId = masterId;
    }

    /**
     * Convenience method to compute the mimetype with the extension of the
     * filename (e.g. mypic.gif -> image/gif)
     * @param cms - the CmsObject to get access to cms ressources.
     * @param filename - the filename to extract the extension from
     * @return the computed mimetype.
     */
    public static String computeMimetype(CmsObject cms, String filename) {
        String mimetype = null;
        // get the extension of the filename to compute mimetype
        int lastIndex = filename.lastIndexOf('.');
        if(lastIndex > 0) {
            String extension = filename.substring(lastIndex + 1);
            mimetype = (String)(cms.readMimeTypes().get(extension));
        }
        if(mimetype == null) {
            return C_DEFAULT_MIMETYPE;
        } else {
            return mimetype;
        }
    }

    //// get and set methods ////

    public int getId() {
        return m_id;
    }

    public CmsUUID getMasterId() {
        return m_masterId;
    }

    public int getPosition() {
        return m_position;
    }

    public int getWidth() {
        return m_width;
    }

    public int getHeight() {
        return m_height;
    }

    public int getSize() {
        return m_size;
    }

    public String getMimetype() {
        return m_mimetype;
    }

    public int getType() {
        return m_type;
    }

    public String getTitle() {
        return m_title;
    }

    public String getName() {
        return m_name;
    }

    public String getDescription() {
        return m_description;
    }

    public byte[] getMedia() {
        return m_media;
    }

    /**
     * Never set this on yourself!
     * It will be computed by the mastermodule
     */
    public void setId(int id) {
        m_id = id;
    }

    /**
     * Never set this on yourself!
     * It will be computed by the mastermodule
     */
    public void setMasterId(CmsUUID masterId) {
        m_masterId = masterId;
    }

    public void setPosition(int pos) {
        m_position = pos;
    }

    public void setWidth(int width) {
        m_width = width;
    }

    public void setHeight(int height) {
        m_height = height;
    }

    public void setSize(int size) {
        m_size = size;
    }

    public void setMimetype(String mimetype) {
        m_mimetype = mimetype;
    }

    public void setType(int type) {
        m_type = type;
    }

    public void setTitle(String title) {
        m_title = title;
    }

    public void setName(String name) {
        m_name = name;
    }

    public void setDescription(String desc) {
        m_description = desc;
    }

    public void setMedia(byte[] media) {
        m_media = media;
        // now set the size
        setSize(m_media.length);
    }

    /**
     * Returns a string representation of this instance.
     * Can be used for debugging.
     */
    public String toString() {
        StringBuffer retValue = new StringBuffer();
        retValue
            .append(getClass().getName() + "{")
            .append("m_id:"+m_id+",")
            .append("m_masterId:"+m_masterId+",")
            .append("m_position:"+m_position+",")
            .append("m_width:"+m_width+",")
            .append("m_height:"+m_height+",")
            .append("m_size:"+m_size+",")
            .append("m_mimetype:"+m_mimetype+",")
            .append("m_type:"+m_type+",")
            .append("m_title:"+m_title+",")
            .append("m_name:"+m_name+",")
            .append("m_id:"+m_id+",")
            .append("m_description:"+m_description+"}");
        return retValue.toString();
    }
}
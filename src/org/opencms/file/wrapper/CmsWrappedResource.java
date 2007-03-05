/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/wrapper/CmsWrappedResource.java,v $
 * Date   : $Date: 2007/03/05 14:04:57 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.commons.CmsPropertyAdvanced;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Creates "virtual" resources not existing in the vfs which are
 * based on existing resources.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.2.3 $
 * 
 * @since 6.5.6
 */
public class CmsWrappedResource {

    /** The extension to use for the property file. */
    public static final String EXTENSION_PROPERTIES = "properties";

    /** The prefix used for a shared property entry. */
    private static final String SUFFIX_PROP_INDIVIDUAL = ".i";

    /** The prefix used for a shared property entry. */
    private static final String SUFFIX_PROP_SHARED = ".s";

    /** The resource this virtual resources is based on. */
    private CmsResource m_base;

    /** Indicates if the virtual resource is a folder or not. */
    private boolean m_isFolder;

    /** The size of the content of the virtual resource. */
    private int m_length;

    /** The root path of the virtual resource. */
    private String m_rootPath;

    /** The type id of the virtual resource. */
    private int m_typeId;

    /**
     * Creates a new virtual resource.<p>
     * 
     * @param res the resource this virtual resource is based on
     */
    public CmsWrappedResource(CmsResource res) {

        m_base = res;

        m_rootPath = res.getRootPath();
        m_typeId = res.getTypeId();
        m_isFolder = res.isFolder();
        m_length = res.getLength();
    }

    /**
     * Adds a file extension to the resource name.<p>
     * 
     * @param cms the actual CmsObject
     * @param resourcename the name of the resource where to add the file extension
     * @param extension the extension to add
     * 
     * @return the resource name with the added file extension
     */
    public static String addFileExtension(CmsObject cms, String resourcename, String extension) {

        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        if (!resourcename.endsWith(extension)) {
            String name = resourcename + extension;
            int count = 0;
            while (cms.existsResource(name)) {
                count++;
                name = resourcename + "." + count + extension;
            }

            return name;
        }

        return resourcename;
    }

    /**
     * Creates a CmsFile with the individual and shared properties as content.<p>
     * 
     * @param cms the initialized CmsObject
     * @param res the resource where to read the properties from
     * @param path the full path to set for the created property file 
     * 
     * @return the created CmsFile with the individual and shared properties as the content
     * 
     * @throws CmsException if something goes wrong
     */
    public static CmsFile createPropertyFile(CmsObject cms, CmsResource res, String path) throws CmsException {

        StringBuffer content = new StringBuffer();

        // header
        content.append("# Properties for file ");
        content.append(res.getRootPath());
        content.append("\n");

        content.append("#\n");
        content.append("# ${property_name}.i : individual property\n");
        content.append("# ${property_name}.s :     shared property\n\n");

        List propertyDef = cms.readAllPropertyDefinitions();
        Map activeProperties = CmsPropertyAdvanced.getPropertyMap(cms.readPropertyObjects(res, false));

        // iterate over all possible properties for the resource
        Iterator i = propertyDef.iterator();
        while (i.hasNext()) {
            CmsPropertyDefinition currentPropertyDef = (CmsPropertyDefinition)i.next();

            String propName = currentPropertyDef.getName();
            CmsProperty currentProperty = (CmsProperty)activeProperties.get(propName);
            if (currentProperty == null) {
                currentProperty = new CmsProperty();
            }

            String individualValue = currentProperty.getStructureValue();
            String sharedValue = currentProperty.getResourceValue();

            if (individualValue == null) {
                individualValue = "";
            }

            if (sharedValue == null) {
                sharedValue = "";
            }

            individualValue = CmsStringUtil.substitute(individualValue, "\n", "\\n");
            sharedValue = CmsStringUtil.substitute(sharedValue, "\n", "\\n");
            
            content.append(propName);
            content.append(SUFFIX_PROP_INDIVIDUAL);
            content.append("=");
            content.append(individualValue);
            content.append("\n");

            content.append(propName);
            content.append(SUFFIX_PROP_SHARED);
            content.append("=");
            content.append(sharedValue);
            content.append("\n\n");
        }

        CmsWrappedResource wrap = new CmsWrappedResource(res);
        wrap.setRootPath(addFileExtension(cms, path, EXTENSION_PROPERTIES));
        wrap.setTypeId(CmsResourceTypePlain.getStaticTypeId());
        wrap.setFolder(false);

        CmsFile ret = wrap.getFile();
        try {

            ret.setContents(content.toString().getBytes(CmsEncoder.ENCODING_UTF_8));
        } catch (UnsupportedEncodingException e) {

            // this will never happen since UTF-8 is always supported
            ret.setContents(content.toString().getBytes());
        }

        return ret;
    }

    /**
     * Removes an added file extension from the resource name.<p>
     * 
     * <ul>
     * <li>If there is only one extension, nothing will be removed.</li>
     * <li>If there are two extensions, the last one will be removed.</li>
     * <li>If there are more than two extensions the last one will be removed and
     * if then the last extension is a number, the extension with the number
     * will be removed too.</li>
     * </ul>
     * 
     * @param cms the initialized CmsObject
     * @param resourcename the resource name to remove the file extension from
     * @param extension the extension to remove
     * 
     * @return the resource name without the removed file extension
     */
    public static String removeFileExtension(CmsObject cms, String resourcename, String extension) {

        if (resourcename.equals("")) {
            resourcename = "/";
        }

        // get the filename without the path
        String name = CmsResource.getName(resourcename);

        String[] tokens = name.split("\\.");
        String suffix = null;

        // check if there is more than one extension
        if (tokens.length > 2) {

            // check if last extension is "jsp"
            if (extension.equalsIgnoreCase(tokens[tokens.length - 1])) {

                suffix = "." + extension;

                // check if there is another extension with a numeric index 
                if (tokens.length > 3) {

                    try {
                        int index = Integer.valueOf(tokens[tokens.length - 2]).intValue();

                        suffix = "." + index + suffix;
                    } catch (NumberFormatException ex) {
                        // noop
                    }
                }
            }
        } else if (tokens.length == 2) {

            // there is only one extension!! 
            // only remove the last extension, if the resource without the extension exists
            // and the extension fits
            if ((cms.existsResource(CmsResource.getFolderPath(resourcename) + tokens[0]))
                && (extension.equals(tokens[1]))) {
                suffix = "." + tokens[1];
            }
        }

        if (suffix != null) {

            String path = resourcename.substring(0, resourcename.length() - suffix.length());
            return path;
        }

        return resourcename;
    }

    /**
     * Set the content (formatted as a property file) as properties to the resource.<p>
     * 
     * @see org.opencms.file.wrapper.CmsWrappedResource#createPropertyFile(CmsObject, CmsResource, String)
     * 
     * @param cms the initialized CmsObject
     * @param resourcename the name of the resource where to set the properties
     * @param content the properties to set (formatted as a property file)
     * 
     * @throws CmsException if something goes wrong
     */
    public static void writePropertyFile(CmsObject cms, String resourcename, byte[] content) throws CmsException {

        Properties properties = new Properties();
        try {
            String props = CmsEncoder.createString(content, CmsEncoder.ENCODING_UTF_8);
            props = CmsEncoder.encodeJavaEntities(props, CmsEncoder.ENCODING_US_ASCII);
            byte[] modContent = props.getBytes(CmsEncoder.ENCODING_US_ASCII);

            properties.load(new ByteArrayInputStream(modContent));

            List propList = new ArrayList();
            Iterator iter = properties.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                String value = (String)properties.get(key);

                if (key.endsWith(SUFFIX_PROP_SHARED)) {
                    propList.add(new CmsProperty(
                        key.substring(0, key.length() - SUFFIX_PROP_SHARED.length()),
                        null,
                        value));
                } else if (key.endsWith(SUFFIX_PROP_INDIVIDUAL)) {
                    propList.add(new CmsProperty(
                        key.substring(0, key.length() - SUFFIX_PROP_INDIVIDUAL.length()),
                        value,
                        null));
                }
            }

            cms.writePropertyObjects(resourcename, propList);
        } catch (IOException e) {
            // noop
        }

    }

    /**
     * Adds the UTF-8 marker add the beginning of the byte array.<p>
     * 
     * @param content the byte array where to add the UTF-8 marker
     * 
     * @return the byte with the added UTF-8 marker at the beginning
     */
    public static byte[] addUtf8Marker(byte[] content) {

        if ((content != null)
            && (content.length >= 3)
            && (content[0] == (byte)0xEF)
            && (content[1] == (byte)0xBB)
            && (content[2] == (byte)0xBF)) {
            return content;
        }

        if (content == null) {
            content = new byte[0];
        }
        
        byte[] bom = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] ret = new byte[bom.length + content.length];

        System.arraycopy(bom, 0, ret, 0, bom.length);
        System.arraycopy(content, 0, ret, bom.length, content.length);

        return ret;
    }

    /**
     * Returns the virtual resource as a file.<p>
     * 
     * @return the virtual resource as a file
     */
    public CmsFile getFile() {

        if (m_base instanceof CmsFile) {
            CmsFile file = (CmsFile)m_base;

            return new CmsFile(
                file.getStructureId(),
                file.getResourceId(),
                file.getContentId(),
                m_rootPath,
                m_typeId,
                file.getFlags(),
                file.getProjectLastModified(),
                file.getState(),
                file.getDateCreated(),
                file.getUserCreated(),
                file.getDateLastModified(),
                file.getUserLastModified(),
                file.getDateReleased(),
                file.getDateExpired(),
                file.getSiblingCount(),
                file.getLength(),
                file.getContents());
        }

        return new CmsFile(getResource());
    }

    /**
     * Returns the length.<p>
     *
     * @return the length
     */
    public int getLength() {

        return m_length;
    }

    /**
     * Returns the virtual resource.<p>
     * 
     * @return the virtual resource
     */
    public CmsResource getResource() {

        return new CmsResource(
            m_base.getStructureId(),
            m_base.getResourceId(),
            m_rootPath,
            m_typeId,
            m_isFolder,
            m_base.getFlags(),
            m_base.getProjectLastModified(),
            m_base.getState(),
            m_base.getDateCreated(),
            m_base.getUserCreated(),
            m_base.getDateLastModified(),
            m_base.getUserLastModified(),
            m_base.getDateReleased(),
            m_base.getDateExpired(),
            m_base.getSiblingCount(),
            m_length);
    }

    /**
     * Returns the rootPath.<p>
     *
     * @return the rootPath
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the typeId.<p>
     *
     * @return the typeId
     */
    public int getTypeId() {

        return m_typeId;
    }

    /**
     * Returns the isFolder.<p>
     *
     * @return the isFolder
     */
    public boolean isFolder() {

        return m_isFolder;
    }

    /**
     * Sets the isFolder.<p>
     *
     * @param isFolder the isFolder to set
     */
    public void setFolder(boolean isFolder) {

        m_isFolder = isFolder;

        if ((m_isFolder) && (!m_rootPath.endsWith("/"))) {
            m_rootPath += "/";
        }
    }

    /**
     * Sets the length.<p>
     *
     * @param length the length to set
     */
    public void setLength(int length) {

        m_length = length;
    }

    /**
     * Sets the rootPath.<p>
     *
     * @param rootPath the rootPath to set
     */
    public void setRootPath(String rootPath) {

        m_rootPath = rootPath;
    }

    /**
     * Sets the typeId.<p>
     *
     * @param typeId the typeId to set
     */
    public void setTypeId(int typeId) {

        m_typeId = typeId;
    }
}

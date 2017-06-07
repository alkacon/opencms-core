/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class with several methods used by different implementations of the
 * interface {@link I_CmsResourceWrapper}.<p>
 *
 * It provides methods to add or remove file extensions to resources, to handle
 * creating and writing property files and to add the byte order mask to UTF-8
 * byte contents.<p>
 *
 * @since 6.2.4
 */
public final class CmsResourceWrapperUtils {

    /** The extension to use for the property file. */
    public static final String EXTENSION_PROPERTIES = "properties";

    /** Property name used for reading / changing the resource type. */
    public static final String PROPERTY_RESOURCE_TYPE = "resourceType";

    /** The prefix used for a shared property entry. */
    public static final String SUFFIX_PROP_INDIVIDUAL = ".i";

    /** The prefix used for a shared property entry. */
    public static final String SUFFIX_PROP_SHARED = ".s";

    /** The UTF-8 bytes to add to the beginning of text contents. */
    public static final byte[] UTF8_MARKER = new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF};

    /** Pattern to use for incoming strings before storing in OpenCms. */
    private static final Pattern PATTERN_UNESCAPE = Pattern.compile("\\\\([^ntru\n\r])");

    /**
     * Hide utility class constructor.<p>
     */
    private CmsResourceWrapperUtils() {

        // noop
    }

    /**
     * Adds a file extension to the resource name.<p>
     *
     * If the file with the new extension already exists, an index count will be
     * added before the final extension.<p>
     *
     * For example: <code>index.html.1.jsp</code>.<p>
     *
     * @see #removeFileExtension(CmsObject, String, String)
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
     * Adds the UTF-8 marker add the beginning of the byte array.<p>
     *
     * @param content the byte array where to add the UTF-8 marker
     *
     * @return the byte with the added UTF-8 marker at the beginning
     */
    public static byte[] addUtf8Marker(byte[] content) {

        if ((content != null)
            && (content.length >= 3)
            && (content[0] == UTF8_MARKER[0])
            && (content[1] == UTF8_MARKER[1])
            && (content[2] == UTF8_MARKER[2])) {
            return content;
        }

        if (content == null) {
            content = new byte[0];
        }

        byte[] ret = new byte[UTF8_MARKER.length + content.length];

        System.arraycopy(UTF8_MARKER, 0, ret, 0, UTF8_MARKER.length);
        System.arraycopy(content, 0, ret, UTF8_MARKER.length, content.length);

        return ret;
    }

    /**
     * Creates a virtual CmsFile with the individual and shared properties as content.<p>
     *
     * For example looks like this:<br/>
     * Title.i=The title of the resource set as individual property<br/>
     * Title.s=The title of the resource set as shared property<br/>
     *
     * @see #writePropertyFile(CmsObject, String, byte[])
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
        content.append("# Properties for resource ");
        content.append(res.getRootPath());
        content.append("\n");

        content.append("#\n");
        content.append("# ${property_name}.i : individual property\n");
        content.append("# ${property_name}.s :     shared property\n\n");

        List<CmsPropertyDefinition> propertyDef = cms.readAllPropertyDefinitions();
        Map<String, CmsProperty> activeProperties = CmsProperty.getPropertyMap(cms.readPropertyObjects(res, false));

        String resourceType = OpenCms.getResourceManager().getResourceType(res).getTypeName();
        content.append("resourceType=");
        content.append(resourceType);
        content.append("\n\n");

        // iterate over all possible properties for the resource
        Iterator<CmsPropertyDefinition> i = propertyDef.iterator();
        while (i.hasNext()) {
            CmsPropertyDefinition currentPropertyDef = i.next();

            String propName = currentPropertyDef.getName();
            CmsProperty currentProperty = activeProperties.get(propName);
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

            individualValue = escapeString(individualValue);
            sharedValue = escapeString(sharedValue);

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
        int plainId = OpenCms.getResourceManager().getResourceType(
            CmsResourceTypePlain.getStaticTypeName()).getTypeId();
        wrap.setTypeId(plainId);
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
     * @see #addFileExtension(CmsObject, String, String)
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
     * Removes the UTF-8 marker from the beginning of the byte array.<p>
     *
     * @param content the byte array where to remove the UTF-8 marker
     *
     * @return the byte with the removed UTF-8 marker at the beginning
     */
    public static byte[] removeUtf8Marker(byte[] content) {

        if ((content != null)
            && (content.length >= 3)
            && (content[0] == UTF8_MARKER[0])
            && (content[1] == UTF8_MARKER[1])
            && (content[2] == UTF8_MARKER[2])) {

            byte[] ret = new byte[content.length - UTF8_MARKER.length];
            System.arraycopy(content, 3, ret, 0, content.length - UTF8_MARKER.length);

            return ret;
        }

        return content;
    }

    /**
     * Takes the content which should be formatted as a property file and set them
     * as properties to the resource.<p>
     *
     * @see #createPropertyFile(CmsObject, CmsResource, String)
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
            props = unescapeString(props);
            props = CmsEncoder.encodeJavaEntities(props, CmsEncoder.ENCODING_ISO_8859_1);
            byte[] modContent = props.getBytes(CmsEncoder.ENCODING_ISO_8859_1);

            properties.load(new ByteArrayInputStream(modContent));

            List<CmsProperty> propList = new ArrayList<CmsProperty>();
            Iterator<Map.Entry<Object, Object>> it = properties.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Object> e = it.next();
                String key = (String)e.getKey();
                String value = (String)e.getValue();

                if (key.endsWith(SUFFIX_PROP_SHARED)) {
                    propList.add(
                        new CmsProperty(key.substring(0, key.length() - SUFFIX_PROP_SHARED.length()), null, value));
                } else if (key.endsWith(SUFFIX_PROP_INDIVIDUAL)) {
                    propList.add(
                        new CmsProperty(key.substring(0, key.length() - SUFFIX_PROP_INDIVIDUAL.length()), value, null));
                }
            }

            cms.writePropertyObjects(resourcename, propList);
            String newType = properties.getProperty(PROPERTY_RESOURCE_TYPE);
            if (newType != null) {
                newType = newType.trim();
                if (OpenCms.getResourceManager().hasResourceType(newType)) {
                    I_CmsResourceType newTypeObj = OpenCms.getResourceManager().getResourceType(newType);
                    cms.chtype(resourcename, newTypeObj.getTypeId());
                }
            }
        } catch (IOException e) {
            // noop
        }

    }

    /**
     * Escapes the value of a property in OpenCms to be displayed
     * correctly in a property file.<p>
     *
     * Mainly handles all escaping sequences that start with a backslash.<p>
     *
     * @see #unescapeString(String)
     *
     * @param value the value with the string to be escaped
     *
     * @return the escaped string
     */
    private static String escapeString(String value) {

        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("\n", "\\n");
        substitutions.put("\t", "\\t");
        substitutions.put("\r", "\\r");

        return CmsStringUtil.substitute(value, substitutions);
    }

    /**
     * Unescapes the value of a property in a property file to
     * be saved correctly in OpenCms.<p>
     *
     * Mainly handles all escaping sequences that start with a backslash.<p>
     *
     * @see #escapeString(String)
     *
     * @param value the value taken form the property file
     *
     * @return the unescaped string value
     */
    private static String unescapeString(String value) {

        Matcher matcher = PATTERN_UNESCAPE.matcher(value);
        return matcher.replaceAll("\\\\\\\\$1");
    }

}

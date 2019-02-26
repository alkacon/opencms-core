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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * The new resource entry dialog which displays the possible "new actions" for the current user.<p>
 *
 * It handles the creation of "simple" resource types like plain or JSP resources.<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsNewResource {

    /** The value for the resource name form action. */
    public static final int ACTION_NEWFORM = 100;

    /** The value for the resource name form submission action. */
    public static final int ACTION_SUBMITFORM = 110;

    /** Constant for the "Next" button in the build button methods. */
    public static final int BUTTON_NEXT = 20;

    /** The default suffix. */
    public static final String DEFAULT_SUFFIX = ".html";

    /** Delimiter for property values, e.g. for available resource types or template sites. */
    public static final char DELIM_PROPERTYVALUES = ',';

    /** The name for the advanced resource form action. */
    public static final String DIALOG_ADVANCED = "advanced";

    /** The name for the resource form action. */
    public static final String DIALOG_NEWFORM = "newform";

    /** The name for the resource form submission action. */
    public static final String DIALOG_SUBMITFORM = "submitform";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "newresource";

    /** List column id constant. */
    public static final String LIST_COLUMN_URI = "nrcu";

    /** Request parameter name for the append html suffix checkbox. */
    public static final String PARAM_APPENDSUFFIXHTML = "appendsuffixhtml";

    /** Request parameter name for the current folder name. */
    public static final String PARAM_CURRENTFOLDER = "currentfolder";

    /** Request parameter name for the new form uri. */
    public static final String PARAM_NEWFORMURI = "newformuri";

    /** Request parameter name for the new resource edit properties flag. */
    public static final String PARAM_NEWRESOURCEEDITPROPS = "newresourceeditprops";

    /** Request parameter name for the new resource type. */
    public static final String PARAM_NEWRESOURCETYPE = "newresourcetype";

    /** Request parameter name for the new resource uri. */
    public static final String PARAM_NEWRESOURCEURI = "newresourceuri";

    /** Session attribute to store advanced mode. */
    public static final String SESSION_ATTR_ADVANCED = "ocms_newres_adv";

    /** Session attribute to store current page. */
    public static final String SESSION_ATTR_PAGE = "ocms_newres_page";

    /** The property value for available resource to reset behaviour to default dialog. */
    public static final String VALUE_DEFAULT = "default";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResource.class);

    /**
     * Returns the value for the Title property from the given resource name.<p>
     *
     * @param name the name of the resource
     *
     * @return the value for the Title property from the given resource name
     */
    public static String computeNewTitleProperty(String name) {

        String title = name;
        int lastDot = title.lastIndexOf('.');
        // check the mime type for the file extension
        if ((lastDot > 0) && (lastDot < (title.length() - 1))) {
            // remove suffix for Title and NavPos property
            title = title.substring(0, lastDot);
        }
        return title;
    }

    /**
     * A factory to return handlers to create new resources.<p>
     *
     * @param type the resource type name to get a new resource handler for, as specified in the explorer type settings
     * @param defaultClassName a default handler class name, to be used if the handler class specified in the explorer type settings cannot be found
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * @return a new instance of the handler class
     * @throws CmsRuntimeException if something goes wrong
     */
    public static Object getNewResourceHandler(
        String type,
        String defaultClassName,
        PageContext context,
        HttpServletRequest req,
        HttpServletResponse res)
    throws CmsRuntimeException {

        if (CmsStringUtil.isEmpty(type)) {
            // it's not possible to hardwire the resource type name on the JSP for Xml content types
            type = req.getParameter(PARAM_NEWRESOURCETYPE);
        }

        String className = defaultClassName;
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_NEW_RES_HANDLER_CLASS_NOT_FOUND_1, className), e);
            }
            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_NEW_RES_HANDLER_CLASS_NOT_FOUND_1, className));
        }

        Object handler = null;
        try {
            Constructor<?> constructor = clazz.getConstructor(
                new Class[] {PageContext.class, HttpServletRequest.class, HttpServletResponse.class});
            handler = constructor.newInstance(new Object[] {context, req, res});
        } catch (Exception e) {

            throw new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_NEW_RES_CONSTRUCTOR_NOT_FOUND_1, className));
        }

        return handler;
    }

    /**
     * Creates a single property object and sets the value individual or shared depending on the OpenCms settings.<p>
     *
     * @param name the name of the property
     * @param value the value to set
     * @return an initialized property object
     */
    protected static CmsProperty createPropertyObject(String name, String value) {

        CmsProperty prop = new CmsProperty();
        prop.setAutoCreatePropertyDefinition(true);
        prop.setName(name);
        if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
            prop.setValue(value, CmsProperty.TYPE_INDIVIDUAL);
        } else {
            prop.setValue(value, CmsProperty.TYPE_SHARED);
        }
        return prop;
    }

    /**
     * Returns the properties to create automatically with the new VFS resource.<p>
     *
     * If configured, the Title and Navigation properties are set on resource creation.<p>
     *
     * @param cms the initialized CmsObject
     * @param resourceName the full resource name
     * @param resTypeName the name of the resource type
     * @param title the Title String to use for the property values
     * @return the List of initialized property objects
     */
    protected static List<CmsProperty> createResourceProperties(
        CmsObject cms,
        String resourceName,
        String resTypeName,
        String title) {

        // create property values
        List<CmsProperty> properties = new ArrayList<CmsProperty>(3);

        // get explorer type settings for the resource type
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resTypeName);
        if (settings.isAutoSetTitle()) {

            // add the Title property
            properties.add(createPropertyObject(CmsPropertyDefinition.PROPERTY_TITLE, title));
        }
        if (settings.isAutoSetNavigation()) {

            // add the NavText property
            properties.add(createPropertyObject(CmsPropertyDefinition.PROPERTY_NAVTEXT, title));

            // calculate the new navigation position for the resource
            List<CmsJspNavElement> navList = new CmsJspNavBuilder(cms).getNavigationForFolder(resourceName);
            float navPos = 1;
            if (navList.size() > 0) {
                CmsJspNavElement nav = navList.get(navList.size() - 1);
                navPos = nav.getNavPosition() + 1;
            }
            // add the NavPos property
            properties.add(createPropertyObject(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(navPos)));
        }
        return properties;
    }
}
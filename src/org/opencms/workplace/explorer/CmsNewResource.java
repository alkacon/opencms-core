/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResource.java,v $
 * Date   : $Date: 2005/07/06 12:45:07 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.explorer;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.jsp.CmsJspNavBuilder;
import org.opencms.jsp.CmsJspNavElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPropertyAdvanced;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
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
 * @author Andreas Zahner 
 * @author Armen Markarian 
 * 
 * @version $Revision: 1.20 $ 
 * 
 * @since 6.0.0 
 */
public class CmsNewResource extends CmsDialog {

    /** The value for the resource name form action. */
    public static final int ACTION_NEWFORM = 100;

    /** The value for the resource name form submission action. */
    public static final int ACTION_SUBMITFORM = 110;

    /** Constant for the "Next" button in the build button methods. */
    public static final int BUTTON_NEXT = 20;

    /** The name for the resource form action. */
    public static final String DIALOG_NEWFORM = "newform";

    /** The name for the resource form submission action. */
    public static final String DIALOG_SUBMITFORM = "submitform";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "newresource";

    /** Request parameter name for the current folder name. */
    public static final String PARAM_CURRENTFOLDER = "currentfolder";

    /** Request parameter name for the new resource edit properties flag. */
    public static final String PARAM_NEWRESOURCEEDITPROPS = "newresourceeditprops";

    /** Request parameter name for the new resource type. */
    public static final String PARAM_NEWRESOURCETYPE = "newresourcetype";

    /** Request parameter name for the new resource uri. */
    public static final String PARAM_NEWRESOURCEURI = "newresourceuri";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResource.class);

    private String m_page;
    private String m_paramCurrentFolder;
    private String m_paramNewResourceEditProps;
    private String m_paramNewResourceType;
    private String m_paramNewResourceUri;
    private String m_paramPage;

    /** a boolean flag that indicates if the create resource operation was successfull or not. */
    private boolean m_resourceCreated;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResource(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResource(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
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
        HttpServletResponse res) throws CmsRuntimeException {

        if (CmsStringUtil.isEmpty(type)) {
            // it's not possible to hardwire the resource type name on the JSP for Xml content types
            type = req.getParameter(PARAM_NEWRESOURCETYPE);
        }

        String className = null;
        CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type);

        if (CmsStringUtil.isNotEmpty(settings.getNewResourceHandlerClassName())) {
            className = settings.getNewResourceHandlerClassName();
        } else {
            className = defaultClassName;
        }

        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {

            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().key(Messages.ERR_NEW_RES_HANDLER_CLASS_NOT_FOUND_1, className), e);
            }
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_NEW_RES_HANDLER_CLASS_NOT_FOUND_1,
                className));
        }

        Object handler = null;
        try {
            Constructor constructor = clazz.getConstructor(new Class[] {
                PageContext.class,
                HttpServletRequest.class,
                HttpServletResponse.class});
            handler = constructor.newInstance(new Object[] {context, req, res});
        } catch (Exception e) {

            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_NEW_RES_CONSTRUCTOR_NOT_FOUND_1,
                className));
        }

        return handler;
    }

    /**
     * Creates the resource using the specified resource name and the newresourcetype parameter.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    public void actionCreateResource() throws JspException {

        try {
            // calculate the new resource Title property value
            String title = computeNewTitleProperty();
            // create the full resource name
            String fullResourceName = computeFullResourceName();
            // create the Title and Navigation properties if configured
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(getParamNewResourceType());
            List properties = createResourceProperties(fullResourceName, resType.getTypeName(), title);
            // create the resource            
            getCms().createResource(fullResourceName, resType.getTypeId(), null, properties);
            setParamResource(fullResourceName);

            setResourceCreated(true);
        } catch (Throwable e) {
            // error creating file, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Forwards to the property dialog if the resourceeditprops parameter is true.<p>
     * 
     * If the parameter is not true, the dialog will be closed.<p>
     * 
     * @throws IOException if forwarding to the property dialog fails
     * @throws ServletException if forwarding to the property dialog fails
     * @throws JspException if an inclusion fails
     */
    public void actionEditProperties() throws IOException, JspException, ServletException {

        boolean editProps = Boolean.valueOf(getParamNewResourceEditProps()).booleanValue();
        if (editProps) {
            // edit properties checkbox checked, forward to property dialog
            Map params = new HashMap();
            params.put(PARAM_RESOURCE, getParamResource());
            params.put(CmsPropertyAdvanced.PARAM_DIALOGMODE, CmsPropertyAdvanced.MODE_WIZARD);
            sendForward(CmsPropertyAdvanced.URI_PROPERTY_DIALOG_HANDLER, params);
        } else {
            // edit properties not checked, close the dialog
            actionCloseDialog();
        }
    }

    /**
     * Forwards to the next page of the new resource wizard after selecting the new resource type.<p>
     * 
     * @throws IOException if forwarding fails
     * @throws ServletException if forwarding fails
     */
    public void actionSelect() throws IOException, ServletException {

        String nextUri = PATH_DIALOGS + getParamNewResourceUri();
        if (nextUri.indexOf("initial=true") == -1) {
            setParamAction(DIALOG_NEWFORM);
            String[] uri = CmsRequestUtil.splitUri(nextUri);
            Map params = CmsRequestUtil.createParameterMap(uri[2]);
            params.putAll(paramsAsParameterMap());
            sendForward(uri[0], params);
        } else {
            try {
                getJsp().include(nextUri);
            } catch (JspException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
            }
        }
    }

    /**
     * Builds the html for the list of possible new resources.<p>
     *  
     * @param attributes optional attributes for the radio input tags
     * @return the html for the list of possible new resources
     */
    public String buildNewList(String attributes) {

        StringBuffer result = new StringBuffer(1024);
        Iterator i = OpenCms.getWorkplaceManager().getExplorerTypeSettings().iterator();
        result.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");

        while (i.hasNext()) {
            CmsExplorerTypeSettings currSettings = (CmsExplorerTypeSettings)i.next();

            // check for the "new resource" page
            if (m_page == null) {
                if (CmsStringUtil.isNotEmpty(currSettings.getNewResourcePage())) {
                    continue;
                }
            } else if (!m_page.equals(currSettings.getNewResourcePage())) {
                continue;
            }

            if (CmsStringUtil.isEmpty(currSettings.getNewResourceUri())) {
                // no new resource URI specified for the current settings, dont't show the type
                continue;
            }

            // check permissions for the type
            CmsPermissionSet permissions;
            try {
                // get permissions of the current user
                permissions = currSettings.getAccess().getAccessControlList().getPermissions(
                    getSettings().getUser(),
                    getCms().getGroupsOfUser(getSettings().getUser().getName()));
            } catch (CmsException e) {
                // error reading the groups of the current user
                permissions = currSettings.getAccess().getAccessControlList().getPermissions(getSettings().getUser());
                LOG.error(Messages.get().key(
                    Messages.LOG_READ_GROUPS_OF_USER_FAILED_1,
                    getSettings().getUser().getName()));
            }
            if (permissions.getPermissionString().indexOf("+c") == -1) {
                // the type has no permission for the current user to be created, don't show the type
                continue;
            }

            result.append("<tr>\n");
            result.append("\t<td><input type=\"radio\" name=\"");
            result.append(PARAM_NEWRESOURCEURI);
            result.append("\"");
            result.append(" value=\"" + CmsEncoder.encode(currSettings.getNewResourceUri()) + "\"");
            if (attributes != null && !"".equals(attributes)) {
                result.append(" " + attributes);
            }
            result.append("></td>\n");
            result.append("\t<td><img src=\""
                + getSkinUri()
                + "filetypes/"
                + currSettings.getIcon()
                + "\" border=\"0\" title=\""
                + key(currSettings.getKey())
                + "\"></td>\n");
            result.append("\t<td>" + key(currSettings.getKey()) + "</td>\n");
            result.append("</tr>\n");

        }
        result.append("</table>\n");

        return result.toString();
    }

    /**
     * Returns the value for the Title property from the given resource name.<p>
     * 
     * Additionally translates the new resource name according to the file translation rules.<p>
     * 
     * @return the value for the Title property from the given resource name
     */
    public String computeNewTitleProperty() {

        String title = getParamResource();
        int lastDot = title.lastIndexOf('.');
        // check the mime type for the file extension 
        if ((lastDot > 0) && (lastDot < (title.length() - 1))) {
            // remove suffix for Title and NavPos property
            title = title.substring(0, lastDot);
        }
        // translate the resource name to a valid OpenCms VFS file name
        String resName = CmsResource.getName(getParamResource().replace('\\', '/'));
        setParamResource(getCms().getRequestContext().getFileTranslator().translateResource(resName));
        return title;
    }

    /**
     * Builds a button row with an "next" and a "cancel" button.<p>
     * 
     * @param nextAttrs optional attributes for the next button
     * @param cancelAttrs optional attributes for the cancel button
     * @return the button row 
     */
    public String dialogButtonsNextCancel(String nextAttrs, String cancelAttrs) {

        return dialogButtons(new int[] {BUTTON_NEXT, BUTTON_CANCEL}, new String[] {nextAttrs, cancelAttrs});
    }

    /**
     * Returns the current folder set by the http request.<p>
     *  
     * If the request parameter value is null/empty then returns the default computed folder.<p>
     *
     * @return the current folder set by the request param or the computed current folder
     */
    public String getParamCurrentFolder() {

        if (CmsStringUtil.isEmpty(m_paramCurrentFolder)) {
            return computeCurrentFolder();
        }

        return m_paramCurrentFolder;
    }

    /**
     * Returns the new resource edit properties flag parameter.<p>
     * 
     * @return the new resource edit properties flag parameter
     */
    public String getParamNewResourceEditProps() {

        return m_paramNewResourceEditProps;
    }

    /**
     * Returns the new resource type parameter.<p>
     * 
     * @return the new resource type parameter
     */
    public String getParamNewResourceType() {

        return m_paramNewResourceType;
    }

    /**
     * Returns the new resource URI parameter.<p>
     * 
     * @return the new resource URI parameter
     */
    public String getParamNewResourceUri() {

        return m_paramNewResourceUri;
    }

    /**
     * Returns the paramPage.<p>
     *
     * @return the paramPage
     */
    public String getParamPage() {

        return m_paramPage;
    }

    /**
     * Returns true if the resource is created successfully; otherwise false.<p>
     * 
     * @return true if the resource is created successfully; otherwise false
     */
    public boolean isResourceCreated() {

        return m_resourceCreated;
    }

    /**
     * Sets the current folder.<p>
     *
     * @param paramCurrentFolder the current folder to set
     */
    public void setParamCurrentFolder(String paramCurrentFolder) {

        m_paramCurrentFolder = paramCurrentFolder;
    }

    /**
     * Sets the new resource edit properties flag parameter.<p>
     * 
     * @param newResourceEditProps the new resource edit properties flag parameter
     */
    public void setParamNewResourceEditProps(String newResourceEditProps) {

        m_paramNewResourceEditProps = newResourceEditProps;
    }

    /**
     * Sets the new resource type parameter.<p>
     * 
     * @param newResourceType the new resource type parameter
     */
    public void setParamNewResourceType(String newResourceType) {

        m_paramNewResourceType = newResourceType;
    }

    /**
     * Sets the new resource URI parameter.<p>
     * 
     * @param newResourceUri the new resource URI parameter
     */
    public void setParamNewResourceUri(String newResourceUri) {

        m_paramNewResourceUri = newResourceUri;
    }

    /**
     * Sets the paramPage.<p>
     *
     * @param paramPage the paramPage to set
     */
    public void setParamPage(String paramPage) {

        m_paramPage = paramPage;
    }

    /**
     * Sets the boolean flag successfullyCreated.<p>
     *   
     * @param successfullyCreated a boolean flag that indicates if the create resource operation was successfull or not
     */
    public void setResourceCreated(boolean successfullyCreated) {

        m_resourceCreated = successfullyCreated;
    }

    /**
     * Returns the full path of the current workplace folder.<p>
     * 
     * @return the full path of the current workplace folder
     */
    protected String computeCurrentFolder() {

        String currentFolder = getSettings().getExplorerResource();
        if (currentFolder == null) {
            // set current folder to root folder
            try {
                currentFolder = getCms().getSitePath(getCms().readFolder("/", CmsResourceFilter.IGNORE_EXPIRATION));
            } catch (CmsException e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(e);
                }
                currentFolder = "/";
            }
        }
        if (!currentFolder.endsWith("/")) {
            // add folder separator to currentFolder
            currentFolder += "/";
        }
        return currentFolder;
    }

    /**
     * Appends the full path to the new resource name given in the resource parameter.<p>
     * 
     * @return the full path of the new resource
     */
    protected String computeFullResourceName() {

        // return the full resource name
        // get the current folder
        String currentFolder = getParamCurrentFolder();
        if (CmsStringUtil.isEmpty(currentFolder)) {
            currentFolder = computeCurrentFolder();
        }
        return currentFolder + getParamResource();
    }

    /**
     * Creates a single property object and sets the value individual or shared depending on the OpenCms settings.<p>
     * 
     * @param name the name of the property
     * @param value the value to set
     * @return an initialized property object 
     */
    protected CmsProperty createPropertyObject(String name, String value) {

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
     * @param resourceName the full resource name
     * @param resTypeName the name of the resource type
     * @param title the Title String to use for the property values
     * @return the List of initialized property objects
     */
    protected List createResourceProperties(String resourceName, String resTypeName, String title) {

        // create property values
        List properties = new ArrayList(3);
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
            List navList = CmsJspNavBuilder.getNavigationForFolder(getCms(), resourceName);
            float navPos = 1;
            if (navList.size() > 0) {
                CmsJspNavElement nav = (CmsJspNavElement)navList.get(navList.size() - 1);
                navPos = nav.getNavPosition() + 1;
            }
            // add the NavPos property
            properties.add(createPropertyObject(CmsPropertyDefinition.PROPERTY_NAVPOS, String.valueOf(navPos)));
        }
        return properties;
    }

    /**
     * @see org.opencms.workplace.CmsDialog#dialogButtonsHtml(java.lang.StringBuffer, int, java.lang.String)
     */
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {

        attribute = appendDelimiter(attribute);

        switch (button) {
            case BUTTON_NEXT:
                result.append("<input name=\"next\" type=\"submit\" value=\"");
                result.append(key("button.nextscreen"));
                result.append("\" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            default:
                super.dialogButtonsHtml(result, button, attribute);
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        if (CmsStringUtil.isNotEmpty(getParamPage())) {
            m_page = getParamPage();
            setParamAction(null);
            setParamNewResourceUri(null);
            setParamPage(null);
        }

        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else if (DIALOG_SUBMITFORM.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM);
        } else if (DIALOG_NEWFORM.equals(getParamAction())) {
            setAction(ACTION_NEWFORM);
            setParamTitle(key("title.new" + getParamNewResourceType()));
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog     
            setParamTitle(key("title.new"));
        }
    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResource.java,v $
 * Date   : $Date: 2006/01/09 16:11:37 $
 * Version: $Revision: 1.21.2.7 $
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
import org.opencms.security.CmsRole;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPropertyAdvanced;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
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
 * @version $Revision: 1.21.2.7 $ 
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

    /** Request parameter name for the append html suffix checkbox. */
    public static final String PARAM_APPENDSUFFIXHTML = "appendsuffixhtml";

    /** Request parameter name for the current folder name. */
    public static final String PARAM_CURRENTFOLDER = "currentfolder";

    /** Request parameter name for the new resource edit properties flag. */
    public static final String PARAM_NEWRESOURCEEDITPROPS = "newresourceeditprops";

    /** Request parameter name for the new resource type. */
    public static final String PARAM_NEWRESOURCETYPE = "newresourcetype";

    /** Request parameter name for the new resource uri. */
    public static final String PARAM_NEWRESOURCEURI = "newresourceuri";

    /** The property value for available resource to reset behaviour to default dialog. */
    public static final String VALUE_DEFAULT = "default";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResource.class);

    private String m_availableResTypes;
    private boolean m_limitedRestypes;
    private String m_page;

    private String m_paramAppendSuffixHtml;
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

        String nextUri = getParamNewResourceUri();
        if (!nextUri.startsWith("/")) {
            // no absolute path given, use default dialog path
            nextUri = PATH_DIALOGS + nextUri;
        }

        setParamAction(DIALOG_NEWFORM);
        CmsUriSplitter splitter = new CmsUriSplitter(nextUri);
        Map params = CmsRequestUtil.createParameterMap(splitter.getQuery());
        params.putAll(paramsAsParameterMap());
        sendForward(splitter.getPrefix(), params);
    }

    /**
     * Builds the html for the list of possible new resources.<p>
     *  
     * @param attributes optional attributes for the radio input tags
     * @return the html for the list of possible new resources
     */
    public String buildNewList(String attributes) {

        StringBuffer result = new StringBuffer(1024);
        result.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\">");

        Iterator i;
        if (m_limitedRestypes) {
            // available resource types limited, create list iterator of given limited types
            List newResTypes = CmsStringUtil.splitAsList(m_availableResTypes, DELIM_PROPERTYVALUES);
            Iterator k = newResTypes.iterator();
            List settings = new ArrayList(newResTypes.size());
            while (k.hasNext()) {
                String resType = (String)k.next();
                // get settings for resource type
                CmsExplorerTypeSettings set = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resType);
                if (set != null) {
                    // add found setting to available resource types
                    settings.add(set);
                }
            }
            // sort explorer type settings by their order
            Collections.sort(settings);
            i = settings.iterator();
        } else {
            // create list iterator from all configured resource types
            i = OpenCms.getWorkplaceManager().getExplorerTypeSettings().iterator();
        }

        while (i.hasNext()) {
            CmsExplorerTypeSettings settings = (CmsExplorerTypeSettings)i.next();

            if (!m_limitedRestypes) {
                // check for the "new resource" page
                if (m_page == null) {
                    if (CmsStringUtil.isNotEmpty(settings.getNewResourcePage())) {
                        continue;
                    }
                } else if (!m_page.equals(settings.getNewResourcePage())) {
                    continue;
                }
            }

            if (CmsStringUtil.isEmpty(settings.getNewResourceUri())) {
                // no new resource URI specified for the current settings, dont't show the type
                continue;
            }

            // check permissions for the type
            CmsPermissionSet permissions = settings.getAccess().getPermissions(getCms());
            if (!permissions.requiresControlPermission()) {
                // the type has no permission for the current user to be created, don't show the type
                continue;
            }

            result.append("<tr>\n");
            result.append("\t<td><input type=\"radio\" name=\"");
            result.append(PARAM_NEWRESOURCEURI);
            result.append("\"");
            result.append(" value=\"" + CmsEncoder.encode(settings.getNewResourceUri()) + "\"");
            if (CmsStringUtil.isNotEmpty(attributes)) {
                result.append(" " + attributes);
            }
            result.append("></td>\n");
            result.append("\t<td><img src=\""
                + getSkinUri()
                + "filetypes/"
                + settings.getIcon()
                + "\" border=\"0\" title=\""
                + key(settings.getKey())
                + "\"></td>\n");
            result.append("\t<td>" + key(settings.getKey()) + "</td>\n");
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
     * Builds a button row with an optional "advanced", "next" and a "cancel" button.<p>
     * 
     * @param advancedAttrs optional attributes for the advanced button
     * @param nextAttrs optional attributes for the next button
     * @param cancelAttrs optional attributes for the cancel button
     * @return the button row 
     */
    public String dialogButtonsAdvancedNextCancel(String advancedAttrs, String nextAttrs, String cancelAttrs) {

        if (m_limitedRestypes && getCms().hasRole(CmsRole.VFS_MANAGER)) {
            return dialogButtons(new int[] {BUTTON_ADVANCED, BUTTON_NEXT, BUTTON_CANCEL}, new String[] {
                advancedAttrs,
                nextAttrs,
                cancelAttrs});
        } else {
            return dialogButtons(new int[] {BUTTON_NEXT, BUTTON_CANCEL}, new String[] {nextAttrs, cancelAttrs});
        }
    }

    /**
     * Builds a button row with a "next" and a "cancel" button.<p>
     * 
     * @param nextAttrs optional attributes for the next button
     * @param cancelAttrs optional attributes for the cancel button
     * @return the button row 
     */
    public String dialogButtonsNextCancel(String nextAttrs, String cancelAttrs) {

        return dialogButtons(new int[] {BUTTON_NEXT, BUTTON_CANCEL}, new String[] {nextAttrs, cancelAttrs});
    }

    /**
     * Returns the parameter to check if a ".html" suffix should be added to the new resource name.<p>
     * 
     * @return the parameter to check if a ".html" suffix should be added to the new resource name
     */
    public String getParamAppendSuffixHtml() {

        return m_paramAppendSuffixHtml;
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
     * Sets the parameter to check if a ".html" suffix should be added to the new resource name.<p>
     * 
     * @param paramAppendSuffixHtml the parameter to check if a ".html" suffix should be added to the new resource name
     */
    public void setParamAppendSuffixHtml(String paramAppendSuffixHtml) {

        m_paramAppendSuffixHtml = paramAppendSuffixHtml;
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
     * Appends a ".html" suffix to the given resource name if no suffix is present and the append suffix option is checked.<p>
     * 
     * @param resourceName the resource name to check
     * @param forceSuffix if true, the suffix is appended overriding the append suffix option
     * @return the reource name with ".html" suffix if no suffix was present and the append suffix option is checked
     */
    protected String appendSuffixHtml(String resourceName, boolean forceSuffix) {

        // append ".html" suffix to new file if not present
        if ((forceSuffix || Boolean.valueOf(getParamAppendSuffixHtml()).booleanValue())
            && resourceName.indexOf('.') < 0) {
            resourceName += ".html";
        }
        return resourceName;
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
            setParamTitle(key(Messages.GUI_NEWRESOURCE_0));

            if (!DIALOG_ADVANCED.equals(getParamAction()) && CmsStringUtil.isEmpty(m_page)) {
                // check for presence of property limiting the new resource types to create
                String newResTypesProperty = "";
                try {
                    newResTypesProperty = getCms().readPropertyObject(
                        getParamCurrentFolder(),
                        CmsPropertyDefinition.PROPERTY_RESTYPES_AVAILABLE,
                        true).getValue();
                } catch (CmsException e) {
                    // ignore this exception, this is a minor issue
                }
                if (CmsStringUtil.isNotEmpty(newResTypesProperty) && !newResTypesProperty.equals(VALUE_DEFAULT)) {
                    m_limitedRestypes = true;
                    m_availableResTypes = newResTypesProperty;
                }
            }
        }
    }
}
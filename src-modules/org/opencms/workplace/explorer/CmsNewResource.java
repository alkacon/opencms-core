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
import org.opencms.security.CmsRole;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUriSplitter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.commons.CmsPropertyAdvanced;
import org.opencms.workplace.list.A_CmsListResourceTypeDialog;
import org.opencms.workplace.list.CmsListColumnDefinition;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemSelectionCustomAction;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.CmsListOrderEnum;

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
 * @since 6.0.0
 */
public class CmsNewResource extends A_CmsListResourceTypeDialog {

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

    /** The available resource types. */
    private String m_availableResTypes;

    /** The limited resource types. */
    private boolean m_limitedRestypes;

    /** The page. */
    private String m_page;

    /** The append suffix parameter. */
    private String m_paramAppendSuffixHtml;

    /** The current folder parameter. */
    private String m_paramCurrentFolder;

    /** The dialog mode parameter. */
    private String m_paramDialogMode;

    /** The new form URI parameter. */
    private String m_paramNewFormUri;

    /** The new resource edit properties parameter. */
    private String m_paramNewResourceEditProps;

    /** The new resource type parameter. */
    private String m_paramNewResourceType;

    /** The new resource URI parameter. */
    private String m_paramNewResourceUri;

    /** The page parameter. */
    private String m_paramPage;

    /** a boolean flag that indicates if the create resource operation was successfull or not. */
    private boolean m_resourceCreated;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNewResource(CmsJspActionElement jsp) {

        super(
            jsp,
            A_CmsListResourceTypeDialog.LIST_ID,
            Messages.get().container(Messages.GUI_NEWRESOURCE_SELECT_TYPE_0),
            null,
            CmsListOrderEnum.ORDER_ASCENDING,
            null);
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

    /**
     * Used to close the current JSP dialog.<p>
     *
     * This method overwrites the close dialog method in the super class,
     * because in case a new folder was created before, after this dialog the tree view has to be refreshed.<p>
     *
     * It tries to include the URI stored in the workplace settings.
     * This URI is determined by the frame name, which has to be set
     * in the framename parameter.<p>
     *
     * @throws JspException if including an element fails
     */
    @Override
    public void actionCloseDialog() throws JspException {

        if (isCreateIndexMode()) {

            // set the current explorer resource to the new created folder
            String updateFolder = CmsResource.getParentFolder(getSettings().getExplorerResource());
            getSettings().setExplorerResource(updateFolder, getCms());
            List<String> folderList = new ArrayList<String>(1);
            if (updateFolder != null) {
                folderList.add(updateFolder);
            }
            getJsp().getRequest().setAttribute(REQUEST_ATTRIBUTE_RELOADTREE, folderList);
        }
        super.actionCloseDialog();
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
            List<CmsProperty> properties = createResourceProperties(fullResourceName, resType.getTypeName(), title);

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
     * @see org.opencms.workplace.list.A_CmsListDialog#actionDialog()
     */
    @Override
    public void actionDialog() throws JspException, ServletException, IOException {

        super.actionDialog();

        switch (getAction()) {
            case ACTION_SUBMITFORM:
                actionCreateResource();
                if (isResourceCreated()) {
                    actionEditProperties(); // redirects only if the edit properties option was checked
                }
                break;
            case ACTION_OK:
                actionSelect();
                break;
            case ACTION_NEWFORM:
                setParamAction(DIALOG_SUBMITFORM);
                break;
            case ACTION_DEFAULT:
            default:
                setParamAction(DIALOG_OK);
                break;
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
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(PARAM_RESOURCE, new String[] {getParamResource()});
            if (isCreateIndexMode()) {
                params.put(
                    CmsPropertyAdvanced.PARAM_DIALOGMODE,
                    new String[] {CmsPropertyAdvanced.MODE_WIZARD_INDEXCREATED});
            } else {
                params.put(CmsPropertyAdvanced.PARAM_DIALOGMODE, new String[] {CmsPropertyAdvanced.MODE_WIZARD});
            }
            forwardEditProperties(params);
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
        Map<String, String[]> params = CmsRequestUtil.createParameterMap(splitter.getQuery());
        params.putAll(paramsAsParameterMap());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_page)
            && (params.containsKey(PARAM_PAGE) && m_page.equals(params.get(PARAM_PAGE)))) {
            // remove page parameter if found and equal to currently shown page
            params.remove(PARAM_PAGE);
        }
        if (!params.containsKey(PARAM_PAGE)) {
            clearSession();
        }
        sendForward(splitter.getPrefix(), params);
    }

    /**
     * Returns the value for the Title property from the given resource name.<p>
     *
     * @return the value for the Title property from the given resource name
     */
    public String computeNewTitleProperty() {

        return computeNewTitleProperty(getParamResource());
    }

    /**
     * Builds a default button row with a continue and cancel button.<p>
     *
     * Override this to have special buttons for your dialog.<p>
     *
     * @return the button row
     */
    @Override
    public String dialogButtons() {

        return dialogButtonsAdvancedNextCancel(
            " onclick=\"submitAdvanced();\"",
            "id=\"nextButton\" disabled=\"disabled\"",
            null);
    }

    /**
     * Builds a button row with an optional "next", "advanced" and a "cancel" button.<p>
     *
     * @param advancedAttrs optional attributes for the advanced button
     * @param nextAttrs optional attributes for the next button
     * @param cancelAttrs optional attributes for the cancel button
     * @return the button row
     */
    public String dialogButtonsAdvancedNextCancel(String advancedAttrs, String nextAttrs, String cancelAttrs) {

        if (m_limitedRestypes && OpenCms.getRoleManager().hasRole(getCms(), CmsRole.VFS_MANAGER)) {
            return dialogButtons(
                new int[] {BUTTON_NEXT, BUTTON_ADVANCED, BUTTON_CANCEL},
                new String[] {nextAttrs, advancedAttrs, cancelAttrs});
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
     * Returns the value of the dialogmode parameter,
     * or null if this parameter was not provided.<p>
     *
     * The dialogmode parameter stores the different modes of the property dialog,
     * e.g. for displaying other buttons in the new resource wizard.<p>
     *
     * @return the value of the usetempfileproject parameter
     */
    public String getParamDialogmode() {

        return m_paramDialogMode;
    }

    /**
     * Returns the new form URI parameter.<p>
     *
     * @return the new form URI parameter
     */
    public String getParamNewFormUri() {

        return m_paramNewFormUri;
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
    @Override
    public String getParamPage() {

        return m_paramPage;
    }

    /**
     * Returns the suffix of the first default file. If nothing is defined, then ".html" is returned.<p>
     *
     * @return the suffix of the first default file otherwise ".html"
     */
    public String getSuffixHtml() {

        String result = "";
        if (OpenCms.getDefaultFiles().size() > 0) {
            String defaultfile = OpenCms.getDefaultFiles().get(0);
            int index = defaultfile.indexOf('.');
            if (index >= 0) {
                result = defaultfile.substring(index, defaultfile.length());
            }
        }
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(result)) {
            result = DEFAULT_SUFFIX;
        }
        return result;
    }

    /**
     * Returns true if the current mode is: create an index page in a newly created folder.<p>
     *
     * @return true if we are in wizard mode to create an index page, otherwise false
     */
    public boolean isCreateIndexMode() {

        return CmsPropertyAdvanced.MODE_WIZARD_CREATEINDEX.equals(getParamDialogmode());
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
     * Overrides the super implementation to avoid problems with double reqource input fields.<p>
     *
     * @see org.opencms.workplace.CmsWorkplace#paramsAsHidden()
     */
    @Override
    public String paramsAsHidden() {

        String resourceName = getParamResource();

        // remove resource parameter from hidden params to avoid problems with double input fields in form
        setParamResource(null);
        String params = super.paramsAsHidden();

        // set resource parameter to stored value
        setParamResource(resourceName);
        return params;
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
     * Sets the value of the dialogmode parameter.<p>
     *
     * @param value the value to set
     */
    public void setParamDialogmode(String value) {

        m_paramDialogMode = value;
    }

    /**
     * Sets the new form URI parameter.<p>
     *
     * @param paramNewFormUri the new form URI parameter to set
     */
    public void setParamNewFormUri(String paramNewFormUri) {

        m_paramNewFormUri = paramNewFormUri;
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
    @Override
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
     * Appends the suffix of the first default file suffix to the given resource name if no suffix is present and the
     * append suffix option is checked.<p>
     *
     * @param resourceName the resource name to check
     * @param forceSuffix if true, the suffix is appended overriding the append suffix option
     * @return the resource name with the default file suffix if no suffix was present and the append suffix option is checked
     */
    protected String appendSuffixHtml(String resourceName, boolean forceSuffix) {

        // append the default suffix (".html") to new file if no standard type was provided
        if ((forceSuffix || Boolean.valueOf(getParamAppendSuffixHtml()).booleanValue())) {

            if (OpenCms.getResourceManager().getMimeType(resourceName, null, null) == null) {
                resourceName += getSuffixHtml();
            }
        }
        return resourceName;
    }

    /**
     * Clears the session attributes storing the page and dialog mode.<p>
     */
    protected void clearSession() {

        getJsp().getRequest().getSession(true).removeAttribute(SESSION_ATTR_ADVANCED);
        getJsp().getRequest().getSession(true).removeAttribute(SESSION_ATTR_PAGE);
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
        String translatedFilename = OpenCms.getResourceManager().getFileTranslator().translateResource(
            getParamResource());
        return currentFolder + translatedFilename;
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
    protected List<CmsProperty> createResourceProperties(String resourceName, String resTypeName, String title) {

        return createResourceProperties(getCms(), resourceName, resTypeName, title);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(256);
        result.append(super.customHtmlStart());

        result.append("<script type='text/javascript'>\n");

        result.append("function enableButton() {\n");
        result.append("\tvar theButton = document.getElementById(\"nextButton\");\n");
        result.append("\tif (theButton.disabled == true) {\n");
        result.append("\t\ttheButton.disabled = false;\n");
        result.append("\t}\n");
        result.append("}\n");

        result.append("function submitAdvanced() {\n");
        result.append(
            "\tdocument.forms[\"" + getList().getId() + "-form\"].action.value = \"" + DIALOG_ADVANCED + "\";\n");
        result.append("\tdocument.forms[\"" + getList().getId() + "-form\"].submit();\n");
        result.append("}\n");

        result.append("</script>");

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsDialog#dialogButtonsHtml(java.lang.StringBuffer, int, java.lang.String)
     */
    @Override
    protected void dialogButtonsHtml(StringBuffer result, int button, String attribute) {

        attribute = appendDelimiter(attribute);

        switch (button) {
            case BUTTON_NEXT:
                result.append("<input name=\"next\" type=\"submit\" value=\"");
                result.append(key(Messages.GUI_BUTTON_NEXTSCREEN_0));
                result.append("\" class=\"dialogbutton\"");
                result.append(attribute);
                result.append(">\n");
                break;
            default:
                super.dialogButtonsHtml(result, button, attribute);
        }
    }

    /**
     * Forwards to a page for editing properties.<p>
     *
     * @param parameters the request parameters which should be passed to the page which we forward to
     * @throws IOException if something goes wrong
     * @throws ServletException if something goes wrong
     */
    protected void forwardEditProperties(Map<String, String[]> parameters) throws IOException, ServletException {

        sendForward(CmsPropertyAdvanced.URI_PROPERTY_DIALOG_HANDLER, parameters);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#getListItems()
     */
    @Override
    protected List<CmsListItem> getListItems() {

        List<CmsListItem> ret = new ArrayList<CmsListItem>();

        Iterator<CmsExplorerTypeSettings> i;
        if (m_limitedRestypes) {

            // available resource types limited, create list iterator of given limited types
            List<String> newResTypes;
            if (m_availableResTypes.indexOf(DELIM_PROPERTYVALUES) > -1) {
                newResTypes = CmsStringUtil.splitAsList(m_availableResTypes, DELIM_PROPERTYVALUES, true);
            } else {
                newResTypes = CmsStringUtil.splitAsList(m_availableResTypes, CmsProperty.VALUE_LIST_DELIMITER, true);
            }
            Iterator<String> k = newResTypes.iterator();
            List<CmsExplorerTypeSettings> settings = new ArrayList<CmsExplorerTypeSettings>(newResTypes.size());
            while (k.hasNext()) {
                String resType = k.next();

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

        CmsResource resource = null;
        try {
            resource = getCms().readResource(getParamCurrentFolder(), CmsResourceFilter.ONLY_VISIBLE);
        } catch (CmsException e) {
            // ignore
        }

        while (i.hasNext()) {
            CmsExplorerTypeSettings settings = i.next();

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
            if (!settings.isEditable(getCms(), resource)
                || !settings.getAccess().getPermissions(getCms(), resource).requiresControlPermission()) {

                // the type has no permission for the current user to be created, don't show the type
                continue;
            }

            // add found setting to list
            CmsListItem item = getList().newItem(settings.getName());
            item.set(LIST_COLUMN_NAME, key(settings.getKey()));
            item.set(
                LIST_COLUMN_ICON,
                "<img src=\""
                    + getSkinUri()
                    + CmsWorkplace.RES_PATH_FILETYPES
                    + settings.getIcon()
                    + "\" style=\"width: 16px; height: 16px;\" />");
            item.set(LIST_COLUMN_URI, CmsEncoder.encode(settings.getNewResourceUri()));
            ret.add(item);
        }

        return ret;
    }

    /**
     * Returns the title to use for the dialog.<p>
     *
     * Checks if a custom title key is given in the explorer type settings
     * and otherwise returns the default title from
     * {@link CmsWorkplaceMessages#getNewResourceTitle(org.opencms.workplace.CmsWorkplace, String)}.<p>
     *
     * @return the title to use for the dialog
     */
    protected String getTitle() {

        String title = null;
        CmsExplorerTypeSettings set = OpenCms.getWorkplaceManager().getExplorerTypeSetting(getParamNewResourceType());
        if ((set != null) && (CmsStringUtil.isNotEmptyOrWhitespaceOnly(set.getTitleKey()))) {
            title = getMessages().key(set.getTitleKey(), true);
        }

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(title)) {
            if (CmsStringUtil.isNotEmpty(getParamNewResourceType())) {
                title = CmsWorkplaceMessages.getNewResourceTitle(this, getParamNewResourceType());
            } else {
                title = getParamTitle();
            }
        }

        return title;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // set default action
        setAction(ACTION_DEFAULT);

        super.initWorkplaceRequestValues(settings, request);

        // build title for new resource dialog
        setParamTitle(key(Messages.GUI_NEWRESOURCE_0));

        String page = (String)request.getSession(true).getAttribute(SESSION_ATTR_PAGE);

        if (CmsStringUtil.isNotEmpty(getParamPage())) {
            m_page = getParamPage();
            request.getSession(true).setAttribute(SESSION_ATTR_PAGE, m_page);
            setParamPage(null);

            if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamNewFormUri())
                || (getParamNewResourceUri().indexOf("?" + PARAM_PAGE) != -1)) {
                setParamNewFormUri(getParamNewResourceUri());
                setParamNewResourceUri(null);
            }

            if ((DIALOG_NEWFORM.equals(getParamAction())) || (LIST_INDEPENDENT_ACTION.equals(getParamAction()))) {
                setParamAction(null);
            }
        } else if (page != null) {
            m_page = page;
        }

        // set the action for the JSP switch
        if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
            //clearSession();
        } else if (DIALOG_SUBMITFORM.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM);
            clearSession();
        } else if (DIALOG_NEWFORM.equals(getParamAction())) {
            clearSession();

            // set resource name if we are in new folder wizard mode
            setInitialResourceName();

            setAction(ACTION_NEWFORM);

            // set the correct title
            setParamTitle(getTitle());

        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
            clearSession();
        }

        // get session attribute storing if we are in advanced mode
        String advAttr = (String)request.getSession(true).getAttribute(SESSION_ATTR_ADVANCED);

        if (DIALOG_ADVANCED.equals(getParamAction()) || (advAttr != null)) {
            // advanced mode to display all possible resource types
            if (advAttr == null) {
                // set attribute that we are in advanced mode
                request.getSession(true).setAttribute(SESSION_ATTR_ADVANCED, "true");
                // remove eventual resource URI parameter value to avoid errors if a type was selected
                setParamNewResourceUri(null);
            }
        } else {

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

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setColumns(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setColumns(CmsListMetadata metadata) {

        super.setColumns(metadata);

        // add column uri
        CmsListColumnDefinition uriCol = new CmsListColumnDefinition(LIST_COLUMN_URI);
        uriCol.setName(Messages.get().container(Messages.GUI_NEWRESOURCE_LIST_COLS_URI_0));
        uriCol.setVisible(false);
        metadata.addColumn(uriCol);

        CmsListItemSelectionCustomAction action = (CmsListItemSelectionCustomAction)metadata.getColumnDefinition(
            LIST_COLUMN_SELECT).getDirectAction(LIST_ACTION_SEL);
        action.setFieldName(PARAM_NEWRESOURCEURI);
        action.setColumn(LIST_COLUMN_URI);
        action.setAttributes(" onclick=\"enableButton();\"");
    }

    /**
     * Sets the initial resource name of the new page.<p>
     *
     * This is used for the "new" wizard after creating a new folder followed
     * by the "create index file" procedure.<p>
     */
    protected void setInitialResourceName() {

        if (isCreateIndexMode()) {

            // creation of an index file in a new folder, use default file name
            String defaultFile = "";
            try {
                defaultFile = OpenCms.getDefaultFiles().get(0);
            } catch (IndexOutOfBoundsException e) {

                // list is empty, ignore
            }
            if (CmsStringUtil.isEmpty(defaultFile)) {

                // make sure that the default file name is not empty
                defaultFile = "index.html";
            }
            setParamResource(defaultFile);
        } else {
            setParamResource("");
        }
    }
}
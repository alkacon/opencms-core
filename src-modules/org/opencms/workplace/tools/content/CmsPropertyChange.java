/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/CmsPropertyChange.java,v $
 * Date   : $Date: 2005/06/23 15:39:30 $
 * Version: $Revision: 1.11 $
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

package org.opencms.workplace.tools.content;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides methods for the change property values dialog.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/administration/properties/change/index.html
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsPropertyChange extends CmsDialog {

    /** Value for the action: show result. */
    public static final int ACTION_SHOWRESULT = 100;

    /** Request parameter value for the action: show result. */
    public static final String DIALOG_SHOWRESULT = "showresult";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "propertychange";
    /** Request parameter name for the property name. */
    public static final String PARAM_NEWVALUE = "newvalue";
    /** Request parameter name for the property name. */
    public static final String PARAM_OLDVALUE = "oldvalue";
    /** Request parameter name for the property name. */
    public static final String PARAM_PROPERTYNAME = "propertyname";
    /** Request parameter name for the property name. */
    public static final String PARAM_RECURSIVE = "recursive";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPropertyChange.class);

    private List m_changedResources;
    
    /** The error message. */
    private String m_errorMessage;

    private String m_paramNewValue;
    private String m_paramOldValue;
    private String m_paramPropertyName;
    private String m_paramRecursive;
    
    private boolean m_validationErrors;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPropertyChange(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPropertyChange(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds the html for the property definition select box.<p>
     * 
     * @param cms the CmsObject
     * @param selectValue the localized value for the "Please select" option
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @param selectedValue the value that is currently selected
     * @return the html for the property definition select box
     */
    public static String buildSelectProperty(CmsObject cms, String selectValue, String attributes, String selectedValue) {

        List propertyDef = new ArrayList();
        try {
            // get all property definitions
            propertyDef = cms.readAllPropertyDefinitions();
        } catch (CmsException e) {
            // should usually never happen
            if (LOG.isInfoEnabled()) {
                LOG.info(e);
            }
        }

        int propertyCount = propertyDef.size();
        List options = new ArrayList(propertyCount + 1);
        List values = new ArrayList(propertyCount + 1);
        options.add(CmsEncoder.escapeXml(selectValue));
        values.add("");
        int selectedIndex = 0;
        int count = 1;

        for (int i = 0; i < propertyCount; i++) {
            // loop property definitions and get definition name
            CmsPropertyDefinition currDef = (CmsPropertyDefinition)propertyDef.get(i);
            if (currDef.getName().equals(selectedValue)) {
                selectedIndex = count;
            }
            options.add(CmsEncoder.escapeXml(currDef.getName()));
            values.add(CmsEncoder.escapeXml(currDef.getName()));
            count += 1;
        }

        CmsDialog wp = new CmsDialog(null);
        return wp.buildSelect(attributes, options, values, selectedIndex);
    }

    /**
     * Changes the property values on the specified resources.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionChange() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        try {
            boolean recursive = Boolean.valueOf(getParamRecursive()).booleanValue();
            if (performChangeOperation(recursive)) {
                // if no exception is caused and "true" is returned change property operation was successful
                setAction(ACTION_SHOWRESULT);
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(C_FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // error while changing property values, show error dialog
            includeErrorpage(this, e);

        }
    }

    /**
     * Builds the html for the result list of resources where the property was changed.<p>
     * 
     * @return the html for the result list
     */
    public String buildResultList() {

        StringBuffer result = new StringBuffer(16);
        if (getChangedResources() != null && getChangedResources().size() > 0) {
            // at least one resource property value has been changed, show list
            for (int i = 0; i < getChangedResources().size(); i++) {
                CmsResource res = (CmsResource)getChangedResources().get(i);
                String resName = getCms().getSitePath(res);
                result.append(resName);
                result.append("<br>\n");
            }
        } else {
            // nothing was changed, show message
            result.append(key("input.propertychange.result.none"));
        }
        return result.toString();
    }

    /**
     * Builds the html for the property definition select box.<p>
     * 
     * @param attributes optional attributes for the &lt;select&gt; tag
     * @return the html for the property definition select box
     */
    public String buildSelectProperty(String attributes) {

        return buildSelectProperty(getCms(), key("please.select"), attributes, getParamPropertyName());
    }
    
    /**
     * Returns the error message.<p>
     *
     * @return the error message
     */
    public String getErrorMessage() {

        if (CmsStringUtil.isEmpty(m_errorMessage)) {
            return "";
        }

        return m_errorMessage;
    }

    /**
     * Returns the value of the newvalue parameter.<p>
     *
     * @return the value of the newvalue parameter
     */
    public String getParamNewValue() {

        return m_paramNewValue;
    }

    /**
     * Returns the value of the oldvalue parametere.<p>
     *
     * @return the value of the oldvalue parameter
     */
    public String getParamOldValue() {

        return m_paramOldValue;
    }

    /**
     * Returns the value of the propertyname parameter.<p>
     *
     * @return the value of the propertyname parameter
     */
    public String getParamPropertyName() {

        return m_paramPropertyName;
    }

    /**
     * Returns the value of the recursive parameter.<p>
     *
     * @return the value of the recursive parameter
     */
    public String getParamRecursive() {

        return m_paramRecursive;
    }

    /**
     * Returns the height for the result list of changed resources.<p>
     * 
     * @return the height for the result list of changed resources
     */
    public String getResultListHeight() {

        if (getChangedResources() != null && getChangedResources().size() > 0) {
            int height = getChangedResources().size() * 14;
            if (height > 300) {
                height = 300;
            }
            return "" + height;
        } else {
            return "14";
        }
    }
    
    /**
     * Returns if validation errors were found.<p>
     * 
     * @return true if validation errors were found, otherwise false
     */
    public boolean hasValidationErrors() {

        return m_validationErrors;
    }

    /**
     * Sets the value of the newvalue parameter.<p>
     *
     * @param paramNewValue the value of the newvalue parameter
     */
    public void setParamNewValue(String paramNewValue) {

        m_paramNewValue = paramNewValue;
    }

    /**
     * Sets the value of the oldvalue parameter.<p>
     *
     * @param paramOldValue the value of the oldvalue parameter
     */
    public void setParamOldValue(String paramOldValue) {

        m_paramOldValue = paramOldValue;
    }

    /**
     * Sets the value of the propertyname parameter.<p>
     *
     * @param paramPropertyName the value of the propertyname parameter
     */
    public void setParamPropertyName(String paramPropertyName) {

        m_paramPropertyName = paramPropertyName;
    }

    /**
     * Sets the value of the recursive parameter.<p>
     *
     * @param paramRecursive the value of the recursive parameter
     */
    public void setParamRecursive(String paramRecursive) {

        m_paramRecursive = paramRecursive;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            if (validateParameters()) {
                // all parameters are valid, proceed
                setAction(ACTION_OK);
            } else {
                // validation error(s), redisplay form
                setAction(ACTION_DEFAULT);
            }
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for change property value dialog     
            setParamTitle(key("title.propertychange"));
        }
    }
    
    /**
     * Sets the error message.<p>
     *
     * @param errorMessage the error message to set
     */
    protected void setErrorMessage(String errorMessage) {

        m_errorMessage = errorMessage;
    }
    
    /**
     * Sets the validation error flag.<p>
     * 
     * @param validationErrors the validation error flag, true if validation errors were found
     */
    protected void setValidationErrors(boolean validationErrors) {

        m_validationErrors = validationErrors;
    }

    /**
     * Returns the changed resources that were affected by the property change action.<p>
     *
     * @return the changed resources that were affected by the property change action
     */
    private List getChangedResources() {

        return m_changedResources;
    }

    /**
     * Performs the main property change value operation on the resource property.<p>
     * 
     * @param recursive true, if the property value has to be changed recursively, otherwise false
     * @return true, if the property values are changed successfully, otherwise false
     * @throws CmsException if changing is not successful
     */
    private boolean performChangeOperation(boolean recursive) throws CmsException {

        // on recursive property changes display "please wait" screen
        if (recursive && !DIALOG_WAIT.equals(getParamAction())) {
            // return false, this will trigger the "please wait" screen
            return false;
        }

        // lock the selected resource
        checkLock(getParamResource());
        // change the property values    
        List changedResources = new ArrayList();
        changedResources = getCms().changeResourcesInFolderWithProperty(
            getParamResource(),
            getParamPropertyName(),
            getParamOldValue(),
            getParamNewValue(),
            recursive);
        setChangedResources(changedResources);
        return true;
    }

    /**
     * Sets the changed resources that were affected by the property change action.<p>
     *
     * @param changedResources the changed resources that were affected by the property change action
     */
    private void setChangedResources(List changedResources) {

        m_changedResources = changedResources;
    }
    
    /**
     * Validates the submitted form parameters.<p>
     * 
     * If parameters are missing, a localized error message String is created.<p>
     * 
     * @return true if all parameters are correct, otherwise false
     *
     */
    private boolean validateParameters() {
        
        Locale locale = getLocale();
        boolean allOk = true;

        StringBuffer validationErrors = new StringBuffer(32);

        // check resource parameter presence
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamResource()) || !getCms().existsResource(getParamResource())) {
            allOk = false;
            validationErrors.append(
                Messages.get().key(locale, Messages.GUI_PROP_CHANGE_VALIDATE_VFS_RESOURCE_0, null)).append("<br>");
        }
        
        // check selected property name
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamPropertyName())) {
            allOk = false;
            validationErrors.append(
                Messages.get().key(locale,  Messages.GUI_PROP_CHANGE_VALIDATE_SELECT_PROPERTY_0, null)).append("<br>");
        }
        
        // check old property value to look up
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamOldValue())) {
            allOk = false;
            validationErrors.append(
                Messages.get().key(locale,  Messages.GUI_PROP_CHANGE_VALIDATE_OLD_PROP_VALUE_0, null)).append("<br>");
        } else {
            try {
                // compile regular expression pattern
                Pattern.compile(getParamOldValue());
            } catch (PatternSyntaxException e) {
                allOk = false;
                validationErrors.append(
                    Messages.get().key(locale,  Messages.GUI_PROP_CHANGE_VALIDATE_OLD_PROP_PATTERN_0, null)).append("<br>");
            }
        }
        
        // check new property value
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamNewValue())) {
            allOk = false;
            validationErrors.append(
                Messages.get().key(locale,  Messages.GUI_PROP_CHANGE_VALIDATE_NEW_PROP_VALUE_0, null));
        }
        
        setErrorMessage(validationErrors.toString());
        setValidationErrors(!allOk);
        return allOk;
    }
    
}

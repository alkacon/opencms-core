/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsNewResourceXmlContent.java,v $
 * Date   : $Date: 2011/03/23 14:52:21 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.I_CmsXmlContentHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * The new resource xmlcontent dialog handles the creation of a xmlcontent.<p>
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_xmlcontent.jsp
 * </ul>
 * <p>
 * 
 * @author Michael Emmerich
 * @author Andreas Zahner
 * 
 * @version $Revision: 1.23 $ 
 * 
 * @since 6.0.0 
 */
public class CmsNewResourceXmlContent extends CmsNewResource {

    /** The value for the check model file presence form action. */
    public static final int ACTION_CHECKMODEL = 210;

    /** The value for the choose model file form action. */
    public static final int ACTION_CHOOSEMODEL = 200;

    /** The name for the check model file presence form action. */
    public static final String DIALOG_CHECKMODEL = "checkmodel";

    /** The name for the choose model file form action. */
    public static final String DIALOG_CHOOSEMODEL = "choosemodel";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsNewResourceXmlContent.class);

    /** Request parameter name for the model file. */
    public static final String PARAM_MODELFILE = "modelfile";

    /** Value for the option to use no model file. */
    public static final String VALUE_NONE = "none";

    /** The selected model file for the new resource. */
    private String m_paramModelFile;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourceXmlContent(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourceXmlContent(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the possible model files for the new resource.<p>
     * 
     * @param cms the current users context to work with
     * @param currentFolder the folder
     * @param newResourceTypeName the resource type name for the new resource to create
     * @return the possible model files for the new resource
     */
    public static List getModelFiles(CmsObject cms, String currentFolder, String newResourceTypeName) {

        try {
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(newResourceTypeName);
            // get the schema for the resource type to create
            String schema = (String)resType.getConfiguration().get(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
            CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, schema);
            // get the content handler for the resource type to create
            I_CmsXmlContentHandler handler = contentDefinition.getContentHandler();
            String masterFolder = handler.getModelFolder(cms, currentFolder);
            if (CmsStringUtil.isNotEmpty(masterFolder) && cms.existsResource(masterFolder)) {
                // folder for master files exists, get all files of the same resource type
                CmsResourceFilter filter = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED.addRequireType(resType.getTypeId());
                return cms.readResources(masterFolder, filter, false);
            } else {
                // no master folder found
                return Collections.EMPTY_LIST;
            }
        } catch (Throwable t) {
            // error determining resource type, should never happen
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Creates the resource using the specified resource name and the newresourcetype parameter.<p>
     * 
     * @throws JspException if inclusion of error dialog fails
     */
    @Override
    public void actionCreateResource() throws JspException {

        try {
            // calculate the new resource Title property value
            String title = computeNewTitleProperty();
            // create the full resource name
            String fullResourceName = computeFullResourceName();
            // eventually append ".html" suffix to new file if not present
            fullResourceName = appendSuffixHtml(fullResourceName, false);
            // create the Title and Navigation properties if configured
            I_CmsResourceType resType = OpenCms.getResourceManager().getResourceType(getParamNewResourceType());
            List properties = createResourceProperties(fullResourceName, resType.getTypeName(), title);

            // set request context attribute for model file if file was selected
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamModelFile())
                && !VALUE_NONE.equals(getParamModelFile())) {
                getCms().getRequestContext().setAttribute(CmsRequestContext.ATTRIBUTE_MODEL, getParamModelFile());
            }

            // create the new resource            
            getCms().createResource(fullResourceName, resType.getTypeId(), null, properties);
            setParamResource(fullResourceName);
            setResourceCreated(true);
        } catch (Throwable e) {
            // error creating file, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Returns the http URI of the current dialog, to be used
     * as value for the "action" attribute of a html form.<p>
     *  
     * @return the http URI of the current dialog
     */
    @Override
    public String getDialogUri() {

        if (!useNewStyle()) {
            return getJsp().link(VFS_PATH_COMMONS + "newresource_xmlcontent.jsp");
        } else {
            return super.getDialogUri();
        }
    }

    /**
     * Returns the possible model files for the new resource.<p>
     * 
     * @return the possible model files for the new resource
     */
    protected List getModelFiles() {

        return getModelFiles(getCms(), getSettings().getExplorerResource(), getParamNewResourceType());
    }

    /**
     * Returns the parameter that specifies the model file name.<p>
     * 
     * @return the parameter that specifies the model file name
     */
    public String getParamModelFile() {

        return m_paramModelFile;
    }

    /**
     * Returns if model files are available for the new resource.<p>
     * 
     * @return true if model files are available for the new resource, otherwise false
     */
    public boolean hasModelFiles() {

        return getModelFiles().size() > 0;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_OK.equals(getParamAction())) {
            setAction(ACTION_OK);
        } else if (DIALOG_CHECKMODEL.equals(getParamAction())) {
            if (hasModelFiles()) {
                // show the next dialog page, which presents a list of model files
                setAction(ACTION_CHOOSEMODEL);
                // put the necessary parameters to a Map before forwarding
                Map params = new HashMap(6);
                params.put(PARAM_RESOURCE, getParamResource());
                params.put(PARAM_NEWRESOURCETYPE, getParamNewResourceType());
                params.put(PARAM_TITLE, getParamTitle());
                // only add parameters if present, otherwise NPE is thrown
                if (CmsStringUtil.isNotEmpty(getParamNewResourceEditProps())) {
                    // edit properties is checked
                    params.put(PARAM_NEWRESOURCEEDITPROPS, getParamNewResourceEditProps());
                }
                if (CmsStringUtil.isNotEmpty(getParamAppendSuffixHtml())) {
                    // append .html suffix is checked
                    params.put(PARAM_APPENDSUFFIXHTML, getParamAppendSuffixHtml());
                }
                if (CmsStringUtil.isNotEmpty(getParamOriginalParams())) {
                    // add the original parameters
                    params.put(PARAM_ORIGINALPARAMS, getParamOriginalParams());
                }
                try {
                    sendForward(CmsNewResourceXmlContentModel.VFS_PATH_MODELDIALOG, params);
                } catch (Exception e) {
                    // error forwarding, log the exception as error
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
            } else {
                // no model files present, create the new resource
                setAction(ACTION_SUBMITFORM);
            }
        } else if (DIALOG_SUBMITFORM.equals(getParamAction())) {
            setAction(ACTION_SUBMITFORM);
        } else if (DIALOG_NEWFORM.equals(getParamAction())) {
            setAction(ACTION_NEWFORM);

            // set the correct title
            setParamTitle(getTitle());
            setInitialResourceName();

        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog     
            setParamTitle(key(Messages.GUI_NEWRESOURCE_XMLCONTENT_0));
            setInitialResourceName();
        }
    }

    /**
     * Sets the parameter that specifies the model file name.<p>
     * 
     * @param paramMasterFile the parameter that specifies the model file name
     */
    public void setParamModelFile(String paramMasterFile) {

        m_paramModelFile = paramMasterFile;
    }
}
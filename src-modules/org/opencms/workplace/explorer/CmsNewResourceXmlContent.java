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

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

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
    public static final String PARAM_MODELFILE = CmsWorkplace.PARAM_MODELFILE;

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
            List<CmsProperty> properties = createResourceProperties(fullResourceName, resType.getTypeName(), title);

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
     * Sets the parameter that specifies the model file name.<p>
     *
     * @param paramMasterFile the parameter that specifies the model file name
     */
    public void setParamModelFile(String paramMasterFile) {

        m_paramModelFile = paramMasterFile;
    }

    /**
     * Returns the possible model files for the new resource.<p>
     *
     * @return the possible model files for the new resource
     */
    protected List<CmsResource> getModelFiles() {

        return CmsResourceTypeXmlContent.getModelFiles(
            getCms(),
            getSettings().getExplorerResource(),
            getParamNewResourceType());
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
                Map<String, String[]> params = new HashMap<String, String[]>(6);
                params.put(PARAM_RESOURCE, new String[] {getParamResource()});
                params.put(PARAM_NEWRESOURCETYPE, new String[] {getParamNewResourceType()});
                params.put(PARAM_TITLE, new String[] {getParamTitle()});
                // only add parameters if present, otherwise NPE is thrown
                if (CmsStringUtil.isNotEmpty(getParamNewResourceEditProps())) {
                    // edit properties is checked
                    params.put(PARAM_NEWRESOURCEEDITPROPS, new String[] {getParamNewResourceEditProps()});
                }
                if (CmsStringUtil.isNotEmpty(getParamAppendSuffixHtml())) {
                    // append .html suffix is checked
                    params.put(PARAM_APPENDSUFFIXHTML, new String[] {getParamAppendSuffixHtml()});
                }
                if (CmsStringUtil.isNotEmpty(getParamOriginalParams())) {
                    // add the original parameters
                    params.put(PARAM_ORIGINALPARAMS, new String[] {getParamOriginalParams()});
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
}
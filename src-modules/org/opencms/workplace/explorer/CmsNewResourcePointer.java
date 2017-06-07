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
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * The new resource pointer dialog handles the creation of a pointer (external link).<p>
 *
 * The following files use this class:
 * <ul>
 * <li>/commons/newresource_pointer.jsp
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsNewResourcePointer extends CmsNewResource {

    /** Request parameter name for the link target. */
    public static final String PARAM_LINKTARGET = "linktarget";

    /** The link target parameter. */
    private String m_paramLinkTarget;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsNewResourcePointer(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsNewResourcePointer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Creates the new pointer resource.<p>
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
            // create the Title and Navigation properties if configured
            List<CmsProperty> properties = createResourceProperties(
                fullResourceName,
                CmsResourceTypePointer.getStaticTypeName(),
                title);
            // the link target
            String linkTarget = getParamLinkTarget();
            if (linkTarget == null) {
                linkTarget = "";
            }
            // create the pointer
            int pointerId = OpenCms.getResourceManager().getResourceType(
                CmsResourceTypePointer.getStaticTypeName()).getTypeId();
            getCms().createResource(fullResourceName, pointerId, linkTarget.getBytes(), properties);
            setParamResource(fullResourceName);
            setResourceCreated(true);
        } catch (Throwable e) {
            // error creating pointer, show error dialog
            setParamMessage(Messages.get().getBundle(getLocale()).key(Messages.ERR_CREATE_LINK_0));
            includeErrorpage(this, e);
        }

    }

    /**
     * Returns the link target request parameter value.<p>
     *
     * @return the link target request parameter value
     */
    public String getParamLinkTarget() {

        return m_paramLinkTarget;
    }

    /**
     * Sets the link target request parameter value.<p>
     *
     * @param linkTarget the link target request parameter value
     */
    public void setParamLinkTarget(String linkTarget) {

        m_paramLinkTarget = linkTarget;
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
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // build title for new resource dialog
            setParamTitle(key(Messages.GUI_NEWRESOURCE_POINTER_0));
        }
    }

}
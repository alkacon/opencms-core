/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/commons/Attic/CmsChaccBrowser.java,v $
 * Date   : $Date: 2005/06/23 10:47:14 $
 * Version: $Revision: 1.12 $
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.commons;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides methods for building the groups and users popup window.<p> 
 * 
 * The following files use this class:
 * <ul>
 * <li>/commons/chaccbrowser.jsp
 * </ul>
 * <p>
 *
 * @author  Andreas Zahner 
 * 
 * @version $Revision: 1.12 $ 
 * 
 * @since 6.0.0 
 */
public class CmsChaccBrowser extends CmsDialog {

    /** Constant for the frame name which is displayed: groups frame. */
    public static final String DIALOG_FRAME_GROUPS = "groups";
    /** Constant for the frame name which is displayed: users frame. */
    public static final String DIALOG_FRAME_USERS = "users";

    /** The dialog type. */
    public static final String DIALOG_TYPE = "chaccbrowser";

    /** Constant for the frame type which is displayed: default frame. */
    public static final int FRAME_DEFAULT = 1;
    /** Constant for the frame type which is displayed: groups frame. */
    public static final int FRAME_GROUPS = 100;
    /** Constant for the frame type which is displayed: users frame. */
    public static final int FRAME_USERS = 200;

    /** Request parameter name for the frame parameter. */
    public static final String PARAM_FRAME = "frame";
    private int m_frame;

    private String m_paramFrame;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsChaccBrowser(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsChaccBrowser(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds a list of all groups and returns an html string.<p>
     * 
     * @return html code for a group list
     * @throws JspException if problems including sub-elements occur   
     */
    public String buildGroupList() throws JspException {

        List groups = new Vector();
        StringBuffer retValue = new StringBuffer(1024);
        try {
            groups = getCms().getGroups();
        } catch (Throwable e) {
            // should usually never happen
            includeErrorpage(this, e);
        }

        for (int i = 0; i < groups.size(); i++) {
            CmsGroup curGroup = (CmsGroup)groups.get(i);
            retValue.append(buildEntryGroup(curGroup));
        }
        return retValue.toString();
    }

    /**
     * Builds a list of all users and returns an html string.<p>
     * 
     * @return html code for a user list
     * @throws JspException if problems including sub-elements occur  
     */
    public String buildUserList() throws JspException {

        List users = new Vector();
        StringBuffer retValue = new StringBuffer(1024);
        try {
            users = getCms().getUsers();
        } catch (Throwable e) {
            // should usually never happen
            includeErrorpage(this, e);
        }

        for (int i = 0; i < users.size(); i++) {
            CmsUser curUser = (CmsUser)users.get(i);
            retValue.append(buildEntryUser(curUser));
        }
        return retValue.toString();
    }

    /**
     * Returns the int representation of the frame parameter value.<p>
     * 
     * @return int representing the frame parameter value
     */
    public int getFrame() {

        return m_frame;
    }

    /**
     * Returns the value of the frame parameter, 
     * or null if this parameter was not provided.<p>
     * 
     * The frame parameter selects the frame which should be displayed.<p>
     * 
     * @return the value of the target parameter
     */
    public String getParamFrame() {

        return m_paramFrame;
    }

    /**
     * Sets the int representation of the frame parameter value.<p>
     * 
     * @param value the int value of the parameter
     */
    public void setFrame(int value) {

        m_frame = value;
    }

    /**
     * Sets the value of the frame parameter.<p>
     * 
     * @param value the value to set
     */
    public void setParamFrame(String value) {

        m_paramFrame = value;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        if (DIALOG_FRAME_GROUPS.equals(getParamFrame())) {
            setFrame(FRAME_GROUPS);
        } else if (DIALOG_FRAME_USERS.equals(getParamFrame())) {
            setFrame(FRAME_USERS);
        } else {
            setFrame(FRAME_DEFAULT);
        }
    }

    /**
     * Creates the html code for a single group entry.<p>
     * 
     * @param group the CmsGroup
     * @return the html code as StringBuffer
     */
    private StringBuffer buildEntryGroup(CmsGroup group) {

        StringBuffer retValue = new StringBuffer(256);
        retValue.append("<span class=\"dialogunmarked maxwidth\" onmouseover=\"className='dialogmarked maxwidth';\""
            + " onmouseout=\"className='dialogunmarked maxwidth'\" onclick=\"top.selectForm('0','"
            + group.getName()
            + "');\">");
        retValue.append("<img src=\"" + getSkinUri() + "commons/group.png\">&nbsp;");
        retValue.append(group.getName());
        retValue.append("</span>");
        return retValue;
    }

    /**
     * Creates the html code for a single user entry.<p>
     * 
     * @param user the CmsUser
     * @return the html code as StringBuffer
     */
    private StringBuffer buildEntryUser(CmsUser user) {

        StringBuffer retValue = new StringBuffer(384);
        retValue.append("<span class=\"dialogunmarked maxwidth\" onmouseover=\"className='dialogmarked maxwidth';\""
            + " onmouseout=\"className='dialogunmarked maxwidth'\" onclick=\"top.selectForm('1','"
            + user.getName()
            + "');\">");
        retValue.append("<img src=\"" + getSkinUri() + "commons/user.png\">&nbsp;");
        retValue.append(user.getName());
        if (!"".equals(user.getFirstname()) || !"".equals(user.getLastname())) {
            retValue.append(" (" + user.getFirstname() + " " + user.getLastname() + ")");
        }
        retValue.append("</span>");
        return retValue;
    }

}

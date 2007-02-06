/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/explorer/CmsExplorerContextMenuBuilder.java,v $
 * Date   : $Date: 2007/02/06 11:29:35 $
 * Version: $Revision: 1.1.2.1 $
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

import org.opencms.file.CmsObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Context menu builder class.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.5.6 
 */
public class CmsExplorerContextMenuBuilder extends CmsWorkplace {

    /** The resource list parameter value. */
    private String m_paramResourcelist;

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsExplorerContextMenuBuilder(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsExplorerContextMenuBuilder(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /** Html fragment constant. */
    private static final String HTML_SPAN_START = "<span class=\"cmenorm\" onmouseover=\"className='cmehigh';\" onmouseout=\"className='cmenorm';\">";
    /** Html fragment constant. */
    private static final String HTML_SPAN_START_INACTIVE = "<span class=\"inanorm\" onmouseover=\"className='inahigh';\" onmouseout=\"className='inanorm';\">";
    /** Html fragment constant. */
    private static final String HTML_SPAN_END = "</span>";

    /**
     * Generates the context menu for the given resources.<p>
     * 
     * @return html code
     */
    public String contextMenu() {

        StringBuffer menu = new StringBuffer();

        // get the resource path list
        List resourceList = CmsStringUtil.splitAsList(getParamResourcelist(), "|");

        // create a resource util object for the first resource in the list
        CmsResourceUtil resUtil;
        try {
            resUtil = new CmsResourceUtil(getCms(), getCms().readResource((String)resourceList.get(0)));
        } catch (CmsException e) {
            // fatal error
            return "";
        }

        // the explorer type settings
        CmsExplorerTypeSettings settings = null;

        // get the context menu configuration for the given selection mode
        CmsExplorerContextMenu contextMenu;

        // single or multi selection?
        boolean isSingleSelection = (resourceList.size() == 1);
        if (isSingleSelection) {
            // get the explorer type setting for the first resource
            try {
                settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(resUtil.getResourceTypeName());
            } catch (Throwable e) {
                return "";
            }
            if (settings == null || !isEditable(getCms(), settings)) {
                // the user has no access to this resource type
                return "";
            }
            // get the context menu configuration
            contextMenu = settings.getContextMenu();
        } else {
            // get the context menu configuration
            contextMenu = OpenCms.getWorkplaceManager().getMultiContextMenu();
            if (OpenCms.getWorkplaceManager().getMultiContextMenu() == null) {
                // no multi context menu defined, do not show menu
                return "";
            }
        }

        menu.append("<div class=\"cm2\">");
        menu.append("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" class=\"cm\">");

        boolean lastWasSeparator = false;
        boolean firstEntryWritten = false;
        String jspWorkplaceUri = OpenCms.getLinkManager().substituteLink(getCms(), CmsWorkplace.PATH_WORKPLACE);

        // for each defined menu item
        Iterator it = contextMenu.getAllEntries().iterator();
        while (it.hasNext()) {
            CmsExplorerContextMenuItem item = (CmsExplorerContextMenuItem)it.next();

            // rebuild the explorer settings
            String itemName = "-";
            String itemLink = " ";
            String itemTarget = "";
            String itemRules = "";

            if (CmsExplorerContextMenuItem.TYPE_ENTRY.equals(item.getType())) {
                itemName = key(item.getKey());
                if (item.getUri().startsWith("/")) {
                    itemLink = OpenCms.getLinkManager().substituteLink(getCms(), item.getUri());
                } else {
                    itemLink = jspWorkplaceUri;
                    itemLink += item.getUri();
                }
                itemTarget = item.getTarget();
                if (itemTarget == null) {
                    itemTarget = "";
                }
                itemRules = CmsStringUtil.substitute(item.getRules(), " ", "");
            }
            // parse the rules to create the autolock column
            itemRules = parseRules(itemRules, item.getKey());

            // 0:unchanged, 1:changed, 2:new, 3:deleted
            int result = -1;

            if (CmsExplorerContextMenuItem.TYPE_SEPARATOR.equals(item.getType())) {
                result = 1;
            } else if (getCms().getRequestContext().currentProject().isOnlineProject()) {
                // online project
                if (isSingleSelection) {
                    if (itemRules.charAt(0) == 'i') {
                        result = 2;
                    } else {
                        if (itemRules.charAt(0) == 'a') {
                            if ((itemLink.indexOf("showlinks=true") > 0) && (resUtil.getLinkType() == 0)) {
                                // special case: resource without siblings
                                result = 2;
                            } else {
                                result = (resUtil.getResourceTypeId() == 0) ? 3 : 4;
                            }
                        }
                    }
                } else {
                    // multi context menu
                    result = 2;
                }
            } else {
                // offline project
                if (isSingleSelection) {
                    if ((resUtil.getProjectState() != CmsResourceUtil.STATE_LOCKED_FOR_PUBLISHING)
                        && !resUtil.isInsideProject()) {
                        // if not publish lock and resource is from online project
                        if (itemRules.charAt(1) == 'i') {
                            result = (CmsExplorerContextMenuItem.TYPE_SEPARATOR.equals(item.getType())) ? 1 : 2;
                        } else {
                            if (itemRules.charAt(1) == 'a') {
                                if (CmsExplorerContextMenuItem.TYPE_SEPARATOR.equals(item.getType())) {
                                    result = 1;
                                } else {
                                    if ((itemLink.indexOf("showlinks=true") > 0) && (resUtil.getLinkType() == 0)) {
                                        // special case: resource without siblings
                                        result = 2;
                                    } else {
                                        result = (resUtil.getResourceTypeId() == 0) ? 3 : 4;
                                    }
                                }
                            }
                        }
                    } else {
                        char display = ' ';
                        // if not publish lock and resource is in this project => we have to differ 4 cases
                        if ((resUtil.getProjectState() != CmsResourceUtil.STATE_LOCKED_FOR_PUBLISHING)
                            && (CmsStringUtil.isEmptyOrWhitespaceOnly(resUtil.getLockedByName()))
                            || (resUtil.getLock().getType().isWorkflow())) {
                            // resource is not locked...
                            if (OpenCms.getWorkplaceManager().autoLockResources()) {
                                // autolock is enabled
                                display = itemRules.charAt(resUtil.getResource().getState().getState() + 6);
                            } else {
                                // autolock is disabled
                                display = itemRules.charAt(resUtil.getResource().getState().getState() + 2);
                            }
                        } else {
                            boolean isSharedLock = resUtil.getLock().getType().isShared();
                            isSharedLock = isSharedLock
                                || (resUtil.getProjectState() == CmsResourceUtil.STATE_LOCKED_FOR_PUBLISHING);
                            // TODO: this is hardcoded for commons/lockchange.jsp !! ...
                            if ((resUtil.getProjectState() == CmsResourceUtil.STATE_LOCKED_FOR_PUBLISHING)
                                && (itemLink.indexOf("lockchange") >= 0)) {
                                // disable steal lock for publish locks
                                display = 'i';
                            } else if (resUtil.getLockedInProjectId() == getCms().getRequestContext().currentProject().getId()) {
                                // locked in this project from ...
                                if (resUtil.getLockedByName().equals(
                                    getCms().getRequestContext().currentUser().getName())) {
                                    // ... the current user ...
                                    if (isSharedLock) {
                                        // ... as shared lock
                                        display = itemRules.charAt(resUtil.getResource().getState().getState() + 14);
                                    } else {
                                        // ... as exclusive lock
                                        display = itemRules.charAt(resUtil.getResource().getState().getState() + 10);
                                    }

                                } else {
                                    // ... someone else
                                    display = itemRules.charAt(resUtil.getResource().getState().getState() + 14);
                                }
                            } else {
                                // locked in an other project ...
                                display = itemRules.charAt(resUtil.getResource().getState().getState() + 14);
                            }
                        }
                        if (display == 'i') {
                            result = 2;
                        } else {
                            if (display == 'a') {
                                if ((itemLink.indexOf("showlinks=true") > 0) && (resUtil.getLinkType() == 0)) {
                                    // special case: resource without siblings
                                    result = 2;
                                } else {
                                    result = (resUtil.getResourceTypeId() == 0) ? 3 : 4;
                                }
                            }
                        }
                    }
                } else {
                    // multi context menu
                    result = 3;
                }
            }
            switch (result) {
                case 1:
                    // separator line
                    if ((firstEntryWritten) && (!lastWasSeparator) && (it.hasNext())) {
                        menu.append("<tr><td class=\"cmsep\"><span class=\"cmsep\"></div></td></tr>");
                        lastWasSeparator = true;
                    }
                    break;
                case 2:
                    // inactive entry
                    menu.append("<tr><td>" + HTML_SPAN_START_INACTIVE + itemName + HTML_SPAN_END + "</td></tr>");
                    lastWasSeparator = false;
                    firstEntryWritten = true;
                    break;
                case 3:
                case 4:
                    // active entry
                    String link;
                    if (isSingleSelection) {
                        link = "href=\"" + itemLink;
                        if (link.indexOf("?") > 0) {
                            link += "&";
                        } else {
                            link += "?";
                        }
                        link += "resource=" + getCms().getSitePath(resUtil.getResource()) + "\"";
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(itemTarget)) {
                            // href has a target set
                            link += " target='" + itemTarget + "'";
                        }
                        menu.append("<tr><td><a class=\"cme\" "
                            + link
                            + ">"
                            + HTML_SPAN_START
                            + itemName
                            + HTML_SPAN_END
                            + "</a></td></tr>");
                    } else {
                        // multi context menu
                        link = "href=\"javascript:top.submitMultiAction('" + itemLink + "');\"";
                        menu.append("<tr><td><a class=\"cme\" "
                            + link
                            + ">"
                            + HTML_SPAN_START
                            + itemName
                            + HTML_SPAN_END
                            + "</a></td></tr>");
                    }
                    lastWasSeparator = false;
                    firstEntryWritten = true;
                    break;
                default:
                    // alert("Undefined result for menu " + a);
                    break;
            }
        } // end for ...
        menu.append("</table></div>");
        return menu.toString();
    }

    /**
     * Returns the resourcelist parameter value.<p>
     *
     * @return the resourcelist parameter value
     */
    public String getParamResourcelist() {

        return m_paramResourcelist;
    }

    /**
     * Sets the resourcelist parameter value.<p>
     *
     * @param paramResourcelist the resourcelist parameter value to set
     */
    public void setParamResourcelist(String paramResourcelist) {

        m_paramResourcelist = paramResourcelist;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        fillParamValues(request);
    }

    /**
     * Checks if the current user has write permissions on the given settings.<p>
     * 
     * @param cms the current cms context
     * @param settings the settings to check
     * 
     * @return <code>true</code> if the current user has write permissions on the given settings
     */
    private boolean isEditable(CmsObject cms, CmsExplorerTypeSettings settings) {

        // determine if this resource type is editable for the current user
        CmsPermissionSet permissions = settings.getAccess().getPermissions(cms);
        return permissions.requiresWritePermission();
    }

    /**
     * Parses the rules and adds a column for the autolock feature of resources.<p>
     * 
     * @param rules the current rules
     * @param key the key name of the current item
     * @return the rules with added autlock rules column
     */
    private String parseRules(String rules, String key) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(rules)) {
            return "";
        }
        StringBuffer newRules = new StringBuffer(rules.length() + 4);
        newRules.append(rules.substring(0, 6));
        if (Messages.GUI_EXPLORER_CONTEXT_LOCK_0.equalsIgnoreCase(key)
            || Messages.GUI_EXPLORER_CONTEXT_UNLOCK_0.equalsIgnoreCase(key)) {
            // for "lock" and "unlock" item, use same rules as "unlocked" column
            newRules.append(rules.substring(2, 6));
        } else {
            // for all other items, use same rules as "locked exclusively by current user" column
            newRules.append(rules.substring(6, 10));
        }
        newRules.append(rules.substring(6));
        return newRules.toString();
    }
}
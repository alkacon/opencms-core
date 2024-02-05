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

package org.opencms.workplace.tools.link;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsInternalLinksValidator;
import org.opencms.relations.CmsRelation;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.CmsListItem;
import org.opencms.workplace.list.CmsListItemDetails;
import org.opencms.workplace.list.CmsListItemDetailsFormatter;
import org.opencms.workplace.list.CmsListMetadata;
import org.opencms.workplace.list.I_CmsListResourceCollector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Internal link validation Dialog.<p>
 *
 * @since 6.5.3
 */
public class CmsInternalLinkValidationList extends A_CmsListExplorerDialog {

    /** List detail error. */
    public static final String LIST_DETAIL_LINKS = "dl";

    /** list id constant. */
    public static final String LIST_ID = "lv";

    /** The internal collector instance. */
    private I_CmsListResourceCollector m_collector;

    /** The validator class. */
    private CmsInternalLinksValidator m_validator;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsInternalLinkValidationList(CmsJspActionElement jsp) {

        super(jsp, LIST_ID, Messages.get().container(Messages.GUI_BROKENLINKS_LIST_NAME_0));
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsInternalLinkValidationList(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListMultiActions()
     */
    @Override
    public void executeListMultiActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#executeListSingleActions()
     */
    @Override
    public void executeListSingleActions() {

        throwListUnsupportedActionException();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListExplorerDialog#getCollector()
     */
    @Override
    public I_CmsListResourceCollector getCollector() {

        if (m_collector == null) {
            m_collector = new CmsInternalLinkValidationFilesCollector(
                this,
                getValidator().getResourcesWithBrokenLinks());
        }
        return m_collector;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#customHtmlStart()
     */
    @Override
    protected String customHtmlStart() {

        StringBuffer result = new StringBuffer(512);
        if (getValidator().getNotVisibleResourcesCount() > 0) {
            result.append(dialogBlockStart(key(Messages.GUI_BROKENLINKS_NOTICE_0)));
            result.append("\n");
            result.append(
                key(
                    Messages.GUI_BROKENLINKS_NOT_VISIBLE_RESOURCES_1,
                    new Object[] {Integer.valueOf(getValidator().getNotVisibleResourcesCount())}));
            result.append("\n");
            result.append(dialogBlockEnd());
        }
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#fillDetails(java.lang.String)
     */
    @Override
    protected void fillDetails(String detailId) {

        // get content
        List resourceNames = getList().getAllContent();
        Iterator i = resourceNames.iterator();
        while (i.hasNext()) {
            CmsListItem item = (CmsListItem)i.next();
            CmsResource res = getCollector().getResource(getCms(), item);
            // check if errors are enabled
            StringBuffer html = new StringBuffer();
            // broken links detail is enabled
            if (detailId.equals(LIST_DETAIL_LINKS)) {
                // get all errors for this resource and show them
                List brokenLinks = getValidator().getBrokenLinksForResource(res.getRootPath());
                if (brokenLinks != null) {
                    Iterator j = brokenLinks.iterator();
                    while (j.hasNext()) {
                        CmsRelation brokenLink = (CmsRelation)j.next();
                        String link = brokenLink.getTargetPath();
                        String siteRoot = OpenCms.getSiteManager().getSiteRoot(link);
                        String siteName = siteRoot;
                        if (siteRoot != null) {
                            String storedSiteRoot = getCms().getRequestContext().getSiteRoot();
                            try {
                                getCms().getRequestContext().setSiteRoot("/");
                                siteName = getCms().readPropertyObject(
                                    siteRoot,
                                    CmsPropertyDefinition.PROPERTY_TITLE,
                                    false).getValue(siteRoot);
                            } catch (CmsException e) {
                                siteName = siteRoot;
                            } finally {
                                getCms().getRequestContext().setSiteRoot(storedSiteRoot);
                            }
                            link = link.substring(siteRoot.length());
                        } else {
                            siteName = "/";
                        }
                        if (!getCms().getRequestContext().getSiteRoot().equals(siteRoot)) {
                            link = key(
                                org.opencms.workplace.commons.Messages.GUI_DELETE_SITE_RELATION_2,
                                new Object[] {siteName, link});
                        }
                        html.append(link);
                        html.append("<br>");
                    }
                    item.set(detailId, html.toString());
                }
            }
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setIndependentActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setIndependentActions(CmsListMetadata metadata) {

        // create list item detail for broken links
        CmsListItemDetails brokenLinks = new CmsListItemDetails(LIST_DETAIL_LINKS);
        brokenLinks.setAtColumn(LIST_COLUMN_NAME);
        brokenLinks.setVisible(true);
        brokenLinks.setShowActionName(Messages.get().container(Messages.GUI_BROKENLINKS_DETAIL_SHOW_LINKS_NAME_0));
        brokenLinks.setShowActionHelpText(Messages.get().container(Messages.GUI_BROKENLINKS_DETAIL_SHOW_LINKS_HELP_0));
        brokenLinks.setHideActionName(Messages.get().container(Messages.GUI_BROKENLINKS_DETAIL_HIDE_LINKS_NAME_0));
        brokenLinks.setHideActionHelpText(Messages.get().container(Messages.GUI_BROKENLINKS_DETAIL_HIDE_LINKS_HELP_0));
        brokenLinks.setFormatter(
            new CmsListItemDetailsFormatter(Messages.get().container(Messages.GUI_BROKENLINKS_DETAIL_LINKS_NAME_0)));
        metadata.addItemDetails(brokenLinks);

        super.setIndependentActions(metadata);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListDialog#setMultiActions(org.opencms.workplace.list.CmsListMetadata)
     */
    @Override
    protected void setMultiActions(CmsListMetadata metadata) {

        // no LMA
    }

    /**
     * Returns the link validator class.<p>
     *
     * @return the link validator class
     */
    private CmsInternalLinksValidator getValidator() {

        if (m_validator == null) {
            // get the content check result object
            Map objects = (Map)getSettings().getDialogObject();
            Object o = objects.get(CmsInternalLinkValidationDialog.class.getName());
            List resources = new ArrayList();
            if ((o != null) && (o instanceof List)) {
                resources = (List)o;
            }
            m_validator = new CmsInternalLinksValidator(getCms(), resources);
        }
        return m_validator;
    }

}
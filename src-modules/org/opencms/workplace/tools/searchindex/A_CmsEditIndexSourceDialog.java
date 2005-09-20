/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/searchindex/A_CmsEditIndexSourceDialog.java,v $
 * Date   : $Date: 2005/09/20 15:39:06 $
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

package org.opencms.workplace.tools.searchindex;

import org.opencms.configuration.CmsSearchConfiguration;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalStateException;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearchIndexSource;
import org.opencms.search.CmsSearchManager;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Abstract dialog class for editing or creating a <code>{@link org.opencms.search.CmsSearchIndexSource}</code>.<p> 
 * 
 * The <code>{@link #PARAM_INDEXSOURCE}</code> ("indexsource") is supported 
 * by means of widget technology (setter / getter).<p>
 * 
 * Also - for accessing search functionality a member <code>{@link #m_searchManager}</code> 
 * is accessible for implementations. <p>
 * 
 * If the property "admintoolhandler-args" contains the sequence "path:/searchindex/new-indexsource", 
 * a new indexsource will be created at first visit of this page 
 * (<code>{@link #initUserObject()}</code>). Else this dialog will be for editing an existing 
 * indexsource and the request parameter "indexsource" is mandatory. <p>
 * 
 * @author Achim Westermann
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.0.0 
 */
public abstract class A_CmsEditIndexSourceDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "indexsource";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** 
     * The request parameter for the indexsource to work with when contacting 
     * this dialog from another. <p>
     * 
     * It may be emtpy if we are on the new indexsource dialog (/searchindex/new-indexsource.html).<p>
     *      
     **/
    public static final String PARAM_INDEXSOURCE = "indexsource";

    /** The user object that is edited on this dialog. */
    protected CmsSearchIndexSource m_indexsource;

    /** The search manager singleton for convenient access. **/
    protected CmsSearchManager m_searchManager;

    /** Stores the value of the request parameter for the search index Name. */
    private String m_paramIndexSource;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public A_CmsEditIndexSourceDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsEditIndexSourceDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Writes the updated search configuration back to the XML 
     * configuration file and refreshes the complete list.<p>
     */
    protected static void writeConfiguration() {

        // update the XML configuration
        OpenCms.writeConfiguration(CmsSearchConfiguration.class);
    }

    /**
     * Commits the edited search index to the search manager.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {

            // if new create it first
            if (!m_searchManager.getSearchIndexSources().keySet().contains(m_indexsource.getName())) {
                m_searchManager.addSearchIndexSource(m_indexsource);
            }
            writeConfiguration();

        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
    }

    /**
     * Returns the request parameter value for parameter indexsource. <p>
     * 
     * @return the request parameter value for parameter indexsource
     */
    public String getParamIndexsource() {

        return m_paramIndexSource;
    }

    /**
     * Sets the request parameter value for parameter indexsource. <p>
     * 
     * @param indexsource the request parameter value for parameter indexsource
     */
    public void setParamIndexsource(String indexsource) {

        m_paramIndexSource = indexsource;
    }

    /**
     * Initializes the user object (a <code>{@link org.opencms.search.CmsSearchIndex}</code> instance.<p>
     * 
     * Implementation always have to call <code>"super.defineWidgets()"</code> first as 
     * this action may only be done here (relies on filled request parameters, the next 
     * following operation <code>{@link CmsWidgetDialog#createDialogHtml()}</code> will 
     * rely on this. <p>
     * 
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected void defineWidgets() {

        initUserObject();
        setKeyPrefix(KEY_PREFIX);

    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Returns the root path of this dialog (path relative to "/system/workplace/admin").<p>
     * 
     * @return the root path of this dialog (path relative to "/system/workplace/admin")
     */
    protected String getToolPath() {

        return "/searchindex/indexsources/indexsource";
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the user object to work with depending on the dialog state and request parameters.<p>
     * 
     * Two initializations of the user object on first dialog call are possible:
     * <ul>
     * <li>edit an existing search index</li>
     * <li>create a new search index with default initialization</li>
     * </ul>
     */
    protected void initUserObject() {

        if (m_indexsource == null) {
            try {
                m_indexsource = m_searchManager.getIndexSource(getParamIndexsource());
                if (m_indexsource == null) {
                    m_indexsource = new CmsSearchIndexSource();
                }
            } catch (Exception e) {
                m_indexsource = new CmsSearchIndexSource();
            }
        }
    }

    /**
     * Overridden to initialize the internal <code>CmsSearchManager</code> before initWorkplaceRequestValues -> 
     * defineWidgets ->  will access it (NPE). <p>
     * 
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        m_searchManager = OpenCms.getSearchManager();
        super.initWorkplaceMembers(jsp);
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current search index source
        Map dialogObject = (Map)getDialogObject();
        if (dialogObject == null) {
            dialogObject = new HashMap();
            dialogObject.put(PARAM_INDEXSOURCE, m_indexsource);
            setDialogObject(dialogObject);
        }

    }

    /**
     * Checks if the new search index dialog has to be displayed.<p>
     * 
     * @return <code>true</code> if the new search index dialog has to be displayed
     */
    protected boolean isNewIndexSource() {

        return DIALOG_INITIAL.equals(getParamAction());
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    protected void validateParamaters() throws Exception {

        if (!isNewIndexSource()) {
            // test the needed parameters
            if (getParamIndexsource() == null && getJsp().getRequest().getParameter("name.0") == null) {
                throw new CmsIllegalStateException(Messages.get().container(
                    Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1,
                    PARAM_INDEXSOURCE));
            }
        }
    }
}
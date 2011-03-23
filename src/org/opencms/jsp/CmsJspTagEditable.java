/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/jsp/CmsJspTagEditable.java,v $
 * Date   : $Date: 2011/03/23 14:51:36 $
 * Version: $Revision: 1.33 $
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

package org.opencms.jsp;

import org.opencms.file.CmsObject;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.flex.CmsFlexController;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.directedit.CmsDirectEditJspIncludeProvider;
import org.opencms.workplace.editors.directedit.CmsDirectEditMode;
import org.opencms.workplace.editors.directedit.CmsDirectEditParams;
import org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;

/**
 * Implementation of the <code>&lt;cms:editable/&gt;</code> tag.<p>
 * 
 * This class is also used to generate the direct edit buttons for the 
 * <code>&lt;cms:include editable="..." /&gt;</code> and <code>&lt;cms:contentload editable="..." /&gt;</code> tags.<p>
 * 
 * Since OpenCms version 6.2.3, the direct edit button HTML generated is controlled by an instance of {@link I_CmsDirectEditProvider}.
 * The default direct edit provider used can be configured in <code>opencms-workplace.xml</code> in the 
 * <code>&lt;directeditprovider class="..." /&gt;</code> node. The standard provider is
 * {@link org.opencms.workplace.editors.directedit.CmsDirectEditDefaultProvider}.
 * It's possible to override the default provider onm a page-by-page basis by initializing direct edit with 
 * <code>&lt;cms:editable provider="...." /&gt;</code> on top of the page.<p>
 * 
 * Since OpenCms version 6.2.3, it is also possible to place the HTML of the direct edit buttons manually.
 * This is intended for pages where the template HTML is not compatible with the direct edit HTML,
 * which usually results in a funny placement of the direct edit buttons in a totally wrong position.
 * To do manual placement of the direct edit buttons, you need to place <code>&lt;cms:editable mode="manual"&gt;</code> and
 * <code>&lt;/cms:editable&gt;</code> around your HTML. Both tags (start and end) will insert HTML according to 
 * the used {@link I_CmsDirectEditProvider}. The direct edit provider used must also support manual 
 * placing, or the manual tags will be ignored and the HTML will be inserted at the automatic position. 
 * A provider which support manual placing is the {@link org.opencms.workplace.editors.directedit.CmsDirectEditTextButtonProvider}.<p>
 * 
 * @version $Revision: 1.33 $ 
 * 
 * @since 6.0.0 
 */
public class CmsJspTagEditable extends BodyTagSupport {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJspTagEditable.class);

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = 4137789622146499225L;

    /** File with editable elements. */
    protected String m_file;

    /** Indicates which direct edit mode is active. */
    protected transient CmsDirectEditMode m_mode;

    /** Class name of the direct edit provider. */
    protected String m_provider;

    /** Indicates if the tag is the first on the page, this mean the header file must be included. */
    private boolean m_firstOnPage;

    /** Indicates if the direct edit HTML is to be placed manually. */
    private boolean m_manualPlacement;

    /**
     * Editable action method.<p>
     * 
     * @param context the current JSP page context
     * @param provider the class name of the direct edit privider to use (may be <code>null</code>, which means use the default)
     * @param mode the direct edit mode to use (may be <code>null</code>, which means current use mode on page)
     * @param fileName optional filename parameter for the direct edit provider (may be <code>null</code>, which means use the default)
     * 
     * @throws JspException in case something goes wrong
     */
    public static void editableTagAction(PageContext context, String provider, CmsDirectEditMode mode, String fileName)
    throws JspException {

        if (mode == CmsDirectEditMode.FALSE) {
            // direct edit is turned off
            return;
        }

        ServletRequest req = context.getRequest();
        if (CmsHistoryResourceHandler.isHistoryRequest(req)) {
            // don't display direct edit buttons on an historical resource
            return;
        }

        CmsFlexController controller = CmsFlexController.getController(req);
        CmsObject cms = controller.getCmsObject();

        if (cms.getRequestContext().currentProject().isOnlineProject()) {
            // direct edit is never enabled in the online project
            return;
        }

        if (CmsWorkplace.isTemporaryFileName(cms.getRequestContext().getUri())) {
            // don't display direct edit buttons if a temporary file is displayed
            return;
        }

        I_CmsDirectEditProvider eb = getDirectEditProvider(context);
        if (eb == null) {
            if (CmsStringUtil.isNotEmpty(fileName) && CmsStringUtil.isEmpty(provider)) {
                // if only a filename but no provider class is given, use JSP includes for backward compatibility
                provider = CmsDirectEditJspIncludeProvider.class.getName();
            }
            // no provider available in page context
            if (CmsStringUtil.isNotEmpty(provider)) {
                try {
                    // create a new instance of the selected provider
                    eb = (I_CmsDirectEditProvider)Class.forName(provider).newInstance();
                } catch (Exception e) {
                    // log error
                    LOG.error(Messages.get().getBundle().key(Messages.ERR_DIRECT_EDIT_PROVIDER_1, provider), e);
                }
            }
            if (eb == null) {
                // use configured direct edit provider as a fallback
                eb = OpenCms.getWorkplaceManager().getDirectEditProvider();
            }
            if (mode == null) {
                // use automatic placement by default
                mode = CmsDirectEditMode.AUTO;
            }
            eb.init(cms, mode, fileName);
            // store the provider in the page context
            setDirectEditProvider(context, eb);
        }
        if (eb.isManual(mode)) {
            // manual mode, insert required HTML
            CmsDirectEditParams params = getDirectEditProviderParams(context);
            if (params != null) {
                // insert direct edit start HTML
                eb.insertDirectEditStart(context, params);
            } else {
                // insert direct edit end HTML
                eb.insertDirectEditEnd(context);
            }
        } else {
            // insert direct edit header HTML
            eb.insertDirectEditIncludes(context, new CmsDirectEditParams(cms.getRequestContext().getUri()));
        }
    }

    /**
     * Closes the current direct edit element.<p>
     * 
     * @param context the current JSP page context
     * 
     * @throws JspException in case something goes wrong
     */
    public static void endDirectEdit(PageContext context) throws JspException {

        // get the direct edit bean from the context
        I_CmsDirectEditProvider eb = getDirectEditProvider(context);

        if (eb != null) {
            // the direct edit bean must be available
            eb.insertDirectEditEnd(context);
        }
    }

    /**
     * Returns the current initialized instance of the direct edit provider.<p>
     * 
     * @param context the current JSP page context
     * 
     * @return the current initialized instance of the direct edit provider
     */
    public static I_CmsDirectEditProvider getDirectEditProvider(PageContext context) {

        // get the direct edit provider from the request attributes
        return (I_CmsDirectEditProvider)context.getRequest().getAttribute(
            I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER);
    }

    /**
     * Includes the "direct edit" start element that adds HTML for the editable area to 
     * the output page.<p>
     * 
     * @param context the current JSP page context    
     * @param params the direct edit parameters
     * 
     * @return <code>true</code> in case a direct edit element has been opened
     * 
     * @throws JspException in case something goes wrong
     */
    public static boolean startDirectEdit(PageContext context, CmsDirectEditParams params) throws JspException {

        // get the direct edit bean from the context
        I_CmsDirectEditProvider eb = getDirectEditProvider(context);

        boolean result = false;
        if (eb != null) {
            // the direct edit bean must be available
            if (eb.isManual(params.getMode())) {
                // store the given parameters for the next manual call
                setDirectEditProviderParams(context, params);
            } else {
                // automatic mode, insert direct edit HTML
                result = eb.insertDirectEditStart(context, params);
            }
        }

        return result;
    }

    /**
     * Returns the current initialized instance of the direct edit provider parameters from the given page context.<p>
     * 
     * Also removes the parameters from the given page context.<p>
     * 
     * @param context the current JSP page context
     * 
     * @return the current initialized instance of the direct edit provider parameters
     */
    protected static CmsDirectEditParams getDirectEditProviderParams(PageContext context) {

        // get the current request
        ServletRequest req = context.getRequest();
        // get the direct edit params from the request attributes
        CmsDirectEditParams result = (CmsDirectEditParams)req.getAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER_PARAMS);
        if (result != null) {
            req.removeAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER_PARAMS);
        }
        return result;
    }

    /**
     * Sets the current initialized instance of the direct edit provider.<p>
     * 
     * @param context the current JSP page context
     * 
     * @param provider the current initialized instance of the direct edit provider to set
     */
    protected static void setDirectEditProvider(PageContext context, I_CmsDirectEditProvider provider) {

        // set the direct edit provider as attribute to the request
        context.getRequest().setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER, provider);
    }

    /**
     * Sets the current initialized instance of the direct edit provider parameters to the page context.<p>
     * 
     * @param context the current JSP page context
     * @param params the current initialized instance of the direct edit provider parameters to set
     */
    protected static void setDirectEditProviderParams(PageContext context, CmsDirectEditParams params) {

        // set the direct edit params as attribute to the request
        context.getRequest().setAttribute(I_CmsDirectEditProvider.ATTRIBUTE_DIRECT_EDIT_PROVIDER_PARAMS, params);
    }

    /**
     * Close the direct edit tag, also prints the direct edit HTML to the current page.<p>
     * 
     * @return {@link #EVAL_PAGE}
     * 
     * @throws JspException in case something goes wrong
     */
    @Override
    public int doEndTag() throws JspException {

        if (m_firstOnPage || m_manualPlacement) {
            // only execute action for the first "editable" tag on the page (include file), or in manual mode
            editableTagAction(pageContext, m_provider, m_mode, m_file);
        }
        if (OpenCms.getSystemInfo().getServletContainerSettings().isReleaseTagsAfterEnd()) {
            // need to release manually, JSP container may not call release as required (happens with Tomcat)
            release();
        }

        return EVAL_PAGE;
    }

    /**
     * Opens the direct edit tag, if manual mode is set then the next 
     * start HTML for the direct edit buttons is printed to the page.<p>
     * 
     * @return {@link #EVAL_BODY_INCLUDE}
     * 
     * @throws JspException in case something goes wrong
     */
    @Override
    public int doStartTag() throws JspException {

        if (!CmsFlexController.isCmsOnlineRequest(pageContext.getRequest())
            && !CmsWorkplace.isTemporaryFileName(CmsFlexController.getCmsObject(pageContext.getRequest()).getRequestContext().getUri())) {
            // all this does NOT apply to the "online" project, or for temporary files
            I_CmsDirectEditProvider eb = getDirectEditProvider(pageContext);
            // if no provider is available this is the first "editable" tag on the page
            m_firstOnPage = (eb == null);
            m_manualPlacement = false;
            if (m_mode == CmsDirectEditMode.MANUAL) {
                // manual mode requested, we may need to insert HTML
                if (!m_firstOnPage && (eb != null)) {
                    // first tag on a page is only for insertion of header HTML
                    if (eb.isManual(m_mode)) {
                        // the provider supports manual placement of buttons
                        m_manualPlacement = true;
                        editableTagAction(pageContext, m_provider, m_mode, m_file);
                    }
                }
            }
        } else {
            // this will ensure the "end" tag is also ignored in the online project
            m_firstOnPage = false;
            m_manualPlacement = false;
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Gets the file with elements for direct editing.<p>
     * 
     * @return the file
     */
    public String getFile() {

        return m_file != null ? m_file : "";
    }

    /**
     * Returns the direct edit mode.<p>
     *
     * @return the direct edit mode
     */
    public String getMode() {

        return m_mode != null ? m_mode.toString() : "";
    }

    /**
     * Returns the class name of the direct edit provider.<p>
     *
     * @return the class name of the direct edit provider
     */
    public String getProvider() {

        return m_provider != null ? m_provider : "";
    }

    /**
     * Releases any resources we may have (or inherit).<p>
     */
    @Override
    public void release() {

        super.release();
        m_file = null;
        m_provider = null;
        m_mode = null;
        m_firstOnPage = false;
        m_manualPlacement = true;
    }

    /**
     * Sets the file with elements for direct editing.<p>
     * 
     * @param file the file to set 
     */
    public void setFile(String file) {

        if (file != null) {
            m_file = file;
        }
    }

    /**
     * Sets the direct edit mode.<p>
     *
     * @param mode the direct edit mode to set
     */
    public void setMode(String mode) {

        m_mode = CmsDirectEditMode.valueOf(mode);
    }

    /**
     * Sets the class name of the direct edit provider.<p>
     *
     * @param provider the class name of the direct edit provider to set
     */
    public void setProvider(String provider) {

        if (provider != null) {
            m_provider = provider;
        }
    }
}
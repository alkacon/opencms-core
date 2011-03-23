/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/directedit/A_CmsDirectEditProvider.java,v $
 * Date   : $Date: 2011/03/23 14:50:39 $
 * Version: $Revision: 1.8 $
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

package org.opencms.workplace.editors.directedit;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessages;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.editors.Messages;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Basic functions for direct edit providers.<p>
 * 
 * @author Alexander Kandzior
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.2.3 
 */
public abstract class A_CmsDirectEditProvider implements I_CmsDirectEditProvider {

    /** Default direct edit include file URI for post 6.2.3 direct edit providers. */
    protected static final String INCLUDE_FILE_DEFAULT = "/system/workplace/editors/direct_edit_include.txt";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsDirectEditProvider.class);

    /** The current users OpenCms context. */
    protected CmsObject m_cms;

    /** The parameters form the configuration. */
    protected Map m_configurationParameters;

    /** The editor button style to use. */
    protected int m_editButtonStyle;

    /** Value of the "fileName" parameter. */
    protected String m_fileName;

    /** Used to access the editor messages. */
    protected CmsMessages m_messages;

    /** Indicates which direct edit mode is used. */
    protected CmsDirectEditMode m_mode;

    /** Used to generate the edit id's. */
    protected Random m_rnd;

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        if (m_configurationParameters == null) {
            m_configurationParameters = new TreeMap();
        }
        m_configurationParameters.put(paramName, paramValue);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public Map getConfiguration() {

        if (m_configurationParameters == null) {
            // configuration parameters are usually null if the constructor has been used
            return Collections.EMPTY_MAP;
        }
        // this implementation ensures that this is an unmodifiable Map in #initConfiguration() 
        return m_configurationParameters;
    }

    /**
     * Calculates the direct edit resource information for the given VFS resource.<p>
     * 
     * This includes the direct edit permissions. 
     * If the permissions are not {@link CmsDirectEditPermissions#INACTIVE}, then the resource and lock
     * information is also included in the result.<p> 
     * 
     * @param resourceName the name of the VFS resource to get the direct edit info for
     * 
     * @return the direct edit resource information for the given VFS resource
     */
    public CmsDirectEditResourceInfo getResourceInfo(String resourceName) {

        try {
            // first check some simple preconditions for direct edit
            if (m_cms.getRequestContext().currentProject().isOnlineProject()) {
                // don't show direct edit button in online project
                return CmsDirectEditResourceInfo.INACTIVE;
            }
            if (CmsWorkplace.isTemporaryFileName(resourceName)) {
                // don't show direct edit button on a temporary file
                return CmsDirectEditResourceInfo.INACTIVE;
            }
            if (!m_cms.isInsideCurrentProject(resourceName)) {
                // don't show direct edit button on files not belonging to the current project
                return CmsDirectEditResourceInfo.INACTIVE;
            }
            // read the target resource
            CmsResource resource = m_cms.readResource(resourceName, CmsResourceFilter.ALL);
            if (!OpenCms.getResourceManager().getResourceType(resource.getTypeId()).isDirectEditable()) {
                // don't show direct edit button for non-editable resources 
                return CmsDirectEditResourceInfo.INACTIVE;
            }
            // check the resource lock
            CmsLock lock = m_cms.getLock(resource);
            boolean locked = !(lock.isUnlocked() || lock.isOwnedInProjectBy(
                m_cms.getRequestContext().currentUser(),
                m_cms.getRequestContext().currentProject()));
            // check the users permissions on the resource
            if (m_cms.hasPermissions(
                resource,
                CmsPermissionSet.ACCESS_WRITE,
                false,
                CmsResourceFilter.IGNORE_EXPIRATION)) {
                // only if write permissions are granted the resource may be direct editable                
                if (locked) {
                    // a locked resource must be shown as "disabled"
                    return new CmsDirectEditResourceInfo(CmsDirectEditPermissions.DISABLED, resource, lock);
                }
                // if we have write permission and the resource is not locked then direct edit is enabled
                return new CmsDirectEditResourceInfo(CmsDirectEditPermissions.ENABLED, resource, lock);
            }
        } catch (Exception e) {
            // all exceptions: don't mix up the result HTML, always return INACTIVE mode 
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(Messages.LOG_CALC_EDIT_MODE_FAILED_1, resourceName), e);
            }
        }
        // otherwise the resource is not direct editable
        return CmsDirectEditResourceInfo.INACTIVE;
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#init(org.opencms.file.CmsObject, org.opencms.workplace.editors.directedit.CmsDirectEditMode, java.lang.String)
     */
    public void init(CmsObject cms, CmsDirectEditMode mode, String fileName) {

        m_cms = cms;
        m_fileName = fileName;
        if (CmsStringUtil.isEmpty(m_fileName)) {
            m_fileName = INCLUDE_FILE_DEFAULT;
        }
        m_mode = mode != null ? mode : CmsDirectEditMode.AUTO;

        m_rnd = new Random();
        CmsUserSettings settings = new CmsUserSettings(cms);
        m_messages = new CmsMessages(Messages.get().getBundleName(), settings.getLocale());
        m_editButtonStyle = settings.getEditorButtonStyle();
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        // we need a Map with a defined order of keys for serializing the configuration
        if (m_configurationParameters == null) {
            // suppress the compiler warning, this is never true
            m_configurationParameters = Collections.EMPTY_MAP;
        } else {
            m_configurationParameters = Collections.unmodifiableMap(m_configurationParameters);
        }
        if (m_configurationParameters == null) {
            // suppress the compiler warning, this is never true
            throw new CmsConfigurationException(null);
        }
    }

    /**
     * @see org.opencms.workplace.editors.directedit.I_CmsDirectEditProvider#isManual(org.opencms.workplace.editors.directedit.CmsDirectEditMode)
     */
    public boolean isManual(CmsDirectEditMode mode) {

        return (mode == CmsDirectEditMode.MANUAL)
            || ((m_mode == CmsDirectEditMode.MANUAL) && (mode == CmsDirectEditMode.TRUE));
    }

    /**
     * Returns the given link resolved according to the OpenCms context and export rules.<p>
     * 
     * @param target the link target to resolve
     * 
     * @return the given link resolved according to the OpenCms context and export rules
     */
    protected String getLink(String target) {

        return OpenCms.getLinkManager().substituteLink(m_cms, target);
    }

    /**
     * Returns the next random edit id.<p>
     * 
     * Random edit id's are used because to separate multiple direct edit buttons on one page.<p>
     * 
     * @return the next random edit id
     */
    protected String getNextDirectEditId() {

        return "ocms_".concat(String.valueOf(m_rnd.nextInt(1000000)));
    }

    /**
     * Prints the given content string to the given page context.<p>
     * 
     * Does not print anything if the content is <code>null</code>.<p>
     * 
     * @param context the JSP page context to print the content to
     * @param content the content to print
     * 
     * @throws JspException in case the content could not be written to the page conext
     */
    protected void print(PageContext context, String content) throws JspException {

        if (content != null) {
            try {
                context.getOut().print(content);
            } catch (IOException e) {
                throw new JspException(e);
            }
        }
    }
}
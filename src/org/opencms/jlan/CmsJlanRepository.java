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

package org.opencms.jlan;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.file.wrapper.CmsSilentWrapperException;
import org.opencms.file.wrapper.I_CmsResourceWrapper;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.repository.CmsRepositoryFilter;
import org.opencms.repository.CmsRepositoryManager;
import org.opencms.repository.I_CmsRepository;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.filesys.DiskDeviceContext;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.TreeConnection;

import com.google.common.collect.Lists;

/**
 * Repository class for configuring repositories for Alfresco JLAN.<p>
 */
public class CmsJlanRepository implements I_CmsRepository {

    /** Parameter for controlling whether byte order marks should be added to plaintext files. */
    public static final String PARAM_ADD_BOM = "addBOM";

    /** The parameter for the project in which this repository should operate. */
    public static final String PARAM_PROJECT = "project";

    /** Name of the parameter to configure the root directory. */
    public static final String PARAM_ROOT = "root";

    /** Name of the parameter to configure resource wrappers. */
    public static final String PARAM_WRAPPER = "wrapper";

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJlanRepository.class);

    /** Flag which controls whether the CmsObjectWrapper should add byte order marks for plain files. */
    private boolean m_addByteOrderMark;

    /** The CMS context. */
    private CmsObject m_cms;

    /** The configuration for this repository. */
    private CmsParameterConfiguration m_configuration = new CmsParameterConfiguration();

    /** The shared disk device. */
    private DiskSharedDevice m_device;

    /** The JLAN device context for this repository. */
    private DiskDeviceContext m_deviceContext;

    /** The JLAN disk interface for this repository. */
    private DiskInterface m_diskInterface;

    /** The name of the repository. */
    private String m_name;

    /** The disk interface. */
    private CmsJlanDiskInterface m_originalDiskInterface;

    /** The configured project. */
    private CmsProject m_project;

    /** The name of the configured project. */
    private String m_projectName;

    /** The root VFS directory of the repository. */
    private String m_root;

    /** The list of wrappers configured for this repository. */
    private List<I_CmsResourceWrapper> m_wrappers = Lists.newArrayList();

    /**
     * Creates a new repository instance.<p>
     */
    public CmsJlanRepository() {

        m_deviceContext = new CmsJlanDeviceContext(this);
        m_deviceContext.enableChangeHandler(true);
        m_deviceContext.setFileServerNotifications(true);
        m_originalDiskInterface = new CmsJlanDiskInterface();
        m_diskInterface = createLoggingProxy(m_originalDiskInterface);
    }

    /**
     * Creates a dynamic proxy for a disk interface which logs the method calls and their results.<p>
     *
     * @param impl the disk interface for which a logging proxy should be created
     *
     * @return the dynamic proxy which logs methods calls
     */
    public static DiskInterface createLoggingProxy(final DiskInterface impl) {

        return (DiskInterface)Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[] {DiskInterface.class},
            new InvocationHandler() {

                @SuppressWarnings("synthetic-access")
                public Object invoke(Object target, Method method, Object[] params) throws Throwable {

                    // Just to be on the safe side performance-wise, we only log the parameters/result
                    // if the info channel is enabled
                    if (LOG.isInfoEnabled()) {
                        List<String> paramStrings = new ArrayList<String>();
                        for (Object param : params) {
                            paramStrings.add("" + param);
                        }
                        String paramsAsString = CmsStringUtil.listAsString(paramStrings, ", ");
                        LOG.info("Call: " + method.getName() + " " + paramsAsString);
                    }
                    try {
                        Object result = method.invoke(impl, params);
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Returned from " + method.getName() + ": " + result);
                        }
                        return result;
                    } catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if ((cause != null) && (cause instanceof CmsSilentWrapperException)) {
                            // not really an error
                            LOG.info(cause.getCause().getLocalizedMessage(), cause.getCause());
                        } else {
                            LOG.error(e.getLocalizedMessage(), e);
                        }
                        throw e.getCause();
                    }
                }
            });
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_configuration.add(paramName, paramValue);

    }

    /**
     * Checks if a user may access this repository.<p>
     *
     * @param user the name of the user
     *
     * @return true if the user may access the repository
     */
    public boolean allowAccess(String user) {

        try {
            return m_cms.getPermissions(m_root, user).requiresViewPermission();
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return true;
        }
    }

    /**
     * Creates a CmsObjectWrapper for the current session.<p>
     *
     * @param session the current session
     * @param connection the tree connection
     *
     * @return the correctly configured CmsObjectWrapper for this session
     *
     * @throws CmsException if something goes wrong
     */
    public CmsObjectWrapper getCms(SrvSession session, TreeConnection connection) throws CmsException {

        String userName = session.getClientInformation().getUserName();
        userName = CmsJlanUsers.translateUser(userName);
        CmsContextInfo contextInfo = new CmsContextInfo(m_cms.getRequestContext());
        contextInfo.setUserName(userName);
        CmsObject newCms = OpenCms.initCmsObject(m_cms, contextInfo);
        newCms.getRequestContext().setSiteRoot(getRoot());
        newCms.getRequestContext().setCurrentProject(getProject());
        CmsObjectWrapper result = new CmsObjectWrapper(newCms, getWrappers());
        result.setAddByteOrderMark(m_addByteOrderMark);
        result.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
        return result;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_configuration;
    }

    /**
     * Gets the device context for this repository.<p>
     *
     * @return the device context
     */
    public DiskDeviceContext getDeviceContext() {

        return m_deviceContext;
    }

    /**
     * Gets the disk interface for this repository.<p>
     *
     * @return the disk interface
     */
    public DiskInterface getDiskInterface() {

        return m_diskInterface;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#getFilter()
     */
    public CmsRepositoryFilter getFilter() {

        return null;
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * Gets the configured project.<p>
     *
     * @return the configured project
     */
    public CmsProject getProject() {

        return m_project;
    }

    /**
     * Gets the root directory configured for this repository.<p>
     *
     * @return the root directory
     */
    public String getRoot() {

        return m_root;
    }

    /**
     * Gets the resource wrappers which have been configured for this repository.<p>
     *
     * @return the resource wrappers which have been configured
     */
    public List<I_CmsResourceWrapper> getWrappers() {

        return m_wrappers;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() throws CmsConfigurationException {

        List<I_CmsResourceWrapper> wrapperObjects = CmsRepositoryManager.createResourceWrappersFromConfiguration(
            getConfiguration(),
            PARAM_WRAPPER,
            LOG);
        m_wrappers = Collections.unmodifiableList(wrapperObjects);
        m_root = getConfiguration().getString(PARAM_ROOT, "").trim();
        m_projectName = getConfiguration().getString(PARAM_PROJECT, "Offline").trim();
        String addByteOrderMarkStr = getConfiguration().getString(PARAM_ADD_BOM, "" + true).trim();
        m_addByteOrderMark = Boolean.parseBoolean(addByteOrderMarkStr);
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#initializeCms(org.opencms.file.CmsObject)
     */
    public void initializeCms(CmsObject cms) throws CmsException {

        m_cms = cms;
        m_project = m_cms.readProject(m_projectName);
        m_device = new DiskSharedDevice(getName(), getDiskInterface(), getDeviceContext(), 0);
        m_device.addAccessControl(new CmsRepositoryAccessControl(this));
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#setFilter(org.opencms.repository.CmsRepositoryFilter)
     */
    public void setFilter(CmsRepositoryFilter filter) {

        // do nothing
    }

    /**
     * @see org.opencms.repository.I_CmsRepository#setName(java.lang.String)
     */
    public void setName(String name) {

        // case sensitive share names don't work
        m_name = name.toUpperCase();
    }

    /**
     * Gets the shared device for this repository.<p>
     *
     * @return the shared device
     */
    DiskSharedDevice getSharedDevice() {

        return m_device;
    }

}

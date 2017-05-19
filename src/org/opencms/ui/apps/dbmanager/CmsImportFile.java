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

package org.opencms.ui.apps.dbmanager;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;

import java.util.List;

/**
 *
 */
public class CmsImportFile {

    /**Path of the file (on the server).*/
    private String m_path;

    /**Module in OpenCms.*/
    private CmsModule m_module;

    /**
     * public constructor.<p>
     *
     * @param path of the file
     */
    public CmsImportFile(String path) {
        m_path = path;
    }

    /**
     * Gets the module to import.<p>
     *
     * @return the CmsModule
     */
    public CmsModule getModule() {

        return m_module;
    }

    /**
     * Gets the path of the file.<p>
     *
     * @return the server path
     */
    public String getPath() {

        return m_path;
    }

    /**
     *
     *Class to load and validate import module file.<p>
     *
     * @throws CmsConfigurationException gets thrown when validation fails
     */
    public void loadAndValidate() throws CmsConfigurationException {

        CmsModule module = CmsModuleImportExportHandler.readModuleFromImport(m_path);
        m_module = module;
        List<CmsModuleDependency> dependencies = OpenCms.getModuleManager().checkDependencies(
            module,
            CmsModuleManager.DEPENDENCY_MODE_IMPORT);
        if (!dependencies.isEmpty()) {
            StringBuffer dep = new StringBuffer(32);
            for (int i = 0; i < dependencies.size(); i++) {
                CmsModuleDependency dependency = dependencies.get(i);
                dep.append("\n - ");
                dep.append(dependency.getName());
                dep.append(" (Version: ");
                dep.append(dependency.getVersion());
                dep.append(")");
            }
            //            throw new CmsConfigurationException(
            //                org.opencms.ui.apps.Messages.get().container(
            //                    org.opencms.ui.apps.Messages.ERR_MODULEMANAGER_ACTION_MODULE_DEPENDENCY_2,
            //                    m_path,
            //                    new String(dep)));
        }
    }

}

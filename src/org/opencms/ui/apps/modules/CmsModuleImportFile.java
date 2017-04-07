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

package org.opencms.ui.apps.modules;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.module.CmsModuleImportExportHandler;
import org.opencms.module.CmsModuleManager;

import java.util.List;

/**
 * A bean representing a module zip file to be imported.<p>
 */
public class CmsModuleImportFile {

    /** The module data read from the zip. */
    private CmsModule m_module;

    /** The path of the zip fiile. */
    private String m_path;

    /**
     * Creates a new instance.<p>
     *
     * @param path the path of the module zip file to import
     */
    public CmsModuleImportFile(String path) {
        m_path = path;
    }

    /**
     * Gets the module data.<p>
     *
     * @return the module data
     */
    public CmsModule getModule() {

        return m_module;
    }

    /**
     * Gets the path of the zip file.<p>
     *
     * @return the path of the zip file
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Loads the module data from the zip file and validates whether the module is importable, throwing an exception otherwise.<p>
     *
     * @throws CmsConfigurationException if the module is not importable for some reason
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
            throw new CmsConfigurationException(
                org.opencms.ui.apps.Messages.get().container(
                    org.opencms.ui.apps.Messages.ERR_MODULEMANAGER_ACTION_MODULE_DEPENDENCY_2,
                    m_path,
                    new String(dep)));
        }
    }

}

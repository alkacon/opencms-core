/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/Attic/CmsDependenciesDelete.java,v $
 * Date   : $Date: 2005/06/08 10:46:48 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.modules;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleDependency;
import org.opencms.security.CmsSecurityException;
import org.opencms.workplace.CmsDialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Class to delete a module dependencies.<p>
 * 
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.9.1
 */
public class CmsDependenciesDelete extends CmsDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "DependenciesDelete";

    /** Dependency name. */
    private String m_paramDependency;

    /** Modulename. */
    private String m_paramModule;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDependenciesDelete(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDependenciesDelete(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /** 
     * Commits the edited module.<p>
     */
    public void displayDialog() {

        // refresh the list
        Map objects = (Map)getSettings().getListObject();
        if (objects != null) {
            objects.remove(CmsModulesList.class.getName());
            objects.remove(CmsModulesDependenciesList.class.getName());
        }

        // get the correct module
        String moduleName = getParamModule();
        String dependencyName = getParamDependency();

        try {

            CmsModule module = (CmsModule)OpenCms.getModuleManager().getModule(moduleName).clone();
            // get the current dependencies from the module
            List oldDependencies = module.getDependencies();
            // now loop through the dependencies and create the new list of dependencies
            List newDependencies = new ArrayList();
            Iterator i = oldDependencies.iterator();
            while (i.hasNext()) {
                CmsModuleDependency dep = (CmsModuleDependency)i.next();
                if (!dep.getName().equals(dependencyName)) {
                    newDependencies.add(dep);
                }
            }
            // update the dependencies
            module.setDependencies(newDependencies);
            // update the module
            OpenCms.getModuleManager().updateModule(getCms(), module);
            actionCloseDialog();
        } catch (CmsConfigurationException ce) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_DEPENDENCIES_DELETE_2,
                dependencyName,
                moduleName), ce);
        } catch (CmsSecurityException se) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_DEPENDENCIES_DELETE_2,
                dependencyName,
                moduleName), se);
        } catch (JspException je) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_ACTION_DEPENDENCIES_DELETE_2,
                dependencyName,
                moduleName), je);
        }

    }

    /**
     * Gets the module dependency parameter.<p>
     * 
     * @return the module dependency parameter
     */
    public String getParamDependency() {

        return m_paramDependency;
    }

    /**
     * Gets the module parameter.<p>
     * 
     * @return the module parameter
     */
    public String getParamModule() {

        return m_paramModule;
    }

    /** 
     * Sets the module dependency parameter.<p>
     * @param paramDependency the module dependency parameter
     */
    public void setParamDependency(String paramDependency) {

        m_paramDependency = paramDependency;
    }

    /** 
     * Sets the module parameter.<p>
     * @param paramModule the module parameter
     */
    public void setParamModule(String paramModule) {

        m_paramModule = paramModule;
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

}

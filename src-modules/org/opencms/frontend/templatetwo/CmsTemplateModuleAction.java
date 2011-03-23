/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templatetwo/CmsTemplateModuleAction.java,v $
 * Date   : $Date: 2011/03/23 14:52:16 $
 * Version: $Revision: 1.6 $
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

package org.opencms.frontend.templatetwo;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;
import org.opencms.report.CmsLogReport;

import java.util.Iterator;
import java.util.Locale;

/**
 * Module action class used to enable an additional Editor CssHandler. <p>
 * 
 * @author Michael Emmerich
 * @author Michael Moossen
 * 
 * @version $Revision: 1.6 $
 * 
 * @since 7.0.4
 */
public class CmsTemplateModuleAction extends A_CmsModuleAction {

    /** Path to the configuration files of the release notes. */
    private static final String RELEASE_CONFIG = "/sites/default/release/_config_/";

    /** Name of the Template Two Editor CssHandler class. */
    private static final String TEMPLATETWO_EDITOR_CSS_HANDER = "org.opencms.frontend.templatetwo.CmsTemplateCssHandler";

    /**
     * Default constructor, nothing is really happening here.<p>
     */
    public CmsTemplateModuleAction() {

        // nop
    }

    /**
     * @see org.opencms.module.A_CmsModuleAction#initialize(org.opencms.file.CmsObject, org.opencms.configuration.CmsConfigurationManager, org.opencms.module.CmsModule)
     */
    @Override
    public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {

        super.initialize(adminCms, configurationManager, module);

        // get the workplace manager and add the new handler
        OpenCms.getWorkplaceManager().addEditorCssHandlerToHead(TEMPLATETWO_EDITOR_CSS_HANDER);

        // the following is needed to repair the release module in case it is installed before the template two module
        // since we do not want any dependencies (if tt is missing, use default template)
        final CmsObject cms = adminCms; // this is save since we got a new copy
        Thread thread = new Thread(new Runnable() {

            /**
             * @see java.lang.Runnable#run()
             */
            public void run() {

                synchronized (this) {
                    // wait until the resource types are available
                    try {
                        wait(5000);
                    } catch (Exception e) {
                        // ignore
                        e.printStackTrace();
                    }
                }

                // check if the configuration files exists
                if (!cms.existsResource(RELEASE_CONFIG)) {
                    // nothing to do
                    return;
                } else {
                    // check if they have the right type
                    try {
                        if (cms.readResource(RELEASE_CONFIG + "default").getTypeId() != CmsResourceTypePlain.getStaticTypeId()) {
                            // it is not plain, so assume this resource has the right type and all others too
                            return;
                        }
                    } catch (CmsException e) {
                        // something happens while reading the configuration file
                        e.printStackTrace();
                        return;
                    }
                }

                // create a new project for the changes
                String projectName = "ReleaseModuleRepair";
                CmsProject project = null;
                try {
                    // check if the project exists
                    project = cms.readProject(projectName);
                } catch (CmsException ex) {
                    // project does not exist, so create a new one
                    try {
                        project = cms.createProject(
                            projectName,
                            projectName,
                            OpenCms.getDefaultUsers().getGroupAdministrators(),
                            OpenCms.getDefaultUsers().getGroupAdministrators(),
                            CmsProject.PROJECT_TYPE_TEMPORARY);
                    } catch (CmsException e) {
                        // ignore
                        e.printStackTrace();
                    }
                }
                if (project == null) {
                    // error while creating the temp project
                    return;
                }

                // now switch to the temp project
                cms.getRequestContext().setCurrentProject(project);
                try {
                    // add the folder to the new live demo setup project
                    cms.copyResourceToProject(RELEASE_CONFIG);

                    // iterate the configuration folder
                    Iterator it = cms.readResources(RELEASE_CONFIG, CmsResourceFilter.DEFAULT, false).iterator();
                    while (it.hasNext()) {
                        CmsResource res = (CmsResource)it.next();
                        // get the right type for the given resource
                        int type = 0;
                        if (res.getName().equals("default")) {
                            type = OpenCms.getResourceManager().getResourceType("ttconfig").getTypeId();
                        } else if (res.getName().equals("options")) {
                            type = OpenCms.getResourceManager().getResourceType("ttoptions").getTypeId();
                        } else if (res.getName().equals("style")) {
                            type = OpenCms.getResourceManager().getResourceType("ttstyle").getTypeId();
                        } else if (res.getName().equals("twocolums")) {
                            type = OpenCms.getResourceManager().getResourceType("ttpreset").getTypeId();
                        }
                        if ((type == 0) || (type == res.getTypeId())) {
                            // skip if resource does not match or if it has already the right type
                            continue;
                        }
                        // repair the given resource
                        String resName = cms.getSitePath(res);
                        cms.lockResource(resName);
                        cms.chtype(resName, type);
                    }
                    // now publish the changes
                    CmsLogReport report = new CmsLogReport(Locale.ENGLISH, CmsTemplateModuleAction.class);
                    OpenCms.getPublishManager().publishProject(cms, report);
                    OpenCms.getPublishManager().waitWhileRunning();
                } catch (CmsException e) {
                    // ignore
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}

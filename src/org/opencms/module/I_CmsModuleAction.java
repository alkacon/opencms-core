/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/module/I_CmsModuleAction.java,v $
 * Date   : $Date: 2005/06/23 10:47:28 $
 * Version: $Revision: 1.8 $
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

package org.opencms.module;

import org.opencms.configuration.CmsConfigurationManager;
import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.main.I_CmsEventListener;
import org.opencms.report.I_CmsReport;

/**
 * Module action classes in OpenCms must implement this interface.<p>
 * 
 * A module action class allows to perform special functions on certain 
 * OpenCms lifecycle system events, like {@link #initialize(CmsObject, CmsConfigurationManager, CmsModule)} or 
 * {@link #shutDown(CmsModule)}.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.module.A_CmsModuleAction
 */
public interface I_CmsModuleAction extends I_CmsEventListener {

    /**
     * Will be called by the OpenCms system during server startup.<p>
     * 
     * If a module requires special initialization code, this
     * is a good place to to implement this functions.<p>
     * 
     * Moreover, if the module requires special "one time" setup code, 
     * this should also be implemented here. For example if the module
     * requires special DB tables to be created, you should implement 
     * a check if theses tables exist in this method, and if they don't 
     * exist create them as needed.<p> 
     * 
     * @param adminCms an initialized CmsObject with "Admin" permissions
     * @param configurationManager the initialized OpenCms configuration manager
     * @param module the module of this action instance
     */
    void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module);

    /**
     * Will be called if a module is uninstalled from an OpenCms system.<p>
     * 
     * If you require special code to be executed if a module is uninstalled,
     * implement it in this function.<p>
     * 
     * Please note that there is no <code>install()</code> method. 
     * This is because the class loader will not have the module class 
     * instance available after module installation/upload. If you 
     * need to execute setup/install code, do this in the {@link #initialize(CmsObject, CmsConfigurationManager, CmsModule)}
     * method during the next server startup.<p>
     * 
     * This method is <i>not</i> called if the module this action instance belongs to 
     * is "replaced". In this case {@link #moduleUpdate(CmsModule)} is called after the 
     * new version of the module is installed.<p> 
     * 
     * @param module the module of this action instance
     *
     * @see #initialize(CmsObject, CmsConfigurationManager, CmsModule)
     */
    void moduleUninstall(CmsModule module);

    /**
     * Will be called if the module this action instance belongs to is updated.<p>
     * 
     * @param module the module of this action instance with the updated values
     */
    void moduleUpdate(CmsModule module);

    /**
     * Will be called during a the publish process after the resources have been published,
     * but before the publish event is fired.<p>
     * 
     * If you require special code to be executed after a resource is published,
     * implement it in this function any analyze the publish list for "interesting" resources.<p>
     * 
     * @param cms the user context the publish was executed with
     * @param publishList the list of published resources
     * @param backupTagId the id of the backup tag
     * @param report the report to write messages to
     */
    void publishProject(CmsObject cms, CmsPublishList publishList, int backupTagId, I_CmsReport report);

    /**
     * Will be called by the OpenCms system during server shutdown.<p>
     * 
     * If a module requires special "clean up" functions,
     * for example removing temporary files, this is a good place
     * to implement this functions.<p> 
     * 
     * @param module the module of this action instance
     */
    void shutDown(CmsModule module);
}

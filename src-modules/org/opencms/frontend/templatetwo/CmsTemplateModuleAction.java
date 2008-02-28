/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templatetwo/CmsTemplateModuleAction.java,v $
 * Date   : $Date: 2008/02/28 10:39:04 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.OpenCms;
import org.opencms.module.A_CmsModuleAction;
import org.opencms.module.CmsModule;

/**
 * Module action class used to enable an additional Editor CssHandler. <p>
 * 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 7.0.4
 *
 */
public class CmsTemplateModuleAction extends A_CmsModuleAction {

    /** Name of the Template Two Editor CssHandler class.<p> */
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
    public void initialize(CmsObject adminCms, CmsConfigurationManager configurationManager, CmsModule module) {

        super.initialize(adminCms, configurationManager, module);

        // get the workplace manager and add the new handler
        OpenCms.getWorkplaceManager().addEditorCssHandlerToHead(TEMPLATETWO_EDITOR_CSS_HANDER);

    }

}

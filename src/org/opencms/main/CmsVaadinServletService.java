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

package org.opencms.main;

import java.io.IOException;
import java.io.InputStream;

import com.vaadin.server.Constants;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.ui.UI;

/**
 * A custom servlet service implementation.<p>
 */
public class CmsVaadinServletService extends VaadinServletService {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.<p>
     *
     * @param servlet the servlet instance
     * @param deploymentConfiguration the deployment configuration
     *
     * @throws ServiceException if something goes wrong
     */
    public CmsVaadinServletService(VaadinServlet servlet, DeploymentConfiguration deploymentConfiguration)
    throws ServiceException {
        super(servlet, deploymentConfiguration);

    }

    /**
     * @see com.vaadin.server.VaadinServletService#getThemeResourceAsStream(com.vaadin.ui.UI, java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getThemeResourceAsStream(UI uI, String themeName, String resource) throws IOException {

        String resourcePath = Constants.THEME_DIR_PATH + '/' + themeName + "/" + resource;
        InputStream result = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (result != null) {
            return result;
        } else {
            return super.getThemeResourceAsStream(uI, themeName, resource);
        }
    }

}

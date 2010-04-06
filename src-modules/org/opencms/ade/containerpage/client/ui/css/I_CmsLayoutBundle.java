/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/client/ui/css/Attic/I_CmsLayoutBundle.java,v $
 * Date   : $Date: 2010/04/06 09:49:44 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.containerpage.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;

public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsImageBundle {

    public I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    public interface I_CmsContainerpageCss extends CssResource {

        String saveButton();

        String publishButton();

        String selectionButton();

        String editButton();

        String moveButton();

        String removeButton();

        String propertiesButton();

        String addButton();

        String clipboardButton();

        String extrasButton();

        String sitemapButton();

        String resetButton();

        String optionBar();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarAdd();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarClipboard();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarEdit();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarExit();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarMove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarNew();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarProperties();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarPublish();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRecent();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRemove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSave();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSitemap();
    }

    @Source("containerpage.css")
    I_CmsContainerpageCss containerpageCss();
}

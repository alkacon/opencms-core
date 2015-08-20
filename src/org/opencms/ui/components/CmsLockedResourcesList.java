/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.ui.CmsVaadinUtils;

import java.util.List;

import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;

public class CmsLockedResourcesList extends CmsBasicDialog {

    private CmsFileTable m_table = new CmsFileTable();
    private VerticalLayout m_layout = new VerticalLayout();

    public CmsLockedResourcesList(CmsObject cms, List<CmsResource> resources, Runnable nextAction) {
        Button okButton = new Button(CmsVaadinUtils.messageOk());
        okButton.addClickListener(CmsVaadinUtils.createClickListener(nextAction));
        addButton(okButton);
        displayResourceInfo(resources);
    }
}

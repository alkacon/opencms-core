/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templatetwo/CmsPresetSelectWidget.java,v $
 * Date   : $Date: 2011/03/23 14:52:15 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Displays all existing presets in a select widget.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 7.0.4
 */
public class CmsPresetSelectWidget extends CmsSelectWidget {

    /** The resource type id of the preset. */
    public static final int PRESET_TYPE_ID = 70;

    /**
     * Creates a new select site widget.<p>
     */
    public CmsPresetSelectWidget() {

        super("");
    }

    /**
     * Creates a select widget with the configuration.<p>
     * 
     * @param configuration the configuration (possible options) for the select box
     */
    public CmsPresetSelectWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.I_CmsWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsPresetSelectWidget(getConfiguration());
    }

    /**
     *@see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(CmsObject, I_CmsWidgetDialog, I_CmsWidgetParameter)
     */
    @Override
    protected List parseSelectOptions(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        List result = new ArrayList();

        try {
            List resources = cms.readResources("/", CmsResourceFilter.DEFAULT.addRequireType(PRESET_TYPE_ID), true);
            Iterator iter = resources.iterator();
            while (iter.hasNext()) {
                CmsResource res = (CmsResource)iter.next();

                String title = cms.readPropertyObject(res, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue();
                result.add(new CmsSelectWidgetOption(cms.getSitePath(res), false, title, ""));
            }
        } catch (CmsException e) {
            // noop
        }

        return result;
    }
}

/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templatetwo/CmsCollectorSelectWidget.java,v $
 * Date   : $Date: 2011/03/23 14:52:16 $
 * Version: $Revision: 1.6 $
 *
 * This file is part of the Alkacon OpenCms Add-On Module Package
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * The Alkacon OpenCms Add-On Module Package is free software: 
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Alkacon OpenCms Add-On Module Package is distributed 
 * in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the Alkacon OpenCms Add-On Module Package.  
 * If not, see http://www.gnu.org/licenses/.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com.
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org.
 */

package org.opencms.frontend.templatetwo;

import org.opencms.file.CmsObject;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsSelectWidget;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.widgets.I_CmsWidgetDialog;
import org.opencms.widgets.I_CmsWidgetParameter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a select widget that contains all configured collectors.<p>
 * 
 * @author Alexander Kandzior 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.6 $ 
 */
public class CmsCollectorSelectWidget extends CmsSelectWidget {

    /**
     * Creates a new instance of the feed select widget.<p>
     */
    public CmsCollectorSelectWidget() {

        super();
    }

    /**
     * Creates a new instance of the feed select widget.<p>
     * 
     * @param configuration the widget configuration
     */
    public CmsCollectorSelectWidget(List configuration) {

        super(configuration);

    }

    /**
     * Creates a new instance of the feed select widget.<p>
     * 
     * @param configuration the widget configuration
     */
    public CmsCollectorSelectWidget(String configuration) {

        super(configuration);
    }

    /**
     * @see org.opencms.widgets.CmsSelectWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsCollectorSelectWidget(getConfiguration());
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#getSelectOptions()
     */
    @Override
    protected List getSelectOptions() {

        // for the test case this method needs to be in the feed package
        return super.getSelectOptions();
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List parseSelectOptions(CmsObject cms, I_CmsWidgetDialog widgetDialog, I_CmsWidgetParameter param) {

        if (getSelectOptions() == null) {

            List options = new ArrayList();
            // we want to get the list of configured resource collectors
            Iterator i = OpenCms.getResourceManager().getRegisteredContentCollectors().iterator();
            while (i.hasNext()) {
                // loop over all collectors and add all collector names
                I_CmsResourceCollector collector = (I_CmsResourceCollector)i.next();
                Iterator j = collector.getCollectorNames().iterator();
                while (j.hasNext()) {
                    String name = (String)j.next();
                    // make "allInFolder" the default setting
                    boolean isDefault = "allInFolder".equals(name);
                    CmsSelectWidgetOption option = new CmsSelectWidgetOption(name, isDefault);
                    options.add(option);
                }
            }
            setSelectOptions(options);
        }
        return getSelectOptions();
    }
}
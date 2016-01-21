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

package org.opencms.widgets;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.file.CmsObject;
import org.opencms.main.OpenCms;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.types.CmsXmlDisplayFormatterValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget to select a type and formatter combination.<p>
 */
public class CmsDisplayTypeSelectWidget extends CmsSelectWidget {

    /**
     * @see org.opencms.widgets.CmsSelectWidget#newInstance()
     */
    @Override
    public I_CmsWidget newInstance() {

        return new CmsDisplayTypeSelectWidget();
    }

    /**
     * @see org.opencms.widgets.A_CmsSelectWidget#parseSelectOptions(org.opencms.file.CmsObject, org.opencms.widgets.I_CmsWidgetDialog, org.opencms.widgets.I_CmsWidgetParameter)
     */
    @Override
    protected List<CmsSelectWidgetOption> parseSelectOptions(
        CmsObject cms,
        I_CmsWidgetDialog widgetDialog,
        I_CmsWidgetParameter param) {

        List<CmsSelectWidgetOption> result = new ArrayList<CmsSelectWidgetOption>();
        CmsADEConfigData config = OpenCms.getADEManager().lookupConfiguration(cms, getResourcePath(cms, widgetDialog));
        if (config != null) {
            for (I_CmsFormatterBean formatter : config.getDisplayFormatters(cms)) {
                for (String typeName : formatter.getResourceTypeNames()) {
                    result.add(
                        new CmsSelectWidgetOption(
                            typeName + CmsXmlDisplayFormatterValue.SEPARATOR + formatter.getId(),
                            false,
                            typeName + ": " + formatter.getNiceName()));
                }
            }
        }
        return result;
    }
}

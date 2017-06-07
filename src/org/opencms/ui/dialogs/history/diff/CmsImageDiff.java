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

package org.opencms.ui.dialogs.history.diff;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.gwt.shared.CmsHistoryResourceBean;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsRequestUtil;
import org.opencms.workplace.comparison.CmsHistoryListUtil;

import com.google.common.base.Optional;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Panel;

/**
 * Displays two image versions side by side, scaled.<p>
 */
public class CmsImageDiff implements I_CmsDiffProvider {

    /**
     * @see org.opencms.ui.dialogs.history.diff.I_CmsDiffProvider#diff(org.opencms.file.CmsObject, org.opencms.gwt.shared.CmsHistoryResourceBean, org.opencms.gwt.shared.CmsHistoryResourceBean)
     */
    public Optional<Component> diff(CmsObject cms, CmsHistoryResourceBean v1, CmsHistoryResourceBean v2)
    throws CmsException {

        CmsResource r1 = A_CmsAttributeDiff.readResource(cms, v1);
        if (OpenCms.getResourceManager().matchResourceType(CmsResourceTypeImage.getStaticTypeName(), r1.getTypeId())) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            String v1Param = v1.getVersion().getVersionNumber() != null
            ? "" + v1.getVersion().getVersionNumber()
            : "" + CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION;
            String v2Param = v2.getVersion().getVersionNumber() != null
            ? "" + v2.getVersion().getVersionNumber()
            : "" + CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION;

            String link1 = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                CmsHistoryListUtil.getHistoryLink(cms, v1.getStructureId(), v1Param));
            String link2 = OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                cms,
                CmsHistoryListUtil.getHistoryLink(cms, v2.getStructureId(), v2Param));
            int scaleWidth = 400;
            int scaleHeight = (2 * scaleWidth) / 3;
            final String scaleParams = "w:" + scaleWidth + ",h:" + scaleHeight + ",t:1"; // scale type 1 for thumbnails (no enlargement)
            link1 = CmsRequestUtil.appendParameter(link1, "__scale", scaleParams);
            link2 = CmsRequestUtil.appendParameter(link2, "__scale", scaleParams);
            Image img1 = new Image("", new ExternalResource(link1));
            Image img2 = new Image("", new ExternalResource(link2));
            for (Image img : new Image[] {img1, img2}) {
                img.setWidth("" + scaleWidth + "px");
            }
            img1.setCaption("V1");
            img2.setCaption("V2");
            hl.addComponent(img1);
            hl.addComponent(img2);
            Panel result = new Panel("Image comparison");
            hl.setMargin(true);
            result.setContent(hl);
            return Optional.fromNullable((Component)result);
        } else {
            return Optional.absent();
        }

    }

}

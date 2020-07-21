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

package org.opencms.ui.client;

import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.ui.components.CmsRichTextArea;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style;
import com.vaadin.client.ui.VRichTextArea;
import com.vaadin.client.ui.richtextarea.RichTextAreaConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.shared.ui.Connect.LoadStyle;

/**
 * Special connector for CmsRichTextArea which adds some styles to the iframe body used in the editor.
 */
@Connect(value = CmsRichTextArea.class, loadStyle = LoadStyle.LAZY)
public class CmsRichTextAreaConnector extends RichTextAreaConnector {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * @see com.vaadin.client.ui.AbstractComponentConnector#createWidget()
     */
    @Override
    public VRichTextArea createWidget() {

        VRichTextArea result = (VRichTextArea)super.createWidget();
        // We can't just add the style here, we have to wait until the iframe is loaded
        result.rta.addInitializeHandler(event -> {
            Element element = result.getElement();
            IFrameElement iframeElement = IFrameElement.as(
                CmsDomUtil.nodeListToList(element.getElementsByTagName("iframe")).get(0));
            String[][] styles = new String[][] {
                {"lineHeight", "1.55"},
                {"fontSize", "14px"},
                {"fontFamily", "\"Open sans\", sans-serif"}};
            Style styleObj = iframeElement.getContentDocument().getBody().getStyle();
            for (String[] styleEntry : styles) {
                styleObj.setProperty(styleEntry[0], styleEntry[1]);
            }
        });
        return result;
    }

}

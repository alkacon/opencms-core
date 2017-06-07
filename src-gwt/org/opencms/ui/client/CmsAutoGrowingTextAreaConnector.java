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

import org.opencms.ui.components.extensions.CmsAutoGrowingTextArea;
import org.opencms.ui.shared.components.CmsAutoGrowingTextAreaState;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.shared.ui.Connect;

/**
 * This connector will manipulate the CSS classes of the extended widget depending on the scroll position.<p>
 */
@Connect(CmsAutoGrowingTextArea.class)
public class CmsAutoGrowingTextAreaConnector extends AbstractExtensionConnector {

    /** The serial version id. */
    private static final long serialVersionUID = -9079215265941920364L;

    /** The widget to enhance. */
    private Widget m_widget;

    /** The line height. */
    private int m_lineHeight;

    /** The padding height. */
    private int m_paddingHeight;

    /** The last value. */
    private String m_lastValue = "";

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsAutoGrowingTextAreaState getState() {

        return (CmsAutoGrowingTextAreaState)super.getState();
    }

    /**
     * @see com.vaadin.client.extensions.AbstractExtensionConnector#extend(com.vaadin.client.ServerConnector)
     */
    @Override
    protected void extend(ServerConnector target) {

        // Get the extended widget
        m_widget = ((ComponentConnector)target).getWidget();
        m_widget.addDomHandler(new KeyUpHandler() {

            public void onKeyUp(KeyUpEvent event) {

                handle();

            }
        }, KeyUpEvent.getType());

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                handle();
            }
        });

    }

    /**
     * Resize the text area.<p>
     */
    protected void handle() {

        Element e = m_widget.getElement();
        if (e instanceof TextAreaElement) {
            TextAreaElement elem = (TextAreaElement)e;
            int scrollHeight = elem.getScrollHeight();
            // allow text area to shrink
            if (m_lastValue.length() > elem.getValue().length()) {
                elem.setRows(getState().getMinRows());
            }
            int currentRows = elem.getRows();
            if (m_lineHeight == 0) {
                elem.setRows(2);
                int heightTwo = elem.getClientHeight();
                elem.setRows(1);
                int heightOne = elem.getClientHeight();
                m_lineHeight = heightTwo - heightOne;
                m_paddingHeight = heightOne - m_lineHeight;
                elem.setRows(currentRows);
            }
            if (m_lineHeight > 0) {
                int totalHeight = scrollHeight - m_paddingHeight;
                int requiredRows = ((scrollHeight - m_paddingHeight) / m_lineHeight);
                if ((totalHeight % m_lineHeight) > 0) {
                    requiredRows++;
                }
                int minRows = getState().getMinRows();
                int maxRows = getState().getMaxRows();
                if ((requiredRows <= minRows) && (currentRows != minRows)) {
                    elem.setRows(minRows);
                } else if ((requiredRows >= maxRows) && (currentRows != maxRows)) {
                    elem.setRows(maxRows);
                } else if (requiredRows != currentRows) {
                    elem.setRows(requiredRows);
                }
            }
        }
    }
}

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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.CmsPopup;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.Frame;

/**
 * This class represents a popup which displays an IFrame.<p>
 *
 * It also exports a Javascript function to close the popup when given the id of the popup.<p>
 *
 * @since 8.0.0
 */
public class CmsFramePopup extends CmsPopup {

    /** The handler which is called when the popup closes itself. */
    protected Runnable m_closeHandler;

    /** The iframe for the popup content. */
    Frame m_frame;

    /** The id of this popup. */
    private String m_id;

    /**
     * Constructor.<p>
     *
     * @param title the title of the popup dialog
     * @param url the URL which should be opened in the popup
     */

    public CmsFramePopup(String title, String url) {

        super(title);
        m_frame = new Frame();
        add(m_frame);
        m_frame.setUrl(url);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        exportCloseFunction();
        super.center();
    }

    /**
     * Returns the frame contained in this popup.<p>
     *
     * @return a frame
     */
    public Frame getFrame() {

        return m_frame;
    }

    /**
     * Hide the popup, but only after the current event has been processed.<p>
     */
    public void hideDelayed() {

        // The reason for using this function, instead of calling hide directly, is that
        // the latter leads to harmless but annoying Javascript errors when called from
        // Javascript inside the IFrame, since the IFrame is closed before the function returns.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                if (m_closeHandler != null) {
                    m_closeHandler.run();
                }

                hide();
            }
        });
    }

    /**
     * Sets the handler which should be called when the popup closes itself.<p>
     *
     * @param closeHandler the "close" handler
     */
    public void setCloseHandler(Runnable closeHandler) {

        m_closeHandler = closeHandler;
    }

    /**
     * Sets the id of this IFrame popup.<p>
     *
     * The popup can be closed by calling the cmsCloseDialog Javascript function with the same id as a parameter.<p>
     *
     * @param id the new id
     */
    public void setId(String id) {

        m_id = id;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        exportCloseFunction();
        super.show();
    }

    /**
     * Exports a Javascript function 'cmsCloseDialog', which, when passed the id of a CmsFramePopup as a parameter, will close that dialog.<p>
     */
    protected native void exportCloseFunction() /*-{
                                                var w = $wnd;
                                                w.CmsFramePopup_instances = w.CmsFramePopup_instances || {};
                                                // register the current instance under its id
                                                w.CmsFramePopup_instances[this.@org.opencms.gwt.client.ui.input.CmsFramePopup::m_id] = this;
                                                if (!w.cmsCloseDialog) {
                                                w.cmsCloseDialog = function(arg) {
                                                var instance = w.CmsFramePopup_instances[arg];
                                                instance.@org.opencms.gwt.client.ui.input.CmsFramePopup::hideDelayed()();
                                                // remove current instance
                                                delete w.CmsFramePopup_instances[this.@org.opencms.gwt.client.ui.input.CmsFramePopup::m_id];
                                                } // cmsCloseDialog
                                                } // if
                                                }-*/;

    /**
     * test.<p>
     */
    protected native void setGroupFormValue() /*-{
                                              var w = $wnd;
                                              w.CmsFramePopup_instances = w.CmsFramePopup_instances || {};
                                              // register the current instance under its id
                                              w.CmsFramePopup_instances[this.@org.opencms.gwt.client.ui.input.CmsFramePopup::m_id] = this;
                                              if (!w.cmsCloseDialog) {
                                              w.cmsCloseDialog = function(arg) {
                                              var instance = w.CmsFramePopup_instances[arg];
                                              instance.@org.opencms.gwt.client.ui.input.CmsFramePopup::hideDelayed()();
                                              // remove current instance
                                              delete w.CmsFramePopup_instances[this.@org.opencms.gwt.client.ui.input.CmsFramePopup::m_id];
                                              } // cmsCloseDialog
                                              } // if
                                              }-*/;

}

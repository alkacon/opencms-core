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

package org.opencms.gwt.client.util;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsIFrame;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;

import jsinterop.annotations.JsType;

/**
 * Singleton that creates and manages access to the shared iframe used for embedded Vaadin dialogs, mainly in the page editor.
 */
@JsType
public class CmsEmbeddedDialogFrame {

    /** The singleton instance. */
    private static CmsEmbeddedDialogFrame m_instance = new CmsEmbeddedDialogFrame();

    /** The loader used to load dialogs - this is set by the Javascript code in the iframe. */
    private I_CmsEmbeddedDialogLoader m_loader;

    /** The actual iframe. */
    private CmsIFrame m_frame;

    /**
     * Hidden default constructor.
     */
    protected CmsEmbeddedDialogFrame() {

        // do nothing
    }

    /**
     * Gets the singleton instance.
     *
     * @return the instance
     */
    public static CmsEmbeddedDialogFrame get() {

        return m_instance;
    }

    /**
     * Hides the iframe.
     */
    public void hide() {

        if (m_frame != null) {
            setFrameVisible(false);
        }

    }

    /**
     * Sets the dialog loader.
     *
     * <p>This is called by the Javascript code in the iframe.
     *
     * @param loader the class used to load dialogs in the iframe itself
     */
    public void installEmbeddedDialogLoader(I_CmsEmbeddedDialogLoader loader) {

        m_loader = loader;
    }

    /**
     * Triggers loading of a new dialog in the iframe.
     *
     * <p>If the iframe has not been created/initialized yet, this will trigger the initialization and load the dialog afterwards.
     *
     * @param dialogInfoJson the serialized dialog info JSON from an I_CmsEmbeddedDialogInfo bean
     * @param handler the embedded dialog handler
     */
    public void loadDialog(String dialogInfoJson, CmsEmbeddedDialogHandler handler) {

        waitUntilReady(() -> {
            setDialogHandlerCallbacks(handler);
            setFrameVisible(true);
            m_loader.loadDialog(dialogInfoJson);
        });
    }

    /**
     * Triggers initialization of the iframe.
     */
    public void preload() {

        waitUntilReady(() -> {/*do nothing*/});
    }

    /**
     * Initializes the iframe if it isn't  already, and executes the given action afterwards.
     *
     * @param action the action to execute after the iframe is initialized
     */
    protected void waitUntilReady(Runnable action) {

        if (m_loader != null) {
            action.run();
            return;
        }
        if (m_frame == null) {
            String url = CmsCoreProvider.get().getEmbeddedDialogsUrl();
            m_frame = new CmsIFrame("embeddedDialogFrame", url);
            m_frame.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().embeddedDialogFrame());
            setFrameVisible(false);
            RootPanel.get().add(m_frame);
        }
        Timer timer = new Timer() {

            @Override
            public void run() {

                waitUntilReady(action);
            }
        };
        timer.schedule(100);
    }

    /**
     * Sets the dialog handler callbacks in the iframe context.
     *
     * @param handler the handler to set
     */
    private native void setDialogHandlerCallbacks(CmsEmbeddedDialogHandler handler) /*-{
        $wnd.frames.embeddedDialogFrame.connector = handler;
    }-*/;

    private void setFrameVisible(boolean visible) {

        if (m_frame != null) {
            String className = I_CmsLayoutBundle.INSTANCE.dialogCss().embeddedDialogFrameHidden();
            if (visible) {
                m_frame.removeStyleName(className);
            } else {
                m_frame.addStyleName(className);
            }
        }
    }

}

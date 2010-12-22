/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/Attic/A_CmsEntryPoint.java,v $
 * Date   : $Date: 2010/12/22 09:01:11 $
 * Version: $Revision: 1.27 $
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

package org.opencms.gwt.client;

import org.opencms.gwt.client.rpc.CmsLog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * Handles exception handling and more for entry points.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.27 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogServiceAsync
 */
public abstract class A_CmsEntryPoint implements EntryPoint {

    /** Flag which indicates whether initClasses() has already been called. */
    private static boolean initializedClasses;

    /**
     * Default constructor.<p> 
     */
    protected A_CmsEntryPoint() {

        // just for subclassing
    }

    /**
     * @see com.google.gwt.core.client.EntryPoint#onModuleLoad()
     */
    public void onModuleLoad() {

        enableRemoteExceptionHandler();

        I_CmsLayoutBundle.INSTANCE.buttonCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.contextmenuCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.dialogCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.notificationCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.dragdropCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.floatDecoratedPanelCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.generalCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.headerCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.highlightCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.iconsCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listTreeCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.stateCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.tabbedPanelCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.toolbarCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.listItemCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.availabilityCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.fieldsetCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.resourceStateCss().ensureInjected();
        I_CmsLayoutBundle.INSTANCE.dateBoxCss().ensureInjected();

        I_CmsInputLayoutBundle.INSTANCE.inputCss().ensureInjected();

        I_CmsImageBundle.INSTANCE.style().ensureInjected();
        I_CmsImageBundle.INSTANCE.contextMenuIcons().ensureInjected();

        I_CmsInputLayoutBundle.INSTANCE.inputCss().ensureInjected();

        I_CmsToolbarButtonLayoutBundle.INSTANCE.style().ensureInjected();
        I_CmsToolbarButtonLayoutBundle.INSTANCE.toolbarButtonCss().ensureInjected();

        initClasses();

    }

    /**
     * Enables client exception logging on the server.<p>
     */
    protected void enableRemoteExceptionHandler() {

        if (!GWT.isScript()) {
            // In hosted mode, uncaught exceptions are easier to debug 
            return;
        }

        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

            /**
             * @see com.google.gwt.core.client.GWT.UncaughtExceptionHandler#onUncaughtException(java.lang.Throwable)
             */
            public void onUncaughtException(Throwable t) {

                String message = CmsClientStringUtil.getMessage(t);
                CmsNotification.get().send(CmsNotification.Type.WARNING, message);
                CmsLog.log(message + "\n" + CmsClientStringUtil.getStackTrace(t, "\n"));
            }
        });
    }

    /**
     * Helper method for initializing the classes implementing {@link I_CmsHasInit}.<p>
     * 
     * Calling this method more than once will have no effect.<p>
     */
    private void initClasses() {

        if (!initializedClasses) {
            I_CmsClassInitializer initializer = GWT.create(I_CmsClassInitializer.class);
            initializer.initClasses();
            initializedClasses = true;
        }
    }
}

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

package org.opencms.ade.contenteditor.widgetregistry.client;

import org.opencms.acacia.client.I_CmsEntityRenderer;
import org.opencms.acacia.client.I_CmsWidgetFactory;
import org.opencms.ade.contenteditor.shared.CmsExternalWidgetConfiguration;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.Command;

/**
 * The widget registry.<p>
 */
public final class WidgetRegistry {

    /** The register widget function name. */
    public static final String REGISTER_WIDGET_FACTORY_FUNCTION = "registerWidgetFactory";

    /** The widget registry instance. */
    private static WidgetRegistry INSTANCE;

    /** Map of registered renderers by name. */
    private Map<String, I_CmsEntityRenderer> m_renderers = new HashMap<String, I_CmsEntityRenderer>();

    /** The widget registry. */
    private Map<String, I_CmsWidgetFactory> m_widgetRegistry;

    /**
     * Constructor.<p>
     */
    private WidgetRegistry() {

        m_widgetRegistry = new HashMap<String, I_CmsWidgetFactory>();
        exportWidgetRegistration();
    }

    /**
     * Returns the widget registry instance.<p>
     *
     * @return the widget registry instance
     */
    public static WidgetRegistry getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new WidgetRegistry();
        }
        return INSTANCE;
    }

    /**
     * Adds a renderer which should be used by the Acacia editor.<p>
     *
     * @param renderer the renderer to add
     */
    public void addRenderer(I_CmsEntityRenderer renderer) {

        m_renderers.put(renderer.getName(), renderer);
    }

    /**
     * Returns the registered renderers.<p>
     *
     * @return the renderers
     */
    public Collection<I_CmsEntityRenderer> getRenderers() {

        return m_renderers.values();
    }

    /**
     * Returns the registered widget factories.<p>
     *
     * @return the registered widget factories
     */
    public Map<String, I_CmsWidgetFactory> getWidgetFactories() {

        return m_widgetRegistry;
    }

    /**
     * Registers external widgets.<p>
     *
     * @param externalWidgetConfigurations the external widget configurations
     * @param callback the callback to execute when done
     */
    public void registerExternalWidgets(
        List<CmsExternalWidgetConfiguration> externalWidgetConfigurations,
        final Command callback) {

        final Set<String> initCalls = new HashSet<String>();
        for (CmsExternalWidgetConfiguration widgetConfiguration : externalWidgetConfigurations) {
            if (!m_widgetRegistry.containsKey(widgetConfiguration.getWidgetName())) {
                for (String cssResource : widgetConfiguration.getCssResourceLinks()) {
                    CmsDomUtil.ensureStyleSheetIncluded(cssResource);
                }
                for (String javaScriptResource : widgetConfiguration.getJavaScriptResourceLinks()) {
                    CmsDomUtil.ensureJavaScriptIncluded(javaScriptResource);
                }
                String initCall = widgetConfiguration.getInitCall();
                if (initCall != null) {
                    initCalls.add(initCall);
                }
            }
        }
        if (initCalls.isEmpty()) {
            callback.execute();
        } else {
            Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {

                /** The number of repeats. */
                private int m_repeats;

                /**
                 * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
                 */
                public boolean execute() {

                    m_repeats++;
                    Iterator<String> it = initCalls.iterator();
                    while (it.hasNext()) {
                        String initCall = it.next();
                        if (tryInitCall(initCall)) {
                            it.remove();
                        }
                    }
                    if (initCalls.isEmpty()) {
                        callback.execute();
                        return false;
                    } else {
                        if (m_repeats < 100) {
                            return true;
                        } else {
                            showInitCallError(initCalls);
                            return false;
                        }
                    }

                }
            }, 50);
        }
    }

    /**
     * Registers a widget.<p>
     *
     * @param widgetName the widget name
     * @param widgetFactory the widget
     */
    public void registerWidgetFactory(String widgetName, I_CmsWidgetFactory widgetFactory) {

        m_widgetRegistry.put(widgetName, widgetFactory);
    }

    /**
     * Logs an error.<p>
     *
     * @param error the error to log
     */
    protected native void showError(String error) /*-{
                                                  if ($wnd.console) {
                                                  $wnd.console.log(error);
                                                  }
                                                  throw error;
                                                  }-*/;

    /**
     * Logs an error for missing init calls.<p>
     *
     * @param initCalls the set of missing init calls
     */
    protected void showInitCallError(Set<String> initCalls) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("init call(s) not found: ");
        for (String init : initCalls) {
            buffer.append(init);
            buffer.append(" ");
        }
        showError(buffer.toString());
    }

    /**
     * Tries to initializes a widget with the given initialization call. Returns false if the init method was not available within the window context yet.<p>
     *
     * @param initCall the initialization function name
     *
     * @return <code>true</code> if the initialization function was available and has been executed
     */
    protected native boolean tryInitCall(String initCall)/*-{
                                                         try {
                                                         if ($wnd[initCall]) {
                                                         $wnd[initCall]();
                                                         return true;
                                                         }
                                                         } catch (error) {
                                                         throw "Failed excuting " + initCall
                                                         + " to initialize editing widget. \n" + error
                                                         }
                                                         return false;
                                                         }-*/;

    /**
     * Exports the widget registration.<p>
     */
    private native void exportWidgetRegistration() /*-{
                                                   var self = this;
                                                   $wnd[@org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry::REGISTER_WIDGET_FACTORY_FUNCTION] = function(
                                                   factory) {
                                                   self.@org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry::registerWrapper(Lorg/opencms/ade/contenteditor/widgetregistry/client/WidgetFactoryWrapper;)(factory);
                                                   }
                                                   }-*/;

    /**
     * Registers a widget wrapper as widget.<p>
     *
     * @param wrapper the wrapper object
     */
    private void registerWrapper(WidgetFactoryWrapper wrapper) {

        registerWidgetFactory(wrapper.getName(), wrapper);
    }
}

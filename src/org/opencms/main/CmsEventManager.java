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

package org.opencms.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Manager that controls the OpenCms event system.
 *
 * There is only one instance of this event manager class used by the OpenCms runtime.
 * This instance can be obtained by calling {@link OpenCms#getEventManager()}.<p>
 *
 * Events can be used in OpenCms to notify custom event listeners that certain system events have happened.
 * Event listeners have to implement the interface {@link org.opencms.main.I_CmsEventListener}.<p>
 *
 * @since 7.0.0
 *
 * @see org.opencms.main.CmsEvent
 * @see org.opencms.main.I_CmsEventListener
 */
public class CmsEventManager {

    /** Required as template for event list generation. */
    protected static final I_CmsEventListener[] EVENT_LIST = new I_CmsEventListener[0];

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsEventManager.class);

    /** Stores the active event listeners. */
    private Map<Integer, List<I_CmsEventListener>> m_eventListeners;

    /**
     * Create a new instance of an OpenCms event manager.<p>
     */
    public CmsEventManager() {

        m_eventListeners = new HashMap<Integer, List<I_CmsEventListener>>();
    }

    /**
     * Add an OpenCms event listener that listens to all events.<p>
     *
     * @param listener the listener to add
     */
    public void addCmsEventListener(I_CmsEventListener listener) {

        addCmsEventListener(listener, null);
    }

    /**
     * Add an OpenCms event listener.<p>
     *
     * @param listener the listener to add
     * @param eventTypes the events to listen for
     */
    public void addCmsEventListener(I_CmsEventListener listener, int[] eventTypes) {

        synchronized (m_eventListeners) {
            if (eventTypes == null) {
                // no event types given - register the listener for all event types
                eventTypes = new int[] {I_CmsEventListener.LISTENERS_FOR_ALL_EVENTS.intValue()};
            }
            for (int i = 0; i < eventTypes.length; i++) {
                // register the listener for all configured event types
                Integer eventType = Integer.valueOf(eventTypes[i]);
                List<I_CmsEventListener> listeners = m_eventListeners.get(eventType);
                if (listeners == null) {
                    listeners = new ArrayList<I_CmsEventListener>();
                    m_eventListeners.put(eventType, listeners);
                }
                if (!listeners.contains(listener)) {
                    // add listerner only if it is not already registered
                    listeners.add(listener);
                }
            }
        }
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     *
     * @param event the event that is forwarded to all listeners
     */
    public void fireEvent(CmsEvent event) {

        fireEventHandler(m_eventListeners.get(event.getTypeInteger()), event);
        fireEventHandler(m_eventListeners.get(I_CmsEventListener.LISTENERS_FOR_ALL_EVENTS), event);
    }

    /**
     * Notify all event listeners that a particular event has occurred without any additional event data.<p>
     *
     * @param type event type
     */
    public void fireEvent(int type) {

        fireEvent(type, new HashMap<String, Object>());
    }

    /**
     * Notify all event listeners that a particular event has occurred.<p>
     *
     * @param type event type
     * @param data event data
     */
    public void fireEvent(int type, Map<String, Object> data) {

        fireEvent(new CmsEvent(type, data));
    }

    /**
     * Removes a cms event listener.<p>
     *
     * @param listener the listener to remove
     */
    public void removeCmsEventListener(I_CmsEventListener listener) {

        synchronized (m_eventListeners) {
            Iterator<Integer> it = m_eventListeners.keySet().iterator();
            while (it.hasNext()) {
                List<I_CmsEventListener> listeners = m_eventListeners.get(it.next());
                listeners.remove(listener);
            }
        }
    }

    /**
     * Fires the specified event to a list of event listeners.<p>
     *
     * @param listeners the listeners to fire
     * @param event the event to fire
     */
    protected void fireEventHandler(List<I_CmsEventListener> listeners, CmsEvent event) {

        if (!LOG.isDebugEnabled()) {
            // no logging required
            if ((listeners != null) && (listeners.size() > 0)) {
                // handle all event listeners that listen to this event type
                I_CmsEventListener[] list = listeners.toArray(EVENT_LIST);
                // loop through all registered event listeners
                for (int i = 0; i < list.length; i++) {
                    try {
                        // fire the event
                        list[i].cmsEvent(event);
                    } catch (Throwable t) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_CALLING_EVENT_LISTENER_FAILED_2,
                                list[i].getClass().getName(),
                                event.toString()),
                            t);
                    }
                }
            }
        } else {
            // add lots of event debug output (this should usually be disabled)
            // repeat event handling code to avoid multiple "is log enabled" checks in normal operation
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_EVENT_1, event.toString()));
            if ((listeners != null) && (listeners.size() > 0)) {
                // handle all event listeners that listen to this event type
                I_CmsEventListener[] list = listeners.toArray(EVENT_LIST);
                // log the event data
                if (event.getData() != null) {
                    Iterator<String> i = event.getData().keySet().iterator();
                    while (i.hasNext()) {
                        String key = i.next();
                        Object value = event.getData().get(key);
                        LOG.debug(
                            Messages.get().getBundle().key(
                                Messages.LOG_DEBUG_EVENT_VALUE_3,
                                key,
                                value,
                                event.toString()));
                    }
                } else {
                    LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_NO_EVENT_VALUE_1, event.toString()));
                }
                // log all the registered event listeners
                for (int j = 0; j < list.length; j++) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEBUG_EVENT_LISTENERS_3,
                            list[j],
                            Integer.valueOf(j),
                            event.toString()));
                }
                // loop through all registered event listeners
                for (int i = 0; i < list.length; i++) {
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEBUG_EVENT_START_LISTENER_3,
                            list[i],
                            Integer.valueOf(i),
                            event.toString()));
                    try {
                        // fire the event
                        list[i].cmsEvent(event);
                    } catch (Throwable t) {
                        LOG.error(
                            Messages.get().getBundle().key(
                                Messages.ERR_CALLING_EVENT_LISTENER_FAILED_2,
                                list[i].getClass().getName(),
                                event.toString()),
                            t);
                    }
                    LOG.debug(
                        Messages.get().getBundle().key(
                            Messages.LOG_DEBUG_EVENT_END_LISTENER_3,
                            list[i],
                            Integer.valueOf(i),
                            event.toString()));
                }
            } else {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_EVENT_NO_LISTENER_1, event.toString()));
            }
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DEBUG_EVENT_COMPLETE_1, event.toString()));
        }
    }

    /**
     * Returns the map of all configured event listeners.<p>
     *
     * @return the map of all configured event listeners
     */
    protected Map<Integer, List<I_CmsEventListener>> getEventListeners() {

        return m_eventListeners;
    }

    /**
     * Initialize this event manager with all events from the given base event manager.<p>
     *
     * @param base the base event manager to initialize this event manager with
     */
    protected void initialize(CmsEventManager base) {

        m_eventListeners = new HashMap<Integer, List<I_CmsEventListener>>(base.getEventListeners());

    }
}
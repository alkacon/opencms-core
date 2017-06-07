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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;

/**
 * Takes care of the burst of the same event, by skipping the first ones and executing only the last one.<p>
 *
 * Usage example:
 *
 * <pre>
 *   Window.addResizeHandler(new ResizeHandler() {
 *       public void onResize(ResizeEvent event) {
 *           CmsBurstEventManager.get().schedule("resize-window", new Command() {
 *               public void execute() {
 *                   // resize
 *               }
 *           }, 200);
 *       }
 *   });
 * </pre>
 *
 * @since 8.0.0
 *
 * @see <a href="http://ui-programming.blogspot.com/2010/02/gwt-how-to-implement-delayedtask-in.html">Original implementation</a>
 */
public final class CmsBurstEventManager {

    /**
     * The class is model of one 'burst' event that is added to the manager.<p>
     */
    private static class BurstEvent {

        /** The timer. */
        private final Timer m_timer;

        /**
         * Constructor of the 'burst' event.<p>
         *
         * @param name the unique name, which identifies the event
         * @param command command to execute when the timer expires
         */
        public BurstEvent(final String name, final Command command) {

            m_timer = new Timer() {

                /**
                 * @see com.google.gwt.user.client.Timer#run()
                 */
                @Override
                public void run() {

                    if (command != null) {
                        command.execute();
                    }
                    CmsBurstEventManager.get().cancel(name);
                }
            };
        }

        /**
         * Returns the timer.<p>
         *
         * @return the timer
         */
        public Timer getTimer() {

            return m_timer;
        }
    }

    /** The singleton instance. */
    private static CmsBurstEventManager INSTANCE;

    /** The internal memory. */
    private Map<String, BurstEvent> m_memory = new HashMap<String, BurstEvent>();

    /**
     * Hidden constructor.<p>
     */
    private CmsBurstEventManager() {

        // emtpy
    }

    /**
     * Returns the singleton instance.<p>
     *
     * @return the singleton instance
     */
    protected static CmsBurstEventManager get() {

        if (INSTANCE == null) {
            INSTANCE = new CmsBurstEventManager();
        }
        return INSTANCE;
    }

    /**
     * Adds an 'burst' event to the manager.<p>
     *
     * @param name the unique name, which identifies the event
     * @param command command to execute when the timer expires
     * @param delayMsec the timer delay (it's reseted if multiple events are added)
     */
    public void schedule(final String name, final Command command, final int delayMsec) {

        BurstEvent e = m_memory.remove(name);
        if (e != null) {
            // disable the old event
            e.getTimer().cancel();
        }
        // put the new event and schedule it
        e = new BurstEvent(name, command);
        m_memory.put(name, e);
        e.getTimer().schedule(delayMsec);
    }

    /**
     * Removes the event from the manager.<p>
     *
     * @param eventName the name of the event that we need to remove
     */
    public void cancel(final String eventName) {

        BurstEvent oe = m_memory.remove(eventName);
        if (oe != null) {
            // disable the old event.
            oe.getTimer().cancel();
        }
    }
}
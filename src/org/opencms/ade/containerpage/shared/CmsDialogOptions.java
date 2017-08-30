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

package org.opencms.ade.containerpage.shared;

import java.io.Serializable;
import java.util.List;

/**
 * Option dialog data.<p>
 */
public class CmsDialogOptions implements Serializable {

    /**
     * Describes a dialog option.<p>
     */
    public static class Option implements Serializable {

        /** The serial version id. */
        private static final long serialVersionUID = -3919401021902116204L;

        /** The description. */
        private String m_description;

        /** In case the option is disabled. */
        private boolean m_disabled;

        /** The option label. */
        private String m_label;

        /** The option value. */
        private String m_value;

        /**
         * Constructor.<p>
         *
         * @param value the value
         * @param label the label
         * @param description the description
         * @param disabled if disabled
         */
        public Option(String value, String label, String description, boolean disabled) {
            m_value = value;
            m_label = label;
            m_description = description;
            m_disabled = disabled;
        }

        /**
         * Constructor used for serialization.<p>
         */
        protected Option() {
            // nothing to do
        }

        /**
         * Returns the description.<p>
         *
         * @return the description
         */
        public String getDescription() {

            return m_description;
        }

        /**
         * Returns the label.<p>
         *
         * @return the label
         */
        public String getLabel() {

            return m_label;
        }

        /**
         * Returns the value.<p>
         *
         * @return the value
         */
        public String getValue() {

            return m_value;
        }

        /**
         * Returns if the option is disabled.<p>
         *
         * @return if the option is disabled
         */
        public boolean isDisabled() {

            return m_disabled;
        }

    }

    /** Key to trigger a regular delete action. */
    public static final String REGULAR_DELETE = "regular_delete";

    /** The serial version id. */
    private static final long serialVersionUID = 4758296036064643532L;

    /** The dialog info text. */
    private String m_info;

    /** The dialog options. */
    private List<Option> m_options;

    /** The dialog title. */
    private String m_title;

    /**
     * Constructor.<p>
     *
     * @param title the dialog title
     * @param info the dialog info text
     * @param options the options
     */
    public CmsDialogOptions(String title, String info, List<Option> options) {
        m_title = title;
        m_info = info;
        m_options = options;
    }

    /**
     * Constructor used for serialization.<p>
     */
    protected CmsDialogOptions() {
        // nothing to do
    }

    /**
     * Returns the info.<p>
     *
     * @return the info
     */
    public String getInfo() {

        return m_info;
    }

    /**
     * Returns the options.<p>
     *
     * @return the options
     */
    public List<Option> getOptions() {

        return m_options;
    }

    /**
     * Returns the dialog title.<p>
     *
     * @return the dialog title
     */
    public String getTitle() {

        return m_title;
    }

}

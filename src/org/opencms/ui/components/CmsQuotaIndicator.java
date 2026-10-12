/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.components;

import org.opencms.gwt.shared.CmsQuotaStatus;
import org.opencms.gwt.shared.CmsQuotaStatus.State;
import org.opencms.i18n.CmsMessages;
import org.opencms.ui.Messages;
import org.opencms.util.CmsSizeFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;

/**
 * Reusable detailed quota display or compact warning, with server-calculated warning states.<p>
 */
public class CmsQuotaIndicator extends CssLayout {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /** Accessible usage and warning text. */
    private final Label m_text = new Label();

    /** Progress capped at 100 percent. */
    private final ProgressBar m_bar = new ProgressBar();

    /** Budget value overlaid on the horizontal bar. */
    private final Label m_budgetValue = new Label();

    /** Used and remaining capacity, below the bar in capacity display mode. */
    private final CssLayout m_capacityDetails = new CssLayout();

    /** Used capacity. */
    private final Label m_used = new Label();

    /** Remaining capacity or overflow. */
    private final Label m_free = new Label();

    /** Whether to show a capacity heading and a used/free row. */
    private boolean m_capacityDisplay;

    /** Optional application-specific capacity label. */
    private String m_capacityLabel;

    /** Optional application-specific pattern for the used capacity. */
    private String m_usedTextPattern;

    /** Optional application-specific pattern for capacity exceeding the budget. */
    private String m_overTextPattern;

    /** Display locale. */
    private final Locale m_locale;

    /** Whether to show numeric details in the tooltip only for finite quotas. */
    private boolean m_barOnly;

    /** Whether to overlay the usage text on the bar. */
    private boolean m_textInsideBar;

    /** Whether to display a single usage line, independently of warning visibility. */
    private boolean m_textOnly;

    /** Whether to place the percentage beside the bar in a fixed-width text column. */
    private boolean m_horizontal;

    /** Last snapshot, retained when changing display modes. */
    private CmsQuotaStatus m_status;

    /** Whether to display warnings only. */
    private final boolean m_compact;

    /**
     * Constructor.<p>
     *
     * @param locale the display locale
     * @param compact true to show warnings only, without a bar
     */
    public CmsQuotaIndicator(Locale locale, boolean compact) {

        m_locale = locale;
        m_compact = compact;
        setWidth("100%");
        m_bar.setWidth("100%");
        m_capacityDetails.setStyleName("o-quota-details");
        m_capacityDetails.setWidth("100%");
        m_used.setStyleName("o-quota-used");
        m_free.setStyleName("o-quota-free");
        m_capacityDetails.addComponents(m_used, m_free);
        m_capacityDetails.setVisible(false);
        m_budgetValue.setStyleName("o-quota-value");
        m_budgetValue.setVisible(false);
        addComponents(m_text, m_bar, m_capacityDetails, m_budgetValue);
    }

    /**
     * Shows finite quotas as a bar only, keeping details in its tooltip.
     * Unavailable states and compact warnings retain their explanatory text.<p>
     *
     * @param barOnly whether to hide the visible usage label
     */
    public void setBarOnly(boolean barOnly) {

        m_barOnly = barOnly;
        setStatus(m_status);
    }

    /**
     * Shows the total budget above the bar and the used/free amounts below it.
     * This takes precedence over the inline text mode; compact warnings remain unchanged.<p>
     *
     * @param capacityDisplay whether to use the capacity layout
     */
    public void setCapacityDisplay(boolean capacityDisplay) {

        m_capacityDisplay = capacityDisplay;
        setStatus(m_status);
    }

    /**
     * Sets the localized label above the capacity bar without changing other display modes.<p>
     *
     * @param label the label without a colon, or null to use the standard heading
     */
    public void setCapacityLabel(String label) {

        m_capacityLabel = label;
        setStatus(m_status);
    }

    /**
     * Places the percentage and bar side by side, hiding the capacity heading and detail row.
     * Takes precedence over other presentations, without changing the compact warning filter.<p>
     *
     * @param horizontal whether to use the two-column presentation
     */
    public void setHorizontal(boolean horizontal) {

        m_horizontal = horizontal;
        setStatus(m_status);
    }

    /**
     * Sets the localized text below the bar when the budget is exceeded.<p>
     *
     * @param pattern a message pattern with {0} for the formatted excess, or null for the standard text
     */
    public void setOverTextPattern(String pattern) {

        m_overTextPattern = pattern;
        setStatus(m_status);
    }

    /**
     * Displays a new snapshot. No thresholds or inheritance rules are duplicated in this component.<p>
     *
     * @param status the snapshot, or null if unavailable
     */
    public void setStatus(CmsQuotaStatus status) {

        m_status = status;
        State state = status == null ? State.UNAVAILABLE : status.getState();
        CmsMessages messages = Messages.get().getBundle(m_locale);
        setStyleName("o-quota o-quota-" + state.name().toLowerCase(Locale.ROOT));
        String text;
        boolean limited = (state == State.NORMAL) || (state == State.WARNING) || (state == State.EXCEEDED);
        boolean unlimited = state == State.UNLIMITED;
        boolean horizontal = m_horizontal && !m_compact;
        boolean capacityDisplay = (m_capacityDisplay || horizontal)
            && (limited || unlimited)
            && !m_compact
            && (!m_textOnly || horizontal);
        boolean textInsideBar = m_textInsideBar
            && limited
            && !m_compact
            && !capacityDisplay
            && !m_textOnly
            && !horizontal;
        if (horizontal) {
            addStyleName("o-size-indicator-horizontal");
        }
        if (capacityDisplay) {
            addStyleName("o-quota-capacity");
        }
        if (textInsideBar) {
            addStyleName("o-quota-inline");
        }
        if (limited) {
            text = messages.key(
                m_textOnly ? Messages.GUI_QUOTA_USAGE_TEXT_3 : Messages.GUI_QUOTA_USAGE_3,
                CmsSizeFormatter.format(status.getUsedBytes(), m_locale),
                CmsSizeFormatter.format(status.getQuotaBytes(), m_locale),
                formatPercentage(status));
            if ((state == State.WARNING) && !textInsideBar && !m_textOnly) {
                text = messages.key(Messages.GUI_QUOTA_WARNING_1, text);
            } else if ((state == State.EXCEEDED) && !textInsideBar && !m_textOnly) {
                text = messages.key(Messages.GUI_QUOTA_EXCEEDED_1, text);
            }
            m_bar.setValue((float)Math.min(1, status.getPercentage() / 100));
        } else {
            String key = state == State.UNLIMITED
            ? Messages.GUI_QUOTA_UNLIMITED_0
            : state == State.UNCONFIGURED ? Messages.GUI_QUOTA_UNCONFIGURED_0 : Messages.GUI_QUOTA_UNAVAILABLE_0;
            text = messages.key(key);
            if ((status != null) && (status.getUsedBytes() >= 0)) {
                text = messages.key(
                    Messages.GUI_QUOTA_USED_2,
                    CmsSizeFormatter.format(status.getUsedBytes(), m_locale),
                    text);
            }
        }
        String description = text;
        if (capacityDisplay) {
            String capacity = unlimited
            ? messages.key(Messages.GUI_QUOTA_UNLIMITED_VALUE_0)
            : CmsSizeFormatter.format(status.getQuotaBytes(), m_locale);
            text = m_capacityLabel == null
            ? messages.key(Messages.GUI_QUOTA_CAPACITY_1, capacity)
            : m_capacityLabel + ": " + capacity;
            m_used.setValue(
                m_usedTextPattern == null
                ? messages.key(Messages.GUI_QUOTA_OCCUPIED_1, CmsSizeFormatter.format(status.getUsedBytes(), m_locale))
                : MessageFormat.format(m_usedTextPattern, CmsSizeFormatter.format(status.getUsedBytes(), m_locale)));
            if (unlimited) {
                m_bar.setValue(1f);
                m_free.setValue("");
            } else {
                long remaining = status.getQuotaBytes() - status.getUsedBytes();
                m_free.setValue(
                    (remaining < 0) && (m_overTextPattern != null)
                    ? MessageFormat.format(m_overTextPattern, CmsSizeFormatter.format(Math.abs(remaining), m_locale))
                    : messages.key(
                        remaining < 0 ? Messages.GUI_QUOTA_OVER_1 : Messages.GUI_QUOTA_FREE_1,
                        CmsSizeFormatter.format(Math.abs(remaining), m_locale)));
            }
        }
        boolean showBudgetValue = horizontal && (limited || unlimited);
        m_budgetValue.setVisible(showBudgetValue);
        m_budgetValue.setValue(showBudgetValue ? text : "");
        if (horizontal) {
            text = limited
            ? formatPercentage(status) + " %"
            : unlimited ? "" : state == State.UNCONFIGURED ? messages.key(Messages.GUI_QUOTA_UNCONFIGURED_0) : "—";
        }
        if (showBudgetValue && status.isIndividualQuota()) {
            addStyleName("o-quota-individual");
        }
        m_text.setDescription(
            horizontal && !unlimited && (state != State.UNCONFIGURED) ? description : null,
            ContentMode.TEXT);
        m_free.setVisible(!unlimited);
        m_capacityDetails.setVisible(capacityDisplay && !horizontal);
        m_text.setValue(text);
        m_bar.setVisible((limited || capacityDisplay) && !m_compact && (!m_textOnly || horizontal));
        m_bar.setDescription(
            horizontal ? description : textInsideBar || capacityDisplay ? null : text,
            ContentMode.TEXT);
        m_text.setVisible(
            horizontal || m_textOnly || capacityDisplay || textInsideBar || !m_barOnly || !limited || m_compact);
        setVisible(!m_compact || (status == null) || status.isWarningVisible());
    }

    /**
     * Displays numeric usage inside a taller bar instead of in a tooltip.
     * Compact warnings and states without a finite budget remain plain text.<p>
     *
     * @param textInsideBar whether to overlay the usage text
     */
    public void setTextInsideBar(boolean textInsideBar) {

        m_textInsideBar = textInsideBar;
        setStatus(m_status);
    }

    /**
     * Shows usage as percentage and used/total capacity without a bar or detail row.
     * Unlike compact warning mode, this also displays healthy and unlimited budgets.<p>
     *
     * @param textOnly whether to use the single-line presentation
     */
    public void setTextOnly(boolean textOnly) {

        m_textOnly = textOnly;
        setStatus(m_status);
    }

    /**
     * Sets the localized text below the bar for the used capacity.<p>
     *
     * @param pattern a message pattern with {0} for the formatted size, or null for the standard text
     */
    public void setUsedTextPattern(String pattern) {

        m_usedTextPattern = pattern;
        setStatus(m_status);
    }

    /**
     * Formats utilization as a whole percentage, truncating the fractional part.<p>
     *
     * @param status the quota snapshot
     * @return utilization without decimal places
     */
    private String formatPercentage(CmsQuotaStatus status) {

        NumberFormat format = NumberFormat.getNumberInstance(m_locale);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(0);
        BigDecimal percentage = BigDecimal.valueOf(status.getUsedBytes()).multiply(BigDecimal.valueOf(100)).divide(
            BigDecimal.valueOf(status.getQuotaBytes()),
            0,
            RoundingMode.DOWN);
        return format.format(percentage);
    }
}

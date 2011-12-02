/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Label;

/**
 * Single line label with text truncation and tool tip.<p>
 * 
 * @since 8.0.0
 */
public class CmsLabel extends Label implements I_CmsTruncable {

    /**
     * Interface for generating HTML titles (tooltips) for a label.<p>
     */
    public interface I_TitleGenerator {

        /**
         * Should return the title, or null if no title should be displayed.<p>
         * 
         * @param originalText the original untruncated text stored in the label 
         *  
         * @return the title to display, or null if no title should be displayed 
         */
        String getTitle(String originalText);
    }

    /** The CSS bundle instance used for this widget.<p> */
    protected static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** List of elements to measure. */
    protected static List<Element> m_elements;

    /** The original untruncated text stored in the label. */
    protected String m_originalText;

    /** The title generator. */
    private I_TitleGenerator m_titleGenerator;

    /**
     * Creates an empty label.<p>
     */
    public CmsLabel() {

        setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().truncatingLabel());
    }

    /**
     * Creates an empty label using the given element.<p>
     * 
     * @param element the element to use 
     */
    public CmsLabel(Element element) {

        super(element);
        setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().truncatingLabel());
    }

    /**
     * Creates a label with the specified text.<p>
     * 
     * @param text the new label's text
     */
    public CmsLabel(String text) {

        super(text);
        setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().truncatingLabel());
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onAttach()
     */
    @Override
    public void onAttach() {

        // just for visibility
        super.onAttach();
    }

    /**
     * Sets the inner HTML of the label.<p>
     * 
     * Avoid using this, better use {@link #setText(String)}<p>
     * 
     * @param html the HTML to set
     */
    public void setHTML(String html) {

        getElement().setInnerHTML(html);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    @Override
    public void setText(String text) {

        super.setText(text);
        m_originalText = text;
        setTitle(getTitle());
    }

    /**
     * Sets the title generator.<p>
     * 
     * @param titleGen the new title generator 
     */
    public void setTitleGenerator(I_TitleGenerator titleGen) {

        m_titleGenerator = titleGen;
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int labelWidth) {

        if (labelWidth > 0) {
            getElement().getStyle().setWidth(labelWidth, Unit.PX);
        }
    }

    /**
     * Updates the title.<p>
     * 
     * @param truncating true if the label is being truncated 
     */
    public void updateTitle(boolean truncating) {

        String title = getTitle(truncating);
        Element element = getElement();
        if (title == null) {
            element.removeAttribute(CmsDomUtil.Attribute.title.name());
        } else {
            element.setAttribute(CmsDomUtil.Attribute.title.name(), title);
        }
    }

    /**
     * Returns the title to be displayed, which is either produced by a title generator,
     * or is equal to the original text if no title generator is set and the label is being 
     * truncated.<p>
     * 
     * @param truncating true if the label is being truncated 
     * 
     * @return the title to display 
     */
    protected String getTitle(boolean truncating) {

        if (m_titleGenerator != null) {
            return m_titleGenerator.getTitle(m_originalText);
        }
        return m_originalText;
    }
}

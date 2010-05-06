/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/CmsLabel.java,v $
 * Date   : $Date: 2010/05/06 07:21:11 $
 * Version: $Revision: 1.7 $
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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.I_CmsTruncable;
import org.opencms.gwt.client.ui.css.I_CmsInputCss;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.util.CmsDebugLog;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsTextMetrics;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * Label with smart text truncating and tool tip.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public class CmsLabel extends Widget implements HasHorizontalAlignment, HasText, HasHTML, I_CmsTruncable {

    /** The CSS bundle instance used for this widget.<p> */
    protected static final I_CmsInputCss CSS = I_CmsInputLayoutBundle.INSTANCE.inputCss();

    /** List of elements to measure. */
    protected static List<Element> m_elements;

    /** Current horizontal alignment. */
    private HorizontalAlignmentConstant m_horzAlign;

    /**
     * Creates an empty label.<p>
     */
    public CmsLabel() {

        this(Document.get().createDivElement());
        fixInline();
    }

    /**
     * Creates an empty label using the given element.<p>
     * 
     * @param element the element to use 
     */
    public CmsLabel(Element element) {

        setElement(element);
        fixInline();
    }

    /**
     * Creates a label with the specified text.<p>
     * 
     * @param text the new label's text
     */
    public CmsLabel(String text) {

        this();
        setText(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#getHorizontalAlignment()
     */
    public HorizontalAlignmentConstant getHorizontalAlignment() {

        return m_horzAlign;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHTML#getHTML()
     */
    public String getHTML() {

        return getElement().getInnerHTML();
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#getText()
     */
    public String getText() {

        return getElement().getInnerText();
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
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#setHorizontalAlignment(com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant)
     */
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {

        m_horzAlign = align;
        getElement().getStyle().setProperty(CmsDomUtil.Style.textAlign.name(), align.getTextAlignString());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasHTML#setHTML(java.lang.String)
     */
    public void setHTML(String html) {

        getElement().setInnerHTML(html);
        // reset tooltip
        getElement().removeAttribute(CmsDomUtil.Attribute.title.name());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasText#setText(java.lang.String)
     */
    public void setText(String text) {

        getElement().setInnerText(text);
        // reset tooltip
        getElement().removeAttribute(CmsDomUtil.Attribute.title.name());
    }

    /**
     * @see org.opencms.gwt.client.ui.I_CmsTruncable#truncate(java.lang.String, int)
     */
    public void truncate(String textMetricsKey, int labelWidth) {

        Element element = getElement();
        String title = element.getAttribute(CmsDomUtil.Attribute.title.name());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(title)) {
            element.setInnerText(title);
            element.removeAttribute(CmsDomUtil.Attribute.title.name());
        }

        // measure the actual text width
        CmsTextMetrics tm = CmsTextMetrics.get(element, textMetricsKey);
        String text = element.getInnerText();
        int textWidth = tm.getWidth(text);
        tm.release();

        CmsDebugLog log = CmsDebugLog.getInstance();

        if (labelWidth >= textWidth) {
            // nothing to do
            return;
        }

        log.printLine("truncate");
        log.printLine("text: " + text);
        log.printLine("elemWidth: " + labelWidth);
        log.printLine("textWidth: " + textWidth);

        // if the text does not have enough space, fix it
        int maxChars = (int)((float)labelWidth / (float)textWidth * text.length());
        if (maxChars < 1) {
            maxChars = 1;
        }
        String newText = text.substring(0, maxChars - 1);
        if (text.startsWith("/")) {
            // file name?
            newText = CmsStringUtil.formatResourceName(text, maxChars);
        } else if (maxChars > 2) {
            // enough space for ellipsis?
            newText += CmsDomUtil.Entity.hellip.html();
        }
        if (newText.isEmpty()) {
            // if empty, it could break the layout
            newText = CmsDomUtil.Entity.nbsp.html();
        }
        // use html instead of text because of the entities
        element.setInnerHTML(newText);
        // add tooltip with the original text
        element.setAttribute(CmsDomUtil.Attribute.title.name(), text);
        // set the corresponding style
        element.addClassName(I_CmsInputLayoutBundle.INSTANCE.inputCss().labelTruncated());
    }

    /**
     * Helper method for changing the label's CSS display property from inline to inline-block (if possible).<p>
     * 
     * This avoids some display problems, e.g. in Chrome.<p>
     */
    private void fixInline() {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            public void execute() {

                Element element = getElement();
                String display = CmsDomUtil.getCurrentStyle(element, CmsDomUtil.Style.display);
                if (display.equalsIgnoreCase("inline")) {
                    element.addClassName(CSS.inlineBlock());
                    element.addClassName(CSS.alignBottom());
                }
            }
        });
    }
}

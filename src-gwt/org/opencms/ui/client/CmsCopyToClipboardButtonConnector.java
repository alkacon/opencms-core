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

package org.opencms.ui.client;

import org.opencms.ui.components.CmsCopyToClipboardButton;
import org.opencms.ui.shared.components.CmsCopyToClipboardState;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.vaadin.client.annotations.OnStateChange;
import com.vaadin.client.ui.ConnectorFocusAndBlurHandler;
import com.vaadin.client.ui.VButton;
import com.vaadin.client.ui.button.ButtonConnector;
import com.vaadin.shared.ui.Connect;

/**
 * The copy to clip-board/select text button client connector.<p>
 */
@Connect(CmsCopyToClipboardButton.class)
public class CmsCopyToClipboardButtonConnector extends ButtonConnector {

    /** The serial version id. */
    private static final long serialVersionUID = -5124036048815760156L;

    /** Indicating if the copy command is supported by the client browser. */
    boolean m_copySupported;

    /**
     * @see com.vaadin.client.ui.AbstractConnector#getState()
     */
    @Override
    public CmsCopyToClipboardState getState() {

        return (CmsCopyToClipboardState)super.getState();
    }

    /**
     * @see com.vaadin.client.ui.button.ButtonConnector#init()
     */
    @Override
    public void init() {

        VButton button = getWidget();
        button.client = getConnection();
        ConnectorFocusAndBlurHandler.addHandlers(this);

        m_copySupported = isCopyToClipboardSupported();
        button.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                event.preventDefault();
                event.stopPropagation();
                onButtonClick();
            }
        });
    }

    /**
     * Copy the text content of the matching element to the clip-board.<p>
     *
     * @param selector the query selector matching the target element
     */
    native void copyToClipboard(String selector)/*-{

        var doc = $wnd.document;
        var targetElement = doc.querySelector(selector);
        if (targetElement != null) {
            var text = targetElement.textContent;
            var textArea = document.createElement("textarea");

            // add some styles to hide the text area
            textArea.style.position = 'fixed';
            textArea.style.top = 0;
            textArea.style.left = 0;
            textArea.style.width = '2em';
            textArea.style.height = '2em';
            textArea.style.padding = 0;
            textArea.style.border = 'none';
            textArea.style.outline = 'none';
            textArea.style.boxShadow = 'none';
            textArea.style.background = 'transparent';
            textArea.style.color = 'transparent';
            textArea.value = text;

            document.body.appendChild(textArea);

            textArea.select();

            try {
                var successful = document.execCommand('copy');
                var msg = successful ? 'successful' : 'unsuccessful';
            } catch (err) {
            }
            document.body.removeChild(textArea);
        }
    }-*/;

    /**
     * Checks whether the copy command is supported.<p>
     *
     * @return <code>true</code> if the copy command is supported
     */
    native boolean isCopyToClipboardSupported()/*-{
        var result = document.queryCommandSupported('copy');
        if (result) {
            var uMatch = navigator.userAgent.match(/Firefox\/(.*)$/);
            if (uMatch && uMatch.length > 1) {
                result = uMatch[1] >= 41;
            }
        }
        return result;
    }-*/;

    /**
     * Handles the button click.<p>
     */
    void onButtonClick() {

        if (m_copySupported) {
            copyToClipboard(getState().getSelector());
        } else {
            selectText(getState().getSelector());
        }
    }

    /**
     * Selects the text content of the given element.<p>
     *
     * @param selector the element selector
     */
    native void selectText(String selector)/*-{

        var doc = $wnd.document;
        var targetElement = doc.querySelector(selector);
        if (targetElement != null) {
            targetElement.focus();

            var range, selection;
            if (doc.body.createTextRange) {
                range = doc.body.createTextRange();
                range.moveToElementText(targetElement);
                range.select();
            } else if ($wnd.getSelection) {
                selection = $wnd.getSelection();
                range = doc.createRange();
                range.selectNodeContents(targetElement);
                selection.removeAllRanges();
                selection.addRange(range);
            }
        }
    }-*/;

    /**
     * Overriding the set caption method, to show alternative text in case copy to clip-board is not supported.<p>
     */
    @OnStateChange({"caption", "captionAsHtml"})
    void setCaption() {

        String caption = m_copySupported || CmsStringUtil.isEmptyOrWhitespaceOnly(getState().getAlternativeText())
        ? getState().caption
        : getState().getAlternativeText();
        getWidget().setText(caption);
    }
}

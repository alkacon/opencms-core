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

import org.opencms.gwt.client.CmsEditableDataJSO;
import org.opencms.gwt.client.I_CmsDescendantResizeHandler;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.impl.DOMImpl;
import org.opencms.gwt.client.util.impl.DocumentStyleImpl;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Utility class to access the HTML DOM.<p>
 *
 * @since 8.0.0
 */
public final class CmsDomUtil {

    /**
     * HTML tag attributes.<p>
     */
    public static enum Attribute {

        /** class. */
        clazz {

            /**
             * @see java.lang.Enum#toString()
             */
            @Override
            public String toString() {

                return "class";
            }
        },

        /** title. */
        title;
    }

    /**
     * Helper class to encapsulate an attribute/value pair.<p>
     */
    public static class AttributeValue {

        /** The attribute. */
        private Attribute m_attr;

        /** The attribute value. */
        private String m_value;

        /**
         * Constructor.<p>
         *
         * @param attr the attribute
         */
        public AttributeValue(Attribute attr) {

            this(attr, null);
        }

        /**
         * Constructor.<p>
         *
         * @param attr the attribute
         * @param value the value
         */
        public AttributeValue(Attribute attr, String value) {

            m_attr = attr;
            m_value = value;
        }

        /**
         * Returns the attribute.<p>
         *
         * @return the attribute
         */
        public Attribute getAttr() {

            return m_attr;
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
         * Sets the value.<p>
         *
         * @param value the value to set
         */
        public void setValue(String value) {

            m_value = value;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            StringBuffer sb = new StringBuffer();
            sb.append(m_attr.toString());
            if (m_value != null) {
                sb.append("=\"").append(m_value).append("\"");
            }
            return sb.toString();
        }
    }

    /**
     * CSS Colors.<p>
     */
    public static enum Color {

        /** CSS Color. */
        red;
    }

    /**
     * HTML entities.<p>
     */
    public static enum Entity {

        /** non-breaking space. */
        hellip,

        /** non-breaking space. */
        nbsp;

        /**
         * Returns the HTML code for this entity.<p>
         *
         * @return the HTML code for this entity
         */
        public String html() {

            return "&" + super.name() + ";";
        }
    }

    /** Form methods. */
    public static enum Method {

        /** The get method. */
        get,
        /** The post method. */
        post;
    }

    /**
     * CSS Properties.<p>
     */
    public static enum Style {

        /** CSS Property. */
        backgroundColor,

        /** CSS Property. */
        backgroundImage,

        /** CSS property. */
        borderLeftWidth,

        /** CSS property. */
        borderRightWidth,

        /** CSS Property. */
        borderStyle,

        /** CSS property. */
        boxSizing,

        /** CSS Property. */
        display,

        /** CSS Property. */
        floatCss {

            /**
             * @see java.lang.Enum#toString()
             */
            @Override
            public String toString() {

                return "float";
            }
        },

        /** CSS Property. */
        fontFamily,

        /** CSS Property. */
        fontSize,

        /** CSS Property. */
        fontSizeAdjust,

        /** CSS Property. */
        fontStretch,

        /** CSS Property. */
        fontStyle,

        /** CSS Property. */
        fontVariant,

        /** CSS Property. */
        fontWeight,

        /** CSS Property. */
        height,

        /** CSS Property. */
        left,

        /** CSS Property. */
        letterSpacing,

        /** CSS Property. */
        lineHeight,

        /** CSS Property. */
        marginBottom,

        /** CSS Property. */
        marginTop,

        /** CSS Property. */
        maxHeight,

        /** CSS Property. */
        minHeight,

        /** CSS Property. */
        opacity,

        /** CSS Property. */
        overflow,

        /** CSS Property. */
        padding,

        /** CSS property. */
        paddingLeft,

        /** CSS property. */
        paddingRight,

        /** CSS Property. */
        position,

        /** CSS Property. */
        right,

        /** CSS Property. */
        textAlign,

        /** CSS Property. */
        textDecoration,

        /** CSS Property. */
        textIndent,

        /** CSS Property. */
        textShadow,

        /** CSS Property. */
        textTransform,

        /** CSS Property. */
        top,

        /** CSS Property. */
        visibility,

        /** CSS Property. */
        whiteSpace,

        /** CSS Property. */
        width,

        /** CSS Property. */
        wordSpacing,

        /** CSS Property. */
        wordWrap,

        /** CSS Property. */
        zIndex;

    }

    /**
     * CSS Property values.<p>
     */
    public static enum StyleValue {

        /** CSS Property value. */
        absolute,

        /** CSS Property value. */
        auto,

        /** CSS Property value. */
        hidden,

        /** CSS Property value. */
        inherit,

        /** CSS Property value. */
        none,

        /** CSS Property value. */
        normal,

        /** CSS Property value. */
        nowrap,

        /** CSS Property value. */
        transparent;
    }

    /**
     * HTML Tags.<p>
     */
    public static enum Tag {

        /** HTML Tag. */
        a,

        /** HTML Tag. */
        ALL {

            /**
             * @see java.lang.Enum#toString()
             */
            @Override
            public String toString() {

                return "*";
            }
        },

        /** HTML Tag. */
        b,

        /** HTML Tag. */
        body,

        /** HTML Tag. */
        div,

        /** HTML Tag. */
        h1,

        /** HTML Tag. */
        h2,

        /** HTML Tag. */
        h3,

        /** HTML Tag. */
        h4,

        /** HTML-Tag. */
        iframe,

        /** HTML Tag. */
        li,

        /** HTML Tag. */
        p,

        /** HTML Tag. */
        script,

        /** HTML Tag. */
        span,

        /** HTML Tag. */
        table,

        /** HTML Tag. */
        ul;
    }

    /** Enumeration of link/form targets. */
    public static enum Target {

        /** Target blank. */
        BLANK("_blank"),

        /** Unspecified target. */
        NONE(""),

        /** Target parent. */
        PARENT("_parent"),

        /** Target self. */
        SELF("_self"),

        /** Target top. */
        TOP("_top");

        /** The target representation. */
        private String m_representation;

        /**
         * Constructor.<p>
         *
         * @param representation the target representation
         */
        Target(String representation) {

            m_representation = representation;
        }

        /**
         * Returns the target representation.<p>
         * @return the target representation
         */
        public String getRepresentation() {

            return m_representation;
        }
    }

    /** Browser dependent DOM implementation. */
    private static DOMImpl domImpl;

    /** The dynamic style sheet object. */
    private static JavaScriptObject m_dynamicStyleSheet;

    /** Stores the scroll bar width measurement. */
    private static int m_scrollbarWidth = -1;

    /** Browser dependent style implementation. */
    private static DocumentStyleImpl styleImpl;

    /**
     * Hidden constructor.<p>
     */
    private CmsDomUtil() {

        // doing nothing
    }

    /**
     * Adds an overlay div to the element.<p>
     *
     * @param element the element
     */
    public static void addDisablingOverlay(Element element) {

        Element overlay = DOM.createDiv();
        overlay.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().disablingOverlay());
        element.getStyle().setPosition(Position.RELATIVE);
        element.appendChild(overlay);
    }

    /**
     * Adds a CSS style rule to a dynamically inserted style sheet.<p>
     *
     * @param rule the style rule
     */
    public static native void addDynamicStyleRule(String rule) /*-{
        var style = @org.opencms.gwt.client.util.CmsDomUtil::m_dynamicStyleSheet;
        if (style == null) {
            var style = $wnd.document.createElement("style");
            style.appendChild($wnd.document.createTextNode(""));
            $wnd.document.head.appendChild(style);
            @org.opencms.gwt.client.util.CmsDomUtil::m_dynamicStyleSheet = style;
        }
        style.sheet.insertRule(rule, 0);
    }-*/;

    /**
     * Returns if the given client position is over the given element.<p>
     * Use <code>-1</code> for x or y to ignore one ordering orientation.<p>
     *
     * @param element the element
     * @param x the client x position, use <code>-1</code> to ignore x position
     * @param y the client y position, use <code>-1</code> to ignore y position
     *
     * @return <code>true</code> if the given position is over the given element
     */
    public static boolean checkPositionInside(Element element, int x, int y) {

        // ignore x / left-right values for x == -1
        if (x != -1) {
            // check if the mouse pointer is within the width of the target
            int left = CmsDomUtil.getRelativeX(x, element);
            int offsetWidth = element.getOffsetWidth();
            if ((left <= 0) || (left >= offsetWidth)) {
                return false;
            }
        }
        // ignore y / top-bottom values for y == -1
        if (y != -1) {
            // check if the mouse pointer is within the height of the target
            int top = CmsDomUtil.getRelativeY(y, element);
            int offsetHeight = element.getOffsetHeight();
            if ((top <= 0) || (top >= offsetHeight)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clears the elements hover state by removing it from the DOM and re-attaching it.<p>
     *
     * @param element the element
     */
    public static void clearHover(Element element) {

        Element parent = element.getParentElement();
        Element sibling = element.getNextSiblingElement();
        element.removeFromParent();
        parent.insertBefore(element, sibling);
    }

    /**
    * Removes the opacity attribute from the element's inline-style.<p>
    *
    * @param element the DOM element to manipulate
    */
    public static void clearOpacity(Element element) {

        getStyleImpl().clearOpacity(element);
    }

    /**
     * Clones the given element.<p>
     *
     * It creates a new element with the same tag, and sets the class attribute,
     * and sets the innerHTML.<p>
     *
     * @param element the element to clone
     *
     * @return the cloned element
     */
    public static Element clone(Element element) {

        Element elementClone = DOM.createElement(element.getTagName());
        elementClone.setClassName(element.getClassName());
        elementClone.setInnerHTML(element.getInnerHTML());
        return elementClone;
    }

    /**
     * Generates a closing tag.<p>
     *
     * @param tag the tag to use
     *
     * @return HTML code
     */
    public static String close(Tag tag) {

        return "</" + tag.name() + ">";
    }

    /**
     * Copy the text content of the matching element to the clip-board.<p>
     *
     * @param selector the query selector matching the target element
     *
     * @return in case the command was executed successfully
     */
    public static native boolean copyToClipboard(String selector)/*-{

        var doc = $wnd.document;
        var targetElement = doc.querySelector(selector);
        if (targetElement != null) {
            var textAreaAdded = false;
            var textArea;
            if ("TEXTAREA" == targetElement.tagName) {
                textArea = targetElement;
            } else {
                textAreaAdded = true;
                var text = targetElement.textContent;
                textArea = document.createElement("textarea");

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
            }
            textArea.select();
            var result = false;
            try {
                result = document.execCommand('copy');
            } catch (err) {
            }
            if (textAreaAdded) {
                document.body.removeChild(textArea);
            } else {
                // remove selection
                textArea.selectionStart = textArea.selectionEnd;
            }
            return result;
        }
    }-*/;

    /**
     * This method will create an {@link com.google.gwt.dom.client.Element} for the given HTML.
     * The HTML should have a single root tag, if not, the first tag will be used and all others discarded.<p>
     * Script-tags will be removed.<p>
     *
     * @param html the HTML to use for the element
     *
     * @return the created element
     *
     * @throws Exception if something goes wrong
     */
    public static Element createElement(String html) throws Exception {

        Element wrapperDiv = DOM.createDiv();
        wrapperDiv.setInnerHTML(html);
        Element elementRoot = wrapperDiv.getFirstChildElement();
        wrapperDiv.removeChild(elementRoot);
        // just in case we have a script tag outside the root HTML-tag
        while ((elementRoot != null) && (elementRoot.getTagName().toLowerCase().equals(Tag.script.name()))) {
            elementRoot = wrapperDiv.getFirstChildElement();
            wrapperDiv.removeChild(elementRoot);
        }
        if (elementRoot == null) {
            CmsDebugLog.getInstance().printLine(
                "Could not create element as the given HTML has no appropriate root element");
            throw new IllegalArgumentException(
                "Could not create element as the given HTML has no appropriate root element");
        }
        return elementRoot;
    }

    /**
     * Convenience method to assemble the HTML to use for a button face.<p>
     *
     * @param text text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     * @param align the alignment of the text in reference to the image
     *
     * @return the HTML
     */
    public static String createFaceHtml(String text, String imageClass, HorizontalAlignmentConstant align) {

        StringBuffer sb = new StringBuffer();
        if (align == HasHorizontalAlignment.ALIGN_LEFT) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {
                sb.append(text.trim());
            }
        }
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(imageClass)) {
            String clazz = imageClass;
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {
                if (align == HasHorizontalAlignment.ALIGN_LEFT) {
                    clazz += " " + I_CmsLayoutBundle.INSTANCE.buttonCss().spacerLeft();
                } else {
                    clazz += " " + I_CmsLayoutBundle.INSTANCE.buttonCss().spacerRight();
                }
            }
            AttributeValue attr = new AttributeValue(Attribute.clazz, clazz);
            sb.append(enclose(Tag.span, "", attr));
        }
        if (align == HasHorizontalAlignment.ALIGN_RIGHT) {
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(text)) {
                sb.append(text.trim());
            }
        }
        return sb.toString();
    }

    /**
     * Creates an iFrame element with the given name attribute.<p>
     *
     * @param name the name attribute value
     *
     * @return the iFrame element
     */
    public static com.google.gwt.dom.client.Element createIFrameElement(String name) {

        return getDOMImpl().createIFrameElement(Document.get(), name);
    }

    /**
     * Encloses the given text with the given tag.<p>
     *
     * @param tag the tag to use
     * @param text the text to enclose
     * @param attrs the optional tag attributes
     *
     * @return HTML code
     */
    public static String enclose(Tag tag, String text, AttributeValue... attrs) {

        return open(tag, attrs) + text + close(tag);
    }

    /**
     * Ensures a script tag is present within the window document context.<p>
     *
     * @param javascriptLink the link to the java script resource
     */
    public static void ensureJavaScriptIncluded(String javascriptLink) {

        ensureJavaScriptIncluded(javascriptLink, true);
    }

    /**
     * Ensures a script tag is present within the window document context.<p>
     *
     * @param javascriptLink the link to the java script resource
     */
    public static void ensureJavaScriptIncluded(String javascriptLink, boolean async) {

        if (!isJavaScriptPresent(javascriptLink)) {
            injectScript(javascriptLink, async, null);
        }
    }

    /**
     * Ensures a script tag is present within the window document context.<p>
     *
     * @param javascriptLink the link to the java script resource
     */
    public static void ensureJavaScriptIncluded(String javascriptLink, boolean async, JavaScriptObject callback) {

        if (!isJavaScriptPresent(javascriptLink)) {
            injectScript(javascriptLink, async, callback);
        }
    }

    /**
     * Triggers a mouse-out event for the given element.<p>
     *
     * Useful in case something is capturing all events.<p>
     *
     * @param element the element to use
     */
    public static void ensureMouseOut(Element element) {

        NativeEvent nativeEvent = Document.get().createMouseOutEvent(
            0,
            0,
            0,
            0,
            0,
            false,
            false,
            false,
            false,
            0,
            null);
        element.dispatchEvent(nativeEvent);
    }

    /**
     * Triggers a mouse-out event for the given target.<p>
     *
     * Useful in case something is capturing all events.<p>
     *
     * @param target the target to use
     */
    public static void ensureMouseOut(HasHandlers target) {

        NativeEvent nativeEvent = Document.get().createMouseOutEvent(
            0,
            0,
            0,
            0,
            0,
            false,
            false,
            false,
            false,
            0,
            null);
        DomEvent.fireNativeEvent(nativeEvent, target);
    }

    /**
     * Triggers a mouse-over event for the given element.<p>
     *
     * Useful in case something is capturing all events.<p>
     *
     * @param element the element to use
     */
    public static void ensureMouseOver(Element element) {

        NativeEvent nativeEvent = Document.get().createMouseOverEvent(
            0,
            0,
            0,
            0,
            0,
            false,
            false,
            false,
            false,
            0,
            null);
        element.dispatchEvent(nativeEvent);
    }

    /**
     * Checks the window.document for given style-sheet and includes it if required.<p>
     *
     * @param styleSheetLink the style-sheet link
     */
    public static native void ensureStyleSheetIncluded(String styleSheetLink)/*-{
        var styles = $wnd.document.styleSheets;
        for (var i = 0; i < styles.length; i++) {
            if (styles[i].href != null
                    && styles[i].href.indexOf(styleSheetLink) >= 0) {
                // style-sheet is present
                return;
            }
        }
        // include style-sheet into head
        var headID = $wnd.document.getElementsByTagName("head")[0];
        var cssNode = $wnd.document.createElement('link');
        cssNode.type = 'text/css';
        cssNode.rel = 'stylesheet';
        cssNode.href = styleSheetLink;
        headID.appendChild(cssNode);
    }-*/;

    /**
     * Ensures that the given element is visible.<p>
     *
     * Assuming the scrollbars are on the container element, and that the element is a child of the container element.<p>
     *
     * @param containerElement the container element, has to be parent of the element
     * @param element the element to be seen
     * @param animationTime the animation time for scrolling, use zero for no animation
     */
    public static void ensureVisible(final Element containerElement, Element element, int animationTime) {

        Element item = element;
        int realOffset = 0;
        while ((item != null) && (item != containerElement)) {
            realOffset += element.getOffsetTop();
            item = item.getOffsetParent();
        }
        final int endScrollTop = realOffset - (containerElement.getOffsetHeight() / 2);

        if (animationTime <= 0) {
            // no animation
            containerElement.setScrollTop(endScrollTop);
            return;
        }
        final int startScrollTop = containerElement.getScrollTop();
        (new Animation() {

            /**
             * @see com.google.gwt.animation.client.Animation#onUpdate(double)
             */
            @Override
            protected void onUpdate(double progress) {

                containerElement.setScrollTop(startScrollTop + (int)((endScrollTop - startScrollTop) * progress));
            }
        }).run(animationTime);
    }

    /**
     * Escapes a String so it may be printed as text content or attribute
     * value in a HTML page or an XML file.<p>
     *
     * This method replaces the following characters in a String:
     * <ul>
     * <li><b>&lt;</b> with &amp;lt;
     * <li><b>&gt;</b> with &amp;gt;
     * <li><b>&amp;</b> with &amp;amp;
     * <li><b>&quot;</b> with &amp;quot;
     * </ul><p>
     *
     * @param source the string to escape
     *
     * @return the escaped string
     */
    public static String escapeXml(String source) {

        if (source == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(source.length() * 2);

        for (int i = 0; i < source.length(); ++i) {
            char ch = source.charAt(i);
            switch (ch) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    // don't escape already escaped international and special characters
                    int terminatorIndex = source.indexOf(";", i);
                    if (terminatorIndex > 0) {
                        if (source.substring(i + 1, terminatorIndex).matches("#[0-9]+")) {
                            result.append(ch);
                            break;
                        }
                    }

                    // note that to other "break" in the above "if" block
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                default:
                    result.append(ch);
            }
        }
        return new String(result);
    }

    /**
     * Fires a focus event for the given widget.<p>
     *
     * @param widget the widget
     */
    public static void fireFocusEvent(Widget widget) {

        NativeEvent nativeEvent = Document.get().createFocusEvent();
        DomEvent.fireNativeEvent(nativeEvent, widget, widget.getElement());
    }

    /**
     * Ensures any embedded flash players are set opaque so UI elements may be placed above them.<p>
     *
     * @param element the element to work on
     */
    public static native void fixFlashZindex(Element element)/*-{

        var embeds = element.getElementsByTagName('embed');
        for (i = 0; i < embeds.length; i++) {
            embed = embeds[i];
            var new_embed;
            // everything but Firefox & Konqueror
            if (embed.outerHTML) {
                var html = embed.outerHTML;
                // replace an existing wmode parameter
                if (html.match(/wmode\s*=\s*('|")[a-zA-Z]+('|")/i))
                    new_embed = html.replace(/wmode\s*=\s*('|")window('|")/i,
                            "wmode='transparent'");
                // add a new wmode parameter
                else
                    new_embed = html.replace(/<embed\s/i,
                            "<embed wmode='transparent' ");
                // replace the old embed object with the fixed version
                embed.insertAdjacentHTML('beforeBegin', new_embed);
                embed.parentNode.removeChild(embed);
            } else {
                // cloneNode is buggy in some versions of Safari & Opera, but works fine in FF
                new_embed = embed.cloneNode(true);
                if (!new_embed.getAttribute('wmode')
                        || new_embed.getAttribute('wmode').toLowerCase() == 'window')
                    new_embed.setAttribute('wmode', 'transparent');
                embed.parentNode.replaceChild(new_embed, embed);
            }
        }
        // loop through every object tag on the site
        var objects = element.getElementsByTagName('object');
        for (i = 0; i < objects.length; i++) {
            object = objects[i];
            var new_object;
            // object is an IE specific tag so we can use outerHTML here
            if (object.outerHTML) {
                var html = object.outerHTML;
                // replace an existing wmode parameter
                if (html
                        .match(/<param\s+name\s*=\s*('|")wmode('|")\s+value\s*=\s*('|")[a-zA-Z]+('|")\s*\/?\>/i))
                    new_object = html
                            .replace(
                                    /<param\s+name\s*=\s*('|")wmode('|")\s+value\s*=\s*('|")window('|")\s*\/?\>/i,
                                    "<param name='wmode' value='transparent' />");
                // add a new wmode parameter
                else
                    new_object = html
                            .replace(/<\/object\>/i,
                                    "<param name='wmode' value='transparent' />\n</object>");
                // loop through each of the param tags
                var children = object.childNodes;
                for (j = 0; j < children.length; j++) {
                    try {
                        if (children[j] != null) {
                            var theName = children[j].getAttribute('name');
                            if (theName != null && theName.match(/flashvars/i)) {
                                new_object = new_object
                                        .replace(
                                                /<param\s+name\s*=\s*('|")flashvars('|")\s+value\s*=\s*('|")[^'"]*('|")\s*\/?\>/i,
                                                "<param name='flashvars' value='"
                                                        + children[j]
                                                                .getAttribute('value')
                                                        + "' />");
                            }
                        }
                    } catch (err) {
                    }
                }
                // replace the old embed object with the fixed versiony
                object.insertAdjacentHTML('beforeBegin', new_object);
                object.parentNode.removeChild(object);
            }
        }

    }-*/;

    /**
     * Generates a form element with hidden input fields.<p>
     *
     * @param action the form action
     * @param method the form method
     * @param target the form target
     * @param values the input values
     *
     * @return the generated form element
     */
    public static FormElement generateHiddenForm(
        String action,
        Method method,
        String target,
        Map<String, String> values) {

        FormElement formElement = Document.get().createFormElement();
        formElement.setMethod(method.name());
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(target)) {
            formElement.setTarget(target);
        }
        formElement.setAction(action);
        for (Entry<String, String> input : values.entrySet()) {
            formElement.appendChild(createHiddenInput(input.getKey(), input.getValue()));
        }
        return formElement;
    }

    /**
     * Generates a form element with hidden input fields.<p>
     *
     * @param action the form action
     * @param method the form method
     * @param target the form target
     * @param values the input values
     *
     * @return the generated form element
     */
    public static FormElement generateHiddenForm(
        String action,
        Method method,
        Target target,
        Map<String, String> values) {

        return generateHiddenForm(action, method, target.getRepresentation(), values);
    }

    /**
     * Returns the currently focused element.<p>
     *
     * @return the currently focused element
     */
    public static native Element getActiveElement() /*-{
        return $wnd.document.activeElement;
    }-*/;

    /**
     * Gets the edit data for all oc-editable elements in the page.<p>
     *
     * @return the list of edit data
     */
    public static List<CmsEditableDataJSO> getAllEditableDataForPage() {

        List<Element> elems = CmsDomUtil.getElementsByClass(CmsGwtConstants.CLASS_EDITABLE, Tag.ALL);
        List<CmsEditableDataJSO> result = Lists.newArrayList();
        for (Element elem : elems) {
            String jsonData = elem.getAttribute(CmsGwtConstants.ATTR_DATA_EDITABLE);
            CmsEditableDataJSO data = CmsEditableDataJSO.parseEditableData(jsonData);
            result.add(data);
        }
        return result;
    }

    /**
     * Returns the given element or it's closest ancestor with the given class.<p>
     *
     * Returns <code>null</code> if no appropriate element was found.<p>
     *
     * @param element the element
     * @param className the class name
     *
     * @return the matching element
     */
    public static Element getAncestor(Element element, String className) {

        if (element == null) {
            return null;
        }

        if (hasClass(className, element)) {
            return element;
        }
        if (element.getTagName().equalsIgnoreCase(Tag.body.name())) {
            return null;
        }
        return getAncestor(element.getParentElement(), className);
    }

    /**
     * Returns the given element or it's closest ancestor with the given tag name.<p>
     *
     * Returns <code>null</code> if no appropriate element was found.<p>
     *
     * @param element the element
     * @param tag the tag name
     *
     * @return the matching element
     */
    public static Element getAncestor(Element element, Tag tag) {

        if ((element == null) || (tag == null)) {
            return null;
        }
        if (element.getTagName().equalsIgnoreCase(tag.name())) {
            return element;
        }
        if (element.getTagName().equalsIgnoreCase(Tag.body.name())) {
            return null;
        }
        return getAncestor(element.getParentElement(), tag);
    }

    /**
     * Returns the given element or it's closest ancestor with the given tag and class.<p>
     *
     * Returns <code>null</code> if no appropriate element was found.<p>
     *
     * @param element the element
     * @param tag the tag name
     * @param className the class name
     *
     * @return the matching element
     */
    public static Element getAncestor(Element element, Tag tag, String className) {

        if (element.getTagName().equalsIgnoreCase(tag.name()) && hasClass(className, element)) {
            return element;
        }
        if (element.getTagName().equalsIgnoreCase(Tag.body.name())) {
            return null;
        }
        return getAncestor(element.getParentElement(), tag, className);
    }

    /**
     * Returns the computed style of the given element.<p>
     *
     * @param element the element
     * @param style the CSS property
     *
     * @return the currently computed style
     */
    public static String getCurrentStyle(Element element, Style style) {

        return getStyleImpl().getCurrentStyle(element, style.toString());
    }

    /**
     * Returns the computed style of the given element as floating point number.<p>
     *
     * @param element the element
     * @param style the CSS property
     *
     * @return the currently computed style
     */
    public static double getCurrentStyleFloat(Element element, Style style) {

        String currentStyle = getCurrentStyle(element, style);
        return CmsClientStringUtil.parseFloat(currentStyle);
    }

    /**
     * Returns the computed style of the given element as number.<p>
     *
     * @param element the element
     * @param style the CSS property
     *
     * @return the currently computed style
     */
    public static int getCurrentStyleInt(Element element, Style style) {

        String currentStyle = getCurrentStyle(element, style);
        return CmsClientStringUtil.parseInt(currentStyle);
    }

    /**
     * Determines the position of the list collector editable content.<p>
     *
     * @param editable the editable marker tag
     *
     * @return the position
     */
    public static CmsPositionBean getEditablePosition(Element editable) {

        CmsPositionBean result = new CmsPositionBean();
        int dummy = -999;
        // setting minimum height
        result.setHeight(20);
        result.setWidth(60);
        result.setLeft(dummy);
        result.setTop(dummy);
        Element sibling = editable.getNextSiblingElement();
        while ((sibling != null)
            && !CmsDomUtil.hasClass(CmsGwtConstants.CLASS_EDITABLE, sibling)
            && !CmsDomUtil.hasClass(CmsGwtConstants.CLASS_EDITABLE_END, sibling)) {
            // only consider element nodes

            if ((sibling.getNodeType() == Node.ELEMENT_NODE)
                && !sibling.getTagName().equalsIgnoreCase(Tag.script.name())) {
                if (!CmsDomUtil.hasClass(CmsGwtConstants.CLASS_EDITABLE_SKIP, sibling)) {
                    CmsPositionBean siblingPos = CmsPositionBean.generatePositionInfo(sibling);
                    result.setLeft(
                        ((result.getLeft() == dummy) || (siblingPos.getLeft() < result.getLeft()))
                        ? siblingPos.getLeft()
                        : result.getLeft());
                    result.setTop(
                        ((result.getTop() == dummy) || (siblingPos.getTop() < result.getTop()))
                        ? siblingPos.getTop()
                        : result.getTop());
                    result.setHeight(
                        ((result.getTop() + result.getHeight()) > (siblingPos.getTop() + siblingPos.getHeight()))
                        ? result.getHeight()
                        : (siblingPos.getTop() + siblingPos.getHeight()) - result.getTop());
                    result.setWidth(
                        ((result.getLeft() + result.getWidth()) > (siblingPos.getLeft() + siblingPos.getWidth()))
                        ? result.getWidth()
                        : (siblingPos.getLeft() + siblingPos.getWidth()) - result.getLeft());
                }
            }

            sibling = sibling.getNextSiblingElement();
        }
        if ((result.getTop() == dummy) && (result.getLeft() == dummy)) {
            result = CmsPositionBean.generatePositionInfo(editable);
        }
        if (result.getHeight() == -1) {
            // in case no height was set
            result = CmsPositionBean.generatePositionInfo(editable);
            result.setHeight(20);
            result.setWidth((result.getWidth() < 60) ? 60 : result.getWidth());
        }

        return result;
    }

    /**
     * Utility method to determine the effective background color.<p>
     *
     * @param element the element
     *
     * @return the background color
     */
    public static String getEffectiveBackgroundColor(Element element) {

        String backgroundColor = CmsDomUtil.getCurrentStyle(element, Style.backgroundColor);
        if ((CmsStringUtil.isEmptyOrWhitespaceOnly(backgroundColor)
            || isTransparent(backgroundColor)
            || backgroundColor.equals(StyleValue.inherit.toString()))) {

            if ((Document.get().getBody() != element) && (element.getParentElement() != null)) {

                backgroundColor = getEffectiveBackgroundColor(element.getParentElement());
            } else {
                // if body element has still no background color set default to white
                backgroundColor = "#FFFFFF";
            }
        }

        return backgroundColor;
    }

    /**
     * Returns all elements from the DOM with the given CSS class.<p>
     *
     * @param className the class name to look for
     *
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className) {

        return getElementsByClass(className, Tag.ALL, Document.get().getBody());
    }

    /**
     * Returns all elements with the given CSS class including the root element.<p>
     *
     * @param className the class name to look for
     * @param rootElement the root element of the search
     *
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className, Element rootElement) {

        return getElementsByClass(className, Tag.ALL, rootElement);

    }

    /**
     * Returns all elements from the DOM with the given CSS class and tag name.<p>
     *
     * @param className the class name to look for
     * @param tag the tag
     *
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className, Tag tag) {

        return getElementsByClass(className, tag, Document.get().getBody());
    }

    /**
     * Returns all elements with the given CSS class and tag name including the root element.<p>
     *
     * @param className the class name to look for
     * @param tag the tag
     * @param rootElement the root element of the search
     *
     * @return the matching elements
     */
    public static List<Element> getElementsByClass(String className, Tag tag, Element rootElement) {

        if ((rootElement == null) || (className == null) || (className.trim().length() == 0) || (tag == null)) {
            return null;
        }
        className = className.trim();
        List<Element> result = new ArrayList<Element>();
        if (internalHasClass(className, rootElement)) {
            result.add(rootElement);
        }
        NodeList<Element> elements = querySelectorAll(tag + "." + className, rootElement);
        for (int i = 0; i < elements.getLength(); i++) {
            result.add(elements.getItem(i));
        }
        return result;
    }

    /**
     * Returns the first direct child matching the given class name.<p>
     *
     * @param element the parent element
     * @param className the class name to match
     *
     * @return the child element
     */
    public static Element getFirstChildWithClass(Element element, String className) {

        NodeList<Node> children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            if (children.getItem(i).getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)children.getItem(i);
                if (child.hasClassName(className)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Gets the root &lt;html&gt; element.
     *
     * @return the html element
     */
    public static native Element getHtmlElement() /*-{
        return $wnd.document.querySelector("html");
    }-*/;

    /**
     * Returns the content height of the given iFrame element.<p>
     *
     * @param iframe the iFrame element
     *
     * @return the content height
     */
    public static native int getIFrameContentHeight(Element iframe)/*-{
        var doc = iframe.contentDocument ? iframe.contentDocument
                : iframe.contentWindow.document;
        var body = doc.body;
        var html = doc.documentElement;
        var height = Math.max(body.scrollHeight, body.offsetHeight,
                html.clientHeight, html.scrollHeight, html.offsetHeight);
        return height;
    }-*/;

    /**
     * Returns the element position relative to its siblings.<p>
     *
     * @param e the element to get the position for
     *
     * @return the position, or <code>-1</code> if not found
     */
    public static int getPosition(Element e) {

        NodeList<Node> childNodes = e.getParentElement().getChildNodes();
        for (int i = childNodes.getLength(); i >= 0; i--) {
            if (childNodes.getItem(i) == e) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the next ancestor to the element with an absolute, fixed or relative position.<p>
     *
     * @param child the element
     *
     * @return the positioning parent element (may be <code>null</code>)
     */
    public static Element getPositioningParent(Element child) {

        Element parent = child.getParentElement();
        while (parent != null) {
            String parentPositioning = CmsDomUtil.getCurrentStyle(parent, Style.position);
            if (Position.RELATIVE.getCssName().equals(parentPositioning)
                || Position.ABSOLUTE.getCssName().equals(parentPositioning)
                || Position.FIXED.getCssName().equals(parentPositioning)) {
                return parent;
            }
            parent = parent.getParentElement();
        }
        return RootPanel.getBodyElement();
    }

    /**
     * Gets the horizontal position of the given x-coordinate relative to a given element.<p>
     *
     * @param x the coordinate to use
     * @param target the element whose coordinate system is to be used
     *
     * @return the relative horizontal position
     *
     * @see com.google.gwt.event.dom.client.MouseEvent#getRelativeX(com.google.gwt.dom.client.Element)
     */
    public static int getRelativeX(int x, Element target) {

        return (x - target.getAbsoluteLeft()) + /* target.getScrollLeft() + */target.getOwnerDocument().getScrollLeft();
    }

    /**
     * Gets the vertical position of the given y-coordinate relative to a given element.<p>
     *
     * @param y the coordinate to use
     * @param target the element whose coordinate system is to be used
     *
     * @return the relative vertical position
     *
     * @see com.google.gwt.event.dom.client.MouseEvent#getRelativeY(com.google.gwt.dom.client.Element)
     */
    public static int getRelativeY(int y, Element target) {

        return (y - target.getAbsoluteTop()) + /* target.getScrollTop() +*/target.getOwnerDocument().getScrollTop();
    }

    /**
     * Measures the scroll bar width.<p>
     *
     * @return the scroll bar width
     */
    public static int getScrollbarWidth() {

        if (m_scrollbarWidth == -1) {
            Element div = DOM.createDiv();
            div.setAttribute("style", "width:100px; height:100px; overflow: scroll; position:absolute; top:-9999px;");
            RootPanel.getBodyElement().appendChild(div);
            m_scrollbarWidth = div.getOffsetWidth() - div.getClientWidth();
            div.removeFromParent();
        }
        return m_scrollbarWidth;
    }

    /**
     * Returns the DOM window object.<p>
     *
     * @return the DOM window object
     */
    public static native JavaScriptObject getWindow() /*-{
        return $wnd;
    }-*/;

    /**
     * Returns the Z index from the given style.<p>
     *
     * This is a workaround for a bug with {@link com.google.gwt.dom.client.Style#getZIndex()} which occurs with IE in
     * hosted mode.<p>
     *
     * @param style the style object from which the Z index property should be fetched
     *
     * @return the z index
     */
    public static native String getZIndex(com.google.gwt.dom.client.Style style)
    /*-{
        return "" + style.zIndex;
    }-*/;

    /**
     * Utility method to determine if the given element has a set background.<p>
     *
     * @param element the element
     *
     * @return <code>true</code> if the element has a background set
     */
    public static boolean hasBackground(Element element) {

        String backgroundColor = CmsDomUtil.getCurrentStyle(element, Style.backgroundColor);
        String backgroundImage = CmsDomUtil.getCurrentStyle(element, Style.backgroundImage);
        if ((isTransparent(backgroundColor))
            && ((backgroundImage == null)
                || (backgroundImage.trim().length() == 0)
                || backgroundImage.equals(StyleValue.none.toString()))) {
            return false;
        }
        return true;
    }

    /**
     * Utility method to determine if the given element has a set background image.<p>
     *
     * @param element the element
     *
     * @return <code>true</code> if the element has a background image set
     */
    public static boolean hasBackgroundImage(Element element) {

        String backgroundImage = CmsDomUtil.getCurrentStyle(element, Style.backgroundImage);
        if ((backgroundImage == null)
            || (backgroundImage.trim().length() == 0)
            || backgroundImage.equals(StyleValue.none.toString())) {
            return false;
        }
        return true;
    }

    /**
     * Utility method to determine if the given element has a set border.<p>
     *
     * @param element the element
     *
     * @return <code>true</code> if the element has a border
     */
    public static boolean hasBorder(Element element) {

        String borderStyle = CmsDomUtil.getCurrentStyle(element, Style.borderStyle);
        if ((borderStyle == null) || borderStyle.equals(StyleValue.none.toString()) || (borderStyle.length() == 0)) {
            return false;
        }
        return true;

    }

    /**
     * Indicates if the given element has a CSS class.<p>
     *
     * @param className the class name to look for
     * @param element the element
     *
     * @return <code>true</code> if the element has the given CSS class
     */
    public static boolean hasClass(String className, Element element) {

        return internalHasClass(className.trim(), element);
    }

    /**
     * Returns if the given element has any dimension.<p>
     * All visible elements should have a dimension.<p>
     *
     * @param element the element to test
     *
     * @return <code>true</code> if the given element has any dimension
     */
    public static boolean hasDimension(Element element) {

        return (element.getOffsetHeight() > 0) || (element.getOffsetWidth() > 0);
    }

    /**
     * Checks whether the copy command is supported by the client browser.<p>
     *
     * @return <code>true</code> if the copy command is supported
     */
    public static native boolean isCopyToClipboardSupported()/*-{
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
     * Checks whether a given script resource is present within the window context.<p>
     *
     * @param javascriptLink the resource URL
     *
     * @return <code>true</code> if the script resource is present within the window context
     */
    public static native boolean isJavaScriptPresent(String javascriptLink)/*-{
        var scripts = $wnd.document.scripts;
        for (var i = 0; i < scripts.length; i++) {
            if (scripts[i].src != null
                    && scripts[i].src.indexOf(javascriptLink) >= 0) {
                // script resource is present
                return true;
            }
        }
        return false;
    }-*/;

    /**
     * Gives an element the overflow:auto property.<p>
     *
     * @param elem a DOM element
     */
    public static void makeScrollable(Element elem) {

        elem.getStyle().setOverflow(Overflow.AUTO);
    }

    /**
     * Gives the element of a widget the overflow:auto property.<p>
     *
     * @param widget the widget to make scrollable
     */
    public static void makeScrollable(Widget widget) {

        makeScrollable(widget.getElement());
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String messagePopupBlocked() {

        return Messages.get().key(Messages.GUI_POPUP_BLOCKED_0);
    }

    /**
     * Message accessor.<p>
     *
     * @return the message string
     */
    public static String messagePopupBlockedTitle() {

        return Messages.get().key(Messages.GUI_POPUP_BLOCKED_TITLE_0);
    }

    /**
     * Converts a NodeList to a List of elements.<p>
     *
     * @param nodelist the node list
     * @return the list of elements
     */
    public static List<Element> nodeListToList(NodeList<Element> nodelist) {

        List<Element> result = Lists.newArrayList();
        for (int i = 0; i < nodelist.getLength(); i++) {
            result.add(nodelist.getItem(i));
        }
        return result;
    }

    /**
     * Generates an opening tag.<p>
     *
     * @param tag the tag to use
     * @param attrs the optional tag attributes
     *
     * @return HTML code
     */
    public static String open(Tag tag, AttributeValue... attrs) {

        StringBuffer sb = new StringBuffer();
        sb.append("<").append(tag.name());
        for (AttributeValue attr : attrs) {
            sb.append(" ").append(attr.toString());
        }
        sb.append(">");
        return sb.toString();
    }

    /**
     * Opens a new browser window. The "name" and "features" arguments are
     * specified <a href=
     * 'http://developer.mozilla.org/en/docs/DOM:window.open'>here</a>.
     *
     * @param url the URL that the new window will display
     * @param name the name of the window (e.g. "_blank")
     * @param features the features to be enabled/disabled on this window
     */
    public static native void openWindow(String url, String name, String features) /*-{
        var w = $wnd.open(url, name, features);
        if (!w) {
            @org.opencms.gwt.client.util.CmsDomUtil::showPopupBlockerMessage()();
        }
    }-*/;

    /**
     * Parses the given string into a JSON object.<p>
     *
     * @param jsonString the string to parse
     *
     * @return the JSON object
     */
    public static native JavaScriptObject parseJSON(String jsonString)/*-{
        return (typeof $wnd.JSON != 'undefined') && $wnd.JSON.parse(jsonString)
                || eval('(' + jsonString + ')');
    }-*/;

    /**
     * Positions an element in the DOM relative to another element.<p>
     *
     * @param elem the element to position
     * @param referenceElement the element relative to which the first element should be positioned
     * @param dx the x offset relative to the reference element
     * @param dy the y offset relative to the reference element
     */
    public static void positionElement(Element elem, Element referenceElement, int dx, int dy) {

        com.google.gwt.dom.client.Style style = elem.getStyle();
        style.setLeft(0, Unit.PX);
        style.setTop(0, Unit.PX);
        int myX = elem.getAbsoluteLeft();
        int myY = elem.getAbsoluteTop();
        int refX = referenceElement.getAbsoluteLeft();
        int refY = referenceElement.getAbsoluteTop();
        int newX = (refX - myX) + dx;
        int newY = (refY - myY) + dy;
        style.setLeft(newX, Unit.PX);
        style.setTop(newY, Unit.PX);
    }

    /**
     * Positions an element inside the given parent, reordering the content of the parent and returns the new position index.<p>
     * This is none absolute positioning. Use for drag and drop reordering of drop targets.<p>
     * Use <code>-1</code> for x or y to ignore one ordering orientation.<p>
     *
     * @param element the child element
     * @param parent the parent element
     * @param currentIndex the current index position of the element, use -1 if element is not attached to the parent yet
     * @param x the client x position, use <code>-1</code> to ignore x position
     * @param y the client y position, use <code>-1</code> to ignore y position
     *
     * @return the new index position
     */
    public static int positionElementInside(Element element, Element parent, int currentIndex, int x, int y) {

        if ((x == -1) && (y == -1)) {
            // this is wrong usage, do nothing
            CmsDebugLog.getInstance().printLine("this is wrong usage, doing nothing");
            return currentIndex;
        }
        int indexCorrection = 0;
        int previousTop = 0;
        for (int index = 0; index < parent.getChildCount(); index++) {
            Node node = parent.getChild(index);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element)node;
            if (child == element) {
                indexCorrection = 1;
            }
            String positioning = CmsDomUtil.getCurrentStyle(child, Style.position);
            if (Position.ABSOLUTE.getCssName().equals(positioning) || Position.FIXED.getCssName().equals(positioning)) {
                // only not 'position:absolute' elements into account,
                // not visible children will be excluded in the next condition
                continue;
            }
            int left = 0;
            int width = 0;
            int top = 0;
            int height = 0;
            if (y != -1) {
                // check if the mouse pointer is within the height of the element
                top = CmsDomUtil.getRelativeY(y, child);
                height = child.getOffsetHeight();
                if ((top <= 0) || (top >= height)) {
                    previousTop = top;
                    continue;
                }
            }
            if (x != -1) {
                // check if the mouse pointer is within the width of the element
                left = CmsDomUtil.getRelativeX(x, child);
                width = child.getOffsetWidth();
                if ((left <= 0) || (left >= width)) {
                    previousTop = top;
                    continue;
                }
            }

            boolean floatSort = false;
            String floating = "";
            if ((top != 0) && (top == previousTop)) {
                floating = getCurrentStyle(child, Style.floatCss);
                if ("left".equals(floating) || "right".equals(floating)) {
                    floatSort = true;
                }
            }
            previousTop = top;
            if (child == element) {
                return currentIndex;
            }
            if ((y == -1) || floatSort) {
                boolean insertBefore = false;
                if (left < (width / 2)) {
                    if (!(floatSort && "right".equals(floating))) {
                        insertBefore = true;
                    }
                } else if (floatSort && "right".equals(floating)) {
                    insertBefore = true;
                }
                if (insertBefore) {
                    parent.insertBefore(element, child);
                    currentIndex = index - indexCorrection;
                    return currentIndex;
                } else {
                    parent.insertAfter(element, child);
                    currentIndex = (index + 1) - indexCorrection;
                    return currentIndex;
                }
            }
            if (top < (height / 2)) {
                parent.insertBefore(element, child);
                currentIndex = index - indexCorrection;
                return currentIndex;
            } else {
                parent.insertAfter(element, child);
                currentIndex = (index + 1) - indexCorrection;
                return currentIndex;
            }

        }
        // not over any child position
        if ((currentIndex >= 0) && (element.getParentElement() == parent)) {
            // element is already attached to this parent and no new position available
            // don't do anything
            return currentIndex;
        }
        int top = CmsDomUtil.getRelativeY(y, parent);
        int offsetHeight = parent.getOffsetHeight();
        if ((top >= (offsetHeight / 2))) {
            // over top half, insert as first child
            parent.insertFirst(element);
            currentIndex = 0;
            return currentIndex;
        }
        // over bottom half, insert as last child
        parent.appendChild(element);
        currentIndex = parent.getChildCount() - 1;
        return currentIndex;
    }

    /**
     * Returns the first element matching the given CSS selector.<p>
     *
     * @param selector the CSS selector
     * @param context the context element, may be <code>null</code>
     *
     * @return the matching element
     */
    public static native Element querySelector(String selector, Element context)/*-{
        if (context != null) {
            return context.querySelector(selector);
        } else {
            return $doc.querySelector(selector);
        }
    }-*/;

    /**
     * Returns a list of elements matching the given CSS selector.<p>
     *
     * @param selector the CSS selector
     * @param context the context element, may be <code>null</code>
     *
     * @return the list of matching elements
     */
    public static native NodeList<Element> querySelectorAll(String selector, Element context)/*-{
        if (context != null) {
            return context.querySelectorAll(selector);
        } else {
            $doc.querySelectorAll(selector);
        }
    }-*/;

    /**
     * Removes any present overlay from the element and it's children.<p>
     *
     * @param element the element
     */
    public static void removeDisablingOverlay(Element element) {

        List<Element> overlays = CmsDomUtil.getElementsByClass(
            I_CmsLayoutBundle.INSTANCE.generalCss().disablingOverlay(),
            Tag.div,
            element);
        if (overlays == null) {
            return;
        }
        for (Element overlay : overlays) {
            overlay.getParentElement().getStyle().clearPosition();
            overlay.removeFromParent();
        }
        element.removeClassName(I_CmsLayoutBundle.INSTANCE.generalCss().hideOverlay());
    }

    /**
     * Removes all script tags from the given element.<p>
     *
     * @param element the element to remove the script tags from
     *
     * @return the resulting element
     */
    public static Element removeScriptTags(Element element) {

        NodeList<Element> scriptTags = element.getElementsByTagName(Tag.script.name());
        // iterate backwards over list to ensure all tags get removed
        for (int i = scriptTags.getLength() - 1; i >= 0; i--) {
            scriptTags.getItem(i).removeFromParent();
        }
        return element;
    }

    /**
     * Removes all script tags from the given string.<p>
     *
     * @param source the source string
     *
     * @return the resulting string
     */
    public static native String removeScriptTags(String source)/*-{

        var matchTag = /<script[^>]*?>[\s\S]*?<\/script>/g;
        return source.replace(matchTag, "");
    }-*/;

    /**
     * Calls {@link org.opencms.gwt.client.I_CmsDescendantResizeHandler#onResizeDescendant()} on the closest resizable ancestor.<p>
     *
     * @param parent the parent widget
     */
    public static void resizeAncestor(Widget parent) {

        while (parent != null) {
            if (parent instanceof I_CmsDescendantResizeHandler) {
                ((I_CmsDescendantResizeHandler)parent).onResizeDescendant();
                return;
            } else {
                parent = parent.getParent();
            }
        }
    }

    /**
     * Loads a list of stylesheets and invokes a Javascript callback after everything has been loaded.<p>
     *
     * @param stylesheets the array of stylesheet uris
     * @param callback the callback to call after everything is loaded
     */
    public static void safeLoadStylesheets(String[] stylesheets, JavaScriptObject callback) {

        CmsStylesheetLoader loader = new CmsStylesheetLoader(Arrays.asList(stylesheets), new Runnable() {

            public native void call(JavaScriptObject jsCallback) /*-{
        jsCallback();
    }-*/;

            public void run() {

                if (callback != null) {
                    call(callback);
                }
            }

        });
        loader.loadWithTimeout(5000);
    }

    /**
     * Sets an attribute on a Javascript object.<p>
     *
     * @param jso the Javascript object
     * @param key the attribute name
     * @param value the new attribute value
     */
    public static native void setAttribute(JavaScriptObject jso, String key, JavaScriptObject value) /*-{
        jso[key] = value;
    }-*/;

    /**
     * Sets an attribute on a Javascript object.<p>
     *
     * @param jso the Javascript object
     * @param key the attribute name
     * @param value the new attribute value
     */
    public static native void setAttribute(JavaScriptObject jso, String key, String value) /*-{
        jso[key] = value;
    }-*/;

    /**
     * Sets the stylesheet text for the stylesheet with the given ID.<p>
     *
     * If the stylesheet with the id does not already exist, it is created.
     *
     * @param id the stylesheet id
     * @param styleText the stylesheet text
     */
    public static void setStylesheetText(String id, String styleText) {

        Document document = Document.get();
        Element elem = document.getElementById(id);
        if (elem == null) {
            elem = document.createStyleElement();
            elem.setId(id);
            document.getHead().appendChild(elem);
        }
        elem.setInnerHTML(styleText);
    }

    /**
     * Sets a CSS class to show or hide a given overlay. Will not add an overlay to the element.<p>
     *
     * @param element the parent element of the overlay
     * @param show <code>true</code> to show the overlay
     */
    public static void showOverlay(Element element, boolean show) {

        if (show) {
            element.removeClassName(I_CmsLayoutBundle.INSTANCE.generalCss().hideOverlay());
        } else {
            element.addClassName(I_CmsLayoutBundle.INSTANCE.generalCss().hideOverlay());
        }
    }

    /**
     * Shows a message that a popup was blocked.<p>
     */
    public static void showPopupBlockerMessage() {

        CmsAlertDialog alertDialog = new CmsAlertDialog(messagePopupBlockedTitle(), messagePopupBlocked());
        alertDialog.center();
    }

    /**
     * Returns the text content to any HTML.
     *
     * @param html the HTML
     *
     * @return the text content
     */
    public static String stripHtml(String html) {

        if (html == null) {
            return null;
        }
        Element el = DOM.createDiv();
        el.setInnerHTML(html);
        return el.getInnerText();
    }

    /**
     * Updates a set of style properties on the given style object, and returns a map with the previous values.
     *
     * If a value in the map is null, it is interpreted as clearing the style property with that name.
     *
     * @param style the style object to update
     * @param properties the map of properties to change
     *
     * @return the map of previous values of the given properties
     */
    public static Map<String, String> updateStyle(
        com.google.gwt.dom.client.Style style,
        Map<String, String> properties) {

        Map<String, String> oldProps = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String prop = entry.getKey();
            String value = entry.getValue();
            String oldValue = style.getProperty(prop);
            oldProps.put(prop, oldValue);
            if (value != null) {
                style.setProperty(prop, value);
            } else {
                style.clearProperty(prop);
            }

        }
        return oldProps;
    }

    /**
     * Wraps a widget in a scrollable FlowPanel.<p>
     *
     * @param widget the original widget
     * @return the wrapped widget
     */
    public static FlowPanel wrapScrollable(Widget widget) {

        FlowPanel wrapper = new FlowPanel();
        wrapper.add(widget);
        makeScrollable(wrapper);
        return wrapper;
    }

    /**
     * Creates a hidden input field with the given name and value.<p>
     *
     * @param name the field name
     * @param value the field value
     * @return the input element
     */
    private static InputElement createHiddenInput(String name, String value) {

        InputElement input = Document.get().createHiddenInputElement();
        input.setName(name);
        input.setValue(value);
        return input;
    }

    /**
     * Returns the DOM implementation.<p>
     *
     * @return the DOM implementation
     */
    private static DOMImpl getDOMImpl() {

        if (domImpl == null) {
            domImpl = GWT.create(DOMImpl.class);
        }
        return domImpl;
    }

    /**
     * Returns the document style implementation.<p>
     *
     * @return the document style implementation
     */
    private static DocumentStyleImpl getStyleImpl() {

        if (styleImpl == null) {
            styleImpl = GWT.create(DocumentStyleImpl.class);
        }
        return styleImpl;
    }

    /**
     * Injects a script tag into the page head.<p>
     *
     * @param scriptLink the link to the javascript resource
     * @param async the value for the async attribute of the new script node
     * @param onload load handler for the script
     */
    private static native void injectScript(String scriptLink, boolean async, JavaScriptObject onload)/*-{
        var headID = $wnd.document.getElementsByTagName("head")[0];
        var scriptNode = $wnd.document.createElement('script');
        scriptNode.async = async;
        if (onload) {
            scriptNode.onload = onload;
        }
        scriptNode.src = scriptLink;
        headID.appendChild(scriptNode);
    }-*/;

    /**
     * Internal method to indicate if the given element has a CSS class.<p>
     *
     * @param className the class name to look for
     * @param element the element
     *
     * @return <code>true</code> if the element has the given CSS class
     */
    private static boolean internalHasClass(String className, Element element) {

        boolean hasClass = false;
        try {
            String elementClass = element.getClassName().trim();
            hasClass = elementClass.equals(className);
            hasClass |= elementClass.contains(" " + className + " ");
            hasClass |= elementClass.startsWith(className + " ");
            hasClass |= elementClass.endsWith(" " + className);
        } catch (Throwable t) {
            // ignore
        }
        return hasClass;
    }

    /**
     * Checks if the given color value is transparent.<p>
     *
     * @param backgroundColor the color value
     *
     * @return <code>true</code> if transparent
     */
    private static boolean isTransparent(String backgroundColor) {

        // not only check 'transparent' but also 'rgba(0, 0, 0, 0)' as returned by chrome
        return StyleValue.transparent.toString().equalsIgnoreCase(backgroundColor)
            || "rgba(0, 0, 0, 0)".equalsIgnoreCase(backgroundColor);
    }

}

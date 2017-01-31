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

package org.opencms.ui.components.codemirror;

import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONObject;
import org.opencms.ui.components.Messages;
import org.opencms.workplace.CmsWorkplace;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.json.client.JSONException;
import com.vaadin.annotations.JavaScript;
import com.vaadin.data.Property;
import com.vaadin.event.FieldEvents;
import com.vaadin.shared.ui.JavaScriptComponentState;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;

import elemental.json.JsonArray;

/**
 * Code mirror input component.<p>
 */
@JavaScript({"codemirror-connector.js"})
public class CmsCodeMirror extends AbstractJavaScriptComponent
implements Property<String>, Property.ValueChangeNotifier {

    /** The available editor languages. */
    public enum CodeMirrorLanguage {
        /** CSS. */
        CSS("css", new String[] {"css"}),
        /** HTML. */
        HTML("text/html", new String[] {"html", "htm", "xhtml"}),
        /** JAVA. */
        JAVA("text/x-java", new String[] {"java"}),
        /** JavaScript. */
        JAVASCRIPT("javascript", new String[] {"js"}),
        /** JSP. */
        JSP("application/x-jsp", new String[] {"jsp"}),
        /** XML. */
        XML("application/xml", new String[] {"xml", "xsd"});

        /** The language name. */
        private final String m_languageName;

        /** The supported file types. */
        private Set<String> m_supportedFileTypes;

        /**
         * Constructor.<p>
         *
         * @param languageName the language name
         * @param fileTypes the file types suported by this language
         */
        private CodeMirrorLanguage(String languageName, String[] fileTypes) {
            m_languageName = languageName;
            m_supportedFileTypes = new HashSet<String>(Arrays.asList(fileTypes));
        }

        /**
         * Returns the language name.<p>
         *
         * @return the language name
         */
        public String getLanguageName() {

            return m_languageName;
        }

        /**
         * Returns whether the given file ending is a supported file type.<p>
         *
         * @param fileNameSuffix the file name suffix
         *
         * @return <code>true</code> in case the file type is supported
         */
        public boolean isSupportedFileType(String fileNameSuffix) {

            return m_supportedFileTypes.contains(fileNameSuffix);
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            return m_languageName;
        }
    }

    /**
     * The editor state.<p>
     */
    public static class CodeMirrorState extends JavaScriptComponentState {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /** The close brackets flag. */
        public boolean m_closeBrackets = true;

        /** The content value. */
        public String m_contentValue = "//please start to code";

        /** The required CSS stylesheet URIs. */
        public String[] m_cssURIs;

        /** The search and replace enabled flag. */
        public boolean m_enableSearchReplace = false;

        /** The undo redo enabled flag. */
        public boolean m_enableUndoRedo = false;

        /** The font size. */
        public String m_fontSize = "14px";

        /** The height. */
        public String m_height = "600";

        /** The highlighting flag. */
        public boolean m_highlighting = true;

        /** The editor instance id. */
        public long m_id;

        /** The line wrapping flag. */
        public boolean m_lineWrapping = true;

        /** The language mode. */
        public String m_mode = CodeMirrorLanguage.JAVASCRIPT.toString();

        /** The required JavaScript resource URIs. */
        public String[] m_scriptURIs;

        /** The short cut descriptions HTML. */
        public String m_shortcutsMessage;

        /** The tab visibility flag. */
        public boolean m_tabsVisible = true;

        /** The theme. */
        public String m_theme = CodeMirrorTheme.ECLIPSE.toString();

        /** The width. */
        public String m_width = "600";

        /** The code mirror localization. */
        public String m_messages;
    }

    /** The available editor themes. */
    public enum CodeMirrorTheme {
        /** Theme. */
        AMBIANCE("ambiance"),
        /** Theme. */
        BASE16_DARK("base16-dark"),
        /** Theme. */
        BASE16_LIGHT("base16-light"),
        /** Theme. */
        BLACKBOARD("blackboard"),
        /** Theme. */
        COBALT("cobalt"),
        /** Theme. */
        DAY_3024("3024-day"),
        /** Theme. */
        DEFAULT("default"),
        /** Theme. */
        ECLIPSE("eclipse"),
        /** Theme. */
        ELEGANT("elegant"),
        /** Theme. */
        ERLANG_DARK("erlang-dark"),
        /** Theme. */
        ERLANG_LIGHT("erlang-light"),
        /** Theme. */
        LESSER_DARK("lesser-dark"),
        /** Theme. */
        MBO("mbo"),
        /** Theme. */
        MDN_LIKE("mdn-like"),
        /** Theme. */
        MIDNIGHT("midnight"),
        /** Theme. */
        MONOKAI("monokai"),
        /** Theme. */
        NEAT("neat"),
        /** Theme. */
        NIGHT("night"),
        /** Theme. */
        NIGHT_3024("3024-night"),
        /** Theme. */
        PARAISO_DARK("paraiso-dark"),
        /** Theme. */
        PARAISO_LIGHT("paraiso-light"),
        /** Theme. */
        PASTEL_ON_DARK("paster-on-dark"),
        /** Theme. */
        RUBYBLUE("rubyblue"),
        /** Theme. */
        SOLARIZED_DARK("solarized dark"),
        /** Theme. */
        SOLARIZED_LIGHT("solarized light"),
        /** Theme. */
        THE_MATRIX("the-matrix"),
        /** Theme. */
        TOMORROW_NIGHT_EIGHTIES("tomorrow-night-eighties"),
        /** Theme. */
        TWILIGHT("twilight"),
        /** Theme. */
        VIBRANT_INK("vibrant-ink"),
        /** Theme. */
        XQ_DARK("xq-dark"),
        /** Theme. */
        XQ_LIGHT("xq-light");

        /** The theme name. */
        private final String m_themeName;

        /**
         * Constructor.<p>
         *
         * @param themeName the theme name
         */
        private CodeMirrorTheme(String themeName) {
            this.m_themeName = themeName;
        }

        /**
         * Returns the theme name.<p>
         *
         * @return the theme name
         */
        public String getThemeName() {

            return m_themeName;
        }

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {

            return m_themeName;
        }

    }

    /**
     * The code mirror change event.<p>
     */
    public static class ValueChangeEvent extends Component.Event implements Property.ValueChangeEvent {

        /** The serial version id. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new event object with the specified source field object.
         *
         * @param eventSource the field that caused the event.
         */
        public ValueChangeEvent(CmsCodeMirror eventSource) {
            super(eventSource);
        }

        /**
         * Gets the Property which triggered the event.
         *
         * @return the Source Property of the event.
         */
        @Override
        public Property getProperty() {

            return (Property<?>)getSource();
        }
    }

    /** The blur method. */
    private static final Method BLUR_METHOD;

    /** The required CSS stylesheet URIs. */
    private static String[] CSS_URIS = null;

    /** The HTML id prefix. */
    private static final String HTML_ID_PREFIX = "cm-addon-";

    /** The required JavaScript resource URIs. */
    private static String[] JAVASCRIPT_URIS = null;

    /** The code mirror component count, used as HTML ids. */
    private static long m_componentCount = 0;

    /** The serial version id. */
    private static final long serialVersionUID = -4921119175861329688L;

    /** Them value change method. */
    private static final Method VALUE_CHANGE_METHOD;

    static {
        try {
            VALUE_CHANGE_METHOD = Property.ValueChangeListener.class.getDeclaredMethod(
                "valueChange",
                new Class[] {Property.ValueChangeEvent.class});
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException("Internal error finding methods in ValueChangeListener");
        }

        try {
            BLUR_METHOD = FieldEvents.BlurListener.class.getDeclaredMethod(
                "blur",
                new Class[] {FieldEvents.BlurEvent.class});
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException("Internal error finding methods in ValueChangeListener");
        }
    }

    /** The current content value. */
    private String m_codeValue;

    /** The component id. */
    private final long m_componentId;

    /**
     * Constructor.<p>
     */
    public CmsCodeMirror() {
        m_componentId = m_componentCount;
        m_componentCount++;
        m_codeValue = "";
        getState().m_id = m_componentId;
        getState().m_contentValue = m_codeValue;
        CmsMessages messages = Messages.get().getBundle(UI.getCurrent().getLocale());
        getState().m_shortcutsMessage = getShortcutMessages(messages);
        getState().m_messages = getLocalizationMessages(messages);

        if (CSS_URIS == null) {
            CSS_URIS = new String[] {
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/lib/codemirror.css"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/theme/eclipse.css"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/dialog/dialog.css"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/hint/show-hint.css")};
        }
        getState().m_cssURIs = CSS_URIS;
        if (JAVASCRIPT_URIS == null) {
            JAVASCRIPT_URIS = new String[] {
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/lib/codemirror.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/dialog/dialog.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/search/searchcursor.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/js/search.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/edit/closebrackets.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/edit/closetag.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/edit/matchbrackets.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/hint/show-hint.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/hint/html-hint.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/hint/javascript-hint.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/hint/xml-hint.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/fold/foldcode.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/fold/brace-fold.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/fold/xml-fold.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/comment/comment.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/addon/selection/active-line.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/mode/css/css.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/mode/xml/xml.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/mode/clike/clike.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/mode/javascript/javascript.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/mode/css/css.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/dist/mode/htmlmixed/htmlmixed.js"),
                CmsWorkplace.getStaticResourceUri("/editors/codemirror/js/htmlembedded_modified.js")};
        }
        getState().m_scriptURIs = JAVASCRIPT_URIS;

        addFunction("onBlur", new JavaScriptFunction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void call(JsonArray arguments) throws JSONException {

                onBlur(arguments.getString(0));
            }
        });
        addFunction("onChange", new JavaScriptFunction() {

            private static final long serialVersionUID = 1L;

            @Override
            public void call(JsonArray arguments) throws JSONException {

                onChange(arguments.getString(0));
            }
        });
        addStyleName("o-codemirror");
    }

    /**
     * Adds a blur listener.<p>
     *
     * @param listener the blur listener
     */
    public void addBlurListener(FieldEvents.BlurListener listener) {

        addListener(FieldEvents.BlurEvent.class, listener, BLUR_METHOD);
        markAsDirty();
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#addListener(com.vaadin.data.Property.ValueChangeListener)
     * @deprecated use {@link #addValueChangeListener(com.vaadin.data.Property.ValueChangeListener)}
     */
    @Deprecated
    public void addListener(com.vaadin.data.Property.ValueChangeListener listener) {

        addValueChangeListener(listener);
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#addValueChangeListener(com.vaadin.data.Property.ValueChangeListener)
     */
    public void addValueChangeListener(Property.ValueChangeListener listener) {

        addListener(Property.ValueChangeEvent.class, listener, VALUE_CHANGE_METHOD);
        // ensure "automatic immediate handling" works
        markAsDirty();
    }

    /**
     * Returns the font size.<p>
     *
     * @return the font size
     */
    public String getFontSize() {

        return getState().m_fontSize;
    }

    /**
     * @see com.vaadin.data.Property#getType()
     */
    public Class<? extends String> getType() {

        return String.class;
    }

    /**
     * Returns the current editor content value.<p>
     *
     * @return the editor content value
     */
    public String getValue() {

        return m_codeValue;
    }

    /**
     * Returns whether auto close brackets is active.<p>
     *
     * @return <code>true</code> if auto close brackets is active
     */
    public boolean isCloseBrackets() {

        return getState().m_closeBrackets;
    }

    /**
     * Returns whether highlighting is active.<p>
     *
     * @return <code>true</code> if highlighting is active
     */
    public boolean isHighlighting() {

        return getState().m_highlighting;
    }

    /**
     * Returns whether line wrapping is active.<p>
     *
     * @return <code>true</code> if line wrapping is active
     */
    public boolean isLineWrapping() {

        return getState().m_lineWrapping;
    }

    /**
     * Returns whether tabs are visible.<p>
     *
     * @return <code>true</code> if tabs are visible
     */
    public boolean isTabsVisible() {

        return getState().m_tabsVisible;
    }

    /**
     * Registers the given buttons as the search and replace buttons.<p>
     *
     * @param search the search button
     * @param replace the replace button
     */
    public void registerSearchReplace(Button search, Button replace) {

        if (getState().m_enableSearchReplace) {
            throw new RuntimeException("Search/replace already registered.");
        }
        search.setId(HTML_ID_PREFIX + m_componentId + "-search");
        replace.setId(HTML_ID_PREFIX + m_componentId + "-replace");
        getState().m_enableSearchReplace = true;
        markAsDirty();
    }

    /**
     * Registers the given buttons as undo redo buttons
     *
     * @param undo the undo button
     * @param redo the redo button
     */
    public void registerUndoRedo(Button undo, Button redo) {

        if (getState().m_enableUndoRedo) {
            throw new RuntimeException("Undo/redo already registered.");
        }
        undo.setId(HTML_ID_PREFIX + m_componentId + "-undo");
        redo.setId(HTML_ID_PREFIX + m_componentId + "-redo");
        getState().m_enableUndoRedo = true;
        markAsDirty();
    }

    /**
     * Removes the given blur listener.<p>
     *
     * @param listener the listener to remove
     */
    public void removeBlurListener(FieldEvents.BlurListener listener) {

        removeListener(FieldEvents.BlurEvent.class, listener, BLUR_METHOD);
        // ensure "automatic immediate handling" works
        markAsDirty();
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#removeListener(com.vaadin.data.Property.ValueChangeListener)
     * @deprecated use {@link #removeValueChangeListener(com.vaadin.data.Property.ValueChangeListener)}
     */
    @Deprecated
    public void removeListener(com.vaadin.data.Property.ValueChangeListener listener) {

        removeValueChangeListener(listener);
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#removeValueChangeListener(com.vaadin.data.Property.ValueChangeListener)
     */
    public void removeValueChangeListener(Property.ValueChangeListener listener) {

        removeListener(Property.ValueChangeEvent.class, listener, VALUE_CHANGE_METHOD);
        markAsDirty();
    }

    /**
     * Sets the auto close brackets feature enabled.<p>
     *
     * @param closeBrackets <code>true</code> to auto close brackets
     */
    public void setCloseBrackets(boolean closeBrackets) {

        getState().m_closeBrackets = closeBrackets;
        markAsDirty();
    }

    /**
     * Sets the editor font size.<p>
     *
     * @param fontSize the editor font size
     */
    public void setFontSize(String fontSize) {

        getState().m_fontSize = fontSize;
        markAsDirty();
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#setHeight(float, com.vaadin.server.Sizeable.Unit)
     */
    @Override
    public void setHeight(float height, Unit unit) {

        super.setHeight(height, unit);
        getState().m_height = height + unit.getSymbol();
        markAsDirty();
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#setHeight(java.lang.String)
     */
    @Override
    public void setHeight(String height) {

        super.setHeight(height);
        getState().m_height = height;
        markAsDirty();
    }

    /**
     * Sets the language highlighting enabled.<p>
     *
     * @param highlighting <code>true</code> to highlight
     */
    public void setHighlighting(boolean highlighting) {

        getState().m_highlighting = highlighting;
        markAsDirty();
    }

    /**
     * Sets the editor language.<p>
     *
     * @param codeMirrorLanguage the editor language
     */
    public void setLanguage(CodeMirrorLanguage codeMirrorLanguage) {

        getState().m_mode = codeMirrorLanguage.getLanguageName();
        markAsDirty();
    }

    /**
     * Sets line wrapping enabled.<p>
     *
     * @param lineWrapping <code>true</code> to wrap lines
     */
    public void setLineWrapping(boolean lineWrapping) {

        getState().m_lineWrapping = lineWrapping;
        markAsDirty();
    }

    /**
     * Sets tab characters visible.<p>
     *
     * @param tabsVisible <code>true</code> to show tab characters
     */
    public void setTabsVisible(boolean tabsVisible) {

        getState().m_tabsVisible = tabsVisible;
        markAsDirty();
    }

    /**
     * Sets the editor theme.<p>
     *
     * @param codeMirrorTheme the editor theme
     */
    public void setTheme(CodeMirrorTheme codeMirrorTheme) {

        //   getState().m_id = m_componentId;
        getState().m_theme = codeMirrorTheme.getThemeName();
        markAsDirty();
    }

    /**
     * Sets the editor content value.<p>
     *
     * @param value the content value
     */
    public void setValue(String value) {

        m_codeValue = value;

        getState().m_contentValue = value;
        markAsDirty();
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#setWidth(float, com.vaadin.server.Sizeable.Unit)
     */
    @Override
    public void setWidth(float width, Unit unit) {

        super.setWidth(width, unit);
        getState().m_width = width + unit.getSymbol();
        markAsDirty();
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#setWidth(java.lang.String)
     */
    @Override
    public void setWidth(String width) {

        super.setWidth(width);
        getState().m_width = width;
        markAsDirty();
    }

    /**
     * @see com.vaadin.ui.AbstractJavaScriptComponent#getState()
     */
    @Override
    protected CodeMirrorState getState() {

        return (CodeMirrorState)super.getState();
    }

    /**
     * Called on blur.<p>
     *
     * @param value the content value
     */
    void onBlur(String value) {

        m_codeValue = value;
        fireEvent(new FieldEvents.BlurEvent(CmsCodeMirror.this));
    }

    /**
     * Called on blur.<p>
     *
     * @param value the content value
     */
    void onChange(String value) {

        boolean changed = !m_codeValue.equals(value);
        m_codeValue = value;
        if (changed) {
            fireEvent(new ValueChangeEvent(this));
        }
    }

    /**
     * Returns the code mirror localization JSON.<p>
     *
     * @param messages the message bundle to use
     *
     * @return the localization
     */
    private String getLocalizationMessages(CmsMessages messages) {

        JSONObject result = new JSONObject();
        try {
            result.put("search", messages.key(Messages.GUI_CODEMIRROR_LANG_SEARCH_0));
            result.put("hint", messages.key(Messages.GUI_CODEMIRROR_LANG_HINT_0));
            result.put("replace", messages.key(Messages.GUI_CODEMIRROR_LANG_REPLACE_0));
            result.put("replacewith", messages.key(Messages.GUI_CODEMIRROR_LANG_REPLACE_WITH_0));
            result.put("replaceconfirm", messages.key(Messages.GUI_CODEMIRROR_LANG_REPLACE_CONFIRM_0));
            result.put("replaceyes", messages.key(Messages.GUI_CODEMIRROR_LANG_REPLACE_YES_0));
            result.put("replaceno", messages.key(Messages.GUI_CODEMIRROR_LANG_RELACE_NO_0));
            result.put("replacestop", messages.key(Messages.GUI_CODEMIRROR_LANG_REPLACE_STOP_0));

            result.put("fontsize", messages.key(Messages.GUI_CODEMIRROR_LANG_FONT_SIZE_0));
        } catch (org.opencms.json.JSONException e) {
            // should never happen
        }
        return result.toString();
    }

    /**
     * Returns the localized short cut messages HTML.<p>
     *
     * @param messages the message bundle to use
     *
     * @return the HTML
     */
    private String getShortcutMessages(CmsMessages messages) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<span class=\"col1\"><b>").append(messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_SEARCH_0));
        buffer.append("</b> ").append(messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_SEARCH_HELP_0));
        buffer.append(" </span><span class=\"col2\"><b>").append(
            messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_FIND_NEXT_0));
        buffer.append("</b> ").append(messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_FIND_NEXT_HELP_0));
        buffer.append(" </span><span class=\"col3\"><b>").append(
            messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_FIND_PREVIOUS_0));
        buffer.append("</b> ").append(messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_FIND_PREVIOUS_HELP_0));
        buffer.append(" </span><span class=\"col1\"><b>").append(
            messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_REPLACE_0));
        buffer.append("</b> ").append(messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_REPLACE_HELP_0));
        buffer.append(" </span><span class=\"col2\"><b>").append(
            messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_REPLACE_ALL_0));
        buffer.append("</b> ").append(messages.key(Messages.GUI_CODEMIRROR_SHORTCUT_REPLACE_ALL_HELP_0)).append(
            "</span>");
        return buffer.toString();
    }
}

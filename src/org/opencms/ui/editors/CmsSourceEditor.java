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

package org.opencms.ui.editors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypeJsp;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.lock.CmsLockUtil.LockedFile;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsEditor;
import org.opencms.ui.apps.I_CmsAppSettings;
import org.opencms.ui.apps.I_CmsAppUIContext;
import org.opencms.ui.apps.I_CmsHasShortcutActions;
import org.opencms.ui.components.CmsConfirmationDialog;
import org.opencms.ui.components.CmsErrorDialog;
import org.opencms.ui.components.CmsToolBar;
import org.opencms.ui.components.I_CmsWindowCloseListener;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.components.codemirror.CmsCodeMirror;
import org.opencms.ui.components.codemirror.CmsCodeMirror.CodeMirrorLanguage;
import org.opencms.xml.content.CmsXmlContent;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.ui.ComboBox;

/**
 * The plain text editor.<p>
 */
@SuppressWarnings("deprecation")
public class CmsSourceEditor
implements I_CmsEditor, I_CmsWindowCloseListener, ViewChangeListener, I_CmsHasShortcutActions {

    /**
     * Stores the editor settings.<p>
     */
    public static class EditorSettings implements I_CmsAppSettings {

        /** JSON key constant. */
        private static final String BRACKETS = "brackets";
        /** JSON key constant. */
        private static final String FONTSIZE = "fontsize";
        /** JSON key constant. */
        private static final String HIGHLIGHTING = "highlighting";
        /** JSON key constant. */
        private static final String TABS = "tabs";
        /** JSON key constant. */
        private static final String WRAPPING = "wrapping";

        /** The auto close brackets flag. */
        boolean m_closeBrackets = true;

        /** The font size. */
        String m_fontSize = "16px";

        /** The highlighting flag. */
        boolean m_highlighting = true;

        /** The line wrapping flag. */
        boolean m_lineWrapping;

        /** The tab visibility flag. */
        boolean m_tabsVisible = true;

        /**
         * Returns the font size.
         * @return the font size.
         */
        public String getFontSize() {

            return m_fontSize;
        }

        /**
         * @see org.opencms.ui.apps.I_CmsAppSettings#getSettingsString()
         */
        public String getSettingsString() {

            JSONObject json = new JSONObject();
            try {
                json.put(BRACKETS, m_closeBrackets);
                json.put(HIGHLIGHTING, m_highlighting);
                json.put(WRAPPING, m_lineWrapping);
                json.put(FONTSIZE, m_fontSize);
                json.put(TABS, m_tabsVisible);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return json.toString();
        }

        /**
         * Returns a flag, indicating if automatically closing brackets is activated.
         * @return a flag, indicating if automatically closing brackets is activated.
         */
        public boolean isCloseBracketsActive() {

            return m_closeBrackets;
        }

        /**
         * Returns a flag, indicating if highlighting is activated.
         * @return a flag, indicating if highlighting is activated.
         */
        public boolean isHighlightingActive() {

            return m_highlighting;
        }

        /**
         * Returns a flag, indicating if line wrapping is activated.
         * @return a flag, indicating if line wrapping is activated.
         */
        public boolean isLineWrappingActive() {

            return m_lineWrapping;
        }

        /**
         * Returns a flag, indicating if tabs should be visible.
         * @return a flag, indicating if tabs should be visible.
         */
        public boolean isTabsVisibleActive() {

            return m_tabsVisible;
        }

        /**
         * @see org.opencms.ui.apps.I_CmsAppSettings#restoreSettings(java.lang.String)
         */
        public void restoreSettings(String storedSettings) {

            try {
                JSONObject json = new JSONObject(storedSettings);
                if (json.has(BRACKETS)) {
                    m_closeBrackets = json.getBoolean(BRACKETS);
                }
                if (json.has(HIGHLIGHTING)) {
                    m_highlighting = json.getBoolean(HIGHLIGHTING);
                }
                if (json.has(WRAPPING)) {
                    m_lineWrapping = json.getBoolean(WRAPPING);
                }
                if (json.has(TABS)) {
                    m_tabsVisible = json.getBoolean(TABS);
                }
                if (json.has(FONTSIZE)) {
                    m_fontSize = json.getString(FONTSIZE);
                }

            } catch (JSONException e) {
                //       LOG.error("Failed to restore file explorer settings from '" + storedSettings + "'", e);
            }
        }
    }

    /** Exit shortcut. */
    private static final Action ACTION_EXIT = new ShortcutAction(
        "Ctrl+Shift+X",
        ShortcutAction.KeyCode.X,
        new int[] {ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.SHIFT});

    /** Exit shortcut, (using Apple CMD as modifier). */
    private static final Action ACTION_EXIT_CMD = new ShortcutAction(
        "CMD+Shift+X",
        ShortcutAction.KeyCode.X,
        new int[] {ShortcutAction.ModifierKey.META, ShortcutAction.ModifierKey.SHIFT});

    /** Save shortcut. */
    private static final Action ACTION_SAVE = new ShortcutAction(
        "Ctrl+S",
        ShortcutAction.KeyCode.S,
        new int[] {ShortcutAction.ModifierKey.CTRL});

    /** Save & Exit shortcut. */
    private static final Action ACTION_SAVE_AND_EXIT = new ShortcutAction(
        "Ctrl+Shift+S",
        ShortcutAction.KeyCode.S,
        new int[] {ShortcutAction.ModifierKey.CTRL, ShortcutAction.ModifierKey.SHIFT});

    /** Save & Exit shortcut, (using Apple CMD as modifier). */
    private static final Action ACTION_SAVE_AND_EXIT_CMD = new ShortcutAction(
        "CMD+Shift+S",
        ShortcutAction.KeyCode.S,
        new int[] {ShortcutAction.ModifierKey.META, ShortcutAction.ModifierKey.SHIFT});

    /** Save shortcut, (using Apple CMD as modifier). */
    private static final Action ACTION_SAVE_CMD = new ShortcutAction(
        "CMD+S",
        ShortcutAction.KeyCode.S,
        new int[] {ShortcutAction.ModifierKey.META});

    /** The available font sizes. */
    private static final String[] FONT_SIZES = new String[] {"8px", "10px", "12px", "14px", "16px", "18px", "20px"};

    /** The serial version id. */
    private static final long serialVersionUID = 726920483145397926L;

    /** The editor back link. */
    String m_backLink;

    /** The code mirror instance. */
    CmsCodeMirror m_codeMirror;

    /** The bundle editor shortcuts. */
    Map<Action, Runnable> m_shortcutActions;

    /** The content changed flag. */
    private boolean m_changed;

    /** The cleared flag. */
    private boolean m_cleared;

    /** The exit button. */
    private Button m_exit;

    /** The current file. */
    private LockedFile m_file;

    /** The save button. */
    private Button m_save;

    /** The save and exit button. */
    private Button m_saveAndExit;

    /**
     * Constructor.<p>
     */
    public CmsSourceEditor() {

        m_shortcutActions = new HashMap<Action, Runnable>();
        Runnable save = new Runnable() {

            public void run() {

                save();
            }
        };
        m_shortcutActions.put(ACTION_SAVE, save);
        m_shortcutActions.put(ACTION_SAVE_CMD, save);
        Runnable saveExit = new Runnable() {

            public void run() {

                saveAndExit();
            }
        };
        m_shortcutActions.put(ACTION_SAVE_AND_EXIT, saveExit);
        m_shortcutActions.put(ACTION_SAVE_AND_EXIT_CMD, saveExit);
        Runnable exit = new Runnable() {

            public void run() {

                exit();
            }
        };
        m_shortcutActions.put(ACTION_EXIT, exit);
        m_shortcutActions.put(ACTION_EXIT_CMD, exit);
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#afterViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public void afterViewChange(ViewChangeEvent event) {

        // nothing to do
    }

    /**
     * @see com.vaadin.navigator.ViewChangeListener#beforeViewChange(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent)
     */
    public boolean beforeViewChange(final ViewChangeEvent event) {

        if (m_changed) {
            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EDITOR_CLOSE_CAPTION_0),
                CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EDITOR_CLOSE_TEXT_0),
                new Runnable() {

                    public void run() {

                        clear();
                        event.getNavigator().navigateTo(event.getViewName());
                    }
                });
            return false;
        }
        if (!m_cleared) {
            clear();
        }
        return true;
    }

    /**
     * Returns the syntax highlighting type for the currently edited resource.<p>
     *
     * @param resource the resource to edit
     *
     * @return the syntax highlighting type
     */
    public CodeMirrorLanguage getHighlightMode(CmsResource resource) {

        if (resource != null) {
            // determine resource type
            int type = resource.getTypeId();
            if (CmsResourceTypeJsp.isJspTypeId(type)) {
                // JSP file
                return CodeMirrorLanguage.JSP;
            }
            if (CmsResourceTypeXmlContent.isXmlContent(resource) || CmsResourceTypeXmlPage.isXmlPage(resource)) {
                // XML content file or XML page file
                return CodeMirrorLanguage.XML;
            }
            // all other files will be matched according to their suffix
            int dotIndex = resource.getName().lastIndexOf('.');
            if (dotIndex != -1) {
                String suffix = resource.getName().substring(dotIndex + 1).toLowerCase();
                for (CodeMirrorLanguage lang : CodeMirrorLanguage.values()) {
                    if (lang.isSupportedFileType(suffix)) {
                        return lang;
                    }
                }
            }
        }
        // return HTML type as default
        return CodeMirrorLanguage.HTML;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#getPriority()
     */
    public int getPriority() {

        return 10;
    }

    /**
     * @see org.opencms.ui.apps.I_CmsHasShortcutActions#getShortcutActions()
     */
    public Map<Action, Runnable> getShortcutActions() {

        return m_shortcutActions;
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#initUI(org.opencms.ui.apps.I_CmsAppUIContext, org.opencms.file.CmsResource, java.lang.String, java.util.Map)
     */
    public void initUI(I_CmsAppUIContext context, CmsResource resource, String backLink, Map<String, String> params) {

        CmsObject cms = A_CmsUI.getCmsObject();
        if (OpenCms.getADEManager().isEditorRestricted(cms, resource)) {
            throw new CmsRuntimeException(
                org.opencms.ade.contenteditor.Messages.get().container(
                    org.opencms.ade.contenteditor.Messages.ERR_EDITOR_RESTRICTED_0));
        }
        CmsMessages messages = Messages.get().getBundle(UI.getCurrent().getLocale());
        context.showInfoArea(false);
        context.setAppTitle(messages.key(Messages.GUI_SOURCE_EDITOR_TITLE_0));
        CmsAppWorkplaceUi.setWindowTitle(
            CmsVaadinUtils.getMessageText(
                org.opencms.ui.apps.Messages.GUI_CONTENT_EDITOR_TITLE_2,
                resource.getName(),
                CmsResource.getParentFolder(A_CmsUI.getCmsObject().getSitePath(resource))));
        m_backLink = backLink;
        m_codeMirror = new CmsCodeMirror();
        m_codeMirror.setSizeFull();
        context.setAppContent(m_codeMirror);
        context.enableDefaultToolbarButtons(false);
        m_saveAndExit = CmsToolBar.createButton(
            FontOpenCms.SAVE_EXIT,
            messages.key(Messages.GUI_BUTTON_SAVE_AND_EXIT_0),
            true);
        m_saveAndExit.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                saveAndExit();
            }

        });
        m_saveAndExit.setEnabled(false);
        context.addToolbarButton(m_saveAndExit);

        m_save = CmsToolBar.createButton(FontOpenCms.SAVE, messages.key(Messages.GUI_BUTTON_SAVE_0), true);
        m_save.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                save();
            }

        });
        m_save.setEnabled(false);
        context.addToolbarButton(m_save);

        Button undo = CmsToolBar.createButton(FontOpenCms.UNDO, messages.key(Messages.GUI_BUTTON_UNDO_0), true);
        context.addToolbarButton(undo);

        Button redo = CmsToolBar.createButton(FontOpenCms.REDO, messages.key(Messages.GUI_BUTTON_REDO_0), true);
        context.addToolbarButton(redo);

        m_codeMirror.registerUndoRedo(undo, redo);

        Button search = CmsToolBar.createButton(FontOpenCms.SEARCH, messages.key(Messages.GUI_BUTTON_SEARCH_0), true);
        context.addToolbarButton(search);

        Button replace = CmsToolBar.createButton(
            FontOpenCms.SEARCH_REPLACE,
            messages.key(Messages.GUI_BUTTON_REPLACE_0),
            true);
        context.addToolbarButton(replace);

        m_codeMirror.registerSearchReplace(search, replace);

        EditorSettings settings;
        try {
            settings = OpenCms.getWorkplaceAppManager().getAppSettings(A_CmsUI.getCmsObject(), EditorSettings.class);

        } catch (Exception e) {
            settings = new EditorSettings();
        }

        final Button toggleHighlight = CmsToolBar.createButton(
            FontOpenCms.HIGHLIGHT,
            messages.key(Messages.GUI_BUTTON_TOGGLE_HIGHLIGHTING_0));
        toggleHighlight.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Button b = event.getButton();
                boolean pressed = b.getStyleName().contains(OpenCmsTheme.BUTTON_PRESSED);
                if (pressed) {
                    b.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
                } else {
                    b.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
                }
                m_codeMirror.setHighlighting(!pressed);
            }

        });
        if (settings.m_highlighting) {
            m_codeMirror.setHighlighting(true);
            toggleHighlight.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            m_codeMirror.setHighlighting(false);
        }
        context.addToolbarButtonRight(toggleHighlight);

        final Button toggleLineWrap = CmsToolBar.createButton(
            FontOpenCms.WRAP_LINES,
            messages.key(Messages.GUI_BUTTON_TOGGLE_LINE_WRAPPING_0));
        toggleLineWrap.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Button b = event.getButton();
                boolean pressed = b.getStyleName().contains(OpenCmsTheme.BUTTON_PRESSED);
                if (pressed) {
                    b.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
                } else {
                    b.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
                }
                m_codeMirror.setLineWrapping(!pressed);
            }

        });
        if (settings.m_lineWrapping) {
            m_codeMirror.setLineWrapping(true);
            toggleLineWrap.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            m_codeMirror.setLineWrapping(false);
        }
        context.addToolbarButtonRight(toggleLineWrap);

        final Button toggleBrackets = CmsToolBar.createButton(
            FontOpenCms.BRACKETS,
            messages.key(Messages.GUI_BUTTON_TOBBLE_BRACKET_AUTOCLOSE_0));
        toggleBrackets.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Button b = event.getButton();
                boolean pressed = b.getStyleName().contains(OpenCmsTheme.BUTTON_PRESSED);
                if (pressed) {
                    b.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
                } else {
                    b.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
                }
                m_codeMirror.setCloseBrackets(!pressed);
            }

        });
        if (settings.m_closeBrackets) {
            m_codeMirror.setCloseBrackets(true);
            toggleBrackets.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            m_codeMirror.setCloseBrackets(false);
        }
        context.addToolbarButtonRight(toggleBrackets);

        final Button toggleTabs = CmsToolBar.createButton(
            FontOpenCms.INVISIBLE_CHARS,
            messages.key(Messages.GUI_BUTTON_TOGGLE_TAB_VISIBILITY_0));
        toggleTabs.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                Button b = event.getButton();
                boolean pressed = b.getStyleName().contains(OpenCmsTheme.BUTTON_PRESSED);
                if (pressed) {
                    b.removeStyleName(OpenCmsTheme.BUTTON_PRESSED);
                } else {
                    b.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
                }
                m_codeMirror.setTabsVisible(!pressed);
            }

        });
        if (settings.m_tabsVisible) {
            m_codeMirror.setTabsVisible(true);
            toggleTabs.addStyleName(OpenCmsTheme.BUTTON_PRESSED);
        } else {
            m_codeMirror.setTabsVisible(false);
        }
        context.addToolbarButtonRight(toggleTabs);

        ComboBox modeSelect = new ComboBox();
        modeSelect.setWidth("115px");
        modeSelect.addStyleName(OpenCmsTheme.TOOLBAR_FIELD);
        modeSelect.addStyleName(OpenCmsTheme.REQUIRED_BUTTON);
        modeSelect.setNullSelectionAllowed(false);
        modeSelect.setImmediate(true);
        modeSelect.setNewItemsAllowed(false);
        for (CodeMirrorLanguage lang : CodeMirrorLanguage.values()) {
            modeSelect.addItem(lang);
            modeSelect.setItemCaption(lang, lang.name());
        }
        CodeMirrorLanguage lang = getHighlightMode(resource);
        modeSelect.setValue(lang);
        m_codeMirror.setLanguage(lang);
        modeSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                m_codeMirror.setLanguage((CodeMirrorLanguage)event.getProperty().getValue());
            }
        });
        context.addToolbarButtonRight(modeSelect);

        ComboBox fontSizeSelect = new ComboBox();
        fontSizeSelect.setWidth("75px");
        fontSizeSelect.addStyleName(OpenCmsTheme.TOOLBAR_FIELD);
        fontSizeSelect.addStyleName(OpenCmsTheme.REQUIRED_BUTTON);
        fontSizeSelect.setNullSelectionAllowed(false);
        fontSizeSelect.setImmediate(true);
        fontSizeSelect.setNewItemsAllowed(false);
        for (int i = 0; i < FONT_SIZES.length; i++) {
            fontSizeSelect.addItem(FONT_SIZES[i]);
        }
        fontSizeSelect.setValue(settings.m_fontSize);
        fontSizeSelect.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                m_codeMirror.setFontSize((String)event.getProperty().getValue());
            }
        });
        context.addToolbarButtonRight(fontSizeSelect);
        m_codeMirror.setFontSize(settings.m_fontSize);

        m_exit = CmsToolBar.createButton(FontOpenCms.EXIT, messages.key(Messages.GUI_BUTTON_CANCEL_0), true);
        m_exit.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                exit();
            }

        });
        context.addToolbarButtonRight(m_exit);

        try {
            m_file = LockedFile.lockResource(A_CmsUI.getCmsObject(), resource);
            String content = new String(m_file.getFile().getContents(), m_file.getEncoding());
            m_codeMirror.setValue(content);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
        }
        m_codeMirror.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 1L;

            public void valueChange(ValueChangeEvent event) {

                onChange((String)event.getProperty().getValue());
            }
        });
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#matchesResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource, boolean)
     */
    public boolean matchesResource(CmsObject cms, CmsResource resource, boolean plainText) {

        I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource);
        return matchesType(type, plainText);
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#matchesType(org.opencms.file.types.I_CmsResourceType, boolean)
     */
    public boolean matchesType(I_CmsResourceType type, boolean plainText) {

        return !((type instanceof CmsResourceTypeBinary) || (type instanceof CmsResourceTypeImage));
    }

    /**
     * @see org.opencms.ui.editors.I_CmsEditor#newInstance()
     */
    public I_CmsEditor newInstance() {

        return new CmsSourceEditor();
    }

    /**
     * @see org.opencms.ui.components.I_CmsWindowCloseListener#onWindowClose()
     */
    public void onWindowClose() {

        clear();
    }

    /**
     * Unlocks the edited file before leaving the editor.<p>
     */
    void clear() {

        m_cleared = true;
        m_changed = false;
        m_file.tryUnlock();
        OpenCms.getWorkplaceAppManager().storeAppSettings(
            A_CmsUI.getCmsObject(),
            EditorSettings.class,
            getCurrentSettings());
    }

    /**
     * Exits the editor without saving.<p>
     * Will ask to confirm exit on changed contents.<p>
     */
    void exit() {

        if (m_changed) {
            CmsConfirmationDialog.show(
                CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EDITOR_CLOSE_CAPTION_0),
                CmsVaadinUtils.getMessageText(org.opencms.ui.apps.Messages.GUI_EDITOR_CLOSE_TEXT_0),
                new Runnable() {

                    public void run() {

                        exitInternal();
                    }
                });
        } else {
            exitInternal();
        }

    }

    /**
     * Exits the editor without saving.<p>
     */
    void exitInternal() {

        clear();
        CmsEditor.openBackLink(m_backLink);
    }

    /**
     * Called on content change.<p>
     *
     * @param value the changed content value
     */
    void onChange(String value) {

        m_changed = true;
        m_save.setEnabled(true);
        m_saveAndExit.setEnabled(true);
    }

    /**
     * Saves the current editor content.<p>
     */
    void save() {

        CmsObject cms = A_CmsUI.getCmsObject();
        try {
            byte[] content = m_codeMirror.getValue().getBytes(m_file.getEncoding());
            m_file.getFile().setContents(content);
            cms.getRequestContext().setAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE, Boolean.TRUE);
            cms.writeFile(m_file.getFile());
            m_changed = false;
            m_save.setEnabled(false);
            m_saveAndExit.setEnabled(false);
        } catch (Exception e) {
            CmsErrorDialog.showErrorDialog(e);
        } finally {
            cms.getRequestContext().removeAttribute(CmsXmlContent.AUTO_CORRECTION_ATTRIBUTE);
        }
    }

    /**
     * Saves the current editor content and leaves the editor.<p>
     */
    void saveAndExit() {

        save();
        exit();
    }

    /**
     * Returns the current editor settings.<p>
     *
     * @return the current editor settings
     */
    private EditorSettings getCurrentSettings() {

        EditorSettings result = new EditorSettings();
        result.m_closeBrackets = m_codeMirror.isCloseBrackets();
        result.m_lineWrapping = m_codeMirror.isLineWrapping();
        result.m_highlighting = m_codeMirror.isHighlighting();
        result.m_tabsVisible = m_codeMirror.isTabsVisible();
        result.m_fontSize = m_codeMirror.getFontSize();
        return result;
    }
}

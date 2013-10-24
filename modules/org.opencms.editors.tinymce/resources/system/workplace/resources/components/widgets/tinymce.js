/*
 * These scripts are required for the TinyMCE widgets in the xml content editor
 */

// editor global objects
var editorInstances = new Array();
var contentFields = new Array();
var expandedToolbars = new Array();
var editorsLoaded = false;

// generates the TinyMCE instances
function generateEditors() {
    for (var i=0; i<editorInstances.length; i++) {
        var editInst = editorInstances[i];
        editInst.ReplaceTextarea();
    }
}

// writes the HTML from the editor instances back to the textareas
function submitHtml(form) {
    for (var i=0; i<contentFields.length; i++) {
        var cf = contentFields[i];
        var editInst = tinyMCE.get('ta_' + cf.getAttribute('id', 0));
        var editedContent = editInst.getContent();
        if (editedContent != null && editedContent != "null") {
            cf.value = encodeURIComponent(editedContent);
        }
    }
}

// checks if at least one of the editors was loaded successfully
function editorsLoaded() {
    return editorsLoaded;
}




function setupTinyMCE(editor) {
    var editorInstance=editor;
   editor.on('init',function() {
       addCustomShortcuts(editorInstance);
   });
}

// initializes the tinyMCE editor instance with the given options
function initTinyMCE(options){
    // default options:
    var defaults={
            relative_urls: false,
            remove_script_host: false,
            mode: "exact",
            theme: "modern",
            file_browser_callback: cmsTinyMceFileBrowser,
            setup: function(editor) { setupTinyMCE(editor); },
            plugins: "anchor,charmap,code,textcolor,autolink,lists,pagebreak,layer,table,save,hr,image,link,emoticons,insertdatetime,preview,media,searchreplace,print,paste,directionality,fullscreen,noneditable,visualchars,nonbreaking,template,wordcount,advlist,-opencms",
            menubar: false,
            toolbar_items_size: 'small',
            resize: false,
            width: '100%'
          };
    // check for fullpage option
    if (options["fullpage"]){
        defaults["plugins"]+=",fullpage";
    }
    if (options["contextmenu"]){
        defaults["plugins"]+=",contextmenu";
    }
    if (options["style_formats"]){
        try{
        options["style_formats"]=eval('('+options["style_formats"]+')');
        }catch(error){
            delete options["style_formats"];
            alert("Error while parsing style formats option for tinyMCE: "+error);
        }
    }
    // the fullpage attribute needs to be removed otherwise tinyMCE won't start
    delete options["fullpage"];
    $.extend(defaults, options);
    tinyMCE.init(defaults);
}

function addCustomShortcuts(editor){
    editor.addShortcut('ctrl+shift+z','','Redo');
    editor.addShortcut('ctrl+l','','Link');
}
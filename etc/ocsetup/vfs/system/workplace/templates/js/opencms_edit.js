//------------------------------------------------------//
// Script for editcontrol
//------------------------------------------------------//

// Definition of Constants
var CLOSE=1;
var SAVECLOSE=2;
var SAVE=3;

var UNDO=4;
var REDO=5;

var SEARCH=6;
var REPLACE=7;
var GOTO=8;

var CUT=9;
var COPY=10;
var PASTE=11;

var IMPORT=12;
var EXPORT=13;
var EXPORTAS=14;

var PRINT=15;

// Indicates if the text of the editor window is already set
var textSetted = false;

var windowWidth=null;
var windowHeight=null;


function getDimensions() {
	if( NS==true ) {
		windowWidth = innerWidth-20;
		windowHeight = innerHeight-20;
	}
	else if( IE==true ) {
		windowWidth = document.body.clientWidth-20;
		windowHeight = document.body.clientHeight-20;
	}
}


function displayCode() {
  document.GUIApplet.setCode(unescape(text));
}


function writeAppletTag()
{
  var str=null;
  getDimensions();
  document.open();
  if (NS==true) 
	document.write('<APPLET name="GUIApplet" CODE="GUIApplet" CODEBASE="/" WIDTH="'+windowWidth+'" HEIGHT="'+(windowHeight-40)+'" mayscript></APPLET>');
  else if (IE==true)
  {
	str = '<OBJECT classid=clsid:EB3A74C0-5343-101D-BB4D-040224009C02 height=100% id=edit1 name=edit1 onfocus=setTextDelayed(); width=100%><PARAM NAME="_Version" VALUE="131083"><PARAM NAME="_ExtentX" VALUE="22887"><PARAM NAME="_ExtentY" VALUE="7223"><PARAM NAME="_StockProps" VALUE="125"><PARAM NAME="Text" VALUE="Loading ....."><PARAM NAME="ForeColor" VALUE="0"><PARAM NAME="BackColor" VALUE="16777215"><PARAM NAME="BorderStyle" VALUE="1"><PARAM NAME="Enabled" VALUE="-1"><PARAM NAME="AutoIndent" VALUE="-1"><PARAM NAME="BackColorSelected" VALUE="8388608"><PARAM NAME="BookmarksMax" VALUE="16">';
    str= str + '<PARAM NAME="CanChangeFile" VALUE="0">';
	str= str + '<PARAM NAME="CanChangeFont" VALUE="0">';
	str= str + '<PARAM NAME="CaretWidth" VALUE="0">';
	str= str + '<PARAM NAME="DefaultSelection" VALUE="-1">';
	str= str + '<PARAM NAME="ExtraComments" VALUE="0">';
	str= str + '<PARAM NAME="Item2AsComment" VALUE="0">';
	str= str + '<PARAM NAME="UnixStyleSave" VALUE="0">';
	str= str + '<PARAM NAME="CurrentWordAsText" VALUE="0">';
	str= str + '<PARAM NAME="MultilineItems" VALUE="0">';
	str= str + '<PARAM NAME="MultilineStrings" VALUE="0">';
	str= str + '<PARAM NAME="SinglelineStrings" VALUE="0">';
	str= str + '<PARAM NAME="ExtraHorzSpacing" VALUE="3">';
	str= str + '<PARAM NAME="ExtraVertSpacing" VALUE="0">';
	str= str + '<PARAM NAME="FileMask" VALUE="\/All Files\/*.*\/">';
	str= str + '<PARAM NAME="FileName" VALUE="+">';
	str= str + '<PARAM NAME="ForeColorSelected" VALUE="16777215">';
	str= str + '<PARAM NAME="HasFile" VALUE="-1">';
	str= str + '<PARAM NAME="HasMenu" VALUE="0">';
	str= str + '<PARAM NAME="Highlight" VALUE="0">';
	str= str + '<PARAM NAME="InsertMode" VALUE="1">';
	str= str + '<PARAM NAME="ScrollBars" VALUE="3">';
	str= str + '<PARAM NAME="Syntax" VALUE="">';
	str= str + '<PARAM NAME="PaintMode" VALUE="0">';
	str= str + '<PARAM NAME="TabStopSize" VALUE="4">';
	str= str + '<PARAM NAME="ReadOnly" VALUE="0">';
	str= str + '<PARAM NAME="UndoDepth" VALUE="-1">';
	str= str + '<PARAM NAME="InitializeType" VALUE="0">';
	str= str + '<PARAM NAME="GroupNumber" VALUE="0">';
	str= str + '<PARAM NAME="StartInComments" VALUE="0">';
	str= str + '<PARAM NAME="MacStyleSave" VALUE="0">';
	str= str + '<PARAM NAME="WantTab" VALUE="-1">';
	str= str + '<PARAM NAME="WordWrap" VALUE="0">';
	str= str + '<PARAM NAME="WordWrapAuto" VALUE="-1">';
	str= str + '<PARAM NAME="WordWrapRMargin" VALUE="0">';
	str= str + '<PARAM NAME="WordWrapWidth" VALUE="0">';
	str= str + '<PARAM NAME="NoPrintDialog" VALUE="0">';
	str= str + '<PARAM NAME="NoPrintProgress" VALUE="0">';
	str= str + '<PARAM NAME="WrapKeys" VALUE="0">';
	str= str + '<PARAM NAME="BackColorPrint" VALUE="16777215">';
	str= str + '<PARAM NAME="PrintJobName" VALUE="Document">';
	str= str + '<PARAM NAME="ZeroSubstitute" VALUE="0"></OBJECT>';
	str= str + '<INPUT TYPE=HIDDEN NAME="CONTENT">';
	str= str + '<INPUT TYPE=HIDDEN NAME="action">';
	str= str + '<INPUT TYPE=HIDDEN NAME="file" value="' + filename + '">';	
	document.write(str);
	}
  document.close();
}

// loads the file content into the editor
function setText()
{
   	// setting text can not be done now here for the text editor.
	// MS IE 5 has problems with setting text when the editor control is
	// not loaded. 
	// Workaround: focus() the text editor here and set the text
	// using the onFocus event of the editor.
	if(IE == true) 
	{
		document.all.edit1.focus();
	}
	else
	{
		displayCode();
	}
}

// load the file content into the editor. this is called by the onFocus event of the edit control
function setTextDelayed()
{
	if(! textSetted) {
		document.all.edit1.Text = unescape(text);
		textSetted = true;
	}
}

function doSubmit()
{
	document.EDITOR.CONTENT.value = escape(document.EDITOR.edit1.Text);
}

// Function action on button click
function doEdit(para)
{
	switch(para)
	{
	case 1:
	{
		doSubmit();
		document.EDITOR.action.value = "exit";
		document.EDITOR.submit();
		break;
	}
	case 2:
	{
		doSubmit();
		document.EDITOR.action.value = "saveexit";
		document.EDITOR.submit();
		break;
	}
	case 3:
	{
		doSubmit();
		document.EDITOR.action.value = "save";
		document.EDITOR.submit();
		break;
	}
	case 4:
	{
		document.all.edit1.Undo();
		break;	
	}
	case 5:
	{
		document.all.edit1.Redo();
		break;	
	}
	case 6:
	{
		document.all.edit1.ShowFindDialog();
		break;
	}
	case 7:
	{
		document.all.edit1.ShowReplaceDialog();
		break;
	}
	case 8:
	{
		document.all.edit1.ShowGotoLineDialog();
		break;	
	}
	case 9:
	{
		document.all.edit1.CutToClipboard();
		break;
	}
	case 10:
	{
		document.all.edit1.CopyToClipboard();
		break;
	}
	case 11:
	{
		document.all.edit1.PasteFromClipboard();
		break;
	}
	case 12:
	{
	    document.all.edit1.OpenFile();
		break;
	}
	case 13:
	{
	    document.all.edit1.SaveFile();
		break;
	}
	case 14:
	{
	    document.all.edit1.SaveFileAs();
		break;
	}
	case 15:
	{
	    document.all.edit1.PrintText();
		break;
	}
	case 16:
	{
		// dummy tag for help
		break;
	}
	default:
	{
		alert("NYI");
		break;
	}
}	
	document.all.edit1.focus();
}
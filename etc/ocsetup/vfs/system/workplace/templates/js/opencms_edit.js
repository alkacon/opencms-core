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


// to load the file content into the editor
function setText()
{
   	document.all.edit1.Text = unescape(text);
	EDITOR.edit1.focus();
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
		document.EDITOR.save.value = "0";
		document.EDITOR.EXIT.value = "1";
		document.EDITOR.submit();
		break;
	}
	case SAVECLOSE:
	{
		doSubmit();
		document.EDITOR.save.value = "1";
		document.EDITOR.EXIT.value = "1";
		document.EDITOR.submit();
		break;
	}
	case SAVE:
	{
		doSubmit();
		document.EDITOR.save.value = "1";
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
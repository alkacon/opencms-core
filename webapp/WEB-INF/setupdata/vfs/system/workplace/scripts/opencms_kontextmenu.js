// mouseevent

function start() {
    document.onclick = mouseClicked;
    if(ns){document.captureEvents(Event.CLICK); }
}

//------------------------------------------
// global variables and global functions
// m.schleich 03.01.2000
// m.stanke 26.04.2000
//------------------------------------------
var shown = false;
var letztelyr='null';
var x=0;
var y=0;

// decides, if the kontextmenu should be displayed
var disableKontext = false;

// parameter for the kontextmenu
var kontextparam;
var kontextid;

// which operation is clicked
var DO_LOCK=1;
var DO_LOCKUSER=2;
var DO_UNLOCK=3; 

var DO_COPY=4;
var DO_COPYFOLDER=42;
var DO_MOVE=5;
var DO_RENAME=6;
var DO_DELETE=7;

var DO_CHOWN=8;
var DO_CHGRP=9;
var DO_CHMOD=10;
var DO_CHTYPE=11;
var DO_META=12;
var DO_METAVIEW=13;

var DO_EDIT=14;
var DO_HTMLEDIT=15;
var DO_VIEW=16;
var DO_EXPORT=17;

var DO_TEMPLATEEDIT=18;
var DO_DATAEDIT=19;

var DO_EDITDOC=20;
var DO_ATE=21;
var DO_MAILEDIT=22;
var DO_NEWSEDIT=23;
var DO_LINKEDIT=24;

var DO_COPYF=30;
var DO_MOVEF=31;
var DO_RENAMEF=32;
var DO_DELETEF=33;

var DO_HISTORY=40;
var DO_HISTVIEW=41;
//=======================================
var DO_PUNLOCK=1;
var DO_PONLINELOCK=2;
var DO_PONLINE=3;
var DO_PDELETE=4

var DO_PTASK=10;
var DO_TASK=99;

var DO_PREACT=20;

//=======================================
var DO_TQUERY=1;
var DO_TACCEPT=2;
var DO_TDUE=3;
var DO_TPRIORITY=4;
var DO_TCOMMENT=5;
var DO_TFORWARD=6;
var DO_TOK=7;
var DO_TMESSAGE=8;
var DO_TREAKT=9;
var DO_TTAKE=10;


gecko = (navigator.userAgent.toLowerCase().indexOf('gecko') != -1);

// mouse coordinates on click
function mouseClicked(e)
{
        if (ie) {x=event.x; y=event.y;}
        if (ns) {x=e.x; y=e.y;}
        if(gecko){x=e.pageX;      y=e.pageY;}
        hidemenu(letztelyr);
}

//----------------------------------------
// function to display the filecontextmenu
// parameters:
// welche: layer name
// parameter: parameter for menu
// id: each link for layer must have a unique id number
// m.schleich 03.12.1999
//----------------------------------------
function showkontext(welche, parameter, id)
{

    kontextparam = parameter.replace(/\//g, "%2F");
    kontextid = id; 

    if(gecko){

        document.getElementById(welche).style.visibility = "visible";
        document.getElementById(welche).style.left = x;
        document.getElementById(welche).style.top =  y;

    }else{

        if(disableKontext == false) {
            // set the kontextparameter
            if(welche!='')
            {
    
            if (!shown || id!=altid)
            {
    
                if(y >= (screen.availHeight/2))
                {
    
                    if(ie)lyrheight=checklyrheight(welche);
    
                    if(ns)lyrheight=document.layers[welche].clip.height;
    
                    lyrheight='-'+lyrheight;
                    eval(ypos_01+welche+ypos_02+'y'+yoffset+lyrheight);
                }
                else
                {
                    lyrheight='-0';
                }
                eval(xpos_01+welche+xpos_02+'x'+xoffset);
                eval(ypos_01+welche+ypos_02+'y'+yoffset+lyrheight);
    
                hidemenu(letztelyr);
                eval(layerzeigen_01+welche+layerzeigen_02);
    
                shown = true;
            }
            else
            {
                hidemenu(letztelyr);
                shown = false;
            }
            
            altid=id;
            }
        }
    }
    letztelyr=welche;
}


// hides the file context (layer)
function hidemenu(welche)
{
    if(gecko){
        if(welche!='null')document.getElementById(welche).style.visibility = "hidden";
    }else{
        if(welche!='null')
        {
            eval(layerverstecken_01+welche+layerverstecken_02);
            //shown=false;
        }
        else
        {
            return;
        }
    }
}

function checklyrheight(welche)
{
    switch(welche)
         {
           case 'plain':
            {
             return 220;
             break;
            }
            case 'plainlock':
            {
             return 220;
             break;
            }
            case 'plainlockuser':
            {
             return 220;
             break;
            }
            case 'pic':
            {
             return 220;
             break;
            }
           case 'online_p':
            {
             return 81;
             break;
            }
            default:
            {
             return 220;
             break;
            }
        }

}

// which action on layer is clicked
function doAction(action) {


    hidemenu(letztelyr);
    var encLasturl = simpleEscape(location.href);

    switch(action)  {
        case 1: {

            location.href='lock.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 2:   {
            location.href='lockchange.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 3: {
            location.href='unlock.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 4: {
            location.href='copy.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 5: {
            location.href='move.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 6: {
            location.href='rename.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 7: {
            location.href='delete.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 8: {
            location.href='chown.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 9: {
            location.href='chgrp.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 10: {
            location.href='chmod.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 11: {
            location.href='chtype.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        } case 12: {
            location.href='property.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        }
        case 13:
        {
            location.href='explorer_files_metashow.html';
            break;
        } case 14: {
            top.location.href='edit_text.html?file='+ kontextparam;
            break;
        } case 15: {
            top.location.href='edit_html.html?file='+ kontextparam;
            break;
        }
        case 16:
        {
            top.location.href='edit_view.html';
            break;
        }
        case 18: 
        {
            // Template Editor
            top.location.href='edit_page.html?file='+ kontextparam;
            break;
        }

        case 23: 
        {
            // News Editor
            top.body.location.href='news_edit.html?action=edit&lasturl=' + simpleEscape(top.body.location.href) + '&file='+ kontextparam + '&initial=true';
            break;
        }
        
        case 24:
        { 
            location.href='explorer_files_new_link.html?file='+ kontextparam + '&newlink=false&lasturl=' + encLasturl;
            break;
        }
        
        case 30:
        {
            location.href='explorer_files_copyf.html';
            break;
        }
        case 31:
        {
            location.href='explorer_files_movef.html';
            break;
        }
        case 32:
        {
            location.href='explorer_files_renamef.html';
            break;
        }
        case 33:
        {
            location.href='explorer_files_deletef.html';
            break;
        } case 40:{
            location.href='history.html?lasturl=' + encLasturl + '&file='+ kontextparam + '&initial=true';
            break;
        }
        case 41:
        {
            location.href='explorer_files_historyshow.html';
            break;
        } case 42: {
            location.href='copy.html?file='+ kontextparam;
            break;
        } case 98:{
            location.href='NewsEditBodyBackoffice.html?id=' + kontextid;
            break;
        } case 97:{
            window.top.location.href='NewsDeleteBackoffice.html?id=' + kontextid;
            break;
        }       
    }
}

function doPAction(action) // which action on layer Project is clicked
{

    hidemenu(letztelyr); 
    var encLasturl = simpleEscape(location.href);
    
    switch(action)
    {
        case 1:
        {
            location.href='administration_content_lockchange.html?lasturl=' + encLasturl + '&projectid=' + kontextparam+ '&initial=true';
            break;
        }
        case 2:
        {
            confirm('Sind Sie sicher, dass das Projekt veröffentlicht werden soll? \nEs sind noch Dateien in Bearbeitung.');
            break;
        }
        case 3:
        {
            location.href='../../../jsp/dialogs/publishproject.html?lasturl=' + encLasturl + '&projectid=' + kontextparam+ '&initial=true';
            break;
        }
        case 4:
        {
            location.href='administration_content_delete_project.html?lasturl=' + encLasturl + '&projectid=' + kontextparam+ '&initial=true';
            break;
        }
        case 10:
        {
            location.href='tasks_content_new_forproject.html?lasturl=' + encLasturl;
            break;
        }
        case 20:
        {
            location.href='administration_content_projecthistory_react.html?lasturl=' + encLasturl;
            break;
        }
        case 99:
        {
            location.href='tasks_content_new_forfile.html';
            break;
        }
    }
}

function doTAction(action) // which action on layer Task is clicked
{
    hidemenu(letztelyr);
    var encLasturl = simpleEscape(location.href);
    
    switch(action)
    {
        case 1:
        {
            location.href='tasks_content_dialogquery.html?lasturl=' + encLasturl + '&action=query&taskid='+ kontextparam;
            break;
        }
        case 2:
        { 
            location.href='tasks_content_detail.html?lasturl=' + encLasturl + '&action=accept&taskid='+ kontextparam; 
            break;
        }
        case 3:
        {
            location.href='tasks_content_detail.html?lasturl=' + encLasturl + '&action=due&taskid='+ kontextparam;
            break;
        }
        case 4:
        {
            location.href='tasks_content_dialogpriority.html?lasturl=' + encLasturl + '&taskid='+ kontextparam;
            break;
        }
        case 5:
        {
            location.href='tasks_content_dialogcomment.html?lasturl=' + encLasturl + '&taskid='+ kontextparam;
            break;
        }
        case 6:
        {
            location.href='tasks_content_dialogforward.html?lasturl=' + encLasturl + '&taskid='+ kontextparam;
            break;
        } 
        case 7:
        {
            location.href='tasks_content_detail.html?lasturl=' + encLasturl + '&action=ok&taskid='+ kontextparam; 
            break;
        } 
        case 8:
        {
            location.href='tasks_content_dialogmsg.html?lasturl=' + encLasturl + '&action=message&taskid='+ kontextparam; 
            break;
        }
        case 9:
        {
            location.href='tasks_content_dialogreakt.html?lasturl=' + encLasturl + '&taskid='+ kontextparam; 
            break;
        }
        case 10:
        {
            location.href='tasks_content_detail.html?lasturl=' + encLasturl + '&action=take&taskid='+ kontextparam;
            break;
        } 
    }
}

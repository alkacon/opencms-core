
function vars_resources() {
    this.stati = new Array("unchanged","changed","new","deleted");
    this.descr = new Array("Name","Title","Type","Changed&nbsp;on","Size","State","Owner","Group","Permission");
    this.actProject;
    this.onlineProject;
    this.lockedBy="locked by:";
    this.titleString="Arbeitsplatz von";
    this.actDirectory;
    

    // curent username
    this.userName = "tom";

    // view configuration (which columns are shown)
    this.viewcfg = 255;                 
}


function initialize_resources() {

/*
    vi.resource[0]=new res(                     - number (!)
            "image",                            - resourcename
            "pics/ic_file_image.gif"            - resource icon
    );
*/

    vi.iconPath="pics/";

    vi.resource[0]=new res("image",vi.iconPath+"ic_file_image.gif");
    vi.resource[1]=new res("text",vi.iconPath+"ic_file_plain.gif");
    vi.resource[2]=new res("script",vi.iconPath+"ic_file_script.gif");
    vi.resource[3]=new res("binary",vi.iconPath+"ic_file_binary.gif");
    vi.resource[4]=new res("page",vi.iconPath+"ic_file_page.gif");
    vi.resource[5]=new res("folder",vi.iconPath+"ic_file_folder.gif");
    vi.resource[6]=new res("newspage",vi.iconPath+"ic_file_newspage.gif");

/*
    addMenuEntry(
            0,                          - this menu is for which resourcetype
            "image",                    - title of menuitem
            "http://www.framfab.de",    - link of the menuitem
            "_top",                     - target
            "1222211112222"             - rules
             |||||  |   |
             |||||  |   locked from another user
             |||||  locked from me
             ||||deleted
             |||new             (unlocked)              
             ||changed
             |unchanged
             |
             online project?
    );
*/

    addMenuEntry(0,"image","http://www.framfab.de","_top","1222211111111");
    addMenuEntry(0,"11aaAabbbba","http://www.framfab.de","","1222211111111");
    addMenuEntry(0,"blaAh","","","1222211111111");
    addMenuEntry(0,"blaAh","","","1111111111111");
    addMenuEntry(0,"blaAh","","","1111111111111");
    addMenuEntry(0,"blaAh","","","1111111111111");
    addMenuEntry(0,"blaAh","","","1111111111111");

    addMenuEntry(1,"text","http://www.framfab.de","_top","1111111111111");
    addMenuEntry(1,"11aaAabbbba","http://www.framfab.de","","1111111111111");
    addMenuEntry(1,"blaAh","","","1111111111111");
    addMenuEntry(1,"blaAh","","","1111111111111");
    addMenuEntry(1,"blaAh","","","1111111111111");
    addMenuEntry(1,"blaAh","","","1111111111111");
    addMenuEntry(1,"blaAh","","","1111111111111");
    addMenuEntry(1,"blaAh","","","1111111111111");
    
    addMenuEntry(2,"script","http://www.framfab.de","_top","1111111111111");
    addMenuEntry(2,"11Aaaabbbba","http://www.framfab.de","","1111111111111");
    addMenuEntry(2,"blaAh","","","1111111111111");
    addMenuEntry(2,"blaAh","","","1111111111111");
    addMenuEntry(2,"blaAh","","","1111111111111");
    addMenuEntry(2,"blaAh","","","1111111111111");
    addMenuEntry(2,"blaAh","","","1111111111111");
    addMenuEntry(2,"blaAh","","","1111111111111");
    
    addMenuEntry(3,"binary","http://www.framfab.de","_top","1111111111111");
    addMenuEntry(3,"11aaaAbbbba","http://www.framfab.de","","1111111111111");
    addMenuEntry(3,"blaAh","","","1111111111111");
    addMenuEntry(3,"blaAh","","","1111111111111");
    addMenuEntry(3,"blaAh","","","1111111111111");
    addMenuEntry(3,"blaAh","","","1111111111111");
    addMenuEntry(3,"blaAh","","","1111111111111");
    addMenuEntry(3,"blaAh","","","1111111111111");
    addMenuEntry(3,"blaAh","","","1111111111111");
    addMenuEntry(3,"blaAh","","","111111111111");
    
    addMenuEntry(4,"page","http://www.framfab.de","_top","1111111111211");
    addMenuEntry(4,"11aaAabbbba","http://www.framfab.de","","1111111111111");
    addMenuEntry(4,"blaAh","","","1111111111111");
    addMenuEntry(4,"11aaAabbbba","","","1111111111111");
    addMenuEntry(4,"blaAh","","","1111111111111");
    addMenuEntry(4,"11aaAabbbba","","","1111111111111");
    
    addMenuEntry(5,"folder","http://www.framfab.de","_top","1111111111111");
    addMenuEntry(5,"11aaAabbbba","http://www.framfab.de","","1111111111111");
    addMenuEntry(5,"blaAh","","","1111111111111");
    addMenuEntry(5,"blaAh","","","1111111111111");
    addMenuEntry(5,"blaAh","","","1111111111111");
    addMenuEntry(5,"blaAh","","","1111111111111");
    addMenuEntry(5,"blaAh","","","1111111111111");
    addMenuEntry(5,"blaAh","","","1111111111111");
    
    addMenuEntry(6,"newspage","http://www.framfab.de","_top","1111111111111");
    addMenuEntry(6,"11aAabbbba","http://www.framfab.de","","1111111111111");
    addMenuEntry(6,"blaAh","","","1111111111111");
    addMenuEntry(6,"blaAh","","","1111111111111");
    addMenuEntry(6,"blaAh","","","1111111111111");
    addMenuEntry(6,"blaAh","","","1111111111111");
}
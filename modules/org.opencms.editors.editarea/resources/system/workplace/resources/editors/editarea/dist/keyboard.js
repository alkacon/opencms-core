var clavier_cds=new Object(146);
	clavier_cds[8]="Retour arriere";
	clavier_cds[9]="Tabulation";
	clavier_cds[12]="Milieu (pave numerique)";
	clavier_cds[13]="Entrer";
	clavier_cds[16]="Shift";
	clavier_cds[17]="Ctrl";
	clavier_cds[18]="Alt";
	clavier_cds[19]="Pause";
	clavier_cds[20]="Verr Maj";
	clavier_cds[27]="Esc";
	clavier_cds[32]="Espace";
	clavier_cds[33]="Page up";
	clavier_cds[34]="Page down";
	clavier_cds[35]="End";
	clavier_cds[36]="Begin";
	clavier_cds[37]="Fleche gauche";
	clavier_cds[38]="Fleche haut";
	clavier_cds[39]="Fleche droite";
	clavier_cds[40]="Fleche bas";
	clavier_cds[44]="Impr ecran";
	clavier_cds[45]="Inser";
	clavier_cds[46]="Suppr";
	clavier_cds[91]="Menu Demarrer Windows / touche pomme Mac";
	clavier_cds[92]="Menu Demarrer Windows";
	clavier_cds[93]="Menu contextuel Windows";
	clavier_cds[112]="F1";
	clavier_cds[113]="F2";
	clavier_cds[114]="F3";
	clavier_cds[115]="F4";
	clavier_cds[116]="F5";
	clavier_cds[117]="F6";
	clavier_cds[118]="F7";
	clavier_cds[119]="F8";
	clavier_cds[120]="F9";
	clavier_cds[121]="F10";
	clavier_cds[122]="F11";
	clavier_cds[123]="F12";
	clavier_cds[144]="Verr Num";
	clavier_cds[145]="Arret defil";



	function keyDown(e){
		if(!e){	// if IE
			e=event;
		}

		// send the event to the plugins
		for(var i in editArea.plugins){
			if(typeof(editArea.plugins[i].onkeydown)=="function"){
				if(editArea.plugins[i].onkeydown(e)===false){ // stop propaging
					if(editArea.nav['isIE'])
						e.keyCode=0;
					return false;
				}
			}
		}

		var target_id=(e.target || e.srcElement).id;
		var use=false;
		if (clavier_cds[e.keyCode])
			letter=clavier_cds[e.keyCode];
		else
			letter=String.fromCharCode(e.keyCode);

		var low_letter= letter.toLowerCase();

		if(letter=="Page up" && !editArea.nav['isOpera']){
			editArea.execCommand("scroll_page", {"dir": "up", "shift": ShiftPressed(e)});
			use=true;
		}else if(letter=="Page down" && !editArea.nav['isOpera']){
			editArea.execCommand("scroll_page", {"dir": "down", "shift": ShiftPressed(e)});
			use=true;
		}else if(editArea.is_editable==false){
			// do nothing but also do nothing else (allow to navigate with page up and page down)
			return true;
		}else if(letter=="Tabulation" && target_id=="textarea" && !CtrlPressed(e) && !AltPressed(e)){
			if(ShiftPressed(e))
				editArea.execCommand("invert_tab_selection");
			else
				editArea.execCommand("tab_selection");

			use=true;
			if(editArea.nav['isOpera'] || (editArea.nav['isFirefox'] && editArea.nav['isMacOS']) )	// opera && firefox mac can't cancel tabulation events...
				setTimeout("editArea.execCommand('focus');", 1);
		}else if(letter=="Entrer" && target_id=="textarea"){
			if(editArea.press_enter())
				use=true;
		}else if(letter=="Entrer" && target_id=="area_search"){
			editArea.execCommand("area_search");
			use=true;
		}else  if(letter=="Esc"){
			editArea.execCommand("close_all_inline_popup", e);
			use=true;
		}else if(CtrlPressed(e) && !AltPressed(e) && !ShiftPressed(e)){
			switch(low_letter){
				case "f":
					editArea.execCommand("area_search");
					use=true;
					break;
				case "r":
					editArea.execCommand("area_replace");
					use=true;
					break;
				case "q":
					editArea.execCommand("close_all_inline_popup", e);
					use=true;
					break;
				case "h":
					editArea.execCommand("change_highlight");
					use=true;
					break;
				case "g":
					setTimeout("editArea.execCommand('go_to_line');", 5);	// the prompt stop the return false otherwise
					use=true;
					break;
				case "e":
					editArea.execCommand("show_help");
					use=true;
					break;
				case "z":
					use=true;
					editArea.execCommand("undo");
					break;
				case "y":
					use=true;
					editArea.execCommand("redo");
					break;
				default:
					break;
			}
		}

		// check to disable the redo possibility if the textarea content change
		if(editArea.next.length > 0){
			setTimeout("editArea.check_redo();", 10);
		}

		setTimeout("editArea.check_file_changes();", 10);

		if(use){
			// in case of a control that sould'nt be used by IE but that is used => THROW a javascript error that will stop key action
			if(editArea.nav['isIE'])
				e.keyCode=0;
			return false;
		}
		//alert("Test: "+ letter + " ("+e.keyCode+") ALT: "+ AltPressed(e) + " CTRL "+ CtrlPressed(e) + " SHIFT "+ ShiftPressed(e));

		return true;

	};


	// return true if Alt key is pressed
	function AltPressed(e) {
		if (window.event) {
			return (window.event.altKey);
		} else {
			if(e.modifiers)
				return (e.altKey || (e.modifiers % 2));
			else
				return e.altKey;
		}
	};

	// return true if Ctrl key is pressed
	function CtrlPressed(e) {
		if (window.event) {
			return (window.event.ctrlKey);
		} else {
			return (e.ctrlKey || (e.modifiers==2) || (e.modifiers==3) || (e.modifiers>5));
		}
	};

	// return true if Shift key is pressed
	function ShiftPressed(e) {
		if (window.event) {
			return (window.event.shiftKey);
		} else {
			return (e.shiftKey || (e.modifiers>3));
		}
	};



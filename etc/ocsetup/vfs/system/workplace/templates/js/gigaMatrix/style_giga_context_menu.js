// Write the stylesheet reference
if(parseInt(navigator.appVersion)>3){
	if((navigator.appVersion.indexOf("Mac"))>=0)
		document.write('<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/giga_context_menus_ie.css\">');
	else{
		if(navigator.appName == "Microsoft Internet Explorer")
			document.write('<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/giga_context_menus_ie.css\">');
		else
			document.write('<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/giga_context_menus_ns.css\">');
	}
}

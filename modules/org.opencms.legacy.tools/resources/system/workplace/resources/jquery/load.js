function load_script(src, type) {
	var document_scripts = document.getElementsByTagName(type == 'js' ? "script" : "link");
	for (document_scripts_index = 0; document_scripts_index < document_scripts.length; ++document_scripts_index) {
		var document_script = document_scripts[document_scripts_index];
		if (type=='js') {
			if (document_script.src) {
				var test = document_script.src;
				if ((test.match(src+"$")==src) || (src.match(test+"$")==test)) return false;
			}
		} else {
			if (document_script.href) {
				var test = document_script.href;
				if ((test.match(src+"$")==src) || (src.match(test+"$")==test)) return false;
			}
		}		
	}
	var script;
	if (type=='js') {
		script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = src;
	} else {
		script = document.createElement('link');
		script.type = 'text/css';
		script.rel = 'stylesheet';
		script.href = src;
	}
	document.getElementsByTagName('head')[0].appendChild(script);
}
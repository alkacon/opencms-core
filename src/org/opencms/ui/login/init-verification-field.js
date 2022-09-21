{ 
    var fields = document.querySelectorAll(".o-verification-code-field");
    for (var i = 0; i < fields.length; i++) { 
        var field = fields[i];
        field.setAttribute("autocomplete", "one-time-code");
    }
}
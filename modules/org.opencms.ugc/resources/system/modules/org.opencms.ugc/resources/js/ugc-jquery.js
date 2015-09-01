 function UGC () {
 
    if (arguments.length < 3) {
        alert(
            "UGC requires at least 3 arguments:\n\n" +
            "1: The id of the form in the HTML\n" +
            "2: The mapping callback function\n" + 
            "3: The error callback function\n" + 
            "4: (optional) The custom form init callback function\n" +
            "5: (optional) The wait indicator callback function"
        );
    }
 
    this.formId = arguments[0];
    this.mappingsCallback = arguments[1];
    this.errorCallback = arguments[2];
    this.formInitCallback = arguments.length > 3 ? arguments[3] : null;
    this.waitIndicatorCallback = arguments.length > 4 ? arguments[4] : null;
    
    // the mappings from form field id's to XML content xpath
    this.mappings = {};

    // UCG session
    this.session = null;
    // the content as it was provided from the server
    this.content = null;    

    // a copy / clone of the content, used for the modified result
    this.contentClone = null;
    
    this.OPTIONAL = "OPTIONAL";
    this.UPLOAD = "UPLOAD";
    
    // element that directly returns the form DOM element after initialization
    this.form = null;
 }
 
function UGCMapping (ugc, args) {
    // this is the main mapping object that maps a form Id to a content xpath
    this.formId = args[0];
    this.contentPath = args[1];
    this.isUpload = false;
    this.deleteEmptyValue = false;
    this.notEmpty = false;
    if (args.length > 1) {
        // check the additional arguments for further options
        for (i = 2; i < args.length; i++) {
            if (ugc.UPLOAD === args[i]) {
                // mark this as an upload field, which means we don't fill it automatically in the form and content             
                this.isUpload = true;               
            } else if (ugc.OPTIONAL === args[i]) {
                // decides if an empty value is actually deleted in the modified content, 
                // or kept as an empty string (the default)
                // use this for optional values in the XML content that should be fully removed
                // if only an empty string is provided in the form
                this.deleteEmptyValue = true;
            }
            // else { alert("Ignoring: " + args[i]); }
        }
    }
}

UGC.prototype.map = function() {
    // add a new mapping
    var mapping = new UGCMapping(this, arguments);
    // this.debugMap(mapping);
    this.mappings[arguments[0]] = mapping;
}
 
UGC.prototype.debugMap = function(mapping) {
    alert("Xpath: " + mapping.contentPath + "\nFormId: " +  mapping.formId + "\nDeleteEmpty: " + mapping.deleteEmptyValue + "\nUpload: " + mapping.isUpload);
}       

UGC.prototype.debugContent = function(contentArray) {
    var result = "";
    for (var key in contentArray) {
        if (contentArray.hasOwnProperty(key)) {
            var value = contentArray[key];
            result += "Xpath: " + key + "\nValue: " + value + "\n\n";
        }
    }
    alert(result);
}

 UGC.prototype.getForm = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // lookup the form element with the given name
        return $("#" + this.formId + " :input[name='" + arguments[0] + "']");
    } else if (arguments.length == 0) {
        // zero arguments, return the complete form
        var theForm = $("#" + this.formId);
        if (this.form == null) {
            // set the form DOM access element 
            this.form = theForm[0];     
        }
        return theForm;
    }
    // no argument returns null
    return null;
}; 

UGC.prototype.getFormVal = function(name) {
    // lookup the value from the form element with the given name
    return this.getForm(name).val();
};

UGC.prototype.getXpath = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        return this.mappings[arguments[0]].contentPath;
    }
    return null;
}

 UGC.prototype.formHas = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // single value: lookup the value from the form element with the given name
        return this.getFormVal(arguments[0]).trim().length > 0;
    } else {
        // iterate the array of arguments and check for all with shout-circuit
        for (i = 0; i < arguments.length; i++) {    
            if (this.formHasNot(arguments[i])) {
                return false;
            }
        }
        return true;
    }
    // in case of no arguments at all
    return false;
};

 UGC.prototype.formHasNot = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // single value: lookup the value from the form element with the given name
        return this.getFormVal(arguments[0]).trim().length <= 0;
    } else {
        // iterate the array of arguments and check for all with shout-circuit
        for (i = 0; i < arguments.length; ++i) {
            if (this.formHas(arguments[i])) {
                return false;
            }
        }   
        return true;
    }
    // in case of no arguments at all
    return false;
};

 UGC.prototype.formHasOne = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // single value: lookup the value from the form element with the given name
        return this.formHas(arguments[0]);
    } else {
        // iterate the array of arguments and check for all with shout-circuit
        for (i = 0; i < arguments.length; ++i) {
            if (this.formHas(arguments[i])) {
                return true;
            }
        }   
        return false;
    }
    // in case of no arguments at all
    return false;
};

 UGC.prototype.contentHas = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // single value: lookup the value from the content array
        var value = this.getContent(arguments[0]);
        return (typeof value === "undefined") ? false : true;
    } else {
        // iterate the array of arguments and check for all with shout-circuit
        for (i = 0; i < arguments.length; i++) {    
            if (this.contentHasNot(arguments[i])) {
                return false;
            }
        }
        return true;
    }
    // in case of no arguments at all
    return false;
};

 UGC.prototype.contentHasNot = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // single value: lookup the value from the content array
        var value = this.getContent(arguments[0]);
        return (typeof value === "undefined") ? true : false;
    } else {
        // iterate the array of arguments and check for all with shout-circuit
        for (i = 0; i < arguments.length; i++) {    
            if (this.contentHas(arguments[i])) {
                return false;
            }
        }
        return true;
    }
    // in case of no arguments at all
    return false;
};

 UGC.prototype.contentHasOne = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // single value: lookup the value from the content array
        return this.contentHas(arguments[0]);
    } else {
        // iterate the array of arguments and check for all with shout-circuit
        for (i = 0; i < arguments.length; ++i) {
            if (this.contentHas(arguments[i])) {
                return true;
            }
        }   
        return false;
    }
    // in case of no arguments at all
    return false;
};

 UGC.prototype.setForm = function() {
    // check if the name parameter was provided, if not we have to initialize everything later
    if (this.content != null) {
        if (arguments.length == 1) {
            // set the form element with the given name to the content value stored in the mapping with the same name
            this.getForm(arguments[0]).val(this.content[this.mappings[arguments[0]].contentPath]);
        } else if (arguments.length == 2) {
            // set the form element with the given name to the given value
            this.getForm(arguments[0]).val(arguments[1]);            
        } else {
            // fill the complete form with all mapped values
            this.fillForm();
        }
    }
};

 UGC.prototype.setContent = function() {
    // check if the name parameter was provided, if not we have to initialize everything later
    var formId = (arguments.length > 0) ? arguments[0] : null;
    if (formId != null) {
        // set the content (clone) value stored in the mapping with the given name to the form element value with the same 
        var value = (arguments.length > 1) ? arguments[1] : this.getFormVal(formId);
        var mapping = this.mappings[formId];
        this.contentClone[mapping.contentPath] = value;
        if (value.trim().length <= 0) {
            if (mapping.deleteEmptyValue) {
                this.contentClone[mapping.contentPath] = null;
            }       
        }
    } else {
        // no form id provided, set the complete content with all mapped values
        this.fillContent();     
    }
};

 UGC.prototype.deleteContent = function(name) {
    if (this.contentClone != null) {
        // delete the content (clone) value stored in the mapping with the given name
        this.contentClone[this.mappings[name].contentPath] = null;
    }
};

 UGC.prototype.deleteParentContent = function(name) {
    if (this.contentClone != null) {
        // delete the content (clone) value stored in the mapping with the given name
        var xpath = this.mappings[name].contentPath;
        var parentName = xpath.substring(0, xpath.lastIndexOf("/"));
        for (var key in this.contentClone) {
            if (this.contentClone.hasOwnProperty(key) && (key.indexOf(parentName) == 0)) {
                delete this.contentClone[key];
            }
        }
        this.contentClone[parentName] = null;
    }
};

 UGC.prototype.getContent = function() {
    // check if we have one or more arguments
    if (arguments.length == 1) {
        // return the selected value from the original content array
        return this.contentClone[this.getXpath(arguments[0])];
    } 
    // return the content complete clone map
    return this.contentClone;
};

UGC.prototype.createContentClone = function() {
    // initialize the clone array
    this.contentClone = {}; 
    if (this.content != null) {
        // copy all the elements from the content to the clone
        for (var key in this.content) {
            if (this.content.hasOwnProperty(key)) {
                this.contentClone[key] = this.content[key];
            }
        }
    }
    // return the generated clone
    return this.contentClone;
}

UGC.prototype.fillForm = function() {
    // iterate over all mappings, fill the form with the mapped values
    for (var key in this.mappings) {
        var mapping = this.mappings[key];
        if (!mapping.isUpload) { 
            this.setForm(mapping.formId);
        } // else { alert("Ignoring: " + mapping.contentPath); }
    }
}

UGC.prototype.fillContent = function() {
    // iterate over all mappings, fill the content with values from the mapped form elements
    for (var key in this.mappings) {
        var mapping = this.mappings[key];   
        if (!mapping.isUpload) { 
            this.setContent(mapping.formId);
        } // else { alert("Ignoring: " + mapping.contentPath); }
    }
}

UGC.prototype.setSession = function() {
    // set the session to the provided parameter
    this.session = arguments[0];
    // initialize the content from the session
    this.content = this.session.getValues();
    if (this.contentClone == null) {
        // initialize the content clone on first call
        this.createContentClone();  
    }    
};

UGC.prototype.getSession = function() {
    // wrapper for session
    return this.session;
};

UGC.prototype.destroySession = function() {
    // wrapper for session
    if (this.session != null) this.session.destroy();
};

// global UGC variable to avoid context problems when initializing
var globalUGC = null;

UGC.prototype.initSession = function() {
    // method to be used in callback from UGC main API
    globalUGC.setSession(arguments[0]);
    // initialize the form with the data from the context
    globalUGC.setForm();
    if (globalUGC.formInitCallback != null) {
        // custom user form initialization
        globalUGC.formInitCallback();
    }    
};

UGC.prototype.init = function() {
    // initialize the mappings
    this.mappingsCallback();
    // initialize the user generated content API
    var sessionId = arguments[0];  
    if (this.waitIndicatorCallback != null) {
        OpenCmsUgc.setWaitIndicatorCallback(this.waitIndicatorCallback);
    }
    OpenCmsUgc.initFormForSession(sessionId, this.form, this.initSession, this.errorCallback);
};

UGC.prototype.setWaitIndicator = function() {
    // set the wait indicator callback
    this.waitIndicatorCallback = arguments[0];
}

UGC.prototype.setMappings = function() {
    // set the wait indicator callback
    this.mappingsCallback = arguments[0];
}

UGC.prototype.setError = function() {
    // set the wait indicator callback
    this.errorCallback = arguments[0];
}

UGC.prototype.setFormInit = function() {
    // set the wait indicator callback
    this.formInitCallback = arguments[0];
}    

UGC.prototype.initForm = function() {
    // method to be used for checking if the form exists and initializing the mappings
    if ((arguments.length == 0) && (this.getForm() != null)) {
        // store the used UGC object to access later in the init function
        globalUGC = this;
        return true;
    }
    return false;
};

UGC.prototype.uploadFiles = function() {
    // convenience wrapper to access "uploadFiles" in the UGC session 
    if (arguments.length != 2) {
        alert(
            "UGC.uploadFiles requires 2 arguments:\n\n" +
            "1: Array of id's that indicate the fields with the file uploads\n" +
            "2: The after upload handler function callback"
        );
    }
    this.getSession().uploadFiles(arguments[0], arguments[1], this.errorCallback);    
}

UGC.prototype.saveContent = function() {
    // convenience wrapper to access "saveContent" in the UGC session 
    if (arguments.length != 2) {
        alert(
            "UGC.saveContent requires 2 arguments:\n\n" +
            "1: Content object to save\n" +
            "2: The after save handler function callback"
        );
    }
    this.getSession().saveContent(arguments[0], arguments[1], this.errorCallback);    
}

UGC.prototype.validate = function() {
    // convenience wrapper to access "validate" in the UGC session 
    if (arguments.length != 2) {
        alert(
            "UGC.validate requires 2 arguments:\n\n" +
            "1: Content object to validate\n" +
            "2: The validation results handler function callback"
        );
    }
    this.getSession().validate(arguments[0], arguments[1]);    
}

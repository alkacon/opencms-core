// resetting the jQuery instance
var __cmsJQuery=jQuery;
if (__storedJqueryInstance!=null){
    jQuery=__storedJqueryInstance;
    __storedJqueryInstance=null;
    $=jQuery;
}
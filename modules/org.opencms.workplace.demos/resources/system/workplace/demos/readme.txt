put here your demo tools.

to activate the demos view follow these steps:
- add the following node to your opencms-workplace.xml configuration file:
        <root>
          <key>demos</key>
          <uri>/system/workplace/demos/</uri>
          <name>${key.GUI_DEMOS_VIEW_ROOT_NAME_0}</name>
          <helptext>${key.GUI_DEMOS_VIEW_ROOT_HELP_0}</helptext>
        </root>
under /opencms/workplace/tool-manager/roots/

- if not already set, set the following properties for the /system/workplace/views/demos/ folder:
cache=bypass
navText=${key.GUI_DEMOS_VIEW_ROOT_NAME_0}
default-file=admin-fs.jsp

var version = cms.version();
var updateableVersions = new Array(
    "4.4.0 Gom Jabbar",
    "4.5.0 Gom Jabbar",
    "4.5.1 Gom Jabbar",
    "4.5.2 Gom Jabbar",
    "4.5.3 Gom Jabbar" );
var ready = false;

echo("");
echo("Checking OpenCms version-number");
echo("Current Version of OpenCms is " + version);
for(i=0; i < updateableVersions.length; i++) {
    echo("Checking " + updateableVersions[i]);
    if(updateableVersions[i] == version) {
        echo("Ready for patch");
        ready = true;
        break;
    }
}

if(! ready) {
    echo("");
    echo("");
    echo("");
    echo("Check failed.");
    echo("You cannot patch this version.");
    echo("Please press <ctrl> c to abort.");
    for(;;) {
        input();
    }
}

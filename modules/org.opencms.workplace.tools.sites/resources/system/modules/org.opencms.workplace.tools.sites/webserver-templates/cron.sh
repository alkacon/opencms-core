#!/bin/bash

find_webconfiguration () {
        find /var/lib/tomcat/webapps/opencms/WEB-INF/server-scripts/configs/ -name 'opencms_*' -exec cp {} /etc/apache2/sites-available/ \;
}


check_site_enabled () {
cd /etc/apache2/sites-available
for file in opencms_*;
do
        if [ ! -f /etc/apache2/sites-enabled/$file ]; then
                a2ensite $file
                /etc/init.d/apache2 reload
        fi
done

}

find_webconfiguration
check_site_enabled

# ---------------------------------------------------
# Cron Expression:
# */15 * * * * /root/scripts/copy_vhosts.sh

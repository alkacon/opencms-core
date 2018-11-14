#!/bin/bash

##################
#
# Display usage info.
#
showUsage() {
    echo ""
    echo "${red}Usage: `basename "${0}"` ${bold}[LOCALE] [DIRECTION]${normal}"
    echo ""
    echo "Copies localization files between the file system and the OpenCms database."
    echo ""
    echo "Supported ${bold}[DIRECTION]${normal}s:"
    echo "${bold}  --toRfs     ${normal}Copy to the RFS from mounted OpenCms VFS"
    echo "${bold}  --fromRfs   ${normal}Copy from the RFS to the mounted OpenCms VFS"
    echo "${bold}  --testOnly  ${normal}Don't copy anything, only testing the replacement"
    echo ""
    exit 1
}


##################
#
# Set env variable for options.
#
setOptions() {

    while true; do
        case "${1}" in
            --fromRfs )
                MODE="fromRfs"
                echo "Using direction: --fromRfs"
                shift ;;
            --toRfs )
                MODE="toRfs"
                echo "Using direction: --toRfs"
                shift ;;
            --testOnly )
                MODE="testOnly"
                echo "Only testing the replacement, no actual copy is performed"
                shift ;;
            --verbose )
                OPT_VERBOSE="true"
                CMD_OPTS=" -v "
                echo "Activated option: --verbose"
                shift ;;
            "" ) break ;;
            * )
                echo "Invalid option \"${1}\" provided!"
                showUsage
                break ;;
        esac
    done
}


##################
#
# Set colors if stdout is a terminal.
#
if test -t 1; then
    # see if it supports colors...
    NCOLORS=$(tput colors)
    if test -n "${NCOLORS}" && test ${NCOLORS} -ge 8; then
        bold="$(tput bold)"
        underline="$(tput smul)"
        standout="$(tput smso)"
        normal="$(tput sgr0)"
        black="$(tput setaf 0)"
        red="$(tput setaf 1)"
        green="$(tput setaf 2)"
        yellow="$(tput setaf 3)"
        blue="$(tput setaf 4)"
        magenta="$(tput setaf 5)"
        cyan="$(tput setaf 6)"
        white="$(tput setaf 7)"
    fi
fi

LOCALE="${1}"
MODULE_PATH="modules/org.opencms.localizations"
VFS_PATH="${OCMOUNT}/system/${MODULE_PATH}/i18n"

# Check opencms is properly mounted
# if [ -z "${OCMOUNT}" ] || [ ! -d "${VFS_PATH}/" ]; then
if [ ! -d "${VFS_PATH}/" ]; then
    echo ""
    echo "${red}Error: ${bold}The OpenCms VFS mount folder is not accessible!${normal}"
    echo "Make sure the environment variable OCMOUNT is correctly set."
    echo "OCMOUNT='${OCMOUNT}' VFS_PATH='${VFS_PATH}'"
    echo ""
    exit 1
fi

# Check if called with the correct number of parameters
if [ -z "${LOCALE}" ]; then
    showUsage
fi

setOptions "${@:2}"

LOCALE_PATH="./modules/org.opencms.locale.${LOCALE}/resources/system/workplace/locales/${LOCALE}/messages"
CORE_BUNDLES=$(find ./src* ./modules/* \( -name *messages.properties -o -name *workplace.properties \))
CORE_BUNDLES2=$(find ./src* -name *messages.properties)

readarray -t EN_BUNDLES_RFS <<< "${CORE_BUNDLES}"

EN_BUNDLES_VFS=()
LOCALE_BUNDLES_RFS=()
LOCALE_BUNDLES_VFS=()

for IDX in "${!EN_BUNDLES_RFS[@]}"
do
    printf -v NUM "%03d" $IDX

    # replace all / chars in RFS path with a -
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_RFS[$IDX]//\//.}
    # remove .. in path
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]//\.\./}
    # remove prefix src-modules. src-setup. and src.
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]/#src-setup\./}
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]/#src-modules\./}
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]/#src\./}
    # remove prefix modules ... classes.
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]/#modules*classes\./}
    # replace suffix workplace.properties with messages.properties
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]/%\workplace.properties/messages.properties}
    # replace suffix .properties with _en
    EN_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]/%\.properties/_en}

    # replace RFS prefix src-modules and src-setup with src
    LOCALE_BUNDLES_RFS[$IDX]=${EN_BUNDLES_RFS[$IDX]/#\.\/src-modules/./src}
    LOCALE_BUNDLES_RFS[$IDX]=${LOCALE_BUNDLES_RFS[$IDX]/#\.\/src-setup/./src}
    # replace RFS prefix modules ... classes/ with src
    LOCALE_BUNDLES_RFS[$IDX]=${LOCALE_BUNDLES_RFS[$IDX]/#\.\/modules*classes/./src}
    # now replace src prefix locale path
    LOCALE_BUNDLES_RFS[$IDX]=${LOCALE_BUNDLES_RFS[$IDX]/#\.\/src/$LOCALE_PATH}
    # insert locale before suffix .properties
    LOCALE_BUNDLES_RFS[$IDX]=${LOCALE_BUNDLES_RFS[$IDX]/%\.properties/_$LOCALE.properties}

    LOCALE_BUNDLES_VFS[$IDX]=${EN_BUNDLES_VFS[$IDX]//_en/_$LOCALE}

    echo ""
    echo "${NUM}: ${EN_BUNDLES_RFS[$IDX]}"
    echo " ${LOCALE}: ${LOCALE_BUNDLES_RFS[$IDX]}"
    echo " --> ${EN_BUNDLES_VFS[$IDX]}"
    echo " --> ${LOCALE_BUNDLES_VFS[$IDX]}"

    if [ "${MODE}" == "fromRfs" ]; then
        if [ -f ${EN_BUNDLES_RFS[$IDX]} ]; then
            cp -v ${EN_BUNDLES_RFS[$IDX]} ${VFS_PATH}/${EN_BUNDLES_VFS[$IDX]}
        fi

        if [ -f ${LOCALE_BUNDLES_RFS[$IDX]} ]; then
            cp -v ${LOCALE_BUNDLES_RFS[$IDX]} ${VFS_PATH}/${LOCALE_BUNDLES_VFS[$IDX]}
        fi
    fi

    if [ "${MODE}" == "toRfs" ]; then
        if [ -f ${VFS_PATH}/${EN_BUNDLES_VFS[$IDX]} ]; then
            cp -v ${VFS_PATH}/${EN_BUNDLES_VFS[$IDX]} ${EN_BUNDLES_RFS[$IDX]}
        fi

        if [ -f ${VFS_PATH}/${LOCALE_BUNDLES_VFS[$IDX]} ]; then
            cp -v ${VFS_PATH}/${LOCALE_BUNDLES_VFS[$IDX]} ${LOCALE_BUNDLES_RFS[$IDX]}
        fi
    fi
done



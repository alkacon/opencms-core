#!/bin/bash
# Simplified version of module-checkin.sh meant for a manual Git workflow using the OpenCms JLAN share as a source of exported modules. 

getExportedModule(){
	echo $( ls | grep "${module}.zip" | head -n1 )
}

testGitRepository(){
	__pwd=$(pwd)
	cd $REPOSITORY_HOME
	if [[ $? != 0 ]]; then
		echo "ERROR: The GIT repository's main folder \"$REPOSITORY_HOME\" does not exist."
		exit 5
	fi
	echo "Status of the git repository:"
	git status
	echo
	if [[ $? != 0 ]]; then
		echo "ERROR: You have not specified a GIT repository's main folder via\
              \"REPOSITORY_HOME\" ($REPOSITORY_HOME)"
		exit 6
	fi
	if [[ -n $(git status --porcelain) ]]; then
		if [[ $resetHead == 1 || $resetRemoteHead == 1 || $ignoreUnclean == 1 ]]; then
			echo " WARN: found unclean git repository, but continue."
		else
			echo " ERROR: found unclean git repository."
			exit 10
		fi
	else
		echo " * Test ok: found clean git repository at \"$(pwd)\"."
	fi
	cd $__pwd
}

adjustGitConfig(){
	__pwd=$(pwd)
	cd $REPOSITORY_HOME

	if [[ ! -z $userName ]]; then
		oldUserName=$(git config --local user.name)
		git config --replace-all user.name "$userName"
		adjustedUserName=1
		echo "     * Adjusted local git user.name from \"$oldUserName\" to \"$userName\"."
	else
		echo "     * Do not adjust current user.name. Using \"$(git config user.name)\"."
	fi
	if [[ ! -z $userEmail ]]; then
		oldUserEmail=$(git config --local user.email)
		git config --replace-all user.email "$userEmail"
		adjustedUserEmail=1
		echo "     * Adjusted local git user.email from \"$oldUserEmail\" to \"$userEmail\"."
	else
		echo "     * Do not adjust current user.email. Using \"$(git config user.email)\"."
	fi
	echo "Here's your local git configuration after the adjustments:"
	git config --local -l | awk '$0="   * "$0'
	echo
	cd $__pwd
}

resetGitConfig(){
	__pwd=$(pwd)
	cd $REPOSITORY_HOME
	if [[ $adjustedUserName == 1 ]]; then
		if [[ -z $oldUserName ]]; then
			git config --unset user.name
			echo "     * Reset local git user.name from \"$userName\" to <unset>."
		else
			git config --replace-all user.name "$oldUserName"
			echo "     * Reset local git user.name from \"$userName\" to \"$oldUserName\"."
		fi
	else
		echo "     * The git user.name was not adjusted. So there is nothing to do."
	fi
	if [[ $adjustedUserEmail == 1 ]]; then
		if [[ -z $oldUserEmail ]]; then
			git config --unset user.email
			echo "     * Reset local git user.email from \"$userEmail\" to <unset>."
		else
			git config --replace-all user.email "$oldUserEmail"
			echo "     * Reset local git user.email from \"$userEmail\" to \"$oldUserEmail\"."
		fi
	else
		echo "     * The git user.email was not adjusted. So there is nothing to do."
	fi
	echo "Here's your local git configuration after the reset:"
	git config --local -l | awk '$0="   * "$0'
	echo
	cd $__pwd
}

testModuleExportFolder(){
	__pwd=$(pwd)
	cd $moduleExportFolder
	if [[ $? != 0 ]]; then
		echo "ERROR: The specified module export folder \"$moduleExportFolder\" does not exist."
		exit 7
	fi
	echo " * Test ok: Module export folder \"$(pwd)\" exists."
	cd $_pwd
}

testModuleMainFolder(){
	__pwd=$(pwd)
	cd $MODULE_PATH
	if [[ $? != 0 ]]; then
		echo "ERROR: The specified module export folder \"$MODULE_PATH\" does not exist."
		exit 8
	fi
	echo " * Test ok: Module main folder \"$(pwd)\" exists."
	cd $__pwd
}


echo
echo "Started script for automatic check in of OpenCms modules into a GIT repository."
echo "-------------------------------------------------------------------------------"
echo
echo "Reading command line arguments ..."

#read commandline arguments
while [ "$1" != "" ]; do
    case $1 in
        -m | --modules )       	shift
                                modulesToExport=$1
								echo " * Read modules to export: \"$modulesToExport\"."
                                ;;
        --no-exclude-libs )		excludeLibs=0;
        						echo " * Read no-exclude-libs option."
        						;;
        --exclude-libs )		excludeLibs=1;
        						echo " * Read exclude-libs option."
        						;;
		--export-folder )		shift
								moduleExportFolder=$1
								echo " * Read module export folder: \"$moduleExportFolder\"."
								;;
		--ignore-unclean )      ignoreUnclean=1
								echo " * Read ignore-unclean option."
								;;
		--no-ignore-unclean )   ignoreUnclean=0
								echo " * Read no-ignore-unclean option."
								;;
		--copy-and-unzip )      copyAndUnzip=1
								echo " * Read copy-and-unzip option."
								;;
		--no-copy-and-unzip )   copyAndUnzip=0
								echo " * Read no-copy-and-unzip option."
								;;
        * )             		configfile=$1
								echo " * Read config file: \"$configfile\"."
    esac
    shift
done

echo
echo "Reading configuration file ..."
if [[ -z "$configfile" ]]; then
	echo " * ERROR: No config file provided."
	exit 3;

fi
source $configfile
echo " * Read file \"$configfile\":"
cat $configfile | awk '$0="   * "$0'

echo
echo "Values read from configuration:"
echo
echo "* Default modules : ${DEFAULT_MODULES_TO_EXPORT}"
echo "* Repository home : ${REPOSITORY_HOME}"
echo "* Module path     : ${MODULE_PATH}"
echo "* Module export to: ${MODULE_EXPORT_FOLDER}"

echo
echo "Setting parameters ..."

## set modules to export
if [[ -z "$modulesToExport" ]]; then
	modulesToExport=$DEFAULT_MODULES_TO_EXPORT
else
	newModules=""
	for module in $modulesToExport; do
		if [[ ! ($DEFAULT_MODULES_TO_EXPORT =~ (^| )$module($| ) ) ]]; then
			newModules="${newModules}${module} "
		fi
	done
	if [[ $newModules != "" ]]; then
		sed -i "/^DEFAULT_MODULES_TO_EXPORT/s/=\"/=\"$newModules/" $configfile
		echo
		echo " * Added new modules \"$newModules\" to the config file."
		echo
	fi
fi
echo " * Set modules to export: \"$modulesToExport\"."

## set export mode
if [[ -z "$exportMode" ]]; then
	exportMode=$MODULE_EXPORT_MODE
fi
case $exportMode in
	1 )	;;
	* ) exportMode=0
esac
echo " * Set export mode: $exportMode."

## set module export folder
if [[ -z "$moduleExportFolder" ]]; then
	moduleExportFolder=$MODULE_EXPORT_FOLDER
fi
echo " * Set module export folder: \"$moduleExportFolder\"."

echo
echo "Testing module export folder ..."
testModuleExportFolder


echo
echo "Setting parameters (continued) ..."

## set ignore-unclean
if [[ -z "$ignoreUnclean" ]]; then
	if [[ -z "$GIT_IGNORE_UNCLEAN" ]]; then
		ignoreUnclean=0
	else
		ignoreUnclean=$GIT_IGNORE_UNCLEAN
	fi
fi
echo " * Set ignore-unclean: $ignoreUnclean."

## set copy-and-unzip flag
if [[ -z "$copyAndUnzip" ]]; then
	if [[ -z "$COPY_AND_UNZIP" ]]; then
		copyAndUnzip=1
		echo " * Copy & unzip of module zips not specified. Using mode 1, i.e., do copy and unzip."
	else
		copyAndUnzip=$COPY_AND_UNZIP
	fi
fi
echo " * Set copy-and-unzip: $copyAndUnzip."


## set export libs flag
if [[ -z "$excludeLibs" ]]; then
	if [[ -z "$DEFAULT_EXCLUDE_LIBS" ]]; then
		excludeLibs=0
	else
		excludeLibs=$DEFAULT_EXCLUDE_LIBS
	fi
fi
echo " * Set exclude libs flag: $excludeLibs."

# Read associative array from variable MODULE_MAPPINGS, which should have the form "key1=value1 key2=value2..."  

declare -A moduleMappings
if [ ! -z "$MODULE_MAPPINGS" ] ; then
	for MAPPING in $MODULE_MAPPINGS ; do
		IFS="=" read k v <<< "$MAPPING"
		moduleMappings[$k]="$v"		
	done
	command -v perl || {
		echo "Perl is required when using module mappings."
		exit 15
	} 
fi


# test git repository
echo
echo "Testing Git repository and adjusting user information"
testGitRepository

echo
echo "Testing module main folder of the specified repository ..."
testModuleMainFolder

echo "Copy and unzip modules ..."
if [ $copyAndUnzip == 1 ]; then

## copy and unzip modules
	for module in $modulesToExport; do
		echo
		echo " * Handling module ${module} ..."
		echo
		cd $moduleExportFolder
		fileName=$(getExportedModule)
		if [[ ! -z "$fileName" ]]; then
			targetModule=${moduleMappings[$module]}
			if [ -z "$targetModule" ] ; then
				targetModule=$module
			fi 
			echo "   * Found zip file ${fileName}."
			#switch to project's module path
			cd "${MODULE_PATH}"
			#check if a subdirectory for the module exists - if not add it
			if [ ! -d "$targetModule" ]; then
				echo "   * Creating missing module directory \"$targetModule\" under \"$(pwd)\"."
				mkdir $targetModule
			fi
			#go to the modules' subfolder in the project
			cd $targetModule
			#remove leading "/" from MODULE_RESOURCES_SUBFOLDER, if necessary
			if [[ (! -z "$MODULE_RESOURCES_SUBFOLDER") && (${MODULE_RESOURCES_SUBFOLDER:0:1} == "/") ]]; then
				MODULE_RESOURCES_SUBFOLDER=${MODULE_RESOURCES_SUBFOLDER:1}
			fi
			#if necessary, add the resources' subfolder of the module
			if [[ (! -z "$MODULE_RESOURCES_SUBFOLDER") && (! -d "$MODULE_RESOURCES_SUBFOLDER") ]]; then
				echo "   * Creating missing resources subfolder \"$MODULE_RESOURCES_SUBFOLDER\"\
				       under $(pwd)."
				mkdir $MODULE_RESOURCES_SUBFOLDER
			fi
			#if there's a resources subfolder, switch to it
			if [[ -d "$MODULE_RESOURCES_SUBFOLDER" ]]; then
				cd $MODULE_RESOURCES_SUBFOLDER
			fi
			#delete all resources currently checked in in the project
			if [[ "$(pwd)" == "${MODULE_PATH}"* ]]; then
				echo "   * Removing old version of the module resources under $(pwd)."
				rm -fr ./{.[^.],}*
				if [[ $? != 0 ]]; then
					echo
					echo "ERROR: Failed to remove all resources under $(pwd)."
					echo "Exit script with exit code 13."
					echo
					exit 13
				fi
			else
				echo "   * ERROR: Something went wrong the current directory \($(pwd)\) is not a\
				  subdirectory of the repository's configured modules main folder (${MODULE_PATH})."
				exit 4
			fi
			echo "   * Copying "${moduleExportFolder}/${fileName}" to $(pwd) ..."
			#copy the new module .zip
			cp "${moduleExportFolder}/${fileName}" ./
			if [[ $? != 0 ]]; then
				echo
				echo "ERROR: Failed to copy \"${moduleExportFolder}/${fileName}\" to $(pwd)."
				echo "Exit script with exit code 13."
				echo
				exit 13
			fi
			echo "   * Unzipping copied file."
			#unzip it
			unzip -o "${fileName}" | awk '$0="     "$0'
			if [[ $? != 0 ]]; then
				echo
				echo "ERROR: Failed to unzip \"${fileName}\" in $(pwd)."
				echo "Exit script with exit code 13."
				echo
				exit 13
			fi
			if [ ! "$module" == "$targetModule" ] ; then
				echo "Adjusting module name from $module to $targetModule in manifest" 
				manifest=$(find -type f -name manifest.xml | head -1)
				echo "CWD=$(pwd)" 
				echo "Manifest path: $manifest" 
				if [ ! -z "$manifest" ] ; then
					export targetModule
					perl -ne 'if (/<module>/../<\/module>/) { s#<name>.*?</name>#<name>$ENV{"targetModule"}</name>#; } ; print;' < $manifest > "${manifest}.tmp"
					tmpContent=$(cat "${manifest}.tmp")
					if [ ! -z "$tmpContent" ] ; then
						rm "$manifest" 
						mv "${manifest}.tmp" "$manifest" 
					fi
				fi
			fi 
			 
			echo "   * Deleting copy of the .zip file."
			#remove the .zip file
			rm "${fileName}"
			#remove lib/ subfolder if necessary
			echo "   * Removing lib folder ..."
			if [[ $excludeLibs == 1 ]]; then
				libFolder="system/modules/${module}/lib"
				if [[ -d "$libFolder" ]]; then
					rm -fr "$libFolder"
					echo "     * ... lib/ folder \"$(pwd)/$libFolder\" removed."
				else
					echo "     * ... lib/ folder \"$(pwd)/$libFolder\" does not exist. Do nothing."
							fi
			else
				echo "     * ... lib folder shall not be removed. Do nothing."
			fi
		else
			echo "   ! WARN: Skipped module $module because the zip file was not found."
		fi
	done

else
	echo " * Copy and unzip is disabled. Not copying and unzipping modules."
fi

echo
# commit changes
cd $REPOSITORY_HOME

echo
echo
echo "Script completed successfully."
echo "------------------------------"
echo

exit 0

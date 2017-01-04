#!/bin/bash
# This script is called, when the configured modules are already exported from a JSP in OpenCms
# It is called as
# ./modules-chekin.sh <config-file> [modules]
# if no modules are given as second parameter, the module list from the config-file are taken

## Error codes
#  1 - pull failed
#  2 - push failed
#  3 - no config file provided
#  4 - something went wrong when changing folders
#  5 - the configured repository main folder does not exist
#  6 - the configured repository main folder is not a repository main folder
#  7 - the module export folder does not exist
#  8 - the repository's module main folder does not exist
#  9 - the hard reset to ORIG_HEAD failed
# 10 - git repository unclean
# 11 - the hard reset to HEAD failed
# 12 - backup folder does not exist
# 13 - permission problems during file operations
# 14 - checkout error

getExportedModule(){
	case $exportMode in
		1 ) echo $( ls -t | grep "${module}[\.\-\_]\([0-9]\{1,4\}\.\)\{0,3\}zip" | head -n1 )
			;;
		* ) echo $( ls | grep "${module}.zip" | head -n1 )
	esac
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
        --checkout )			checkout=1
								;;                                
        --push )                push=1
								echo " * Read push option."
                                ;;
        --no-push )             push=0
								echo " * Read no-push option."
                                ;;
        --pull-before )         pullbefore=1
								echo " * Read pull-before option."
                                ;;
        --no-pull-before )      pullbefore=0
								echo " * Read no-pull-before option."
                                ;;
        --pull-after )          pullafter=1
								echo " * Read pull-after option."
                                ;;
        --no-pull-after )       pullafter=0
								echo " * Read no-pull-after option."
                                ;;
        --commit )              commit=1
								echo " * Read commit option."
                                ;;
        --no-commit )           commit=0
								echo " * Read no-commit option."
                                ;;                                
        --no-exclude-libs )		excludeLibs=0;
        						echo " * Read no-exclude-libs option."
        						;;
        --exclude-libs )		excludeLibs=1;
        						echo " * Read exclude-libs option."
        						;;
        -msg | --commit-message )	shift
			        			commitMessage=$1
								echo " * Read commit message: \"$commitMessage\"."
								;;
		--export-folder )		shift
								moduleExportFolder=$1
								echo " * Read module export folder: \"$moduleExportFolder\"."
								;;
		--export-mode )			shift
								exportMode=$1
								echo " * Read export mode: $exportMode."
								;;
		--reset-remote-head )     resetRemoteHead=1;
								echo " * Read reset-remote-head option."
								;;
		--reset-head )          resetHead=1
								echo " * Read reset-head option."
								;;
		--ignore-unclean )      ignoreUnclean=1
								echo " * Read ignore-unclean option."
								;;
		--no-ignore-unclean )   ignoreUnclean=0
								echo " * Read no-ignore-unclean option."
								;;
		--only-backup-to )	    shift
								backupFolder=$1
								echo " * Read only-backup-to option with folder \"$backupFolder\"."
								;;
		--copy-and-unzip )      copyAndUnzip=1
								echo " * Read copy-and-unzip option."
								;;
		--no-copy-and-unzip )   copyAndUnzip=0
								echo " * Read no-copy-and-unzip option."
								;;
        --git-user-name )		shift
			        			userName=$1
								echo " * Read git-user-name: \"$userName\"."
								;;
        --git-user-email )		shift
			        			userEmail=$1
								echo " * Read git-user-email: \"$userEmail\"."
								;;
		--ignore-default-git-user-name ) ignoreDefaultName=1
								echo " * Read ignore-default-git-user-name option."
								;;
		--ignore-default-git-user-email ) ignoreDefaultEmail=1
								echo " * Read ignore-default-git-user-email option."
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

if [[ -n "$checkout" ]]; then
	cd $REPOSITORY_HOME
	echo "Fetching from remote..."
	git fetch || echo "Failed to fetch from remote."  
	origin="$(git remote)"
	branch="$(git symbolic-ref --short -q HEAD)"
	echo "Checkout action..."
	commonAncestor=$(git merge-base "$branch" "$origin/$branch")
	currentCommit=$(git rev-parse --verify HEAD)
	if [[ $currentCommit = $commonAncestor ]] ; then
		git reset --hard "$origin/$branch" || exit 14
		exit 0
	else
		echo "Error: Branch $branch has diverged from remote branch."
		exit 14
	fi
fi

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

## only backup modules
if [[ ! -z "$backupFolder" ]]; then
	echo
	echo "Only backup modules ..."
	if [[ -d "$backupFolder" ]]; then
		## backup modules
		echo " * Copying modules to \"$backupFolder\" ..."
		for module in $modulesToExport; do
			echo
			echo "   * Handling module ${module} ..."
			echo
			cd $moduleExportFolder
			fileName=$(getExportedModule)
			if [[ ! -z "$fileName" ]]; then
				echo "     * Found zip file ${fileName}."
				#switch to the backup folder
				cd "${backupFolder}"
				echo "     * Copying \"${moduleExportFolder}/${fileName}\" to $(pwd) ..."
				#copy the new module .zip
				cp -f "${moduleExportFolder}/${fileName}" ./
				if [[ $? != 0 ]]; then
					echo
					echo "ERROR: Failed to copy \"${moduleExportFolder}/${fileName}\" to $(pwd)."
					echo "Exit script with exit code 13."
					echo
					exit 13
				fi
			else
				echo "     ! WARN: Skipped module $module because the zip file was not found."
			fi
		done
		echo
		echo "Successfully finished the backup."
		exit 0
	else
		echo "  * FAILED: Folder \"$backupFolder\" does not exist. Exit the script."
		exit 12
	fi
fi

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

## set push flag
if [[ -z "$push" ]]; then
	if [[ -z "$GIT_PUSH" ]]; then
		$push=0
		echo " * Git push mode not specified. Using mode 0, i.e. do not push."
	else
		push=$GIT_PUSH
	fi	
fi
echo " * Set auto-push: $push."

## set pull-before flag
if [[ -z "$pullbefore" ]]; then
	if [[ -z "$GIT_PULL_BEFORE" ]]; then
		pullbefore=0
		echo " * Git pull-before mode not specified. Using mode 0, i.e. do not pull."
	else
		pullbefore=$GIT_PULL_BEFORE
	fi	
fi
echo " * Set pull-before: $pullbefore."

## set pull-after flag
if [[ -z "$pullafter" ]]; then
	if [[ -z "$GIT_PULL_AFTER" ]]; then
		pullafter=0
		echo " * Git pull-after mode not specified. Using mode 0, i.e. do not pull."
	else
		pullafter=$GIT_PULL_AFTER
	fi	
fi
echo " * Set pull-after: $pullafter."

## set commit flag
if [[ -z "$commit" ]]; then
	if [[ -z "$GIT_COMMIT" ]]; then
		commit=0
		echo " * Git commit mode not specified. Using mode 0, i.e. do not commit."
	else
		commit=$GIT_COMMIT
	fi	
fi
echo " * Set auto-commit: $commit."

## set commit message
if [[ -z "$commitMessage" ]]; then
	if [[ -z "$COMMIT_MESSAGE" ]]; then
		commitMessage="Autocommit of exported modules."
	else
		commitMessage="$COMMIT_MESSAGE"
	fi
fi
echo " * Set commit message: \"$commitMessage\"."

## set export libs flag
if [[ -z "$excludeLibs" ]]; then
	if [[ -z "$DEFAULT_EXCLUDE_LIBS" ]]; then
		excludeLibs=0
	else
		excludeLibs=$DEFAULT_EXCLUDE_LIBS
	fi
fi
echo " * Set exclude libs flag: $excludeLibs."

## set git user name
if [[ -z "$userName" ]]; then
	if [[ -z "$GIT_USER_NAME" || $ignoreDefaultName == 1 ]]; then
		echo " * Do not set git user name."
	else
		userName="$GIT_USER_NAME"
		echo " * Set git user name: \"$userName\"."
	fi
else
	echo " * Set git user name: \"$userName\"."
fi

## set git user name
if [[ -z "$userEmail" ]]; then
	if [[ -z "$GIT_USER_EMAIL" || $ignoreDefaultEmail == 1 ]]; then
		echo " * Do not set git user name."
	else
		userEmail="$GIT_USER_EMAIL"
		echo " * Set git user name: \"$userEmail\"."
	fi
else
	echo " * Set git user name: \"$userEmail\"."
fi

# test git repository
echo
echo "Testing Git repository and adjusting user information"
testGitRepository

# reset to HEAD
if [[ $resetHead == 1 ]]; then
	echo
	echo "Performing \"git reset --hard HEAD\" ..."
	cd $REPOSITORY_HOME
	git reset --hard HEAD
	exitCode=$?
	if [[ $exitCode == 0 ]]; then
		echo " * ... success. Your repository is clean now."
		exit 0
	else
		echo " * ... failed with exit code $exitCode."
		exit 11
	fi
fi

# reset to ORIG_HEAD
if [[ $resetRemoteHead == 1 ]]; then
	echo
	cd $REPOSITORY_HOME	
	origin="$(git remote)"
	branch="$(git symbolic-ref --short -q HEAD)"
	echo "Performing \"git reset --hard $origin/$branch\" ..."
	git reset --hard "$origin/$branch"
	exitCode=$?
	if [[ $exitCode == 0 ]]; then
		echo " * ... success."
		exit 0
	else
		echo " * ... failed with exit code $exitCode."
		exit 9
	fi
fi

echo
echo "Testing module main folder of the specified repository ..."
testModuleMainFolder

echo
echo "Performing pull first ..."
## prepare the repository by pulling if wanted
if [[ $pullbefore == 1 ]]; then
	cd $REPOSITORY_HOME
	if [[ ! -z "$GIT_SSH" ]]; then
		echo "  * Pulling with specified ssh keys."
		ssh-agent bash -c "ssh-add $GIT_SSH; git pull -v"
	else
		echo "  * Pulling."
		git pull -v
	fi
	pullExitCode=$?
	if [[ $pullExitCode != 0 ]]; then
		echo "   * ERROR: Pull failed: $pullExitCode."
		exit 1
	fi
else
	echo " * Skip pulling."
fi
echo


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
			echo "   * Found zip file ${fileName}."
			#switch to project's module path
			cd "${MODULE_PATH}"
			#check if a subdirectory for the module exists - if not add it
			if [ ! -d "$module" ]; then
				echo "   * Creating missing module directory \"$module\" under \"$(pwd)\"."
				mkdir $module
			fi
			#go to the modules' subfolder in the project
			cd $module
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
echo "Performing commit ..."
# commit changes
cd $REPOSITORY_HOME
if [[ $commit == 1 ]]; then
	echo " * Check in to GIT repository"
	echo "   * Step 1: adjust the git configuration ...:"
	adjustGitConfig
	echo "   * Step 2: git add -v --all $MODULE_PATH/*"
	git add -v --all $MODULE_PATH/* | awk '$0="   "$0'
	echo "   * Step 3: bash -c \"git commit -v -m \\\"${commitMessage//\"/\\\\\\\"}\\\" \""
	bash -c "git commit -v -m \"${commitMessage//\"/\\\"}\"" | awk '$0="     "$0'
	resetGitConfig
else
	echo " * Auto-commit disabled. Nothing to do."
fi

echo
echo "Performing pull after commit ..."
## prepare the repository by pulling if wanted
if [[ $pullafter == 1 ]]; then
	cd $REPOSITORY_HOME
	if [[ ! -z "$GIT_SSH" ]]; then
		echo "  * Pulling with specified ssh keys."
		ssh-agent bash -c "ssh-add $GIT_SSH; git pull -v"
	else
		echo "  * Pulling."
		git pull -v
	fi
	pullExitCode=$?
	if [[ $pullExitCode != 0 ]]; then
		echo "   * ERROR: Pull failed: $pullExitCode."
		exit 1
	fi
else
	echo " * Skip pulling."
fi
echo


echo 
echo "Pushing changes ..."
# pushing changes
if [[ $push == 1 ]]; then
		if [[ ! -z "$GIT_SSH" ]]; then
		echo
		echo " * Pushing changes using configured SSH keys."
		pushExitCode=$(ssh-agent bash -c "ssh-add $GIT_SSH; git push & echo $?")
	else
		echo
		echo " * Pushing changes."
		git push -v
		pushExitCode=$?
	fi
	if [[ $pushExitCode != 0 ]]; then
		echo "   * WARN: Pushing failed: $pushExitCode."
		exit 2
	fi
else
	echo " * Auto-Push is disabled. Do nothing."
fi

echo
echo "Script completed successfully."
echo "------------------------------"
echo

exit 0
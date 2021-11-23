import os
import sys
import subprocess
import json
import argparse

CHECKSTYLERR_REPO_PATH = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

parser = argparse.ArgumentParser(description='Script to check out one project from Checkstylerr')
parser.add_argument('--branchName', help='The name of the branch corresponding to the project to be checked out', required=True, metavar='')
parser.add_argument('--workspace', help='The path to a folder to store the checked out project', required=True, metavar='')
args = parser.parse_args()

BRANCH_NAME = args.branchName
WORKSPACE = args.workspace

PROJECT_FOLDER_PATH = os.path.join(WORKSPACE, BRANCH_NAME)
if os.path.isdir(PROJECT_FOLDER_PATH):
    print("The project %s has already been checked out." % BRANCH_NAME)
    sys.exit()

print("Checking out the project %s..." % BRANCH_NAME)

# check out the branch containing the project
cmd = "cd %s; git reset .; git checkout -- .; git clean -f; git checkout %s;" % (CHECKSTYLERR_REPO_PATH, BRANCH_NAME)
subprocess.call(cmd, shell=True)

# pull branch
cmd = "cd %s; git pull;" % CHECKSTYLERR_REPO_PATH
subprocess.call(cmd, shell=True)

# create a folder for the workspace
if not os.path.isdir(WORKSPACE):
    cmd = "mkdir %s" % WORKSPACE
    subprocess.call(cmd, shell=True)

# create a folder for the project in the workspace
cmd = "mkdir %s" % PROJECT_FOLDER_PATH
subprocess.call(cmd, shell=True)

# copy all files to the project folder
cmd = "rsync -a --exclude='scripts/' --exclude='.git/' %s/ %s/" % (CHECKSTYLERR_REPO_PATH, PROJECT_FOLDER_PATH)
subprocess.call(cmd, shell=True)

# check out master
cmd = "cd %s; git reset .; git checkout -- .; git clean -f; git checkout master;" % CHECKSTYLERR_REPO_PATH
subprocess.call(cmd, shell=True)

print("The project %s was checked out." % BRANCH_NAME)

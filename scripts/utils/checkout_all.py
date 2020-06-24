import os
import sys
import subprocess
import json
import argparse
import shlex
import re

CHECKSTYLERR_REPO_PATH = os.path.dirname(os.path.abspath(__file__))

parser = argparse.ArgumentParser(description='Script to check out all projects from Checkstylerr')
parser.add_argument('--workspace', help='The path to a folder to store the checked out projects', required=True, metavar='')
args = parser.parse_args()

WORKSPACE = args.workspace

# fetch repository
cmd = "cd %s; git fetch;" % CHECKSTYLERR_REPO_PATH
subprocess.call(cmd, shell=True)

cmd = "git branch -r"
result = subprocess.Popen(shlex.split(cmd), stdout=subprocess.PIPE)
branches = result.communicate()[0].decode("utf-8")
branch_list = branches.split('\n')

if branch_list:
    for branch in branch_list:
        branch = re.sub(r'origin/', '', branch)
        print(branch)
        if not branch == 'master':
            os.system("python %s/checkout_project.py --branchName %s --workspace %s" % (CHECKSTYLERR_REPO_PATH, branch, WORKSPACE))

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

# get branches
cmd = "git branch -r"
result = subprocess.Popen(shlex.split(cmd), stdout=subprocess.PIPE)
branches = result.communicate()[0].decode("utf-8")
branch_list = branches.strip().split('\n')

if branch_list:
    branch_index = 0
    for branch in branch_list:
        branch_index += 1
        branch_name = branch.replace('*', '').replace('origin/', '').strip()
        print('%s/%s branch: %s' % (branch_index, len(branch_list), branch_name))
        if not branch_name == 'master' and not branch_name == 'HEAD -> master':
            os.system("python %s/checkout_project.py --branchName %s --workspace %s" % (CHECKSTYLERR_REPO_PATH, branch_name, WORKSPACE))

#!/usr/bin/python

import configparser
import shutil
import os
import sys
import subprocess
import shlex
import re
import time
import datetime
import hashlib
from natsort import natsorted

import checkstyle
import git_helper
from utils import *

dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.append(os.path.dirname(dir_path))
config = configparser.ConfigParser()
config.read(os.path.join(dir_path, "config.ini"))
__input_projects_path = config['DEFAULT']['input_projects_path']
__workspace_storage_dir = config['DEFAULT']['workspace4storage']

checkstyle_file_names = [
    'checkstyle.xml',
    '.checkstyle.xml',
    'checkstyle_rules.xml',
    'checkstyle-rules.xml',
    'checkstyle_config.xml',
    'checkstyle-config.xml',
    'checkstyle_configuration.xml',
    'checkstyle-configuration.xml',
    'checkstyle_checker.xml',
    'checkstyle-checker.xml',
    'checkstyle_checks.xml',
    'checkstyle-checks.xml',
    'google_checks.xml',
    'google-checks.xml',
    'sun_checks.xml',
    'sun-checks.xml'
]

def get_workspace_storage_dir_for_repo(repo):
    return os.path.join(__workspace_storage_dir, repo)

def get_workspace_storage_dir_for_repo_and_commit(repo, commit):
    return os.path.join(get_workspace_storage_dir_for_repo(repo), commit)

def find_the_pom_file(dir_path): # the main pom should be the closest to the root
    all_pom_files = find_all_files(dir_path, 'pom.xml')
    pom_file_path = safe_get_first(natsorted(all_pom_files, key=lambda x: (str(x.count('/')) + '_' + x)))
    return pom_file_path

def is_there_suppressions_location_in_pom(pom_file_content):
    pom_lines = pom_file_content.split('\n')
    for line in pom_lines:
        if 'suppressionsLocation' in line:
            return True
    return False

def find_the_checkstyle_file(dir_path):
    checkstyle_files = []
    for checkstyle_file_name in checkstyle_file_names:
        files = find_all_files(dir_path, checkstyle_file_name)
        if len(files) > 0:
            for f in files:
                checkstyle_files.append(f)

    checkstyle_file_path = safe_get_first(sorted(checkstyle_files, key=lambda x: x.count('/')))

    if checkstyle_file_path:
        return checkstyle_file_path, '.' + checkstyle_file_path[len(dir_path):]
    else:
        return None, None

def is_checkstyle_ok(checkstyle_file_content):
    checkstyle_file_content = re.sub(r'<!--(.*?)-->', '', checkstyle_file_content, flags=re.DOTALL)
    lines = checkstyle_file_content.split('\n')
    for line in lines:
        if '${' in line:
            return False
    return True

def get_and_check_the_checkstyle_file(dir_path, repo_info):
    checkstyle_absolute_path, checkstyle_relative_path = find_the_checkstyle_file(dir_path)
    if checkstyle_absolute_path:
        my_print(f'Checkstyle configuration file path: {checkstyle_relative_path}')
        repo_info['checkstyle_absolute_path'] = checkstyle_absolute_path
        repo_info['checkstyle_relative_path'] = checkstyle_relative_path

        checkstyle_file_content = open_file(checkstyle_absolute_path)
        if checkstyle_file_content is None:
            return False
        if not is_checkstyle_ok(checkstyle_file_content):
            my_print('Checkstyle.xml contains variables.')
            return False
        else:
            return True
    else:
        my_print('No Checkstyle configuration file has been found.')
        return False

def get_default_branch(repo):
    default_branch = None
    wd = os.getcwd()
    os.chdir(repo.git.rev_parse('--show-toplevel'))
    cmd_default_branch = 'git symbolic-ref --short HEAD'
    p = subprocess.Popen(shlex.split(cmd_default_branch), stdout=subprocess.PIPE)
    default_branch = p.communicate()[0].decode("utf-8").split('\n')[0]
    os.chdir(wd)
    return default_branch

def get_commit_until_last_modification(repo, checkstyle_file_path):
    commits = []
    checkstyle_last_modification_commit = None
    wd = os.getcwd()
    os.chdir(repo.git.rev_parse('--show-toplevel'))
    cmd_commit_list = 'git log --pretty=format:%H'
    p2 = subprocess.Popen(shlex.split(cmd_commit_list), stdout=subprocess.PIPE)
    commit_list_array = p2.communicate()[0].decode("utf-8")
    cmd_commit_checkstyle = f'git log -n 1 --pretty=format:%H -- "{checkstyle_file_path}"'
    p3 = subprocess.Popen(shlex.split(cmd_commit_checkstyle), stdout=subprocess.PIPE)
    checkstyle_last_modification_commit = p3.communicate()[0].decode("utf-8")
    commit_list = commit_list_array.split('\n')
    commits = commit_list[:commit_list.index(checkstyle_last_modification_commit)] + [checkstyle_last_modification_commit]
    os.chdir(wd)
    return commits, checkstyle_last_modification_commit

def get_java_files_changed_in_commit(repo, commit):
    wd = os.getcwd()
    os.chdir(repo.git.rev_parse('--show-toplevel'))
    cmd_changed_file_list = f'git diff --name-status {commit}^ {commit} "*.java"'
    p1 = subprocess.Popen(shlex.split(cmd_changed_file_list), stdout=subprocess.PIPE)
    changed_file_list_array = p1.communicate()[0].decode("utf-8")

    files_to_checkstyle = []
    for changed_file in list(filter(None, changed_file_list_array.split('\n'))):
        file_status_flag = None
        file_path = None
        if changed_file.startswith('R'):
            file_status_flag, file_path = re.split('\s+', changed_file)[0], re.split('\s+', changed_file)[2]
        else:
            file_status_flag, file_path = re.split('\s+', changed_file)
        if not file_status_flag == 'D' and not file_status_flag == 'U':  # D = deleted file, U = unmerged file
            files_to_checkstyle.append(os.getcwd() + '/' + file_path)
    os.chdir(wd)
    if len(files_to_checkstyle) == 0:
        return None
    return ' '.join(files_to_checkstyle)

def test_checkstyle_execution(repo, repo_info, checkstyle_last_modification_commit):
    checkstyle_jar = None
    cmd = "cd %s; git reset --hard HEAD; git clean -xdf;" % repo.working_dir
    subprocess.call(cmd, shell=True)
    my_print(f'Checking out {checkstyle_last_modification_commit}...')
    repo.git.checkout(checkstyle_last_modification_commit)
    time.sleep(1)
    dir = repo.working_dir
    clean_checkstyle_results(dir)
    checkstyle_jar = checkstyle.test_checkstyle_execution_with_different_checkstyle_versions(repo_info['checkstyle_absolute_path_for_execution'], dir)
    return checkstyle_jar

def check_checkstyle_results(checkstyle_results):
    if None in checkstyle_results:
        return None

    reports_with_violations = {}
    for id, results in enumerate(checkstyle_results):
        files_with_violations = {file: result['violations'] for file, result in results.items() if len(result['violations']) and file.endswith('.java')}
        if len(files_with_violations):
            reports_with_violations[id] = files_with_violations
    return reports_with_violations

def clean_checkstyle_results(dir):
    checkstyle_result_xml = find_all_files(dir, 'checkstyle-result.xml')
    for file in checkstyle_result_xml:
        os.remove(file)

def find_files_with_violations(repo, commit, checkstyle_file_path, file_to_checkstyle_path, checkstyle_jar):
    dir = repo.working_dir

    clean_checkstyle_results(dir)

    checkstyle_results = []
    output, returncode = checkstyle.check(
        checkstyle_file_path=checkstyle_file_path, file_to_checkstyle_path=file_to_checkstyle_path, checkstyle_jar=checkstyle_jar)
    checkstyle_results = [output]

    reports_with_violations = check_checkstyle_results(checkstyle_results)

    repo_name = dir.split('/')[-1]
    violation_count = 0
    if reports_with_violations is not None:
        commit_dir = get_workspace_storage_dir_for_repo_and_commit(repo_name, commit)
        for report_dir, results in reports_with_violations.items():
            for file, violations in results.items():
                my_print(f'{file} has {len(violations)} violation(s).')
                commit_and_file_dir = os.path.join(commit_dir, str(violation_count))
                create_dir(commit_and_file_dir)
                file_name = file.split('/')[-1]
                shutil.copyfile(file, os.path.join(commit_and_file_dir, file_name))
                save_json(commit_and_file_dir, 'violations.json', violations)
                violation_count += 1

    my_print(f'# Files with at least one violation: {violation_count}')
    return violation_count

def create_new_branch_and_push_results(repo_to_push_dir, branch_name, repo_info, collection_info):
    save_json(repo_to_push_dir, 'info.json', collection_info)

    cmd = "cd %s; git init;" % repo_to_push_dir
    subprocess.call(cmd, shell=True)

    cmd = "cd %s; git config user.name '%s';" % (repo_to_push_dir, config['DEFAULT']['github_user_name'])
    subprocess.call(cmd, shell=True)

    cmd = "cd %s; git config user.email '%s';" % (repo_to_push_dir, config['DEFAULT']['github_user_email'])
    subprocess.call(cmd, shell=True)

    cmd = "cd %s; git checkout --orphan %s;" % (repo_to_push_dir, branch_name)
    subprocess.call(cmd, shell=True)

    cmd = "cd %s; echo '# %s' > README.md;" % (repo_to_push_dir, repo_info['repo_slug'])
    subprocess.call(cmd, shell=True)

    cmd = "cd %s; git add .;" % repo_to_push_dir
    subprocess.call(cmd, shell=True)

    cmd = "cd %s; git commit -m '[Automatic commit] Checkstyle violations from %s';" % (repo_to_push_dir, repo_slug)
    subprocess.call(cmd, shell=True)

    cmd = "cd %s; git push https://%s:%s@github.com/%s %s;" % (repo_to_push_dir, config['DEFAULT']['github_user_name'], config['DEFAULT']['github_user_token'], config['DEFAULT']['github_repo_slug'], branch_name)
    subprocess.call(cmd, shell=True)

def cleanup(repo_info):
    delete_dir(git_helper.get_repo_dir(repo_info['user'], repo_info['repo_name'])) # workspace_analyses_dir
    delete_dir(get_workspace_storage_dir_for_repo(repo_info['user'] + "-" + repo_info['repo_name'])) # workspace_storage_dir

repo_slugs = []
if len(sys.argv) > 1:
    repo_slugs.append(sys.argv[1])
else:
    with open(__input_projects_path) as temp_file:
        repo_slugs = [line.rstrip('\n') for line in temp_file]

my_print(f'# Repos: {len(repo_slugs)}')
for repo_slug in repo_slugs:
    my_print(f'## Repo {repo_slug} ##')
    
    repo_info = {}
    user, repo_name = repo_slug.split('/')
    repo_info['repo_slug'] = repo_slug
    repo_info['user'] = user
    repo_info['repo_name'] = repo_name

    default_branch = None
    commits = []
    checkstyle_last_modification_commit = None
    checkstyle_jar = None

    project_branch_name = user + "-" + repo_name
    repo_to_push_dir = get_workspace_storage_dir_for_repo(project_branch_name)

    analysis_started_at = datetime.now()

    try:
        repo = git_helper.open_repo(user, repo_name)
        if repo is None:
            my_print(f'[PROCESS FINISHED] Repository not cloned.')
            cleanup(repo_info)
            continue

        default_branch = get_default_branch(repo)

        if not get_and_check_the_checkstyle_file(repo.working_dir, repo_info):
            my_print(f'[PROCESS FINISHED] Repository did not pass the first sanity checks.')
            cleanup(repo_info)
            continue
        my_print(f'Repo {repo_slug} has passed the first sanity checks.')
        
        commits, checkstyle_last_modification_commit = get_commit_until_last_modification(repo, repo_info['checkstyle_relative_path'])
        my_print(f'The last modification in the Checkstyle configuration file was made in the commit {checkstyle_last_modification_commit}.')
        if len(commits) == 0:
            my_print(f'[PROCESS FINISHED] No commit to be analyzed.')
            cleanup(repo_info)
            continue
        if len(commits) == 1:
            my_print(f'There is {len(commits)} commit since the last modification in the Checkstyle configuration file.')
        else:
            my_print(f'There are {len(commits)} commits since the last modification in the Checkstyle configuration file.')

        create_dir(repo_to_push_dir)
        repo_info['checkstyle_absolute_path_for_execution'] = os.path.join(repo_to_push_dir, 'checkstyle.xml')
        shutil.copyfile(repo_info['checkstyle_absolute_path'], repo_info['checkstyle_absolute_path_for_execution'])

        my_print('-----------------------------------------------------')
        my_print('Testing the execution of Checkstyle on the project...')
        checkstyle_jar = test_checkstyle_execution(repo, repo_info, checkstyle_last_modification_commit)
        if checkstyle_jar is None:
            my_print(f'[PROCESS FINISHED] Failure in executing Checkstyle on the project.')
            cleanup(repo_info)
            continue
        checkstyle_jar_simple_name = checkstyle_jar.split('/')[-1]
        my_print(f'The test passed.')
    except Exception as err:
        my_print(f'[Exception] The violation collection of {repo_slug} did not complete. The exception happened before starting to reproduce Checkstyle violations. Detail: {err}')
        my_print(f'[PROCESS FINISHED] Exception before starting to reproduce Checkstyle violations.')
        cleanup(repo_info)
        continue
    
    my_print('--------------------------------------------------------')
    my_print('The reproduction of Checkstyle violations is starting...')
    
    number_of_commits_found = len(commits)
    number_of_commits_analyzed = 0
    number_of_commits_with_violations = 0

    commit_index = 0
    one_commit_analyzed = False
    for commit in commits[::-1]:
        commit_index += 1
        my_print(f'Commit {commit_index}/{number_of_commits_found}: {repo_slug}/{commit}')
        try:
            cmd = "cd %s; git reset --hard HEAD; git clean -xdf;" % repo.working_dir
            subprocess.call(cmd, shell=True)
            my_print(f'Checking out {commit}...')
            repo.git.checkout(commit)
            time.sleep(1)

            if not os.path.exists(repo_info['checkstyle_absolute_path']):
                my_print('The project does not have a checkstyle.xml file in the current commit. Skip it.')
                continue
            
            checkstyle.insert_property_haltOnException_set_to_false_in_checkstyle_file(repo_info['checkstyle_absolute_path'])
            if not hashlib.md5(open(repo_info['checkstyle_absolute_path'], 'rb').read()).hexdigest() == hashlib.md5(open(repo_info['checkstyle_absolute_path_for_execution'], 'rb').read()).hexdigest():
                my_print('The project has a different checkstyle.xml file in the current commit. Skip it.')
                continue

            pom_file_content = open_file(find_the_pom_file(repo.working_dir))
            if pom_file_content is None:
                my_print('The pom.xml cannot be found or read. Skip commit.')
                continue

            if is_there_suppressions_location_in_pom(pom_file_content):
                my_print('The pom.xml file contains \'suppressionsLocation\'. Skip commit.')
                continue

            file_to_checkstyle_path = None
            if not one_commit_analyzed:
                file_to_checkstyle_path = repo.working_dir
            else:
                file_to_checkstyle_path = get_java_files_changed_in_commit(repo, commit)

            if file_to_checkstyle_path is None:
                my_print('No java file to be analyzed. Skip commit.')
                continue

            violation_count = find_files_with_violations(
                repo, commit, repo_info['checkstyle_absolute_path_for_execution'], file_to_checkstyle_path, checkstyle_jar)
            one_commit_analyzed = True
            number_of_commits_analyzed += 1
            if violation_count > 0:
                number_of_commits_with_violations += 1

        except Exception as err:
            my_print(f'[Exception] The violation collection of {repo_slug} failed in commit {commit}. Detail: {err}')
            delete_dir(get_workspace_storage_dir_for_repo_and_commit(project_branch_name, commit))

    analysis_finished_at = datetime.now()

    if os.path.exists(repo_to_push_dir) and len(os.listdir(repo_to_push_dir)) > 1:
        collection_info = {
            'repo_url': f'https://github.com/{repo_slug}',
            'default_branch': default_branch,
            'analysis_duration': str(analysis_finished_at - analysis_started_at),
            'original_checkstyle_path': repo_info['checkstyle_relative_path'],
            'checkstyle_jar': checkstyle_jar.split('/')[-1],
            'checkstyle_last_modification_commit': checkstyle_last_modification_commit,
            'number_of_commits_found': number_of_commits_found,
            'number_of_commits_analyzed': number_of_commits_analyzed,
            'number_of_commits_with_violations': number_of_commits_with_violations,
            'commits': commits
        }
        create_new_branch_and_push_results(repo_to_push_dir, project_branch_name, repo_info, collection_info)
        my_print(f'[PROCESS FINISHED] New branch created.')
    else:
        my_print(f'[PROCESS FINISHED] No violation was found in the project.')

    cleanup(repo_info)

#!/usr/bin/python

import configparser
import shutil
import os
import sys
import subprocess
import shlex
import re
import time

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

def is_checkstyle_ok(checkstyle_file_content):
    checkstyle_file_content = re.sub(r'<!--(.*?)-->', '', checkstyle_file_content, flags=re.DOTALL)
    lines = checkstyle_file_content.split('\n')
    for line in lines:
        if '${' in line:
            return False
    return True

def find_the_pom(path):
    return safe_get_first(sorted(find_all_files(path, 'pom.xml'), key=lambda x: x.count('/'))) # the main pom should be the closest to the root

def is_there_suppressions_location_in_pom(repo):
    pom_lines = open_file(find_the_pom(repo.working_dir)).split('\n')
    for line in pom_lines:
        if 'suppressionsLocation' in line:
            return True
    return False

def find_the_checkstyle_file(repo):
    path = repo.working_dir

    checkstyle_files = []
    for checkstyle_file_name in checkstyle_file_names:
        files = find_all_files(path, checkstyle_file_name)
        if len(files) > 0:
            for f in files:
                checkstyle_files.append(f)

    checkstyle_file_path = safe_get_first(sorted(checkstyle_files, key=lambda x: x.count('/')))

    if checkstyle_file_path:
        return checkstyle_file_path, '.' + checkstyle_file_path[len(path):]
    else:
        return None, None

def get_repo_info(repo_slug, repo):
    repo_info = {}
    user, repo_name = repo_slug.split('/')
    repo_info['repo'] = repo
    repo_info['repo_slug'] = repo_slug
    repo_info['user'] = user
    repo_info['repo_name'] = repo_name

    checkstyle_absolute_path, checkstyle_relative_path = find_the_checkstyle_file(repo)
    if checkstyle_absolute_path:
        my_print(f'Checkstyle configuration file path: {checkstyle_relative_path}')
        repo_info['checkstyle_absolute_path'] = checkstyle_absolute_path
        repo_info['checkstyle_relative_path'] = checkstyle_relative_path

        if is_checkstyle_ok(open_file(checkstyle_absolute_path)):
            repo_info['checkstyle_ok'] = True
        else:
            repo_info['checkstyle_ok'] = False
    else:
        my_print('No Checkstyle configuration file has been found.')

    return repo_info

def get_commit_until_last_modification(repo, file):
    default_branch = None
    commits = []
    checkstyle_last_modification_commit = None
    wd = os.getcwd()
    os.chdir(repo.git.rev_parse('--show-toplevel'))
    cmd_default_branch = 'git symbolic-ref --short HEAD'
    p = subprocess.Popen(shlex.split(cmd_default_branch), stdout=subprocess.PIPE)
    default_branch = p.communicate()[0].decode("utf-8").split('\n')[0]
    cmd_commit_list = 'git log --pretty=format:%H'
    p2 = subprocess.Popen(shlex.split(cmd_commit_list), stdout=subprocess.PIPE)
    commit_list_array = p2.communicate()[0].decode("utf-8")
    cmd_commit_checkstyle = f'git log -n 1 --pretty=format:%H -- "{file}"'
    p3 = subprocess.Popen(shlex.split(cmd_commit_checkstyle), stdout=subprocess.PIPE)
    checkstyle_last_modification_commit = p3.communicate()[0].decode("utf-8")
    commit_list = commit_list_array.split('\n')
    commits = commit_list[:commit_list.index(checkstyle_last_modification_commit)] + [checkstyle_last_modification_commit]
    os.chdir(wd)
    return default_branch, commits, checkstyle_last_modification_commit

def test_checkstyle_execution(repo, repo_info, checkstyle_last_modification_commit):
    checkstyle_jar = None
    cmd = "cd %s; git reset --hard HEAD; git clean -xdf;" % git_helper.get_repo_dir(repo_info['user'], repo_info['repo_name'])
    subprocess.call(cmd, shell=True)
    my_print(f'Checking out {checkstyle_last_modification_commit}...')
    repo.git.checkout(checkstyle_last_modification_commit)
    time.sleep(1)
    dir = repo.working_dir
    clean_checkstyle_results(dir)
    checkstyle_jar = checkstyle.test_checkstyle_execution_with_different_checkstyle_versions(repo_info['checkstyle_absolute_path'], dir)
    return checkstyle_jar

def check_checkstyle_results(checkstyle_results):
    if None in checkstyle_results:
        return None

    reports_with_errors = {}
    for id, results in enumerate(checkstyle_results):
        files_with_errors = {file: result['errors'] for file, result in results.items() if len(result['errors']) and file.endswith('.java')}
        if len(files_with_errors):
            reports_with_errors[id] = files_with_errors
    return reports_with_errors

def clean_checkstyle_results(dir):
    checkstyle_result_xml = find_all_files(dir, 'checkstyle-result.xml')
    for file in checkstyle_result_xml:
        os.remove(file)

def get_changed_files_in_commit(repo, commit):
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

def find_errored_files(repo, commit, checkstyle_file_path, checkstyle_jar, use_maven=False, check_only_changed_files=True):
    dir = repo.working_dir

    clean_checkstyle_results(dir)

    checkstyle_results = []
    if use_maven:
        pom = find_the_pom(dir)
        cmd = f'mvn -f {pom} clean checkstyle:checkstyle'
        process = subprocess.Popen(cmd.split(" "), stdout=subprocess.PIPE)
        output = process.communicate()[0]
        checkstyle_results = [checkstyle.parse_res(open_file(file)) for file in find_all_files(dir, 'checkstyle-result.xml')]
    else:
        files_to_checkstyle_str = None
        if check_only_changed_files:
            files_to_checkstyle_str = get_changed_files_in_commit(repo, commit)
        else:
            files_to_checkstyle_str = dir

        if files_to_checkstyle_str is not None:
            output, returncode = checkstyle.check(
                checkstyle_file_path=checkstyle_file_path, file_to_checkstyle_path=files_to_checkstyle_str, checkstyle_jar=checkstyle_jar)
            checkstyle_results = [output]

    reports_with_errors = check_checkstyle_results(checkstyle_results)

    repo_name = dir.split('/')[-1]
    error_count = 0
    if reports_with_errors is not None:
        commit_dir = get_workspace_storage_dir_for_repo_and_commit(repo_name, commit)
        for report_dir, results in reports_with_errors.items():
            for file, errors in results.items():
                my_print(f'{file} has {len(errors)} error(s).')
                commit_and_file_dir = os.path.join(commit_dir, str(error_count))
                create_dir(commit_and_file_dir)
                file_name = file.split('/')[-1]
                shutil.copyfile(file, os.path.join(commit_and_file_dir, file_name))
                save_json(commit_and_file_dir, 'errors.json', errors)
                error_count += 1

    my_print(f'# Files with at least one error: {error_count}')
    return error_count

def create_new_branch_and_push_results(repo_info, collection_info):
    repo = repo_info['repo']
    branch_name = repo_info['user'] + "-" + repo_info['repo_name']
    repo_to_push_dir = get_workspace_storage_dir_for_repo(branch_name)
    if os.path.exists(repo_to_push_dir) and len(os.listdir(repo_to_push_dir)) > 0:
        shutil.copyfile(os.path.join(repo.working_dir, repo_info['checkstyle_absolute_path']), os.path.join(repo_to_push_dir, 'checkstyle.xml'))

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

        cmd = "cd %s; git commit -m '[Automatic commit] Errors from %s';" % (repo_to_push_dir, repo_slug)
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
    default_branch = None
    commits = []
    checkstyle_last_modification_commit = None
    checkstyle_jar = None
    repo_cloned_at = datetime.now(pytz.utc).strftime("%Y-%m-%d %H:%M:%S %z")
    user, repo_name = repo_slug.split('/')
    try:
        repo = git_helper.open_repo(user, repo_name)
        if repo is None:
            my_print(f'Something wrong happened when cloning the repo {repo_slug}.')
            cleanup(repo_info)
            continue
        
        repo_info = get_repo_info(repo_slug, repo)
        if 'checkstyle_ok' in repo_info and not repo_info['checkstyle_ok']:
            my_print(f'Repo {repo_slug} did not pass in the first sanity checks.')
            cleanup(repo_info)
            continue
        my_print(f'Repo {repo_slug} has passed in the first sanity checks.')
        
        checkstyle_file_path = repo_info['checkstyle_absolute_path']
        default_branch, commits, checkstyle_last_modification_commit = get_commit_until_last_modification(repo, checkstyle_file_path)
        my_print(f'The last modification in the Checkstyle configuration file was made in the commit {checkstyle_last_modification_commit}.')
        if len(commits) == 0:
            my_print(f'There is no commit to be analyzed.')
            cleanup(repo_info)
            continue
        my_print(f'There is/are {len(commits)} commit(s) since the last modification in the Checkstyle configuration file.')

        my_print('')
        my_print('Testing the execution of Checkstyle in the project...')
        checkstyle_jar = test_checkstyle_execution(repo, repo_info, checkstyle_last_modification_commit)
        if checkstyle_jar is None:
            my_print(f'The test failed.')
            cleanup(repo_info)
            continue
        checkstyle_jar_simple_name = checkstyle_jar.split('/')[-1]
        my_print(f'The test passed.')
    except Exception as err:
        my_print(f'[ERROR] The error collection of {repo_slug} did not complete. The error happened before starting to reproduce Checkstyle errors. Detail: {err}')
        cleanup(repo_info)
        continue
    
    my_print('')
    my_print('The reproduction of Checkstyle errors is starting...')
    
    repo = repo_info['repo']
    number_of_commits_found = len(commits)
    number_of_commits_analyzed = 0
    number_of_commits_with_errors = 0

    commit_index = 0
    for commit in commits[::-1]:
        commit_index += 1
        my_print(f'Commit {commit_index}/{number_of_commits_found}: {repo_slug}/{commit}')
        try:
            cmd = "cd %s; git reset --hard HEAD; git clean -xdf;" % git_helper.get_repo_dir(user, repo_name)
            subprocess.call(cmd, shell=True)
            my_print(f'Checking out {commit}...')
            repo.git.checkout(commit)
            time.sleep(1)
            if not is_there_suppressions_location_in_pom(repo):
                check_only_changed_files = None
                if commit == checkstyle_last_modification_commit:
                    check_only_changed_files = False
                else:
                    check_only_changed_files = True
                error_count = find_errored_files(
                    repo, commit, repo_info['checkstyle_absolute_path'], checkstyle_jar, use_maven=False, check_only_changed_files=check_only_changed_files)
                number_of_commits_analyzed += 1
                if error_count > 0:
                    number_of_commits_with_errors += 1
            else:
                my_print('The pom.xml file contains \'suppressionsLocation\'. Skip commit.')

        except Exception as err:
            my_print(f'[ERROR] The error collection of {repo_slug} failed in commit {commit}. Detail: {err}')
            delete_dir(get_workspace_storage_dir_for_repo_and_commit(user + "-" + repo_name, commit))

    analysis_finished_at = datetime.now(pytz.utc).strftime("%Y-%m-%d %H:%M:%S %z")

    collection_info = {
        'repo_url': f'https://github.com/{repo_slug}',
        'repo_cloned_at': repo_cloned_at,
        'analysis_finished_at': analysis_finished_at,
        'default_branch': default_branch,
        'original_checkstyle_path': repo_info['checkstyle_relative_path'],
        'checkstyle_last_modification_commit': checkstyle_last_modification_commit,
        'number_of_commits_found': number_of_commits_found,
        'number_of_commits_analyzed': number_of_commits_analyzed,
        'number_of_commits_with_errors': number_of_commits_with_errors,
        'commits': commits,
        'checkstyle_jar': checkstyle_jar.split('/')[-1]
    }
    create_new_branch_and_push_results(repo_info, collection_info)

    cleanup(repo_info)

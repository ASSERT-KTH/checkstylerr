import git
from git import Repo
from git import InvalidGitRepositoryError
import os
import configparser

from utils import *

dir_path = os.path.dirname(os.path.realpath(__file__))
config = configparser.ConfigParser()
config.read(os.path.join(dir_path, "config.ini"))
__workspace_analyses_dir = config['DEFAULT']['workspace4analyses']

def get_repo_dir(user, repo_name):
    """
    Returns the location of a given repo
    """
    return os.path.join(__workspace_analyses_dir, f'{user}-{repo_name}')

def clone_repo(user, repo_name, https=False):
    """
    Clones the repo into the workspace4analyses/user/repo_name location
    """
    my_print(f'Cloning {user}/{repo_name}...')
    dir = get_repo_dir(user, repo_name)
    if https:
        return (Repo.clone_from(f'https://github.com/{user}/{repo_name}.git', dir), dir)
    else:
        return (Repo.clone_from(f'git@github.com:{user}/{repo_name}.git', dir), dir)

def open_repo(user, repo_name):
    """
    Opens the repo. If the repo does not exist, it clones it
    """
    dir = get_repo_dir(user, repo_name)
    if os.path.exists(dir):
        try:        
            return Repo(dir)
        except InvalidGitRepositoryError:
            my_print(f'Repo {dir} not found.')
            return None
    else:
        repo, repo_dir = clone_repo(user, repo_name, https=True)
        return repo

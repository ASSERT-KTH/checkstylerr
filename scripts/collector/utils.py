import os
from datetime import datetime, timedelta
import pytz
import shutil
import json

def my_print(message):
    print(f'[{datetime.now(pytz.utc).strftime("%Y-%m-%d %H:%M:%S %z")}] {message}', flush=True)

def open_file(file):
    """
    Opens the file and reads its content
    """
    content = ''
    if file:
        with open(file, 'r+', encoding="utf-8") as file:
            content = file.read()
    return content

def create_dir(dir):
    """
    Creates the dir if it does not exist
    """
    if not os.path.exists(dir):
        os.makedirs(dir)
    return dir

def delete_dir(dir):
    """
    Removes the dir if it exists
    """
    if os.path.exists(dir):
        shutil.rmtree(dir)

def save_file_in_path(file_path, content):
    """
    Writes the content in a file
    """
    try:
        with open(file_path, 'w', encoding="utf-8") as f:
            f.write(content)
    except Exception as err:
        print(err)
        return None
    return file_path

def save_json(dir, file_name, content, sort=False):
    """
    Saves a given dict to the specified location as a json file
    """
    with open(os.path.join(dir, file_name), 'w') as f:
        json.dump(content, f, indent=4, sort_keys=sort)

def find_all_files(path, name):
    """
    Finds all the files that have this name
    """
    result = []
    for root, dirs, files in os.walk(path):
        if name in files:
            result.append(os.path.join(root, name))
    return result

def safe_get_first(l):
    """
    Gets the first element. Returns None if len == 0
    """
    if len(l) > 0:
        return l[0]
    else:
        return None

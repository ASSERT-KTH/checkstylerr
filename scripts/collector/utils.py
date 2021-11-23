import os
from datetime import datetime, timedelta
import pytz
import shutil
import json
import GPUtil

def my_print(message):
    print(f'[{datetime.now(pytz.utc).strftime("%Y-%m-%d %H:%M:%S %z")}] {message}', flush=True)

def open_file(file_path):
    """
    Opens the file and reads its content
    """
    content = None
    if file_path:
        try:
            with open(file_path, 'r+', encoding='utf-8') as file:
                content = file.read()
        except Exception as err:
            try:
                with open(file_path, 'r+', encoding='ISO-8859-1') as file:
                    content = file.read()
            except Exception as err2:
                my_print(f'[Exception] The file {file_path} cannot be read.')
                my_print(f'Detail of the first exception: {err}')
                my_print(f'Detail of the second exception: {err2}')
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
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(content)
    except Exception as err:
        try:
            with open(file_path, 'w', encoding='ISO-8859-1') as file:
                file.write(content)
        except Exception as err2:
            my_print(f'[Exception] The file {file_path} cannot be written.')
            my_print(f'Detail of the first exception: {err}')
            my_print(f'Detail of the second exception: {err2}')
            return None
    return file_path

def open_json(file_path):
    """
    Reads a json file and returns its content
    """
    if os.path.exists(file_path):
        with open(file_path) as file:
            data = json.load(file)
            return data
    my_print('File not found: ' + file_path)
    return None

def save_json(dir, file_name, content, sort=False):
    """
    Saves a given dict to the specified location as a json file
    """
    with open(os.path.join(dir, file_name), 'w') as file:
        json.dump(content, file, indent=4, sort_keys=sort)

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

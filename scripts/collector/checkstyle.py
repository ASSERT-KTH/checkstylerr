#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""A basic python wrapper for checkstyle
    """

import os
import xml.etree.ElementTree as ET
import subprocess

from utils import *

dir_path = os.path.dirname(os.path.realpath(__file__))

_CHECKSTYLE_JARS_DIR = os.path.join(dir_path, "checkstyle_jars")
_CHECKSTYLE_JARS = [
    'checkstyle-8.41.1-all.jar',
    'checkstyle-8.41-all.jar',
    'checkstyle-8.40-all.jar',
    'checkstyle-8.39-all.jar',
    'checkstyle-8.38-all.jar',
    'checkstyle-8.37-all.jar',
    'checkstyle-8.36.2-all.jar',
    'checkstyle-8.36.1-all.jar',
    'checkstyle-8.36-all.jar',
    'checkstyle-8.35-all.jar',
    'checkstyle-8.34-all.jar',
    'checkstyle-8.33-all.jar',
    'checkstyle-8.32-all.jar',
    'checkstyle-8.31-all.jar',
    'checkstyle-8.30-all.jar',
    'checkstyle-8.29-all.jar',
    'checkstyle-8.28-all.jar',
    'checkstyle-8.27-all.jar',
    'checkstyle-8.26-all.jar',
    'checkstyle-8.25-all.jar',
    'checkstyle-8.24-all.jar',
    'checkstyle-8.23-all.jar',
    'checkstyle-8.22-all.jar',
    'checkstyle-8.21-all.jar',
    'checkstyle-8.20-all.jar',
    'checkstyle-8.19-all.jar',
    'checkstyle-8.18-all.jar',
    'checkstyle-8.17-all.jar',
    'checkstyle-8.16-all.jar',
    'checkstyle-8.15-all.jar',
    'checkstyle-8.14-all.jar',
    'checkstyle-8.13-all.jar',
    'checkstyle-8.12-all.jar',
    'checkstyle-8.11-all.jar',
    'checkstyle-8.10.1-all.jar',
    'checkstyle-8.10-all.jar',
    'checkstyle-8.9-all.jar',
    'checkstyle-8.8-all.jar',
    'checkstyle-8.7-all.jar',
    'checkstyle-8.6-all.jar',
    'checkstyle-8.5-all.jar',
    'checkstyle-8.4-all.jar',
    'checkstyle-8.3-all.jar',
    'checkstyle-8.2-all.jar',
    'checkstyle-8.1-all.jar',
    'checkstyle-8.0-all.jar'
]
_CHECKSTYLE_JAR = os.path.join(_CHECKSTYLE_JARS_DIR, _CHECKSTYLE_JARS[0])

def test_checkstyle_execution_with_different_checkstyle_versions(checkstyle_file_path, file_to_checkstyle_path):
    found = False
    index = 0
    while (not found and index < len(_CHECKSTYLE_JARS)):
        try:
            my_print(f'Trying {_CHECKSTYLE_JARS[index]}')
            checkstyle_jar = os.path.join(_CHECKSTYLE_JARS_DIR, _CHECKSTYLE_JARS[index])
            check(checkstyle_file_path, file_to_checkstyle_path, checkstyle_jar=checkstyle_jar)
            return checkstyle_jar
        except:
            index += 1
            continue
    return None

def check(checkstyle_file_path, file_to_checkstyle_path, checkstyle_jar=_CHECKSTYLE_JAR):
    """
    Runs Checkstyle on the file_to_checkstyle_path
    """
    cmd = "java -jar {} -f xml -c {} {} --exclude-regexp .*/test/.* --exclude-regexp .*/resources/.*".format(
        checkstyle_jar, checkstyle_file_path, file_to_checkstyle_path)
    process = subprocess.Popen(cmd.split(" "), stdout=subprocess.PIPE)
    output = process.communicate()[0]
    # deletion of non xml strings
    if process.returncode > 0:
        output = b''.join(output.split(b'</checkstyle>')[0:-1]) + b'</checkstyle>'
    # parsing
    output = parse_output(output)
    return output, process.returncode

def parse_output(output):
    """
    Parses the results from XML to a dict
    """
    xml_output = ET.fromstring(output)
    output_parsed = dict()
    for elem_file in xml_output.getchildren():
        if elem_file.attrib['name'].endswith('.java'):
            output_parsed[elem_file.attrib['name']] = dict()
            output_parsed[elem_file.attrib['name']]['errors'] = list()
            for elem_error in elem_file.getchildren():
                if elem_error.tag == 'error':
                    output_parsed[elem_file.attrib['name']]['errors'].append(elem_error.attrib)
    return output_parsed

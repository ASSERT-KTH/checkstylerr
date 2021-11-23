#!/bin/bash

dir=$(pwd)

input_file=$dir/"input_projects.txt"
run_file="run.sh"
logs_folder="logs"

touch $run_file
mkdir $logs_folder

while IFS= read -r repo_line || [ -n "$repo_line" ]; do
    echo "Found repo $repo_line"

    split=(${repo_line//,/ })
    repo_slug=${split[1]}
    repo_slug_for_file_name=${repo_slug/\//-}

    log_file="$logs_folder/$repo_slug_for_file_name.log"

    echo "python $dir/../collect_violations.py $repo_slug > $log_file 2>&1" >> $run_file
done < $input_file

chmod +x $run_file

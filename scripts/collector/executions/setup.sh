#!/bin/bash

dir=$(pwd)

input_file=$dir/"input_projects.txt"

touch "run.sh"
mkdir logs

while IFS= read -r repo_line || [ -n "$repo_line" ]; do
    echo "Found repo $repo_line"

    split=(${repo_line//,/ })
    repo_slug=${split[1]}
    repo_slug_for_file_name=${repo_slug/\//-}

    log_file="logs/$repo_slug_for_file_name.log"

    echo "python $dir/../collect_violations.py $repo_slug > $log_file 2>&1" >> "run.sh"
done < $input_file

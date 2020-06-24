#!/bin/bash

snic=false

source ./env/bin/activate

dir=$(pwd)

input_file=$dir/"input_projects.txt"

mkdir snic_files
mkdir logs

echo "#!/bin/bash" > run.sh
while IFS= read -r repo_slug || [ -n "$repo_slug" ]; do
    echo "Found repo $repo_slug"

    repo_slug_for_file_name=${repo_slug/\//-}
    snic_file="snic_files/$repo_slug_for_file_name.sh"
    log_file="logs/$repo_slug_for_file_name.log"

    touch $snic_file

    echo "#!/bin/bash" > $snic_file
    
    if $snic; then
        echo "#SBATCH -A SNIC2019-3-453" >> $snic_file
        echo "#SBATCH -J $repo_slug_for_file_name" >> $snic_file
        echo "#SBATCH -c 3" >> $snic_file
        echo "#SBATCH --time=24:00:00" >> $snic_file
        echo "#SBATCH --output $log_file" >> $snic_file
        echo "#SBATCH --error $log_file" >> $snic_file
        echo "srun python $dir/../collect_errors.py $repo_slug" >> $snic_file
        echo "sbatch $snic_file" >> "run.sh"
    else
        echo "python $dir/../collect_errors.py $repo_slug |& tee $log_file" >> $snic_file
        echo "./$snic_file" >> "run.sh"
    fi
done < $input_file

chmod +x run.sh
chmod +x snic_files/*.sh

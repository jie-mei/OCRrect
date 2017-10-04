#!/bin/sh

SUGGEST_DATA_PATH='../../../../tmp/suggest/data'
TRAIN_PARTS=13

# Merge the training data
#for i in 1 3 5 10 100; do
for i in 1; do
  merged_dir="${SUGGEST_DATA_PATH}/suggest.train.top${i}"
  echo "Start merging ${merged_dir}"
  mkdir -p ${merged_dir}
  for j in $(seq -f "%02g" 1 "${TRAIN_PARTS}"); do
    in_folder="${SUGGEST_DATA_PATH}/suggest.train.top${i}.part${j}"
    counter=0
    for in_path in "${in_folder}"/*; do
      orig_file="${in_path#${in_folder}/}"
      out_path="${merged_dir}/suggest.${j}${orig_file:9:99}"
      #echo "in_path: ${in_path}"
      #echo "orig:    ${orig_file}"
      #echo "inpath:  ${in_path}"
      #echo "outputh: ${out_path}"
      cp "${in_path}" "${out_path}"
      (( counter++ ))
    done
    echo "            < ${in_folder} ${counter}"
  done
done

PATH='a/b/c.001.txt'
PREFIX='a/b/'
RESULT=${PATH#${PREFIX}}
RESULT=${RESULT:2:99}

echo $RESULT


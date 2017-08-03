#!/bin/sh
#
# Arguments:
#   1: A text string to be tokenized.

TEMP_PATH='tmp'
REPO_URL='https://github.com/jmei91/elephant'

# Check arguments.
if [ "$#" -ne 1 ]; then
  echo "Usage: ${0} TEXT"
  exit 1
fi

txt=`cat "${1}"`

echo "${txt}"

# # Download elephant source code if not exist.
# if [ ! -e "${TEMP_PATH}/elephant" ]; then
#   git clone "${REPO_URL}" "${TEMP_PATH}/elephant"
# fi
# 
# # Perform tokenization using elephant.
# #tokens=`cd "${TEMP_PATH}/elephant" && echo "${1}" | python2 elephant -m models/english`
# tokens=`cd "${TEMP_PATH}/elephant" && echo "${txt}" | python2 elephant -m models/english`
# 
# # Print tokens in separated lines.
# for tk in ${tokens}; do
#   echo ${tk}
# done

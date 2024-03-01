#!/usr/bin/env bash

# Constants
CONFIG_FILE="config.json"
IMPORT_ERROR="# ERROR: Grading Failed
**We were unable to locate one or more files.** Please make sure of the following:
1. You have uploaded all required *.java* files
2. The first line of each file begins with *\"package\"*

If you're still having trouble, please contact an instructor or TA."

shopt -s globstar &> /dev/null
# Allows us to compile code while ignoring errors (very important)
ecj -cp lib/hamcrest-core-1.3.jar:lib/junit-4.13.2.jar:lib/json-20231013.jar -d bin/ -Xlint -nowarn -1.9 -proceedOnError src/ &> java.out
java -cp bin/:lib/* AutoGrader.GradescopeAutoGrader $CONFIG_FILE &> java.stdout
json_pp < results.json > /autograder/results/results.json

# TestRunner.java will throw results to this file. Cleans the output for Gradescope
cat results.out

# If a file is missing, alert the student (false positives are possible)
if grep -q ".*The import [^ ]* cannot be resolved.*" java.out
then
    echo "One or more files are missing. See java.out for details."
    jq --arg msg "$IMPORT_ERROR" '.output = $msg' /autograder/results/results.json > temp && mv temp /autograder/results/results.json
fi

# Upload compiler and runtime outputs, if files aren't empty
OUTPUT_TEXT=""
if [ -s java.out ]
then
    UPLOAD=$(cat java.out | curl -s -X POST https://bpa.st/curl -F 'raw=<-')
    COMPILER_OUTPUT=$(echo "$UPLOAD" | sed -n 2p | grep -Eo 'https:.*')
    COMPILER_REMOVE=$(echo "$UPLOAD" | sed -n 3p | grep -Eo 'https:.*')
    OUTPUT_TEXT+="Compiler output: [$COMPILER_OUTPUT]($COMPILER_OUTPUT) ([remove]($COMPILER_REMOVE))\\n"
fi
if [ -s java.stdout ]
then
    UPLOAD=$(cat java.stdout | curl -s -X POST https://bpa.st/curl -F 'raw=<-')
    RUNTIME_OUTPUT=$(echo "$UPLOAD" | sed -n 2p | grep -Eo 'https:.*')
    RUNTIME_REMOVE=$(echo "$UPLOAD" | sed -n 3p | grep -Eo 'https:.*')
    OUTPUT_TEXT+="Runtime output: [$RUNTIME_OUTPUT]($RUNTIME_OUTPUT) ([remove]($RUNTIME_REMOVE))"
fi

# Add links to the results.json file, if they exist
if [ -n "$OUTPUT_TEXT" ]
then
    OUTPUT_JSON="{
          \"status\": \"passed\",
          \"name\": \"Console Output (expires after 24 hours)\",
          \"output\": \"$OUTPUT_TEXT\",
          \"output_format\": \"md\",
          \"visibility\": \"hidden\"
        }"
    jq --argjson new_test "$OUTPUT_JSON" '.tests += [$new_test]' /autograder/results/results.json > temp && mv temp /autograder/results/results.json
fi

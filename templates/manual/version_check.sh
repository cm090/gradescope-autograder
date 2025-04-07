#!/usr/bin/env bash

VERSION_CHECK_FILE="/autograder/source/version.json"
VERSION=$(jq -r '.version' "$VERSION_CHECK_FILE" | tr -cd '0-9')
UPDATE_URL=$(jq -r '.update_url' "$VERSION_CHECK_FILE")
SOURCE=$(jq -r '.source' "$VERSION_CHECK_FILE")
REMOTE_VERSION=$(curl -s "$UPDATE_URL" | jq -r '.version')
REMOTE_VERSION_NUM=$(echo "$REMOTE_VERSION" | tr -cd '0-9')

if [ "$REMOTE_VERSION_NUM" -gt "$VERSION" ]; then
    echo "----------------------------------------------------------------------"
    echo "A new version of this autograder template is available: v$REMOTE_VERSION"
    echo "You can download it from the following URL:"
    echo "$SOURCE"
    echo "----------------------------------------------------------------------"
fi
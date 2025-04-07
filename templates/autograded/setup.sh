#!/usr/bin/env bash

# This is what Gradescope runs first
# Update system packages and install requirements
apt update
apt install jq jo ecj java-wrappers openjdk-21-jdk xvfb -y

# Check for a newer version of the template
chmod +x /autograder/source/version_check.sh &>/dev/null
dos2unix /autograder/source/version_check.sh &>/dev/null
bash /autograder/source/version_check.sh

echo "Gradescope Autograder: Autograded Template
Copyright (C) 2025 Canon Maranda <https://about.canon.click>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>."

# This is what Gradescope runs first. Start by updating the system and installing required packages
apt update &> /dev/null
apt install ecj java-wrappers jq -y &> /dev/null
# Gradescope Autograder
### Template for Rose-Hulman CSSE220

## File structure
- run.sh
  - Starts the JUnit tests, moves `results.json` to the correct directory
  - Requires providing the list of test packages
- run_autograder
  - Parses student uploads
- setup.sh
  - Required for upload, no other use
- results_template
  - Displays a message to the student if the autograder fails to run
- lib/
  - Holds `.jar` files to process code
- src/
  - Location of all uploaded Java files
  - AutoGrader/
    - AutoGrader.java
      - Stores JUnit results into `results.json`
    - TestRunner.java
      - Runs all unit test files

## Prerequisites
- Assignment JUnit tests can be graded without intervention
- Students are not submitting graphics work and are not creating their own JUnit tests
- The first line of every Java file declares a package

## Getting started
1. Download this repository
2. Use the [file tool](../file-tool/Csse220FileTool.jar) to automatically generate the autograder zip file. Use the advanced process below if you need to make additional changes.
3. Open `run.sh` and update line 3. This requires listing the packages that contain the unit tests.
4. Read the comment in the main method of `src/AutoGrader/AutoGrader.java`.
5. Import Java test files to the `src/` directory. Do not upload starter code or anything else a student should modify.
6. Zip the contents (everything inside the downloaded folder) and upload to the Gradescope assignment.

## Questions
Check out the [resources folder](../resources)

[Example zip files](https://rosehulman-my.sharepoint.com/:f:/g/personal/marandcp_rose-hulman_edu/EgpfrnhiyzJBr26-3P3l8SUBEiQPOKfskVu1R7ZGRhlObQ?e=wIfnKL)

If you need help using this program, contact me [(Canon Maranda)](https://link.canon.click/from/github).

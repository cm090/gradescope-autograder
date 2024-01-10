# Gradescope Autograder
### Template for Rose-Hulman CSSE220

## File structure
- config.json
  - Provides the autograder with the following information:
    - `classes`: List of test packages and score weights (-1 for no weight)
    - `additional_options.test_visibility`: Whether to show the test results to students (hidden, after_due_date, after_published, visible)
    - `additional_options.timeout_seconds`: How long to run a test before timing out
    - `additional_options.extra_credit_tests`: Number of extra credit tests (works best when all package weights are -1)
- results_error_template
  - Displays a message to the student if the autograder fails to run
- run_autograder
  - Parses student uploads
- run.sh
  - Starts the JUnit tests, moves `results.json` to the correct directory
  - Requires providing the list of test packages
- setup.sh
  - Required for upload, no other use
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
3. Open `config.json` and update the contents.
4. Import Java test files to the `src/` directory. Do not upload starter code or anything else a student should modify.
5. Zip the contents (everything inside the downloaded folder) and upload to the Gradescope assignment.

## Questions
Check out the [resources folder](../resources)

[Example zip files](https://rosehulman-my.sharepoint.com/:f:/g/personal/marandcp_rose-hulman_edu/EgpfrnhiyzJBr26-3P3l8SUBEiQPOKfskVu1R7ZGRhlObQ?e=wIfnKL)

If you need help using this program, contact me [(Canon Maranda)](https://link.canon.click/from/github).

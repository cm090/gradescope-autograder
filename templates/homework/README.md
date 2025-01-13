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
    - Main.java
      - Takes in configuration and metadata files to generate test results
    - ClassFinder.java
      - Determines classes based on package name
    - Configuration.java
      - Singleton class with input data
    - OutputMessage.java
      - Possible success/error messages based on results
    - Results.java
      - Singleton class with test run data
    - Runner.java
      - Prepares test files for processing
    - TestData.java
      - Data corresponding to each test file
    - TestRunner.java
      - Wraps test files to process successes/failures
    - Visibility.java
      - Parameters corresponding to when results are shown on Gradescope

## Prerequisites
- Assignment JUnit tests can be graded without intervention
- Students are not submitting graphics work and are not creating their own JUnit tests
- The first line of every Java file declares a package

## Getting started
1. Download this repository
2. Use the [file tool](../../file-tool/Csse220FileTool.jar) to automatically generate the autograder zip file. Use the advanced process below if you need to make additional changes.
3. Open `config.json` and update the contents.
4. Import Java test files to the `src/` directory. Do not upload starter code or anything else a student should modify.
5. Zip the contents (everything inside the downloaded folder) and upload to the Gradescope assignment.

## Questions
Check out the [resources folder](../../resources/)

[Example zip files](https://rosehulman-my.sharepoint.com/:f:/g/personal/marandcp_rose-hulman_edu/EgpfrnhiyzJBr26-3P3l8SUBEiQPOKfskVu1R7ZGRhlObQ?e=wIfnKL)

If you need help using this program, contact me [(Canon Maranda)](https://link.canon.click/from/github).

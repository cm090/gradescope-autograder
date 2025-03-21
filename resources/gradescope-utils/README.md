# Gradescope Utilities

A set of browser tools to enhance instructor and grader experiences

**Note:** This resource is not maintained through the `gradescope-autograder` project

## List of tools

- Hide old instructor courses
  - Use the `s` key on the homepage to toggle visibility of instructor courses from previous terms
- Force enable SSH option
  - Allows for clicking 'Debug via SSH' when Gradescope thinks a session is still running
- Incomplete grading progress bar
  - Sets progress bar color to red if not at 100%
- Show autograder scores
  - Use the `` ` `` (backtick) key to show scores next to methods while grading programming assignments

## Setup

1. Install the [Injector extension](https://chromewebstore.google.com/detail/injector/bfdonckegflhbiamlmidciapolfccmmb) for Chrome
   - Or use a different JS injection plugin for other browsers
2. Open the extension settings and click the plus (`+`) button
3. Set the domain to `gradescope.com` and click create
4. Make sure the dropdown at the top of the page says `JavaScript`
5. Copy the text from [`script.js`](./script.js) to the textbox and click save
   - Edit the `settings` object to enable or disable specific features
6. Open Gradescope and start using the utilities!
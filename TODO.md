# Things to fix

The app runs, but six things are wrong with it. Run `pytest` and you will see
7 failing tests. Your job is to get them all passing by fixing the app, not by
changing the tests.

They are in order, easiest first. For each one: read the failing test, see the
problem for yourself in the browser, find the cause, fix it, run `pytest`
again, and commit. Then write down your answer to the question, because that
is what an interviewer will ask.

## 1. The server accepts bad input

The form in the browser only lets you pick a severity from 1 to 5 and will not
send an empty description. The server does no such checking, so a request
sent without the browser can save a severity of 9 or a blank description.

- Test: `test_bad_input_is_rejected`
- Question: why is checking in the browser not enough?

## 2. The list is in the wrong order

Log in as sam and look at the list. It is meant to show the newest incident
first. It does not.

- Test: `test_newest_incident_is_listed_first`
- After your fix, run `flask --app app seed` again. Work out why you need to.
- Question: what was the list being sorted by, and why did that go wrong?

## 3. Edits leave no history

Edit an incident and change its severity. The History section on its page,
and the supervisor's audit page, should show the old and new value. Nothing
appears.

- Test: `test_an_edit_is_logged_with_before_and_after`
- Question: what was the code comparing, and what should it have compared?

## 4. A description can run code in your browser

Add an incident with this as the description, then open it:

```
<script>alert("hello")</script>
```

A box pops up. Anyone who can log an incident can make code run in the
browser of whoever reads it, including a supervisor.

- Test: `test_description_is_shown_as_text_not_html`
- Keep line breaks in descriptions working after your fix.
- Question: what is this attack called, and what does Flask normally do to
  stop it?

## 5. Staff can read each other's reports

Log in as priya. Her list only shows her own incidents, which is right. Now
go to `/incidents/1` in the address bar. That one is alex's, and she can read
it and edit it.

- Test: `test_staff_cannot_open_someone_elses_incident`
- Supervisors must still be able to open everything.
- Question: the list was already filtered, so why was that not enough?

## 6. Search breaks on an apostrophe

Search for `couldn't`. The page crashes. That looks like a small bug, but it
is a sign of something much worse: as priya, search for this and look at
whose incidents come back.

```
%') OR 1=1 --
```

- Tests: `test_search_copes_with_an_apostrophe` and
  `test_search_cannot_show_other_peoples_incidents`
- Question: what is this attack called, and why does your fix stop it when
  just removing apostrophes would not?

## Then build something yourself

Once everything passes, pick one and write it with no AI writing the code:

- Show 10 incidents per page, with next and previous links
- Let supervisors download all incidents as a CSV file
- Lock an account for 15 minutes after 5 wrong passwords
- Add CSRF tokens to every form

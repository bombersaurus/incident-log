# Things to fix

The app runs, but six things are wrong with it. Run `mvn test` and you will
see 8 tests fail. Your job is to get them all passing by fixing the app, not
by changing the tests.

They are in order, easiest first. For each one: read the failing test, see the
problem for yourself in the browser, find the cause, fix it, run `mvn test`
again, and commit. Then write down your answer to the question, because that
is what an interviewer will ask.

## 1. The server accepts bad input

The form in the browser only lets you pick a severity from 1 to 5 and will not
send an empty description. The server does not check either, so a request
sent without the browser can save a severity of 9 or a blank description.

- Test: `badInputIsRejected`
- Question: why is checking in the browser not enough?

## 2. The supervisor is treated like ordinary staff

Log in as sam. He is a supervisor, so he should see every incident and have
an "Audit log" link. He sees neither, and `/audit` refuses him.

- Tests: `supervisorSeesEverything` and `auditPageIsForSupervisorsOnly`
- Question: what is the difference between the two ways of comparing text in
  Java, and why did the wrong one compile without complaint?

## 3. Edits leave no history

Edit an incident and change its severity. The History section on its page
should show the old and new value. Nothing appears.

- Test: `anEditIsLoggedWithBeforeAndAfter`
- Question: what was the code comparing, and what should it have compared?

## 4. A description can run code in your browser

Add an incident with this as the description, then open it:

```
<script>alert("hello")</script>
```

A box pops up. Anyone who can log an incident can make code run in the
browser of whoever reads it, including a supervisor.

- Test: `descriptionIsShownAsTextNotHtml`
- Keep line breaks in descriptions working after your fix. CSS can do that.
- Question: what is this attack called, and what does Thymeleaf normally do
  to stop it?

## 5. Staff can read each other's reports

Log in as priya. Her list only shows her own incidents, which is right. Now
open one of alex's by typing its address, for example `/incidents/1`. She can
read it and edit it.

- Test: `staffCannotOpenSomeoneElsesIncident`
- Supervisors must still be able to open everything.
- Question: the list was already filtered, so why was that not enough?

## 6. Search breaks on an apostrophe

Search for `couldn't`. The page crashes. That looks like a small bug, but it
is a sign of something much worse: as priya, search for this and look at
whose incidents come back.

```
%') OR 1=1 --
```

- Tests: `searchCopesWithAnApostrophe` and
  `searchCannotShowOtherPeoplesIncidents`
- Question: what is this attack called, and why does your fix stop it when
  just removing apostrophes would not?

## Then build something yourself

Once everything passes, pick one and write it with no AI writing the code:

- Show 10 incidents per page, with next and previous links
- Let supervisors download all incidents as a CSV file
- Lock an account for 15 minutes after 5 wrong passwords
- Save an edit and its audit rows in one transaction
- Replace the hand-written login with Spring Security

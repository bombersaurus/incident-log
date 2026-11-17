"""Date and time helpers. Times are kept as text in three shapes."""
from datetime import datetime

DB_TIME_FORMAT = "%d/%m/%Y %H:%M"       # how they are stored in the database
FORM_TIME_FORMAT = "%Y-%m-%dT%H:%M"     # what <input type="datetime-local"> sends
DISPLAY_TIME_FORMAT = "%d/%m/%Y %H:%M"  # what people read on the page


def format_for_db(moment):
    return moment.strftime(DB_TIME_FORMAT)


def parse_from_db(text):
    return datetime.strptime(text, DB_TIME_FORMAT)


def parse_from_form(text):
    """Raises ValueError if the text is not a datetime-local value."""
    return datetime.strptime(text, FORM_TIME_FORMAT)


def format_for_form(moment):
    return moment.strftime(FORM_TIME_FORMAT)


def display_time(text):
    """Template filter: database time to UK-style date and time."""
    return parse_from_db(text).strftime(DISPLAY_TIME_FORMAT)

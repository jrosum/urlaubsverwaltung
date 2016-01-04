package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.*;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
@Component
public class SettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_INVALID_EMAIL = "error.entry.mail";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";

    private static final int DAYS_PER_YEAR = 366;
    private static final int HOURS_PER_DAY = 24;
    private static final int MAX_CHARS = 255;

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(Settings.class);
    }


    @Override
    public void validate(Object o, Errors errors) {

        Assert.isTrue(supports(o.getClass()), "The given object must be an instance of Settings");

        Settings settings = (Settings) o;

        validatePublicHolidaysSettings(settings, errors);

        validateOvertimeSettings(settings, errors);

        validateVacationSettings(settings, errors);

        validateSickNoteSettings(settings, errors);

        validateMailSettings(settings, errors);

        validateCalendarSettings(settings, errors);
    }


    private void validatePublicHolidaysSettings(Settings settings, Errors errors) {

        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();

        if (workingTimeSettings.getFederalState() == null) {
            errors.rejectValue("workingTimeSettings.federalState", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForChristmasEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForChristmasEve", ERROR_MANDATORY_FIELD);
        }

        if (workingTimeSettings.getWorkingDurationForNewYearsEve() == null) {
            errors.rejectValue("workingTimeSettings.workingDurationForNewYearsEve", ERROR_MANDATORY_FIELD);
        }
    }


    private void validateOvertimeSettings(Settings settings, Errors errors) {

        WorkingTimeSettings workingTimeSettings = settings.getWorkingTimeSettings();

        Integer maximumOvertime = workingTimeSettings.getMaximumOvertime();

        if (maximumOvertime == null) {
            errors.rejectValue("workingTimeSettings.maximumOvertime", ERROR_MANDATORY_FIELD);

            return;
        }

        if (maximumOvertime < 0) {
            errors.rejectValue("workingTimeSettings.maximumOvertime", ERROR_INVALID_ENTRY);
        }
    }


    private void validateVacationSettings(Settings settings, Errors errors) {

        AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        Integer maximumAnnualVacationDays = absenceSettings.getMaximumAnnualVacationDays();

        if (maximumAnnualVacationDays == null) {
            errors.rejectValue("absenceSettings.maximumAnnualVacationDays", ERROR_MANDATORY_FIELD);
        } else if (maximumAnnualVacationDays < 0 || maximumAnnualVacationDays > DAYS_PER_YEAR) {
            errors.rejectValue("absenceSettings.maximumAnnualVacationDays", ERROR_INVALID_ENTRY);
        }

        Integer maximumMonthsToApplyForLeaveInAdvance = absenceSettings.getMaximumMonthsToApplyForLeaveInAdvance();

        if (maximumMonthsToApplyForLeaveInAdvance == null) {
            errors.rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_MANDATORY_FIELD);
        } else if (maximumMonthsToApplyForLeaveInAdvance <= 0) {
            errors.rejectValue("absenceSettings.maximumMonthsToApplyForLeaveInAdvance", ERROR_INVALID_ENTRY);
        }
    }


    private void validateSickNoteSettings(Settings settings, Errors errors) {

        AbsenceSettings absenceSettings = settings.getAbsenceSettings();

        Integer maximumSickPayDays = absenceSettings.getMaximumSickPayDays();
        Integer daysBeforeEndOfSickPayNotification = absenceSettings.getDaysBeforeEndOfSickPayNotification();

        if (maximumSickPayDays == null) {
            errors.rejectValue("absenceSettings.maximumSickPayDays", ERROR_MANDATORY_FIELD);
        } else if (maximumSickPayDays < 0) {
            errors.rejectValue("absenceSettings.maximumSickPayDays", ERROR_INVALID_ENTRY);
        }

        if (daysBeforeEndOfSickPayNotification == null) {
            errors.rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification", ERROR_MANDATORY_FIELD);
        } else if (daysBeforeEndOfSickPayNotification < 0) {
            errors.rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification", ERROR_INVALID_ENTRY);
        }

        if (maximumSickPayDays != null && daysBeforeEndOfSickPayNotification != null
                && daysBeforeEndOfSickPayNotification > maximumSickPayDays) {
            errors.rejectValue("absenceSettings.daysBeforeEndOfSickPayNotification",
                "settings.sickDays.daysBeforeEndOfSickPayNotification.error");
        }
    }


    private void validateMailSettings(Settings settings, Errors errors) {

        MailSettings mailSettings = settings.getMailSettings();

        validateMailHost(mailSettings, errors);

        validateMailPort(mailSettings, errors);

        validateMailUsername(mailSettings, errors);

        validateMailPassword(mailSettings, errors);

        validateMailFrom(mailSettings, errors);

        validateMailAdministrator(mailSettings, errors);
    }


    private void validateMailHost(MailSettings mailSettings, Errors errors) {

        String hostAttribute = "mailSettings.host";
        String host = mailSettings.getHost();

        if (!StringUtils.hasText(host)) {
            if (mailSettings.isActive()) {
                errors.rejectValue(hostAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(host)) {
                errors.rejectValue(hostAttribute, ERROR_LENGTH);
            }
        }
    }


    private boolean validStringLength(String string) {

        return string.length() <= MAX_CHARS;
    }


    private void validateMailPort(MailSettings mailSettings, Errors errors) {

        String portAttribute = "mailSettings.port";
        Integer port = mailSettings.getPort();

        if (port == null) {
            if (mailSettings.isActive()) {
                errors.rejectValue(portAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (port <= 0) {
                errors.rejectValue(portAttribute, ERROR_INVALID_ENTRY);
            }
        }
    }


    private void validateMailUsername(MailSettings mailSettings, Errors errors) {

        String username = mailSettings.getUsername();

        if (username != null && !validStringLength(username)) {
            errors.rejectValue("mailSettings.username", ERROR_LENGTH);
        }
    }


    private void validateMailPassword(MailSettings mailSettings, Errors errors) {

        String password = mailSettings.getPassword();

        if (password != null && !validStringLength(password)) {
            errors.rejectValue("mailSettings.password", ERROR_LENGTH);
        }
    }


    private void validateMailFrom(MailSettings mailSettings, Errors errors) {

        String fromAttribute = "mailSettings.from";
        String from = mailSettings.getFrom();

        if (!StringUtils.hasText(from)) {
            if (mailSettings.isActive()) {
                errors.rejectValue(fromAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(from)) {
                errors.rejectValue(fromAttribute, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(from)) {
                errors.rejectValue(fromAttribute, ERROR_INVALID_EMAIL);
            }
        }
    }


    private void validateMailAdministrator(MailSettings mailSettings, Errors errors) {

        String administratorAttribute = "mailSettings.administrator";
        String administrator = mailSettings.getAdministrator();

        if (!StringUtils.hasText(administrator)) {
            if (mailSettings.isActive()) {
                errors.rejectValue(administratorAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(administrator)) {
                errors.rejectValue(administratorAttribute, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(administrator)) {
                errors.rejectValue(administratorAttribute, ERROR_INVALID_EMAIL);
            }
        }
    }


    private void validateCalendarSettings(Settings settings, Errors errors) {

        CalendarSettings calendarSettings = settings.getCalendarSettings();

        validateCalendarSettings(calendarSettings, errors);

        validateExchangeCalendarSettings(calendarSettings.getExchangeCalendarSettings(), errors);
    }


    private void validateCalendarSettings(CalendarSettings calendarSettings, Errors errors) {

        String workDayBeginHourAttribute = "calendarSettings.workDayBeginHour";
        Integer workDayBeginHour = calendarSettings.getWorkDayBeginHour();
        validateWorkDayHour(workDayBeginHour, workDayBeginHourAttribute, errors);

        Integer workDayEndHour = calendarSettings.getWorkDayEndHour();
        String workDayEndHourAttribute = "calendarSettings.workDayEndHour";
        validateWorkDayHour(workDayEndHour, workDayEndHourAttribute, errors);

        boolean beginHourValid = workDayBeginHour != null && isValidWorkDayHour(workDayBeginHour);
        boolean endHourValid = workDayEndHour != null && isValidWorkDayHour(workDayEndHour);

        if (beginHourValid && endHourValid
                && (workDayBeginHour.equals(workDayEndHour) || workDayBeginHour > workDayEndHour)) {
            errors.rejectValue(workDayBeginHourAttribute, ERROR_INVALID_ENTRY);
            errors.rejectValue(workDayEndHourAttribute, ERROR_INVALID_ENTRY);
        }
    }


    private boolean isValidWorkDayHour(int workDayHour) {

        return workDayHour > 0 && workDayHour <= HOURS_PER_DAY;
    }


    private void validateWorkDayHour(Integer workDayHour, String attribute, Errors errors) {

        if (workDayHour == null) {
            errors.rejectValue(attribute, ERROR_MANDATORY_FIELD);
        } else {
            if (!isValidWorkDayHour(workDayHour)) {
                errors.rejectValue(attribute, ERROR_INVALID_ENTRY);
            }
        }
    }


    private void validateExchangeCalendarSettings(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        validateExchangeEmail(exchangeCalendarSettings, errors);
        validateExchangePassword(exchangeCalendarSettings, errors);
        validateExchangeCalendarName(exchangeCalendarSettings, errors);
    }


    private void validateExchangeEmail(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String emailAttribute = "calendarSettings.exchangeCalendarSettings.email";
        String email = exchangeCalendarSettings.getEmail();

        if (!StringUtils.hasText(email)) {
            if (exchangeCalendarSettings.isActive()) {
                errors.rejectValue(emailAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(email)) {
                errors.rejectValue(emailAttribute, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(email)) {
                errors.rejectValue(emailAttribute, ERROR_INVALID_EMAIL);
            }
        }
    }


    private void validateExchangePassword(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String passwordAttribute = "calendarSettings.exchangeCalendarSettings.password";
        String password = exchangeCalendarSettings.getPassword();

        if (!StringUtils.hasText(password)) {
            if (exchangeCalendarSettings.isActive()) {
                errors.rejectValue(passwordAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(password)) {
                errors.rejectValue(passwordAttribute, ERROR_LENGTH);
            }
        }
    }


    private void validateExchangeCalendarName(ExchangeCalendarSettings exchangeCalendarSettings, Errors errors) {

        String calendarAttribute = "calendarSettings.exchangeCalendarSettings.calendar";
        String calendar = exchangeCalendarSettings.getPassword();

        if (!StringUtils.hasText(calendar)) {
            if (exchangeCalendarSettings.isActive()) {
                errors.rejectValue(calendarAttribute, ERROR_MANDATORY_FIELD);
            }
        } else {
            if (!validStringLength(calendar)) {
                errors.rejectValue(calendarAttribute, ERROR_LENGTH);
            }
        }
    }
}
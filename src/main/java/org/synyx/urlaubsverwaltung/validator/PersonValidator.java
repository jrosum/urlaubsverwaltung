/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.validator;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.view.PersonForm;

import java.math.BigDecimal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class validate if an person's form ('PersonForm') is filled correctly by the user, else it saves error messages
 * in errors object.
 *
 * @author  Aljona Murygina
 */
public class PersonValidator implements Validator {

    private static final String MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_ENTRY = "error.entry";
    private static final String ERROR_EMAIL = "error.email";
    private static final String ERROR_NUMBER = "error.number";

    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String VACATION_DAYS_ENT = "vacationDaysEnt";
    private static final String REMAINING_VACATION_DAYS_ENT = "remainingVacationDaysEnt";
    private static final String VACATION_DAYS_ACC = "vacationDaysAcc";
    private static final String REMAINING_VACATION_DAYS_ACC = "remainingVacationDaysAcc";
    private static final String YEAR = "year";
    private static final String EMAIL = "email";

    private static final double MAX_DAYS = 365;

    // this was the first version of email regex (commented out on 8th Feb. 2012)
// private static final String EMAIL_PATTERN =
// "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    // a regex for email addresses that are valid, but may be "strange looking" (e.g. tomr$2@example.com)
    // original from: http://www.markussipila.info/pub/emailvalidator.php?action=validate
    // modified by adding following characters: äöüß
    private static final String EMAIL_PATTERN =
        "^[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+(\\.[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+)*@[a-zäöüß0-9-]+(\\.[a-zäöüß0-9-]+)*\\.([a-z]{2,})$";

    private static final String NAME_PATTERN = "\\p{L}+"; // any kind of letter from any language.

    private Pattern pattern;
    private Matcher matcher;

    @Override
    public boolean supports(Class<?> clazz) {

        return PersonForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        PersonForm form = (PersonForm) target;

        // field first name
        validateName(form.getFirstName(), FIRST_NAME, errors);

        // field last name
        validateName(form.getLastName(), LAST_NAME, errors);

        // field email address
        validateEmail(form.getEmail(), errors);

        // field year
        validateYear(form.getYear(), errors);

        // entitlement's days must not be null

        // field entitlement's vacation days
        validateNumberOfDays(form.getVacationDaysEnt(), VACATION_DAYS_ENT, MAX_DAYS, errors);

        // field entitlement's remaining vacation days
        validateNumberOfDays(form.getRemainingVacationDaysEnt(), REMAINING_VACATION_DAYS_ENT, MAX_DAYS, errors);
    }


    private boolean matchPattern(String nameOfPattern, String matchSequence) {

        pattern = Pattern.compile(nameOfPattern);
        matcher = pattern.matcher(matchSequence);

        return matcher.matches();
    }


    /**
     * This method checks if the field firstName or the field lastName is filled and if it is filled, it validates the
     * entry with a regex.
     *
     * @param  name  (may be the field firstName or lastName)
     * @param  field
     * @param  errors
     */
    protected void validateName(String name, String field, Errors errors) {

        // is the name field null/empty?
        if (name == null || !StringUtils.hasText(name)) {
            errors.rejectValue(field, MANDATORY_FIELD);
        } else {
            // contains the name field digits?
            if (!matchPattern(NAME_PATTERN, name)) {
                errors.rejectValue(field, ERROR_ENTRY);
            }
        }
    }


    /**
     * This method checks if the field email is filled and if it is filled, it validates the entry with a regex.
     *
     * @param  email
     * @param  errors
     */
    protected void validateEmail(String email, Errors errors) {

        // is email field null or empty
        if (email == null || !StringUtils.hasText(email)) {
            errors.rejectValue(EMAIL, MANDATORY_FIELD);
        } else {
            // validation with regex
            email = email.trim().toLowerCase();

            if (!matchPattern(EMAIL_PATTERN, email)) {
                errors.rejectValue(EMAIL, ERROR_EMAIL);
            }
        }
    }


    /**
     * This method checks if the field year is filled and if it is filled, it checks if the year entry makes sense (at
     * the moment: from 2010 - 2030 alright)
     *
     * @param  yearForm
     * @param  errors
     */
    protected void validateYear(String yearForm, Errors errors) {

        // is year field filled?
        if (yearForm == null || !StringUtils.hasText(yearForm)) {
            errors.rejectValue(YEAR, MANDATORY_FIELD);
        } else {
            try {
                int year = Integer.parseInt(yearForm);

                if (year < 2010 || year > 2030) {
                    errors.rejectValue(YEAR, ERROR_ENTRY);
                }
            } catch (NumberFormatException ex) {
                errors.rejectValue(YEAR, ERROR_ENTRY);
            }
        }
    }


    /**
     * This method validates if the holiday entitlement's fields remaining vacation days and vacation days are filled
     * and if they are filled, it checks if the number of days is realistic
     *
     * @param  days
     * @param  field
     * @param  maximumDays
     * @param  errors
     */
    protected void validateNumberOfDays(BigDecimal days, String field, double maximumDays, Errors errors) {

        // is field filled?
        if (days == null) {
            if (errors.getFieldErrors(field).isEmpty()) {
                errors.rejectValue(field, MANDATORY_FIELD);
            }
        } else {
            // is number of days < 0 ?
            if (days.compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(field, ERROR_ENTRY);
            }

            // is number of days unrealistic?
            if (days.compareTo(BigDecimal.valueOf(maximumDays)) == 1) {
                errors.rejectValue(field, ERROR_ENTRY);
            }
        }
    }


    /**
     * This method validates if the holidays account's fields are filled and if they are filled, it checks if the number
     * of the holidays account's days are smaller than or equals holiday entitlement days
     *
     * @param  form
     * @param  errors
     */
    public void validateAccountDays(PersonForm form, Errors errors) {

        if (form.getRemainingVacationDaysAcc() == null) {
            if (errors.getFieldErrors(REMAINING_VACATION_DAYS_ACC).isEmpty()) {
                errors.rejectValue(REMAINING_VACATION_DAYS_ACC, MANDATORY_FIELD);
            }
        }

        if (form.getVacationDaysAcc() == null) {
            if (errors.getFieldErrors(VACATION_DAYS_ACC).isEmpty()) {
                errors.rejectValue(VACATION_DAYS_ACC, MANDATORY_FIELD);
            }
        }

        if (form.getVacationDaysAcc() != null) {
            // is number of days < 0 ?
            if (form.getVacationDaysAcc().compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(VACATION_DAYS_ACC, ERROR_ENTRY);
            }
        }

        if (form.getRemainingVacationDaysAcc() != null) {
            // is number of days < 0 ?
            if (form.getRemainingVacationDaysAcc().compareTo(BigDecimal.ZERO) == -1) {
                errors.rejectValue(REMAINING_VACATION_DAYS_ACC, ERROR_ENTRY);
            }
        }

        if (form.getVacationDaysEnt() != null && form.getVacationDaysAcc() != null) {
            // check if number of account's days is greater than number of entitlement's days
            if (form.getVacationDaysAcc().compareTo(form.getVacationDaysEnt()) == 1) {
                errors.rejectValue(VACATION_DAYS_ACC, ERROR_NUMBER);
            }
        }

        if (form.getRemainingVacationDaysEnt() != null && form.getRemainingVacationDaysAcc() != null) {
            // account days must not be greater than entitlement days
            if (form.getRemainingVacationDaysAcc().compareTo(form.getRemainingVacationDaysEnt()) == 1) {
                errors.rejectValue(REMAINING_VACATION_DAYS_ACC, ERROR_NUMBER);
            }
        }
    }
}
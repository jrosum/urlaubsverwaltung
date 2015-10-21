package org.synyx.urlaubsverwaltung.core.person;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class PersonTest {

    @Test
    public void ensureReturnsFirstAndLastNameAsNiceName() {

        Person person = new Person("muster", "Muster", "Max", "");

        Assert.assertEquals("Wrong nice name", "Max Muster", person.getNiceName());
    }


    @Test
    public void ensureReturnsLoginNameAsNiceNameIfFirstAndLastNameAreNotSet() {

        Person person = new Person("muster", "", "", "");

        Assert.assertEquals("Wrong nice name", "muster", person.getNiceName());
    }


    @Test
    public void ensureReturnsTrueIfPersonHasTheGivenRole() {

        Person person = new Person();
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Assert.assertTrue("Should return true if the person has the given role", person.hasRole(Role.BOSS));
    }


    @Test
    public void ensureReturnsFalseIfPersonHasNotTheGivenRole() {

        Person person = new Person();
        person.setPermissions(Collections.singletonList(Role.USER));

        Assert.assertFalse("Should return false if the person has not the given role", person.hasRole(Role.BOSS));
    }


    @Test
    public void ensureReturnsTrueIfPersonHasTheGivenNotificationType() {

        Person person = new Person();
        person.setNotifications(Arrays.asList(MailNotification.NOTIFICATION_USER, MailNotification.NOTIFICATION_BOSS));

        Assert.assertTrue("Should return true if the person has the given notification type",
            person.hasNotificationType(MailNotification.NOTIFICATION_BOSS));
    }


    @Test
    public void ensureReturnsFalseIfPersonHasNotTheGivenNotificationType() {

        Person person = new Person();
        person.setNotifications(Collections.singletonList(MailNotification.NOTIFICATION_USER));

        Assert.assertFalse("Should return false if the person has not the given notification type",
            person.hasNotificationType(MailNotification.NOTIFICATION_BOSS));
    }


    @Test
    public void ensureReturnsEmptyStringAsGravatarURLIfEmailIsEmpty() {

        Person person = new Person();
        person.setEmail(null);

        Assert.assertNotNull("Should not be null", person.getGravatarURL());
        Assert.assertEquals("Wrong Gravatar URL", "", person.getGravatarURL());
    }


    @Test
    public void ensureCanReturnGravatarURL() {

        Person person = new Person();
        person.setEmail("muster@test.de");

        Assert.assertNotNull("Should not be null", person.getGravatarURL());
        Assert.assertNotEquals("Gravatar URL should not be empty", "", person.getGravatarURL());
        Assert.assertNotEquals("Gravatar URL should differ from mail address", person.getEmail(),
            person.getGravatarURL());
    }


    @Test
    public void ensurePermissionsAreUnmodifiable() {

        List<Role> modifiableList = new ArrayList<>();
        modifiableList.add(Role.USER);

        Person person = new Person();
        person.setPermissions(modifiableList);

        try {
            person.getPermissions().add(Role.BOSS);
            Assert.fail("Permissions should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }


    @Test
    public void ensureNotificationsAreUnmodifiable() {

        List<MailNotification> modifiableList = new ArrayList<>();
        modifiableList.add(MailNotification.NOTIFICATION_USER);

        Person person = new Person();
        person.setNotifications(modifiableList);

        try {
            person.getNotifications().add(MailNotification.NOTIFICATION_BOSS);
            Assert.fail("Notifications should be unmodifiable!");
        } catch (UnsupportedOperationException ex) {
            // Expected
        }
    }
}

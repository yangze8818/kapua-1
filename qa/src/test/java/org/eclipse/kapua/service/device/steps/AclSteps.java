/*******************************************************************************
 * Copyright (c) 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech
 *******************************************************************************/
package org.eclipse.kapua.service.device.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.StepData;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authentication.AuthenticationService;
import org.eclipse.kapua.service.authentication.LoginCredentials;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authentication.shiro.UsernamePasswordCredentialsImpl;
import org.eclipse.kapua.service.authorization.access.AccessInfoService;
import org.eclipse.kapua.service.user.User;
import org.eclipse.kapua.service.user.UserFactory;
import org.eclipse.kapua.service.user.UserService;
import org.eclipse.paho.client.mqttv3.MqttException;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Steps for testing Access Control List functionality on Broker service.
 */
public class AclSteps {

    public static final int BROKER_START_WAIT_MILLIS = 5000;

    private static final String SYS_USERNAME = "kapua-sys";

    private static final String SYS_PASSWORD = "kapua-password";

    /**
     * Mqtt device for listening and sending data from/to broker
     */
    private static MqttDevice mqttDevice;

    /**
     * Topic / value pair containing message that was received from broker by client.
     */
    private static Map<String, Map<String, String>> clientMqttMessage;

    /**
     * Topic / value pair containing message that was received from broker by listener.
     */
    private static Map<String, String> listenerMqttMessage;

    /**
     * Authentication service.
     */
    private static AuthenticationService authenticationService;

    /**
     * Account service.
     */
    private static AccountService accountService;

    /**
     * Account factory.
     */
    private static AccountFactory accountFactory;

    /**
     * User service.
     */
    private static UserService userService;

    /**
     * User factory.
     */
    private static UserFactory userFactory;

    /**
     * Credential service.
     */
    private static CredentialService credentialService;

    /**
     * Accessinfo service.
     */
    private static AccessInfoService accessInfoService;

    /**
     * Helper for creating Accoutn, User and other artifacts needed in tests.
     */
    private static AclCreator aclCreator;

    /**
     * Inter step data scratchpad.
     */
    private StepData stepData;

    @Inject
    public AclSteps(StepData stepData) {

        this.stepData = stepData;
    }

    @Before
    public void aclStepsBefore() {

        KapuaLocator locator = KapuaLocator.getInstance();
        authenticationService = locator.getService(AuthenticationService.class);
        accountService = locator.getService(AccountService.class);
        accountFactory = locator.getFactory(AccountFactory.class);
        userService = locator.getService(UserService.class);
        userFactory = locator.getFactory(UserFactory.class);
        credentialService = locator.getService(CredentialService.class);
        accessInfoService = locator.getService(AccessInfoService.class);

        mqttDevice = new MqttDevice();
        clientMqttMessage = new HashMap<>();
        listenerMqttMessage = new HashMap<>();

        aclCreator = new AclCreator(accountService, accountFactory, userService, accessInfoService, credentialService);
    }

    @Given("string \"(.*)\" is published to topic \"(.*)\" with client \"(.*)\"$")
    public void clientPublishString(String payload, String topic, String clientId) {

        mqttDevice.mqttClientPublishString(clientId, payload, topic, clientMqttMessage, listenerMqttMessage);
    }

    @Given("^Mqtt Device is started$")
    public void startMqttDevice() throws KapuaException {

        mqttDevice.mqttSubscriberConnect();
        // Wait for broker to start
        waitInMillis(BROKER_START_WAIT_MILLIS);
        // Login with system user
        char[] passwd = SYS_PASSWORD.toCharArray();
        LoginCredentials credentials = new UsernamePasswordCredentialsImpl(SYS_USERNAME, passwd);
        authenticationService.login(credentials);
    }

    @When("^Mqtt Device is stoped$")
    public void stopMqttDevice() throws KapuaException {

        mqttDevice.mqttClientsDisconnect();
        mqttDevice.mqttSubscriberDisconnect();
        // Logout system user
        authenticationService.logout();
    }

    @Given("^broker with clientId \"(.*)\" and user \"(.*)\" and password \"(.*)\" is listening on topic \"(.*)\"$")
    public void connectClientToBroker(String clientId, String userName, String password, String topicFilter) {

        try {
            mqttDevice.mqttClientConnect(clientId, userName, password, topicFilter);
        } catch (MqttException mqtte) {
            stepData.put("exception", mqtte);
        }
    }

    @Given("^clients are disconnected$")
    public void disconnectClientsFromBroker() {

        mqttDevice.mqttClientsDisconnect();
    }

    @Then("^Broker receives string \"([^\"]*)\" on topic \"([^\"]*)\"$")
    public void brokerReceivesStringOnTopic(String payload, String topic) throws Throwable {

        if ((listenerMqttMessage != null) && (listenerMqttMessage.size() == 1)) {
            String message = listenerMqttMessage.get(topic);
            assertEquals(payload, message);
        } else {
            fail("Message not received by broker.");
        }
    }

    @Then("^Broker doesn't receive string \"([^\"]*)\" on topic \"([^\"]*)\"$")
    public void brokerDoesntReceiveStringOnTopic(String payload, String topic) throws Throwable {

        if ((listenerMqttMessage != null) && (listenerMqttMessage.size() >= 1)) {
            String message = listenerMqttMessage.get(topic);
            assertNotEquals(payload, message);
        }
    }

    @Then("^client \"([^\"]*)\" receives string \"([^\"]*)\" on topic \"([^\"]*)\"$")
    public void iReceiveStringOnTopic(String clientId, String payload, String topic) throws Throwable {

        Map<String, String> messages = clientMqttMessage.get(clientId);
        if ((messages != null) && (messages.size() >= 1)) {
            String message = messages.get(topic);
            assertEquals(payload, message);
        } else {
            fail("Message not received by broker.");
        }
    }

    @Then("^client \"([^\"]*)\" doesn't receive string \"([^\"]*)\" on topic \"([^\"]*)\"$")
    public void clientDoesntReceiveStringOnTopic(String clientId, String payload, String topic) throws Throwable {

        Map<String, String> messages = clientMqttMessage.get(clientId);
        if ((messages != null) && (messages.size() >= 1)) {
            String message = messages.get(topic);
            assertNotEquals(payload, message);
        }
    }

    @And("^broker account and user are created$")
    public void createBrokerAccountAndUser() throws Throwable {

        Account account = aclCreator.createAccount("acme","ACME Corp.", "john@acme.org");
        User user = aclCreator.createUser(account, "luise");
        aclCreator.attachUserCredentials(account, user);
        aclCreator.attachBrokerPermissions(account, user);
    }

    @And("^other broker account and user are created$")
    public void createOtherBrokerAccountAndUser() throws Throwable {

        Account account = aclCreator.createAccount("domino","Domino Corp.", "lisa@domino.org");
        User user = aclCreator.createUser(account, "domina");
        aclCreator.attachUserCredentials(account, user);
        aclCreator.attachBrokerPermissions(account, user);
    }

    @Then("^exception is thrown$")
    public void exceptionIsThrown() throws Throwable {

        Exception e = (Exception) stepData.get("exception");
        assertNotNull("Exception expected!", e);
    }

    /**
     * Simple wait implementation.
     *
     * @param millis milli seconds
     */
    private void waitInMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

}

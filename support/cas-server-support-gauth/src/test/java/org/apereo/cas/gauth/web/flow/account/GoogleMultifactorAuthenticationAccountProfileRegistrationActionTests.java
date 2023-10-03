package org.apereo.cas.gauth.web.flow.account;

import org.apereo.cas.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfileRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    CasWebflowAccountProfileConfiguration.class
},
    properties = "CasFeatureModule.AccountManagement.enabled=true")
@Tag("WebflowMfaActions")
public class GoogleMultifactorAuthenticationAccountProfileRegistrationActionTests {
    @Autowired
    @Qualifier("googleAccountProfileRegistrationAction")
    private Action googleAccountProfileRegistrationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val eventId = googleAccountProfileRegistrationAction.execute(context);
        assertNull(eventId);
        assertNotNull(WebUtils.getMultifactorAuthenticationProvider(context));
    }
}

package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasGoogleAnalyticsTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RemoveGoogleAnalyticsCookieActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCasGoogleAnalyticsTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowActions")
class RemoveGoogleAnalyticsCookieActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_ANALYTICS_REMOVE_COOKIE)
    private Action removeGoogleAnalyticsCookieAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        assertNull(removeGoogleAnalyticsCookieAction.execute(context));
    }
}

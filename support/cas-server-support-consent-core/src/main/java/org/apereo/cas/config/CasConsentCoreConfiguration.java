package org.apereo.cas.config;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.consent.AttributeConsentReportEndpoint;
import org.apereo.cas.consent.AttributeReleaseConsentCipherExecutor;
import org.apereo.cas.consent.ConsentActivationStrategy;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentEngine;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.ConsentableAttributeBuilder;
import org.apereo.cas.consent.DefaultConsentActivationStrategy;
import org.apereo.cas.consent.DefaultConsentDecisionBuilder;
import org.apereo.cas.consent.DefaultConsentEngine;
import org.apereo.cas.consent.GroovyConsentActivationStrategy;
import org.apereo.cas.consent.GroovyConsentRepository;
import org.apereo.cas.consent.InMemoryConsentRepository;
import org.apereo.cas.consent.JsonConsentRepository;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.spi.AuditActionResolver;
import org.apereo.inspektr.audit.spi.AuditResourceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

/**
 * This is {@link CasConsentCoreConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "casConsentCoreConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasConsentCoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "defaultConsentableAttributeBuilder")
    public ConsentableAttributeBuilder defaultConsentableAttributeBuilder() {
        return ConsentableAttributeBuilder.noOp();
    }

    @ConditionalOnMissingBean(name = "consentEngine")
    @Bean
    @RefreshScope
    @Autowired
    public ConsentEngine consentEngine(
        final CasConfigurationProperties casProperties,
        @Qualifier("consentDecisionBuilder")
        final ConsentDecisionBuilder consentDecisionBuilder,
        final List<ConsentableAttributeBuilder> builders,
        @Qualifier("consentRepository")
        final ConsentRepository consentRepository) {
        AnnotationAwareOrderComparator.sortIfNecessary(builders);
        return new DefaultConsentEngine(consentRepository, consentDecisionBuilder, casProperties, builders);
    }

    @ConditionalOnMissingBean(name = "consentCipherExecutor")
    @Bean
    @RefreshScope
    @Autowired
    public CipherExecutor consentCipherExecutor(final CasConfigurationProperties casProperties) {
        val consent = casProperties.getConsent().getCore();
        val crypto = consent.getCrypto();
        if (crypto.isEnabled()) {
            return CipherExecutorUtils.newStringCipherExecutor(crypto, AttributeReleaseConsentCipherExecutor.class);
        }
        LOGGER.debug("Consent attributes stored by CAS are not signed/encrypted.");
        return CipherExecutor.noOp();
    }

    @ConditionalOnMissingBean(name = "consentDecisionBuilder")
    @Bean
    @RefreshScope
    @Autowired
    public ConsentDecisionBuilder consentDecisionBuilder(
        @Qualifier("consentCipherExecutor")
        final CipherExecutor consentCipherExecutor) {
        return new DefaultConsentDecisionBuilder(consentCipherExecutor);
    }

    @ConditionalOnMissingBean(name = "consentActivationStrategy")
    @Bean
    @RefreshScope
    @Autowired
    public ConsentActivationStrategy consentActivationStrategy(
        @Qualifier("consentEngine")
        final ConsentEngine consentEngine,
        final CasConfigurationProperties casProperties) {
        val location = casProperties.getConsent().getActivationStrategyGroovyScript().getLocation();
        if (location != null) {
            return new GroovyConsentActivationStrategy(location, consentEngine, casProperties);
        }
        return new DefaultConsentActivationStrategy(consentEngine, casProperties);
    }

    @ConditionalOnMissingBean(name = "consentRepository")
    @Bean
    @RefreshScope
    @Autowired
    public ConsentRepository consentRepository(final CasConfigurationProperties casProperties) {
        val location = casProperties.getConsent().getJson().getLocation();
        if (location != null) {
            LOGGER.warn("Storing consent records in [{}]. This MAY NOT be appropriate in production. "
                + "Consider choosing an alternative repository format for storing consent decisions", location);
            return new JsonConsentRepository(location);
        }

        val groovy = casProperties.getConsent().getGroovy().getLocation();
        if (groovy != null) {
            return new GroovyConsentRepository(groovy);
        }

        LOGGER.warn("Storing consent records in memory. This option is ONLY relevant for demos and testing purposes.");
        return new InMemoryConsentRepository();
    }

    @Bean
    @Autowired
    @ConditionalOnMissingBean(name = "casConsentAuditTrailRecordResolutionPlanConfigurer")
    public AuditTrailRecordResolutionPlanConfigurer casConsentAuditTrailRecordResolutionPlanConfigurer(
        @Qualifier("authenticationActionResolver")
        final AuditActionResolver authenticationActionResolver,
        @Qualifier("returnValueResourceResolver")
        final AuditResourceResolver returnValueResourceResolver) {
        return plan -> {
            plan.registerAuditActionResolver(AuditActionResolvers.SAVE_CONSENT_ACTION_RESOLVER, authenticationActionResolver);
            plan.registerAuditResourceResolver(AuditResourceResolvers.SAVE_CONSENT_RESOURCE_RESOLVER, returnValueResourceResolver);
        };
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public AttributeConsentReportEndpoint attributeConsentReportEndpoint(
        @Qualifier("consentEngine")
        final ConsentEngine consentEngine,
        @Qualifier("consentRepository")
        final ConsentRepository consentRepository,
        final CasConfigurationProperties casProperties) {
        return new AttributeConsentReportEndpoint(casProperties, consentRepository, consentEngine);
    }
}

package org.apereo.cas.adaptors.yubikey.web.flow;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link YubiKeyAuthenticationPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class YubiKeyAuthenticationPrepareLoginAction extends BaseCasWebflowAction {
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        MultifactorAuthenticationWebflowUtils.putYubiKeyMultipleDeviceRegistrationEnabled(requestContext,
            casProperties.getAuthn().getMfa().getYubikey().isMultipleDeviceRegistrationEnabled());
        return null;
    }
}

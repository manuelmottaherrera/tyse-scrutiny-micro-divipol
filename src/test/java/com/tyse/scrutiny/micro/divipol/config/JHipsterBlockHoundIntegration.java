package com.tyse.scrutiny.micro.divipol.config;

import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

public class JHipsterBlockHoundIntegration implements BlockHoundIntegration {

    @Override
    public void applyTo(BlockHound.Builder builder) {
        builder.allowBlockingCallsInside("org.springframework.validation.beanvalidation.SpringValidatorAdapter", "validate");
        builder.allowBlockingCallsInside("com.tyse.scrutiny.micro.divipol.service.MailService", "sendEmailFromTemplate");
        builder.allowBlockingCallsInside("com.tyse.scrutiny.micro.divipol.security.DomainUserDetailsService", "createSpringSecurityUser");
        builder.allowBlockingCallsInside("org.springframework.web.reactive.result.method.InvocableHandlerMethod", "invoke");
        builder.allowBlockingCallsInside("org.springdoc.core.service.OpenAPIService", "build");
        builder.allowBlockingCallsInside("org.springdoc.core.service.OpenAPIService", "getWebhooks");
        builder.allowBlockingCallsInside("org.springdoc.core.service.AbstractRequestService", "build");

        // Permitir operaciones bloqueantes de OpenPDF en Schedulers.boundedElastic
        builder.allowBlockingCallsInside("com.lowagie.text.pdf.PdfWriter", "getInstance");
        builder.allowBlockingCallsInside("com.lowagie.text.pdf.PdfDocument", "close");
        builder.allowBlockingCallsInside("com.lowagie.text.pdf.BaseFont", "createFont");
        builder.allowBlockingCallsInside("com.tyse.scrutiny.micro.divipol.service.export.DivipolPdfGenerator", "generateReport");
        builder.allowBlockingCallsInside("com.tyse.scrutiny.micro.divipol.service.export.DivipolPdfGenerator", "generateSearchReport");
        builder.allowBlockingCallsInside("com.tyse.scrutiny.micro.divipol.service.export.DivipolPdfGenerator", "initFonts");
        builder.allowBlockingCallsInside("com.tyse.scrutiny.micro.divipol.service.export.DivipolPdfGenerator", "loadEmbeddedFont");
        // jhipster-needle-blockhound-integration - JHipster will add additional gradle plugins here
    }
}

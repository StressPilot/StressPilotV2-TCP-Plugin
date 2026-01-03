package dev.zeann3th.plugins.tcp;

import dev.zeann3th.stresspilot.common.validators.EndpointTypeValidator;
import dev.zeann3th.stresspilot.dto.endpoint.EndpointDTO;
import jakarta.validation.ConstraintValidatorContext;
import org.pf4j.Extension;

@Extension
public class TcpEndpointValidator implements EndpointTypeValidator {

    @Override
    public boolean supports(String endpointType) {
        return "TCP".equalsIgnoreCase(endpointType);
    }

    @Override
    public boolean isValid(EndpointDTO request, ConstraintValidatorContext context) {
        boolean valid = true;
        StringBuilder errorMessage = new StringBuilder("Missing required fields for TCP: ");

        if (request.getUrl() == null || !request.getUrl().contains(":")) {
            valid = false;
            errorMessage.append("URL must be in 'host:port' format. ");
        }

        if (request.getBody() == null) {
            valid = false;
            errorMessage.append("TCP payload (body) is required.");
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage.toString())
                    .addConstraintViolation();
        }

        return valid;
    }
}
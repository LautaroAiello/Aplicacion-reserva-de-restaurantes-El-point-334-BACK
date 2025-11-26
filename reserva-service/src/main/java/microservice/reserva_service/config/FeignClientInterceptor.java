package microservice.reserva_service.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        // 1. Obtener la petición HTTP actual que llegó al controlador
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 2. Extraer el header "Authorization" (donde viene el Bearer token)
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            
            // 3. Si existe el token, inyectarlo en la petición saliente de Feign
            if (authHeader != null) {
                template.header(AUTHORIZATION_HEADER, authHeader);
            }
        }
    }
}

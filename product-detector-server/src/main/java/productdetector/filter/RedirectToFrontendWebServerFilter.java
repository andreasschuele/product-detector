package productdetector.filter;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("development")
public class RedirectToFrontendWebServerFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectToFrontendWebServerFilter.class);

    private String redirectFrontendUrl = "http://localhost:8081/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            if (redirectFrontendUrl.length() > 0
                && !path.contains("/v3/api-docs")     // swagger
                && !path.contains("/webjars")         // swagger
                && !path.contains("/swagger-ui")      // swagger
                && !path.contains("/api/")) {
                String bearerToken = request.getHeader("Authorization");
                String requestURL = request.getRequestURL().toString();

                if (bearerToken == null || bearerToken.isBlank()) {
                    bearerToken = "test";
                }

                if (requestURL.contains("localhost:5000") || requestURL.startsWith("192.168.178.27:5000")) {
                    if (requestURL.startsWith("https://localhost:5000/") ) {
                        requestURL = requestURL.replace("https://localhost:5000/", redirectFrontendUrl);
                    } else if (requestURL.startsWith("http://localhost:5000/") ) {
                        requestURL = requestURL.replace("http://localhost:5000/", redirectFrontendUrl);
                    } else if (requestURL.startsWith("https://192.168.178.27:5000/") ) {
                        requestURL = requestURL.replace("https://192.168.178.27:5000/", redirectFrontendUrl);
                    } else if (requestURL.startsWith("http://192.168.178.27:5000/") ) {
                        requestURL = requestURL.replace("http://192.168.178.27:5000/", redirectFrontendUrl);
                    }

                    HttpClient client = prepareHttpClient(bearerToken);

                    HttpGet clientRequest = new HttpGet(requestURL);
                    HttpResponse clientResponse = client.execute(clientRequest);

                    if (clientResponse.getStatusLine().getStatusCode() == 404) {
                        // If a resource can't be found, then serve the index page.
                        clientRequest = new HttpGet("http://localhost:5000/index.html");
                        clientResponse = client.execute(clientRequest);
                    }

                    String contentType = clientResponse.getEntity().getContentType().getValue();

                    response.setContentType(contentType);

                    IOUtils.copy(clientResponse.getEntity().getContent(), response.getOutputStream());
                } else {
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", "/");
                }

                return;
            }
        } catch (Exception ex) {
            LOGGER.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private HttpClient prepareHttpClient(String bearerToken) {
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        Header sessionTokenHeader = new BasicHeader("Session-Token", bearerToken);
        List<Header> headers = Arrays.asList(sessionTokenHeader);

        clientBuilder = clientBuilder.setDefaultHeaders(headers);

        return clientBuilder.build();
    }

}

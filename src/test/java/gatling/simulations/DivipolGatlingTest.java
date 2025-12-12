package gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Performance test for the Divipol API endpoints.
 *
 * This simulation tests all divipol endpoints including:
 * - Hierarchical navigation (departamentos → municipios → zonas → puestos)
 * - Search and suggestions
 * - Statistics at different levels
 * - Export operations (CSV and PDF)
 *
 * Run with:
 *   ./mvnw gatling:test
 *   ./mvnw gatling:test -Dusers=50 -Dramp=2
 *   ./mvnw gatling:test -DbaseURL=http://localhost:8081
 */
public class DivipolGatlingTest extends Simulation {

    // JWT Configuration - same secret as Consul config/application/data
    // Note: Consul has priority over application-dev.yml
    private static final String JWT_BASE64_SECRET =
        "YWQ5YzhhNzcwN2JjOGRjMTQ0YTMyNzFhYTMyNThhZDA3NWRlMjI2NjlmN2QzOGQxYTI0NmE2OGNlMTViMWZkZTgwNDI4NjAzYTYyZjFlY2FjY2Q5YTIzZDI3NGRmYmNhZjNkMTFkYmU0YmZjOGY5MTY5OTM1NzAxYmEzZThiM2E=";
    private static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    private static final String AUTHORITIES_CLAIM = "auth";

    // Generate a valid JWT token for admin user
    private static String generateJwtToken() {
        byte[] keyBytes = Base64.from(JWT_BASE64_SECRET).decode();
        SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plusSeconds(86400)) // 24 hours validity
            .subject("admin")
            .claims(customClaim -> customClaim.put(AUTHORITIES_CLAIM, Collections.singletonList("ROLE_ADMIN")))
            .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return encoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    // Pre-generated JWT token for all requests
    private final String jwtToken = generateJwtToken();

    // Base URL - defaults to localhost:8081 for microservice
    String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8081");

    // HTTP Protocol configuration
    HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("application/json")
        .acceptEncodingHeader("gzip, deflate")
        .acceptLanguageHeader("es-CO,es;q=0.9,en;q=0.8")
        .connectionHeader("keep-alive")
        .userAgentHeader("Mozilla/5.0 (X11; Linux x86_64) Gatling/3.13.5")
        .silentResources();

    // Headers for authenticated requests (JWT token pre-generated)
    Map<String, String> headersHttpAuthenticated = Map.of("Accept", "application/json", "Authorization", "Bearer " + jwtToken);

    // Headers for CSV export requests
    Map<String, String> headersCsvExport = Map.of("Accept", "text/csv", "Authorization", "Bearer " + jwtToken);

    // Headers for PDF export requests
    Map<String, String> headersPdfExport = Map.of("Accept", "application/pdf", "Authorization", "Bearer " + jwtToken);

    // ========== SCENARIO 1: HIERARCHICAL NAVIGATION ==========
    ChainBuilder hierarchicalNavigation = exec(
        http("01. GET /divipol/departamentos")
            .get("/api/divipol/departamentos")
            .headers(headersHttpAuthenticated)
            .check(status().is(200))
            .check(jsonPath("$[0].coddepto").exists())
    )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("02. GET /divipol/municipios?codDepto=11 (Bogota)")
                .get("/api/divipol/municipios")
                .queryParam("codDepto", "11")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("03. GET /divipol/zonas?codDepto=11&codMpio=1 (Bogota Centro)")
                .get("/api/divipol/zonas")
                .queryParam("codDepto", "11")
                .queryParam("codMpio", "1")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("04. GET /divipol/puestos?codDepto=11&codMpio=1&codZona=1")
                .get("/api/divipol/puestos")
                .queryParam("codDepto", "11")
                .queryParam("codMpio", "1")
                .queryParam("codZona", "1")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(1);

    // ========== SCENARIO 2: SEARCH AND SUGGESTIONS ==========
    ChainBuilder searchOperations = exec(
        http("05. GET /divipol/search?q=BOGOTA (name search)")
            .get("/api/divipol/search")
            .queryParam("q", "BOGOTA")
            .queryParam("mode", "name")
            .headers(headersHttpAuthenticated)
            .check(status().is(200))
            .check(jsonPath("$.content").exists())
    )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("06. GET /divipol/search?q=11&mode=code (code search)")
                .get("/api/divipol/search")
                .queryParam("q", "11")
                .queryParam("mode", "code")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("07. GET /divipol/search/suggestions?q=MED (autocomplete)")
                .get("/api/divipol/search/suggestions")
                .queryParam("q", "MED")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(1);

    // ========== SCENARIO 3: STATISTICS ==========
    ChainBuilder statisticsOperations = exec(
        http("08. GET /divipol/stats (general stats)").get("/api/divipol/stats").headers(headersHttpAuthenticated).check(status().is(200))
    )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("09. GET /divipol/stats/departamento/11 (Bogota stats)")
                .get("/api/divipol/stats/departamento/11")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("10. GET /divipol/stats/municipio/11/1 (Bogota Centro stats)")
                .get("/api/divipol/stats/municipio/11/1")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("11. GET /divipol/stats/zona/11/1/1 (Zone stats)")
                .get("/api/divipol/stats/zona/11/1/1")
                .headers(headersHttpAuthenticated)
                .check(status().is(200))
        )
        .pause(1);

    // ========== SCENARIO 4: EXPORT OPERATIONS ==========
    ChainBuilder exportOperations = exec(
        http("12. GET /divipol/export/filters/csv (all departamentos)")
            .get("/api/divipol/export/filters/csv")
            .headers(headersCsvExport)
            .check(status().is(200))
            .check(header("Content-Type").is("text/csv"))
    )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("13. GET /divipol/export/filters/pdf (all departamentos)")
                .get("/api/divipol/export/filters/pdf")
                .headers(headersPdfExport)
                .check(status().is(200))
                .check(header("Content-Type").is("application/pdf"))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("14. GET /divipol/export/filters/csv?codDepto=11 (Bogota)")
                .get("/api/divipol/export/filters/csv")
                .queryParam("codDepto", "11")
                .headers(headersCsvExport)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("15. GET /divipol/export/search/csv?q=BOGOTA (name search)")
                .get("/api/divipol/export/search/csv")
                .queryParam("q", "BOGOTA")
                .queryParam("mode", "name")
                .headers(headersCsvExport)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("16. GET /divipol/export/search/pdf?q=11&mode=code (code search)")
                .get("/api/divipol/export/search/pdf")
                .queryParam("q", "11")
                .queryParam("mode", "code")
                .headers(headersPdfExport)
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500), Duration.ofSeconds(1))
        .exec(
            http("17. GET /divipol/export/search/csv?exportAll=true (export all)")
                .get("/api/divipol/export/search/csv")
                .queryParam("q", "ESCUELA")
                .queryParam("mode", "name")
                .queryParam("exportAll", "true")
                .headers(headersCsvExport)
                .check(status().is(200))
        )
        .pause(1);

    // ========== COMBINED SCENARIO ==========
    ScenarioBuilder divipolScenario = scenario("Divipol API Performance Test")
        .exec(hierarchicalNavigation)
        .exec(searchOperations)
        .exec(statisticsOperations)
        .exec(exportOperations);

    // ========== LOAD CONFIGURATION ==========
    {
        setUp(
            divipolScenario.injectOpen(
                rampUsers(Integer.getInteger("users", 100)).during(Duration.ofMinutes(Integer.getInteger("ramp", 1)))
            )
        ).protocols(httpConf);
    }
}

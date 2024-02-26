package com.seamfix.nimc.maybeach.filters;

import com.seamfix.nimc.maybeach.configs.AppConfig;
import com.seamfix.nimc.maybeach.utils.Constants;
import com.sf.bioweb.auth.tools.ClientGuard;
import com.sf.bioweb.auth.utils.AuthArgs;
import com.sf.bioweb.auth.utils.AuthResp;
import lombok.extern.slf4j.Slf4j;
import org.keyczar.Crypter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Authenticates only endpoints added in the validatedUrls field
 */
@Slf4j
@Component
public class PaymentStatusFilter extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private Crypter crypter;

    @Autowired
    AppConfig config;

    private static final String GPASS = "GPASS";

    private final Set<String> nonValidatedUrls = new HashSet<>(Arrays.asList(
            "/device/request-activation", "/device/activation-data/{deviceId}/{requestId}",
            "/actuator/**"
    ));
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @SuppressWarnings("PMD.NcssCount")
    @Override
    protected void doFilterInternal(HttpServletRequest requestContext, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (config.isSkipAuthenticationHeaders()) {
            log.debug("skip header authentication is turned on");
            filterChain.doFilter(requestContext, httpServletResponse);
            return;
        }

        String gatePass = requestContext.getHeader(GPASS);
        String deviceId = requestContext.getHeader(Constants.X_DEVICE_ID);

        if (gatePass == null || !validGatePasskey(gatePass, deviceId)) {

            String appVersion = requestContext.getHeader(Constants.X_APP_VERSION);
            String timeStamp = requestContext.getHeader(Constants.X_TIMESTAMP);
            String userId = requestContext.getHeader(Constants.X_USER_ID);
            String signature = requestContext.getHeader(Constants.SIGNATURE);

            String xForwardedFor = requestContext.getHeader("X-FORWARDED-FOR");
            AuthArgs args = new AuthArgs();

            args.setAppVersion(appVersion);
            args.setTimestamp(timeStamp);
            args.setUserId(userId);
            args.setSignature(signature);
            args.setDeviceId(deviceId);
            args.setSaltKey(config.getSaltKey());
            args.setEmgr(entityManager);
            args.setRemoteAddress(requestContext.getRemoteAddr());
            args.setXForwardedFor(xForwardedFor);
            ClientGuard guard = new ClientGuard();
            AuthResp authResp = guard.init(args, crypter);

            if (authResp == null) {
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String status = authResp.getStatus();
            if (!Constants.AUTHORIZED.equalsIgnoreCase(status)) {
                httpServletResponse.setContentType("application/json;charset=UTF-8");
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

//			}
            filterChain.doFilter(requestContext, httpServletResponse);
        }
    }


    @SuppressWarnings("PMD.UnusedFormalParameter")
    private boolean validGatePasskey(String gatePass, String deviceId) {
        //TODO implement gatepass at a later time guard.validateGatePass(gatePassParams);
//        GatePassParams gatePassParams = new GatePassParams();
//        gatePassParams.setDeviceId(deviceId);
//        gatePassParams.setClientType(ClientType.ANDROID_CLIENT);
//        gatePassParams.setGatePass(gatePass);

        return false;

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        boolean skipFilter = nonValidatedUrls.stream().anyMatch(p -> pathMatcher.match(p, request.getServletPath()));
        if (log.isDebugEnabled()) {
            log.debug("skipFilter {} request url {}", skipFilter, request.getServletPath());
        }
        return skipFilter;
    }

}

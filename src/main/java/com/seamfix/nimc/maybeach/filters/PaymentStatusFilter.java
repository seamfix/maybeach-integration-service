package com.seamfix.nimc.maybeach.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class PaymentStatusFilter extends OncePerRequestFilter {

    private final Set<String> nonValidatedUrls = new HashSet<>(Arrays.asList(
            "/device/request-activation", "/device/activation-data/{deviceId}/{requestId}",
            "/actuator/**"
    ));
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @SuppressWarnings("PMD.NcssCount")
    @Override
    protected void doFilterInternal(HttpServletRequest requestContext, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.debug("skip header authentication is turned on");
        filterChain.doFilter(requestContext, httpServletResponse);
/*
        String deviceId = requestContext.getHeader(Constants.X_DEVICE_ID);

        String appVersion = requestContext.getHeader(Constants.X_APP_VERSION);
        String timeStamp = requestContext.getHeader(Constants.X_TIMESTAMP);
        String userId = requestContext.getHeader(Constants.X_USER_ID);
        String signature = requestContext.getHeader(Constants.SIGNATURE);

        AuthArgs args = new AuthArgs();

        args.setAppVersion(appVersion);
        args.setTimestamp(timeStamp);
        args.setUserId(userId);
        args.setSignature(signature);
        args.setDeviceId(deviceId);
        args.setSaltKey(config.getSaltKey());
        args.setEmgr(entityManager);
        args.setRemoteAddress(requestContext.getRemoteAddr());
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

        filterChain.doFilter(requestContext, httpServletResponse);
*/
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

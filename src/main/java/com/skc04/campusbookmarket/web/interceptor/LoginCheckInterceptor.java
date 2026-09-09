package com.skc04.campusbookmarket.web.interceptor;

import com.skc04.campusbookmarket.web.session.SessionConst;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean loginRequired =
                handlerMethod.hasMethodAnnotation(LoginRequired.class);

        if (!loginRequired) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null
                || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {

            String redirectURL = createRedirectURL(request);
            String encodedRedirectURL = URLEncoder.encode(
                    redirectURL,
                    StandardCharsets.UTF_8
            );

            response.sendRedirect("/login?redirectURL=" + encodedRedirectURL);
            return false;
        }

        return true;
    }

    private static String createRedirectURL(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return "/posts";
        }

        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURI;
        }

        return requestURI + "?" + queryString;
    }
}

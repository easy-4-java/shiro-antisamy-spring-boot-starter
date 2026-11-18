package org.apache.shiro.spring.boot.antisamy.web.servlet.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.shiro.spring.boot.antisamy.cache.AntiSamyWrapper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("HttpServletAntiSamyRequestWrapper Tests")
class HttpServletAntiSamyRequestWrapperTest {

    private HttpServletRequest createMockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(request.getParameterValues("key")).thenReturn(new String[]{"value"});
        when(request.getParameter("key")).thenReturn("value");
        when(request.getHeaders("X-Test")).thenReturn(Collections.enumeration(Collections.singletonList("headerValue")));
        when(request.getHeader("X-Test")).thenReturn("headerValue");
        when(request.getCookies()).thenReturn(null);
        when(request.getQueryString()).thenReturn("q=test");
        when(request.getRequestURI()).thenReturn("/test");
        return request;
    }

    @Test
    @DisplayName("Constructor creates non-null wrapper")
    void testConstructor() {
        HttpServletRequest request = createMockRequest();
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, null);
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        assertThat(wrapperReq).isNotNull();
    }

    @Test
    @DisplayName("getParameterMap returns map")
    void testGetParameterMap() {
        HttpServletRequest request = createMockRequest();
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, null);
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        Map<String, String[]> map = wrapperReq.getParameterMap();
        assertThat(map).isNotNull();
    }

    @Test
    @DisplayName("getParameterValues returns null for missing key")
    void testGetParameterValuesNull() {
        HttpServletRequest request = createMockRequest();
        when(request.getParameterValues("missing")).thenReturn(null);
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, null);
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        assertThat(wrapperReq.getParameterValues("missing")).isNull();
    }

    @Test
    @DisplayName("getParameter returns null for missing key")
    void testGetParameterNull() {
        HttpServletRequest request = createMockRequest();
        when(request.getParameter("missing")).thenReturn(null);
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, null);
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        assertThat(wrapperReq.getParameter("missing")).isNull();
    }

    @Test
    @DisplayName("getHeaders returns enumeration")
    void testGetHeaders() {
        HttpServletRequest request = createMockRequest();
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, new String[]{"X-Test"});
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        Enumeration<String> headers = wrapperReq.getHeaders("X-Test");
        assertThat(headers).isNotNull();
    }

    @Test
    @DisplayName("getHeader returns null for missing header")
    void testGetHeaderNull() {
        HttpServletRequest request = createMockRequest();
        when(request.getHeader("X-Missing")).thenReturn(null);
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, null);
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        assertThat(wrapperReq.getHeader("X-Missing")).isNull();
    }

    @Test
    @DisplayName("getHeader returns value for non-policy header")
    void testGetHeaderNonPolicy() {
        HttpServletRequest request = createMockRequest();
        when(request.getHeader("X-Other")).thenReturn("otherValue");
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, new String[]{"X-Test"});
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        assertThat(wrapperReq.getHeader("X-Other")).isEqualTo("otherValue");
    }

    @Test
    @DisplayName("getCookies returns null when no cookies")
    void testGetCookies() {
        HttpServletRequest request = createMockRequest();
        AntiSamyWrapper wrapper = new AntiSamyWrapper(null, null, 0, null);
        HttpServletAntiSamyRequestWrapper wrapperReq = new HttpServletAntiSamyRequestWrapper(wrapper, request);
        assertThat(wrapperReq.getCookies()).isNull();
    }
    @Test
    void cleansParameterMapWithoutMutatingServletValues() {
        String[] rawValues = {"first", "second"};
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameterMap()).thenReturn(Map.of("key", rawValues));
        HttpServletAntiSamyRequestWrapper wrapper = createWrapperWithDeterministicCleaning(request);

        Map<String, String[]> parameters = wrapper.getParameterMap();

        assertThat(parameters.get("key")).containsExactly("clean:first", "clean:second");
        assertThat(rawValues).containsExactly("first", "second");
        assertThatThrownBy(() -> parameters.put("other", new String[]{"value"}))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(wrapper.getParameterMap().get("key")).containsExactly("clean:first", "clean:second");
    }

    @Test
    void cleansCopiesOfCookiesWithoutMutatingServletCookies() {
        Cookie original = new Cookie("example", "first");
        original.setPath("/test");
        original.setHttpOnly(true);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{original});
        HttpServletAntiSamyRequestWrapper wrapper = createWrapperWithDeterministicCleaning(request);

        Cookie cleaned = wrapper.getCookies()[0];

        assertThat(cleaned.getValue()).isEqualTo("clean:first");
        assertThat(cleaned.getPath()).isEqualTo("/test");
        assertThat(cleaned.isHttpOnly()).isTrue();
        assertThat(original.getValue()).isEqualTo("first");
        assertThat(wrapper.getCookies()[0].getValue()).isEqualTo("clean:first");
    }

    private HttpServletAntiSamyRequestWrapper createWrapperWithDeterministicCleaning(HttpServletRequest request) {
        return new HttpServletAntiSamyRequestWrapper(new AntiSamyWrapper(null, null, 0, null), request) {
            @Override
            public String xssClean(String value) {
                return "clean:" + value;
            }
        };
    }

}

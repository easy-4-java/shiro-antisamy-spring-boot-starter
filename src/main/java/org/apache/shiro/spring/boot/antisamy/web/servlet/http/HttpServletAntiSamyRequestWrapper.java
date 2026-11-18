package org.apache.shiro.spring.boot.antisamy.web.servlet.http;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.apache.shiro.spring.boot.antisamy.cache.AntiSamyWrapper;
import org.apache.shiro.spring.boot.antisamy.utils.AntiSamyScanUtils;
import org.apache.shiro.spring.boot.antisamy.utils.XssScanUtils;

/**
 * RichText XSS(Cross Site Scripting)，request
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
public class HttpServletAntiSamyRequestWrapper extends HttpServletRequestWrapper {

	private AntiSamyWrapper antiSamyWrapper = null;
	
	/**
	 * Constructs a new http servlet anti samy request wrapper instance.
	 *
	 * @param antiSamyWrapper the anti samy wrapper
	 * @param request the request
	 */
	public HttpServletAntiSamyRequestWrapper(AntiSamyWrapper antiSamyWrapper,HttpServletRequest request) {
		super(request);
		this.antiSamyWrapper = antiSamyWrapper;
	}
	
	@Override
	/** Returns the parameter map.
	 * @return the result
	 */
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> cleanedParameters = new LinkedHashMap<>();
		super.getParameterMap().forEach((name, values) -> cleanedParameters.put(name, cleanParameterValues(values)));
		return Collections.unmodifiableMap(cleanedParameters);
	}

	@Override
	/** Returns the parameter values.
	 * @param name the name
	 * @return the result
	 */
	public String[] getParameterValues(String name) {
		return cleanParameterValues(super.getParameterValues(name));
	}

	private String[] cleanParameterValues(String[] rawValues) {
		if (Objects.isNull(rawValues)) {
			return null;
		}
		String[] cleanedValues = new String[rawValues.length];
		for (int i = 0; i < rawValues.length; i++) {
			cleanedValues[i] = xssClean(rawValues[i]);
		}
		return cleanedValues;
	}

	@Override
	/** Returns the parameter.
	 * @param name the name
	 * @return the result
	 */
	public String getParameter(String name) {
		String str = super.getParameter(name);
		if (str == null){
			return null;
		}
		return xssClean(str);
	}

	@Override
	/** Returns the headers.
	 * @param name the name
	 * @return the result
	 */
	public Enumeration<String> getHeaders(String name) {
		if(XssScanUtils.isXssHeader(antiSamyWrapper.getPolicyHeaders(), name)){
			return new AntiSamyEnumeration( super.getHeaders(name), antiSamyWrapper);
		}
        return super.getHeaders(name);
    }
	
	@Override
	/** Returns the header.
	 * @param name the name
	 * @return the result
	 */
	public String getHeader(String name) {
		String value = super.getHeader(name);
		if (value == null){
			return null;
		}
		if(XssScanUtils.isXssHeader(antiSamyWrapper.getPolicyHeaders(), name)){
			return xssClean(value);
		}
		return value;
	}
	
	@Override
	/** Returns the cookies.
	 * @return the result
	 */
	public Cookie[] getCookies() {
		Cookie[] existingCookies = super.getCookies();
		if (Objects.isNull(existingCookies)) {
			return null;
		}
		Cookie[] cleanedCookies = new Cookie[existingCookies.length];
		for (int i = 0; i < existingCookies.length; i++) {
			Cookie cookie = (Cookie) existingCookies[i].clone();
			cookie.setValue(xssClean(cookie.getValue()));
			cleanedCookies[i] = cookie;
		}
		return cleanedCookies;
	}

	@Override
	/** Returns the query string.
	 * @return the result
	 */
	public String getQueryString() {
		return xssClean(super.getQueryString());
	}

	/**
	 * xss Clean.
	 *
	 * @param taintedHTML the tainted h t m l
	 * @return the result
	 */
	public String xssClean(String taintedHTML) {
		return AntiSamyScanUtils.xssClean(_getHttpServletRequest(), antiSamyWrapper, taintedHTML);
	}
	
	/**
	 * _get HTTP Servlet Request.
	 *
	 * @return the result
	 */
	protected HttpServletRequest _getHttpServletRequest() {
		 return (HttpServletRequest) super.getRequest();
    }

}

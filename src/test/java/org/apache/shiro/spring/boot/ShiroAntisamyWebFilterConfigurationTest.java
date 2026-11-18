/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.shiro.spring.boot;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.boot.antisamy.web.filter.HttpServletRequestAntisamyFilter;
import org.apache.shiro.spring.boot.antisamy.web.servlet.http.HttpServletAntiSamyRequestWrapper;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {{ @link ShiroAntisamyWebFilterConfiguration }}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("ShiroAntisamyWebFilterConfiguration Tests")
class ShiroAntisamyWebFilterConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(BootConfiguration.class)
            .withBean(SecurityManager.class, () -> mock(SecurityManager.class))
            .withBean(ShiroFilterChainDefinition.class, DefaultShiroFilterChainDefinition::new);

    @Test
    void bootDiscoversFilterWhenEnabled() {
        contextRunner.withPropertyValues("shiro.antisamy.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(HttpServletRequestAntisamyFilter.class);
                    assertThat(context).hasBean("antisamy");
                });
    }

    @Test
    void enabledFilterLoadsBundledPolicyOnFirstRequest() {
        contextRunner.withPropertyValues("shiro.antisamy.enabled=true")
                .run(context -> {
                    HttpServletRequestAntisamyFilter filter = context.getBean(HttpServletRequestAntisamyFilter.class);
                    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/search");
                    request.addParameter("query", "safe text");

                    filter.doFilter(request, new MockHttpServletResponse(), (filteredRequest, response) -> {
                        assertThat(filteredRequest).isInstanceOf(HttpServletAntiSamyRequestWrapper.class);
                        assertThat(((HttpServletRequest) filteredRequest).getParameter("query")).isEqualTo("safe text");
                    });
                });
    }

    @Test
    void bootLeavesFilterDisabledByDefault() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(HttpServletRequestAntisamyFilter.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    static class BootConfiguration {
    }
}

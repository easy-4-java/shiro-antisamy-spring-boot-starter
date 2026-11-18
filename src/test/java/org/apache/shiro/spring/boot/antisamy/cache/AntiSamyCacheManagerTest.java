package org.apache.shiro.spring.boot.antisamy.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link AntiSamyCacheManager}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("AntiSamyCacheManager Tests")
class AntiSamyCacheManagerTest {

    @Test
    @DisplayName("getInstance returns singleton")
    void testGetInstance() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager policyCacheManager = PolicyCacheManager.getInstance(resolver);
        AntiSamyCacheManager instance = AntiSamyCacheManager.getInstance(policyCacheManager);
        assertThat(instance).isNotNull();
        AntiSamyCacheManager instance2 = AntiSamyCacheManager.getInstance(policyCacheManager);
        assertThat(instance).isSameAs(instance2);
    }

    @Test
    @DisplayName("destroy clears the antisamy cache")
    void testDestroy() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager policyCacheManager = PolicyCacheManager.getInstance(resolver);
        AntiSamyCacheManager manager = AntiSamyCacheManager.getInstance(policyCacheManager);
        manager.destroy();
        assertThat(AntiSamyCacheManager.COMPLIED_ANTISAMY).isEmpty();
    }
    @Test
    void reusesCompiledPolicyAndKeepsRequestOptionsSeparate() throws Exception {
        PolicyCacheManager policies = PolicyCacheManager.getInstance(new PathMatchingResourcePatternResolver());
        AntiSamyCacheManager manager = AntiSamyCacheManager.getInstance(policies);

        AntiSamyWrapper first = manager.getDefaultAntiSamyWrapper(AntiSamy.DOM, new String[]{"X-First"});
        AntiSamyWrapper second = manager.getDefaultAntiSamyWrapper(AntiSamy.SAX, new String[]{"X-Second"});

        assertThat(second.getPolicy()).isSameAs(first.getPolicy());
        assertThat(second.getAntiSamy()).isSameAs(first.getAntiSamy());
        assertThat(first.getPolicyHeaders()).containsExactly("X-First");
        assertThat(second.getPolicyHeaders()).containsExactly("X-Second");
        assertThat(first.getScanType()).isEqualTo(AntiSamy.DOM);
        assertThat(second.getScanType()).isEqualTo(AntiSamy.SAX);
        manager.destroy();
        assertThat(manager.getDefaultAntiSamyWrapper(AntiSamy.DOM, null).getPolicy()).isNotSameAs(first.getPolicy());
    }

    @Test
    void reportsMissingPolicyBeforeBuildingScanner() {
        PolicyCacheManager policies = PolicyCacheManager.getInstance(new PathMatchingResourcePatternResolver());
        AntiSamyCacheManager manager = AntiSamyCacheManager.getInstance(policies);

        assertThatThrownBy(() -> manager.getXssAntiSamyWrapper((Policy) null, AntiSamy.DOM, null))
                .isInstanceOf(PolicyException.class)
                .hasMessageContaining("Policy Not Found");
    }

}

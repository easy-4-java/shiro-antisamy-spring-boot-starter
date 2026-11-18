package org.apache.shiro.spring.boot.antisamy.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.owasp.validator.html.Policy;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PolicyCacheManager}.
 *
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
 * @since 1.0.0
 */
@DisplayName("PolicyCacheManager Tests")
class PolicyCacheManagerTest {

    @Test
    @DisplayName("getInstance returns singleton")
    void testGetInstance() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager instance = PolicyCacheManager.getInstance(resolver);
        assertThat(instance).isNotNull();
        PolicyCacheManager instance2 = PolicyCacheManager.getInstance(resolver);
        assertThat(instance).isSameAs(instance2);
    }

    @Test
    @DisplayName("getXssPolicy returns null for blank path")
    void testGetXssPolicyBlankPath() throws Exception {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager manager = PolicyCacheManager.getInstance(resolver);
        assertThat(manager.getXssPolicy("")).isNull();
        assertThat(manager.getXssPolicy((String) null)).isNull();
    }

    @Test
    @DisplayName("getXssPolicy returns null for unreadable resource")
    void testGetXssPolicyUnreadable() throws Exception {
        ResourcePatternResolver resolver = mock(ResourcePatternResolver.class);
        Resource resource = mock(Resource.class);
        when(resolver.getResource(anyString())).thenReturn(resource);
        when(resource.isReadable()).thenReturn(false);
        PolicyCacheManager manager = new PolicyCacheManager(resolver);
        assertThat(manager.getXssPolicy("classpath:test.xml")).isNull();
    }

    @Test
    @DisplayName("getXssPolicy returns null for null resource")
    void testGetXssPolicyNullResource() throws Exception {
        ResourcePatternResolver resolver = mock(ResourcePatternResolver.class);
        when(resolver.getResource(anyString())).thenReturn(null);
        PolicyCacheManager manager = new PolicyCacheManager(resolver);
        assertThat(manager.getXssPolicy("classpath:test.xml")).isNull();
    }

    @Test
    @DisplayName("getXssPolicy(URL) returns null for null url")
    void testGetXssPolicyNullUrl() throws Exception {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager manager = PolicyCacheManager.getInstance(resolver);
        assertThat(manager.getXssPolicy((java.net.URL) null)).isNull();
    }

    @Test
    @DisplayName("getXssPolicy(File) returns null for null file")
    void testGetXssPolicyNullFile() throws Exception {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager manager = PolicyCacheManager.getInstance(resolver);
        assertThat(manager.getXssPolicy((java.io.File) null)).isNull();
    }

    @Test
    @DisplayName("getXssPolicy(File) returns null for non-existent file")
    void testGetXssPolicyNonExistentFile() throws Exception {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager manager = PolicyCacheManager.getInstance(resolver);
        assertThat(manager.getXssPolicy(new java.io.File("/nonexistent.xml"))).isNull();
    }

    @Test
    @DisplayName("destroy clears the policy cache")
    void testDestroy() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        PolicyCacheManager manager = PolicyCacheManager.getInstance(resolver);
        manager.destroy();
        assertThat(PolicyCacheManager.COMPLIED_POLICY).isEmpty();
    }

    @Test
    void sameNamedPolicyFilesKeepTheirOwnDirectives(@TempDir Path directory) throws Exception {
        Path first = writePolicy(Files.createDirectory(directory.resolve("first")), "2000");
        Path second = writePolicy(Files.createDirectory(directory.resolve("second")), "3000");
        PolicyCacheManager manager = PolicyCacheManager.getInstance(new PathMatchingResourcePatternResolver());

        Policy firstPolicy = manager.getXssPolicy(first.toFile());
        Policy secondPolicy = manager.getXssPolicy(second.toFile());
        assertThat(firstPolicy.getDirective("maxInputSize")).isEqualTo("2000");
        assertThat(secondPolicy.getDirective("maxInputSize")).isEqualTo("3000");
        assertThat(manager.getXssPolicy(second.toFile())).isSameAs(secondPolicy);
    }

    @Test
    void repeatedUrlLookupReturnsCachedPolicyUntilDestroy(@TempDir Path directory) throws Exception {
        Path file = writePolicy(directory, "2000");
        URL url = file.toUri().toURL();
        PolicyCacheManager manager = PolicyCacheManager.getInstance(new PathMatchingResourcePatternResolver());

        Policy first = manager.getXssPolicy(url);
        Files.delete(file);
        assertThat(manager.getXssPolicy(url)).isSameAs(first);
        manager.destroy();
        writePolicy(directory, "3000");
        assertThat(manager.getXssPolicy(url).getDirective("maxInputSize")).isEqualTo("3000");
    }

    private static Path writePolicy(Path directory, String maxInputSize) throws Exception {
        try (InputStream input = PolicyCacheManagerTest.class.getResourceAsStream("/antisamy-policy-test.xml")) {
            String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            return Files.writeString(directory.resolve("policy.xml"),
                xml.replace("name=\"maxInputSize\" value=\"2000\"", "name=\"maxInputSize\" value=\"" + maxInputSize + "\""));
        }
    }
}

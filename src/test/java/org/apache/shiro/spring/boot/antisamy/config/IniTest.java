package org.apache.shiro.spring.boot.antisamy.config;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@DisplayName("Ini Tests")
class IniTest {

    @Test
    @DisplayName("Default constructor creates empty Ini")
    void testDefaultConstructor() {
        Ini ini = new Ini();
        assertThat(ini).isNotNull();
        assertThat(ini.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("load parses section header")
    void testLoadSection() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        assertThat(ini.getSection("urls")).isNotNull();
    }

    @Test
    @DisplayName("load parses key-value pairs")
    void testLoadKeyValue() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section).isNotNull();
        assertThat(section.get("/test")).isEqualTo("anon");
    }

    @Test
    @DisplayName("load skips comments")
    void testLoadComments() throws Exception {
        Ini ini = new Ini();
        ini.load("# comment\n; comment\n[urls]\n/test = anon\n");
        assertThat(ini.getSection("urls")).isNotNull();
    }

    @Test
    @DisplayName("getSection returns null for non-existent section")
    void testGetSectionNull() {
        Ini ini = new Ini();
        assertThat(ini.getSection("nonexistent")).isNull();
    }

    @Test
    @DisplayName("getSectionNames returns all section names")
    void testGetSectionNames() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n[main]\nkey = value\n");
        assertThat(ini.getSectionNames()).contains("urls", "main");
    }

    @Test
    @DisplayName("Section get returns null for non-existent key")
    void testSectionGetNull() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section.get("nonexistent")).isNull();
    }

    @Test
    @DisplayName("Section put and get")
    void testSectionPutGet() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        section.put("/new", "authc");
        assertThat(section.get("/new")).isEqualTo("authc");
    }

    @Test
    @DisplayName("Section size")
    void testSectionSize() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n/login = authc\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("size returns number of sections")
    void testSize() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        assertThat(ini.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("isEmpty returns true for empty ini")
    void testIsEmpty() {
        Ini ini = new Ini();
        assertThat(ini.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("containsKey works for sections")
    void testContainsKey() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        assertThat(ini.containsKey("urls")).isTrue();
        assertThat(ini.containsKey("other")).isFalse();
    }

    @Test
    @DisplayName("Constants have expected values")
    void testConstants() {
        assertThat(Ini.DEFAULT_SECTION_NAME).isEqualTo("");
        assertThat(Ini.DEFAULT_CHARSET_NAME).isEqualTo("UTF-8");
        assertThat(Ini.COMMENT_POUND).isEqualTo("#");
        assertThat(Ini.COMMENT_SEMICOLON).isEqualTo(";");
        assertThat(Ini.SECTION_PREFIX).isEqualTo("[");
        assertThat(Ini.SECTION_SUFFIX).isEqualTo("]");
    }

    @Test
    @DisplayName("load handles multi-line values")
    void testMultiLineValues() throws Exception {
        Ini ini = new Ini();
        String continued = "[urls]\n/search = classpath:policies/" + '\\' + "\n  search.xml\n";
        ini.load(continued);

        assertThat(ini.getSectionProperty("urls", "/search")).isEqualTo("classpath:policies/search.xml");
    }

    @Test
    @DisplayName("Section keySet returns keys")
    void testSectionKeySet() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section.keySet()).contains("/test");
    }

    @Test
    @DisplayName("Section values returns values")
    void testSectionValues() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section.values()).contains("anon");
    }

    @Test
    @DisplayName("Section entrySet returns entries")
    void testSectionEntrySet() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section.entrySet()).hasSize(1);
    }

    @Test
    @DisplayName("Section containsKey")
    void testSectionContainsKey() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section.containsKey("/test")).isTrue();
        assertThat(section.containsKey("/other")).isFalse();
    }

    @Test
    @DisplayName("Section containsValue")
    void testSectionContainsValue() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        assertThat(section.containsValue("anon")).isTrue();
        assertThat(section.containsValue("authc")).isFalse();
    }

    @Test
    @DisplayName("Section remove")
    void testSectionRemove() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        section.remove("/test");
        assertThat(section.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("Section clear")
    void testSectionClear() throws Exception {
        Ini ini = new Ini();
        ini.load("[urls]\n/test = anon\n");
        Ini.Section section = ini.getSection("urls");
        section.clear();
        assertThat(section.isEmpty()).isTrue();
    }
    @Test
    void loadsUtf8PolicyNamesAndClosesInputStream() throws Exception {
        Ini ini = new Ini();
        byte[] content = "[urls]\n/产品/** = classpath:策略.xml\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream input = spy(new ByteArrayInputStream(content));

        ini.load(input);

        assertThat(ini.getSectionProperty("urls", "/产品/**")).isEqualTo("classpath:策略.xml");
        verify(input).close();
    }

    @Test
    void copiesNamedAndDefaultSectionsIndependently() {
        Ini defaults = new Ini();
        defaults.setSectionProperty(null, "/**", "classpath:default.xml");
        defaults.setSectionProperty(" urls ", "/health", "classpath:health.xml");
        Ini copy = new Ini(defaults);

        assertThat(copy).isEqualTo(defaults);
        assertThat(copy.hashCode()).isEqualTo(defaults.hashCode());
        assertThat(copy.toString()).isEqualTo("sections=<default>,urls");
        assertThat(copy.getSectionProperty("urls", "/missing", "fallback")).isEqualTo("fallback");
        copy.setSectionProperty("urls", "/health", "classpath:override.xml");
        assertThat(copy.getSectionProperty("urls", "/health", "fallback")).isEqualTo("classpath:override.xml");
        assertThat(copy.removeSection(" urls ")).containsEntry("/health", "classpath:override.xml");
        assertThat(copy).doesNotContainKey("urls");
        assertThat(defaults.getSectionProperty("urls", "/health")).isEqualTo("classpath:health.xml");
        assertThat(copy.getSectionProperty("", "/**")).isEqualTo("classpath:default.xml");
    }

    @Test
    void rejectsPolicyMappingsWithoutAValue() {
        Ini ini = new Ini();

        assertThatIllegalArgumentException().isThrownBy(() -> ini.load("[urls]\n/health=\n"));
    }

}

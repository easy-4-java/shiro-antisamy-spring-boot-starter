package org.apache.shiro.spring.boot.antisamy.utils;

import java.io.StringWriter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Entities Tests")
class EntitiesTest {

    @Test
    @DisplayName("HTML40 entityName returns null for unknown char")
    void testEntityNameUnknown() {
        assertThat(Entities.HTML40.entityName((char) 0)).isNull();
    }

    @Test
    @DisplayName("HTML40 entityName returns name for known char")
    void testEntityNameKnown() {
        // ampersand
        String name = Entities.HTML40.entityName('&');
        assertThat(name).isNotNull();
    }

    @Test
    @DisplayName("HTML40 entityValue returns -1 for unknown name")
    void testEntityValueUnknown() {
        assertThat(Entities.HTML40.entityValue("unknown")).isEqualTo(-1);
    }

    @Test
    @DisplayName("HTML40 entityValue returns value for known name")
    void testEntityValueKnown() {
        int value = Entities.HTML40.entityValue("amp");
        assertThat(value).isEqualTo('&');
    }
    @Test
    void encodesNamedAndNumericEntitiesThroughBothOverloads() throws Exception {
        Entities entities = Entities.HTML40;
        String input = "A & Ω 中";
        String encoded = "A &amp; &Omega; &#20013;";
        StringWriter output = new StringWriter();

        entities.escape(output, input);

        assertThat(output.toString()).isEqualTo(encoded);
        assertThat(entities.escape(input)).isEqualTo(encoded);
        assertThat(entities.unescape(encoded)).isEqualTo(input);
    }

    @Test
    void preservesUnrecognizedEntitiesAndDecodesValidAdjacentEntities() throws Exception {
        Entities entities = Entities.HTML40;
        String input = "plain &unknown; &#abc; &#x110000; &; &#; &&amp; &#x41; &#X42; &#67; &tail";
        String expected = "plain &unknown; &#abc; &#x110000; &; &#; && A B C &tail";
        StringWriter output = new StringWriter();

        entities.unescape(output, "prefix ");
        entities.unescape(output, input);

        assertThat(output.toString()).isEqualTo("prefix " + expected);
        assertThat(entities.unescape(input)).isEqualTo(expected);
        assertThat(entities.unescape("plain")).isEqualTo("plain");
    }

    @Test
    void supportsBothLookupDirectionsAfterTheBackingArrayGrows() {
        Entities entities = new Entities(new Entities.ArrayEntityMap(1));
        entities.addEntities(new String[][]{{"first", "200"}, {"second", "300"}});

        assertThat(entities.entityName(300)).isEqualTo("second");
        assertThat(entities.entityValue("first")).isEqualTo(200);
        assertThat(entities.entityName(400)).isNull();
        assertThat(entities.entityValue("missing")).isEqualTo(-1);
    }

    @Test
    void resolvesOutOfOrderBinaryEntriesAndKeepsFirstNameOnDuplicateValues() {
        Entities entities = new Entities(new Entities.BinaryEntityMap());
        entities.addEntity("middle", 200);
        entities.addEntity("last", 300);
        entities.addEntity("first", 100);
        entities.addEntity("duplicateMiddle", 200);
        entities.addEntity("duplicateFirst", 100);

        assertThat(entities.entityName(100)).isEqualTo("first");
        assertThat(entities.entityName(200)).isEqualTo("middle");
        assertThat(entities.entityName(300)).isEqualTo("last");
        assertThat(entities.entityName(400)).isNull();
        assertThat(entities.entityValue("duplicateFirst")).isEqualTo(-1);
    }

    @Test
    void supportsBothLookupDirectionsInHashAndTreeMaps() {
        for (Entities.EntityMap map : new Entities.EntityMap[]{new Entities.HashEntityMap(),
                new Entities.TreeEntityMap()}) {
            Entities entities = new Entities(map);
            entities.addEntity("sample", 256);

            assertThat(entities.entityName(256)).isEqualTo("sample");
            assertThat(entities.entityValue("sample")).isEqualTo(256);
            assertThat(entities.entityName(300)).isNull();
            assertThat(entities.entityValue("missing")).isEqualTo(-1);
        }
    }

}

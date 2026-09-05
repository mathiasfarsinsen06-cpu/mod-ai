package com.mathiasfarsinsen.modai;

import com.mathiasfarsinsen.modai.knowledge.JsonKnowledgeProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonKnowledgeProviderLoadingTest {

    @Test
    void loadsBlueprintsAndGuidesFromJsonFilesOnDisk(@TempDir Path tempDir) throws IOException {
        Path blueprintFile = tempDir.resolve("farms.json");
        Files.writeString(blueprintFile, """
                {
                  "blueprints": [
                    { "id": "basic_farm", "name": "Basic Farm", "category": "farm",
                      "description": "A simple wheat farm.", "tier": 1,
                      "requiredResources": { "WOOD": 5 } }
                  ],
                  "guides": [
                    { "id": "farming_101", "topic": "Farming basics", "steps": ["Till soil", "Plant seeds"] }
                  ]
                }
                """, StandardCharsets.UTF_8);

        JsonKnowledgeProvider provider = JsonKnowledgeProvider.loadFromDirectory(tempDir);

        assertEquals(1, provider.getBlueprints().size());
        assertEquals("basic_farm", provider.getBlueprints().get(0).getId());
        assertEquals(1, provider.getGuides().size());
        assertEquals("Farming basics", provider.getGuides().get(0).getTopic());
    }

    @Test
    void malformedJsonFileIsSkippedNotFatal(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("broken.json"), "{ this is not valid json ", StandardCharsets.UTF_8);

        JsonKnowledgeProvider provider = JsonKnowledgeProvider.loadFromDirectory(tempDir);

        assertEquals(0, provider.getBlueprints().size());
        assertEquals(0, provider.getGuides().size());
    }

    @Test
    void missingDirectoryFallsBackToEmptyLibrary() {
        JsonKnowledgeProvider provider = JsonKnowledgeProvider.loadFromDirectory(Path.of("/nonexistent/path/xyz"));

        assertEquals(0, provider.getBlueprints().size());
    }
}

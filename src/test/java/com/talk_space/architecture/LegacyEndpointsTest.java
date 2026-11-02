package com.talk_space.architecture;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
class LegacyEndpointsTest {
    @Test void controllerMappingsMatchTheConnectOnlyContract() throws Exception {
        Pattern pattern=Pattern.compile("@(?:RequestMapping|GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|MessageMapping|SubscribeMapping)\\([^)]*\\)|public\\s+[^{};=]+?\\([^{};]*?\\)\\s*\\{");
        var json=new ObjectMapper();
        for (String line : Files.readAllLines(Path.of("src/test/resources/contracts/legacy-endpoints.jsonl"))) {
            String[] parts=line.split("\t",2);
            String source=Files.readString(Path.of("src/main/java").resolve(parts[0]));
            source=source.replaceAll("(?s)/\\*.*?\\*/|//[^\n]*", "");
            List<String> actual=new ArrayList<>();var matcher=pattern.matcher(source);
            while(matcher.find()) actual.add(matcher.group().replaceAll("\\s+"," ").trim());
            assertEquals(json.readTree(parts[1]),json.valueToTree(actual),parts[0]);
        }
    }
}

package com.htyoudao.youdao.module.bpm.service.learningtask;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningMaterialContentParserTest {

    private final LearningMaterialContentParser parser = new LearningMaterialContentParser();

    @Test
    void richTextChangeDoesNotChangeAttachmentFingerprint() {
        String before = "[{\"type\":\"richText\",\"content\":\"<p>A</p>\"},"
                + "{\"type\":\"document\",\"file\":{\"name\":\"制度.pdf\","
                + "\"url\":\"https://files.example/a.pdf\",\"size\":100,\"type\":\"application/pdf\",\"ext\":\"pdf\"}}]";
        String after = before.replace("<p>A</p>", "<p>文案已修改</p>");

        assertEquals(parser.parse(before).getAttachmentFingerprint(),
                parser.parse(after).getAttachmentFingerprint());
    }

    @Test
    void attachmentChangeChangesFingerprint() {
        String before = "[{\"type\":\"image\",\"url\":\"https://files.example/a.png\"}]";
        String after = "[{\"type\":\"image\",\"url\":\"https://files.example/b.png\"}]";

        assertNotEquals(parser.parse(before).getAttachmentFingerprint(),
                parser.parse(after).getAttachmentFingerprint());
    }

    @Test
    void documentMetadataChangeChangesFileVersionButKeepsStableFileKey() {
        String before = "[{\"type\":\"document\",\"file\":{\"name\":\"制度.pdf\","
                + "\"url\":\"https://files.example/a.pdf\",\"size\":100}}]";
        String after = before.replace("\"size\":100", "\"size\":101");
        var oldFile = parser.parse(before).getFiles().get(0);
        var newFile = parser.parse(after).getFiles().get(0);

        assertEquals(oldFile.getFileKey(), newFile.getFileKey());
        assertNotEquals(oldFile.getFileVersion(), newFile.getFileVersion());
    }

    @Test
    void malformedOrMissingAttachmentUrlIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("{}"));
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("[{\"type\":\"video\",\"url\":\"\"}]"));
    }
}

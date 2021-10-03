package com.github.collector.service.validator;

import com.github.collector.service.domain.TargetLocation;
import com.github.collector.service.validator.domain.DocumentType;
import lombok.RequiredArgsConstructor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DocumentValidator implements Validator {

    private final Parser documentParser;

    @Override
    public boolean validate(final TargetLocation targetLocation, final String extension) {
        final DocumentType documentType = Arrays.stream(DocumentType.values())
                .filter(documentType1 -> documentType1.getFileExtension().equals(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown extension to validate: " + extension + "!"));

        final ContentHandler contentHandler = buildContentHandler();
        final Metadata metadata = buildMetadata(documentType);
        final ParseContext context = buildParseContext();

        try {
            documentParser.parse(Files.newInputStream(targetLocation.getPath()), contentHandler, metadata, context);
        } catch (IOException | SAXException | TikaException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isValidatorFor(final String extension) {
        return "pdf".equals(extension)
                || "doc".equals(extension) || "docx".equals(extension)
                || "xls".equals(extension) || "xlsx".equals(extension)
                || "ppt".equals(extension) || "pptx".equals(extension)
                || "xml".equals(extension)
                || "epub".equals(extension);
    }

    private ContentHandler buildContentHandler() {
        return new BodyContentHandler(-1);
    }

    private Metadata buildMetadata(final DocumentType documentType) {
        final Metadata metadata = new Metadata();

        metadata.add("Content-Type", documentType.getMimeType());

        return metadata;
    }

    private ParseContext buildParseContext() {
        return new ParseContext();
    }
}

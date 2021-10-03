package com.github.filecollector.service.validator.configuration;

import com.github.filecollector.service.validator.domain.DocumentType;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParserDecorator;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.microsoft.rtf.RTFParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.xml.XMLParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class TikaConfiguration {

    @Bean
    public Parser parser() {
        return new CompositeParser(new MediaTypeRegistry(),
                ParserDecorator.withTypes(new PDFParser(), Set.of(
                        MediaType.parse(DocumentType.PDF.getMimeType())
                )),
                ParserDecorator.withTypes(new RTFParser(), Set.of(
                        MediaType.parse(DocumentType.RTF.getMimeType())
                )),
                ParserDecorator.withTypes(new EpubParser(), Set.of(
                        MediaType.parse(DocumentType.EPUB.getMimeType())
                )),
                ParserDecorator.withTypes(new OfficeParser(), Set.of(
                        MediaType.parse(DocumentType.DOC.getMimeType()),
                        MediaType.parse(DocumentType.PPT.getMimeType()),
                        MediaType.parse(DocumentType.XLS.getMimeType())
                )),
                ParserDecorator.withTypes(new OOXMLParser(), Set.of(
                        MediaType.parse(DocumentType.DOCX.getMimeType()),
                        MediaType.parse(DocumentType.PPTX.getMimeType()),
                        MediaType.parse(DocumentType.XLSX.getMimeType())
                )),
                ParserDecorator.withTypes(new XMLParser(), Set.of(
                        MediaType.parse(DocumentType.XML.getMimeType())
                ))
        );
    }
}

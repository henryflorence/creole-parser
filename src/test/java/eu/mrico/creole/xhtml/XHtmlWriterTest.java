package eu.mrico.creole.xhtml;

import eu.mrico.creole.Creole;
import eu.mrico.creole.CreoleException;
import eu.mrico.creole.ast.Link;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import eu.mrico.creole.ast.Heading;
import eu.mrico.creole.ast.Document;
import eu.mrico.creole.ast.Image;
import eu.mrico.creole.ast.Paragraph;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class XHtmlWriterTest {

    public XHtmlWriterTest() {
    }

    @Test
    public void htmlWrtier() throws CreoleException, FileNotFoundException {
        InputStream in = getClass().getResourceAsStream("/creole1.0test.txt");
        assertNotNull(in);

        Document doc = Creole.parse(in);

        XHtmlWriter writer = new XHtmlWriter();
        writer.addCssClass("p", new String[] { "creole", "test" });
        writer.addCssClass("pre", "code");

        writer.setDecorator(Paragraph.class, new DivElementDecorator("section"));
        writer.setDecorator(Image.class, new DivElementDecorator("image"));
        writer.setDecorator(Link.class, new DivElementDecorator("link"));

        writer.write(doc, new FileOutputStream("target/creole1.0test.html"));
    }

    @Test
    public void testCssClassCustomization() throws CreoleException, XMLStreamException {

        Document doc = Creole.parse("== Hello World");
        
        XMLStreamWriter xw = mock(XMLStreamWriter.class);

        XHtmlWriter writer = new XHtmlWriter();
        writer.addCssClass("h2", "heading");
        
        writer.write(doc, xw);
        
        verify(xw).writeAttribute("class", "heading");
    }

    @Test
    public void testElementDecoration() throws CreoleException, XMLStreamException {
        Document doc = Creole.parse("== Hello World\n** Bold Text");

        XMLStreamWriter xw = mock(XMLStreamWriter.class);
        XHtmlElementDecorator decorator = mock(XHtmlElementDecorator.class);

        XHtmlWriter writer = new XHtmlWriter();
        writer.setDecorator(Heading.class, decorator);

        writer.write(doc, xw);

        verify(decorator).before(any(Heading.class), any(XMLStreamWriter.class));
        verify(decorator).after(any(Heading.class), any(XMLStreamWriter.class));
    }

    @Test
    public void testElementDecoration2() throws CreoleException, XMLStreamException {

        // create anonymous decorator
        XHtmlElementDecorator<Heading> decorator = new XHtmlElementDecorator<Heading>() {

            @Override
            public void before(Heading element, XMLStreamWriter writer) throws XMLStreamException {
                writer.writeStartElement("div");
                writer.writeAttribute("class", "heading");
            }

            @Override
            public void after(Heading element, XMLStreamWriter writer) throws XMLStreamException {
                writer.writeEndElement();
            }
        };

        Document doc = Creole.parse("== Hello World\n** Bold Text");
        
        XMLStreamWriter xw = mock(XMLStreamWriter.class);
        
        XHtmlWriter writer = new XHtmlWriter();
        writer.setDecorator(Heading.class, decorator);

        writer.write(doc, xw);

        verify(xw).writeStartElement("div");
    }
    
    @Test
    public void testElementAnchor() throws CreoleException, XMLStreamException {

        // create anonymous decorator
        XHtmlElementDecorator<Link> decorator = new XHtmlElementDecorator<Link>() {

            @Override
            public void before(Link element, XMLStreamWriter writer) throws XMLStreamException {
                writer.writeAttribute("target", "_blank");
            }

            @Override
            public void after(Link element, XMLStreamWriter writer) throws XMLStreamException {

            }
        };

        Document doc = Creole.parse("[[ http://www.google.com | google ]]");
        
        OutputStream out = new ByteArrayOutputStream();
        
        XHtmlWriter writer = new XHtmlWriter();
        writer.setDecorator(Link.class, decorator);

        writer.write(doc, out);
        
        assertEquals(out.toString(), "<a target=\"_blank\" href=\"http://www.google.com\">google</a>");
    }
}
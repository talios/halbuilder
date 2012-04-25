package com.theoryinpractise.halbuilder.impl.xml;

import com.google.common.base.Optional;
import com.theoryinpractise.halbuilder.spi.Link;
import com.theoryinpractise.halbuilder.spi.ReadableResource;
import com.theoryinpractise.halbuilder.spi.Renderer;
import com.theoryinpractise.halbuilder.spi.Resource;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.theoryinpractise.halbuilder.impl.api.Support.HREF;
import static com.theoryinpractise.halbuilder.impl.api.Support.HREFLANG;
import static com.theoryinpractise.halbuilder.impl.api.Support.LINK;
import static com.theoryinpractise.halbuilder.impl.api.Support.NAME;
import static com.theoryinpractise.halbuilder.impl.api.Support.REL;
import static com.theoryinpractise.halbuilder.impl.api.Support.SELF;
import static com.theoryinpractise.halbuilder.impl.api.Support.TITLE;


public class XmlRenderer<T> implements Renderer<T> {

    public Optional<T> render(ReadableResource resource, Writer writer) {
        final Element element = renderElement(resource, null);
        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(element, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.absent();
    }

    private Element renderElement(ReadableResource resource, String embeddedRel) {

        final Link resourceLink = resource.getResourceLink();
        final String href = resourceLink.getHref();

        // Create the root element
        final Element resourceElement = new Element("resource");
        resourceElement.setAttribute("href", href);
        if (embeddedRel != null) {
            resourceElement.setAttribute("rel", embeddedRel);
        }

        // Only add namespaces to non-embedded resources
        if (embeddedRel == null) {
            for (Map.Entry<String, String> entry : resource.getNamespaces().entrySet()) {
                resourceElement.addNamespaceDeclaration(
                        Namespace.getNamespace(entry.getKey(), entry.getValue()));
            }
        }

        //add a comment
//        resourceElement.addContent(new Comment("Description of a resource"));

        // add links
        List<Link> links = resource.getLinks();
        for (Link link : links) {
            Element linkElement = new Element(LINK);
            if (!link.getRel().contains(SELF)) {
                linkElement.setAttribute(REL, link.getRel());
                linkElement.setAttribute(HREF, link.getHref());
                if (link.getName().isPresent()) {
                    linkElement.setAttribute(NAME, link.getName().get());
                }
                if (link.getTitle().isPresent()) {
                    linkElement.setAttribute(TITLE, link.getTitle().get());
                }
                if (link.getHreflang().isPresent()) {
                    linkElement.setAttribute(HREFLANG, link.getHreflang().get());
                }
                resourceElement.addContent(linkElement);
            }
        }

        // add properties
        for (Map.Entry<String, Object> entry : resource.getProperties().entrySet()) {
            Element propertyElement = new Element(entry.getKey());
            propertyElement.setContent(new Text(entry.getValue().toString()));
            resourceElement.addContent(propertyElement);
        }

        // add subresources
        for (Entry<String, List<Resource>> resourceEntry : resource.getResources().entrySet()) {
            for (Resource subResource : resourceEntry.getValue()) {
                Element subResourceElement = renderElement(subResource, resourceEntry.getKey());
                resourceElement.addContent(subResourceElement);
            }
        }

        return resourceElement;
    }

}

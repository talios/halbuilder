package com.theoryinpractise.halbuilder.html;

import com.google.common.base.Optional;
import com.googlecode.jatl.Html;
import com.googlecode.jatl.Indenter;
import com.googlecode.jatl.MarkupBuilder;
import com.googlecode.jatl.SimpleIndenter;
import com.theoryinpractise.halbuilder.ReadableResource;
import com.theoryinpractise.halbuilder.Renderer;

import java.io.Writer;
import java.util.Map;

public class HtmlMicroDataRenderer<T> implements Renderer<T> {

//    public static class Section extends com.googlecode.jatl.HtmlBuilder<Section> {
//        @Override
//        protected Section getSelf() {
//            return this;
//        }
//    }


    public Optional<T> render(final ReadableResource resource, Writer writer) {

        Html res = new Html(writer)
                .indent(new SimpleIndenter("\n", "  ", null, null))
                .div();
        renderResource(res, resource);
        res.endAll();

        return Optional.absent();
    }

    private void renderResource(Html res, ReadableResource resource) {
        res.attr("itemscope", "").attr("itemtype", "hal...");

        for (Map.Entry<String, String> entry : resource.getLinks().entries()) {
            res.a().rel(entry.getKey()).href(entry.getValue()).text(entry.getValue()).end();
        }

        Html properties = res.dl();
        properties.id("properties");
        for (Map.Entry<String, Object> entry : resource.getProperties().entrySet()) {
            properties.start("dt", MarkupBuilder.TagClosingPolicy.PAIR).text(entry.getKey()).end();

            properties.dd().attr("itemprop", entry.getKey()).text(entry.getValue().toString())
               .end();
        }

        for (Map.Entry<String, ReadableResource> entry : resource.getResources().entries()) {
            Html subRes = res.div();
            subRes.rel(entry.getKey());
            renderResource(subRes, entry.getValue());
            subRes.end();
        }


    }
}

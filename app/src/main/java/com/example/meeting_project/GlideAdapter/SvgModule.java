package com.example.meeting_project.GlideAdapter;

import android.content.Context;
import android.graphics.drawable.PictureDrawable;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.module.AppGlideModule;
import com.caverock.androidsvg.SVG;

import java.io.IOException;
import java.io.InputStream;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.SimpleResource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.caverock.androidsvg.SVGParseException;

@GlideModule
public class SvgModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.register(SVG.class, PictureDrawable.class, new SvgDrawableTranscoder())
                .append(InputStream.class, SVG.class, new SvgDecoder());
    }

    // Prevents Glide from scanning the manifest for modules
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    public static class SvgDecoder implements ResourceDecoder<InputStream, SVG> {
        @Override
        public boolean handles(@NonNull InputStream source, @NonNull Options options) {
            return true;
        }

        @Override
        public Resource<SVG> decode(@NonNull InputStream source, int width, int height, @NonNull Options options) {
            try {
                SVG svg = SVG.getFromInputStream(source);
                return new SimpleResource<>(svg);
            } catch (SVGParseException e) {
                try {
                    throw new Exception(e.getMessage());
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public static class SvgDrawableTranscoder implements ResourceTranscoder<SVG, PictureDrawable> {
        @Override
        public Resource<PictureDrawable> transcode(@NonNull Resource<SVG> toTranscode, @NonNull Options options) {
            SVG svg = toTranscode.get();
            PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
            return new SimpleResource<>(drawable);
        }
    }
}

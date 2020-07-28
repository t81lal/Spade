package com.krakenrs.spade.app.asm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.NonNull;

public class KlassScene {
    private static final Logger LOGGER = LoggerFactory.getLogger(KlassScene.class);

    private final Collection<KlassSource> sources;

    public KlassScene() {
        sources = new HashSet<>();
    }
    
    public Stream<Klass> getKlasses() {
        Stream<Klass> stream = Stream.empty();
        for(KlassSource source : sources) {
            stream = Stream.concat(stream, source.getKlasses().stream());
        }
        return stream;
    }

    public Klass findKlass(@NonNull String name) {
        name = name.replace(".", "/");
        for (KlassSource source : sources) {
            if (source.hasKlass(name)) {
                // LOGGER.trace("Found loaded klass: {}", name);
                return source.getKlass(name);
            }
        }
        LOGGER.info("Couldn't find loaded klass: {}", name);
        return null;
    }

    private void addSource(KlassSource source) {
        sources.add(source);
    }

    public void addJarSource(File file, int asmOptions) throws IOException {
        LOGGER.info("Loading classes from: {}", file.toString());
        String name = file.getName();
        addJarSource(name, new FileInputStream(file), asmOptions);
    }

    public void addJarSource(String name, InputStream is, int asmOptions) throws IOException {
        KlassSource source = new KlassSource(name);
        source.loadFromJar(is, asmOptions, true);
        addSource(source);
    }
}

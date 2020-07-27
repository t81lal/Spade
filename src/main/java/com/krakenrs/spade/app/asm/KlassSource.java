package com.krakenrs.spade.app.asm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;

public class KlassSource {
    private final String name;
    private final Map<String, Klass> klasses;
    private final Map<String, byte[]> resources;

    public KlassSource() {
        this(null);
    }

    public KlassSource(String name) {
        if (name == null) {
            name = "KlassSource-" + hashCode();
        }
        this.name = name;
        this.klasses = new HashMap<>();
        this.resources = new HashMap<>();
    }

    public Klass putClass(Klass klass) {
        Objects.requireNonNull(klass, "Klass must not be null");
        Objects.requireNonNull(klass.name, "Klass name must not be null");
        return klasses.put(klass.name, klass);
    }

    public byte[] putResource(String name, byte[] resource) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(resource, "resource must not be null");
        return resources.put(name, resource);
    }

    public Klass loadFromClassFile(InputStream is, int options) throws IOException {
        Objects.requireNonNull(is, "inputstream must not be null");

        Klass klass = new Klass(this);
        ClassReader cr = new ClassReader(is);
        cr.accept(klass, options);
        putClass(klass);
        return klass;
    }

    /**
     * Loads the given resource as a byte[]
     * 
     * @param name The name that should be given to the loaded resource
     * @param is An {@link InputStream} that provides the data for the resource
     * @throws IOException
     */
    public void loadResource(String name, InputStream is) throws IOException {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(is, "inputstream must not be null");

        byte[] data = is.readAllBytes();
        resources.put(name, data);
    }

    /**
     * Loads the classes and resources from a Jar file
     * 
     * @param is An {@link InputStream} that provides the data for the jar file
     * @param options The options to be class parser
     * @param loadDirectories Indicates whether files marked as directories should be read and saved as resources
     * @throws IOException
     */
    public void loadFromJar(InputStream is, int options, boolean loadDirectories) throws IOException {
        Objects.requireNonNull(is, "inputstream must not be null");

        // Use ZipInputStream instead of JarInputStream because JarInputStream consumes the manifest
        try (ZipInputStream jIn = new ZipInputStream(is)) {
            ZipEntry e;
            while ((e = jIn.getNextEntry()) != null) {
                String entryName = e.getName();
                if (entryName.endsWith(".class")) {
                    loadFromClassFile(jIn, options);
                } else if (e.isDirectory() == loadDirectories) {
                    loadResource(entryName, jIn);
                }
            }
        }
    }

    public boolean hasKlass(String klassName) {
        Objects.requireNonNull(klassName);
        return klasses.containsKey(klassName);
    }

    public boolean hasResource(String resourceName) {
        Objects.requireNonNull(resourceName);
        return resources.containsKey(resourceName);
    }

    public byte[] getResource(String resourceName) {
        Objects.requireNonNull(resourceName);
        if (resources.containsKey(resourceName)) {
            return resources.get(resourceName);
        } else {
            throw new IllegalArgumentException(resourceName + " does not exists in " + toString());
        }
    }

    public Klass getKlass(String klassName) {
        Objects.requireNonNull(klassName);
        if (klasses.containsKey(klassName)) {
            return klasses.get(klassName);
        } else {
            throw new IllegalArgumentException(klassName + " does not exists in " + toString());
        }
    }

    public Set<Klass> getKlasses() {
        return Collections.unmodifiableSet(new HashSet<>(klasses.values()));
    }

    @Override
    public String toString() {
        return name;
    }
}

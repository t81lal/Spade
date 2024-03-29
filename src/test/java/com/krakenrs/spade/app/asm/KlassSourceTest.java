package com.krakenrs.spade.app.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

public class KlassSourceTest {

    private Klass makeKlass(KlassSource source, String name) {
        Klass k = new Klass(source);
        k.superName = "java/lang/Object";
        k.name = name;
        return k;
    }

    @Test
    public void testPutResource() {
        KlassSource ks = new KlassSource();

        assertThrows(NullPointerException.class, () -> ks.putResource(null, new byte[0]));
        assertThrows(NullPointerException.class, () -> ks.putResource("resource", null));

        byte[] r1 = "R1".getBytes(), r2 = "R2".getBytes();

        assertEquals(null, ks.putResource("resource", r1));
        assertTrue(ks.hasResource("resource"));
        assertEquals(r1, ks.getResource("resource"));

        // Overwrite
        assertEquals(r1, ks.putResource("resource", r2));
        assertTrue(ks.hasResource("resource"));
        assertEquals(r2, ks.getResource("resource"));
    }

    @Test
    public void testPutClass() {
        KlassSource ks = new KlassSource();

        assertThrows(NullPointerException.class, () -> ks.putClass(null));
        assertThrows(NullPointerException.class, () -> ks.putClass(makeKlass(ks, null)));

        Klass k1 = makeKlass(ks, "K1"), k2 = makeKlass(ks, "K1");

        assertEquals(null, ks.putClass(k1));
        assertTrue(ks.hasKlass("K1"));
        assertEquals(k1, ks.getKlass("K1"));

        // Overwrite
        assertEquals(k1, ks.putClass(k2));
        assertTrue(ks.hasKlass("K1"));
        assertEquals(k2, ks.getKlass("K1"));
    }

    @Test
    public void testGetResource() {
        KlassSource ks = new KlassSource();

        assertThrows(NullPointerException.class, () -> ks.getResource(null));
        assertThrows(IllegalArgumentException.class, () -> ks.getResource("resource1"));

        byte[] r1 = "R1".getBytes(), r2 = "R2".getBytes();
        ks.putResource("resource1", r1);
        ks.putResource("resource2", r2);
        assertEquals(r1, ks.getResource("resource1"));
        assertEquals(r2, ks.getResource("resource2"));
    }

    @Test
    public void testGetKlass() {
        KlassSource ks = new KlassSource();

        assertThrows(NullPointerException.class, () -> ks.getKlass(null));
        assertThrows(IllegalArgumentException.class, () -> ks.getKlass("resource"));

        Klass k1 = makeKlass(ks, "K1"), k2 = makeKlass(ks, "K2");
        ks.putClass(k1);
        ks.putClass(k2);
        assertEquals(k1, ks.getKlass("K1"));
        assertEquals(k2, ks.getKlass("K2"));
    }

    @Test
    public void testContainsResource() {
        KlassSource ks = new KlassSource();
        ks.putResource("resource1", new byte[0]);
        assertTrue(ks.hasResource("resource1"));
        assertFalse(ks.hasResource("resource2"));
    }

    @Test
    public void testContainsKlass() {
        KlassSource ks = new KlassSource();
        ks.putClass(makeKlass(ks, "K1"));
        assertTrue(ks.hasKlass("K1"));
        assertFalse(ks.hasKlass("K2"));
    }

    @Test
    public void testLoadClass() throws IOException {
        InputStream thisClassInputStream = getClass().getResourceAsStream(getClass().getSimpleName() + ".class");
        KlassSource ks = new KlassSource();
        Klass k = ks.loadFromClassFile(thisClassInputStream,
                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        assertEquals(getClass().getCanonicalName().replace(".", "/"), k.name);
    }

    @Test
    public void testLoadJar() throws IOException {
        String[] expectedKlasses = { "com/krakenrs/spade/ir/type/MethodType",
                "com/krakenrs/spade/ir/type/Type",
                "com/krakenrs/spade/commons/collections/graph/algo/DepthFirstSearch$1",
                "com/krakenrs/spade/commons/collections/graph/Vertex", "com/krakenrs/spade/ir/type/SimpleTypeManager",
                "com/krakenrs/spade/commons/collections/graph/algo/TarjanScc",
                "com/krakenrs/spade/ir/type/ResolvedClassType", "com/krakenrs/spade/ir/type/ArrayType",
                "com/krakenrs/spade/ir/type/ObjectType", "com/krakenrs/spade/commons/collections/graph/Digraph",
                "com/krakenrs/spade/ir/type/PrimitiveType", "com/krakenrs/spade/ir/type/ClassType",
                "com/krakenrs/spade/ir/type/TypeParsingException",
                "com/krakenrs/spade/commons/collections/graph/algo/DepthFirstSearch$EdgeType",
                "com/krakenrs/spade/ir/type/ObjectType$NullType", "com/krakenrs/spade/ir/type/AnyClassType",
                "com/krakenrs/spade/ir/type/TypeManager",
                "com/krakenrs/spade/commons/collections/graph/algo/DepthFirstSearch$VertexColour",
                "com/krakenrs/spade/ir/type/UnresolvedClassType", "com/krakenrs/spade/ir/type/ValueType",
                "com/krakenrs/spade/commons/collections/graph/Edge",
                "com/krakenrs/spade/commons/collections/graph/algo/DepthFirstSearch" };

        String[] expectedResources = { "META-INF/maven/com.krakenrs/spade/pom.properties",
                "META-INF/maven/com.krakenrs/spade/pom.xml", "META-INF/MANIFEST.MF" };

        KlassSource ks = new KlassSource();
        ks.loadFromJar(getClass().getResourceAsStream("testJar1.jar"),
                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES, false);

        for (String expected : expectedKlasses) {
            assertTrue(ks.hasKlass(expected), expected + " not loaded");
        }

        for (String expected : expectedResources) {
            assertTrue(ks.hasResource(expected), expected + " not loaded");
        }
    }

    @Test
    public void testLoadJarWithDirectories() throws IOException {
        String[] expectedResources = { "com/", "com/krakenrs/", "META-INF/maven/com.krakenrs/",
                "com/krakenrs/spade/ir/", "META-INF/", "com/krakenrs/spade/commons/collections/", "com/krakenrs/spade/",
                "com/krakenrs/spade/commons/collections/graph/algo/", "META-INF/maven/",
                "META-INF/maven/com.krakenrs/spade/", "com/krakenrs/spade/commons/",
                "com/krakenrs/spade/commons/collections/graph/", "com/krakenrs/spade/ir/type/" };

        KlassSource ks = new KlassSource();
        ks.loadFromJar(getClass().getResourceAsStream("testJar1.jar"),
                ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES, true);

        for (String expected : expectedResources) {
            assertTrue(ks.hasResource(expected), expected + " not loaded");
        }
    }
}

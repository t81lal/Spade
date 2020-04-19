package com.krakenrs.spade.testing.invariants;

import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.COLON;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.COMMA;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.EOF;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.FLOAT_LIT;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.INT_LIT;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.LBRACE;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.LBRACKET;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.NL;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.RBRACE;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.RBRACKET;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.STRING_LIT;
import static com.krakenrs.spade.testing.invariants.Lexer.TokenType.WORD;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.krakenrs.spade.commons.collections.graph.Edge;
import com.krakenrs.spade.commons.collections.graph.Vertex;
import com.krakenrs.spade.testing.invariants.GraphAssertionChecker.PropTime;
import com.krakenrs.spade.testing.invariants.Lexer.TokenType;
import com.krakenrs.spade.testing.invariants.json.JsonArray;
import com.krakenrs.spade.testing.invariants.json.JsonBool;
import com.krakenrs.spade.testing.invariants.json.JsonNull;
import com.krakenrs.spade.testing.invariants.json.JsonNumber;
import com.krakenrs.spade.testing.invariants.json.JsonObject;
import com.krakenrs.spade.testing.invariants.json.JsonString;
import com.krakenrs.spade.testing.invariants.json.JsonValue;

public class Parser<V extends Vertex, E extends Edge<V>> {

    enum EntityType {
        NODE, EDGE
    }

    class Entity<T> {
        Map<PropTime, JsonObject> attrs = new HashMap<>();
        String id1, id2;
        // Used for node
        T element;
    }

    class ParserState {
        Map<String, Entity<V>> nodes = new LinkedHashMap<>();
        Set<Entity<E>> edges = new HashSet<>();
        Entity<?> cur;

        V getV(String id) throws ParsingException {
            if (nodes.containsKey(id)) {
                Entity<V> e = nodes.get(id);
                if (e.element != null) {
                    return e.element;
                }
            }
            lexer.error("No node for id '" + id + "'");
            return null;
        }
    }

    private final Function<Integer, V> vertexSupplier;
    private final BiFunction<V, V, E> edgeSupplier;
    private final Lexer lexer;
    
    public Parser(char[] chars, Function<Integer, V> vertexSupplier, BiFunction<V, V, E> edgeSupplier)
            throws ParsingException {
        this.vertexSupplier = vertexSupplier;
        this.edgeSupplier = edgeSupplier;

        this.lexer = new Lexer(chars);
        this.lexer.next();
        // Do this after the first token in case the contents is skipped which would leave a NL as the current token
        this.lexer.setTokenNL(true);
    }

    private GraphAssertionChecker<V, E> createChecker(ParserState state) throws ParsingException {
        Map<PropTime, Map<V, JsonObject>> vertexProps = new HashMap<>();
        Map<PropTime, Map<E, JsonObject>> edgeProps = new HashMap<>();

        Set<V> vertices = new HashSet<>();
        Set<E> edges = new HashSet<>();

        // Map each node to an int id to let the user create the node in a standard way
        int idCounter = 0;
        for (Entry<String, Entity<V>> nodeEntry : state.nodes.entrySet()) {
            V v = vertexSupplier.apply(idCounter++);
            Entity<V> entity = nodeEntry.getValue();
            entity.element = v;

            vertices.add(v);

            for (Entry<PropTime, JsonObject> attrEntry : entity.attrs.entrySet()) {
                vertexProps.computeIfAbsent(attrEntry.getKey(), (key) -> new HashMap<>()).put(v, attrEntry.getValue());
            }
        }

        for (Entity<E> entity : state.edges) {
            E e = edgeSupplier.apply(state.getV(entity.id1), state.getV(entity.id2));
            edges.add(e);

            for (Entry<PropTime, JsonObject> attrEntry : entity.attrs.entrySet()) {
                edgeProps.computeIfAbsent(attrEntry.getKey(), (key) -> new HashMap<>()).put(e, attrEntry.getValue());
            }
        }

        return new GraphAssertionChecker<>(vertexProps, edgeProps, vertices, edges);
    }

    public GraphAssertionChecker<V, E> parse() throws ParsingException {
        ParserState state = new ParserState();

        parseLine(state);
        while (lexer.token().equals(NL)) {
            while (lexer.token().equals(NL)) {
                accept(NL);
            }
            if (lexer.token().equals(EOF)) {
                break;
            }
            parseLine(state);
        }
        accept(EOF);

        return createChecker(state);
    }

    private void parseLine(ParserState state) throws ParsingException {
        TokenType token = lexer.token();
        String id1 = null, id2 = null;

        if (token.equals(WORD) || token.equals(INT_LIT)) {
            // Either PRE/POST or id
            id1 = parseId();
            PropTime pt = getPropTime(id1);
            if (pt != null) {
                if (state.cur == null) {
                    lexer.error("No entity for props");
                } else {
                    state.cur.attrs.put(pt, parseObject());
                }
                return;
            } else {
                // id
                token = lexer.token();
                if (token.equals(WORD) || token.equals(INT_LIT)) {
                    // edge
                    id2 = parseId();

                    Entity<E> e = new Entity<>();
                    e.id1 = id1;
                    e.id2 = id2;
                    state.edges.add(e);
                    state.cur = e;

                    return;
                } else if (token.equals(NL)) {
                    Entity<V> n = new Entity<>();
                    n.id1 = id1;
                    state.nodes.put(id1, n);
                    state.cur = n;

                    return;
                } else {
                    lexer.error("Unexpected token: " + token);
                    return;
                }
            }
        } else {
            lexer.error("Unexpected token: " + token);
            return;
        }
    }

    private PropTime getPropTime(String name) {
        for (PropTime pt : PropTime.values()) {
            if (pt.name().equalsIgnoreCase(name)) {
                return pt;
            }
        }
        return null;
    }

    private String parseId() throws ParsingException {
        TokenType token = lexer.token();
        String lexeme = lexer.lexeme();
        if (token.equals(WORD) || token.equals(INT_LIT)) {
            accept(token);
        }
        return lexeme;
    }

    private JsonObject parseObject() throws ParsingException {
        accept(LBRACE);
        JsonObject obj = new JsonObject();
        members(obj);
        accept(RBRACE);
        return obj;
    }
    
    private JsonArray parseArray() throws ParsingException {
        accept(LBRACKET);
        JsonArray array = new JsonArray();
        elements(array);
        accept(RBRACKET);
        return array;
    }
    
    private void elements(JsonArray array) throws ParsingException {
        if(!lexer.token().equals(RBRACKET)) {
            array.add(value());
            while(lexer.token().equals(COMMA)) {
                accept(COMMA);
                array.add(value());
            }
        }
    }
    
    private JsonValue value() throws ParsingException {
        TokenType token = lexer.token();
        String lexeme = lexer.lexeme();
        
        if(token.equals(LBRACE)) {
            return parseObject();
        } else if(token.equals(LBRACKET)) {
            return parseArray();
        } else if(token.equals(STRING_LIT)) {
            accept(STRING_LIT);
            if(lexeme.equals("true")) {
                return new JsonBool(true);
            } else if(lexeme.equals("false")) {
                return new JsonBool(false);
            } else if(lexeme.equals("null")) {
                return new JsonNull();
            } else {
                return new JsonString(lexeme);
            }
        } else if(token.equals(INT_LIT)) {
            accept(INT_LIT);
            return new JsonNumber(Integer.parseInt(lexeme));
        } else if(token.equals(FLOAT_LIT)) {
            accept(FLOAT_LIT);
            return new JsonNumber(Float.parseFloat(lexeme));
        } else {
            lexer.error("Unexpected token: " + token);
            return null;
        }
    }
    
    private void members(JsonObject obj) throws ParsingException {
        if(!lexer.token().equals(RBRACE)) {
            member(obj);
            while(lexer.token().equals(COMMA)) {
                accept(COMMA);
                member(obj);
            }
        }
    }
    
    private void member(JsonObject obj) throws ParsingException {
        String key = lexer.lexeme();
        accept(Set.of(STRING_LIT, WORD));
        accept(COLON);
        obj.put(key, value());
    }
    
    private void accept(Set<TokenType> expected) throws ParsingException {
        if (!expected.contains(lexer.token())) {
            lexer.error(String.format("Expected %s, got %s", expected, lexer.token()));
        } else {
            lexer.next();
        }
    }

    private void accept(TokenType expected) throws ParsingException {
        if(!lexer.token().equals(expected)) {
            lexer.error(String.format("Expected %s, got %s", expected, lexer.token()));
        } else {
            lexer.next();
        }
    }
}

# Kraken Spade

Spade is a toolkit for facilitating the analysis and modification of compiled Java applications. It can process entire applications including library code to provide extra contextual information to the analyses. The idea is to create a high level, stackless intermediate representation of the Java bytecode language which can then be analysed and transformed in a much easier and more predictable way.
A lof of the ideas in this project as heavily influenced from earlier work done in [Maple-IR](https://github.com/LLVM-but-worse/maple-ir) and the creation of Spade can be almost fully attributed to various pain-points we identified in maple-ir as it grew and became more unmaintainable.

Contrasting directly with maple-ir for a moment, Spade provides the following benefits:
 * Extensive unit testing of core components, aiming towards 100% coverage, including a graph algorithm invariant framework
 * Pipeline transformation framework for facilitating higher order component composition
 * Dependency injection integration(via [Guice](https://github.com/google/guice)) and containerisation
 * Immutable CodeUnit's with predictable hashCode/equals
 * User-focused OO design including code visitors and reducers/folding
 * No edge cases for users to consider when implementing transformers
 * and much more (coming soon)!

## Planned Features
 - Bytecode to SSA IR generator
 - SSA IR to bytecode backtranslater (using [Boissinot et al.]((https://hal.inria.fr/inria-00349925/file/RR.pdf)) style destructor)
 - Text based language for IR
 - Dataflow analysis framework using [Soufflé](https://souffle-lang.github.io/index.html)


# CIPM Metamodels

This repository contains and defines EMF metamodels for CIPM:

* [Lua Grammar and Metamodel](#lua-grammar-and-metamodel)
* [Instrumentation Metamodel](#instrumentation-metamodel)

## Lua Grammar and Metamodel

Located in `org.xtext.lua`, CIPM provides an Xtext grammar for the Lua programming language, originally based on the Xtext grammar from https://melange.inria.fr/. From the grammar, a corresponding Lua metamodel, parser, and serializer are generated. The parser is able to create Lua models from Lua source code, while the serializer outputs Lua source code from a Lua model.

## Instrumentation Metamodel

Located in `tools.cipm.models.instrumentation`, the instrumentation metamodel defines instrumentation points for SEFF actions in the Palladio Component Model.

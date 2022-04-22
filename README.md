# Logic Proposition
> Parse and inspect propositional Logic Propositions

You must use **java 17** to run the jar.\
You may use intellij to leverage the existing config files.\
The entry point is in **Main.java::main**.

## Use

### Running
To run the jar, cd to the directory then `java -jar LogicProposition.jar <arguments>`

### Presets
There are some bundled example preset expressions that you can try.\
If you simply call the program with no arguments a list will be produced.\
To use one, take the index and run `java -jar LogicProposition.jar preset <index>`

### Custom text
You can also use custom text to be parsed as a proposition.\
To use, `java -jar LogicProposition.jar custom "<Content>"`
#### Operators

| Operator | Shorthands                                        |
|----------|---------------------------------------------------|
| Not      | `~` `-` `not` `not true` `not true that` `untrue` |
| Implies  | `→` `->` `implies` `therefore` `then`             |
| Iff      | `<->` `=` `iff`                                   |
| And      | `∧` `&` `and` `but`                               |
| Or       | `∨` `&#124;` `or` `but`                           |
| Xor      | `^` `xor`                                         |
|          | `if` `it is`                                      |

You may also use parentheses `()` and square brackets `[]` to isolate propositions.\
Note that currently `-a or b` parses to `-(a or b)` so you need to `(-a) or b`

### Conversions
You me also request your expression to be expressed only in terms of negations and conjunctions or negations and disjunctions.

This is done by adding an `And` or `Or` at the end of the command.
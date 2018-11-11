# Prim

Un service d'impression en Kotlin

## Submodules

### [Models](models#readme)

* Pure kotlin data models

### [Printer](printer#readme)

* Unopinionated logic layer
* Includes `:models`

### [Printer SQL](printer-sql#readme)

* SQL repository implementation
* Includes `:models`, `printer`

### [Server](server#readme)

* Sample backend printing service
* Includes `:models`, `printer`, `printer-sql`
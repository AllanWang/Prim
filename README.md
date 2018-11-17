# Prim

Un service d'impression en Kotlin

[![Build Status](https://api.travis-ci.com/AllanWang/Prim.svg?branch=master)](https://travis-ci.cim/AllanWang/Prim)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/AllanWang/Prim/master/LICENSE)

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
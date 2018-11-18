# Prim

Un service d'impression en Kotlin

---

Prim is available on JitPack

[![](https://jitpack.io/v/ca.allanwang/prim.svg)](https://jitpack.io/#ca.allanwang/prim)
[![Build Status](https://api.travis-ci.com/AllanWang/Prim.svg?branch=master)](https://travis-ci.com/AllanWang/Prim)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/AllanWang/Prim/master/LICENSE)

To apply, add the following to your root build.gradle:

```gradle
repositories {
    ...
    jcenter()
    maven { url "https://jitpack.io" }
    ...
}
```

Next, you can add whatever dependencies you want:

```gradle 
dependencies {
    compile "ca.allanwang.prim:models:$prim_version"
    compile "ca.allanwang.prim:printer:$prim_version"
    compile "ca.allanwang.prim:printer-sql:$prim_version"
}
```

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
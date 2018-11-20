# Prim :printer

> Unopinionated logic layer

The printer module serves to house all the logic that goes on in Prim.
It is however not self sufficient, as a lot of configuration is made available
through dependency injection. Most notably:

* PrinterConfiguration - a bunch of printer logic depends on components beyond the scope of Prim, 
and is therefore left to the implementor.
* Repositories - a collection of repository interfaces is made available for implementation. 
They typically map to databases, though there is no further constraint beyond that the interfaces must be thread safe.
Every repository has a way of adding and deleting an item by a key, and each has their own specific helper functions.
Read the KDocs for more info.

## Creating Repositories

The repository interfaces can be found [here](https://github.com/AllanWang/Prim/blob/master/printer/src/main/kotlin/ca/allanwang/prim/printer/Repositories.kt).
Simply implement each one and expose them to a koin module for others to use.
Before using in production, make sure to test the implementations against the existing test suite, found [here](https://github.com/AllanWang/Prim/tree/master/printer/src/test/kotlin/ca/allanwang/prim/printer/repos).
To use them, add the following dependency to your module's gradle file:

```gradle
dependencies {
    ...
    testCompile("ca.allanwang.prim:printer:$prim_version", configuration: 'testClasses')
}
```

Next, extend any of the test suites and add junit extensions to inject the necessary koin modules.
In some cases, you may need to make more repositories to expose more methods for the test methods.
The benefit here is that all the main logic and tests are written for you. 
You are free to add more tests in your own class on top of the existing ones.
See the [sql tests](https://github.com/AllanWang/Prim/tree/master/printer-sql/src/test/kotlin/ca/allanwang/prim/printer/sql/repos) for an example.

## Test Suite Format

The following is the general test format for each [Repo]

* TestBase - Basic shared parent for all test suites.
* [Repo]TestHelperBase - Koin injections and helper methods.
    * In most cases, there are `create` methods that associate model creations with a single int id input.
    All fields are unique with unique id inputs, though fields can be overridden.
    This helps simplify the testing process.
* [Repo]TestBase - Collection of tests specific to the injected repos.
If you wish to test your repo implementations, extend this class.
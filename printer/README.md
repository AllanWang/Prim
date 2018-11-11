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
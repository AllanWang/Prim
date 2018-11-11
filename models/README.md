# Prim :models

> Pure kotlin data models

This submodule is intended to house the models used by Prim, 
without any external dependencies.
Classes are all immutable data models.

Note that in some instances, there is both a JSON model and a "specific" model.
The intention here is to [make impossible states impossible](https://www.youtube.com/watch?v=IcgmSRJHu_8)
when working with logic, but also to offer an easy way to make models compatible with network requests.
In such cases, json models have the suffix `JSON`, whereas specific models have the regular naming scheme,
and are typically [sealed classes](https://kotlinlang.org/docs/reference/sealed-classes.html) 
or classes containing other models.

It is also worth noting that we have adopted the experimental [inline classes](https://kotlinlang.org/docs/reference/inline-classes.html)
for important fields. 
The goal is to avoid accidentally mixing up fields with similar types.

For non Kotlin developers, feel free to also check out the features of [data classes](https://kotlinlang.org/docs/reference/data-classes.html).
Most notably, they have structural equality and can be tweaked through `.copy()`
# Without OntoVCS #

## Diffing ##

```
> git diff
diff --git a/pizza.owl b/pizza.owl
index 5ed7779..437e47b 100644
--- a/pizza.owl
+++ b/pizza.owl
@@ -1,1656 +1,4611 @@ 
+<?xml version="1.0"?>
+<!DOCTYPE Ontology [
...
THOUSANDS OF CHANGES HERE
```

## Merging ##

Almost impossible:

<div><img src='https://dl.dropbox.com/u/62722148/img/ontovcs/impossible.png' border='0' width='80%'></img></div>

# With OntoVCS #

## Diffing ##

```
> owl2enable
Enabling OntoVCS for this Git repository
Done

> git difftool

Total additions: 4
Total removals: 2
Ontology format changed to: OWL/XML
New:
    NamedIndividual: pizza:Russia
Modified:
    Class: pizza:Country
    Class: owl:Thing
    NamedIndividual: pizza:America
    NamedIndividual: pizza:England
    NamedIndividual: pizza:France
    NamedIndividual: pizza:Germany
    NamedIndividual: pizza:Italy

- OntologyFormat("OWL Functional Syntax")
+ OntologyFormat("OWL/XML")
- DifferentIndividuals(pizza:America pizza:England
    pizza:France pizza:Germany pizza:Italy )
+ ClassAssertion(owl:Thing pizza:Russia)
+ ClassAssertion(pizza:Country pizza:Russia)
+ DifferentIndividuals(pizza:America pizza:England
    pizza:France pizza:Germany pizza:Italy pizza:Russia )
```

## Merging ##

Simple three-way merge tool:

<div><img src='https://dl.dropbox.com/u/62722148/img/ontovcs/owl2merge.png' border='0' width='80%'></img></div>
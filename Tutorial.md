# Installation #

  1. [Download](http://code.google.com/p/ontovcs/downloads/list) the latest version.
  1. Unpack anywhere.
  1. Add `bin` directory to your PATH.
> > If you are on Windows, `setup.cmd` will do this for you. You will need to log off and then log on again for the changes to take effect.
  1. Type `owl2diff`. You should see the usage hint.
```
> owl2diff
Usage: owl2diff parent.owl child.owl [--format (-f) format] [--measure (-m)] [--summary (-s)] [--wait (-w)]
 parent.owl           : Parent version
 child.owl            : Child version
 --format (-f) format : Format of changes: Functional or Pythonic
 --measure (-m)       : Measure time spent
 --summary (-s)       : Display changes summary
 --wait (-w)          : Do not exit, wait until user presses Enter
```
> > If you see an error, type `java` to see if java is in PATH. If it is not, add your Java bin directory to PATH.
  1. Download [SWT](http://www.eclipse.org/swt/) for your operating system and unpack it to `ontovcs/lib/swt/` so that `swt.jar` is inside `ontovcs/lib/swt/` directory.
  1. Type `owl2merge`. You should see the usage hint and an `owl2merge` window opened. Close the window.

# Repository configuration #

  1. Create a repository and `cd` to it.
> > Mercurial:
```
hg init ontovcstest
cd ontovcstest
```
> > Git:
```
git init ontovcstest
cd ontovcstest
```
  1. Type `owl2enable`.
> > This will enable `owl2diff` and `owl2merge` for this repository.

# Usage #

  1. Create an owl-file and add it under version control.
> > For testing purposes there are some owl-files in the `sample` directory.
> > Just copy one of them to the repository and rename it to `pizza.owl`.
> > Then add it.
> > Mercurial:
```
hg add pizza.owl
```
> > Git:
```
git add pizza.owl
```
  1. Commit
> > Mercurial:
```
hg commit -m "pizza.owl added"
```
> > Git:
```
git commit pizza.owl -m "pizza.owl added"
```
  1. Change one of owl-files in the repository.
> > For example, replace the pizza.owl with another pizza-`*`.owl from the `samples` directory.
  1. Type `hg owl2diff` of `git difftool`.
> > You will see the changes summary, followed by full changes in functional syntax, each prefixed with `+` or `-`:
```
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

- OntologyFormat(OWL Functional Syntax)
+ OntologyFormat(OWL/XML)
- DifferentIndividuals(pizza:America pizza:England
    pizza:France pizza:Germany pizza:Italy )
+ ClassAssertion(owl:Thing pizza:Russia)
+ ClassAssertion(pizza:Country pizza:Russia)
+ DifferentIndividuals(pizza:America pizza:England
    pizza:France pizza:Germany pizza:Italy pizza:Russia )
```
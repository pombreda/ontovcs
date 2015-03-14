# Introduction #

OntoVCS is aimed at Semantic Web ontology engineers who used to use Version Control / Source Code Management Systems.
Many ontology developers/engineers already use VCS/SCM systems to manage changes in their ontologies. This toolset is intended to make their work easier.

# Details #

As VCS/SCM systems support tracking of file versions, the algorithms they use are based upon file comparison. The nature of ontologies is that an ontology is a model which can be represented in many [formats](http://www.w3.org/TR/owl2-overview/#sec-syn) (syntaxes). Even variants of a given ontology saved by different [editors](http://goo.gl/l2XuN) in a given format will likely differ. Moreover, the order of statements in an ontology does not play a role.
That is why most built-in tools like diff and merge are useless when working with ontologies. OntoVCS tries to reduce this discrepancy, being a substitute of text-based diff and merge tools.


# Limitations #

We develop usable tools for comparing and merging ontologies, but we cannot change the algorithms used by a specific VCS/SCM. So we cannot take advantage of our algorithms at the stages of transmitting and storing of data. If we aim at optimizing these stages, it would turn into a large-scale project of a complete version control system for ontologies, which would take long time to complete and mature.
# skuber-util

*Work In Progress*

Scala utilities for Kubernetes clusters. 
Builds on the [skuber](https://github.com/doriordan/skuber) Scala client library for Kubernetes.
This initial version features the abilty to load sets of Kubernetes resources from Yaml or Json sources into a Scala (i.e. skuber) model, without needing to specify the type of each resource - the Kubernetes type of each loaded resource is determined dynamically by examining the resource kind and apiVersion fields, and uses that to parse the resource into the appropriate Skuber model class. See the YamlLoadSpec test as an example.

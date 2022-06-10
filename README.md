# checkstylerr

This repository contains 20M+ Checkstyle violations mined from 600+ GitHub projects.

The violations were collected in the context of the research project Styler (see [Styler: learning formatting conventions to repair Checkstyle violations](http://arxiv.org/pdf/1904.01754)).

## Organization of the data

Each branch of this repository contains Checkstyle violations from one GitHub project and is structured as follows:

```
├── <commit SHA>
│   ├── <violation_id, per file, starting with 0>
│   │   ├── <java file>: the Java file containing violations.
│   │   └── violations.json: a json file containing specific information about the violations, including location, severity, message, and Checkstyle rule.
│   ├── ...
├── ...
├── checkstyle.xml: the Checkstyle ruleset of the project, which was used to detect the violations.
└── info.json: a json file containing key information about the violation collection process, such as repository URL, branch, and Checkstyle version.
```

To download the data, one can use the scripts `scripts/utils/checkout_all.py` and `scripts/utils/checkout_project.py`. Warning: 20G of data.

## Related research project

If one uses checkstylerr data, one should cite the following paper:

```bibtex
@article{styler2022,
    title = {Styler: learning formatting conventions to repair Checkstyle violations},
    author = {Benjamin Loriot and Fernanda Madeiral and Martin Monperrus},
    journal = {Empirical Software Engineering, Springer},
    year = {2022},
    url = {http://arxiv.org/pdf/1904.01754},
}
```

# Waltz

In a nutshell Waltz allows you to visualize and define your organisation's technology landscape. Think of it like a structured Wiki for your architecture.


Learn more
  - [Features](docs/features/README.md)
  - [Product Site](https://waltz.finos.org/)
  - [FINOS Announcement](https://www.finos.org/blog/introduction-to-finos-waltz) - now part of the [Linux Foundation](https://www.linuxfoundation.org/blog/2020/04/finos-joins-the-linux-foundation/)

Getting started
 - [Building](docs/development/build.md) 
 - [Running](waltz-web/README.md)

---

[![Build Status](https://travis-ci.org/finos/waltz.svg?branch=master)](https://travis-ci.org/finos/waltz) 
[![Language Grade: JavaScript](https://img.shields.io/lgtm/grade/javascript/g/khartec/waltz.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/khartec/waltz/context:javascript) 
[![Language Grade: Java](https://img.shields.io/lgtm/grade/java/g/khartec/waltz.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/khartec/waltz/context:java)
[![FINOS - Active](https://cdn.jsdelivr.net/gh/finos/contrib-toolbox@master/images/badge-active.svg)](https://finosfoundation.atlassian.net/wiki/display/FINOS/Active)

## Corporate Contributors

Org | | Notes
--- | --- |---
![DB Logo](https://avatars1.githubusercontent.com/u/34654027?s=30&v=4 "Deutsche Bank") | Deutsche Bank | [press release](https://www.db.com/newsroom_news/2018/deutsche-bank-takes-next-step-in-open-source-journey-en-11484.htm) 
![NWM Logo](https://avatars2.githubusercontent.com/u/54027700?s=30&v=4 "Natwest Markets") | NatWest Markets | [press release](https://www.nwm.com/about-us/media/articles/natwest-markets-to-expand-open-source-coding)

## Technology Stack

### Server

- Java 8
- Embedded Jetty or WAR file (tested on Tomcat 7/8)
- Spark framework
- JDBC via JOOQ

See [pom.xml](https://github.com/finos/waltz/blob/master/pom.xml) for a full list of Java dependencies.


### Supported Databases

- MariaDB
- Postgres 
- Microsoft SQL Server (2012+)  
  - requires [JOOQ Pro license](https://www.jooq.org/download/) to build from source


### Client

- Browser based
    - IE 10+, Chrome, Safari, Firefox)
- AngularJS 1.7
- Bootstrap 3
- D3 

See [package.json](https://github.com/finos/waltz/blob/master/waltz-ng/package.json) for full list of javascript dependencies.

## Roadmap

Checkout [the project milestones](https://github.com/finos/waltz/milestones) and browse through the Todo, work in progress and done issues.

## Contributing

1. Fork it (<https://github.com/finos/waltz/fork>)
2. Create your feature branch (`git checkout -b feature/fooBar`)
3. Read our [contribution guidelines](.github/CONTRIBUTING.md) and [Community Code of Conduct](https://www.finos.org/code-of-conduct)
4. Commit your changes (`git commit -am 'Add some fooBar'`)
5. Push to the branch (`git push origin feature/fooBar`)
6. Create a new Pull Request

_NOTE:_ Commits and pull requests to FINOS repositories will only be accepted from those contributors with an active, executed Individual Contributor License Agreement (ICLA) with FINOS OR who are covered under an existing and active Corporate Contribution License Agreement (CCLA) executed with FINOS. Commits from individuals not covered under an ICLA or CCLA will be flagged and blocked by the FINOS Clabot tool. Please note that some CCLAs require individuals/employees to be explicitly named on the CCLA.

*Need an ICLA? Unsure if you are covered under an existing CCLA? Email [help@finos.org](mailto:help@finos.org)*

## Contributors

All contributions should contain the standard license and copyright notice (see [Notice file](NOTICE.md)).  

Individual and organisational contributors are listed in [the contributors file](CONTRIBUTORS.md)

## External Resources

[Using ArchUnit to formalize architecture rules in the Waltz codebase](https://medium.com/@davidwatkins73/using-archunit-to-formalize-architecture-rules-in-the-waltz-code-base-5fd3e092fc22)

## License

Copyright (C) 2020 Waltz open source project

Distributed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).

SPDX-License-Identifier: [Apache-2.0](https://spdx.org/licenses/Apache-2.0)

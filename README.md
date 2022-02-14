
# Data Prepper

This project has moved to [OpenSearch Data Prepper](https://github.com/opensearch-project/data-prepper).

You are currently viewing the inactive and archived OpenDistro Data Prepper.
All work is now happening in the [OpenSearch Data Prepper](https://github.com/opensearch-project/data-prepper)
project. The OpenSearch Data Prepper can send events to OpenSearch, OpenDistro, and ElasticSearch 7.x.
The OpenSearch Data Prepper already has new features and improvements, with many
more planned.

The last version of OpenDistro Data Prepper was version 1.0.3 which was
released December 2021 with log4j security patches.

To help you migrate to [OpenSearch Data Prepper](https://github.com/opensearch-project/data-prepper),
we have a short migration guide below.

## Migrating to OpenSearch Data Prepper

This section provides instructions for migrating from
the OpenDistro Data Prepper to OpenSearch Data Prepper.

### Change your Pipeline Configuration

The `elasticsearch` sink has changed to `opensearch`. You will
need to change your existing pipeline to use the `opensearch` plugin
instead of `elasticsearch`.

Please note that while the plugin is titled `opensearch` it remains compatible
with OpenDistro and ElasticSearch 7.x.

### Update Docker Image

The OpenDistro Data Prepper Docker image was located at `amazon/opendistro-for-elasticsearch-data-prepper`.
You will need to change this value to `opensearchproject/opensearch-data-prepper`.

# Old README

The remainder of this document is the old README file.

## Table of Contents

- [Overview](docs/readme/overview.md)
- Trace Analytics
    - [Overview](docs/readme/trace_overview.md)
    - [Trace Analytics Setup](docs/readme/trace_setup.md)
    - [Scaling and Tuning](docs/readme/trace_tuning.md)
- Project Details
    - [Setup](docs/readme/project_setup.md)
    - [Error Handling](docs/readme/error_handling.md)
    - [Logging](docs/readme/logs.md)
    - [Monitoring](docs/readme/monitoring.md)
    - [Contribute](#Contribute)
    - [Code of Conduct](#Code-of-Conduct)
    - [Security Issue Notifications](#Security-Issue-Notifications)
    - [License](#License)


## Contribute

We invite developers from the larger Open Distro community to contribute and help improve test coverage and give us feedback on where improvements can be made in design, code and documentation. You can look at  [contribution guide](CONTRIBUTING.md) for more information on how to contribute.

## Code of Conduct

This project has adopted an [Open Source Code of Conduct](CODE_OF_CONDUCT.md).

## Security Issue Notifications

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.

## License

This library is licensed under the Apache 2.0 License. [Refer](LICENSE)

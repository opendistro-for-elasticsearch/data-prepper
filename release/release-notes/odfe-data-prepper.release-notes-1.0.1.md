## 2021-12-10 Version 1.0.1

---

### Security
* Fixes [CVE-2021-44228](https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44228) by using Apache Log4J 2.15.0. [#942](https://github.com/opendistro-for-elasticsearch/data-prepper/pull/942), [#944](https://github.com/opendistro-for-elasticsearch/data-prepper/pull/944)
* Run yum update on Docker images to get all security patches at the time of the Data Prepper Docker build. [#943](https://github.com/opendistro-for-elasticsearch/data-prepper/pull/943)

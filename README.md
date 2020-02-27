<p align="center">
  February 2020 DZ Interview Assessment
</p>

<p align="center">
  <a href="https://github.com/mycaule/dz-assessment/actions"><img src="https://github.com/mycaule/dz-assessment/workflows/CI/badge.svg?branch=master" alt="Build Status"></a>
  <br>
  <br>
</p>

### Pre-requisites

For the Python project,
```bash
npm install -g heroku
pip3 install metaflow pandas matplotlib notebook
```

For the Scala project, [`sbt`](https://www.scala-sbt.org/download.html).

### Exercise 1: Data Engineering

We used Python as a programming language.

**To run the batch script**

```bash
cd exercise1/
python3 stats.py show
python3 stats.py run --group country
python3 stats.py run --group user_id
jupyter-notebook stats.ipynb
```
See [`stats.ipynb`](exercise1/stats.ipynb) and [GitHub Actions logs](https://github.com/mycaule/dz-assessment/actions)

### Exercise 2: Software Engineering

We used Scala as a programming language.

The backend uses the Play framework, together with Slick to access a PostgreSQL database.
The frontend uses the Laminar framework for UI rendering.

**To build the project locally**

```bash
cd exercise2/
sbt stage

export JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/michel?user=michel&password="
sbt dev
sbt backend/run
```

**To deploy on Heroku**

```bash
heroku login
heroku apps:create dz-assessment --region eu
heroku addons:create heroku-postgresql:hobby-dev
heroku config:set APPLICATION_SECRET=mycoolsecret

sbt clean stage backend/deployHeroku
```
Documentation: https://documenter.getpostman.com/view/9909796/SzKVRJFf

### References

- [Netflix - Open-Sourcing Metaflow, a Human-Centric Framework for Data Science](https://netflixtechblog.com/open-sourcing-metaflow-a-human-centric-framework-for-data-science-fa72e04a5d9)
  - [`Netflix/metaflow/tutorials/02-statistics`](https://github.com/Netflix/metaflow/tree/master/metaflow/tutorials/02-statistics)
  - [`xbenji/deezer_data`](https://github.com/xbenji/deezer_data)
- [Heroku - Getting started with Scala and Play](https://devcenter.heroku.com/articles/getting-started-with-scala)
  - [Antoine Doeraene - Deploying a full stack Scala application on Heroku](https://medium.com/@antoine.doeraene/deploying-a-full-stack-scala-application-on-heroku-6d8093a913b3)
  - [`sherpal/full-scala-scala-heroku`](https://github.com/sherpal/full-scala-scala-heroku)
- [Philipp Keller - Tags: Database schemas](http://howto.philippkeller.com/2005/04/24/Tags-Database-schemas/), [performance tests](http://howto.philippkeller.com/2005/06/19/Tagsystems-performance-tests/)
- [Deezer developers - API explorer](https://developers.deezer.com/api/explorer)
- [Deezer developers - JavaScript SDK](https://developers.deezer.com/sdk/javascript/api)

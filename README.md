# flattening-the-curve

A full-stack Scala application powered by ZIO and Laminar.

Starting point was created with **[zio-app](https://github.com/kitlangton/zio-app)**.

I'm just consuming Kit Langtons great work and his workshop sessions to get somewhat acquainted with this powerful way of programming.

## Adapt non locale run

1. Change Config.scala in Frontend


## Run Manually (works!)

1. `sbt '~frontend/fastLinkJS'` in another tab.
2. `adminPassword=secret sbt '~backend/reStart'` in another tab.
3. `yarn install`
4. `yarn exec vite`
5. open `http://localhost:3000`

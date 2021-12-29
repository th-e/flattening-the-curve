# flattening-the-curve

A full-stack Scala application powered by ZIO and Laminar.

Created with **[zio-app](https://github.com/kitlangton/zio-app)**.

## Run with zio-app  (does not work for me!)

1. `zio-app dev`
2. open `http://localhost:3000`

## Run Manually (works!)

1. `sbt '~frontend/fastLinkJS'` in another tab.
2. `sbt '~backend/reStart'` in another tab.
3. `yarn install`
4. `yarn exec vite`
5. open `http://localhost:3000`

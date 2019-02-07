# JSON Schema Viewer

This directory includes enhanced version of [json-schema-viewer](https://github.com/jlblcc/json-schema-viewer), tool for visualizing JSON Schemas.

Changes:

- Replaced Ruby SASS with node-sass, Ruby is not needed anymore.
- Included layout tweaks from https://github.com/Opetushallitus/json-schema-viewer

## Build instructions

1. Install dependencies via Bower: `bower install`
2. Install dependencies via NPM: `npm install`
3. Build project via Grunt: `grunt prod`

- This will create a production version at
  _/resources/public/json-viewer_. The production version
  includes concatenated and minified js/css.
- Note: jQuery/JQuery Mobile are **not** included in the js builds, they will be included from Cloudflare CDN.

## Development

1. Generate `dev.html` using `grunt dev`
2. Launch web server, e.g. `python -m SimpleHTTPServer 9001`
3. Access dev version at `http://localhost:9001/dev.html`
4. Make changes to `json-schema-viewer.js` and `./lib/example.js`
5. Reload your browser to see the changes

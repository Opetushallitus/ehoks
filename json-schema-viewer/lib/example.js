(function($) {
  var JSON_SCHEMA_PATH = "/ehoks-virkailija-backend-freeze/doc/swagger.json";
  // uncomment next line (and override dev_schema.json with latest /ehoks-virkailija-backend/doc/swagger.json) when doing development
  // JSON_SCHEMA_PATH = "dev_schema.json";

  JSV.schema = JSON_SCHEMA_PATH;

  $("body").one("pagecontainershow", function(event, ui) {
    window
      .fetch(JSON_SCHEMA_PATH)
      .then(function(r) {
        return r.json();
      })
      .then(function(jsonSchema) {
        // add custom properties to vanilla JSON schema from swagger
        jsonSchema.id = "#ehoks";
        jsonSchema.title = "eHOKS";

        // defines which schema models we show at root level
        jsonSchema.properties = [
          "HOKS",
          "HOKSPaivitys",
          "HOKSKorvaus",
          "HOKSLuonti"
        ].reduce(function(result, key) {
          if (jsonSchema.definitions[key]) {
            result[key] = { $ref: "#/definitions/" + key };
          }
          return result;
        }, {});

        // add definition's identifier as title if not previously defined
        jsonSchema.definitions = Object.keys(jsonSchema.definitions).reduce(
          function(definitions, key) {
            definitions[key] = jsonSchema.definitions[key];
            if (!definitions[key].title) {
              definitions[key].title = key;
            }
            return definitions;
          },
          {}
        );

        // add enriched schema to JSON Schema Viewer's cache
        tv4.addSchema(JSON_SCHEMA_PATH, jsonSchema);

        JSV.init(
          {
            schema: JSV.schema
          },
          function() {
            // display schema version
            JSV.setVersion(tv4.getSchema(JSV.treeData.schema).version);
            // handle permalink
            if (window.jsvInitPath) {
              var node = JSV.expandNodePath(window.jsvInitPath.split("-"));

              JSV.flashNode(node);
              JSV.clickTitle(node);
            } else {
              JSV.resetViewer();
            }
          }
        );
      });
  });
})(jQuery);

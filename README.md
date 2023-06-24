# data-visualiser
A tool for easily generating visualisations from a database. A conceptual model is used to generate suggestions for an appropriate chart based on the selected information.

## To run from a release (v0.3+)
- Java 20 must be used when running the application.

Extract the correct zipped folder from the release and run the following command from within the extracted directory:
```
java --module-path ./lib --add-modules javafx.controls,javafx.fxml,javafx.web -jar data-visualiser.jar
```

## Connecting to a database
Once the app is running, the correct format of database information is:
- **URL:** jdbc:postgresql://localhost/database_name
- **Username:** username
- **Password:** password
- **Schema:** public

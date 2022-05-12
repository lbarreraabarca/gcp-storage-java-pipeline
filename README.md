# GCP Cloud Storage Data Pipeline

## Description

This service allows upload a local file to GCP cloud Storage bucket.

## Contract

```json
{
    "gcpCloudStorage": {
        "landingPath": "gs://my-bucket/test/data.csv",
        "localPath": "/my-local-path/data.csv"
    } 
}
```

## Environment Variables

```
export GOOGLE_APPLICATION_CREDENTIALS=[your-service-account-path]
export ENV_GCP_PROJECT_ID=[your-gcp-project-id]
export ENV_INPUT_CONTRACT=ewogICAgImdjcENsb3VkU3RvcmFnZSI6IHsKICAgICAgICAibGFuZGluZ1BhdGgiOiAiZ3M6Ly9teS1idWNrZXQvdGVzdC9kYXRhLmNzdiIsCiAgICAgICAgImxvY2FsUGF0aCI6ICIvbXktbG9jYWwtcGF0aC9kYXRhLmNzdiIKICAgIH0gCn0=
```

## How to use ?
```bash
mvn clean package -DskipTests
java -jar target/aws_s3_java_pipeline-1.0.jar
```
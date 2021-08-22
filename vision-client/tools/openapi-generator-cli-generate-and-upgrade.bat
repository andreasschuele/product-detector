rmdir generated /s /q
rmdir ..\src\main\generated\java /s /q
mkdir ..\src\main\generated\java

java -jar openapi-generator-cli-5.1.1.jar generate --auth "Authorization: Basic dXNlcjp1c2Vy" ^
  -i http://localhost:5080/api/v1/docs/v3^
  -g java ^
  -o generated ^
  --library native ^
  --api-package vision.client.generated.vision.client.api ^
  --model-package vision.client.generated.vision.client.model ^
  --invoker-package vision.client.generated.vision.client.invoker ^
  --group-id vision.client.generated.vision ^
  --artifact-id vision.client.generated.vision-openapi-client ^
  --artifact-version 0.0.1-SNAPSHOT
  
robocopy generated\src\main\java ..\src\main\generated\java *.java /s

rmdir generated /s /q
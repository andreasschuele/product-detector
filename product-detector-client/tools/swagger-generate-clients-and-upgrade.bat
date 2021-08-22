rmdir ..\src\api\product-detector /s /q
mkdir ..\src\api\product-detector

java -jar swagger-codegen-cli-3.0.26.jar generate --auth "Authorization: Basic ZGVtbzpkZW1vIyE=" -i http://localhost:5000/v3/api-docs -l typescript-axios -o ..\src\api\product-detector
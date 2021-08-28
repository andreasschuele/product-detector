# Product Detector

The Product Detector is a simple web application that supports the classification of products based on taken pictures. Users can create products and maintain additional information, as easily record product pictures for machine learning. The machine learning approach uses a lightweight CNN (MobileNetV2) and TensorFlow 2 for the classification task. The application is a proof of concept developed as part of a study program and is based on a food retailer scenario to classify fruits and vegetables at the checkout.

## Showcase

![alt text][showcase1]

## Features

- detect products based on taken pictures

- maintain products (add, edit, delete)

    - maintain a thumbnail product image

    - maintain product related notes

- record product pictures for training

- product detection (classification) training

- decoupled training and classification in a separate vision-server component  

## Components

- product-detector-client - The Product Detector web application client. Used technologies & tools: HTML, CSS, JavaScript, TypeScript, React, Webpack, swagger-codegen-cli

- product-detector-server - The Product Detector web application server. Used technologies & tools: Java, Spring Boot, Liquibase, PostgreSQL

- vision-server - This contains a set of components which handle the actual image classification based on MobileNetV2. The functionality is provided via a REST API. Used technologies & tools: Python, Flask, TensorFlow, Keras, tinydb 

- vision-client - This contains a client implementation for Java to communicate with the vision-server. Used technologies & tools: Java, openapi-generator-cli

- vision-client-cli - This contains a CLI implementation to communicate with the vision-server. Used technologies & tools: Java, picocli

## How to setup the Product Detector?

As the Product Detector is a set of different components that use different technology stacks you will need to install the respective tools and dependencies for each components. The following list provides a high-level overview of the steps required to build and run the application.

1. Start the vision-server:

    - Requirements:

        - Python 3.8.5
    
    - Install dependencies:

    - ```$ pip install -r requirements.txt ```

    - Start vision-server:

    - ```$ python vision_server.py ```

    - Setup an empty vision model:

        - ```$ curl -X POST "http://127.0.0.1:5080/api/v1/vision/model" -u admin:admin -H  "accept: application/json" -H  "Content-Type: application/json" -d "{\"modelName\":\"product-detector\"}" ```

    - Optionally you can use the Swagger UI http://127.0.0.1:5080/api/v1/docs/v3/swagger-ui/#/
        
2. Build the vision-client:

    - Requirements:

        - openjdk 15 2020-09-15

    - Build:

        - ```$ mvn package```

3. The Product Detector server:

    - Requirements:

        - openjdk 15 2020-09-15

    - Database - Either setup a PostgreSQL database or use Vagrant (operations/vagrant) and VirtualBox to setup a development environment.
    
    - Verify and update server settings defined in the 'application.yaml'.

    - Package and export dependencies:

        - ```$ mvn package```
        - ```$ mvn dependency:copy-dependencies -DoutputDirectory=target/library```    

    - Start server:

        - ```$ java -classpath "target\library\*;target\product-detector-server-1.0-SNAPSHOT.jar" productdetector.Application ```
    
        - Make sure that the 'development' Spring profile is active otherwise the 'RedirectToFrontendWebServerFilter' bean wont redirect to the local Product Detector client.

4. The Product Detector client:

    - Requirements:

        - node v12.18.3
        - npm 6.14.11

    - Install dependencies:

        - ```$ npm install```

    - Start client:

        - ```$ npm run-script start ```

5. Access Product Detector 'http://localhost:5000'

    On the localhost and with active 'development' Spring profile the RedirectToFrontendWebServerFilter bean should redirect to the Product Detector client development web server.

## Known issues:

* A crash or killing the vision-server process results in loosing added image data. - The vision-server uses tinydb, which wasn't a good choice due to it's slow performance handling large amount of data. To tackle the slow saving the vision-server only persists the tinydb file during the shutdown of the server.

* Accessing the Product Detector from a remote device requires an HTTPS setup of the Product Detector server otherwise the web-camera can't be accessed due to browser security.
	
## License

This repository is released under MIT license (see LICENSE.txt).

[showcase1]: assets/showcase1.gif "Product Detector showcase"
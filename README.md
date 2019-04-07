# Esenin Server

This project aims at unification of NLP tools. 
Each tool is placed in separate _docker_ container, which doesn't depend on the outside world 
and is only able to receive requests and return the results of text processing. 
All containers are handled by a centralized server, which also can handle the user HTTP requests. 
Each containers can be configured and don't need to be installed manually, 
user specify necessary containers and their configurations in one global config.

### Installation

##### Step 0
You will need [`java`](https://www.java.com/en/download/) to run the application 
and [`docker`](https://docs.docker.com/install/) for containers where NLP tools are stored. 
Also take a look at these [instructions](https://docs.docker.com/install/linux/linux-postinstall/#configure-docker-to-start-on-boot)
if you are having troubles with non-root permisions. 

##### Step 1

Download [the latest release](https://github.com/esenin-org/esenin-server/releases). 

##### Step 2

Create file `modules.conf` next to the downloaded archive. 
Take a look at [example of configuration](https://github.com/esenin-org/esenin-server/blob/master/modules.conf)
or [Configuration](#configuration) section
and specify the necessary modules in your config. 

##### Step 3

Run the application: `java -jar <downloaded_archive>`.

##### Step 4

Connect to the `esenin-server` via [wrapper for chosen programming language or manually](#usage). 

### Configuration

Config is written in [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md). 
It should contain one array called `modules`, where each module has `name`, `nlp-func` and `source` fields:
- `name` is an arbitrary string and it is used to locate the container.  
- `nlp-func` indicates what NLP function is implemented by this module.
- `source` specifies location of the image for `docker` container. Only `dockerhub` is supported for now. 

Example:
```hocon
{
  modules = [
    {
      name = "syntaxnet"
      nlp-func = "pos"
      source = "dockerhub:esenin/syntaxnet"
    }
    {
      name = "big-artm"
      nlp-func = "tm"
      source = "dockerhub:esenin/bigartm"
    }
  ]
}
```

### NLP tools

Currently supported tools:
- [SyntaxNet](https://github.com/tensorflow/models/tree/master/research/syntaxnet). 
It uses `esenin/syntaxnet` dockerhub image, sources of this image can be found [here](https://github.com/esenin-org/esenin-syntaxnet). It implements the `pos` function. 
- [BigArtm](https://github.com/bigartm/bigartm).
It uses `esenin/bigartm` dockerhub image, sources of this image can be found [here](https://github.com/esenin-org/esenin-bigartm). It implements the `tm` function.


### Usage

`esenin-server` accepts HTTP requests on `/nlp/<nlp-func>` endpoints.
Each function has its own JSON request format which is described in [NLP functions section](#nlp-functions).

For constructing these JSON requests and parse results it's recommended to use special wrappers for various programming languages:
- [`esenin-python`](https://github.com/esenin-org/esenin-python) for Python.
- [`esenin-cpp`](https://github.com/esenin-org/esenin-cpp) for C++.

### NLP functions

##### `pos`
Takes arbitrary _russian_ text and returns Part Of Speech tags.

Example request: 
```json
{
  "text": "Мама мыла раму."
}
``` 
Example response: 
```json
{
  "words": [
    {
      "word": "Мама",
      "connection_label": "nsubj",    
      "connection_index": 1,
      "pos": "NOUN++"
    },
    {
      "word": "мыла",
      "connection_label": "root",
      "connection_index": -1,
      "pos": "VERB++"
    },
    {
      "word": "раму",
      "connection_label": "obj",
      "connection_index": 1,
      "pos": "NOUN++"
    },
    {
      "word": ".",
      "connection_label": "punct",
      "connection_index": 2,
      "pos": "PUNCT++"
    }
  ]
}
```

#### `tm`

This function consists of two requests:
 - `fit` request that trains topic modeling algorithm.
   - It takes list of documents, where document is a list of terms, and number of topics. 
   - Trains topic modeling algorithm with given terms and number of topics.
   - Returns the id of trained model, it's used in `topics` request.
 - `topics` request that returns probability of each topic to be in each term
   - It takes id of trained topic model and a term.
   - Returns probabilities of a term to be in each topic.

Example `fit` request:
```json
{
  "terms": [["Мама", "мыла", "раму"], ["Мама", "мыла", "окно"], ["Мама", "мыла", "пол"]],
  "topics": 10
}
```  
Example `fit` response:
```json
{
  "id": "754b4def-0c2d-4a64-a7d8-4bedd9626471"
}
```

Example `topics` request:
```json
{
  "id": "754b4def-0c2d-4a64-a7d8-4bedd9626471",
  "term": "Мама"
}
```

Example `topics` response:
```json
{"topics": [0.04, 0.2, 0.15, 0.12, 0.15, 0.16, 0.05, 0.03, 0.1, 0.004]}
```





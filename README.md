# Esenin Server

This project aims at unification of NLP tools. 
Each tool is placed in separate _docker_ container, which doesn't depend on the outside world 
and is only able to receive requests and return the results of text processing. 
All containers are handled by a centralized server, which also can handle the user HTTP requests. 
Each containers can be configured and don't need to be installed manually, 
user specify necessary containers and their configurations at one global config.

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
or [Configuration](#Configuration) section
and specify the necessary modules in your config. 

##### Step 3

Run the application: `java -jar <downloaded_archive>`.

### Configuration

Config is written in [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md). 
It should contain one array called `modules`, where each module has `name`, `nlp-func` and `source` fields:
- `name` is an arbitrary string and it is used to locate the container.  
- `nlp-func` indicates what NLP function is implement by this module.
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
  ]
}
```

### NLP tools

Currently supported tools:
- [SyntaxNet](https://github.com/tensorflow/models/tree/master/research/syntaxnet). 
It uses `esenin/syntaxnet` dockerhub image. It implements the `pos` function. 


### Usage

While running `esenin-server` can accept HTTP-requests on `/nlp/<nlp-func>` addresses.
Each function has its own json request format which are described in [NLP functions section](#NLP-functions).

But it's more preferably to use special wrappers for various programming languages:
- [`esenin-python`](https://github.com/esenin-org/esenin-python) for Python.  

### NLP functions

##### `pos`
It takes arbitrary _russian_ text and returns Part Of Speech tags. 

Example request: 
```json
{
  "string": "Мама мыла раму."
}

``` 
Example response: 
```json
{
  "input":"Мама мыла раму.",
  "output":[
    {
      "word":"Мама",
      "label":"nsubj",
      "break_level":0,
      "category":"",
      "head":1,
      "tag":"attribute { name: \"Animacy\" value: \"Anim\" } attribute { name: \"Case\" value: \"Nom\" } attribute { name: \"Gender\" value: \"Fem\" } attribute { name: \"Number\" value: \"Sing\" } attribute { name: \"fPOS\" value: \"NOUN++\" } "
    },
    {
      "word":"мыла",
      "label":"root",
      "break_level":1,
      "category":"",
      "head":-1,
      "tag":"attribute { name: \"Aspect\" value: \"Imp\" } attribute { name: \"Gender\" value: \"Fem\" } attribute { name: \"Mood\" value: \"Ind\" } attribute { name: \"Number\" value: \"Sing\" } attribute { name: \"Tense\" value: \"Past\" } attribute { name: \"VerbForm\" value: \"Fin\" } attribute { name: \"Voice\" value: \"Act\" } attribute { name: \"fPOS\" value: \"VERB++\" } "
    },
    {
      "word":"раму",
      "label":"obj",
      "break_level":1,
      "category":"",
      "head":1,
      "tag":"attribute { name: \"Animacy\" value: \"Inan\" } attribute { name: \"Case\" value: \"Acc\" } attribute { name: \"Gender\" value: \"Fem\" } attribute { name: \"Number\" value: \"Sing\" } attribute { name: \"fPOS\" value: \"NOUN++\" } "
    },
    {
      "word":".",
      "label":"punct",
      "break_level":0,
      "category":"",
      "head":2,
      "tag":"attribute { name: \"fPOS\" value: \"PUNCT++\" } "
    }
  ]
}
```


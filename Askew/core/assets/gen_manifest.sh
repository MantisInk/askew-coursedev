#!/bin/bash
# This gets around an annoying namespacing issue with jars. - Trevor
# You can manually add the names of files in textures directories, but this 
# will do it for you :D
find texture/* -type f -exec echo {} \; | sed -E 's@(.*)@\1@g' > texture_manifest.txt

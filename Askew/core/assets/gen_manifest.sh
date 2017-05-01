#!/bin/bash
# This gets around an annoying namespacing issue with jars. - Trevor
# You can manually add the names of files in textures directories, but this 
# will do it for you :D
#find -regextype sed -regex "texture/^(?!packed$|animated$)(^.*$)/.png" -type f -exec echo {} \; | sed -E 's@(.*)@\1@g' > texture_manifest.txt
#find -regextype sed -regex "\./texture/(^.*$)/.*.png" -type f -exec echo {} \; | sed -E 's@(.*)@\1@g' > texture_manifest.txt
find -regextype posix-extended -regex '\./texture/(.*)/.*.png' -type f -exec echo {} \;  | sed -E 's@(.*)@\1@g; /animated/d; /packed/d; s@\./@@g' > texture_manifest.txt
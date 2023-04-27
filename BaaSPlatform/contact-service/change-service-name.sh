#!/bin/bash

name=$1
old_name=service_name

find ./helloworld-service/ -type f -exec sed -i -e "s/${old_name}/${name}/g" {} \;

#find ./ -name "$old_name" -exec bash -c "mv "$old_name" "${1%$old_name}"$name" - '{}' \;


for i in $(find ./helloworld-service/ -name service_name -type d) ; do
    mv "$i" "${i%$old_name}$name"
done

cd helloworld-service && pwd

rm -f .git/index
git add .
git commit -m "initial commit"
git push

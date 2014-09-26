#!/bin/sh
for BACKEND in scalaJson jawn json4s lift argonaut; do
  sed "s/BACKEND/$BACKEND/g" presrc/json.scala > src/$BACKEND.scala
done

#!/bin/sh
for BACKEND in scalaJson spray jawn json4s lift argonaut; do
  sed "s/BACKEND/$BACKEND/g" pre/json.scala > src/$BACKEND.scala
done

#!/bin/sh
for BACKEND in scalaJson spray play jawn json4s argonaut; do
  sed "s/BACKEND/$BACKEND/g" pre/json.scala > src/$BACKEND.scala
done

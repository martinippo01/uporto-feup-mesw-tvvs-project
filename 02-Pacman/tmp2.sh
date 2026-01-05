#!/bin/bash

PROJECT_NAME=$(basename "$(pwd)")

find src/test/java -name "*MutationTests.java" | while read -r test_file; do
  # Relative path from src/test/java
  relative_path=${test_file#src/test/java/}

  # Build production class path
  class_file="src/main/java/${relative_path%MutationTests.java}.java"

  # Only output if the production class exists
  if [[ -f "$class_file" ]]; then
    echo "$PROJECT_NAME,$test_file,$class_file"
  fi
done


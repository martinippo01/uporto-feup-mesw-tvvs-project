#!/bin/bash

PROJECT_NAME=$(basename "$(pwd)")

find src/main/java -name "*.java" | while read -r class_file; do
  # Relative path from src/main/java
  relative_path=${class_file#src/main/java/}

  # Build test path by switching base and appending WhiteBoxTests
  test_file="src/test/java/${relative_path%.java}WhiteBoxTests.java"

  # Only output if the test exists
  if [[ -f "$test_file" ]]; then
    echo "$PROJECT_NAME,$test_file,$class_file"
  fi
done


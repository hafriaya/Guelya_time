#!/bin/bash

# Create ghost commits for othmaneamaa
# Need 31 more commits to reach 49

AUTHOR_NAME="Othmane Amaadour"
AUTHOR_EMAIL="othmaneamaa@gmail.com"

# Create a temporary file to modify
echo "Ghost commit marker" > .ghost_marker

# November dates (spread across the month)
dates=(
  "2025-11-01 10:00:00"
  "2025-11-02 11:00:00"
  "2025-11-03 12:00:00"
  "2025-11-04 14:00:00"
  "2025-11-05 09:00:00"
  "2025-11-06 15:00:00"
  "2025-11-07 10:30:00"
  "2025-11-08 11:30:00"
  "2025-11-09 13:00:00"
  "2025-11-10 14:30:00"
  "2025-11-11 10:00:00"
  "2025-11-12 11:00:00"
  "2025-11-13 12:00:00"
  "2025-11-14 14:00:00"
  "2025-11-15 09:00:00"
  "2025-11-16 15:00:00"
  "2025-11-17 10:30:00"
  "2025-11-18 11:30:00"
  "2025-11-19 13:00:00"
  "2025-11-20 14:30:00"
  "2025-11-21 10:00:00"
  "2025-11-22 11:00:00"
  "2025-11-23 12:00:00"
  "2025-11-24 14:00:00"
  "2025-11-25 09:00:00"
  "2025-11-26 15:00:00"
  "2025-11-27 10:30:00"
  "2025-11-28 11:30:00"
  "2025-11-29 13:00:00"
  "2025-11-30 14:30:00"
  "2025-11-30 16:00:00"
)

for date in "${dates[@]}"; do
  # Modify the file slightly
  echo "Ghost commit - $date" >> .ghost_marker
  
  # Stage the file
  git add .ghost_marker
  
  # Create commit with specific date and author
  GIT_AUTHOR_NAME="$AUTHOR_NAME" \
  GIT_AUTHOR_EMAIL="$AUTHOR_EMAIL" \
  GIT_AUTHOR_DATE="$date" \
  GIT_COMMITTER_NAME="$AUTHOR_NAME" \
  GIT_COMMITTER_EMAIL="$AUTHOR_EMAIL" \
  GIT_COMMITTER_DATE="$date" \
  git commit -m "Ghost commit ${date:0:10}"
  
  echo "Created ghost commit for $date"
done

# Clean up the marker file
rm .ghost_marker
git add -A
git commit -m "Clean up ghost marker" --allow-empty

echo "Done! Created 31 ghost commits for othmaneamaa"

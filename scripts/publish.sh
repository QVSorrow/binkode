#!/usr/bin/env bash
#
# Release driver for binkode. Secret-free on purpose: it expects the signing key
# and (for a real release) the Central Portal credentials to already be present in
# the environment as ORG_GRADLE_PROJECT_* variables. For local use, run the
# gitignored scripts/publish.local.sh, which injects them from 1Password and then
# calls this script. A CI workflow can do the same from repo secrets.
#
# Usage:
#   scripts/publish.sh dry       # publishToMavenLocal - signs, no network, verifies .asc output
#   scripts/publish.sh release   # publishToMavenCentral - uploads as a PENDING deployment
#
set -euo pipefail

mode="${1:-}"
case "$mode" in
  dry)     task="publishToMavenLocal"   ;;
  release) task="publishToMavenCentral" ;;
  *) echo "usage: $0 {dry|release}" >&2; exit 2 ;;
esac

cd "$(dirname "$0")/.."

# The build only calls signAllPublications() when signingInMemoryKey is present;
# without it the run would silently produce unsigned artifacts that Central rejects.
: "${ORG_GRADLE_PROJECT_signingInMemoryKey:?signing key not set - run scripts/publish.local.sh instead}"
: "${ORG_GRADLE_PROJECT_signingInMemoryKeyPassword:?signing passphrase not set}"

if [[ "$task" == "publishToMavenCentral" ]]; then
  : "${ORG_GRADLE_PROJECT_mavenCentralUsername:?Central username not set}"
  : "${ORG_GRADLE_PROJECT_mavenCentralPassword:?Central password not set}"
  echo "This uploads io.github.qvsorrow:binkode to Maven Central as a PENDING deployment."
  echo "Nothing goes public until you press Publish in the Central Portal UI."
  read -r -p "Continue? [y/N] " reply
  [[ "$reply" == "y" || "$reply" == "Y" ]] || { echo "aborted"; exit 1; }
fi

./gradlew clean "$task"

if [[ "$task" == "publishToMavenLocal" ]]; then
  echo
  echo "Published to ~/.m2. Expect a matching .asc for every artifact:"
  ls -1 "$HOME"/.m2/repository/io/github/qvsorrow/binkode/*/ 2>/dev/null \
    || echo "  (nothing found - did the build publish?)"
else
  echo
  echo "Uploaded. Now open https://central.sonatype.com -> Deployments,"
  echo "review the pending binkode deployment, and press Publish."
fi

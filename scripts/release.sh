#!/usr/bin/env bash
# The decisions of the Release workflow, kept out of the YAML so release.test.sh can
# check them against a local origin and a fake gh.
#
#   release.sh checkout <tag>        check out <tag> if origin has it; otherwise stay put
#   release.sh publish <tag> <file>  create the release (and a missing tag, on HEAD), upload <file>
set -euo pipefail

usage="usage: release.sh checkout <tag> | publish <tag> <file>"
cmd=${1:?$usage}
tag=${2:?$usage}

case $cmd in
checkout)
	# ls-remote exits 2 for "no such tag". Anything else stops the run: a tag that exists
	# but could not be seen would get the DMG of this branch instead of its own commit.
	status=0
	git ls-remote --exit-code --tags origin "refs/tags/$tag" >/dev/null || status=$?
	case $status in
	0)
		git fetch -q --depth=1 origin "refs/tags/$tag:refs/tags/$tag"
		git checkout -q --detach "refs/tags/$tag"
		echo "Building the existing tag $tag ($(git rev-parse HEAD))"
		;;
	2) echo "No tag $tag yet, publish creates it on $(git rev-parse HEAD)" ;;
	*) exit "$status" ;;
	esac
	;;
publish)
	file=${3:?$usage}
	# A re-run finds the release it made and only replaces the file.
	if ! gh release view "$tag" >/dev/null 2>&1; then
		gh release create "$tag" --target "$(git rev-parse HEAD)" --title "Ofis $tag" --prerelease --generate-notes
	fi
	gh release upload "$tag" "$file" --clobber
	;;
*)
	echo "$usage" >&2
	exit 2
	;;
esac
